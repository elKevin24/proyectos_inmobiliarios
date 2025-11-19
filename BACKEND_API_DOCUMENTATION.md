# RESUMEN COMPLETO DE LA ESTRUCTURA DEL BACKEND

## 1. ARQUITECTURA GENERAL

El proyecto utiliza una **arquitectura de capas** organizada como:
```
src/main/java/com/inmobiliaria/terrenos/
├── config/                  # Configuración (Security, Properties)
├── domain/                  # Capa de dominio (Entidades, Enumerados, Repositorios)
├── application/             # Capa de aplicación (DTOs, Servicios)
├── infrastructure/          # Capa de infraestructura (JWT, Auditoría, Tenant)
├── interfaces/              # Capa de interfaces (Controladores REST)
└── shared/                  # Código compartido (Excepciones)
```

## 2. AUTENTICACIÓN Y SEGURIDAD

### 2.1 Configuración de Seguridad (SecurityConfig.java)
- **Framework**: Spring Security 6 con JWT (Stateless)
- **Endpoints públicos**:
  - `/api/v1/auth/**` - Autenticación
  - `/api/v1/tenants/register` - Registro de tenants
  - `/swagger-ui/**`, `/api-docs/**` - Documentación
  - `/actuator/health` - Health check
  
- **Endpoints protegidos**: Todos los demás requieren autenticación con Bearer Token

### 2.2 JWT (JwtService.java)
- **Algoritmo**: HMAC-SHA con clave secreta
- **Claims**: Incluye tenant_id en el token
- **Tokens**:
  - Access Token: Expira según configuración
  - Refresh Token: Para renovación

### 2.3 CORS Configuration
```
Orígenes permitidos:
- http://localhost:3000
- http://localhost:5173
Métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS
```

## 3. CONTROLADORES REST (12 total)

### 3.1 AuthController (No listado en exploracion pero se conoce)
```
POST /api/v1/auth/login
  Input: { email, password }
  Output: { access_token, refresh_token, token_type, expires_in, user_info }

POST /api/v1/auth/refresh
  Input: { refresh_token }
  Output: { access_token, refresh_token, ... }

POST /api/v1/auth/register
  Input: { email, password, nombre, apellido, tenantNombre }
  Output: { access_token, refresh_token, ... }
```

### 3.2 TerrenoController (/api/v1/terrenos)
```
GET    /api/v1/terrenos                 # Listar con filtros (proyectoId, estado, disponibles)
GET    /api/v1/terrenos/{id}            # Obtener por ID
POST   /api/v1/terrenos                 # Crear nuevo
PUT    /api/v1/terrenos/{id}            # Actualizar
DELETE /api/v1/terrenos/{id}            # Eliminar (soft delete)
PATCH  /api/v1/terrenos/{id}/estado     # Cambiar estado

Permisos:
- VER: TERRENO_VER, ADMIN
- CREAR: TERRENO_CREAR, ADMIN
- EDITAR: TERRENO_EDITAR, ADMIN
- ELIMINAR: TERRENO_ELIMINAR, ADMIN
```

### 3.3 ProyectoController (/api/v1/proyectos)
```
GET    /api/v1/proyectos                      # Listar con filtros (estado, disponibles, activos)
GET    /api/v1/proyectos/{id}                 # Obtener por ID
POST   /api/v1/proyectos                      # Crear
PUT    /api/v1/proyectos/{id}                 # Actualizar
DELETE /api/v1/proyectos/{id}                 # Eliminar (soft delete)
PATCH  /api/v1/proyectos/{id}/estado          # Cambiar estado
GET    /api/v1/proyectos/{id}/plano-interactivo  # Obtener plano con coordenadas

Permisos:
- VER: PROYECTO_VER, ADMIN
- CREAR: PROYECTO_CREAR, ADMIN
- EDITAR: PROYECTO_EDITAR, ADMIN
- ELIMINAR: PROYECTO_ELIMINAR, ADMIN
```

