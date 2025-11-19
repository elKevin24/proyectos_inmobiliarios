# 🏗️ Arquitectura del Sistema

Documentación técnica de la arquitectura del Sistema de Gestión de Proyectos Inmobiliarios.

## 📐 Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Pages      │  │  Components  │  │   Services   │          │
│  │              │  │              │  │              │          │
│  │ - Dashboard  │  │ - Layout     │  │ - authService│          │
│  │ - Terrenos   │  │ - MapEditor  │  │ - terrenoSvc │          │
│  │ - Proyectos  │  │ - PlanoViewer│  │ - proyectoSvc│          │
│  │ - Clientes   │  │ - ImageUpload│  │ - clienteSvc │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         ↓                                     ↓                  │
│  ┌──────────────────────────────────────────────────┐          │
│  │           State Management (Zustand)              │          │
│  └──────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↕ HTTP/REST + JWT
┌─────────────────────────────────────────────────────────────────┐
│                     API GATEWAY (Spring Boot)                    │
│  ┌──────────────────────────────────────────────────┐          │
│  │         Security Filter Chain (JWT)               │          │
│  │  - JwtAuthenticationFilter                        │          │
│  │  - TenantFilter (Multi-tenancy)                   │          │
│  └──────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Controllers  │→ │   Services   │→ │ Repositories │          │
│  │   (REST)     │  │  (Business   │  │  (JPA/Data)  │          │
│  │              │  │    Logic)    │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         ↓                 ↓                    ↓                 │
│  ┌──────────────────────────────────────────────────┐          │
│  │         Infrastructure Layer                      │          │
│  │  - Auditoría (AOP)                                │          │
│  │  - Multi-tenancy (TenantContext)                  │          │
│  │  - Mappers (MapStruct)                            │          │
│  └──────────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE (PostgreSQL)                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Tenants    │  │   Projects   │  │  Terrenos    │          │
│  │   Users      │  │   Clientes   │  │  Ventas      │          │
│  │   Roles      │  │   Pagos      │  │  Auditoría   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

## 🎯 Arquitectura Frontend (React)

### Estructura de Capas

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │
│         (Pages & Components)            │
│  - Renderizado de UI                    │
│  - Manejo de eventos del usuario        │
│  - Validación de formularios            │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│         State Management Layer          │
│              (Zustand Stores)           │
│  - Estado global de la aplicación       │
│  - Caché de datos                       │
│  - Sincronización de estado             │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│         Service/API Layer               │
│         (Axios Services)                │
│  - Comunicación con backend             │
│  - Manejo de tokens JWT                 │
│  - Transformación de datos              │
└─────────────────────────────────────────┘
```

### Patrones de Diseño Frontend

#### 1. **Component Composition**
```jsx
<Layout>
  <ProtectedRoute>
    <TerrenosList />
  </ProtectedRoute>
</Layout>
```

#### 2. **Custom Hooks** (Zustand)
```javascript
const { terrenos, fetchTerrenos } = useTerrenoStore();
```

#### 3. **Container/Presenter Pattern**
- **Container**: Páginas que manejan lógica (TerrenosList)
- **Presenter**: Componentes puros de UI (MapEditor)

#### 4. **Service Layer Pattern**
```javascript
// Centralización de lógica de API
terrenoService.getAll()
  .then(data => /* handle */)
```

## 🏢 Arquitectura Backend (Spring Boot)

### Arquitectura Hexagonal (Ports & Adapters)

```
┌──────────────────────────────────────────────────────────┐
│                    INTERFACES LAYER                       │
│                 (Driving Adapters)                        │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │ REST         │  │  Security    │                      │
│  │ Controllers  │  │  Filters     │                      │
│  └──────────────┘  └──────────────┘                      │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                       │
│                 (Business Logic)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Services   │  │     DTOs     │  │   Mappers    │   │
│  │              │  │              │  │ (MapStruct)  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                          │
│                  (Core Business)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Entities    │  │    Enums     │  │  Value       │   │
│  │    (JPA)     │  │              │  │  Objects     │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│                 INFRASTRUCTURE LAYER                      │
│                   (Driven Adapters)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Repositories │  │   Security   │  │  Multi-      │   │
│  │     (JPA)    │  │     (JWT)    │  │  Tenancy     │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │  Auditoría   │  │  File        │                     │
│  │    (AOP)     │  │  Storage     │                     │
│  └──────────────┘  └──────────────┘                     │
└──────────────────────────────────────────────────────────┘
```

### Flujo de una Request

```
1. Client Request
   ↓
