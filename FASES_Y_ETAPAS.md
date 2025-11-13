# Fases y Etapas de Desarrollo

## FASE 0: Preparación (1-2 semanas)

### Etapa 0.1: Setup Inicial
- [ ] Crear repositorio Git
- [ ] Configurar estructura proyecto Maven/Gradle (Backend)
- [ ] Crear proyecto Vite (Frontend)
- [ ] Documentación técnica inicial
- [ ] Configurar CI/CD (GitHub Actions o similar)
- [ ] Setup Docker/Docker Compose local

### Etapa 0.2: Configuración Base
- [ ] Spring Boot configuración inicial
- [ ] PostgreSQL setup local
- [ ] React + Vite configuración
- [ ] Variables de entorno (.env)
- [ ] Conexión BD local

---

## FASE 1: MVP Backend (3-4 semanas)

### Etapa 1.1: Base de Datos y Entidades
- [ ] Crear schema PostgreSQL completo
- [ ] Migraciones Flyway
- [ ] Entidades JPA (Tenant, Proyecto, Fase, Terreno, Usuario, Rol)
- [ ] Relaciones y constraints
- [ ] Índices estratégicos

### Etapa 1.2: Autenticación y Seguridad
- [ ] JWT authentication
- [ ] TenantContext + TenantFilter
- [ ] Encriptación contraseñas (bcrypt)
- [ ] Spring Security configuración
- [ ] Validación de tenant en requests

### Etapa 1.3: Controllers y Endpoints (CRUD Básico)
- [ ] AuthController (login, logout, refresh)
- [ ] TenantController (crear empresa, info)
- [ ] ProyectoController (CRUD proyectos)
- [ ] FaseController (CRUD fases)
- [ ] TerrenoController (CRUD terrenos)
- [ ] UsuarioController (CRUD usuarios)

### Etapa 1.4: Services y Lógica de Negocio
- [ ] AuthService
- [ ] TenantService
- [ ] ProyectoService
- [ ] TerrenoService
- [ ] UsuarioService
- [ ] Validaciones de negocio

### Etapa 1.5: Repositorios
- [ ] Repository interfaces (Spring Data JPA)
- [ ] Queries personalizadas con filtros tenant_id
- [ ] Índices de performance

---

## FASE 2: MVP Frontend (3-4 semanas)

### Etapa 2.1: Setup y Estructura
- [ ] Configurar React Router
- [ ] Zustand stores (auth, property, ui)
- [ ] Configurar Tailwind CSS
- [ ] API client (axios + interceptores)
- [ ] Layouts base (Sidebar, Navigation)

### Etapa 2.2: Autenticación
- [ ] Pantalla Login
- [ ] Gestión token JWT (localStorage)
- [ ] useAuth hook
- [ ] Rutas protegidas (PrivateRoute)
- [ ] Logout

### Etapa 2.3: Gestión de Propiedades (Terrenos)
- [ ] PropertyList (tabla con búsqueda)
- [ ] PropertyDetail (vista individual)
- [ ] PropertyForm (crear/editar)
- [ ] Filtros por estado/fase
- [ ] Paginación

### Etapa 2.4: Visualización de Mapas
- [ ] React Leaflet integración
- [ ] Carga de plano desde proyecto
- [ ] Identificación de lotes en plano
- [ ] Zoom, pan, interactividad
- [ ] Popup con información terreno

### Etapa 2.5: Transacciones Básicas
- [ ] QuotationForm (generar cotizaciones)
- [ ] ReservationModal (crear apartados)
- [ ] SaleForm (registrar ventas)
- [ ] TransactionHistory (historial)
- [ ] Estados y validaciones

---

## FASE 3: Transacciones Completas (2-3 semanas)

### Etapa 3.1: Backend - Cotizaciones
- [ ] CotizacionEntity, Repository, Controller
- [ ] Lógica de cálculo de precio
- [ ] Aplicación automática de descuentos
- [ ] Validaciones de vigencia

### Etapa 3.2: Backend - Apartados
- [ ] ApartadoEntity, Repository, Controller
- [ ] Manejo de duración y vencimiento
- [ ] Estados (ACTIVO → COMPLETADO/VENCIDO/CANCELADO)
- [ ] Transición a venta
- [ ] Auditoría de cambios de estado

### Etapa 3.3: Backend - Ventas
- [ ] VentaEntity, Repository, Controller
- [ ] Cálculo de comisiones
- [ ] Estados (PENDIENTE → PAGADO/CANCELADA/ANULADA)
- [ ] Acreditación de apartados
- [ ] Registro de datos de comprador

