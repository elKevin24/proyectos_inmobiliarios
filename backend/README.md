# 🏗️ Terrenos SaaS Backend

Backend del sistema SaaS Multi-tenant para Gestión Integral de Venta de Terrenos.

## 📋 Tecnologías

- **Java 21** - Eclipse Temurin
- **Spring Boot 3.4.0**
- **PostgreSQL 16**
- **Maven 3.9+**
- **Docker & Docker Compose**

### Dependencias Principales

- Spring Data JPA
- Spring Security 6
- JWT (jjwt 0.12.6)
- Flyway
- Lombok
- MapStruct
- SpringDoc OpenAPI (Swagger)
- Testcontainers

## 🚀 Inicio Rápido

### Opción 1: Docker Compose (Recomendado)

```bash
# 1. Clonar repositorio
cd proyectos_inmobiliarios

# 2. Copiar variables de entorno
cp .env.example .env

# 3. Editar .env con tus configuraciones
nano .env

# 4. Levantar servicios (PostgreSQL + Backend)
docker-compose up -d

# 5. Ver logs
docker-compose logs -f backend

# 6. Detener servicios
docker-compose down
```

El backend estará disponible en: `http://localhost:8080`

### Opción 2: Ejecución Local

**Requisitos:**
- Java 21
- Maven 3.9+
- PostgreSQL 14+

```bash
# 1. Instalar dependencias
cd backend
mvn clean install

# 2. Configurar PostgreSQL
createdb terrenos_db

# 3. Configurar variables de entorno
export DATABASE_URL=jdbc:postgresql://localhost:5432/terrenos_db
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
export JWT_SECRET=your-secret-key-here

# 4. Ejecutar aplicación
mvn spring-boot:run

# O con el JAR
java -jar target/terrenos-backend-1.0.0-SNAPSHOT.jar
```

## 📚 Documentación API

Una vez iniciado el backend, la documentación Swagger está disponible en:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## 🏗️ Arquitectura

```
backend/
├── src/main/java/com/inmobiliaria/terrenos/
│   ├── TerrenosSaasApplication.java    # Clase principal
│   ├── config/                          # Configuraciones
│   ├── domain/                          # Capa de dominio
│   │   ├── entity/                      # Entidades JPA
│   │   ├── repository/                  # Repositorios
│   │   └── enums/                       # Enumeraciones
│   ├── application/                     # Capa de aplicación
│   │   ├── service/                     # Servicios de negocio
│   │   ├── usecase/                     # Casos de uso
│   │   └── dto/                         # Data Transfer Objects
│   ├── infrastructure/                  # Infraestructura
│   │   ├── persistence/                 # Persistencia
│   │   ├── security/                    # Seguridad
│   │   ├── tenant/                      # Multi-tenancy
│   │   └── audit/                       # Auditoría
│   ├── interfaces/                      # Interfaces
│   │   ├── rest/                        # Controllers REST
│   │   └── mapper/                      # Mappers (MapStruct)
│   └── shared/                          # Compartido
│       ├── exception/                   # Excepciones
│       └── util/                        # Utilidades
└── src/main/resources/
    ├── application.yml                  # Configuración
    └── db/migration/                    # Migraciones Flyway
```

## 🔐 Seguridad

- **Multi-tenant**: Aislamiento total por `tenant_id`
- **JWT**: Autenticación basada en tokens
- **BCrypt**: Hash de contraseñas
- **Spring Security 6**: Control de acceso
- **CORS**: Configurado para frontend

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de integración
mvn verify

# Con cobertura
mvn clean test jacoco:report
```

## 🗄️ Base de Datos

### Migraciones con Flyway

Las migraciones se ejecutan automáticamente al iniciar la aplicación.

```sql
-- Ubicación: src/main/resources/db/migration/
V1__create_tenants_table.sql
V2__create_usuarios_table.sql
...
```

### Acceso a pgAdmin

Si iniciaste con `docker-compose --profile tools up`:

- **URL**: `http://localhost:5050`
- **Email**: `admin@terrenos.com`
- **Password**: `admin`

## 🔧 Configuración

### Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `DATABASE_URL` | URL de PostgreSQL | `jdbc:postgresql://localhost:5432/terrenos_db` |
| `DATABASE_USERNAME` | Usuario de BD | `postgres` |
| `DATABASE_PASSWORD` | Contraseña de BD | `postgres` |
| `JWT_SECRET` | Secret para JWT | (cambiar en producción) |
| `JWT_EXPIRATION` | Expiración token (ms) | `86400000` (24h) |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos | `http://localhost:3000` |

### Perfiles Spring

- **dev**: Desarrollo (logs detallados, Swagger habilitado)
- **prod**: Producción (logs mínimos, Swagger deshabilitado)

## 📦 Build para Producción

```bash
# Build con Maven
mvn clean package -DskipTests

# Build imagen Docker
docker build -t terrenos-backend:latest ./backend

# Run imagen
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/db \
  -e JWT_SECRET=your-secret \
  terrenos-backend:latest
```

## 🔍 Monitoreo

### Actuator Endpoints

- **Health**: `http://localhost:8080/actuator/health`
- **Info**: `http://localhost:8080/actuator/info`
- **Metrics**: `http://localhost:8080/actuator/metrics`

## 📝 Próximos Pasos

1. ✅ Configuración base
2. ✅ Entidades y multi-tenancy
3. ⏳ Sistema de autenticación JWT
4. ⏳ Migraciones Flyway
5. ⏳ Controllers REST
6. ⏳ Tests unitarios e integración

## 📄 Licencia

Proyecto privado - Todos los derechos reservados

## 👨‍💻 Autor

Kevin - Developer
