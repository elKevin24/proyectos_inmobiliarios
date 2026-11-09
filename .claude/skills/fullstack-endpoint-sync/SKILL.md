---
name: fullstack-endpoint-sync
description: Crea o sincroniza un endpoint REST en Spring Boot 4 (con aislamiento tenant_id, @PreAuthorize, MapStruct, excepciones de dominio y test) con su servicio Axios y store Zustand en React 19.
disable-model-invocation: true
---

# Fullstack Endpoint Sync (Terrenos SaaS)

Sigue este flujo vertical cada vez que agregues o modifiques un endpoint REST en el proyecto:

1. **Seguridad y Multi-tenancy (Backend)**:
   - Extraer siempre `Long tenantId = TenantContext.getCurrentTenant();` en el `Service`.
   - Filtrar por `tenantId` y `deleted = false` en el `Repository`.
   - Anotar el método en el `Controller` con `@PreAuthorize("hasAnyAuthority('<MODULO>_<ACCION>', 'ADMIN')")` y verificar que el permiso exista en `src/main/resources/db/migration/V7__*.sql` y `src/test/resources/data-test.sql`.
2. **Excepciones y Mapeo**:
   - Lanzar la excepción específica del dominio (`PagoException`, `TerrenoException`, etc.) en `shared/exception/`.
   - Usar `MapStruct` (`interfaces/mapper/`) para convertir entre `Entity` y `DTO`.
3. **Cliente Frontend (React 19)**:
   - Exponer el método en `frontend/src/services/<modulo>Service.js` usando la instancia `api` (`axios`).
   - Conectar la acción en `frontend/src/store/<modulo>Store.js` (Zustand) y notificar con `react-hot-toast`.
4. **Verificación**:
   - Agregar test unitario/integración en `src/test/java/com/inmobiliaria/terrenos/` y ejecutar `./mvnw test`.
   - Ejecutar `cd frontend && npm run lint && npm run build`.