### 3.4 ClienteController (/api/v1/clientes)
```
GET    /api/v1/clientes                   # Listar con filtros (estado, nombre, activos)
GET    /api/v1/clientes/{id}              # Obtener cliente
GET    /api/v1/clientes/{id}/historial    # Historial de transacciones
POST   /api/v1/clientes                   # Crear
PUT    /api/v1/clientes/{id}              # Actualizar
DELETE /api/v1/clientes/{id}              # Eliminar

Permisos:
- VER: CLIENTE_VER, ADMIN
- CREAR: CLIENTE_CREAR, ADMIN
- EDITAR: CLIENTE_EDITAR, ADMIN
- ELIMINAR: CLIENTE_ELIMINAR, ADMIN
```

### 3.5 FaseController (/api/v1/fases)
```
GET    /api/v1/fases                 # Listar con filtros (proyectoId, activas)
GET    /api/v1/fases/{id}            # Obtener
POST   /api/v1/fases                 # Crear
PUT    /api/v1/fases/{id}            # Actualizar
DELETE /api/v1/fases/{id}            # Eliminar

Permisos:
- VER: PROYECTO_VER, ADMIN
- CREAR: PROYECTO_CREAR, ADMIN
- EDITAR: PROYECTO_EDITAR, ADMIN
- ELIMINAR: PROYECTO_ELIMINAR, ADMIN
```

### 3.6 VentaController (/api/v1/ventas)
```
GET    /api/v1/ventas                 # Listar con filtro (estado)
GET    /api/v1/ventas/{id}            # Obtener
POST   /api/v1/ventas                 # Crear venta
PATCH  /api/v1/ventas/{id}/estado     # Cambiar estado
DELETE /api/v1/ventas/{id}            # Eliminar

Permisos:
- VER: VENTA_VER, ADMIN
- CREAR: VENTA_CREAR, ADMIN
- EDITAR: VENTA_EDITAR, ADMIN
- ELIMINAR: VENTA_ELIMINAR, ADMIN
```

### 3.7 ApartadoController (/api/v1/apartados)
```
GET    /api/v1/apartados              # Listar con filtros (vigentes, vencidos)
GET    /api/v1/apartados/{id}         # Obtener
POST   /api/v1/apartados              # Crear
PUT    /api/v1/apartados/{id}/cancelar # Cancelar
DELETE /api/v1/apartados/{id}         # Eliminar

Permisos:
- VER: APARTADO_VER, ADMIN
- CREAR: APARTADO_CREAR, ADMIN
- EDITAR: APARTADO_EDITAR, ADMIN
- ELIMINAR: APARTADO_ELIMINAR, ADMIN
```

### 3.8 PagoController (/api/v1/pagos)
```
POST   /api/v1/pagos  # Registrar pago

Permisos:
- REGISTRAR: PAGO_REGISTRAR, ADMIN
```

### 3.9 PlanPagoController (/api/v1/planes-pago)
```
GET    /api/v1/planes-pago                    # Listar
GET    /api/v1/planes-pago/{id}               # Obtener
GET    /api/v1/planes-pago/{id}/amortizaciones  # Ver tabla de amortización
GET    /api/v1/planes-pago/{id}/estado-cuenta   # Estado de cuenta
POST   /api/v1/planes-pago                    # Crear
PUT    /api/v1/planes-pago/{id}               # Actualizar
DELETE /api/v1/planes-pago/{id}               # Eliminar
```

### 3.10 ReporteController (/api/v1/reportes)
```
GET /api/v1/reportes/dashboard                # Dashboard con estadísticas
GET /api/v1/reportes/proyectos                # Estadísticas por proyecto
GET /api/v1/reportes/proyectos/{id}           # Estadísticas de un proyecto

Permisos:
- REPORTE_VER, ADMIN
```

### 3.11 AuditoriaController (/api/v1/auditoria)
```
GET /api/v1/auditoria/logs                    # Listar logs
GET /api/v1/auditoria/logs/{id}               # Obtener log
```

### 3.12 CotizacionController, ArchivoController
- Gestión de cotizaciones y archivos adjuntos

## 4. ENTIDADES JPA (Domain/entity)

### 4.1 Usuario
```java
- id: Long (PK)
- tenantId: Long (FK) - Multi-tenant
- nombre: String
- apellido: String
- email: String (Unique)
- password: String (BCrypt)
- telefono: String
- activo: Boolean
- ultimoAcceso: LocalDateTime
- roles: Set<Rol> (ManyToMany)
- passwordResetToken, passwordResetExpiry
```

