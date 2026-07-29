#!/bin/bash
set -euo pipefail

echo "==================================="
echo "🚀 Iniciando Pipeline CI Local"
echo "==================================="

cleanup() {
  if [[ -n "${BACKEND_PID:-}" ]] && kill -0 "$BACKEND_PID" 2>/dev/null; then
    echo "🛑 Deteniendo Backend..."
    kill "$BACKEND_PID" || true
  fi
}

trap cleanup EXIT

# 1. Ejecutar pruebas de backend con cobertura
echo "☕ Ejecutando pruebas de Backend (Java/Maven)..."
./mvnw clean verify

# 2. Levantar el backend en segundo plano
echo "🔄 Levantando el servidor de Backend en localhost:8080..."
./mvnw -DskipTests spring-boot:run -Dspring-boot.run.profiles=test > backend.log 2>&1 &
BACKEND_PID=$!

# Esperar a que el backend esté listo usando healthcheck
echo "⏳ Esperando a que el Backend inicialice completamente..."
for i in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null; then
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "❌ El Backend no respondió a tiempo"
    cat backend.log
    exit 1
  fi
  sleep 2
done

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

# 6. Instalar browsers de Playwright y ejecutar E2E
echo "🎭 Instalando browsers de Playwright..."
npx playwright install --with-deps chromium

echo "🎭 Ejecutando suite de pruebas E2E completa..."
npx playwright test

echo "✅ ¡Todas las pruebas han pasado exitosamente! La suite es completamente rigurosa."
