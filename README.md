# 🏢 Sistema de Gestión de Proyectos Inmobiliarios

Sistema SaaS Multi-tenant completo para la gestión de proyectos inmobiliarios, venta de terrenos, clientes y transacciones. Incluye backend robusto con Spring Boot y frontend moderno con React.

## 🌟 Características Principales

### Backend
- ✅ **Multi-tenancy** - Aislamiento completo de datos por empresa
- ✅ **Autenticación JWT** - Seguridad stateless con tokens
- ✅ **Sistema de Permisos** - Control de acceso granular por rol
- ✅ **Auditoría Completa** - Trazabilidad de todas las operaciones críticas
- ✅ **Sistema de Pagos** - Gestión de cuotas y amortizaciones
- ✅ **API RESTful** - Documentada con OpenAPI/Swagger

### Frontend
- ✅ **React 18 + Vite** - Framework moderno y ultra rápido
- ✅ **Mapas Interactivos** - Leaflet para visualización de terrenos
- ✅ **Subida de Planos** - Upload y visualización de planos del proyecto
- ✅ **CRUD Completo** - Proyectos, Terrenos, Clientes, Ventas
- ✅ **Dashboard Interactivo** - Estadísticas en tiempo real
- ✅ **Diseño Responsive** - Funciona en desktop, tablet y móvil

## 🚀 Stack Tecnológico

### Backend
- **Java 21** - Última versión LTS con Virtual Threads
- **Spring Boot 3.4.0** - Framework principal
- **Spring Security 6** - Autenticación JWT
- **Spring Data JPA** - ORM con Hibernate
- **PostgreSQL 14+** - Base de datos
- **Flyway** - Migraciones de BD
- **MapStruct** - Mapeo de DTOs
- **SpringDoc OpenAPI** - Swagger UI

### Frontend
- **React 18** - Biblioteca de UI
- **Vite** - Build tool y dev server
- **React Router DOM** - Routing SPA
- **Axios** - Cliente HTTP
- **Zustand** - State management
- **React Hook Form** - Validación de formularios
- **Leaflet** - Mapas interactivos
- **React Icons** - Iconos

## 📋 Requisitos Previos