### 4.2 Terreno
```java
- id: Long (PK)
- tenantId: Long
- proyectoId: Long (FK)
- faseId: Long (FK, Opcional)
- numeroLote: String (Unique per proyecto)
- manzana: String
- area: BigDecimal (m²)
- frente: BigDecimal
- fondo: BigDecimal
- precioBase: BigDecimal
- precioAjuste: BigDecimal
- precioMultiplicador: BigDecimal
- precioFinal: BigDecimal (Calculado: (base + ajuste) × multiplicador)
- estado: EstadoTerreno ENUM
- coordenadasPlano: JSON (JSONB)
- poligono: JSON (JSONB)
- caracteristicas: JSON (JSONB)
- observaciones: TEXT
- deleted: Boolean (Soft delete)
- createdAt, updatedAt, createdBy, updatedBy
```

### 4.3 Proyecto
```java
- id: Long (PK)
- tenantId: Long
- nombre: String (200 chars)
- descripcion: TEXT
- direccion, ciudad, estado, codigoPostal
- latitud, longitud: Double
- tipoPrecio: ENUM (FIJO, POR_METRO, RANGO)
- precioBase, precioMaximo: BigDecimal
- totalTerrenos, terrenosDisponibles, terrenosApartados, terrenosVendidos: Integer
- estadoProyecto: ENUM (PLANIFICACION, EN_VENTA, AGOTADO, SUSPENDIDO, CANCELADO)
- observaciones: TEXT
- deleted, createdAt, updatedAt, createdBy, updatedBy
```

### 4.4 Cliente
```java
- id: Long (PK)
- tenantId: Long
- nombre, apellido: String
- email: String
- telefono, telefonoSecundario: String
- direccion, ciudad, estado, codigoPostal, pais: String
- rfc: String (13 chars)
- curp: String (18 chars)
- fechaNacimiento: LocalDate
- origen: ENUM (OrigenCliente)
- estadoCliente: ENUM (PROSPECTO, INTERESADO, COMPRADOR, INACTIVO)
- preferencias: JSON (JSONB)
- notas: TEXT
- deleted, createdAt, updatedAt, createdBy, updatedBy
```

### 4.5 PlanPago
```java
- id: Long (PK)
- tenantId, ventaId, clienteId: Long
- tipoPlan: ENUM (TipoPlanPago)
- frecuenciaPago: ENUM (SEMANAL, QUINCENAL, MENSUAL, BIMESTRAL, TRIMESTRAL, SEMESTRAL, ANUAL)
- montoTotal: BigDecimal
- enganche: BigDecimal
- montoFinanciado: BigDecimal (Calculado)
- tasaInteresAnual, tasaInteresMensual: BigDecimal
- aplicaInteres: Boolean
- numeroPagos: Integer
- plazoMeses: Integer
- tasaMoraMensual: BigDecimal
- diasGracia: Integer
- fechaInicio, fechaPrimerPago, fechaUltimoPago: LocalDate
- notas: TEXT
```

### 4.6 Pago
```java
- id: Long (PK)
- tenantId, planPagoId: Long
- montoPagado: BigDecimal
- fechaPago: LocalDate
- metodoPago: ENUM
- referencia: String
- estado: EstadoAmortizacion ENUM
```

### 4.7 Amortizacion
```java
- id: Long (PK)
- tenantId, planPagoId: Long
- numeroAmortizacion: Integer
- fechaVencimiento: LocalDate
- montoCapital: BigDecimal
- montoInteres: BigDecimal
- montoTotal: BigDecimal
- montoPagado: BigDecimal
- saldoPendiente: BigDecimal
- estado: EstadoAmortizacion ENUM
- fechaPago: LocalDate
```

### 4.8 Venta
```java
- id, terrenoId, proyectoId: Long
- apartadoId: Long (Opcional)
- usuarioId: Long (Vendedor)
- compradorNombre, Email, Telefono, Direccion: String
- compradorRfc, compradorCurp: String
- fechaVenta: LocalDate
- precioTotal: BigDecimal
- montoApartadoAcreditado: BigDecimal
- montoFinal: BigDecimal
- porcentajeComision: BigDecimal
- montoComision: BigDecimal
- formaPago: String
- estado: EstadoVenta ENUM (PENDIENTE, PAGADO, CANCELADO)
```

