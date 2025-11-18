# 🗺️ Hoja de Ruta - Sistema de Gestión Inmobiliaria SaaS

## 📋 Resumen Ejecutivo

**Estado Actual:** Backend Funcional (v1.0.0-beta)
**Progreso General:** ████████░░ 80%

### Módulos Completados: 8/10
- ✅ Autenticación y Autorización
- ✅ Gestión de Proyectos
- ✅ Gestión de Terrenos/Lotes
- ✅ Gestión de Fases
- ✅ Transacciones (Cotizaciones, Apartados, Ventas)
- ✅ Reportes y Dashboard
- ✅ Gestión de Archivos con Versionamiento
- ✅ Plano Interactivo

### Pendientes: 2/10
- ⏳ Gestión de Clientes/Compradores
- ⏳ Gestión de Pagos y Amortizaciones

---

## ✅ Fase 1: Infraestructura y Base (COMPLETADO)

### 1.1 Configuración Inicial ✅
- [x] Proyecto Spring Boot 3.4.0 con Java 21
- [x] PostgreSQL 16 con Flyway
- [x] Arquitectura Hexagonal/Clean
- [x] Multi-tenancy (discriminator-based)
- [x] Docker y Docker Compose
- [x] GitHub Actions CI/CD
- [x] Codespaces configuration

**Commits:**
- `refactor: Reestructurar proyecto eliminando carpeta backend redundante`

---

## ✅ Fase 2: Autenticación (COMPLETADO)

### 2.1 Sistema de Autenticación JWT ✅
- [x] Registro de empresa (tenant) + usuario admin
- [x] Login con JWT (access token + refresh token)
- [x] Refresh token endpoint
- [x] Multi-tenant context (ThreadLocal)
- [x] TenantInterceptor para extraer tenant_id del JWT
- [x] Permisos granulares por módulo
- [x] Roles configurables

### 2.2 Gestión de Usuarios ✅
- [x] CRUD de usuarios
- [x] Asignación de roles
- [x] Desactivación de usuarios
- [x] Hash de contraseñas con BCrypt

**Base de Datos:**
- Migración V1: tenants
- Migración V2: usuarios, roles, permisos, usuarios_roles

**Commits:**
- `feat: Implementar sistema de autenticación JWT completo`

---

## ✅ Fase 3: Gestión de Proyectos (COMPLETADO)

### 3.1 Proyectos Inmobiliarios ✅
- [x] CRUD completo de proyectos
- [x] Estados: PLANIFICACION, EN_VENTA, VENDIDO, FINALIZADO, CANCELADO
- [x] Cambio de estado con validaciones
- [x] Ubicación con coordenadas (lat/lng)
- [x] Tipos de precio: FIJO, VARIABLE
- [x] Contadores automáticos (terrenos disponibles, apartados, vendidos)
- [x] Filtros: activos, disponibles, por estado

**Base de Datos:**
- Migración V3: proyectos

**Commits:**
- `feat: Implementar gestión completa de proyectos inmobiliarios`

---

## ✅ Fase 4: Gestión de Terrenos (COMPLETADO)

### 4.1 Terrenos/Lotes ✅
- [x] CRUD completo de terrenos
- [x] Estados: DISPONIBLE, APARTADO, VENDIDO, RESERVADO
- [x] Cálculo automático de precio: `(base + ajuste) × multiplicador`
- [x] Dimensiones: área, frente, fondo
- [x] Identificación: número de lote, manzana
- [x] Coordenadas para plano interactivo (JSONB)
- [x] Actualización automática de contadores del proyecto
- [x] Búsquedas: por proyecto, fase, estado, rango de precio, rango de área

**Base de Datos:**
- Migración V4: terrenos (incluye coordenadas_plano)

**Commits:**
- `feat: Implementar gestión completa de terrenos/lotes`

---

## ✅ Fase 5: Organización por Fases (COMPLETADO)

