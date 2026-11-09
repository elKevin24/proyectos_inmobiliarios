-- ================================================
-- V1: Crear tabla de Tenants (Empresas)
-- ================================================

CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    razon_social VARCHAR(100),
    rfc VARCHAR(13) UNIQUE,
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(255) UNIQUE NOT NULL,
    sitio_web VARCHAR(255),
    logo_url VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Límites
    max_usuarios INTEGER DEFAULT 10,
    max_proyectos INTEGER DEFAULT 5,
    
    -- Configuración
    configuracion JSONB,
    
    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- Índices
CREATE INDEX idx_tenants_activo ON tenants(activo);
CREATE INDEX idx_tenants_email ON tenants(email);
CREATE INDEX idx_tenants_deleted ON tenants(deleted);

-- Comentarios
COMMENT ON TABLE tenants IS 'Tabla de empresas/organizaciones (multi-tenant)';
COMMENT ON COLUMN tenants.configuracion IS 'Configuración personalizada en formato JSON';
