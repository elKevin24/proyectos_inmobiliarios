-- ================================================
-- V3: Crear tablas de Proyectos y Fases
-- ================================================

CREATE TYPE estado_proyecto AS ENUM (
    'PLANIFICACION',
    'EN_VENTA',
    'AGOTADO',
    'SUSPENDIDO',
    'CANCELADO'
);

CREATE TYPE tipo_precio AS ENUM ('FIJO', 'VARIABLE');

-- Tabla de Proyectos
CREATE TABLE IF NOT EXISTS proyectos (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    direccion TEXT NOT NULL,
    ciudad VARCHAR(100),
    estado VARCHAR(100),
    codigo_postal VARCHAR(10),
    
    -- Ubicación geográfica
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    
    -- Plano/Mapa
    plano_url VARCHAR(500),
    plano_metadata JSONB,
    
    -- Configuración de precios
    tipo_precio tipo_precio NOT NULL DEFAULT 'VARIABLE',
    precio_base DECIMAL(15, 2),
    
    -- Estado
    estado estado_proyecto NOT NULL DEFAULT 'PLANIFICACION',
    fecha_inicio DATE,
    fecha_fin DATE,
    
    -- Estadísticas (denormalizadas para performance)
    total_terrenos INTEGER DEFAULT 0,
    terrenos_disponibles INTEGER DEFAULT 0,
    terrenos_apartados INTEGER DEFAULT 0,
    terrenos_vendidos INTEGER DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uk_proyectos_tenant_nombre UNIQUE (tenant_id, nombre)
);

-- Tabla de Fases
CREATE TABLE IF NOT EXISTS fases (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    proyecto_id BIGINT NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    orden INTEGER NOT NULL DEFAULT 1,
    
    -- Plano específico de la fase (opcional)
    plano_url VARCHAR(500),
    
    -- Estado
    estado estado_proyecto NOT NULL DEFAULT 'PLANIFICACION',
    fecha_inicio DATE,
    fecha_fin DATE,
    
    -- Estadísticas
    total_terrenos INTEGER DEFAULT 0,
    terrenos_disponibles INTEGER DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uk_fases_proyecto_nombre UNIQUE (proyecto_id, nombre)
);

-- Índices
CREATE INDEX idx_proyectos_tenant_id ON proyectos(tenant_id);
CREATE INDEX idx_proyectos_estado ON proyectos(estado);
CREATE INDEX idx_fases_tenant_id ON fases(tenant_id);
CREATE INDEX idx_fases_proyecto_id ON fases(proyecto_id);
CREATE INDEX idx_fases_estado ON fases(estado);

-- Comentarios
COMMENT ON TABLE proyectos IS 'Proyectos inmobiliarios de venta de terrenos';
COMMENT ON TABLE fases IS 'Fases de un proyecto (subdivisiones)';
COMMENT ON COLUMN proyectos.plano_metadata IS 'Metadata del plano en formato JSON';
