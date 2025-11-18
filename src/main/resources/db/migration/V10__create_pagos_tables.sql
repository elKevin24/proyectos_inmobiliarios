-- =====================================================
-- Migración V10: Crear tablas de Sistema de Pagos
-- Descripción: Planes de pago, amortizaciones y pagos
-- Autor: Kevin
-- Fecha: 2025-01-18
-- =====================================================

-- Crear ENUM para tipo de plan de pago
CREATE TYPE tipo_plan_pago AS ENUM (
    'CONTADO',
    'FINANCIAMIENTO_PROPIO',
    'CREDITO_BANCARIO',
    'MIXTO'
);

-- Crear ENUM para frecuencia de pago
CREATE TYPE frecuencia_pago AS ENUM (
    'SEMANAL',
    'QUINCENAL',
    'MENSUAL',
    'BIMESTRAL',
    'TRIMESTRAL',
    'SEMESTRAL',
    'ANUAL'
);

-- Crear ENUM para estado de amortización
CREATE TYPE estado_amortizacion AS ENUM (
    'PENDIENTE',
    'PAGADO',
    'VENCIDO',
    'PARCIALMENTE_PAGADO'
);

-- Crear ENUM para estado de pago
CREATE TYPE estado_pago AS ENUM (
    'APLICADO',
    'CANCELADO',
    'REEMBOLSADO'
);

-- Crear ENUM para método de pago
CREATE TYPE metodo_pago AS ENUM (
    'EFECTIVO',
    'TRANSFERENCIA',
    'CHEQUE',
    'TARJETA_CREDITO',
    'TARJETA_DEBITO',
    'OTRO'
);

-- =====================================================
-- Tabla de Planes de Pago
-- =====================================================
CREATE TABLE IF NOT EXISTS planes_pago (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    venta_id BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL,

    -- Configuración del plan
    tipo_plan tipo_plan_pago NOT NULL,
    frecuencia_pago frecuencia_pago NOT NULL DEFAULT 'MENSUAL',

    -- Montos
    monto_total DECIMAL(15, 2) NOT NULL,
    enganche DECIMAL(15, 2) DEFAULT 0,
    monto_financiado DECIMAL(15, 2) NOT NULL,

    -- Intereses
    tasa_interes_anual DECIMAL(5, 2) DEFAULT 0, -- Porcentaje anual
    tasa_interes_mensual DECIMAL(5, 4) DEFAULT 0, -- Calculado automáticamente
    aplica_interes BOOLEAN NOT NULL DEFAULT FALSE,

    -- Plazos
    numero_pagos INTEGER NOT NULL, -- Total de cuotas
    plazo_meses INTEGER, -- Duración en meses

    -- Mora
    tasa_mora_mensual DECIMAL(5, 2) DEFAULT 0, -- Porcentaje de mora
    dias_gracia INTEGER DEFAULT 0, -- Días después del vencimiento sin mora

    -- Fechas
    fecha_inicio DATE NOT NULL,
    fecha_primer_pago DATE NOT NULL,
    fecha_ultimo_pago DATE,

    -- Observaciones
    notas TEXT,

    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_planes_pago_venta UNIQUE (venta_id)
);

-- =====================================================
-- Tabla de Amortizaciones (Cuotas)
-- =====================================================
CREATE TABLE IF NOT EXISTS amortizaciones (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    plan_pago_id BIGINT NOT NULL REFERENCES planes_pago(id) ON DELETE CASCADE,

    -- Identificación de la cuota
    numero_cuota INTEGER NOT NULL,

    -- Montos de la cuota
    capital DECIMAL(15, 2) NOT NULL,
    interes DECIMAL(15, 2) DEFAULT 0,
    monto_cuota DECIMAL(15, 2) NOT NULL, -- capital + interés

    -- Montos pagados
    monto_pagado DECIMAL(15, 2) DEFAULT 0,
    monto_pendiente DECIMAL(15, 2) NOT NULL,

    -- Mora
    mora_acumulada DECIMAL(15, 2) DEFAULT 0,
    dias_atraso INTEGER DEFAULT 0,

    -- Fechas
    fecha_vencimiento DATE NOT NULL,
    fecha_pago DATE,

    -- Estado
    estado estado_amortizacion NOT NULL DEFAULT 'PENDIENTE',

    -- Saldo después del pago
    saldo_restante DECIMAL(15, 2), -- Saldo de capital después de pagar esta cuota

    -- Observaciones
    notas TEXT,

    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_amortizacion_plan_numero UNIQUE (plan_pago_id, numero_cuota)
);

