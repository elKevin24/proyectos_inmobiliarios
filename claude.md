# Sistema de Gestión de Proyectos Inmobiliarios - SaaS Multi-tenant

## Descripción General

Sistema integral SaaS multi-tenant para gestión de proyectos inmobiliarios y venta de terrenos/lotes. Incluye catálogo de proyectos, fases y terrenos, ingesta automática de planos con Visión Computacional (OpenCV + OCR), CRM de clientes, cotizaciones, apartados, ventas, planes de pago/amortización, cobranza, reportes ejecutivos, auditoría AOP y plano interactivo en Leaflet.

**Stack Tecnológico:**
- **Backend:** Java 25, Spring Boot 4.1.0, Spring Security 7.2.0, Hibernate 7.1.0 (`hypersistence-utils-hibernate-71`)
- **Base de Datos:** PostgreSQL 16 (JSONB + GIN indexes), Flyway (V1-V11)
- **Seguridad & Mapeo:** JWT Authentication (`jjwt 0.13.0`), MapStruct 1.6.3, Lombok, SpringDoc OpenAPI 3.0.3 (`spring-boot-jackson2`)
- **Frontend:** React 19, Vite 7, Zustand 5, React Router 7, Leaflet 1.9, Recharts 3.10, Axios, JSPDF / XLSX
- **CV Engine:** Python, FastAPI, OpenCV, Tesseract OCR, Server-Sent Events (SSE)
- **Testing:** JUnit 5, Mockito, Spring Boot Test (`RestTestClient`/`TestRestTemplate`), H2 (`MODE=PostgreSQL`), Testcontainers 2.0.5

**Arquitectura:** Hexagonal/Clean Architecture (Domain, Application, Infrastructure, Interfaces) + Microservicio CV Engine asíncrono.

---

## Estructura del Proyecto

```
src/main/java/com/inmobiliaria/terrenos/
├── domain/               # Entidades y repositorios
│   ├── entity/          # JPA Entities
│   ├── repository/      # Spring Data JPA Repositories
│   └── enums/           # Enumeraciones de dominio
├── application/         # Lógica de negocio
│   ├── dto/             # Data Transfer Objects
│   └── service/         # Servicios de aplicación
├── infrastructure/      # Infraestructura técnica
│   ├── audit/           # Auditoría AOP (@Auditable, AuditAspect)
│   ├── cv/              # PlanoStatusStore (SSE)
│   ├── scheduler/       # AuditArchiveScheduler
│   ├── security/        # JWT, SecurityConfig
│   └── tenant/          # Multi-tenancy (TenantContext, TenantFilter)
├── interfaces/          # Capa de presentación
│   ├── rest/            # 14 REST Controllers
│   └── mapper/          # MapStruct Mappers
└── shared/
    └── exception/       # GlobalExceptionHandler + Excepciones por dominio

src/main/resources/
├── db/migration/        # Flyway migrations (V1-V11)
└── application.yml      # Configuración

frontend/src/
├── components/          # Layout, MapEditor, PlanoViewer, PlanoValidator, ImageUploader
├── pages/               # 30 páginas React 19
├── services/            # Clientes Axios hacia /api/v1
└── store/               # Stores Zustand

cv-engine/
└── main.py              # Microservicio FastAPI + OpenCV + Tesseract
```

---

## Multi-Tenancy

**Implementación:** Discriminator-based (`tenant_id` en cada tabla)

**TenantContext:**
```java
ThreadLocal<Long> tenantId
```

**JWT Claims:**
```json
{
  "tenant_id": 1,
  "user_id": 123,
  "roles": ["ADMIN"]
}
```

**Filtro:** `TenantFilter` extrae `tenant_id` del JWT y lo guarda en `TenantContext`.

**Validación:** Todos los servicios validan que el recurso pertenezca al tenant actual.

---

## Módulos Implementados (14/14)

