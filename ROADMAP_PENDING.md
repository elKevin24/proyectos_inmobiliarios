# Roadmap Pendiente - Siguientes Fases

## Resumen

**Fecha:** 2026-07-16
**Estado:** Frontend expansion completada — pendientes bugs criticos + gaps funcionales
**Version actual:** 1.4.0-beta
**Tests:** 144 tests backend, 0 failures

---

## Fase 5 - Bugs Criticos Frontend (PRIORIDAD ALTA)

### 5.1 Bugs de Runtime (impiden funcionalidad)

| # | Bug | Archivo(s) | Problema | Fix Propuesto |
|---|-----|-----------|----------|---------------|
| 1 | **Ruta `proyectos/:id` no existe** | `App.jsx`, `ProyectosList.jsx:143` | "Ver detalles" en tarjetas de proyecto redirige a `/` | Crear `ProyectoDetail.jsx` + agregar ruta `<Route path="proyectos/:id" element={<ProyectoDetail />} />` |
| 2 | **`PagoForm` usa `useParams().ventaId` pero ruta es `:id`** | `PagoForm.jsx:8` vs `App.jsx:92` | `ventaId` siempre `undefined`, form envia `NaN` | Cambiar ruta a `:ventaId` o usar `useParams().id` |
| 3 | **`PagoForm` envia campos incorrectos al backend** | `PagoForm.jsx:16-23` vs `PagoController.java:47` | Envia `ventaId`/`monto` pero backend espera `planPagoId`/`montoPagado` | Corregir campos del form: `planPagoId`, `montoPagado`, `metodoPago` |
| 4 | **`planPagoService` llama endpoint inexistente** | `planPagoService.js:26` | Llama `/amortizaciones` pero backend expone `/tabla-amortizacion` | Cambiar a `/tabla-amortizacion` |
| 5 | **`pagoService` tiene 4 endpoints fantasma** | `pagoService.js` | `getByVenta`, `getById`, `update`, `delete` retornan 404 | Eliminar o implementar en backend |
| 6 | **`ventaService` tiene PUT y cancelar fantasma** | `ventaService.js:20,30` | Backend usa `PATCH /{id}/estado`, no `PUT /{id}` ni `POST /{id}/cancelar` | Corregir a `PATCH /{id}/estado` con body `{ estado: "CANCELADA" }` |

### 5.2 Gaps Funcionales (funcionalidad incompleta)

| # | Gap | Archivo(s) | Problema | Fix Propuesto |
|---|-----|-----------|----------|---------------|
| 7 | **Sin link "Gestionar Fases" en tarjetas de proyecto** | `ProyectosList.jsx` | Fases son inaccesibles desde la UI | Agregar icono `<FaLayerGroup>` con link a `/proyectos/:id/fases` |
| 8 | **`terrenoService.getByProyecto()` llama endpoint inexistente** | `terrenoService.js:36` | Llama `/terrenos/proyecto/{id}` pero backend usa `?proyectoId=X` | Cambiar a `api.get('/terrenos', { params: { proyectoId } })` |
| 9 | **`proyectoService.getEstadisticas()` llama endpoint inexistente** | `proyectoService.js:36` | Llama `/proyectos/{id}/estadisticas` pero stats estan en `/reportes/proyectos/{id}` | Cambiar a `/reportes/proyectos/{id}` |
| 10 | **`archivoService.getById()` llama endpoint inexistente** | `archivoService.js:34` | Llama `GET /archivos/{id}` pero solo existe `GET /archivos/{id}/download` | Cambiar a `/archivos/{id}/download` o eliminar si no se usa |
| 11 | **`ProyectoForm` tiene key `estado` duplicada** | `ProyectoForm.jsx:57,61` | Location state (text input) se pierde silenciosamente por overwrite | Renombrar a `estadoUbicacion` o separar en dos campos distintos |
| 12 | **Inconsistencia `terrenoNumero` vs `terrenoNumeroLote`** | `VentasList.jsx:111` vs `Dashboard.jsx:292` | Dos nombres de campo para el mismo concepto | Verificar campo real en backend y unificar |

### 5.3 Imports Muertos (limpieza)

| # | Archivo | Imports sin usar |
|---|---------|-----------------|
| 13 | `FasesList.jsx:4` | `FaEye`, `FaEyeSlash`, `FaMap` |
| 14 | `FaseForm.jsx:3` | `FaCalendar` |
| 15 | `Layout.jsx:4` | `FaLayerGroup` |

---

## Fase 6 - Backend Gaps (PRIORIDAD MEDIA)

### 6.1 Endpoints Fantasma en Frontend

El frontend tiene servicios que llaman endpoints que no existen en el backend:

