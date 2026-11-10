import asyncio
import io
import logging
import os
import re
import time
import uuid
from concurrent.futures import ThreadPoolExecutor
from typing import List, Optional

import cv2
import numpy as np
import pytesseract
from fastapi import FastAPI, Form, HTTPException
from pydantic import BaseModel
from pdf2image import convert_from_path

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("cv-engine")

app = FastAPI(
    title="Terrenos CV Engine",
    description="Microservicio de visión por computadora y OCR para extracción de lotes en planos inmobiliarios.",
    version="2.0.0"
)

UPLOAD_DIR = os.getenv("UPLOAD_DIR", "/app/uploads")
MAX_IMAGE_DIMENSION = int(os.getenv("MAX_IMAGE_DIMENSION", "8000"))
MIN_LOTE_AREA = int(os.getenv("MIN_LOTE_AREA", "1000"))
MAX_LOTE_AREA = int(os.getenv("MAX_LOTE_AREA", "5000000"))
THREAD_WORKERS = int(os.getenv("THREAD_WORKERS", "4"))

_executor = ThreadPoolExecutor(max_workers=THREAD_WORKERS)

# Tesseract config optimizado con whitelist de caracteres comunes en num de lote
TESSERACT_CONFIG = r'--oem 3 --psm 6 -c tessedit_char_whitelist=0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZLOTE-lt '


class Point(BaseModel):
    x: int
    y: int


class LoteDetectado(BaseModel):
    id_temporal: str
    numero_lote_detectado: str
    area_detectada: str
    confianza_ocr: float           # 0-100%
    poligono: List[Point]
    coordenadas_centro: Point


class ExtractionResult(BaseModel):
    archivo_procesado: str
    total_lotes_detectados: int
    lotes: List[LoteDetectado]
    tiempo_procesamiento_ms: float


def _resolve_upload_path(file_path: str) -> str:
    """
    Resuelve de forma segura la ruta del plano dentro de UPLOAD_DIR.
    Previene ataques de Path Traversal y soporta fallback a la raíz de UPLOAD_DIR
    cuando ArchivoService almacena el archivo sin subdirectorio de proyecto.
    """
    if not file_path or not file_path.strip():
        raise HTTPException(status_code=400, detail="Ruta de archivo vacía")

    upload_dir = os.path.abspath(os.getenv("UPLOAD_DIR", UPLOAD_DIR))
    candidate = os.path.abspath(os.path.join(upload_dir, file_path))

    try:
        in_upload_dir = os.path.commonpath([upload_dir, candidate]) == upload_dir
    except ValueError:
        in_upload_dir = False

    if not in_upload_dir or candidate == upload_dir:
        raise HTTPException(status_code=400, detail="Ruta de archivo inválida o fuera del directorio permitido")

    if os.path.isfile(candidate):
        return candidate

    # Fallback: ArchivoService guarda archivos directamente en UPLOAD_DIR/<nombreAlmacenado>
    basename = os.path.basename(file_path)
    if basename:
        fallback = os.path.abspath(os.path.join(upload_dir, basename))
        if os.path.commonpath([upload_dir, fallback]) == upload_dir and os.path.isfile(fallback):
            logger.info("Archivo resuelto mediante fallback en raíz de uploads: %s", fallback)
            return fallback

    raise HTTPException(status_code=404, detail=f"Archivo no encontrado: {file_path}")


def _load_image(full_path: str) -> np.ndarray:
    """Carga imagen desde disco; si es PDF, convierte la primera página a imagen."""
    ext = os.path.splitext(full_path)[1].lower()
    if ext == ".pdf":
        try:
            pages = convert_from_path(full_path, first_page=1, last_page=1)
            if not pages:
                raise ValueError("El archivo PDF no contiene páginas leíbles.")
            pil_image = pages[0]
            open_cv_image = np.array(pil_image)
            # Convertir RGB a BGR para OpenCV
            return cv2.cvtColor(open_cv_image, cv2.COLOR_RGB2BGR)
        except Exception as e:
            logger.error("Error al convertir PDF %s: %s", full_path, e)
            raise ValueError(f"No se pudo procesar el archivo PDF: {e}")
    else:
        image = cv2.imread(full_path)
        if image is None and os.path.isfile(full_path):
            try:
                raw = np.fromfile(full_path, dtype=np.uint8)
                if raw.size > 0:
                    image = cv2.imdecode(raw, cv2.IMREAD_COLOR)
            except Exception:
                image = None
        if image is None:
            raise ValueError(f"No se pudo leer la imagen en {full_path}. Formato inválido o corrupto.")
        return image


