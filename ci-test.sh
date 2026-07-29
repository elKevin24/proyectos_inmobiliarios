#!/bin/bash
set -e

echo "==================================="
echo "🚀 Iniciando Pipeline CI Local"
echo "==================================="

# 1. Ejecutar pruebas unitarias de backend
echo "☕ Ejecutando pruebas unitarias de Backend (Java/Maven)..."
./mvnw clean test

# 2. Levantar el backend en segundo plano
echo "🔄 Levantando el servidor de Backend en localhost:8080..."
./mvnw spring-boot:run &
BACKEND_PID=$!

# Esperar a que el backend esté listo (polling al endpoint de health o simplemente esperar)
echo "⏳ Esperando 20 segundos a que el Backend inicialice completamente..."
sleep 20

# 3. Preparar frontend
echo "⚛️ Preparando Frontend..."
cd frontend
npm ci
npm run lint

# 4. Pruebas Unitarias Frontend
echo "🧪 Ejecutando pruebas unitarias de Frontend..."
npm run test:unit

# 5. Build de Frontend
echo "🏗️ Construyendo Frontend para producción..."
npm run build

# 6. Ejecutar pruebas E2E con Playwright (Playwright webServer debería apuntar a localhost:5173 o similar,
#    pero asume que el frontend local se levanta automáticamente o Playwright lo hace mediante webServer en la config)
echo "🎭 Ejecutando suite de pruebas E2E completa (91 tests)..."
npx playwright test

# Guardar el código de salida de Playwright
E2E_EXIT_CODE=$?

echo "🛑 Deteniendo Backend..."
kill $BACKEND_PID

if [ $E2E_EXIT_CODE -eq 0 ]; then
  echo "✅ ¡Todas las pruebas han pasado exitosamente! La suite es completamente rigurosa."
else
  echo "❌ Las pruebas E2E han fallado. Revisa el reporte de Playwright."
  exit 1
fi