### 4.9 Apartado
```java
- id, terrenoId, clienteId: Long
- clienteNombre: String
- montoApartado: BigDecimal
- porcentajeApartado: BigDecimal
- fechaApartado: LocalDate
- fechaVencimiento: LocalDate
- diasVigencia: Integer
- estado: EstadoApartado ENUM
- motivo, observaciones: TEXT
```

### 4.10 Fase
```java
- id, proyectoId: Long
- nombre: String
- descripcion: TEXT
- numeroFase: Integer
- fechaInicio, fechaFin: LocalDate
- porcentajeAvance: BigDecimal
```

### 4.11 Rol
```java
- id: Long (PK)
- nombre: String (ADMIN, SUPERVISOR, VENDEDOR, SECRETARIA, CONTADOR)
- descripcion: TEXT
- permisos: Set<Permiso> (ManyToMany)
```

### 4.12 Permiso
```java
- id: Long (PK)
- nombre: String (TERRENO_VER, TERRENO_CREAR, TERRENO_EDITAR, etc.)
- descripcion: TEXT
```

### 4.13 Audit Entities
```
AuditLogCritica    - Logs de cambios en datos críticos
AuditLogSimple     - Logs simples
AuditLogArchive    - Archivo de logs antiguos (archivado automáticamente)
```

## 5. DTOs (Data Transfer Objects)

### 5.1 Auth DTOs
```
LoginRequest:
  - email: String
  - password: String

AuthResponse:
  - access_token: String
  - refresh_token: String
  - token_type: String = "Bearer"
  - expires_in: Long
  - user_info: { id, nombre, email, tenantId, tenantNombre }
```

### 5.2 Terreno DTOs
```
CreateTerrenoRequest:
  - proyectoId: Long (required)
  - faseId: Long (optional)
  - numeroLote: String (required, max 50)
  - manzana: String (optional, max 50)
  - area: BigDecimal (required, > 0)
  - frente, fondo: BigDecimal (optional, > 0)
  - precioBase: BigDecimal (required, > 0)
  - precioAjuste: BigDecimal (optional)
  - precioMultiplicador: BigDecimal (optional)
  - precioFinal: BigDecimal (optional, calculated)
  - caracteristicas: String (max 500)
  - observaciones: String (max 1000)
  - estado: EstadoTerreno (optional)
  - coordenadasPlano: CoordenadasPlano (optional)

TerrenoResponse:
  - id, tenantId, proyectoId, faseId: Long
  - numeroLote, manzana: String
  - area, frente, fondo: BigDecimal
  - precioBase, precioAjuste, precioMultiplicador, precioFinal: BigDecimal
  - estado: EstadoTerreno
  - caracteristicas, observaciones: String
  - createdAt, updatedAt: LocalDateTime
  
  Methods:
  - getPrecioPorMetro(): BigDecimal
  - isDisponible(): Boolean
  - isEnProceso(): Boolean
  - getIdentificadorCompleto(): String

UpdateTerrenoRequest:
  - Similar a CreateTerrenoRequest
```

### 5.3 Proyecto DTOs
```
CreateProyectoRequest:
  - nombre: String (required)
  - descripcion: String (optional)
  - direccion, ciudad, estado, codigoPostal: String
  - latitud, longitud: Double
  - tipoPrecio: TipoPrecio (required)
  - precioBase, precioMaximo: BigDecimal

ProyectoResponse:
  - id, tenantId: Long
  - nombre, descripcion, direccion, ciudad, estado, codigoPostal: String
  - latitud, longitud: Double
  - tipoPrecio: TipoPrecio
  - precioBase, precioMaximo: BigDecimal
  - totalTerrenos, terrenosDisponibles, terrenosApartados, terrenosVendidos: Integer
  - estadoProyecto: EstadoProyecto
  - createdAt, updatedAt: LocalDateTime
  
  Methods:
  - getPorcentajeOcupacion(): Double
  - getPorcentajeDisponibilidad(): Double

UpdateProyectoRequest:
  - Similar a CreateProyectoRequest
```