def _preprocess_roi_for_ocr(roi_gray: np.ndarray) -> np.ndarray:
    """Preprocesa el ROI para maximizar la tasa de acierto del OCR Tesseract."""
    h, w = roi_gray.shape[:2]
    if h == 0 or w == 0:
        return roi_gray

    # Escalar hacia arriba si la región es muy pequeña (Tesseract responde mejor a fuentes >= 30px)
    if h < 40 or w < 40:
        scale = max(40 / h, 40 / w)
        roi_gray = cv2.resize(roi_gray, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)

    # Suavizado para reducir ruido de escaneo
    roi_gray = cv2.GaussianBlur(roi_gray, (3, 3), 0)

    # Binarización Otsu para contraste óptimo de texto
    _, roi_thresh = cv2.threshold(roi_gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    return roi_thresh


def _process_image_sync(full_path: str, min_area: int = MIN_LOTE_AREA) -> dict:
    """Procesamiento síncrono OpenCV + Tesseract optimizado."""
    start = time.perf_counter()

    image = _load_image(full_path)

    h, w = image.shape[:2]
    scale = 1.0
    if max(h, w) > MAX_IMAGE_DIMENSION:
        scale = MAX_IMAGE_DIMENSION / max(h, w)
        image = cv2.resize(image, None, fx=scale, fy=scale, interpolation=cv2.INTER_AREA)
        logger.info("Imagen redimensionada a max dimensión %d (%dx%d)", MAX_IMAGE_DIMENSION, int(w * scale), int(h * scale))

    inv_scale = 1.0 / scale
    area_scale = inv_scale * inv_scale

    # Convertir a escala de grises
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Umbralizado adaptativo para manejar variaciones de iluminación y fondos en planos
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)
    thresh = cv2.adaptiveThreshold(
        blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 11, 2
    )

    # Operación morfológica de cierre para sellar líneas de lotes
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    morph = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel, iterations=2)

    # Detección jerárquica de contornos para soportar lotes subdivididos dentro de manzanas
    contours, hierarchy = cv2.findContours(morph, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    max_valid_area = min(MAX_LOTE_AREA, int(gray.shape[0] * gray.shape[1] * 0.90))
    areas = [cv2.contourArea(c) for c in contours]
    valid_indices = {
        idx for idx, a in enumerate(areas)
        if min_area <= a <= max_valid_area
    }

    # Excluir contornos contenedores (marcos o manzanas) que ya tienen sub-contornos de lote válidos
    leaf_valid_indices = set(valid_indices)
    if hierarchy is not None and len(hierarchy) > 0:
        hier = hierarchy[0]
        for idx in valid_indices:
            parent_idx = int(hier[idx][3])
            while parent_idx != -1:
                if parent_idx in leaf_valid_indices:
                    leaf_valid_indices.discard(parent_idx)
                parent_idx = int(hier[parent_idx][3])

    lotes: List[dict] = []

    for idx in sorted(leaf_valid_indices):
        cnt = contours[idx]
        area = areas[idx] * area_scale

        # Aproximación de polígono
        epsilon = 0.015 * cv2.arcLength(cnt, True)
        approx = cv2.approxPolyDP(cnt, epsilon, True)

        poligono = [
            Point(x=int(round(p[0][0] * inv_scale)), y=int(round(p[0][1] * inv_scale)))
            for p in approx
        ]

        # Centroide del polígono
        M = cv2.moments(cnt)
        if M["m00"] != 0:
            cx = int(round((M["m10"] / M["m00"]) * inv_scale))
            cy = int(round((M["m01"] / M["m00"]) * inv_scale))
        else:
            cx, cy = 0, 0

        # Región de Interés (ROI) para OCR
        x, y, bw, bh = cv2.boundingRect(cnt)
        # Añadir pequeña tolerancia a la ROI
        pad = 2
        y1, y2 = max(0, y - pad), min(gray.shape[0], y + bh + pad)
        x1, x2 = max(0, x - pad), min(gray.shape[1], x + bw + pad)
        roi = gray[y1:y2, x1:x2]

        texto_confiable = ""
        confianza_promedio = 0.0

        if roi.size > 0:
            roi_prep = _preprocess_roi_for_ocr(roi)
            try:
                data = pytesseract.image_to_data(
                    roi_prep, config=TESSERACT_CONFIG, output_type=pytesseract.Output.DICT
                )
                confs = [int(c) for c in data['conf'] if int(c) > 0]
                textos = [data['text'][i].strip() for i in range(len(data['text'])) if int(data['conf'][i]) >= 30 and data['text'][i].strip()]
                
                if textos:
                    texto_confiable = " ".join(textos)
                if confs:
                    confianza_promedio = sum(confs) / len(confs)
            except Exception as e:
                logger.debug("Tesseract fallo en ROI (%d,%d): %s", x, y, e)

        # Limpiar texto del lote
        text_upper = texto_confiable.upper().replace("LOTE", "").replace("LT", "").strip()
        matches = re.findall(r"[A-Z0-9]+", text_upper)
        num_lote = "".join(matches) if matches else "N/D"

        lotes.append(
            LoteDetectado(
                id_temporal=str(uuid.uuid4()),
                numero_lote_detectado=num_lote,
                area_detectada=str(round(float(area), 2)),
                confianza_ocr=round(float(confianza_promedio), 1),
                poligono=poligono,
                coordenadas_centro=Point(x=cx, y=cy),
            ).model_dump()
        )

    elapsed_ms = round((time.perf_counter() - start) * 1000, 2)
    return {
        "lotes": lotes,
        "elapsed_ms": elapsed_ms,
    }


@app.get("/health")
async def health_check():
    checks = {}
    ok = True

    try:
        cv2.getBuildInformation()
        checks["opencv"] = "ok"
    except Exception:
        checks["opencv"] = "error"
        ok = False

    try:
        ver = pytesseract.get_tesseract_version()
        checks["tesseract"] = f"ok (v{ver})"
    except Exception:
        checks["tesseract"] = "error"
        ok = False

    upload_dir = os.getenv("UPLOAD_DIR", UPLOAD_DIR)
    checks["upload_dir"] = "ok" if os.path.isdir(upload_dir) else "missing"

    return {"status": "ok" if ok else "degraded", "checks": checks}


@app.post("/api/cv/extract-lots", response_model=ExtractionResult)
async def extract_lots(
    file_path: str = Form(...),
    min_area: Optional[int] = Form(None)
):
    """
    Extrae polígonos de lotes y texto OCR de un plano (PNG, JPG, PDF).
    - `file_path`: Ruta relativa dentro de UPLOAD_DIR (ej. "planos/plano1.png" o "planos/plano1.pdf")
    - `min_area`: Área mínima opcional en px² para filtrar contornos pequeños.
    """
    full_path = _resolve_upload_path(file_path)

    area_filtro = min_area if min_area is not None else MIN_LOTE_AREA

    try:
        loop = asyncio.get_running_loop()
        result = await loop.run_in_executor(
            _executor, _process_image_sync, full_path, area_filtro
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.exception("Error procesando imagen %s", file_path)
        raise HTTPException(status_code=500, detail=f"Error interno al procesar plano: {e}")

    return ExtractionResult(
        archivo_procesado=file_path,
        total_lotes_detectados=len(result["lotes"]),
        lotes=result["lotes"],
        tiempo_procesamiento_ms=result["elapsed_ms"],
    )

