import os
import unittest
import numpy as np
from fastapi.testclient import TestClient

# Impostor UPLOAD_DIR para evitar depender de montajes externos durante tests
os.environ["UPLOAD_DIR"] = "/tmp"

from main import app, _preprocess_roi_for_ocr, _load_image


class TestCvEngine(unittest.TestCase):

    def setUp(self):
        self.client = TestClient(app)

    def test_health_endpoint(self):
        """Verifica que el endpoint de salud responda estado 200 y JSON estructurado."""
        response = self.client.get("/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("status", data)
        self.assertIn("checks", data)
        self.assertIn("opencv", data["checks"])
        self.assertIn("tesseract", data["checks"])

    def test_extract_lots_file_not_found(self):
        """Verifica que /api/cv/extract-lots retorne 404 cuando el archivo no existe."""
        response = self.client.post(
            "/api/cv/extract-lots",
            data={"file_path": "planos_inexistentes/plano_no_existe.png"}
        )
        self.assertEqual(response.status_code, 404)
        self.assertIn("Archivo no encontrado", response.json()["detail"])

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
            _load_image("/tmp/archivo_inexistente_12345.png")


if __name__ == "__main__":
    unittest.main()