- **Java 21** o superior ([descargar](https://adoptium.net/))
- **Node.js 18+** y **npm** ([descargar](https://nodejs.org/))
- **Maven 3.8+** (o usar el wrapper incluido)
- **Docker y Docker Compose** (para PostgreSQL)
- **Git**

## 🏁 Inicio Rápido

### 1. Clonar el repositorio

```bash
git clone https://github.com/elKevin24/proyectos_inmobiliarios.git
cd proyectos_inmobiliarios
```

### 2. Configurar variables de entorno

**Backend:**
```bash
cp .env.example .env
# Editar .env con tus valores
```

**Frontend:**
```bash
cd frontend
cp .env.example .env
# Configurar VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### 3. Levantar PostgreSQL

```bash
docker-compose up -d
```

Esto iniciará:
- **PostgreSQL** en puerto `5432`
- **pgAdmin** en `http://localhost:5050` (usuario: `admin@terrenos.com`, password: `admin`)

### 4. Ejecutar el Backend

```bash
# Con Maven wrapper (recomendado)
./mvnw spring-boot:run

# O compilar y ejecutar
./mvnw clean package
java -jar target/terrenos-backend-1.0.0-SNAPSHOT.jar
```

Backend disponible en: **http://localhost:8080**

### 5. Ejecutar el Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend disponible en: **http://localhost:5173**

## 📚 Documentación de API

Una vez iniciado el backend, accede a:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🗂️ Estructura del Proyecto

```
proyectos_inmobiliarios/
├── frontend/                           # Aplicación React
│   ├── src/
│   │   ├── components/                 # Componentes reutilizables
│   │   │   ├── Layout.jsx
│   │   │   ├── MapEditor.jsx
│   │   │   ├── PlanoViewer.jsx
│   │   │   ├── ImageUploader.jsx
│   │   │   └── ProtectedRoute.jsx
│   │   ├── pages/                      # Páginas de la aplicación
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── TerrenosList.jsx
│   │   │   ├── TerrenoForm.jsx
│   │   │   ├── TerrenoDetail.jsx
│   │   │   ├── ProyectosList.jsx
│   │   │   ├── ProyectoForm.jsx
│   │   │   ├── ProyectoPlano.jsx
│   │   │   ├── ClientesList.jsx
│   │   │   ├── ClienteForm.jsx
│   │   │   └── VentasList.jsx
│   │   ├── services/                   # Servicios de API
│   │   ├── store/                      # Estado global (Zustand)
│   │   ├── styles/                     # Estilos CSS
│   │   └── App.jsx
│   ├── package.json
│   └── vite.config.js
│
├── src/                                # Backend Spring Boot
│   ├── main/
│   │   ├── java/com/inmobiliaria/terrenos/
│   │   │   ├── config/                 # Configuraciones
│   │   │   ├── domain/                 # Entidades JPA
│   │   │   ├── application/            # DTOs y servicios
│   │   │   ├── infrastructure/         # JWT, Multi-tenant, Auditoría
│   │   │   ├── interfaces/             # Controllers REST
│   │   │   └── shared/                 # Excepciones y utilidades
│   │   └── resources/
│   │       ├── db/migration/           # Migraciones Flyway
│   │       └── application.yml
│   └── test/
│
├── docker-compose.yml
├── pom.xml
├── BACKEND_API_DOCUMENTATION.md        # Documentación completa del API
├── BACKEND_FILE_PATHS.md               # Rutas de archivos del backend
└── README.md
```

## 🎯 Funcionalidades Implementadas

### 🏗️ Gestión de Proyectos
- Crear, editar, eliminar proyectos inmobiliarios
- Subir planos/mapas del proyecto
- Visualizar terrenos sobre el plano
- Estadísticas de ocupación en tiempo real
- Filtros y búsqueda

### 🏞️ Gestión de Terrenos
- CRUD completo de terrenos/lotes
- Editor de mapas interactivo
- Dibujo de polígonos para delimitar terrenos
- Cálculo automático de precios
- Estados: Disponible, Apartado, Vendido, Reservado
- Vista detallada con mapa

### 👥 Gestión de Clientes
- CRUD completo de clientes
- Validación de RFC y CURP
- Estados: Prospecto, Interesado, Comprador, Inactivo
- Información fiscal completa
- Notas y seguimiento

### 💰 Gestión de Ventas
- Registro de ventas
- Planes de pago personalizados
- Cálculo de amortizaciones
- Tabla de pagos
- Estados de venta

### 📊 Dashboard
- Estadísticas de terrenos
- Totales por estado
- Accesos rápidos
- Visualización de datos

### 🔐 Seguridad
- Autenticación con JWT
- Control de acceso por roles
- Permisos granulares
- Multi-tenancy
- Auditoría de operaciones críticas

## 🔒 Autenticación

### Registro de Nueva Empresa

```bash
curl -X POST http://localhost:8080/api/v1/tenants/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombreEmpresa": "Mi Inmobiliaria",
    "email": "admin@miinmobiliaria.com",
    "password": "Password123!",
    "nombre": "Juan",
    "apellidos": "Pérez",
    "telefono": "5512345678"
  }'
```

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@miinmobiliaria.com",
    "password": "Password123!"
  }'
```

Respuesta:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 86400000
}
```

### Usar el Token

```bash
curl -X GET http://localhost:8080/api/v1/proyectos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

## 🗄️ Base de Datos

### Esquema Principal

**Tablas de Core:**
- `tenants` - Empresas (multi-tenancy)
- `users` - Usuarios del sistema
- `roles` - Roles de usuario
- `permissions` - Permisos granulares

**Tablas de Negocio:**
- `proyectos` - Proyectos inmobiliarios
- `fases` - Fases de proyectos
- `terrenos` - Lotes/inmuebles
- `clientes` - Clientes y prospectos
- `cotizaciones` - Cotizaciones
- `apartados` - Reservas temporales
- `ventas` - Transacciones de venta
- `planes_pago` - Esquemas de financiamiento
- `pagos` - Registro de pagos
- `amortizaciones` - Tabla de amortización

**Tablas de Sistema:**
- `archivos` - Gestión de archivos
- `auditoria` - Auditoría simple
- `auditoria_critica` - Auditoría de operaciones críticas

### Conexión a PostgreSQL

```bash
# Con psql
psql -h localhost -p 5432 -U postgres -d terrenos_db

# Con pgAdmin
# http://localhost:5050
# Email: admin@terrenos.com
# Password: admin
```

## 🧪 Testing

### Backend

```bash
# Todos los tests
./mvnw test

# Tests de integración
./mvnw verify

# Con cobertura
./mvnw clean verify jacoco:report
```

### Frontend

```bash
cd frontend
npm test
```

## 📦 Build para Producción

### Backend

```bash
./mvnw clean package -DskipTests
# JAR generado en: target/terrenos-backend-1.0.0-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
# Archivos generados en: dist/
```

### Docker

```bash
# Build de imagen backend
docker build -t terrenos-backend:1.0.0 .

# Build de imagen frontend
cd frontend
docker build -t terrenos-frontend:1.0.0 .
```

## 🚀 Despliegue

### Con Docker Compose

```bash
docker-compose up -d
```

Esto levanta:
- PostgreSQL
- Backend (Spring Boot)
- Frontend (React)
- pgAdmin

### Variables de Entorno Producción

**Backend (.env):**
```env
DATABASE_URL=jdbc:postgresql://postgres:5432/terrenos_db
JWT_SECRET=cambiar-en-produccion-usar-valor-seguro-minimo-256-bits
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
SPRING_PROFILES_ACTIVE=prod
```

**Frontend (.env):**
```env
VITE_API_BASE_URL=https://api.tudominio.com/api/v1
```

## 📊 Endpoints Principales

### Autenticación
- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/refresh` - Refrescar token
- `POST /api/v1/tenants/register` - Registrar empresa

### Proyectos
- `GET /api/v1/proyectos` - Listar proyectos
- `POST /api/v1/proyectos` - Crear proyecto
- `GET /api/v1/proyectos/{id}` - Obtener proyecto
- `PUT /api/v1/proyectos/{id}` - Actualizar proyecto
- `DELETE /api/v1/proyectos/{id}` - Eliminar proyecto

### Terrenos
- `GET /api/v1/terrenos` - Listar terrenos
- `POST /api/v1/terrenos` - Crear terreno
- `GET /api/v1/terrenos/{id}` - Obtener terreno
- `PUT /api/v1/terrenos/{id}` - Actualizar terreno
- `DELETE /api/v1/terrenos/{id}` - Eliminar terreno
- `GET /api/v1/terrenos/proyecto/{proyectoId}` - Terrenos por proyecto

### Clientes
- `GET /api/v1/clientes` - Listar clientes
- `POST /api/v1/clientes` - Crear cliente
- `GET /api/v1/clientes/{id}` - Obtener cliente
- `PUT /api/v1/clientes/{id}` - Actualizar cliente
- `DELETE /api/v1/clientes/{id}` - Eliminar cliente

### Ventas
- `GET /api/v1/ventas` - Listar ventas
- `POST /api/v1/ventas` - Crear venta
- `GET /api/v1/ventas/{id}` - Obtener venta
- `POST /api/v1/ventas/{id}/cancelar` - Cancelar venta

### Archivos
- `POST /api/v1/archivos/upload` - Subir archivo
- `GET /api/v1/archivos/{id}/download` - Descargar archivo

Ver documentación completa en: `BACKEND_API_DOCUMENTATION.md`

## 📱 Capturas de Pantalla

### Dashboard
Panel principal con estadísticas de terrenos y accesos rápidos.

### Gestión de Proyectos
Listado, creación y edición de proyectos con subida de planos.

### Mapas Interactivos
Editor de polígonos para delimitar terrenos sobre mapas o planos.

### Vista de Plano
Visualización de plano del proyecto con terrenos superpuestos en colores según su estado.

## 🛣️ Roadmap

### FASE 1 - Backend Completo ✅
- [x] Arquitectura multi-tenant
- [x] Sistema de autenticación JWT
- [x] CRUD de proyectos y terrenos
- [x] Sistema de ventas y pagos
- [x] Auditoría completa

### FASE 2 - MVP Frontend ✅
- [x] React + Vite
- [x] Autenticación
- [x] CRUD de terrenos
- [x] Visualización de mapas
- [x] Transacciones
- [x] CRUD de proyectos
- [x] CRUD de clientes
- [x] Subida de planos
- [x] Visualizador de planos

### FASE 3 - Testing y Auditoría (Siguiente)
- [ ] Tests unitarios frontend
- [ ] Tests E2E con Cypress
- [ ] Tests de integración backend completos
- [ ] Cobertura de código >80%

### FASE 4 - Optimizaciones (Futuro)
- [ ] Caché con Redis
- [ ] Optimización de queries
- [ ] Compresión de imágenes
- [ ] CDN para assets estáticos

### FASE 5 - Funcionalidades Avanzadas (Futuro)
- [ ] Reportes en PDF
- [ ] Exportación a Excel
- [ ] Notificaciones en tiempo real
- [ ] Dashboard de analytics
- [ ] Integración con pasarelas de pago

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'feat: Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo desarrollo privado.

## 👨‍💻 Autor

**Kevin** - Fullstack Developer

## 🔗 Enlaces Útiles

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
- [Leaflet Documentation](https://leafletjs.com/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 📞 Soporte

Para reportar bugs o solicitar nuevas funcionalidades, por favor abre un issue en el repositorio.

---

**¡Gracias por usar el Sistema de Gestión de Proyectos Inmobiliarios!** 🏢✨
