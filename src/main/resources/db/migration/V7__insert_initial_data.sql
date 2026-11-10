-- ================================================
-- V7: Insertar datos iniciales (Permisos y Roles estándar)
-- ================================================

-- ========================================
-- PERMISOS DEL SISTEMA
-- ========================================

-- Permisos de Proyectos
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('PROYECTO_CREAR', 'Crear Proyectos', 'Permite crear nuevos proyectos', 'PROYECTOS'),
('PROYECTO_EDITAR', 'Editar Proyectos', 'Permite editar proyectos existentes', 'PROYECTOS'),
('PROYECTO_ELIMINAR', 'Eliminar Proyectos', 'Permite eliminar proyectos', 'PROYECTOS'),
('PROYECTO_VER', 'Ver Proyectos', 'Permite ver proyectos', 'PROYECTOS')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Fases
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('FASE_VER', 'Ver Fases', 'Permite ver fases de proyectos', 'FASES'),
('FASE_CREAR', 'Crear Fases', 'Permite crear nuevas fases', 'FASES'),
('FASE_EDITAR', 'Editar Fases', 'Permite editar fases existentes', 'FASES'),
('FASE_ELIMINAR', 'Eliminar Fases', 'Permite eliminar fases', 'FASES')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Terrenos
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('TERRENO_CREAR', 'Crear Terrenos', 'Permite crear nuevos terrenos', 'TERRENOS'),
('TERRENO_EDITAR', 'Editar Terrenos', 'Permite editar terrenos existentes', 'TERRENOS'),
('TERRENO_ELIMINAR', 'Eliminar Terrenos', 'Permite eliminar terrenos', 'TERRENOS'),
('TERRENO_VER', 'Ver Terrenos', 'Permite ver terrenos', 'TERRENOS')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Clientes
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('CLIENTE_VER', 'Ver Clientes', 'Permite ver clientes', 'CLIENTES'),
('CLIENTE_CREAR', 'Crear Clientes', 'Permite crear clientes', 'CLIENTES'),
('CLIENTE_EDITAR', 'Editar Clientes', 'Permite editar clientes', 'CLIENTES'),
('CLIENTE_ELIMINAR', 'Eliminar Clientes', 'Permite eliminar clientes', 'CLIENTES')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Cotizaciones
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('COTIZACION_CREAR', 'Crear Cotizaciones', 'Permite crear cotizaciones', 'COTIZACIONES'),
('COTIZACION_VER', 'Ver Cotizaciones', 'Permite ver cotizaciones', 'COTIZACIONES'),
('COTIZACION_ELIMINAR', 'Eliminar Cotizaciones', 'Permite eliminar cotizaciones', 'COTIZACIONES')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Apartados
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('APARTADO_CREAR', 'Crear Apartados', 'Permite crear apartados/reservas', 'APARTADOS'),
('APARTADO_EDITAR', 'Editar Apartados', 'Permite editar apartados', 'APARTADOS'),
('APARTADO_CANCELAR', 'Cancelar Apartados', 'Permite cancelar apartados', 'APARTADOS'),
('APARTADO_ELIMINAR', 'Eliminar Apartados', 'Permite eliminar apartados', 'APARTADOS'),
('APARTADO_VER', 'Ver Apartados', 'Permite ver apartados', 'APARTADOS')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Ventas
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('VENTA_CREAR', 'Crear Ventas', 'Permite registrar ventas', 'VENTAS'),
('VENTA_EDITAR', 'Editar Ventas', 'Permite editar ventas', 'VENTAS'),
('VENTA_CANCELAR', 'Cancelar Ventas', 'Permite cancelar ventas', 'VENTAS'),
('VENTA_ELIMINAR', 'Eliminar Ventas', 'Permite eliminar ventas', 'VENTAS'),
('VENTA_VER', 'Ver Ventas', 'Permite ver ventas', 'VENTAS')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Planes de Pago
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('PLAN_PAGO_VER', 'Ver Planes de Pago', 'Permite ver planes de pago', 'PLANES_PAGO'),
('PLAN_PAGO_CREAR', 'Crear Planes de Pago', 'Permite crear planes de pago', 'PLANES_PAGO'),
('PLAN_PAGO_EDITAR', 'Editar Planes de Pago', 'Permite editar planes de pago', 'PLANES_PAGO'),
('PLAN_PAGO_ELIMINAR', 'Eliminar Planes de Pago', 'Permite eliminar planes de pago', 'PLANES_PAGO')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Pagos
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('PAGO_VER', 'Ver Pagos', 'Permite consultar pagos', 'PAGOS'),
('PAGO_CREAR', 'Crear Pagos', 'Permite crear pagos', 'PAGOS'),
('PAGO_REGISTRAR', 'Registrar Pagos', 'Permite registrar pagos', 'PAGOS'),
('PAGO_ELIMINAR', 'Eliminar Pagos', 'Permite cancelar o eliminar pagos', 'PAGOS')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Archivos
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('ARCHIVO_VER', 'Ver Archivos', 'Permite ver archivos', 'ARCHIVOS'),
('ARCHIVO_CREAR', 'Crear Archivos', 'Permite crear archivos', 'ARCHIVOS'),
('ARCHIVO_SUBIR', 'Subir Archivos', 'Permite subir archivos', 'ARCHIVOS'),
('ARCHIVO_ELIMINAR', 'Eliminar Archivos', 'Permite eliminar archivos', 'ARCHIVOS')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Usuarios
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('USUARIO_CREAR', 'Crear Usuarios', 'Permite crear nuevos usuarios', 'USUARIOS'),
('USUARIO_EDITAR', 'Editar Usuarios', 'Permite editar usuarios', 'USUARIOS'),
('USUARIO_ELIMINAR', 'Eliminar Usuarios', 'Permite eliminar usuarios', 'USUARIOS'),
('USUARIO_VER', 'Ver Usuarios', 'Permite ver usuarios', 'USUARIOS'),
('USUARIO_ASIGNAR_ROLES', 'Asignar Roles', 'Permite asignar roles a usuarios', 'USUARIOS')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Reportes
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('REPORTE_VER', 'Ver Reportes', 'Acceso general a reportes y dashboard', 'REPORTES'),
('REPORTE_VENTAS', 'Reporte de Ventas', 'Acceso al reporte de ventas', 'REPORTES'),
('REPORTE_DISPONIBILIDAD', 'Reporte de Disponibilidad', 'Acceso al reporte de disponibilidad', 'REPORTES'),
('REPORTE_RENTABILIDAD', 'Reporte de Rentabilidad', 'Acceso al reporte de rentabilidad', 'REPORTES'),
('REPORTE_COMISIONES', 'Reporte de Comisiones', 'Acceso al reporte de comisiones', 'REPORTES'),
('REPORTE_APARTADOS', 'Reporte de Apartados', 'Acceso al reporte de apartados', 'REPORTES'),
('REPORTE_INGRESOS', 'Reporte de Ingresos', 'Acceso al reporte de ingresos vs gastos', 'REPORTES'),
('REPORTE_ACTIVIDAD', 'Reporte de Actividad', 'Acceso al reporte de actividad', 'REPORTES'),
('REPORTE_PRECIOS', 'Reporte de Precios', 'Acceso al reporte de evolución de precios', 'REPORTES')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Auditoría
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('AUDITORIA_VER', 'Ver Auditoría', 'Permite ver los logs de auditoría', 'AUDITORIA'),
('AUDITORIA_EXPORTAR', 'Exportar Auditoría', 'Permite exportar logs de auditoría', 'AUDITORIA')
ON CONFLICT (codigo) DO NOTHING;

-- Permisos de Configuración y Administración
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('CONFIG_EMPRESA', 'Configurar Empresa', 'Permite configurar datos de la empresa', 'CONFIGURACION'),
('CONFIG_PRECIOS', 'Configurar Precios', 'Permite configurar precios y descuentos', 'CONFIGURACION'),
('CONFIG_SISTEMA', 'Configurar Sistema', 'Acceso a configuración del sistema', 'CONFIGURACION'),
('ADMIN', 'Administrador Total', 'Acceso administrativo total al tenant', 'ADMIN')
ON CONFLICT (codigo) DO NOTHING;

-- Asignar todos los permisos del sistema a cualquier rol ADMIN existente
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permisos p
WHERE r.nombre = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Comentario sobre los roles estándar
COMMENT ON TABLE roles IS 
'Roles estándar del sistema:
- ADMIN: Acceso completo a su tenant
- SUPERVISOR: Gestión de proyectos asignados
- VENDEDOR: Crear y gestionar transacciones
- SECRETARIA: Lectura y exportación de datos
- CONTADOR: Acceso a información financiera y reportes';
