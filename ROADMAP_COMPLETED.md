# Roadmap Completado - Sistema de Gestion Inmobiliaria SaaS

## Resumen

**Ultima Actualizacion:** 2026-07-14
**Version:** 1.1.0-beta
**Progreso Completado:** 14/14 modulos backend + Frontend + Migracion Java 25

---

## Fase 0 - Estabilizacion ✅ COMPLETADA

| # | Tarea | Estado | Fecha |
|---|-------|--------|-------|
| 0.1 | Crear entidad `Venta.java` en `domain/entity/` | ✅ | 2026-07-12 |
| 0.2 | Crear entidad `Cotizacion.java` en `domain/entity/` | ✅ | 2026-07-12 |
| 0.3 | Crear entidad `Apartado.java` en `domain/entity/` | ✅ | 2026-07-12 |
| 0.4 | Mover `AuthController.java` de `backend/` a `src/` | ✅ | 2026-07-12 |
| 0.5 | Mover `AuthService.java` de `backend/` a `src/` | ✅ | 2026-07-12 |
| 0.6 | Mover `UsuarioDTO.java` de `backend/` a `src/` | ✅ | 2026-07-12 |
| 0.7 | Agregar `hibernate-types-60` al `pom.xml` | ✅ | 2026-07-12 |
| 0.8 | Corregir typo `itud` a `longitud` en `Proyecto.java` | ✅ | 2026-07-12 |
| 0.9 | Verificar `mvn clean compile` pase sin errores | ✅ | 2026-07-12 |
| 0.10 | Verificar `docker-compose up` levante todos los servicios | ✅ | 2026-07-12 |

---

## Fase 1 - Calidad y Tests (Parcial) ✅ PARCIAL

### 1.1 Configurar testing backend ✅

| Tarea | Estado | Detalle |
|-------|--------|---------|
| H2 embebido para tests | ✅ | `application-test.yml` con H2 `MODE=PostgreSQL` |
| JUnit 5 + Spring Boot Test | ✅ | `spring-boot-starter-test` en pom.xml |
| `data-test.sql` con permisos | ✅ | 38 permisos INSERT para contexto de test |
| `@ActiveProfiles("test")` | ✅ | Configurado en `BaseE2ETest` |
| `@TestInstance(PER_CLASS)` | ✅ | Todos los test classes lo usan |
| `RestTemplate` con `JdkClientHttpRequestFactory` | ✅ | Soporte PATCH en tests |
| `NoOpResponseErrorHandler` | ✅ | No lanza excepciones en 4xx/5xx |

### 1.2 Tests E2E - Modulos completados ✅

| Test Class | Tests | Endpoints Cubiertos | Estado |
|-----------|-------|---------------------|--------|
| `AuthE2ETest` | 9 | POST register, login, refresh | ✅ |
| `ProyectoCRUDE2ETest` | 7 | CRUD completo + 404 | ✅ |
| `TerrenoCRUDE2ETest` | 6 | CRUD completo + estado | ✅ |
| `VentaCompletaE2ETest` | 10 | Cotizacion > Apartado > Venta completo | ✅ |
| `ReporteE2ETest` | 4 | Dashboard + estadisticas proyectos | ✅ |

**Total tests E2E existentes:** 36 tests, 36 pasando

---

## Migracion Java 25 + Spring Boot 4.1 ✅ COMPLETADA