### Etapa 3.4: Backend - Descuentos
- [ ] DescuentoEntity, Repository, Controller
- [ ] Creación y aplicación manual
- [ ] Validación de vigencia
- [ ] Histórico de usos

### Etapa 3.5: Frontend - Transacciones Completas
- [ ] Integrar cotizaciones con mapa
- [ ] Flujo apartado → venta
- [ ] Validaciones de estados
- [ ] Mensajes de confirmación
- [ ] Historial completo de transacción

---

## FASE 4: Auditoría (1-2 semanas)

### Etapa 4.1: Auditoría Simple
- [ ] AuditLogSimpleEntity
- [ ] Registrar logins, logouts, exports
- [ ] Vista de logs (admin)
- [ ] Limpieza automática (1 año)

### Etapa 4.2: Auditoría Crítica
- [ ] AuditLogCriticaEntity
- [ ] Aspecto AOP para capturar cambios
- [ ] Campos: tabla, id, campo, valor_anterior, valor_nuevo
- [ ] Registrar en precios, transacciones, usuarios
- [ ] Motivo (opcional)

### Etapa 4.3: Consultas de Auditoría
- [ ] AuditoriaController
- [ ] Queries por usuario, tabla, rango fecha
- [ ] Exportación de logs
- [ ] Acceso solo admin

### Etapa 4.4: Archivado de Logs
- [ ] Migración automática logs > 1 año
- [ ] Tabla ARCHIVE separada
- [ ] Scheduler que ejecuta cada noche

---

## FASE 5: Reportes (2-3 semanas)

### Etapa 5.1: Infraestructura de Reportes
- [ ] ReportService base
- [ ] ReportController
- [ ] Queries de lectura optimizadas
- [ ] Caché (Redis opcional)

### Etapa 5.2: Reportes 1-4
- [ ] Reporte 1: Ventas por Período
- [ ] Reporte 2: Disponibilidad
- [ ] Reporte 3: Rentabilidad
- [ ] Reporte 4: Comisiones
- [ ] Exportación PDF/Excel

### Etapa 5.3: Reportes 5-8
- [ ] Reporte 5: Apartados
- [ ] Reporte 6: Ingresos vs Gastos
- [ ] Reporte 7: Actividad
- [ ] Reporte 8: Evolución Precios
- [ ] Gráficos (Recharts)

### Etapa 5.4: Frontend Reportes
- [ ] ReportsPage
- [ ] Filtros por fecha, proyecto, vendedor
- [ ] Visualización de datos
- [ ] Gráficos
- [ ] Botones exportar PDF/Excel

---

## FASE 6: Gestión de Usuarios y Roles (1-2 semanas)

### Etapa 6.1: Backend - Roles y Permisos
- [ ] RolEntity, PermisoEntity
- [ ] ROL_PERMISO mapping
- [ ] Roles estándar (ADMIN, SUPERVISOR, VENDEDOR, SECRETARIA, CONTADOR)
- [ ] Permisos estándar

### Etapa 6.2: Backend - Gestión de Usuarios
- [ ] UsuarioController mejorado
- [ ] Asignación de roles
- [ ] Asignación de proyectos
- [ ] Activar/desactivar usuarios
- [ ] Cambio de contraseña

### Etapa 6.3: Frontend - Gestión de Usuarios
- [ ] UsersPage (para admin)
- [ ] Formulario crear usuario
- [ ] Modal editar usuario
- [ ] Tabla usuarios con búsqueda
- [ ] Cambio de estado (activo/inactivo)

### Etapa 6.4: Validación de Permisos en Frontend
- [ ] Mostrar/ocultar opciones según rol
- [ ] Deshabilitar botones no permitidos
- [ ] Mensajes de permiso denegado

---

## FASE 7: Gestión Avanzada de Precios (1-2 semanas)

### Etapa 7.1: Backend - Precios
- [ ] PrecioService con lógica completa
- [ ] Cálculo: Base + Ajustes × Multiplicadores
- [ ] Configuración por proyecto (FIJO/VARIABLE)
- [ ] Cambios de precio auditados

### Etapa 7.2: Backend - Descuentos Avanzados
- [ ] Descuentos por porcentaje/monto
- [ ] Descuentos por período
- [ ] Descuentos por terrenos específicos/fases/rangos
- [ ] Validación de vigencia automática

### Etapa 7.3: Frontend - Configuración de Precios
- [ ] PriceSettingsForm (al crear proyecto)
- [ ] Elegir FIJO o VARIABLE
- [ ] DiscountForm (crear descuentos)
- [ ] HistoricoPrecios (ver cambios)