| Servicio | Metodo | Endpoint Frontend | Existe en Backend? | Accion |
|----------|--------|-------------------|--------------------|---------|
| `pagoService` | `getByVenta()` | `GET /pagos/venta/{id}` | ❌ | Implementar en `PagoController` |
| `pagoService` | `getById()` | `GET /pagos/{id}` | ❌ | Implementar en `PagoController` |
| `pagoService` | `update()` | `PUT /pagos/{id}` | ❌ | Implementar en `PagoController` |
| `pagoService` | `delete()` | `DELETE /pagos/{id}` | ❌ | Implementar en `PagoController` |
| `ventaService` | `update()` | `PUT /ventas/{id}` | ❌ | Backend usa `PATCH /{id}/estado` |
| `ventaService` | `cancelar()` | `POST /ventas/{id}/cancelar` | ❌ | Backend usa `PATCH /{id}/estado` |
| `terrenoService` | `getByProyecto()` | `GET /terrenos/proyecto/{id}` | ❌ | Backend usa `GET /terrenos?proyectoId=X` |
| `proyectoService` | `getEstadisticas()` | `GET /proyectos/{id}/estadisticas` | ❌ | Stats en `/reportes/proyectos/{id}` |
| `archivoService` | `getById()` | `GET /archivos/{id}` | ❌ | Solo existe `/{id}/download` |
| `planPagoService` | `getAmortizaciones()` | `GET /planes-pago/{id}/amortizaciones` | ❌ | Backend expone `/tabla-amortizacion` |

**Decision:** O implementar los endpoints faltantes en el backend, o corregir el frontend para usar los endpoints que existen.

### 6.2 Auditoria - Endpoints sin Coverege Frontend

| Endpoint Backend | Descripcion | Prioridad |
|-----------------|-------------|-----------|
| `GET /auditoria/campo/{tabla}/{id}/{campo}` | Historial de un campo especifico | Baja |
| `GET /auditoria/logins/{usuarioId}` | Logins recientes de un usuario | Baja |

---

## Fase 7 - Testing Docker/Testcontainers (PENDIENTE)

### 7.1 PlanoIngestaController tests

**Endpoint coverage:** 3 endpoints (excluidos de E2E por dependencia de infraestructura)

| Endpoint | Descripcion |
|----------|-------------|
| POST /api/v1/proyectos/{id}/planos/analizar | Requiere SSE + CV analysis |
| GET /api/v1/proyectos/{id}/planos/estado/{tareaId} | Requiere SSE |
| POST /api/v1/proyectos/{id}/planos/confirmar | |

**Requiere:** Mock del servicio de analisis de planos o Docker con OpenCV + Tesseract.

### 7.2 Integration tests con PostgreSQL real

Reemplazar H2 por PostgreSQL via Testcontainers para:
- Validar queries JSONB reales (actualmente ignoradas en H2)
- Validar GIN indexes
- Validar comportamiento especifico de PostgreSQL

---

## Fase 8 - Frontend Avanzado (PENDIENTE)

### 8.1 Modulos sin Frontend

| Modulo | Backend | Frontend | Notas |
|--------|---------|----------|-------|
| Plano Ingesta | 3 endpoints | ✅ `PlanoValidatorPage.jsx` | Funcional via cv-engine service |
| Dashboard Proyecto | `GET /reportes/proyectos/{id}` | ❌ | Estadisticas detalladas por proyecto |

### 8.2 UX Mejoras

| Tarea | Descripcion | Prioridad |
|-------|-------------|-----------|
| Filtros de busqueda server-side | Implementar paginacion en listados grandes | Media |
| Loading states consistentes | Spinner uniforme en todas las paginas | Baja |
| Error handling uniforme | Toast notifications en vez de alert() | Media |
| Responsive mobile | Optimizar sidebar y tablas para movil | Baja |
| Dark mode | Toggle de tema oscuro/claro | Baja |

### 8.3 Charts y Graficas

El dashboard actual no tiene graficas. Instalar libreria de charts:
- **Opcion 1:** `chart.js` + `react-chartjs-2` (ligero, popular)
- **Opcion 2:** `recharts` (React-native, declarativo)
- **Opcion 3:** `@nivo/bar` + `@nivo/pie` (moderno, animado)

Graficas sugeridas:
- Disponibilidad por proyecto (barras)
- Ventas mensuales (linea)
- Distribucion de estados (pie)
- Ticket promedio por proyecto (barras)

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

- **CORS tests**: Spring Security CORS se testea mejor con MockMvc o Testcontainers. Pendiente Fase 7.
- **PlanoIngestaController E2E**: Requiere SSE + CV infra. Pendiente Fase 7.
- **RBAC tests con roles custom**: Requieren setup a nivel de servicio (no endpoint REST para crear usuarios no-ADMIN). Implementado via inyeccion directa de repositorios.

---

## Resumen de Prioridad

| Fase | Tipo | Items | Esfuerzo Est. |
|------|------|-------|---------------|
| **5** | Bugs criticos frontend | 15 | 2-3 horas |
| **6** | Backend gaps (endpoints fantasma) | 10 | 3-4 horas |
| **7** | Testing Docker/Testcontainers | 2 | 1-2 dias |
| **8** | Frontend avanzado | 8 | 3-5 dias |
| **9** | Produccion | 9 | 5-7 dias |
