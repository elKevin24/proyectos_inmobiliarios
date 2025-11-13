-- ================================================
-- V4: Crear tabla de Terrenos
-- ================================================

CREATE TYPE estado_terreno AS ENUM (
    'DISPONIBLE',
    'APARTADO',
    'VENDIDO',
    'NO_DISPONIBLE'
);

-- Tabla de Terrenos
CREATE TABLE IF NOT EXISTS terrenos (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    proyecto_id BIGINT NOT NULL REFERENCES proyectos(id) ON DELETE CASCADE,
    fase_id BIGINT REFERENCES fases(id) ON DELETE SET NULL,
    
    -- Identificación
    numero_lote VARCHAR(50) NOT NULL,
    manzana VARCHAR(50),
    
    -- Dimensiones
    area DECIMAL(10, 2) NOT NULL,
    frente DECIMAL(10, 2),
    fondo DECIMAL(10, 2),
    
    -- Ubicación en el plano
    coordenadas_plano JSONB,
    poligono JSONB,
    
    -- Precio
    precio_base DECIMAL(15, 2) NOT NULL,
    precio_ajuste DECIMAL(15, 2) DEFAULT 0,
    precio_multiplicador DECIMAL(5, 2) DEFAULT 1.00,
    precio_final DECIMAL(15, 2) NOT NULL,
    
    -- Estado
    estado estado_terreno NOT NULL DEFAULT 'DISPONIBLE',
    
    -- Información adicional
    observaciones TEXT,
    caracteristicas JSONB,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uk_terrenos_proyecto_numero UNIQUE (proyecto_id, numero_lote, manzana)
);

-- Índices
CREATE INDEX idx_terrenos_tenant_id ON terrenos(tenant_id);
CREATE INDEX idx_terrenos_proyecto_id ON terrenos(proyecto_id);
CREATE INDEX idx_terrenos_fase_id ON terrenos(fase_id);
CREATE INDEX idx_terrenos_estado ON terrenos(estado);
CREATE INDEX idx_terrenos_numero_lote ON terrenos(numero_lote);

-- Índice GIN para búsqueda en JSON
CREATE INDEX idx_terrenos_caracteristicas ON terrenos USING GIN (caracteristicas);

-- Comentarios
COMMENT ON TABLE terrenos IS 'Terrenos/Lotes individuales de un proyecto';
COMMENT ON COLUMN terrenos.coordenadas_plano IS 'Coordenadas en el plano en formato JSON';
COMMENT ON COLUMN terrenos.poligono IS 'Polígono del terreno en formato GeoJSON';
COMMENT ON COLUMN terrenos.precio_final IS 'Precio calculado: (precio_base + precio_ajuste) * precio_multiplicador';
