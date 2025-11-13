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
('PROYECTO_VER', 'Ver Proyectos', 'Permite ver proyectos', 'PROYECTOS');

-- Permisos de Terrenos
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('TERRENO_CREAR', 'Crear Terrenos', 'Permite crear nuevos terrenos', 'TERRENOS'),
('TERRENO_EDITAR', 'Editar Terrenos', 'Permite editar terrenos existentes', 'TERRENOS'),
('TERRENO_ELIMINAR', 'Eliminar Terrenos', 'Permite eliminar terrenos', 'TERRENOS'),
('TERRENO_VER', 'Ver Terrenos', 'Permite ver terrenos', 'TERRENOS');

-- Permisos de Cotizaciones
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('COTIZACION_CREAR', 'Crear Cotizaciones', 'Permite crear cotizaciones', 'COTIZACIONES'),
('COTIZACION_VER', 'Ver Cotizaciones', 'Permite ver cotizaciones', 'COTIZACIONES'),
('COTIZACION_ELIMINAR', 'Eliminar Cotizaciones', 'Permite eliminar cotizaciones', 'COTIZACIONES');

-- Permisos de Apartados
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('APARTADO_CREAR', 'Crear Apartados', 'Permite crear apartados/reservas', 'APARTADOS'),
('APARTADO_EDITAR', 'Editar Apartados', 'Permite editar apartados', 'APARTADOS'),
('APARTADO_CANCELAR', 'Cancelar Apartados', 'Permite cancelar apartados', 'APARTADOS'),
('APARTADO_VER', 'Ver Apartados', 'Permite ver apartados', 'APARTADOS');

-- Permisos de Ventas
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('VENTA_CREAR', 'Crear Ventas', 'Permite registrar ventas', 'VENTAS'),
('VENTA_EDITAR', 'Editar Ventas', 'Permite editar ventas', 'VENTAS'),
('VENTA_CANCELAR', 'Cancelar Ventas', 'Permite cancelar ventas', 'VENTAS'),
('VENTA_VER', 'Ver Ventas', 'Permite ver ventas', 'VENTAS');

-- Permisos de Usuarios
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('USUARIO_CREAR', 'Crear Usuarios', 'Permite crear nuevos usuarios', 'USUARIOS'),
('USUARIO_EDITAR', 'Editar Usuarios', 'Permite editar usuarios', 'USUARIOS'),
('USUARIO_ELIMINAR', 'Eliminar Usuarios', 'Permite eliminar usuarios', 'USUARIOS'),
('USUARIO_VER', 'Ver Usuarios', 'Permite ver usuarios', 'USUARIOS'),
('USUARIO_ASIGNAR_ROLES', 'Asignar Roles', 'Permite asignar roles a usuarios', 'USUARIOS');

-- Permisos de Reportes
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('REPORTE_VENTAS', 'Reporte de Ventas', 'Acceso al reporte de ventas', 'REPORTES'),
('REPORTE_DISPONIBILIDAD', 'Reporte de Disponibilidad', 'Acceso al reporte de disponibilidad', 'REPORTES'),
('REPORTE_RENTABILIDAD', 'Reporte de Rentabilidad', 'Acceso al reporte de rentabilidad', 'REPORTES'),
('REPORTE_COMISIONES', 'Reporte de Comisiones', 'Acceso al reporte de comisiones', 'REPORTES'),
('REPORTE_APARTADOS', 'Reporte de Apartados', 'Acceso al reporte de apartados', 'REPORTES'),
('REPORTE_INGRESOS', 'Reporte de Ingresos', 'Acceso al reporte de ingresos vs gastos', 'REPORTES'),
('REPORTE_ACTIVIDAD', 'Reporte de Actividad', 'Acceso al reporte de actividad', 'REPORTES'),
('REPORTE_PRECIOS', 'Reporte de Precios', 'Acceso al reporte de evolución de precios', 'REPORTES');

-- Permisos de Auditoría
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('AUDITORIA_VER', 'Ver Auditoría', 'Permite ver los logs de auditoría', 'AUDITORIA'),
('AUDITORIA_EXPORTAR', 'Exportar Auditoría', 'Permite exportar logs de auditoría', 'AUDITORIA');

-- Permisos de Configuración
INSERT INTO permisos (codigo, nombre, descripcion, modulo) VALUES
('CONFIG_EMPRESA', 'Configurar Empresa', 'Permite configurar datos de la empresa', 'CONFIGURACION'),
('CONFIG_PRECIOS', 'Configurar Precios', 'Permite configurar precios y descuentos', 'CONFIGURACION'),
('CONFIG_SISTEMA', 'Configurar Sistema', 'Acceso a configuración del sistema', 'CONFIGURACION');

-- Comentario sobre los roles estándar
COMMENT ON TABLE roles IS 
'Roles estándar del sistema:
- ADMIN: Acceso completo a su tenant
- SUPERVISOR: Gestión de proyectos asignados
- VENDEDOR: Crear y gestionar transacciones
- SECRETARIA: Lectura y exportación de datos
- CONTADOR: Acceso a información financiera y reportes';