### 1. Autenticación (✅ Completo)
- **Endpoints:**
  - `POST /api/v1/auth/register` - Registro de empresa + usuario admin
  - `POST /api/v1/auth/login` - Login con JWT
  - `POST /api/v1/auth/refresh` - Refresh token
- **Entidades:** Tenant, Usuario, Rol, Permiso
- **Seguridad:** BCrypt, JWT con refresh token
- **Migración:** V1 (tenants), V2 (usuarios, roles, permisos)

### 2. Proyectos (✅ Completo)
- **Endpoints:** CRUD + cambio de estado
- **Estados:** PLANIFICACION, EN_VENTA, VENDIDO, FINALIZADO, CANCELADO
- **Contador automático:** totalTerrenos, terrenosDisponibles, apartados, vendidos
- **Migración:** V3

### 3. Terrenos/Lotes (✅ Completo)
- **Endpoints:** CRUD + cambio de estado
- **Estados:** DISPONIBLE, APARTADO, VENDIDO, RESERVADO
- **Cálculo automático de precio:** `(precioBase + ajuste) × multiplicador`
- **Coordenadas:** Soporte para plano interactivo (JSONB)
- **Migración:** V4

### 4. Fases (✅ Completo)
- **Endpoints:** CRUD por proyecto
- **Organización:** Fases numeradas dentro de un proyecto
- **Migración:** V5

### 5. Transacciones (✅ Completo)

#### Cotizaciones
- Presupuestos para clientes interesados
- Cálculo de descuentos
- Fecha de vigencia

#### Apartados
- Reserva de terreno con anticipo
- Cambia estado del terreno a APARTADO
- Duración configurable (días)
- Vencimiento automático

#### Ventas
- Conversión de apartado o venta directa
- Cambia estado del terreno a VENDIDO
- Cálculo de comisiones
- Formas de pago: CONTADO, CREDITO_BANCARIO, FINANCIAMIENTO_PROPIO

**Migración:** V6 (cotizaciones, apartados, ventas, descuentos)

### 6. Reportes y Dashboard (✅ Completo)
- **Endpoints:**
  - `GET /api/v1/reportes/dashboard` - Dashboard general
  - `GET /api/v1/reportes/proyectos` - Estadísticas por proyecto
  - `GET /api/v1/reportes/proyectos/{id}` - Stats de proyecto específico
- **Métricas:**
  - Total proyectos, terrenos, ventas
  - Porcentaje de ocupación
  - Monto total de ventas y comisiones
  - Ticket promedio
  - Tasa de conversión (cotizaciones → ventas)

### 7. Gestión de Archivos (✅ Completo)
- **Tipos:** PLANO_PROYECTO, PLANO_TERRENO, IMAGEN_PROYECTO, IMAGEN_TERRENO, DOCUMENTO_PROYECTO, CONTRATO, ESCRITURA
- **Versionamiento:** Mantiene historial de versiones (v1, v2, v3...)
- **Validación:** Tamaño máximo 10MB, extensiones permitidas (pdf, png, jpg, dwg, dxf, doc, docx)
- **Almacenamiento:** Local con nombres UUID
- **Endpoints:**
  - `POST /api/v1/archivos/upload` - Upload multipart
  - `GET /api/v1/archivos/{id}/download` - Download
  - `GET /api/v1/archivos/galeria/{proyectoId}` - Galería de imágenes
  - `GET /api/v1/archivos/versiones/{proyectoId}` - Historial de versiones
- **Migración:** V8

### 8. Plano Interactivo e Ingesta CV (✅ Completo)
- **Endpoints:**
  - `GET /api/v1/proyectos/{id}/plano-interactivo` - Retorna imagen de fondo + polígonos coloreados por estado
  - `POST /api/v1/proyectos/{id}/planos/analizar` - Inicia análisis asíncrono con CV Engine (HTTP 202)
  - `GET /api/v1/proyectos/{id}/planos/estado/{tareaId}` - Stream SSE de progreso en tiempo real
  - `POST /api/v1/proyectos/{id}/planos/confirmar` - Persiste lotes detectados y validados