-- =====================================================
-- Tabla de Pagos
-- =====================================================
CREATE TABLE IF NOT EXISTS pagos (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    plan_pago_id BIGINT NOT NULL REFERENCES planes_pago(id) ON DELETE CASCADE,
    amortizacion_id BIGINT REFERENCES amortizaciones(id) ON DELETE SET NULL,
    cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL,

    -- Información del pago
    fecha_pago DATE NOT NULL,
    monto_pagado DECIMAL(15, 2) NOT NULL,

    -- Distribución del pago
    monto_a_capital DECIMAL(15, 2) DEFAULT 0,
    monto_a_interes DECIMAL(15, 2) DEFAULT 0,
    monto_a_mora DECIMAL(15, 2) DEFAULT 0,

    -- Método de pago
    metodo_pago metodo_pago NOT NULL,
    referencia_pago VARCHAR(100), -- Número de cheque, referencia de transferencia, etc.

    -- Estado
    estado estado_pago NOT NULL DEFAULT 'APLICADO',

    -- Información adicional
    observaciones TEXT,
    comprobante_ruta VARCHAR(500), -- Ruta del comprobante de pago

    -- Usuario que registró
    usuario_id BIGINT,

    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- =====================================================
-- Índices para optimización
-- =====================================================

-- Índices de planes_pago
CREATE INDEX idx_planes_pago_tenant_id ON planes_pago(tenant_id);
CREATE INDEX idx_planes_pago_venta_id ON planes_pago(venta_id);
CREATE INDEX idx_planes_pago_cliente_id ON planes_pago(cliente_id);
CREATE INDEX idx_planes_pago_tipo_plan ON planes_pago(tipo_plan);
CREATE INDEX idx_planes_pago_deleted ON planes_pago(deleted);

-- Índices de amortizaciones
CREATE INDEX idx_amortizaciones_tenant_id ON amortizaciones(tenant_id);
CREATE INDEX idx_amortizaciones_plan_pago_id ON amortizaciones(plan_pago_id);
CREATE INDEX idx_amortizaciones_estado ON amortizaciones(estado);
CREATE INDEX idx_amortizaciones_fecha_vencimiento ON amortizaciones(fecha_vencimiento);
CREATE INDEX idx_amortizaciones_deleted ON amortizaciones(deleted);

-- Índices de pagos
CREATE INDEX idx_pagos_tenant_id ON pagos(tenant_id);
CREATE INDEX idx_pagos_plan_pago_id ON pagos(plan_pago_id);
CREATE INDEX idx_pagos_amortizacion_id ON pagos(amortizacion_id);
CREATE INDEX idx_pagos_cliente_id ON pagos(cliente_id);
CREATE INDEX idx_pagos_fecha_pago ON pagos(fecha_pago);
CREATE INDEX idx_pagos_metodo_pago ON pagos(metodo_pago);
CREATE INDEX idx_pagos_estado ON pagos(estado);
CREATE INDEX idx_pagos_deleted ON pagos(deleted);

-- =====================================================
-- Comentarios
-- =====================================================

COMMENT ON TABLE planes_pago IS 'Planes de financiamiento de ventas';
COMMENT ON TABLE amortizaciones IS 'Cuotas/amortizaciones de planes de pago';
COMMENT ON TABLE pagos IS 'Registro de pagos aplicados a amortizaciones';

COMMENT ON COLUMN planes_pago.tasa_interes_anual IS 'Tasa de interés anual en porcentaje (ej: 12.5)';
COMMENT ON COLUMN planes_pago.tasa_interes_mensual IS 'Tasa de interés mensual calculada automáticamente';
COMMENT ON COLUMN planes_pago.numero_pagos IS 'Número total de cuotas del plan';
COMMENT ON COLUMN planes_pago.tasa_mora_mensual IS 'Porcentaje de mora por mes de atraso';

COMMENT ON COLUMN amortizaciones.numero_cuota IS 'Número de cuota (1, 2, 3, ...)';
COMMENT ON COLUMN amortizaciones.capital IS 'Monto de capital de la cuota';
COMMENT ON COLUMN amortizaciones.interes IS 'Monto de interés de la cuota';
COMMENT ON COLUMN amortizaciones.saldo_restante IS 'Saldo de capital después de esta cuota';
COMMENT ON COLUMN amortizaciones.mora_acumulada IS 'Mora acumulada por atraso en el pago';

COMMENT ON COLUMN pagos.monto_a_capital IS 'Porción del pago aplicada a capital';
COMMENT ON COLUMN pagos.monto_a_interes IS 'Porción del pago aplicada a interés';
COMMENT ON COLUMN pagos.monto_a_mora IS 'Porción del pago aplicada a mora';
COMMENT ON COLUMN pagos.referencia_pago IS 'Número de referencia del pago (cheque, transferencia, etc.)';