2. Security Filter Chain
   - JwtAuthenticationFilter → Valida token
   - TenantFilter → Extrae tenant_id
   ↓
3. REST Controller
   - Recibe Request
   - Valida @Valid
   ↓
4. Service Layer
   - Lógica de negocio
   - Validaciones
   ↓
5. Repository
   - Query a BD
   - Filtrado automático por tenant_id
   ↓
6. Auditoria (AOP)
   - @AfterReturning → Guarda log
   ↓
7. MapStruct Mapper
   - Entity → DTO
   ↓
8. Response al Client
```

## 🔒 Seguridad

### Flujo de Autenticación JWT

```
┌──────────┐        ┌──────────┐        ┌──────────┐
│          │  POST  │          │  Query │          │
│  Client  │───────→│ Backend  │───────→│   DB     │
│          │ /login │          │  User  │          │
└──────────┘        └──────────┘        └──────────┘
                         │
                         │ Genera JWT
                         ↓
                    ┌─────────────┐
                    │ Access Token│
                    │ - sub: email│
                    │ - tenant_id │
                    │ - roles     │
                    │ - exp: 24h  │
                    └─────────────┘
                         │
                         │ + Refresh Token
                         ↓
┌──────────┐        ┌──────────┐
│          │ ←──────│          │
│  Client  │ Tokens │ Backend  │
│          │        │          │
└──────────┘        └──────────┘
     │
     │ Almacena en localStorage
     │
     │ Request subsecuente
     ↓
┌──────────┐        ┌──────────┐
│          │ Header │          │
│  Client  │───────→│ Backend  │
│          │Bearer  │          │
└──────────┘        └──────────┘
                         │
                    Valida JWT
                         │
                    Extrae Claims
                         │
                    TenantContext