- **Estructura de Coordenadas:**
```json
{
  "tipo": "poligono",
  "puntos": [
    {"x": 100, "y": 150},
    {"x": 200, "y": 150},
    {"x": 200, "y": 350},
    {"x": 100, "y": 350}
  ]
}
```

### 9. Clientes / CRM (✅ Completo)
- **Endpoints:** CRUD + búsqueda + historial unificado (`GET /api/v1/clientes/{id}/historial`)
- **Estados:** PROSPECTO, INTERESADO, COMPRADOR, INACTIVO
- **Migración:** V9

### 10. Planes de Pago, Amortizaciones y Pagos (✅ Completo)
- **Planes de Pago (`PlanPagoController`):** CRUD, consulta por venta (`GET /api/v1/planes-pago/venta/{ventaId}`), tabla de amortización y estado de cuenta.
- **Pagos (`PagoController`):** `GET /api/v1/pagos`, `POST /api/v1/pagos`, `GET /api/v1/pagos/{id}`, `GET /api/v1/pagos/venta/{ventaId}` y `DELETE /api/v1/pagos/{id}`.
- **Migración:** V10

### 11. Auditoría y Archivado (✅ Completo)
- **Endpoints (`AuditoriaController`):** `/simple`, `/critica`, `/registro/{tabla}/{id}`, `/campo/{tabla}/{id}/{campo}`, `/logins/{usuarioId}`, `/archivar`.
- **Migración:** V11

---

## Base de Datos

**PostgreSQL 16** con Flyway migrations

### Migraciones:
- **V1:** tenants
- **V2:** usuarios, roles, permisos, usuarios_roles
- **V3:** proyectos
- **V4:** terrenos (incluye coordenadas_plano JSONB + GIN index)
- **V5:** fases
- **V6:** cotizaciones, apartados, ventas, descuentos
- **V7:** datos iniciales (INSERT permisos y roles)
- **V8:** archivos
- **V9:** clientes
- **V10:** planes_pago, amortizaciones, pagos
- **V11:** audit_log_simple, audit_log_critica, audit_log_archive

### Índices Importantes:
- `tenant_id` en todas las tablas
- GIN indexes en columnas JSONB (`coordenadas_plano`, `caracteristicas`)
- Unique constraints: `(proyecto_id, numero_lote, manzana)`
- Foreign keys con `ON DELETE CASCADE` para tenant isolation

---

## Autenticación y Autorización

### Permisos por Módulo:
```
PROYECTO_VER, PROYECTO_CREAR, PROYECTO_EDITAR, PROYECTO_ELIMINAR
FASE_VER, FASE_CREAR, FASE_EDITAR, FASE_ELIMINAR
TERRENO_VER, TERRENO_CREAR, TERRENO_EDITAR, TERRENO_ELIMINAR
CLIENTE_VER, CLIENTE_CREAR, CLIENTE_EDITAR, CLIENTE_ELIMINAR
COTIZACION_VER, COTIZACION_CREAR, COTIZACION_ELIMINAR
APARTADO_VER, APARTADO_CREAR, APARTADO_EDITAR, APARTADO_ELIMINAR
VENTA_VER, VENTA_CREAR, VENTA_EDITAR, VENTA_ELIMINAR
PLAN_PAGO_VER, PLAN_PAGO_CREAR, PLAN_PAGO_EDITAR, PLAN_PAGO_ELIMINAR
PAGO_VER, PAGO_REGISTRAR, PAGO_ELIMINAR
REPORTE_VER
ARCHIVO_VER, ARCHIVO_CREAR, ARCHIVO_ELIMINAR
ADMIN (acceso total)
```

### Roles Sugeridos:
- **ADMIN:** Todos los permisos
- **VENDEDOR:** Ver proyectos/terrenos, crear cotizaciones/apartados/ventas
- **GERENTE:** Ver reportes, gestionar proyectos
- **SOPORTE:** Ver información, subir archivos

