---
name: flyway-testcontainer-check
description: Valida migraciones Flyway (V12+) y genera tests de integracion con Testcontainers PostgreSQL para columnas JSONB (coordenadas_plano, caracteristicas), indices GIN e ingesta SSE.
---

# Flyway & Testcontainers Check

Usa esta skill para trabajar en la **Fase 6 (Testing Docker/Testcontainers)** o al añadir nuevas migraciones SQL:

1. **Inmutabilidad de Flyway**:
   - Nunca edites `V1__` a `V11__`. Toda alteración de esquema debe ir en `src/main/resources/db/migration/V12__<descripcion>.sql`.
   - Incluye siempre `tenant_id BIGINT NOT NULL REFERENCES tenants(id)` y `deleted BOOLEAN DEFAULT FALSE` en tablas nuevas.
2. **Tests con PostgreSQL Real (`Testcontainers`)**:
   - Para validar queries nativas `JSONB` (`@Type(JsonBinaryType.class)` de `hypersistence-utils-hibernate-71`) e índices `GIN`, extiende `BaseIntegrationTest` con `@Testcontainers` y `PostgreSQLContainer<?>`.
3. **Tests de Ingesta de Planos (`PlanoIngestaController`)**:
   - Simula la respuesta de `cv-engine` (`POST /api/cv/extract-lots`) mockeando `RestClient` o levantando el contenedor `cv-engine`, y verifica las transiciones del `SseEmitter` en `PlanoStatusStore`.
