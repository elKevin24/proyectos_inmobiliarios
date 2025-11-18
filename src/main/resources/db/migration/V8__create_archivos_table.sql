-- =====================================================
-- Migración V8: Tabla de Archivos
-- Descripción: Gestión de archivos (planos, imágenes, documentos)
-- Autor: Kevin
-- Fecha: 2025-01-18
-- =====================================================

-- Tabla: archivos
CREATE TABLE IF NOT EXISTS archivos (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    proyecto_id BIGINT REFERENCES proyectos(id) ON DELETE CASCADE,
    terreno_id BIGINT REFERENCES terrenos(id) ON DELETE CASCADE,
    venta_id BIGINT REFERENCES ventas(id) ON DELETE CASCADE,

    tipo VARCHAR(50) NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_almacenado VARCHAR(500) NOT NULL,
    ruta VARCHAR(1000) NOT NULL,
    extension VARCHAR(100),
    mime_type VARCHAR(100),
    tamanio_bytes BIGINT,

    version INTEGER NOT NULL DEFAULT 1,
    descripcion VARCHAR(500),
    tags VARCHAR(100),
    es_activo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Índices para optimizar búsquedas
CREATE INDEX idx_archivo_tenant ON archivos(tenant_id);
CREATE INDEX idx_archivo_proyecto ON archivos(proyecto_id);
CREATE INDEX idx_archivo_terreno ON archivos(terreno_id);
CREATE INDEX idx_archivo_venta ON archivos(venta_id);
CREATE INDEX idx_archivo_tipo ON archivos(tipo);
CREATE INDEX idx_archivo_deleted ON archivos(deleted);
CREATE INDEX idx_archivo_es_activo ON archivos(es_activo);

-- Comentarios
COMMENT ON TABLE archivos IS 'Almacena metadata de archivos subidos (planos, imágenes, documentos)';
COMMENT ON COLUMN archivos.tipo IS 'Tipo de archivo: PLANO_PROYECTO, PLANO_TERRENO, IMAGEN_PROYECTO, IMAGEN_TERRENO, DOCUMENTO_PROYECTO, etc.';
COMMENT ON COLUMN archivos.nombre_original IS 'Nombre original del archivo subido por el usuario';
COMMENT ON COLUMN archivos.nombre_almacenado IS 'Nombre único generado para almacenar el archivo físicamente';
COMMENT ON COLUMN archivos.ruta IS 'Ruta completa del archivo en el sistema de archivos';
COMMENT ON COLUMN archivos.version IS 'Número de versión del archivo (para versionamiento)';
COMMENT ON COLUMN archivos.es_activo IS 'Indica si es la versión activa del archivo';
