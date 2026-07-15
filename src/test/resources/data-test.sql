-- Seed permissions for E2E tests (AuthService.register requires permisoRepository.findAll())

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('PROYECTO_CREAR', 'Crear Proyectos', 'Permite crear nuevos proyectos', 'PROYECTOS', false),
('PROYECTO_EDITAR', 'Editar Proyectos', 'Permite editar proyectos existentes', 'PROYECTOS', false),
('PROYECTO_ELIMINAR', 'Eliminar Proyectos', 'Permite eliminar proyectos', 'PROYECTOS', false),
('PROYECTO_VER', 'Ver Proyectos', 'Permite ver proyectos', 'PROYECTOS', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('TERRENO_CREAR', 'Crear Terrenos', 'Permite crear nuevos terrenos', 'TERRENOS', false),
('TERRENO_EDITAR', 'Editar Terrenos', 'Permite editar terrenos existentes', 'TERRENOS', false),
('TERRENO_ELIMINAR', 'Eliminar Terrenos', 'Permite eliminar terrenos', 'TERRENOS', false),
('TERRENO_VER', 'Ver Terrenos', 'Permite ver terrenos', 'TERRENOS', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('COTIZACION_CREAR', 'Crear Cotizaciones', 'Permite crear cotizaciones', 'COTIZACIONES', false),
('COTIZACION_VER', 'Ver Cotizaciones', 'Permite ver cotizaciones', 'COTIZACIONES', false),
('COTIZACION_ELIMINAR', 'Eliminar Cotizaciones', 'Permite eliminar cotizaciones', 'COTIZACIONES', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('APARTADO_CREAR', 'Crear Apartados', 'Permite crear apartados/reservas', 'APARTADOS', false),
('APARTADO_EDITAR', 'Editar Apartados', 'Permite editar apartados', 'APARTADOS', false),
('APARTADO_CANCELAR', 'Cancelar Apartados', 'Permite cancelar apartados', 'APARTADOS', false),
('APARTADO_VER', 'Ver Apartados', 'Permite ver apartados', 'APARTADOS', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('VENTA_CREAR', 'Crear Ventas', 'Permite registrar ventas', 'VENTAS', false),
('VENTA_EDITAR', 'Editar Ventas', 'Permite editar ventas', 'VENTAS', false),
('VENTA_CANCELAR', 'Cancelar Ventas', 'Permite cancelar ventas', 'VENTAS', false),
('VENTA_VER', 'Ver Ventas', 'Permite ver ventas', 'VENTAS', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('USUARIO_CREAR', 'Crear Usuarios', 'Permite crear nuevos usuarios', 'USUARIOS', false),
('USUARIO_EDITAR', 'Editar Usuarios', 'Permite editar usuarios', 'USUARIOS', false),
('USUARIO_ELIMINAR', 'Eliminar Usuarios', 'Permite eliminar usuarios', 'USUARIOS', false),
('USUARIO_VER', 'Ver Usuarios', 'Permite ver usuarios', 'USUARIOS', false),
('USUARIO_ASIGNAR_ROLES', 'Asignar Roles', 'Permite asignar roles a usuarios', 'USUARIOS', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('REPORTE_VENTAS', 'Reporte de Ventas', 'Acceso al reporte de ventas', 'REPORTES', false),
('REPORTE_DISPONIBILIDAD', 'Reporte de Disponibilidad', 'Acceso al reporte de disponibilidad', 'REPORTES', false),
('REPORTE_RENTABILIDAD', 'Reporte de Rentabilidad', 'Acceso al reporte de rentabilidad', 'REPORTES', false),
('REPORTE_COMISIONES', 'Reporte de Comisiones', 'Acceso al reporte de comisiones', 'REPORTES', false),
('REPORTE_APARTADOS', 'Reporte de Apartados', 'Acceso al reporte de apartados', 'REPORTES', false),
('REPORTE_INGRESOS', 'Reporte de Ingresos', 'Acceso al reporte de ingresos vs gastos', 'REPORTES', false),
('REPORTE_ACTIVIDAD', 'Reporte de Actividad', 'Acceso al reporte de actividad', 'REPORTES', false),
('REPORTE_PRECIOS', 'Reporte de Precios', 'Acceso al reporte de evolución de precios', 'REPORTES', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('AUDITORIA_VER', 'Ver Auditoría', 'Permite ver los logs de auditoría', 'AUDITORIA', false),
('AUDITORIA_EXPORTAR', 'Exportar Auditoría', 'Permite exportar logs de auditoría', 'AUDITORIA', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('CONFIG_EMPRESA', 'Configurar Empresa', 'Permite configurar datos de la empresa', 'CONFIGURACION', false),
('CONFIG_PRECIOS', 'Configurar Precios', 'Permite configurar precios y descuentos', 'CONFIGURACION', false),
('CONFIG_SISTEMA', 'Configurar Sistema', 'Acceso a configuración del sistema', 'CONFIGURACION', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('CLIENTE_VER', 'Ver Clientes', 'Permite ver clientes', 'CLIENTES', false),
('CLIENTE_CREAR', 'Crear Clientes', 'Permite crear clientes', 'CLIENTES', false),
('CLIENTE_EDITAR', 'Editar Clientes', 'Permite editar clientes', 'CLIENTES', false),
('CLIENTE_ELIMINAR', 'Eliminar Clientes', 'Permite eliminar clientes', 'CLIENTES', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('PLAN_PAGO_VER', 'Ver Planes de Pago', 'Permite ver planes de pago', 'PLANES_PAGO', false),
('PLAN_PAGO_CREAR', 'Crear Planes de Pago', 'Permite crear planes de pago', 'PLANES_PAGO', false),
('PLAN_PAGO_EDITAR', 'Editar Planes de Pago', 'Permite editar planes de pago', 'PLANES_PAGO', false),
('PLAN_PAGO_ELIMINAR', 'Eliminar Planes de Pago', 'Permite eliminar planes de pago', 'PLANES_PAGO', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('PAGO_REGISTRAR', 'Registrar Pagos', 'Permite registrar pagos', 'PAGOS', false);

INSERT INTO permisos (codigo, nombre, descripcion, modulo, deleted) VALUES
('ARCHIVO_CREAR', 'Crear Archivos', 'Permite subir archivos', 'ARCHIVOS', false);
