-- ================================================
-- V5: Crear tablas de Transacciones
-- ================================================

CREATE TYPE estado_apartado AS ENUM (
    'ACTIVO',
    'COMPLETADO',
    'VENCIDO',
    'CANCELADO'
);

CREATE TYPE estado_venta AS ENUM (
    'PENDIENTE',
    'PAGADO',
    'CANCELADA',
    'ANULADA'
);

CREATE TYPE tipo_descuento AS ENUM (
    'PORCENTAJE',
    'MONTO_FIJO'
);

-- Tabla de Cotizaciones
CREATE TABLE IF NOT EXISTS cotizaciones (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    terreno_id BIGINT NOT NULL REFERENCES terrenos(id) ON DELETE RESTRICT,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    
    -- Datos del cliente potencial
    cliente_nombre VARCHAR(255) NOT NULL,
    cliente_email VARCHAR(255),
    cliente_telefono VARCHAR(20),
    
    -- Precios
    precio_base DECIMAL(15, 2) NOT NULL,
    descuento DECIMAL(15, 2) DEFAULT 0,
    precio_final DECIMAL(15, 2) NOT NULL,
    
    -- Vigencia
    fecha_cotizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vigencia DATE,
    
    -- Observaciones
    observaciones TEXT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Tabla de Apartados (Reservas)
CREATE TABLE IF NOT EXISTS apartados (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    terreno_id BIGINT NOT NULL REFERENCES terrenos(id) ON DELETE RESTRICT,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    cotizacion_id BIGINT REFERENCES cotizaciones(id) ON DELETE SET NULL,
    
    -- Datos del cliente
    cliente_nombre VARCHAR(255) NOT NULL,
    cliente_email VARCHAR(255),
    cliente_telefono VARCHAR(20) NOT NULL,
    cliente_direccion TEXT,
    
    -- Montos
    monto_apartado DECIMAL(15, 2) NOT NULL,
    precio_total DECIMAL(15, 2) NOT NULL,
    
    -- Fechas
    fecha_apartado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento TIMESTAMP NOT NULL,
    duracion_dias INTEGER NOT NULL DEFAULT 30,
    
    -- Estado
    estado estado_apartado NOT NULL DEFAULT 'ACTIVO',
    
    -- Información adicional
    observaciones TEXT,
    documentos JSONB,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Tabla de Ventas
CREATE TABLE IF NOT EXISTS ventas (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    terreno_id BIGINT NOT NULL REFERENCES terrenos(id) ON DELETE RESTRICT,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    apartado_id BIGINT REFERENCES apartados(id) ON DELETE SET NULL,
    
    -- Folio/Número de venta
    folio VARCHAR(50) NOT NULL,
    
    -- Datos del comprador
    comprador_nombre VARCHAR(255) NOT NULL,
    comprador_email VARCHAR(255),
    comprador_telefono VARCHAR(20) NOT NULL,
    comprador_direccion TEXT,
    comprador_rfc VARCHAR(13),
    comprador_curp VARCHAR(18),
    
    -- Montos
    precio_total DECIMAL(15, 2) NOT NULL,
    monto_apartado_acreditado DECIMAL(15, 2) DEFAULT 0,
    monto_final DECIMAL(15, 2) NOT NULL,
    
    -- Comisión
    porcentaje_comision DECIMAL(5, 2),
    monto_comision DECIMAL(15, 2),
    
    -- Fechas
    fecha_venta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_escrituracion DATE,
    
    -- Estado
    estado estado_venta NOT NULL DEFAULT 'PENDIENTE',
    
    -- Información adicional
    forma_pago VARCHAR(100),
    observaciones TEXT,
    documentos JSONB,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uk_ventas_tenant_folio UNIQUE (tenant_id, folio)
);

-- Tabla de Descuentos
CREATE TABLE IF NOT EXISTS descuentos (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    proyecto_id BIGINT REFERENCES proyectos(id) ON DELETE CASCADE,
    
    -- Configuración
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    codigo VARCHAR(50),
    tipo tipo_descuento NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    
    -- Aplicabilidad
    aplica_a_terrenos JSONB,
    aplica_a_fases JSONB,
    
    -- Vigencia
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Límites
    usos_maximos INTEGER,
    usos_actuales INTEGER DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Índices
CREATE INDEX idx_cotizaciones_tenant_id ON cotizaciones(tenant_id);
CREATE INDEX idx_cotizaciones_terreno_id ON cotizaciones(terreno_id);
CREATE INDEX idx_cotizaciones_usuario_id ON cotizaciones(usuario_id);
CREATE INDEX idx_cotizaciones_fecha ON cotizaciones(fecha_cotizacion);

CREATE INDEX idx_apartados_tenant_id ON apartados(tenant_id);
CREATE INDEX idx_apartados_terreno_id ON apartados(terreno_id);
CREATE INDEX idx_apartados_estado ON apartados(estado);
CREATE INDEX idx_apartados_fecha_vencimiento ON apartados(fecha_vencimiento);

CREATE INDEX idx_ventas_tenant_id ON ventas(tenant_id);
CREATE INDEX idx_ventas_terreno_id ON ventas(terreno_id);
CREATE INDEX idx_ventas_estado ON ventas(estado);
CREATE INDEX idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX idx_ventas_folio ON ventas(folio);

CREATE INDEX idx_descuentos_tenant_id ON descuentos(tenant_id);
CREATE INDEX idx_descuentos_proyecto_id ON descuentos(proyecto_id);
CREATE INDEX idx_descuentos_activo ON descuentos(activo);

-- Comentarios
COMMENT ON TABLE cotizaciones IS 'Cotizaciones generadas para clientes potenciales';
COMMENT ON TABLE apartados IS 'Apartados/Reservas de terrenos con plazo de vencimiento';
COMMENT ON TABLE ventas IS 'Ventas concretadas de terrenos';
COMMENT ON TABLE descuentos IS 'Descuentos configurables por proyecto/terreno';