### 5.1 Fases de Proyectos ✅
- [x] CRUD de fases
- [x] Numeración secuencial
- [x] Fechas de inicio y fin
- [x] Total de terrenos por fase
- [x] Estado activo/inactivo
- [x] Búsqueda de fases activas con terrenos disponibles

**Base de Datos:**
- Migración V5: fases

**Commits:**
- `feat: Implementar gestión completa de fases de proyectos`

---

## ✅ Fase 6: Transacciones de Venta (COMPLETADO)

### 6.1 Cotizaciones ✅
- [x] Crear cotizaciones para clientes interesados
- [x] Cálculo de descuentos (monto y porcentaje)
- [x] Precio final calculado
- [x] Fecha de vigencia
- [x] Búsqueda por cliente y vigencia
- [x] Eliminación (soft delete)

### 6.2 Apartados ✅
- [x] Crear apartado desde cotización (opcional)
- [x] Monto de apartado (anticipo)
- [x] Duración configurable en días
- [x] Cambio automático de estado del terreno a APARTADO
- [x] Estados: VIGENTE, VENCIDO, CANCELADO, CONVERTIDO
- [x] Cancelación con motivo (libera terreno)
- [x] Búsqueda por vigencia y vencimiento
- [x] Actualización automática de contadores

### 6.3 Ventas ✅
- [x] Crear venta desde apartado (lo marca como CONVERTIDO)
- [x] Venta directa sin apartado
- [x] Cambio automático de estado del terreno a VENDIDO
- [x] Datos del comprador (nombre, RFC, CURP, dirección)
- [x] Cálculo de comisiones
- [x] Formas de pago: CONTADO, CREDITO_BANCARIO, FINANCIAMIENTO_PROPIO
- [x] Estados: PENDIENTE, PAGADO, CANCELADO
- [x] Actualización automática de contadores
- [x] Solo se pueden eliminar ventas CANCELADAS

**Base de Datos:**
- Migración V6: cotizaciones, apartados, ventas

**Commits:**
- `feat: Implementar módulos completos de transacciones (Cotizaciones, Apartados, Ventas)`

---

## ✅ Fase 7: Reportes y Analítica (COMPLETADO)

### 7.1 Dashboard General ✅
- [x] Total de proyectos (activos, finalizados)
- [x] Total de terrenos (disponibles, apartados, vendidos)
- [x] Porcentaje de ocupación
- [x] Total de cotizaciones (vigentes)
- [x] Total de apartados (vigentes, vencidos)
- [x] Total de ventas (pendientes, pagadas)
- [x] Monto total de ventas y comisiones
- [x] Ticket promedio
- [x] Tasa de conversión (cotizaciones → ventas)

### 7.2 Estadísticas por Proyecto ✅
- [x] Endpoint para todos los proyectos con stats
- [x] Endpoint para proyecto específico
- [x] Terrenos por estado
- [x] Porcentajes de ocupación y disponibilidad
- [x] Ventas del proyecto
- [x] Ticket promedio del proyecto

**Commits:**
- `feat: Implementar módulo completo de Reportes y Dashboard`

---

## ✅ Fase 8: Gestión de Archivos (COMPLETADO)

### 8.1 Sistema de Archivos ✅
- [x] Upload multipart (PDF, imágenes, DWG, documentos)
- [x] Tipos: PLANO_PROYECTO, PLANO_TERRENO, IMAGEN_PROYECTO, IMAGEN_TERRENO, DOCUMENTO_PROYECTO, CONTRATO, ESCRITURA
- [x] Versionamiento automático (v1, v2, v3...)
- [x] Solo una versión activa por archivo
- [x] Validación de tamaño (10MB max) y extensiones
- [x] Almacenamiento local con nombres UUID
- [x] Download con seguridad multi-tenant
- [x] Galería de imágenes por proyecto
- [x] Historial de versiones
- [x] Eliminación lógica (soft delete)

**Base de Datos:**
- Migración V8: archivos

**Commits:**
- `feat: Implementar gestión completa de archivos con versionamiento`

---

## ✅ Fase 9: Plano Interactivo (COMPLETADO)