### 5.4 Cliente DTOs
```
CreateClienteRequest:
  - nombre, apellido: String (required)
  - email: String (optional)
  - telefono: String (required)
  - telefonoSecundario: String (optional)
  - direccion, ciudad, estado, codigoPostal, pais: String
  - rfc, curp: String (optional)
  - fechaNacimiento: LocalDate (optional)
  - origen: OrigenCliente (optional)
  - estadoCliente: EstadoCliente (optional)
  - preferencias: String (JSON, optional)
  - notas: String (optional)

ClienteResponse:
  - id, tenantId: Long
  - nombre, apellido, nombreCompleto, email: String
  - telefono, telefonoSecundario: String
  - direccion, ciudad, estado, codigoPostal, pais: String
  - rfc, curp: String
  - fechaNacimiento: LocalDate
  - origen: OrigenCliente
  - estadoCliente: EstadoCliente
  - preferencias, notas: String
  - totalCotizaciones, totalApartados, totalVentas: Integer
  - createdAt, updatedAt: LocalDateTime

ClienteHistorialResponse:
  - cliente: ClienteResponse
  - cotizaciones: List<CotizacionResponse>
  - apartados: List<ApartadoResponse>
  - ventas: List<VentaResponse>

UpdateClienteRequest:
  - Similar a CreateClienteRequest
```

### 5.5 PlanPago DTOs
```
CreatePlanPagoRequest:
  - ventaId: Long (required)
  - tipoPlan: TipoPlanPago (required)
  - frecuenciaPago: FrecuenciaPago (default MENSUAL)
  - montoTotal: BigDecimal (required, > 0)
  - enganche: BigDecimal (>= 0, <= montoTotal)
  - tasaInteresAnual: BigDecimal (0-100)
  - aplicaInteres: Boolean (required)
  - numeroPagos: Integer (required, 1-600)
  - plazoMeses: Integer (1-600)
  - tasaMoraMensual: BigDecimal (0-20)
  - diasGracia: Integer (0-90)
  - fechaInicio: LocalDate (required, present or future)
  - fechaPrimerPago: LocalDate (required, future, after fechaInicio)
  - notas: String (max 5000)

PlanPagoResponse:
  - id, ventaId, clienteId: Long
  - tipoPlan: TipoPlanPago
  - frecuenciaPago: FrecuenciaPago
  - montoTotal, enganche, montoFinanciado: BigDecimal
  - tasaInteresAnual, tasaInteresMensual: BigDecimal
  - aplicaInteres: Boolean
  - numeroPagos, plazoMeses: Integer
  - tasaMoraMensual: BigDecimal
  - diasGracia: Integer
  - fechaInicio, fechaPrimerPago, fechaUltimoPago: LocalDate
  - totalAmortizaciones, amortizacionesPagadas: Integer
  - amortizacionesPendientes, amortizacionesVencidas: Integer
  - totalPagado, totalPendiente: BigDecimal
  - porcentajeAvance: BigDecimal
  - notas: String
  - createdAt, updatedAt: LocalDateTime

UpdatePlanPagoRequest:
  - Similar a CreatePlanPagoRequest
```

### 5.6 Pago DTOs
```
CreatePagoRequest:
  - planPagoId: Long (required)
  - montoPagado: BigDecimal (required, > 0)
  - fechaPago: LocalDate (required)
  - metodoPago: MetodoPago (required)
  - referencia: String (optional)

PagoResponse:
  - id, planPagoId: Long
  - montoPagado: BigDecimal
  - fechaPago: LocalDate
  - metodoPago: MetodoPago
  - referencia: String
  - estado: EstadoAmortizacion
  - createdAt: LocalDateTime
```

