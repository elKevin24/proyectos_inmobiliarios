# ==============================================================================
# Multi-stage Dockerfile para el Frontend React (Vite)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build
# ------------------------------------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# Copiar manifiestos de dependencias para aprovechar caché de capas Docker
COPY package*.json ./

# Instalación limpia y determinista de dependencias
RUN npm ci

# Copiar el código fuente completo
COPY . .

# Argumento para URL base de la API (configurable en tiempo de build)
ARG VITE_API_BASE_URL=http://localhost:8080/api/v1
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

# Compilar bundle de producción optimizado con Vite
RUN npm run build

# ------------------------------------------------------------------------------
# Stage 2: Runtime con Nginx Alpine
# ------------------------------------------------------------------------------
FROM nginx:alpine AS runner

# Remover archivos estáticos por defecto de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiar archivos compilados del stage builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copiar configuración personalizada de Nginx con SPA fallback y proxy reverso
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto HTTP estándar
EXPOSE 80

# Healthcheck nativo de contenedor
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Iniciar servidor Nginx en primer plano
CMD ["nginx", "-g", "daemon off;"]