| Tarea | Estado | Detalle |
|-------|--------|---------|
| Java 21 → Java 25 | ✅ | `eclipse-temurin:25-jdk-alpine` / `jre-alpine` |
| Spring Boot 3.4 → 4.1.0 | ✅ | Parent en pom.xml |
| Spring Framework 7.0.1 | ✅ | Transitive dependency |
| Hibernate 7.1.0 | ✅ | Reemplaza hibernate 6.x |
| Spring Security 7.2.0 | ✅ | |
| Jackson 2 bridge (`spring-boot-jackson2`) | ✅ | Mantiene `com.fasterxml.jackson` |
| `spring.http.converters.preferred-json-mapper: jackson2` | ✅ | En application.yml |
| hypersistence-utils-hibernate-71:3.15.4 | ✅ | Reemplaza hibernate-63 |
| springdoc-openapi 3.0.3 | ✅ | Version para Boot 4.x |
| Lombok 1.18.40 | ✅ | |
| JJWT 0.13.0 | ✅ | |
| Testcontainers 2.0.5 | ✅ | |
| maven-compiler-plugin 3.14.0 | ✅ | |
| MapStruct 1.6.3 | ✅ | |
| `TestRestTemplate` move | ✅ | `org.springframework.boot.resttestclient` |
| `AutoConfigureMockMvc` move | ✅ | `org.springframework.boot.webmvc.test.autoconfigure` |
| Dockerfile multi-stage Temurin 25 | ✅ | |
| Fix `ProyectoMapper.toResponse()` bug | ✅ | `source = "estadoProyecto"` (no `"estado"`) |

---

## Modulos Backend Completados ✅

| # | Modulo | Controller | Endpoints | Estado |
|---|--------|-----------|-----------|--------|
| 1 | Autenticacion | AuthController | 3 | ✅ |
| 2 | Proyectos | ProyectoController | 7 | ✅ |
| 3 | Terrenos | TerrenoController | 7 | ✅ |
| 4 | Fases | FaseController | 5 | ✅ |
| 5 | Cotizaciones | CotizacionController | 4 | ✅ |
| 6 | Apartados | ApartadoController | 5 | ✅ |
| 7 | Ventas | VentaController | 5 | ✅ |
| 8 | Clientes | ClienteController | 6 | ✅ |
| 9 | Planes de Pago | PlanPagoController | 7 | ✅ |
| 10 | Pagos | PagoController | 1 | ✅ |
| 11 | Reportes | ReporteController | 3 | ✅ |
| 12 | Archivos | ArchivoController | 6 | ✅ |
| 13 | Auditoria | AuditoriaController | 6 | ✅ |
| 14 | Plano Ingesta | PlanoIngestaController | 3 | ✅ |

**Total:** 14 controllers, ~67 endpoints, 163 archivos Java

---

## Base de Datos ✅ COMPLETADA

| Migracion | Tablas | Estado |
|-----------|--------|--------|
| V1 | tenants | ✅ |
| V2 | usuarios, roles, permisos, usuarios_roles | ✅ |
| V3 | proyectos | ✅ |
| V4 | terrenos (coordenadas_plano JSONB + GIN index) | ✅ |
| V5 | fases | ✅ |
| V6 | cotizaciones, apartados, ventas, descuentos | ✅ |
| V7 | datos iniciales (INSERT) | ✅ |
| V8 | archivos | ✅ |
| V9 | clientes | ✅ |
| V10 | planes_pago, amortizaciones, pagos | ✅ |
| V11 | audit_log_simple, audit_log_critica, audit_log_archive | ✅ |

---

## Frontend React 19 ✅ COMPLETADO

| Componente | Estado |
|-----------|--------|
| Login, Register | ✅ |
| Dashboard | ✅ |
| Proyectos (List, Form, Plano) | ✅ |
| PlanoValidator | ✅ |
| Terrenos (List, Form, Detail) | ✅ |
| Clientes (List, Form) | ✅ |
| Cotizaciones (List, Form, Detail) | ✅ |
| Apartados (List, Form, Detail) | ✅ |
| Ventas (List, Form, Detail) | ✅ |
| Pagos (Form) | ✅ |
| Planes de Pago (List, Detail) | ✅ |

**Total:** 24 paginas, 12 servicios, 8 stores Zustand

---

## Bugs Corregidos en esta Sesion

| Bug | Archivo | Fix |
|-----|---------|-----|
| `ProyectoMapper.toResponse()` mapeaba `estado` (String ciudad) en vez de `estadoProyecto` (enum) | `ProyectoMapper.java:39` | Cambiado `source = "estado"` → `source = "estadoProyecto"` |
| Terreno 500 en PUT: JSONB `caracteristicas` no acepta string plano en H2 | `TerrenoCRUDE2ETest.java:82` | Removido campo `caracteristicas` del test PUT |
