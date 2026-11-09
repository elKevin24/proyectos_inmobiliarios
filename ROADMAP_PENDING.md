# Roadmap Pendiente - Siguientes Fases

## Resumen

**Fecha:** 2026-11-09
**Estado:** Fases 5, 7 y 8 completadas — Backend + Frontend sincronizados
**Versión actual:** 1.6.0-beta
**Tests backend:** 174 tests (Unitarios + Controllers + E2E), 0 failures
**Frontend:** 30 páginas (`PagosList` y `PlanPagoForm` incluidos), build limpio

---

## ✅ COMPLETADO Recientemente

### Fase 5 - Bugs Críticos Frontend (Completada)
- Creado `ProyectoDetail.jsx` y corregidas rutas en `App.jsx` (`ventas/:ventaId/pagos/nuevo`).
- Sincronizados servicios (`pagoService`, `planPagoService`, `ventaService`, `terrenoService`, `proyectoService`, `archivoService`).
- Eliminados imports muertos en `FasesList.jsx`, `FaseForm.jsx` y `Layout.jsx`.

### Fase 7 - Frontend Avanzado (Completada)
- **Gráficos en Dashboard:** Integrado `recharts` con 4 gráficos interactivos (ventas mensuales, distribución general, estados de lotes por proyecto, ticket promedio).
- **Notificaciones Toast:** Estandarizado `react-hot-toast`.
- **Paginación y UX:** Implementada paginación client-side, spinners de carga, modo oscuro (Dark Mode) y menú móvil off-canvas.

### Fase 8 - Backend Gaps y Excepciones de Dominio (Completada)
- **Pagos REST y UI:** Implementados `GET /api/v1/pagos`, `GET /api/v1/pagos/{id}`, `GET /api/v1/pagos/venta/{ventaId}` y `DELETE /api/v1/pagos/{id}` en `PagoController.java` + `pagoService.js` + vista `PagosList.jsx`.
- **Planes de Pago:** Conectado `GET /api/v1/planes-pago/venta/{ventaId}` en `planPagoService.js` y creada vista `PlanPagoForm.jsx`.
- **Auditoría Store:** Conectados `GET /auditoria/campo/{tabla}/{id}/{campo}` (`fetchHistorialCampo`) y `GET /auditoria/logins/{usuarioId}` (`fetchUltimosLogins`) en `auditoriaStore.js`.
- **Jerarquía de Excepciones:** Creadas 13 excepciones específicas por dominio (`*Exception.java`) manejadas en `GlobalExceptionHandler.java` y cubiertas con `PagoControllerTest` y `PlanPagoControllerTest`.

---

## ⏳ Fase 6 - Testing Docker/Testcontainers (PENDIENTE)

### 6.1 PlanoIngestaController Tests
**Cobertura pendiente:** 3 endpoints (excluidos de E2E en H2 por dependencia de infraestructura asíncrona SSE + CV Engine).

| Endpoint | Descripción |
|----------|-------------|
| `POST /api/v1/proyectos/{id}/planos/analizar` | Inicia análisis asíncrono con CV Engine y retorna `tareaId` (HTTP 202) |
| `GET /api/v1/proyectos/{id}/planos/estado/{tareaId}` | Canal SSE (`SseEmitter`) de progreso en tiempo real |
| `POST /api/v1/proyectos/{id}/planos/confirmar` | Persiste lotes validados con polígonos `JSONB` |

**Requiere:** Mock de `RestClient` hacia `cv-engine` o contenedor Docker con OpenCV + Tesseract.

### 6.2 Integration Tests con PostgreSQL Real (`Testcontainers`)
Reemplazar H2 por PostgreSQL 16 vía Testcontainers para:
- Validar queries nativas `JSONB` (`coordenadas_plano`, `caracteristicas`).
- Validar índices `GIN` y restricciones de clave foránea con `tenant_id`.

---

## ⏳ Fase 9 - Producción y Observabilidad (PENDIENTE)

### 9.1 Observabilidad
| Tarea | Descripción |
|-------|-------------|
| Actuator endpoints | Exponer y asegurar `/actuator/health`, `/actuator/metrics`, `/actuator/info` |
| Prometheus metrics | Exportar métricas de negocio y JVM con Micrometer |
| Logging estructurado | Formato JSON para agregación en ELK / Loki |

### 9.2 Performance e Infraestructura
| Tarea | Descripción |
|-------|-------------|
| Frontend en Docker Compose | Agregar contenedor `frontend` (Nginx multi-stage) a `docker-compose.yml` |
| Caché Redis | Caché para reportes frecuentes y sesiones |
| Load testing con k6 | Prueba de estrés con 100 usuarios concurrentes |
| Tuning HikariCP e Índices | Optimización de pool de conexiones y `EXPLAIN ANALYZE` en queries de reportes |

### 9.3 Seguridad en Producción
| Tarea | Descripción |
|-------|-------------|
| Rate limiting | Limitar peticiones por IP y `tenant_id` (Bucket4j / Redis) |
| CORS estricto | Restringir orígenes permitidos por variable de entorno |
| Security Headers | Configurar CSP, HSTS y `X-Frame-Options` en Spring Security / Nginx |

---

## Resumen de Prioridad Actual

| Fase | Tipo | Items | Esfuerzo Est. |
|------|------|-------|---------------|
| **6** | Testing Docker/Testcontainers + SSE CV Engine | 2 | 1-2 días |
| **9** | Producción (Observabilidad, Redis, Docker Frontend, Seguridad) | 10 | 5-7 días |
