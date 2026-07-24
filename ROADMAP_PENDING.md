# Roadmap Pendiente - Siguientes Fases

## Resumen

**Fecha:** 2026-07-16
**Estado:** Fase 5 completada — bugs criticos y medios corregidos
**Version actual:** 1.5.0-beta
**Tests backend:** 144 tests, 0 failures
**Frontend:** 29 paginas, build limpio

---

## ✅ COMPLETADO en esta sesion

### Fase 5 - Bugs Criticos Frontend

| # | Bug | Fix | Estado |
|---|-----|-----|--------|
| 1 | Ruta `proyectos/:id` no existia — "Ver detalles" redirigia a `/` | Creado `ProyectoDetail.jsx` + ruta en `App.jsx` | ✅ |
| 2 | `PagoForm` usaba `useParams().ventaId` pero ruta era `:id` | Cambiado ruta a `ventas/:ventaId/pagos/nuevo` | ✅ |
| 3 | `pagoService` tenia 4 endpoints fantasma | Ya estaba corregido (solo `create`) | ✅ |
| 4 | `planPagoService` usaba `/amortizaciones` | Ya estaba en `/tabla-amortizacion` | ✅ |
| 5 | `ventaService.update()` usaba PUT inexistente | Renombrado a `updateEstado` (PATCH), `update` como alias seguro | ✅ |
| 6 | `terrenoService.getByProyecto()` endpoint fantasma | Ya usaba `params: { proyectoId }` | ✅ |
| 7 | Sin link "Gestionar Fases" en ProyectosList | Ya existia `FaLayerGroup` link | ✅ |
| 8 | `proyectoService.getEstadisticas()` endpoint fantasma | Ya usaba `/reportes/proyectos/{id}` | ✅ |
| 9 | `archivoService.getById()` endpoint fantasma | Ya estaba eliminado | ✅ |
| 10 | `ProyectoForm` key `estado` duplicada | Confirmado correcto: `estado` (String ubicacion) vs `estadoProyecto` (enum status) | ✅ |
| 11 | `terrenoNumero` vs `terrenoNumeroLote` inconsistente | Ya unificado a `terrenoNumeroLote` | ✅ |
| 12-14 | Imports muertos en FasesList, FaseForm, Layout | Eliminados `FaEye`, `FaEyeSlash`, `FaMap`, `FaCalendar`, `FaLayerGroup` | ✅ |

**Archivos creados:** 1 (`ProyectoDetail.jsx`)
**Archivos modificados:** 6 (`App.jsx`, `PagoForm.jsx`, `ventaService.js`, `FasesList.jsx`, `FaseForm.jsx`, `Layout.jsx`)

### Fase 7 - Frontend Avanzado

| # | Mejora | Fix | Estado |
|---|--------|-----|--------|
| 1 | Gráficos en Dashboard | Integrado `recharts` con 4 gráficos interactivos (ventas mensuales, distribución general, estados de lotes por proyecto, ticket promedio) | ✅ |
| 2 | Notificaciones toast uniformes | Reemplazado alerts con `react-hot-toast` | ✅ |
| 3 | Paginación client-side y spinners | Implementado en ClientesList, TerrenosList y VentasList | ✅ |
| 4 | Tema Oscuro y Responsive | Añadido botón sol/luna y menú hamburguesa colapsable off-canvas en cabecera móvil | ✅ |

---

## Fase 6 - Testing Docker/Testcontainers (PENDIENTE)

### 6.1 PlanoIngestaController tests

**Endpoint coverage:** 3 endpoints (excluidos de E2E por dependencia de infraestructura)

| Endpoint | Descripcion |
|----------|-------------|
| POST /api/v1/proyectos/{id}/planos/analizar | Requiere SSE + CV analysis |
| GET /api/v1/proyectos/{id}/planos/estado/{tareaId} | Requiere SSE |
| POST /api/v1/proyectos/{id}/planos/confirmar | |

**Requiere:** Mock del servicio de analisis de planos o Docker con OpenCV + Tesseract.

### 6.2 Integration tests con PostgreSQL real

