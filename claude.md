# Sistema de Gestión de Proyectos Inmobiliarios - SaaS Multi-tenant

## Descripción General

Sistema backend para gestión de proyectos inmobiliarios con venta de terrenos/lotes. Incluye gestión de proyectos, fases, terrenos, cotizaciones, apartados, ventas, reportes y plano interactivo.

**Stack Tecnológico:**
- Java 21
- Spring Boot 3.4.0
- PostgreSQL 16
- JWT Authentication
- MapStruct
- Flyway
- OpenAPI/Swagger

**Arquitectura:** Hexagonal/Clean Architecture (Domain, Application, Infrastructure, Interfaces)

---

## Estructura del Proyecto

```
src/main/java/com/inmobiliaria/terrenos/
├── domain/               # Entidades y repositorios
│   ├── entity/          # JPA Entities
│   ├── repository/      # Spring Data JPA Repositories
│   └── enums/          # Enumeraciones de dominio
├── application/         # Lógica de negocio
│   ├── dto/            # Data Transfer Objects
│   └── service/        # Servicios de aplicación
├── infrastructure/      # Infraestructura técnica
│   ├── security/       # JWT, SecurityConfig
│   └── tenant/         # Multi-tenancy (TenantContext)
└── interfaces/          # Capa de presentación
    ├── rest/           # REST Controllers
    └── mapper/         # MapStruct Mappers

src/main/resources/
├── db/migration/       # Flyway migrations (V1-V8)
└── application.yml     # Configuración
```

---

## Multi-Tenancy

**Implementación:** Discriminator-based (tenant_id en cada tabla)

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

**Interceptor:** TenantInterceptor extrae tenant_id del JWT y lo guarda en TenantContext

**Validación:** Todos los servicios validan que el recurso pertenezca al tenant actual

---

## Módulos Implementados

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

**Migración:** V6 (cotizaciones, apartados, ventas)

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

**Migración:** V8