---

## Configuración de Entorno

### Variables de Entorno:
```bash
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=terrenos_saas
DB_USER=postgres
DB_PASSWORD=password

# JWT
JWT_SECRET=tu-secret-key-muy-segura-de-al-menos-256-bits
JWT_EXPIRATION=86400000      # 24 horas
JWT_REFRESH_EXPIRATION=604800000  # 7 días

# File Storage
FILE_UPLOAD_DIR=uploads
```

---

## Flujos de Negocio Principales

### 1. Proceso de Venta Completo
```
1. Cliente interesado → Crear Cotización
2. Cliente decide apartar → Crear Apartado
   - Estado terreno: DISPONIBLE → APARTADO
   - Se registra anticipo
3. Cliente completa pago → Crear Venta
   - Apartado: VIGENTE → CONVERTIDO
   - Estado terreno: APARTADO → VENDIDO
   - Se calculan comisiones
4. Si es financiamiento propio → Crear Plan de Pago → Registrar Pagos
```

### 2. Gestión de Plano Interactivo
```
1. Subir imagen de plano → POST /archivos/upload (tipo: PLANO_PROYECTO)
2. Analizar con CV Engine → POST /proyectos/{id}/planos/analizar + SSE /estado/{tareaId}
3. Confirmar lotes o crear terrenos manualmente → POST /proyectos/{id}/planos/confirmar
4. Frontend obtiene plano interactivo → GET /proyectos/{id}/plano-interactivo
5. Renderizar en Leaflet con polígonos coloreados según estado
```

### 3. Versionamiento de Planos
```
1. Subir plano v1 → nombre: "plano_maestro.pdf"
2. Sistema crea versión 1, esActivo=true
3. Subir plano v2 → mismo nombre "plano_maestro.pdf"
4. Sistema:
   - Desactiva v1 (esActivo=false)
   - Crea v2 (version=2, esActivo=true)
5. Consultar historial → GET /archivos/versiones/{proyectoId}?nombreOriginal=plano_maestro.pdf
```

---

## Testing

### Suite Automatizada (JUnit 5 + Spring Boot Test)
```bash
./mvnw test
```
Incluye 144+ tests E2E y unitarios (incluyendo aislamiento Multi-tenant y RBAC).

### Colección Postman/REST Client
Ver: `api-examples.http` - Contiene ejemplos de todos los endpoints

### Swagger UI
```
http://localhost:8080/swagger-ui.html
http://localhost:8080/api-docs
```

---

## Decisiones Técnicas Importantes

### 1. Soft Delete
- Todas las entidades tienen campo `deleted BOOLEAN DEFAULT FALSE`
- Filtrado automático con `@SQLRestriction("deleted = false")`
- Permite auditoría y recuperación de datos

### 2. Precio de Terrenos
- Precio final calculado: `(precioBase + precioAjuste) × precioMultiplicador`
- Permite ajustes individuales (esquina, vista, etc.)
- Se calcula automáticamente en `@PrePersist`

### 3. Contadores de Proyecto
- Actualizados automáticamente al cambiar estado de terreno
- Evita consultas COUNT en cada request
- Métrica en tiempo real

### 4. JSONB para Flexibilidad
- `coordenadas_plano`: Polígonos del plano interactivo
- `caracteristicas`: Datos adicionales no estructurados
- Mapeados con `@Type(JsonBinaryType.class)` de `hypersistence-utils-hibernate-71`

### 5. Excepciones Tipadas por Dominio
- Cada módulo cuenta con su excepción específica en `shared/exception/*Exception.java` que extiende `BusinessException`, manejada centralmente por `GlobalExceptionHandler`.

---

## Próximos Pasos Sugeridos

Ver: `ROADMAP.md` y `ROADMAP_PENDING.md` para la hoja de ruta actualizada.

---

**Última Actualización:** 2026-11-09
**Versión del Sistema:** 1.6.0-beta
**Autor:** Kevin