Reemplazar H2 por PostgreSQL via Testcontainers para:
- Validar queries JSONB reales (actualmente ignoradas en H2)
- Validar GIN indexes
- Validar comportamiento especifico de PostgreSQL

---

## Fase 7 - Frontend Avanzado (COMPLETADO)

### 7.1 Charts y Graficas

El dashboard actual no tiene graficas. Instalar libreria de charts:
- **Opcion 1:** `chart.js` + `react-chartjs-2` (ligero, popular)
- **Opcion 2:** `recharts` (React-native, declarativo)
- **Opcion 3:** `@nivo/bar` + `@nivo/pie` (moderno, animado)

Graficas sugeridas:
- Disponibilidad por proyecto (barras)
- Ventas mensuales (linea)
- Distribucion de estados (pie)
- Ticket promedio por proyecto (barras)

### 7.2 UX Mejoras

| Tarea | Descripcion | Prioridad |
|-------|-------------|-----------|
| Filtros de busqueda server-side | Implementar paginacion en listados grandes | Media |
| Loading states consistentes | Spinner uniforme en todas las paginas | Baja |
| Error handling uniforme | Toast notifications en vez de alert() | Media |
| Responsive mobile | Optimizar sidebar y tablas para movil | Baja |
| Dark mode | Toggle de tema oscuro/claro | Baja |

---

## Fase 8 - Backend Gaps (PENDIENTE)

### 8.1 Endpoints sin Coverege Frontend

| Endpoint Backend | Descripcion | Prioridad |
|-----------------|-------------|-----------|
| `GET /auditoria/campo/{tabla}/{id}/{campo}` | Historial de un campo especifico | Baja |
| `GET /auditoria/logins/{usuarioId}` | Logins recientes de un usuario | Baja |
| `GET /planes-pago/venta/{ventaId}` | Plan de pago por venta (backend tiene, frontend no) | Media |

### 8.2 Pagos - Endpoints Faltantes

El frontend `pagoService` solo tiene `create()`, pero el backend `PagoController` solo expone `POST /pagos`. Para funcionalidad completa se necesitarian:

| Endpoint | Descripcion | Prioridad |
|----------|-------------|-----------|
| `GET /pagos/venta/{ventaId}` | Historial de pagos de una venta | Alta |
| `GET /pagos/{id}` | Detalle de un pago | Media |
| `DELETE /pagos/{id}` | Eliminar pago | Baja |

---

## Fase 9 - Produccion (PENDIENTE)

### 9.1 Observabilidad

| Tarea | Descripcion |
|-------|-------------|
| Actuator endpoints | `/health`, `/metrics`, `/info` |
| Prometheus metrics | Exportar metricas de negocio |
| Logging estructurado | JSON logs para ELK |

### 9.2 Performance

| Tarea | Descripcion |
|-------|-------------|
| Load testing con k6 | 100 usuarios concurrentes |
| Cache Redis | Para reportes frecuentes |
| Connection pooling tuning | HikariCP config |
| Index query optimization | Analizar queries lentas |

### 9.3 Seguridad Produccion

| Tarea | Descripcion |
|-------|-------------|
| Rate limiting | Limitar requests por IP/tenant |
| CORS configurado | Dominios permitidos |
| Helmet headers | Security headers HTTP |
| Audit log retention | Politica de retencion de logs |

---

## Exclusiones

- **CORS tests**: Spring Security CORS se testea mejor con MockMvc o Testcontainers. Pendiente Fase 6.
- **PlanoIngestaController E2E**: Requiere SSE + CV infra. Pendiente Fase 6.
- **RBAC tests con roles custom**: Requieren setup a nivel de servicio (no endpoint REST para crear usuarios no-ADMIN). Implementado via inyeccion directa de repositorios.

---

## Resumen de Prioridad

| Fase | Tipo | Items | Esfuerzo Est. |
|------|------|-------|---------------|
| **6** | Testing Docker/Testcontainers | 2 | 1-2 dias |
| **7** | Frontend avanzado (charts, UX) | 7 | 3-5 dias |
| **8** | Backend gaps (endpoints faltantes) | 6 | 2-3 dias |
| **9** | Produccion (observabilidad, perf) | 9 | 5-7 dias |