### 5.7 Venta DTOs
```
CreateVentaRequest:
  - terrenoId: Long (required)
  - apartadoId: Long (optional)
  - compradorNombre: String (required)
  - compradorEmail: String (optional)
  - compradorTelefono: String (required)
  - compradorDireccion: String (optional)
  - compradorRfc, compradorCurp: String (optional)
  - fechaVenta: LocalDate (optional, defaults to today)
  - precioTotal: BigDecimal (optional, defaults to terreno price)
  - montoApartadoAcreditado: BigDecimal (optional)
  - montoFinal: BigDecimal (optional)
  - porcentajeComision: BigDecimal (optional)
  - formaPago: String (optional)
  - observaciones: String (optional)

VentaResponse:
  - id, terrenoId, proyectoId, apartadoId, usuarioId: Long
  - terrenoNumeroLote, terrenoManzana: String
  - proyectoNombre: String
  - usuarioNombre: String
  - compradorNombre, Email, Telefono, Direccion, Rfc, Curp: String
  - fechaVenta: LocalDate
  - precioTotal, montoApartadoAcreditado, montoFinal: BigDecimal
  - porcentajeComision, montoComision: BigDecimal
  - formaPago: String
  - estado: EstadoVenta
  - observaciones: String
  - createdAt, updatedAt: LocalDateTime
  
  Methods:
  - getMontoNeto(): BigDecimal
  - getPorcentajePagadoConApartado(): BigDecimal
  - isPagada(): Boolean
  - getTerrenoIdentificador(): String
```

### 5.8 Apartado DTOs
```
CreateApartadoRequest:
  - terrenoId: Long (required)
  - clienteId: Long (required)
  - clienteNombre: String (optional)
  - montoApartado: BigDecimal (optional)
  - porcentajeApartado: BigDecimal (optional, 1-100)
  - diasVigencia: Integer (optional, 1-365)
  - observaciones: String (optional)

ApartadoResponse:
  - id, terrenoId, clienteId: Long
  - clienteNombre: String
  - montoApartado: BigDecimal
  - porcentajeApartado: BigDecimal
  - fechaApartado, fechaVencimiento: LocalDate
  - diasVigencia: Integer
  - diasFaltantes: Integer
  - estado: EstadoApartado
  - observaciones: String
  - createdAt, updatedAt: LocalDateTime
  
  Methods:
  - isVigente(): Boolean
  - isVencido(): Boolean
```

### 5.9 Reporte DTOs
```
DashboardResponse:
  - totalProyectos, proyectosActivos: Integer
  - totalTerrenos, terrenosDisponibles, terrenosApartados, terrenosVendidos: Integer
  - totalVentas, ventasPagadas: Integer
  - totalClientes, clientesActivos: Integer
  - totalApartados, apartadosVigentes, apartadosVencidos: Integer
  - montoTotalVentas, montoComisiones: BigDecimal
  - porcentajeAvanceVentas: BigDecimal

ProyectoEstadisticasResponse:
  - proyectoId, proyectoNombre: Long, String
  - totalTerrenos, terrenosDisponibles, terrenosApartados, terrenosVendidos: Integer
  - precioPromedio, precioMinimo, precioMaximo: BigDecimal
  - montoTotalVentas: BigDecimal
  - porcentajeOcupacion: BigDecimal
```

## 6. ENUMERADOS

### 6.1 Estados
```
EstadoTerreno:
  - DISPONIBLE
  - APARTADO
  - VENDIDO
  - RESERVADO

EstadoProyecto:
  - PLANIFICACION
  - EN_VENTA
  - AGOTADO
  - SUSPENDIDO
  - CANCELADO

EstadoCliente:
  - PROSPECTO
  - INTERESADO
  - COMPRADOR
  - INACTIVO

EstadoVenta:
  - PENDIENTE
  - PAGADO
  - CANCELADO

EstadoApartado:
  - VIGENTE
  - VENCIDO
  - CONVERTIDO_A_VENTA
  - CANCELADO

EstadoAmortizacion:
  - PENDIENTE
  - PAGADA
  - VENCIDA
  - CONDONADA

EstadoPago:
  - PENDIENTE
  - PROCESANDO
  - COMPLETADO
  - FALLIDO
```

### 6.2 Configuraciones
```
TipoPrecio:
  - FIJO
  - POR_METRO
  - RANGO

TipoPlanPago:
  - CONTADO
  - CREDITO
  - APARTADO_CON_FINANCIAMIENTO

FrecuenciaPago:
  - SEMANAL (7 días)
  - QUINCENAL (15 días)
  - MENSUAL (30 días)
  - BIMESTRAL (60 días)
  - TRIMESTRAL (90 días)
  - SEMESTRAL (180 días)
  - ANUAL (365 días)

MetodoPago:
  - EFECTIVO
  - TRANSFERENCIA
  - CHEQUE
  - TARJETA_CREDITO
  - TARJETA_DEBITO
  - OTRO

OrigenCliente:
  - DIRECTO
  - REFERENCIA
  - PUBLICIDAD
  - TELEFONO
  - EMAIL
  - REDES_SOCIALES
```