### Etapa 7.4: Frontend - Visualización de Precios
- [ ] Mostrar desglose en cotización
- [ ] Mostrar descuentos aplicados
- [ ] Mostrar precio final vs base

---

## FASE 8: Gestión de Proyectos y Fases Completa (1 semana)

### Etapa 8.1: Backend
- [ ] ProjectService mejorado
- [ ] Cambios de estado (PLANIFICACIÓN → EN_VENTA → AGOTADO/SUSPENDIDO/CANCELADO)
- [ ] Validaciones de transición
- [ ] Cálculo automático de estado (cuando se agotan terrenos)

### Etapa 8.2: Frontend
- [ ] ProjectForm (crear con fases)
- [ ] FaseForm (agregar fases)
- [ ] ProjectDetail con timeline de fases
- [ ] Cambio de estado visual

---

## FASE 9: Visualizador de Planos Avanzado (1-2 semanas)

### Etapa 9.1: Carga de Planos
- [ ] Upload de planos (JPG, PNG, PDF)
- [ ] Almacenamiento en S3
- [ ] Preview en tiempo real
- [ ] Validación de tamaño/formato

### Etapa 9.2: Viewer Avanzado
- [ ] React Leaflet con overlay de imagen
- [ ] Herramientas de medición (opcional)
- [ ] Dibujo de polígonos (lotes)
- [ ] Identificación automática de terrenos
- [ ] Info box al hacer click

### Etapa 9.3: Integración Terrenos-Mapa
- [ ] Cargar terrenos en mapa
- [ ] Colorear por estado (disponible/apartado/vendido)
- [ ] Popup con info terreno
- [ ] Click → ir a detalle

---

## FASE 10: Testing y Optimización (2-3 semanas)

### Etapa 10.1: Testing Backend
- [ ] Unit tests (Services)
- [ ] Integration tests (Controllers)
- [ ] Tests de auditoría
- [ ] Tests de multi-tenant (validar aislamiento)

### Etapa 10.2: Testing Frontend
- [ ] Unit tests (componentes)
- [ ] Tests de integración (flujos)
- [ ] Tests de autenticación

### Etapa 10.3: Performance
- [ ] Optimización queries (índices, n+1)
- [ ] Caché (Redis para reportes)
- [ ] Code splitting (Frontend)
- [ ] Compresión (GZIP, images)

### Etapa 10.4: Seguridad
- [ ] HTTPS/TLS configurado
- [ ] Validaciones de input
- [ ] CSRF protection
- [ ] Rate limiting
- [ ] CORS configurado

---

## FASE 11: Deployment (1 semana)

### Etapa 11.1: Preparación
- [ ] Dockerfiles (Backend, Frontend)
- [ ] Docker Compose producción
- [ ] Variables de entorno configuradas
- [ ] Backups estrategia

### Etapa 11.2: Deployment
- [ ] Ambiente staging
- [ ] Ambiente producción
- [ ] Certificados SSL
- [ ] Monitoreo (logs, métricas)

### Etapa 11.3: Post-Deployment
- [ ] Smoke tests
- [ ] Validación de permisos
- [ ] Auditoría funcionando
- [ ] Backups automáticos

---

## TOTAL: 20-26 semanas (~5-6 meses)

### Timeline Estimado:
- Fase 0: 1-2 semanas
- Fases 1-2: 6-8 semanas (desarrollo paralelo)
- Fase 3: 2-3 semanas
- Fase 4: 1-2 semanas
- Fase 5: 2-3 semanas
- Fase 6: 1-2 semanas
- Fase 7: 1-2 semanas
- Fase 8: 1 semana
- Fase 9: 1-2 semanas
- Fase 10: 2-3 semanas
- Fase 11: 1 semana

---

## Dependencias entre Fases

```
Fase 0 (Setup)
  ↓
Fases 1 & 2 (Paralelas - Backend MVP + Frontend MVP)
  ↓
Fase 3 (Transacciones)
  ↓
Fase 4 (Auditoría)
  ↓
Fase 5 (Reportes)
  ↓
Fase 6 (Usuarios/Roles)
  ↓
Fase 7 (Precios Avanzados)
  ↓
Fase 8 (Proyectos/Fases)
  ↓
Fase 9 (Mapas Avanzados)
  ↓
Fase 10 (Testing/Optimización)
  ↓
Fase 11 (Deployment)
```

---

## MVP Funcional (Fases 0-5)

Después de **FASE 5**, tendrás un MVP completo con:
✅ Multi-tenant funcionando
✅ CRUD de proyectos, terrenos, transacciones
✅ Mapas interactivos
✅ Auditoría robusta
✅ 8 reportes completos
✅ Autenticación y control de acceso

Estimado: **12-15 semanas**

