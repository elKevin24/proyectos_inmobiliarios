-- ====================================================================
-- Migración V12: Añadir columna deleted_at para soporte consistente
-- de soft delete y compatibilidad completa con BaseEntity
-- ====================================================================

ALTER TABLE proyectos ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE fases ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE terrenos ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE cotizaciones ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE apartados ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE planes_pago ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE amortizaciones ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE pagos ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
