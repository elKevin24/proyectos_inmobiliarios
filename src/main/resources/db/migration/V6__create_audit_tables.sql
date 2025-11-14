-- ================================================
-- V6: Crear tablas de Auditoría
-- ================================================

-- Tabla de Auditoría Simple (acciones generales)
CREATE TABLE IF NOT EXISTS audit_log_simple (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id) ON DELETE CASCADE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_email VARCHAR(255),
    
    -- Acción
    accion VARCHAR(100) NOT NULL,
    descripcion TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Metadata
    metadata JSONB,
    
    -- Timestamp
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Auditoría Crítica (cambios de datos importantes)
CREATE TABLE IF NOT EXISTS audit_log_critica (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    usuario_id BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
    usuario_email VARCHAR(255),
    
    -- Tabla y registro afectado
    tabla VARCHAR(100) NOT NULL,
    registro_id BIGINT NOT NULL,
    
    -- Campo modificado
    campo VARCHAR(100) NOT NULL,
    valor_anterior TEXT,
    valor_nuevo TEXT,
    
    -- Contexto
    operacion VARCHAR(20) NOT NULL,
    motivo TEXT,
    ip_address VARCHAR(45),
    
    -- Timestamp
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Archivo de Auditoría (logs antiguos)
CREATE TABLE IF NOT EXISTS audit_log_archive (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    tipo VARCHAR(20) NOT NULL,
    datos JSONB NOT NULL,
    fecha_original TIMESTAMP NOT NULL,
    fecha_archivo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para Audit Simple
CREATE INDEX idx_audit_simple_tenant_id ON audit_log_simple(tenant_id);
CREATE INDEX idx_audit_simple_usuario_id ON audit_log_simple(usuario_id);
CREATE INDEX idx_audit_simple_accion ON audit_log_simple(accion);
CREATE INDEX idx_audit_simple_fecha ON audit_log_simple(fecha);

-- Índices para Audit Crítica
CREATE INDEX idx_audit_critica_tenant_id ON audit_log_critica(tenant_id);
CREATE INDEX idx_audit_critica_usuario_id ON audit_log_critica(usuario_id);
CREATE INDEX idx_audit_critica_tabla ON audit_log_critica(tabla);
CREATE INDEX idx_audit_critica_registro_id ON audit_log_critica(registro_id);
CREATE INDEX idx_audit_critica_fecha ON audit_log_critica(fecha);

-- Índices para Archive
CREATE INDEX idx_audit_archive_tenant_id ON audit_log_archive(tenant_id);
CREATE INDEX idx_audit_archive_tipo ON audit_log_archive(tipo);
CREATE INDEX idx_audit_archive_fecha_original ON audit_log_archive(fecha_original);

-- Índices GIN para búsqueda en JSON
CREATE INDEX idx_audit_simple_metadata ON audit_log_simple USING GIN (metadata);
CREATE INDEX idx_audit_archive_datos ON audit_log_archive USING GIN (datos);

-- Comentarios
COMMENT ON TABLE audit_log_simple IS 'Auditoría de acciones generales (logins, exports, etc)';
COMMENT ON TABLE audit_log_critica IS 'Auditoría de cambios críticos (precios, transacciones, usuarios)';
COMMENT ON TABLE audit_log_archive IS 'Archivo de logs antiguos (> 1 año)';