```

### Multi-Tenancy

**Estrategia**: Aislamiento a nivel de aplicación (Shared Database)

```sql
-- Todas las tablas tienen tenant_id
CREATE TABLE terrenos (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    numero VARCHAR(50) NOT NULL,
    ...
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Index para optimizar queries por tenant
CREATE INDEX idx_terrenos_tenant_id ON terrenos(tenant_id);
```

**Flujo de Aislamiento**:

```
Request → JWT → Extrae tenant_id → TenantContext
                                          ↓
                                  Repository Query
                                          ↓
                          WHERE tenant_id = :tenantId
```

## 🗄️ Modelo de Datos

### Diagrama Entidad-Relación Simplificado

```
┌──────────┐          ┌───────────┐         ┌──────────┐
│ Tenants  │←────────○│   Users   │○───────→│  Roles   │
│          │  1:N     │           │  N:M     │          │
│ - id     │          │ - id      │          │ - id     │
│ - nombre │          │ - email   │          │ - nombre │
└──────────┘          │ - tenant  │          └──────────┘
                      └───────────┘
                            │
                            │ 1:N
                            ↓
┌──────────┐          ┌───────────┐         ┌──────────┐
│Proyectos │          │ Terrenos  │         │ Clientes │
│          │○────────→│           │         │          │
│ - id     │  1:N     │ - id      │         │ - id     │
│ - nombre │          │ - numero  │         │ - nombre │
│ - tenant │          │ - area    │         │ - tenant │
└──────────┘          │ - estado  │         └──────────┘
                      │ - tenant  │               │
                      └───────────┘               │
                            │                     │
                            │ 1:N                 │
                            ↓                     ↓
                      ┌───────────┐         ┌──────────┐
                      │  Ventas   │○───────○│PlanPago  │
                      │           │  1:1    │          │
                      │ - id      │         │ - id     │
                      │ - monto   │         │ - tipo   │
                      │ - tenant  │         └──────────┘
                      └───────────┘
                            │
                            │ 1:N
                            ↓
                      ┌───────────┐
                      │   Pagos   │
                      │           │
                      │ - id      │
                      │ - monto   │
                      │ - fecha   │
                      └───────────┘
```

## 🚀 Escalabilidad

### Estrategias Implementadas

#### 1. **Horizontal Scaling**
- **Stateless Backend**: No se guarda estado en servidor
- **JWT**: Token auto-contenido, no requiere session store
- **Load Balancer Ready**: Múltiples instancias del backend

#### 2. **Database Optimization**
- **Indexes**: En tenant_id, fechas, estados
- **Connection Pooling**: HikariCP
- **Lazy Loading**: Relaciones N:M lazy

#### 3. **Caching (Futuro)**
```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  Client  │────→│  Redis   │────→│ Backend  │
│          │     │  Cache   │     │          │
└──────────┘     └──────────┘     └──────────┘
```

## 🔍 Monitoreo y Observabilidad

### Logs Estructurados

```java
@Slf4j
public class TerrenoService {
    public TerrenoResponse create(TerrenoRequest request) {
        log.info("Creating terreno for tenant: {}",
            TenantContext.getTenantId());
        // ...
    }
}
```

### Métricas (Actuator)

```
GET /actuator/health    → Health check
GET /actuator/metrics   → Métricas JVM
GET /actuator/info      → Info de la app
```

### Auditoría

```sql
-- Tabla de auditoría
CREATE TABLE auditoria_critica (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    tipo_operacion VARCHAR(50),
    entidad VARCHAR(100),
    entidad_id BIGINT,
    usuario_id BIGINT,
    detalles JSONB,
    timestamp TIMESTAMP DEFAULT NOW()
);
```

## 🧪 Testing

### Pirámide de Testing

```
         /\
        /  \      Unit Tests (70%)
       /────\     - Services
      /      \    - Mappers
     /────────\   - Utilities
    /          \
   /────────────\ Integration Tests (20%)
  /              \- Controllers + DB
 /────────────────\
/                  \ E2E Tests (10%)
────────────────────- Cypress/Selenium
```

## 📈 Performance

### Optimizaciones Backend

1. **Lazy Loading**: Relaciones cargadas solo cuando se necesitan
2. **Projection Queries**: SELECT solo campos necesarios
3. **Batch Processing**: Operaciones masivas en lotes
4. **Connection Pooling**: HikariCP con 10 conexiones

### Optimizaciones Frontend

1. **Code Splitting**: Lazy loading de rutas
2. **Memoization**: React.memo para componentes
3. **Virtual Scrolling**: Listas grandes con paginación
4. **Image Optimization**: Compresión y lazy loading

## 🔐 Seguridad en Profundidad

### Capas de Seguridad

```
1. HTTPS/TLS          → Encriptación en tránsito
2. CORS               → Control de orígenes
3. JWT Validation     → Autenticación
4. Role-Based Access  → Autorización
5. Multi-Tenancy      → Aislamiento de datos
6. Input Validation   → Prevención de inyección
7. SQL Parameterized  → Prevención SQL injection
8. XSS Protection     → Headers de seguridad
9. CSRF (futuro)      → Tokens anti-CSRF
10. Rate Limiting     → Prevención de abuso
```

## 🔄 CI/CD (Futuro)

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│   Git    │───→│  GitHub  │───→│   Test   │───→│  Deploy  │
│  Commit  │    │ Actions  │    │  Suite   │    │   Prod   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                      │
                      ├─→ Build Backend (Maven)
                      ├─→ Build Frontend (npm)
                      ├─→ Run Tests
                      ├─→ Security Scan
                      ├─→ Docker Build
                      └─→ Deploy to Cloud
```

## 📚 Tecnologías y Patrones

### Backend
- **Clean Architecture** ✅
- **Hexagonal Architecture** ✅
- **Domain-Driven Design** ✅
- **Repository Pattern** ✅
- **Service Layer** ✅
- **DTO Pattern** ✅
- **AOP (Aspect-Oriented)** ✅

### Frontend
- **Component-Based Architecture** ✅
- **Container/Presenter Pattern** ✅
- **Service Layer Pattern** ✅
- **State Management (Zustand)** ✅
- **Custom Hooks** ✅

## 🎯 Mejores Prácticas

### Backend
- ✅ Separation of Concerns
- ✅ Single Responsibility Principle
- ✅ Dependency Injection
- ✅ Immutable DTOs
- ✅ Exception Handling Global
- ✅ Validación en múltiples capas

### Frontend
- ✅ Component Reusability
- ✅ Unidirectional Data Flow
- ✅ Presentational vs Container Components
- ✅ Custom Hooks for Logic Reuse
- ✅ Error Boundaries
- ✅ Responsive Design

---

Esta arquitectura está diseñada para ser **escalable**, **mantenible** y **segura**, siguiendo las mejores prácticas de la industria.
