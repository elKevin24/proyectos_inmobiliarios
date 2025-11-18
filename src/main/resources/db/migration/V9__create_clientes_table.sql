-- =====================================================
-- Migración V9: Crear tabla de Clientes
-- Descripción: Gestión de clientes/compradores del sistema
-- Autor: Kevin
-- Fecha: 2025-01-18
-- =====================================================

-- Crear ENUM para origen del cliente
CREATE TYPE origen_cliente AS ENUM (
    'REFERIDO',
    'REDES_SOCIALES',
    'VISITA_DIRECTA',
    'PUBLICIDAD',
    'RECOMENDACION',
    'EVENTO',
    'OTRO'
);

-- Crear ENUM para estado del cliente
CREATE TYPE estado_cliente AS ENUM (
    'PROSPECTO',
    'INTERESADO',
    'COMPRADOR',
    'INACTIVO'
);

-- Tabla de Clientes
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,

    -- Información personal
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150),
    telefono VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),

    -- Dirección
    direccion VARCHAR(255),
    ciudad VARCHAR(100),
    estado VARCHAR(100),
    codigo_postal VARCHAR(10),
    pais VARCHAR(100) DEFAULT 'México',

    -- Identificación oficial
    rfc VARCHAR(13),
    curp VARCHAR(18),
    fecha_nacimiento DATE,

    -- Información comercial
    origen origen_cliente,
    estado_cliente estado_cliente NOT NULL DEFAULT 'PROSPECTO',

    -- Notas y observaciones
    notas TEXT,
    preferencias JSONB, -- Para guardar preferencias de búsqueda, presupuesto, etc.

    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    -- Constraint de unicidad
    CONSTRAINT uk_clientes_tenant_email UNIQUE (tenant_id, email)
);

-- Índices para optimización de búsquedas
CREATE INDEX idx_clientes_tenant_id ON clientes(tenant_id);
CREATE INDEX idx_clientes_estado_cliente ON clientes(estado_cliente);
CREATE INDEX idx_clientes_email ON clientes(email);
CREATE INDEX idx_clientes_telefono ON clientes(telefono);
CREATE INDEX idx_clientes_nombre_apellido ON clientes(nombre, apellido);
CREATE INDEX idx_clientes_deleted ON clientes(deleted);

-- Índice GIN para búsquedas en preferencias JSONB
CREATE INDEX idx_clientes_preferencias ON clientes USING GIN (preferencias);

-- Comentarios
COMMENT ON TABLE clientes IS 'Clientes y compradores del sistema';
COMMENT ON COLUMN clientes.origen IS 'Cómo conoció el cliente el proyecto';
COMMENT ON COLUMN clientes.estado_cliente IS 'Estado actual del cliente en el funnel de ventas';
COMMENT ON COLUMN clientes.preferencias IS 'Preferencias del cliente en formato JSON (presupuesto, ubicación, tamaño, etc.)';

-- =====================================================
-- Agregar cliente_id a tablas de transacciones
-- =====================================================

-- Agregar cliente_id a cotizaciones
ALTER TABLE cotizaciones
ADD COLUMN cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL;

CREATE INDEX idx_cotizaciones_cliente_id ON cotizaciones(cliente_id);

COMMENT ON COLUMN cotizaciones.cliente_id IS 'Referencia al cliente (opcional, mantiene compatibilidad con datos existentes)';

-- Agregar cliente_id a apartados
ALTER TABLE apartados
ADD COLUMN cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL;

CREATE INDEX idx_apartados_cliente_id ON apartados(cliente_id);

COMMENT ON COLUMN apartados.cliente_id IS 'Referencia al cliente (opcional, mantiene compatibilidad con datos existentes)';

-- Agregar cliente_id a ventas
ALTER TABLE ventas
ADD COLUMN cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL;

CREATE INDEX idx_ventas_cliente_id ON ventas(cliente_id);

COMMENT ON COLUMN ventas.cliente_id IS 'Referencia al cliente (opcional, mantiene compatibilidad con datos existentes)';
