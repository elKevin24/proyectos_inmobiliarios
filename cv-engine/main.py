import asyncio
import logging
import os
import re
import time
import uuid
from concurrent.futures import ThreadPoolExecutor
from typing import List

import cv2
import numpy as np
import pytesseract
from fastapi import FastAPI, Form, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("cv-engine")

app = FastAPI(title="Terrenos CV Engine")

UPLOAD_DIR = os.getenv("UPLOAD_DIR", "/app/uploads")
MAX_IMAGE_DIMENSION = int(os.getenv("MAX_IMAGE_DIMENSION", "8000"))
MIN_LOTE_AREA = int(os.getenv("MIN_LOTE_AREA", "1000"))

_executor = ThreadPoolExecutor(max_workers=int(os.getenv("THREAD_WORKERS", "2")))


class Point(BaseModel):
    x: int
    y: int


class LoteDetectado(BaseModel):
    id_temporal: str
    numero_lote_detectado: str
    area_detectada: str
    confianza_ocr: float           # 0-100: el frontend marca en rojo si < 60%
    poligono: List[Point]
    coordenadas_centro: Point


class ExtractionResult(BaseModel):
    archivo_procesado: str
    total_lotes_detectados: int
    lotes: List[LoteDetectado]
    tiempo_procesamiento_ms: float


def _process_image_sync(full_path: str) -> dict:
    """Funcion sincronica que ejecuta OpenCV + Tesseract. Se ejecuta en ThreadPool."""
    start = time.perf_counter()

    image = cv2.imread(full_path)
    if image is None:
        raise ValueError("No se pudo leer la imagen. Formato invalido.")

    h, w = image.shape[:2]
    if max(h, w) > MAX_IMAGE_DIMENSION:
        scale = MAX_IMAGE_DIMENSION / max(h, w)
        image = cv2.resize(image, None, fx=scale, fy=scale, interpolation=cv2.INTER_AREA)
        logger.info("Imagen redimensionada de %dx%d a %dx%d", w, h, int(w * scale), int(h * scale))

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    _, thresh = cv2.threshold(gray, 200, 255, cv2.THRESH_BINARY_INV)
    kernel = np.ones((3, 3), np.uint8)
    morph = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel, iterations=2)

    contours, _ = cv2.findContours(morph, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    lotes: List[dict] = []
    tesseract_config = r"--oem 3 --psm 6"

    for cnt in contours:
        area = cv2.contourArea(cnt)
        if area < MIN_LOTE_AREA:
            continue

        epsilon = 0.02 * cv2.arcLength(cnt, True)
        approx = cv2.approxPolyDP(cnt, epsilon, True)

        poligono = [Point(x=int(p[0][0]), y=int(p[0][1])) for p in approx]

        M = cv2.moments(cnt)
        if M["m00"] != 0:
            cx = int(M["m10"] / M["m00"])
            cy = int(M["m01"] / M["m00"])
        else:
            cx, cy = 0, 0

        x, y, bw, bh = cv2.boundingRect(cnt)
        roi = gray[y : y + bh, x : x + bw]

        try:
            data = pytesseract.image_to_data(
                roi, config=tesseract_config, output_type=pytesseract.Output.DICT
            )
            # Filtrar palabras con confianza >= 40%
            texto_confiable = " ".join([
                data['text'][i]
                for i in range(len(data['text']))
                if int(data['conf'][i]) >= 40 and data['text'][i].strip()
            ])
            confs_validas = [int(c) for c in data['conf'] if int(c) > 0]
            confianza_promedio = sum(confs_validas) / len(confs_validas) if confs_validas else 0.0
        except Exception as e:
            logger.warning("Tesseract fallo en ROI (%d,%d): %s", x, y, e)
            texto_confiable = ""
            confianza_promedio = 0.0

        text_upper = texto_confiable.upper().replace("LOTE", "").replace("LT", "").strip()
        matches = re.findall(r"[A-Z0-9]+", text_upper)
        num_lote = "".join(matches) if matches else "N/D"

        lotes.append(
            LoteDetectado(
                id_temporal=str(uuid.uuid4()),
                numero_lote_detectado=num_lote,
                area_detectada=str(round(area, 2)),
                confianza_ocr=round(confianza_promedio, 1),
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

    checks["upload_dir"] = "ok" if os.path.isdir(UPLOAD_DIR) else "missing"

    return {"status": "ok" if ok else "degraded", "checks": checks}


@app.post("/api/cv/extract-lots")
async def extract_lots(file_path: str = Form(...)):
    """
    Recibe la ruta relativa del archivo dentro de UPLOAD_DIR.
    Ejemplo: file_path="planos/proyecto_1/plano.png"
    La ejecucion de OpenCV + Tesseract se delega a un ThreadPool
    para no bloquear el event loop de asyncio.
    """
    full_path = os.path.join(UPLOAD_DIR, file_path)

    if not os.path.isfile(full_path):
        raise HTTPException(status_code=404, detail=f"Archivo no encontrado: {file_path}")

    try:
        result = await asyncio.to_thread(_process_image_sync, full_path)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logger.exception("Error procesando imagen %s", file_path)
        raise HTTPException(status_code=500, detail=f"Error interno al procesar imagen: {e}")

    return ExtractionResult(
        archivo_procesado=file_path,
        total_lotes_detectados=len(result["lotes"]),
        lotes=result["lotes"],
        tiempo_procesamiento_ms=result["elapsed_ms"],
    )