### 8. Plano Interactivo (✅ Completo)
- **Endpoint:** `GET /api/v1/proyectos/{id}/plano-interactivo`
- **Funcionalidad:**
  - Retorna imagen de plano de fondo
  - Lista de terrenos con coordenadas (polígonos)
  - Color automático según estado:
    - Verde (#4CAF50) = DISPONIBLE
    - Amarillo (#FFC107) = APARTADO
    - Rojo (#F44336) = VENDIDO
    - Azul (#2196F3) = RESERVADO
  - Estadísticas del proyecto
  - Datos completos de cada terreno

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

---

## Base de Datos

**PostgreSQL 16** con Flyway migrations

### Migraciones:
- **V1:** tenants
- **V2:** usuarios, roles, permisos, usuarios_roles
- **V3:** proyectos
- **V4:** terrenos (incluye coordenadas_plano JSONB)
- **V5:** fases
- **V6:** cotizaciones, apartados, ventas
- **V7:** (Pendiente si existe)
- **V8:** archivos

### Índices Importantes:
- tenant_id en todas las tablas
- GIN indexes en columnas JSONB (coordenadas_plano, caracteristicas)
- Unique constraints: (proyecto_id, numero_lote, manzana)
- Foreign keys con ON DELETE CASCADE para tenant isolation

---

## Autenticación y Autorización

### Permisos por Módulo:
```
PROYECTO_VER, PROYECTO_CREAR, PROYECTO_EDITAR, PROYECTO_ELIMINAR
TERRENO_VER, TERRENO_CREAR, TERRENO_EDITAR, TERRENO_ELIMINAR
COTIZACION_VER, COTIZACION_CREAR, COTIZACION_ELIMINAR
APARTADO_VER, APARTADO_CREAR, APARTADO_EDITAR, APARTADO_ELIMINAR
VENTA_VER, VENTA_CREAR, VENTA_EDITAR, VENTA_ELIMINAR
REPORTE_VER
ARCHIVO_VER, ARCHIVO_SUBIR, ARCHIVO_ELIMINAR
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

### application.yml:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:terrenos_saas}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:password}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true

  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

file:
  storage:
    upload-dir: ${FILE_UPLOAD_DIR:uploads}
    max-file-size: 10485760
    allowed-extensions:
      - pdf
      - png
      - jpg
      - jpeg
      - dwg
      - dxf
      - doc
      - docx

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}
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
```

### 2. Gestión de Plano Interactivo
```
1. Subir imagen de plano → POST /archivos/upload (tipo: PLANO_PROYECTO)
2. Crear terrenos con coordenadas → POST /terrenos (incluir coordenadasPlano)
3. Frontend obtiene plano interactivo → GET /proyectos/{id}/plano-interactivo
4. Renderizar SVG/Canvas con polígonos coloreados según estado
5. Click en terreno muestra detalles completos
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

## Ejemplos de Uso

### Registro de Empresa
```bash
POST /api/v1/auth/register
{
  "nombreEmpresa": "Inmobiliaria Los Pinos",
  "emailEmpresa": "contacto@inmobiliaria-pinos.com",
  "nombre": "Carlos",
  "apellido": "Mendoza",
  "email": "carlos@inmobiliaria-pinos.com",
  "password": "Admin123!"
}
```

### Crear Proyecto
```bash
POST /api/v1/proyectos
Authorization: Bearer {token}
{
  "nombre": "Residencial Los Pinos",
  "direccion": "Carretera México-Cuernavaca Km 42",
  "ciudad": "Cuernavaca",
  "totalTerrenos": 50,
  "estadoProyecto": "PLANIFICACION"
}
```

### Crear Terreno con Coordenadas
```bash
POST /api/v1/terrenos
Authorization: Bearer {token}
{
  "proyectoId": 1,
  "numeroLote": "A-001",
  "manzana": "A",
  "area": 200.50,
  "precioBase": 500000.00,
  "coordenadasPlano": {
    "tipo": "poligono",
    "puntos": [
      {"x": 100, "y": 150},
      {"x": 200, "y": 150},
      {"x": 200, "y": 350},
      {"x": 100, "y": 350}
    ]
  }
}
```

### Obtener Plano Interactivo
```bash
GET /api/v1/proyectos/1/plano-interactivo
Authorization: Bearer {token}
```

---

## Testing

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
- Los repositorios filtran automáticamente con `@Where(clause = "deleted = false")`
- Permite auditoría y recuperación de datos

### 2. Precio de Terrenos
- Precio final calculado: `(precioBase + precioAjuste) × precioMultiplicador`
- Permite ajustes individuales (esquina, vista, etc.)
- Se calcula automáticamente en @PrePersist

### 3. Contadores de Proyecto
- Actualizados automáticamente al cambiar estado de terreno
- Evita consultas COUNT en cada request
- Métrica en tiempo real

### 4. JSONB para Flexibilidad
- `coordenadas_plano`: Polígonos del plano interactivo
- `caracteristicas`: Datos adicionales no estructurados
- Permite evolución del schema sin migraciones

### 5. Versionamiento de Archivos
- Mismo nombre + mismo proyecto = nueva versión
- Solo una versión activa a la vez
- Historial completo disponible

---

## Problemas Conocidos y Soluciones

### 1. MapStruct con JSONB
**Problema:** MapStruct no convierte automáticamente objetos a JSON string
**Solución:** TerrenoMapper como clase abstracta con métodos helper para conversión JSON

### 2. Hibernate Types para JSONB
**Problema:** JPA no soporta JSONB nativamente
**Solución:** Usar `@Type(JsonBinaryType.class)` de hibernate-types

### 3. Multi-tenant en Queries
**Problema:** Olvidar filtrar por tenant_id puede exponer datos de otros tenants
**Solución:** Todos los métodos de repositorio incluyen tenantId en el nombre y parámetros

---

## Próximos Pasos Sugeridos

Ver: `ROADMAP.md` para la hoja de ruta completa

---

## Commits Importantes

```bash
# Autenticación
feat: Implementar sistema de autenticación JWT completo

# Proyectos
feat: Implementar gestión completa de proyectos inmobiliarios

# Terrenos
feat: Implementar gestión completa de terrenos/lotes

# Fases
feat: Implementar gestión completa de fases de proyectos

# Transacciones
feat: Implementar módulos completos de transacciones (Cotizaciones, Apartados, Ventas)

# Reportes
feat: Implementar módulo completo de Reportes y Dashboard

# Archivos
feat: Implementar gestión completa de archivos con versionamiento

# Plano Interactivo
feat: Implementar sistema de plano interactivo con coordenadas de terrenos
```

---

## Contacto y Documentación

- **Branch de Desarrollo:** `claude/lee-los-ar-016DCnn3qZgVVSpAFqnXDMRy`
- **Documentos de Diseño:** Ver README.md, FASES_Y_ETAPAS.md, DISEÑO_SISTEMA_SAAS_TERRENOS.md
- **Swagger:** http://localhost:8080/swagger-ui.html

---

**Última Actualización:** 2025-01-18
**Versión del Sistema:** 1.0.0-beta
**Autor:** Kevin
