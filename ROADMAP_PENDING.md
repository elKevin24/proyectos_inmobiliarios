# Roadmap Pendiente - Siguientes Fases

## Resumen

**Fecha:** 2026-07-16
**Estado:** Fase 2 Security completada — suite total: ~129 tests, 0 failures
**Version actual:** 1.3.0-beta

---

## ✅ COMPLETADO en esta sesion

### Fase 2 - Testing de Seguridad y Aislamiento

**Archivos creados:**
- `MultiTenancyE2ETest.java` — 6 tests de aislamiento multi-tenancy
- `RbacE2ETest.java` — 9 tests RBAC (403 Forbidden + 4xx para tokens inválidos)

**Fixes aplicados en `data-test.sql`:**
- Agregados: `ARCHIVO_VER`, `ARCHIVO_ELIMINAR`, `VENTA_ELIMINAR`, `APARTADO_ELIMINAR`, `REPORTE_VER`, `ADMIN`

**Tests de Multi-tenancy (6 tests):**
| Test | Verificación |
|------|-------------|
| Tenant B no puede GET proyecto de A | 404 (no existe para ese tenant) |
| Tenant B no puede GET terreno de A | 404 |
| Tenant B no puede GET cliente de A | 404 |
| Listado de Tenant B no incluye recursos de A | Array filtrado por tenant |
| Tenant B no puede PUT proyecto de A | 404 |
| Tenant B no puede DELETE proyecto de A | 404 + verificar que A sigue vivo |

**Tests RBAC (9 tests):**
| Test | Permiso faltante | Status esperado |
|------|-----------------|----------------|
| VENDEDOR GET /proyectos | PROYECTO_VER | 403 |
| VENDEDOR POST /proyectos | PROYECTO_CREAR | 403 |
| VENDEDOR GET /clientes | CLIENTE_VER | 403 |
| VENDEDOR GET /reportes | REPORTE_VER | 403 |
| VENDEDOR GET /auditoria | ADMIN | 403 |
| Sin permisos GET /proyectos,/terrenos,/clientes | Todos | 403 |
| VENDEDOR con COTIZACION_VER GET /cotizaciones | Tiene permiso | 200 ✓ |
| Token inválido | — | 4xx |
| Sin token | — | 4xx |

**Técnica usada para RBAC:**
- Inyección directa de `RolRepository`, `UsuarioRepository`, `JwtService` en el test
- Crear roles con subconjunto de permisos via repositorios (sin endpoint REST)
- Generar JWT directamente con `jwtService.generateTokenWithTenant()` para evitar login

---

## Fase 3 - Testing con Docker/Testcontainers (PENDIENTE)

### 3.1 PlanoIngestaController tests

**Endpoint coverage:** 3 endpoints (excluidos de E2E por dependencia de infraestructura)

| Endpoint | Descripcion |
|----------|-------------|
| POST /api/v1/planos/ingestar | Requiere SSE + CV analysis |
| GET /api/v1/planos/estado/{id} | Requiere SSE |
| GET /api/v1/planos/{id} | |

**Requiere:** Mock del servicio de análisis de planos o Docker con OpenCV + Tesseract.

### 3.2 Integration tests con PostgreSQL real

Reemplazar H2 por PostgreSQL via Testcontainers para:
- Validar queries JSONB reales (actualmente ignoradas en H2)
- Validar GIN indexes
- Validar comportamiento específico de PostgreSQL

---

## Fase 4 - Produccion (PENDIENTE)

### 4.1 CI/CD Pipeline

| Tarea | Descripcion |
|-------|-------------|
| GitHub Actions workflow | `mvn test` en cada PR |
| Docker image build + push | En merge a main |
| Deploy automatico | A staging |

### 4.2 Observabilidad

| Tarea | Descripcion |
|-------|-------------|
| Actuator endpoints | `/health`, `/metrics`, `/info` |
| Prometheus metrics | Exportar métricas de negocio |
| Logging estructurado | JSON logs para ELK |

### 4.3 Performance

| Tarea | Descripcion |
|-------|-------------|
| Load testing con k6 | 100 usuarios concurrentes |
| Cache Redis | Para reportes frecuentes |
| Connection pooling tuning | HikariCP config |

---

## Exclusiones

- **CORS tests**: Spring Security CORS se testea mejor con MockMvc o Testcontainers. Pendiente Fase 3.
- **PlanoIngestaController E2E**: Requiere SSE + CV infra. Pendiente Fase 3.
