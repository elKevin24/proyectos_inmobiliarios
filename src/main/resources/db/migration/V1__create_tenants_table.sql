-- ================================================
-- V1: Crear tabla de Tenants (Empresas)
-- ================================================

CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    rfc VARCHAR(13) UNIQUE,
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(255) UNIQUE NOT NULL,
    sitio_web VARCHAR(255),
    logo_url VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Configuración
    configuracion JSONB,
    
    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Índices
CREATE INDEX idx_tenants_activo ON tenants(activo);
CREATE INDEX idx_tenants_email ON tenants(email);

-- Comentarios
COMMENT ON TABLE tenants IS 'Tabla de empresas/organizaciones (multi-tenant)';
COMMENT ON COLUMN tenants.configuracion IS 'Configuración personalizada en formato JSON';