### 6.3 Roles
```
RolEnum:
  - ADMIN (Control total)
  - SUPERVISOR (Gestión de proyectos y fases)
  - VENDEDOR (Creación y seguimiento de transacciones)
  - SECRETARIA (Lectura y exportación)
  - CONTADOR (Acceso financiero y reportes)
```

### 6.4 Auditoría
```
TipoAccionAudit:
  - CREATE, UPDATE, DELETE, EXPORT, DOWNLOAD

TipoOperacionAudit:
  - CRITICA, NORMAL, LECTURA

TipoArchivo:
  - CONTRATO, DOCUMENTO, PLANO, COMPROBANTE, OTRO
```

## 7. PERMISOS DEL SISTEMA

Basados en roles con formato `RECURSO_ACCION`:

```
TERRENO_VER, TERRENO_CREAR, TERRENO_EDITAR, TERRENO_ELIMINAR
PROYECTO_VER, PROYECTO_CREAR, PROYECTO_EDITAR, PROYECTO_ELIMINAR
CLIENTE_VER, CLIENTE_CREAR, CLIENTE_EDITAR, CLIENTE_ELIMINAR
VENTA_VER, VENTA_CREAR, VENTA_EDITAR, VENTA_ELIMINAR
APARTADO_VER, APARTADO_CREAR, APARTADO_EDITAR, APARTADO_ELIMINAR
PAGO_REGISTRAR
REPORTE_VER
ADMIN (Todos los permisos)
```

## 8. ESTRUCTURA DE BASE DE DATOS (Inferido)

### Tablas principales:
```
usuarios
roles
usuario_rol (join table)
permisos
rol_permiso (join table)
proyectos
terrenos
fases
clientes
apartados
ventas
planes_pago
pagos
amortizaciones
cotizaciones
archivos
audit_log_simple
audit_log_critica
audit_log_archive
```

## 9. CARACTERÍSTICAS PRINCIPALES

### 9.1 Multi-tenancy
- Cada recurso incluye `tenantId`
- Filtrado automático por tenant en servicios
- TenantFilter middleware

### 9.2 Soft Delete
- Campo `deleted` en entidades
- Recuperación de datos eliminados posible
- Automático en `@Where` annotations

### 9.3 Auditoría
- Registro automático de cambios críticos
- Logs con usuario, fecha, acción
- Archivado automático de logs antiguos

### 9.4 Seguridad
- Contraseñas hasheadas con BCrypt (strength 12)
- JWT stateless authentication
- CORS configurado para localhost
- PreAuthorize con permisos granulares

### 9.5 Validación
- Validación en DTOs con Jakarta Validation
- Reglas de negocio en servicios
- Manejo de excepciones centralizado

### 9.6 Cálculos Dinámicos
- Precio final de terreno: (base + ajuste) × multiplicador
- Tasa mensual calculada: tasa anual / 12
- Monto financiado: total - enganche
- Porcentajes ocupación y disponibilidad en proyectos

## 10. PUNTOS CLAVE PARA EL FRONTEND

1. **Autenticación**: Enviar `Authorization: Bearer {access_token}` en headers
2. **Tenant**: Se obtiene del token JWT (claim `tenant_id`)
3. **Paginación**: No completamente implementada en listados (considerar agregar)
4. **Filtros**: Usados como query parameters
5. **Errores**: GlobalExceptionHandler devuelve ErrorResponse estructurado
6. **Estados**: Usar enumerados para validar valores permitidos
7. **Permisos**: Verificar role del usuario para habilitar/deshabilitar UI
8. **Precios**: Siempre usar BigDecimal para operaciones monetarias
9. **Fechas**: LocalDate para fechas sin hora, LocalDateTime para auditoría
10. **Coordinadas**: Objetos JSON en campos JSONB para renderización de planos