### 9.1 Visualización de Plano ✅
- [x] Endpoint GET /proyectos/{id}/plano-interactivo
- [x] Retorna imagen de plano + terrenos con coordenadas
- [x] Coordenadas como polígonos JSONB
- [x] Color automático según estado:
  - Verde (#4CAF50) = DISPONIBLE
  - Amarillo (#FFC107) = APARTADO
  - Rojo (#F44336) = VENDIDO
  - Azul (#2196F3) = RESERVADO
- [x] Estadísticas del proyecto
- [x] Datos completos de cada terreno
- [x] Integración con sistema de archivos
- [x] Soporte para terrenos sin coordenadas

### 9.2 Gestión de Coordenadas ✅
- [x] Campo coordenadasPlano en CreateTerrenoRequest
- [x] Campo coordenadasPlano en UpdateTerrenoRequest
- [x] Conversión automática JSON ↔ objeto en TerrenoMapper
- [x] Validación de estructura de coordenadas

**Entidades Creadas:**
- Terreno.java (JPA entity con JSONB)
- Proyecto.java (JPA entity)
- Fase.java (JPA entity)

**Commits:**
- `feat: Implementar sistema de plano interactivo con coordenadas de terrenos`

---

## ⏳ Fase 10: Gestión de Clientes (PENDIENTE)

### 10.1 Módulo de Clientes/Compradores 🔜
- [ ] Entidad Cliente con datos completos
- [ ] CRUD de clientes
- [ ] Relación con cotizaciones, apartados, ventas
- [ ] Historial de transacciones por cliente
- [ ] Búsqueda avanzada de clientes
- [ ] Exportación de datos de clientes
- [ ] Notas y seguimiento de clientes

**Prioridad:** Alta
**Tiempo Estimado:** 1-2 días

**Base de Datos:**
- Migración V9: clientes
- Actualizar V6: agregar cliente_id a cotizaciones, apartados, ventas

### 10.2 CRM Básico 🔜
- [ ] Pipeline de ventas
- [ ] Actividades y seguimiento
- [ ] Recordatorios y tareas
- [ ] Comunicación (email/SMS tracking)

**Prioridad:** Media
**Tiempo Estimado:** 2-3 días

---

## ⏳ Fase 11: Gestión de Pagos (PENDIENTE)

### 11.1 Sistema de Pagos y Amortizaciones 🔜
- [ ] Entidad Plan de Pagos
- [ ] Amortizaciones (cuotas)
- [ ] Registro de pagos recibidos
- [ ] Estados: PENDIENTE, PAGADO, VENCIDO, PARCIAL
- [ ] Cálculo de intereses
- [ ] Generación automática de calendario de pagos
- [ ] Recordatorios de pagos próximos
- [ ] Mora y cargos por retraso

**Prioridad:** Alta
**Tiempo Estimado:** 2-3 días

**Base de Datos:**
- Migración V10: planes_pago
- Migración V11: amortizaciones
- Migración V12: pagos

### 11.2 Reportes Financieros 🔜
- [ ] Estado de cuenta por venta
- [ ] Reporte de cobranza
- [ ] Pagos pendientes
- [ ] Proyección de ingresos
- [ ] Análisis de morosidad

**Prioridad:** Media
**Tiempo Estimado:** 1-2 días

---

## 🎯 Fase 12: Mejoras y Optimizaciones (FUTURO)

### 12.1 Performance 🔮
- [ ] Caché con Redis para dashboard
- [ ] Paginación en todos los listados
- [ ] Índices adicionales basados en uso real
- [ ] Query optimization con EXPLAIN ANALYZE
- [ ] Lazy loading de relaciones JPA

### 12.2 Auditoría 🔮
- [ ] Tabla de auditoría centralizada
- [ ] Log de cambios en entidades críticas
- [ ] Quién hizo qué y cuándo
- [ ] Restauración de versiones anteriores

### 12.3 Notificaciones 🔮
- [ ] Email notifications (venta, apartado, vencimientos)
- [ ] SMS notifications
- [ ] Push notifications
- [ ] Plantillas de emails personalizables
- [ ] Queue con RabbitMQ/Kafka

### 12.4 Integraciones 🔮
- [ ] Pasarela de pagos (Stripe, PayPal, Conekta)
- [ ] Firma electrónica (DocuSign, Adobe Sign)
- [ ] WhatsApp Business API
- [ ] Google Maps API para ubicaciones
- [ ] Almacenamiento en cloud (S3, Google Cloud Storage)

### 12.5 Multi-idioma 🔮
- [ ] i18n support (español, inglés)
- [ ] Mensajes de error localizados
- [ ] Documentación en inglés

---

## 🚀 Fase 13: Frontend (PRÓXIMO GRAN PASO)

### 13.1 Tecnologías Sugeridas
- **Framework:** React 18 + TypeScript o Next.js 14
- **UI Library:** Material-UI, Ant Design, o Tailwind CSS + Shadcn/UI
- **State Management:** React Query + Zustand
- **Forms:** React Hook Form + Zod
- **Charts:** Recharts o Chart.js
- **Maps/Plano:** Konva.js, Fabric.js, o SVG nativo

### 13.2 Módulos Frontend 🔮
- [ ] Login y registro
- [ ] Dashboard con gráficas
- [ ] Gestión de proyectos (CRUD)
- [ ] Gestión de terrenos (CRUD)
- [ ] **Plano interactivo con SVG/Canvas**
  - [ ] Renderizar imagen de fondo
  - [ ] Dibujar polígonos sobre terrenos
  - [ ] Colorear según estado
  - [ ] Click para ver detalles
  - [ ] Tooltip on hover
  - [ ] Editor de coordenadas (arrastrar puntos)
- [ ] Proceso de venta (cotización → apartado → venta)
- [ ] Gestión de archivos (upload, gallery, versiones)
- [ ] Reportes y gráficas
- [ ] Gestión de usuarios y permisos

**Tiempo Estimado:** 4-6 semanas

---

## 📊 Métricas de Progreso

### Backend
| Módulo | Estado | Progreso | Archivos | Endpoints |
|--------|--------|----------|----------|-----------|
| Autenticación | ✅ | 100% | 15+ | 3 |
| Proyectos | ✅ | 100% | 8 | 6 |
| Terrenos | ✅ | 100% | 8 | 7 |
| Fases | ✅ | 100% | 7 | 5 |
| Cotizaciones | ✅ | 100% | 5 | 4 |
| Apartados | ✅ | 100% | 5 | 5 |
| Ventas | ✅ | 100% | 6 | 5 |
| Reportes | ✅ | 100% | 4 | 3 |
| Archivos | ✅ | 100% | 9 | 7 |
| Plano Interactivo | ✅ | 100% | 7 | 1 |
| Clientes | ⏳ | 0% | 0 | 0 |
| Pagos | ⏳ | 0% | 0 | 0 |

**Total:** 74+ archivos Java, 41+ endpoints REST

### Base de Datos
| Tabla | Estado | Relaciones | Índices |
|-------|--------|------------|---------|
| tenants | ✅ | - | 1 |
| usuarios | ✅ | tenant, roles | 3 |
| roles | ✅ | permisos | 2 |
| permisos | ✅ | - | 1 |
| proyectos | ✅ | tenant | 4 |
| fases | ✅ | tenant, proyecto | 3 |
| terrenos | ✅ | tenant, proyecto, fase | 6 (+ GIN) |
| cotizaciones | ✅ | tenant, terreno | 4 |
| apartados | ✅ | tenant, terreno, cotizacion | 5 |
| ventas | ✅ | tenant, terreno, apartado | 5 |
| archivos | ✅ | tenant, proyecto, terreno, venta | 7 |
| clientes | ⏳ | - | - |
| planes_pago | ⏳ | - | - |
| amortizaciones | ⏳ | - | - |
| pagos | ⏳ | - | - |

**Total:** 11/15 tablas (73%)

---

## 🎯 Objetivos a Corto Plazo (1-2 semanas)

1. **Gestión de Clientes** ⭐⭐⭐
   - Crear entidad Cliente
   - CRUD completo
   - Relación con transacciones

2. **Sistema de Pagos** ⭐⭐⭐
   - Planes de pago
   - Amortizaciones
   - Registro de pagos

3. **Testing** ⭐⭐
   - Unit tests para servicios críticos
   - Integration tests para endpoints principales
   - Cobertura mínima 60%

4. **Documentación** ⭐
   - Completar Swagger descriptions
   - Diagramas de flujo
   - Guía de deployment

---

## 🎯 Objetivos a Medio Plazo (1-2 meses)

1. **Frontend React** ⭐⭐⭐
   - Setup inicial con TypeScript
   - Integración con API
   - Plano interactivo funcional
   - Dashboard con gráficas

2. **Notificaciones** ⭐⭐
   - Email para eventos importantes
   - SMS para recordatorios

3. **Integraciones** ⭐⭐
   - Pasarela de pagos
   - Firma electrónica

4. **Performance** ⭐
   - Caché
   - Optimización de queries

---

## 🎯 Objetivos a Largo Plazo (3-6 meses)

1. **Mobile App** 🔮
   - React Native o Flutter
   - Features básicos para vendedores en campo

2. **Analytics Avanzado** 🔮
   - ML para predicción de ventas
   - Análisis de tendencias

3. **Marketplace** 🔮
   - Portal público para compradores
   - Búsqueda de terrenos disponibles

---

## 📈 Línea de Tiempo

```
Enero 2025
├─ Semana 3 ✅ Backend Core (Auth, Proyectos, Terrenos, Fases)
└─ Semana 4 ✅ Transacciones, Reportes, Archivos, Plano Interactivo

Febrero 2025
├─ Semana 1 🔜 Clientes y Pagos
├─ Semana 2 🔜 Testing y Documentación
├─ Semana 3 🔮 Frontend Setup
└─ Semana 4 🔮 Frontend Dashboard y Proyectos

Marzo 2025
├─ Semana 1-2 🔮 Frontend Plano Interactivo
├─ Semana 3-4 🔮 Frontend Transacciones

Abril 2025
├─ Semana 1-2 🔮 Notificaciones e Integraciones
└─ Semana 3-4 🔮 Beta Testing y Ajustes
```

---

## 🔑 Siguientes Pasos Inmediatos

### Esta Semana:
1. ✅ ~~Implementar Plano Interactivo~~ (COMPLETADO)
2. 🔜 Crear módulo de Clientes
3. 🔜 Implementar sistema de Pagos

### Próxima Semana:
1. 🔜 Tests unitarios
2. 🔜 Completar documentación Swagger
3. 🔜 Preparar ambiente de staging

---

## 📝 Notas Importantes

- **Multi-tenancy:** Validar siempre tenant_id en todas las operaciones
- **Soft Delete:** Nunca eliminar físicamente, siempre usar deleted=true
- **Auditoría:** Agregar created_by y updated_by en futuras migraciones
- **Seguridad:** Revisar permisos antes de deploy a producción
- **Performance:** Monitorear queries lentas con pg_stat_statements

---

## 🤝 Contribuciones

### Archivos Clave para Nuevos Desarrolladores:
1. `claude.md` - Documentación técnica completa
2. `ROADMAP.md` - Este archivo
3. `api-examples.http` - Ejemplos de uso de todos los endpoints
4. `README.md` - Información general del proyecto
5. `DISEÑO_SISTEMA_SAAS_TERRENOS.md` - Diseño del sistema

### Proceso de Desarrollo:
1. Crear feature branch desde `main`
2. Implementar feature siguiendo arquitectura hexagonal
3. Agregar tests
4. Actualizar `api-examples.http`
5. Crear Pull Request
6. Code review
7. Merge a `main`

---

**Última Actualización:** 2025-01-18
**Versión:** 1.0.0-beta
**Próxima Revisión:** 2025-02-01
