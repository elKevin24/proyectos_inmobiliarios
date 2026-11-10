import os
import tempfile
import unittest
import cv2
import numpy as np
from fastapi.testclient import TestClient

# Configurar UPLOAD_DIR temporal multiplataforma antes de importar main
_TEMP_UPLOAD_DIR = tempfile.mkdtemp(prefix="cv_engine_test_")
os.environ["UPLOAD_DIR"] = _TEMP_UPLOAD_DIR

from main import app, _preprocess_roi_for_ocr, _load_image


class TestCvEngine(unittest.TestCase):

    def setUp(self):
        self.upload_dir = tempfile.mkdtemp(prefix="cv_engine_case_")
        os.environ["UPLOAD_DIR"] = self.upload_dir
        self.client = TestClient(app)

    def _create_synthetic_blueprint(self, full_path: str) -> None:
        """Crea un plano sintético blanco con dos lotes subdivididos en negro."""
        os.makedirs(os.path.dirname(full_path), exist_ok=True)
        canvas = np.full((500, 600, 3), 255, dtype=np.uint8)
        # Lote 1 y Lote 2 compartiendo muro central dentro de una manzana
        cv2.rectangle(canvas, (50, 50), (280, 350), (0, 0, 0), 3)
        cv2.rectangle(canvas, (280, 50), (510, 350), (0, 0, 0), 3)
        cv2.imwrite(full_path, canvas)

    def test_health_endpoint(self):
        """Verifica que el endpoint de salud responda estado 200 y JSON estructurado."""
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("status", data)
        self.assertIn("checks", data)
        self.assertIn("opencv", data["checks"])
        self.assertIn("tesseract", data["checks"])
        self.assertEqual(data["checks"]["upload_dir"], "ok")

    def test_extract_lots_file_not_found(self):
        """Verifica que /api/cv/extract-lots retorne 404 cuando el archivo no existe."""
        response = self.client.post(
            "/api/cv/extract-lots",
            data={"file_path": "planos_inexistentes/plano_no_existe.png"}
        )
        self.assertEqual(response.status_code, 404)
        self.assertIn("Archivo no encontrado", response.json()["detail"])

    def test_extract_lots_path_traversal_blocked(self):
        """Verifica que rutas con Path Traversal fuera de UPLOAD_DIR sean rechazadas con 400."""
        response = self.client.post(
            "/api/cv/extract-lots",
            data={"file_path": "../../etc/passwd"}
        )
        self.assertEqual(response.status_code, 400)

    def test_extract_lots_synthetic_blueprint_contract(self):
        """Verifica la detección de lotes subdivididos y el contrato JSON esperado por CvEngineResponse.java."""
        rel_path = "planos/proyecto_1/plano_sintetico.png"
        full_path = os.path.join(self.upload_dir, rel_path)
        self._create_synthetic_blueprint(full_path)

        response = self.client.post(
            "/api/cv/extract-lots",
            data={"file_path": rel_path, "min_area": 5000}
        )
        self.assertEqual(response.status_code, 200)
        data = response.json()

        self.assertEqual(data["archivo_procesado"], rel_path)
        self.assertGreaterEqual(data["total_lotes_detectados"], 2)
        self.assertEqual(len(data["lotes"]), data["total_lotes_detectados"])
        self.assertIsInstance(data["tiempo_procesamiento_ms"], (int, float))

        lote = data["lotes"][0]
        self.assertIn("id_temporal", lote)
        self.assertIn("numero_lote_detectado", lote)
        self.assertIn("area_detectada", lote)
        self.assertIn("confianza_ocr", lote)
        self.assertIn("poligono", lote)
        self.assertIn("coordenadas_centro", lote)
        self.assertGreaterEqual(len(lote["poligono"]), 4)

    def test_extract_lots_flat_upload_dir_fallback(self):
        """Verifica compatibilidad cuando ArchivoService guarda en UPLOAD_DIR/<uuid>.png y se pide planos/proyecto_X/<uuid>.png."""
        filename = "uuid-plano-flat.png"
        flat_path = os.path.join(self.upload_dir, filename)
        self._create_synthetic_blueprint(flat_path)

        response = self.client.post(
            "/api/cv/extract-lots",
            data={"file_path": f"planos/proyecto_99/{filename}", "min_area": 5000}
        )
        self.assertEqual(response.status_code, 200)
        self.assertGreaterEqual(response.json()["total_lotes_detectados"], 2)

    def test_preprocess_roi_for_ocr(self):
        """Verifica que la función de preprocesamiento roi escale y procese arreglos NumPy."""
        # Crear imagen sintética en escala de grises de 20x20 px
        roi_dummy = np.zeros((20, 20), dtype=np.uint8)
        processed = _preprocess_roi_for_ocr(roi_dummy)

        # Debe haberse redimensionado al menos a 40px de alto o ancho
        self.assertGreaterEqual(processed.shape[0], 40)
        self.assertGreaterEqual(processed.shape[1], 40)

    def test_load_image_invalid_path(self):
        """Verifica que _load_image lance ValueError si la ruta es inválida."""
        with self.assertRaises(ValueError):
            _load_image(os.path.join(self.upload_dir, "archivo_inexistente_12345.png"))


if __name__ == "__main__":
    unittest.main()

