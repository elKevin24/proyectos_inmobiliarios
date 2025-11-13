# Terrenos SaaS Backend

Backend del sistema SaaS Multi-tenant para Gestión de Venta de Terrenos.

## 🚀 Stack Tecnológico

- **Java 21** - Última versión LTS con Virtual Threads, Records, Pattern Matching
- **Spring Boot 3.4.0** - Framework principal
- **Spring Security 6** - Autenticación y autorización con JWT
- **Spring Data JPA** - ORM con Hibernate
- **PostgreSQL 14+** - Base de datos principal
- **Flyway** - Migraciones de base de datos
- **MapStruct** - Mapeo de DTOs (compile-time, type-safe)
- **Lombok** - Reducción de boilerplate
- **SpringDoc OpenAPI** - Documentación automática de API (Swagger)
- **Testcontainers** - Tests de integración con PostgreSQL real

## 📋 Requisitos Previos

- **Java 21** o superior ([descargar](https://adoptium.net/))
- **Maven 3.8+** (o usar el wrapper incluido `./mvnw`)
- **Docker y Docker Compose** (para PostgreSQL local)
- **Git**

## 🏁 Inicio Rápido

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/proyectos-inmobiliarios-backend.git
cd proyectos-inmobiliarios-backend
```

### 2. Configurar variables de entorno

```bash
cp .env.example .env
# Editar .env con tus valores (cambiar JWT_SECRET en producción)
```

### 3. Levantar PostgreSQL con Docker

```bash
docker-compose up -d
```

Esto iniciará:
- **PostgreSQL** en puerto `5432`
- **pgAdmin** en `http://localhost:5050` (usuario: `admin@terrenos.com`, password: `admin`)

### 4. Ejecutar migraciones (automático al iniciar)

Las migraciones de Flyway se ejecutan automáticamente al arrancar la aplicación.

### 5. Ejecutar la aplicación

```bash
# Con Maven wrapper (recomendado)
./mvnw spring-boot:run

# O con Maven instalado
mvn spring-boot:run

# O compilar y ejecutar el JAR
./mvnw clean package
java -jar target/terrenos-backend-1.0.0-SNAPSHOT.jar
```

La aplicación estará disponible en: **http://localhost:8080**

## 📚 Documentación de API

Una vez iniciada la aplicación, accede a:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🗃️ Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/inmobiliaria/terrenos/
│   │   │   ├── config/                 # Configuraciones (Security, CORS, etc.)
│   │   │   ├── domain/
│   │   │   │   ├── entity/             # Entidades JPA
│   │   │   │   ├── repository/         # Repositorios Spring Data
│   │   │   │   └── enums/              # Enumeraciones
│   │   │   ├── application/
│   │   │   │   ├── dto/                # DTOs (Request/Response)
│   │   │   │   ├── service/            # Servicios de aplicación
│   │   │   │   └── usecase/            # Casos de uso
│   │   │   ├── infrastructure/
│   │   │   │   ├── security/           # JWT, UserDetails, Filters
│   │   │   │   ├── tenant/             # Multi-tenant (TenantContext)
│   │   │   │   ├── persistence/        # Configuración JPA
│   │   │   │   └── audit/              # Auditoría (AOP)
│   │   │   ├── interfaces/
│   │   │   │   ├── rest/               # Controllers REST
│   │   │   │   └── mapper/             # Mappers (MapStruct)
│   │   │   └── shared/
│   │   │       ├── exception/          # Excepciones personalizadas
│   │   │       └── util/               # Utilidades
│   │   └── resources/
│   │       ├── db/migration/           # Migraciones Flyway
│   │       └── application.yml         # Configuración
│   └── test/                           # Tests
├── pom.xml
├── docker-compose.yml
├── Dockerfile
└── README.md
```

## 🔒 Seguridad

### Autenticación JWT

El sistema usa **JWT stateless** con las siguientes características:

- Tokens firmados con **HMAC-SHA256**
- Access token válido por **24 horas**
- Refresh token válido por **7 días**
- Password hashing con **BCrypt** (12 rounds)

### Endpoints Públicos

- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/refresh` - Refrescar token
- `POST /api/v1/tenants/register` - Registrar nueva empresa
- `/swagger-ui.html` - Documentación
- `/actuator/health` - Health check

Todos los demás endpoints requieren autenticación con JWT.

### Ejemplo de uso

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@example.com","password":"password123"}'

# Respuesta:
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 86400000
}

# 2. Usar el token en requests
curl -X GET http://localhost:8080/api/v1/proyectos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

## 🏢 Multi-tenancy

El sistema implementa **multi-tenancy a nivel de aplicación**:

- Cada empresa (tenant) tiene un `tenant_id` único
- Todas las tablas incluyen `tenant_id`
- El `tenant_id` se extrae del JWT y se establece en `TenantContext`
- Todos los queries filtran automáticamente por `tenant_id`
- Aislamiento total de datos entre tenants

## 🗄️ Base de Datos

### Migraciones

Las migraciones de Flyway se encuentran en `src/main/resources/db/migration/`:

- `V1__create_tenants_table.sql` - Tabla de empresas
- `V2__create_users_roles_permissions_tables.sql` - Usuarios, roles, permisos
- `V3__create_projects_and_phases_tables.sql` - Proyectos y fases
- `V4__create_terrenos_table.sql` - Terrenos/lotes
- `V5__create_transactions_tables.sql` - Cotizaciones, apartados, ventas
- `V6__create_audit_tables.sql` - Auditoría (simple y crítica)
- `V7__insert_initial_data.sql` - Permisos estándar

### Conexión directa a PostgreSQL

```bash
# Con psql
psql -h localhost -p 5432 -U postgres -d terrenos_db

# Con pgAdmin
# http://localhost:5050
# Email: admin@terrenos.com
# Password: admin
```

## 🧪 Testing

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar solo tests unitarios
./mvnw test -Dtest="*Test"

# Ejecutar tests de integración (con Testcontainers)
./mvnw verify

# Con cobertura
./mvnw clean verify jacoco:report
```

## 📦 Build y Deploy

### Build del JAR

```bash
./mvnw clean package -DskipTests
```

El JAR se genera en `target/terrenos-backend-1.0.0-SNAPSHOT.jar`

### Docker

```bash
# Build de imagen Docker
docker build -t terrenos-backend:1.0.0 .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/terrenos_db \
  -e JWT_SECRET=your-secret-key \
  terrenos-backend:1.0.0
```

## 🔧 Configuración Avanzada

### Perfiles de Spring

- **dev** - Desarrollo (logs verbosos, Swagger habilitado)
- **prod** - Producción (logs mínimos, Swagger deshabilitado)

```bash
# Ejecutar con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Variables de Entorno

Ver `.env.example` para todas las variables disponibles.

## 📊 Monitoreo

Spring Boot Actuator expone métricas en `/actuator`:

- `/actuator/health` - Estado de salud
- `/actuator/metrics` - Métricas de la aplicación
- `/actuator/info` - Información del build

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia [PENDIENTE].

## 👨‍💻 Autor

**Kevin** - Developer

## 🔗 Enlaces

- [Documentación de Spring Boot 3.4](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Documentación de Spring Security 6](https://docs.spring.io/spring-security/reference/index.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [MapStruct](https://mapstruct.org/)
