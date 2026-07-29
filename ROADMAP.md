# Roadmap - Sistema de Gestion Inmobiliaria SaaS

## Estado Actual (Julio 2026)

# 🗺️ Hoja de Ruta - Sistema de Gestión Inmobiliaria SaaS

## 📋 Resumen Ejecutivo

**Estado Actual:** Backend + Frontend Funcional (v1.1.0-beta)
**Progreso General:** █████████░ 92%
**Última Actualización Real:** 2026-07-29

### Módulos Completados: 12/13
- ✅ Autenticación y Autorización (JWT + multi-tenant)
- ✅ Gestión de Proyectos
- ✅ Gestión de Terrenos/Lotes
- ✅ Gestión de Fases
- ✅ Transacciones (Cotizaciones, Apartados, Ventas)
- ✅ Reportes y Dashboard
- ✅ Gestión de Archivos con Versionamiento
- ✅ Plano Interactivo (visualización)
- ✅ **Gestión de Clientes/CRM Básico** ← completado
- ✅ **Gestión de Pagos y Amortizaciones** ← completado
- ✅ **Frontend React completo** (todas las páginas y rutas)
- ✅ **Módulo CV: Ingesta automática de planos** (OpenCV + Tesseract + SSE) ← nuevo
- ✅ **Tests unitarios e integración** (249/249)
- ⏳ **QA automatizada ampliada**: E2E, regresión, RBAC, performance y CI/CD

---

### Modulos Implementados

- [x] Autenticacion JWT (login, register, refresh, multi-tenant)
- [x] Gestión de Proyectos (CRUD, estados, contadores)
- [x] Gestion de Terrenos/Lotes (CRUD, precio calculado, coordenadas JSONB)
- [x] Gestion de Fases (CRUD por proyecto)
- [x] Cotizaciones (descuentos, vigencia, busqueda por cliente)
- [x] Apartados (anticipo, vencimiento, cancelacion con motivo)
- [x] Ventas (conversion desde apartado, comisiones, formas de pago)
- [x] Clientes (CRUD, historial de transacciones)
- [x] Planes de Pago (amortizaciones, tabla de amortizacion, estado de cuenta)
- [x] Pagos (registro, validacion, estados)
- [x] Reportes y Dashboard (metricas generales y por proyecto)
- [x] Gestion de Archivos (upload, download, versionamiento, galeria)
- [x] Plano Interactivo (imagen + poligonos coloreados por estado)
- [x] Auditoria (logs simples/criticos, archivado automatico)
- [x] CV Engine - OCR de planos (OpenCV + Tesseract, async)
- [x] Frontend React 19 (24 paginas, 12 servicios, 8 stores Zustand)

### Problemas Conocidos (Bloqueantes)
- ✅ **Resueltos:** Todas las entidades (`Venta.java`, `Cotizacion.java`, `Apartado.java`), Auth desubicado y typos en `Proyecto.java` fueron corregidos. 
- ✅ **Compilación y Tests:** Backend compila al 100% y ejecuta **249/249 tests** pasando exitosamente.

---

## 🎯 Asignación de Roles de Desarrollo

### 👨‍💻 Dev A (Lead Backend & Architect)
Responsable de Arquitectura, Seguridad, Integraciones Complejas (PDF, Pagos, CV OCR) e Infraestructura (Redis, Schedulers, Docker, Webhooks).

---

## Fase 0 — Estabilización (COMPLETADA ✅)

*El backend compila sin errores y todas las entidades principales están en `src/main/java/`.*

---

## Fase 1 — Calidad y Tests (COMPLETADA ✅)

*249 tests unitarios y de integración pasando al 100%.*

---

## Fase 1.5 — QA, Automatización y Regresión

*Capa transversal de QA para proteger entregas futuras y reducir regresiones.*

| # | Actividad QA | Alcance | Herramientas / Enfoque | Estado |
|---|--------------|---------|------------------------|--------|
| Q.1 | Estrategia de cobertura | Definir matriz de riesgos por módulo, priorizar rutas críticas y fijar umbrales de cobertura | Unit tests, integration tests, criterios por impacto | ⏳ Pendiente |
| Q.2 | Expansión de tests unitarios | Cubrir servicios, validaciones, mappers, utilidades y casos borde | JUnit, Mockito, parameterized tests | ⏳ Pendiente |
| Q.3 | Tests de integración | Validar repositorios, transacciones, seguridad y consultas con contexto real | Spring Boot Test, Testcontainers, PostgreSQL | ⏳ Pendiente |
| Q.4 | Suite E2E crítica | Automatizar flujos de negocio de extremo a extremo | Playwright/Cypress sobre login, CRUD, ventas, pagos | ⏳ Pendiente |
| Q.5 | Regresión automatizada | Crear smoke/regression pack para PRs y releases | Runs selectivos por etiqueta o carpeta | ⏳ Pendiente |
| Q.6 | Seguridad y RBAC | Probar permisos por rol, acceso cross-tenant y endpoints protegidos | MockMvc + matrices de roles/permisos | ⏳ Pendiente |
| Q.7 | Rendimiento | Medir tiempos de respuesta, queries lentas y capacidad mínima del sistema | k6/JMeter, perfiles de carga | ⏳ Pendiente |
| Q.8 | Quality gates en CI/CD | Bloquear merge si fallan tests, lint, coverage o checks de seguridad | GitHub Actions, reports de cobertura, badges | ⏳ Pendiente |
| Q.9 | Datos y ambientes de prueba | Estandarizar seeds, fixtures y reset entre ejecuciones | Test data builders, seeders, Docker | ⏳ Pendiente |

---

### Prioridades Por Sprint

**Sprint QA 1 - Base de confianza**
- [ ] Definir matriz de riesgos por modulo y flujos criticos
- [ ] Fijar umbrales minimos de cobertura por capa
- [ ] Estandarizar fixtures, builders y seeds
- [ ] Consolidar ejecucion local y en CI de unit tests e integration tests

**Sprint QA 2 - Regresion y seguridad**
- [ ] Cubrir servicios criticos con tests unitarios adicionales
- [ ] Validar RBAC por rol y por tenant
- [ ] Agregar pruebas de acceso denegado y cross-tenant
- [ ] Crear smoke suite para PRs

**Sprint QA 3 - E2E y performance**
- [ ] Automatizar flujos criticos de negocio
- [ ] Ejecutar regresion E2E antes de releases
- [ ] Medir tiempos de respuesta y estabilidad bajo carga
- [ ] Identificar endpoints y queries lentas

**Sprint QA 4 - Quality gates**
- [ ] Bloquear merge si fallan checks obligatorios
- [ ] Publicar reportes de cobertura y ejecucion
- [ ] Integrar estado de tests en PRs
- [ ] Establecer criterio de release quality

---

### Criterios De Salida

- [ ] Cobertura unitaria e integracion visible por modulo y por capa
- [ ] Suite E2E critica estable y reproducible
- [ ] RBAC validado con casos positivos y negativos
- [ ] Performance baseline definido para endpoints clave
- [ ] Pipeline CI/CD con gates obligatorios activos
- [ ] Datos de prueba estandarizados y reseteables

---

### Checklist De Aceptacion Por Modulo

| Modulo | Cobertura Minima | Integracion | E2E | Seguridad | Observaciones |
|--------|------------------|------------|-----|-----------|---------------|
| Auth / JWT | Alta | Si | Si | Si | Incluye refresh, logout y acceso denegado |
| Proyectos | Media | Si | Si | Si | Validar multi-tenant |
| Terrenos / Fases | Media | Si | Si | Si | Incluir validaciones y filtros |
| Cotizaciones / Apartados / Ventas | Alta | Si | Si | Si | Flujos criticos de negocio |
| Pagos / Amortizaciones | Alta | Si | Si | Si | Validar estados y transiciones |
| Reportes / Dashboard | Media | Si | Parcial | No | Priorizar integridad de datos |
| Archivos / CV Engine | Media | Si | Parcial | Si | Dependencia de infraestructura |
| Auditoria | Media | Si | No | Si | Validar trazabilidad y acceso admin |

## Fase 2 — Frontend: Pulir y Completar

*En desarrollo paralelo.*

---

## Fase 3 — Funcionalidades Backend & Integraciones [Dev A]

*Asignado a Dev A (Lead Backend & Architect).*

| # | Tarea Dev A | Módulo / Detalle | Archivos Clave | Estado |
|---|-------------|------------------|----------------|--------|
| A.1 | **Cron Schedulers** | Expiración automática de apartados vencidos (`ApartadoScheduler`) y archivado nocturno de logs (`AuditArchiveScheduler`) | `infrastructure/scheduler/` | 🟡 En Desarrollo |
| A.2 | **Generación de PDFs** | Engine de PDFs para Contrato de Compraventa, Estado de Cuenta y Tabla de Amortización | `application/service/PdfService.java` | ⏳ Pendiente |
| A.3 | **Pasarela de Pagos (MercadoPago/Stripe)** | Integración de checkout y controller receptor de Webhooks para confirmación instantánea de pago | `interfaces/rest/PagoWebhookController.java` | ⏳ Pendiente |
| A.4 | **Notificaciones por Email** | Servicio de correos transaccionales (confirmación venta, vencimiento apartado) | `infrastructure/notification/` | ⏳ Pendiente |
| A.5 | **Caché y Performance (Redis)** | Configuración de Redis para caché de catálogos y terrenos (`@Cacheable`) | `config/RedisConfig.java` | ⏳ Pendiente |
| A.6 | **Rate Limiting & Seguridad** | Limitador de peticiones por tenant/IP y auditoría AOP avanzada | `infrastructure/security/` | ⏳ Pendiente |

---

## Fase 4 — DevOps y Calidad [Dev A]

*Para que el proyecto sea desplegable y mantenible.*

| # | Tarea | Detalle | Esfuerzo |
|---|-------|---------|----------|
| 4.1 | CI/CD pipeline | GitHub Actions: build + test en cada PR, deploy automatico en merge a main | 2 dias |
| 4.2 | Frontend en Docker | Agregar servicio `frontend` al docker-compose | 1 dia |
| 4.3 | Redis | Cache de sesiones, rate limiting, colas de trabajo | 2 dias |
| 4.4 | Logging centralizado | Structured logging en backend, ELK o Loki | 3 dias |
| 4.5 | Monitoreo | Metricas con Micrometer + Prometheus, health checks detallados | 2 dias |
| 4.6 | SSL/HTTPS | Certificados Let's Encrypt via nginx reverse proxy | 1 dia |
| 4.7 | Backup automatico | pg_dump cron + almacenamiento en S3 | 1 dia |
| 4.8 | Documentacion API | Verificar Swagger/OpenAPI este completo y actualizado | 2 dias |

---

## Fase 5 — Escalabilidad y SaaS

*Para convertirlo en un producto multi-tenant real.*

| # | Tarea | Detalle | Esfuerzo |
|---|-------|---------|----------|
| 5.1 | Onboarding de tenants | Wizard de primer uso: crear empresa, subir logo, configurar moneda | 1 semana |
| 5.2 | Planes de suscripcion | Free / Pro / Enterprise con limites de proyectos, terrenos, usuarios | 2 semanas |
| 5.3 | Facturacion | Integracion con pasarela de pago (Stripe/MercadoPago) | 1 semana |
| 5.4 | Personalizacion de marca | Cada tenant sube su logo, colores, dominio personalizado | 3 dias |
| 5.5 | API publica | Endpoints documentados para integraciones externas (CRM, contabilidad) | 1 semana |
| 5.6 | Webhooks | Eventos push: nueva venta, pago recibido, apartado vencido | 3 dias |
| 5.7 | Multi-idioma | i18n en frontend (espanol + ingles minimo) | 1 semana |

---

## Fase 6 — Inteligencia de Negocio

*Valor anadido con datos.*

| # | Tarea | Detalle | Esfuerzo |
|---|-------|---------|----------|
| 6.1 | Prediccion de ventas | Modelo simple basado en historial (tendencia, estacionalidad) | 2 semanas |
| 6.2 | Analisis de precios | Sugerencia automatica de precio basado en zona, area, tendencia | 1 semana |
| 6.3 | Deteccion de fraudes | Alertas por patrones inusuales en pagos o apartados | 1 semana |
| 6.4 | Chatbot integrado | Asistente virtual para preguntas frecuentes del cliente | 2 semanas |
| 6.5 | OCR mejorado | Entrenar modelo personalizado para planos del cliente | 2 semanas |

---

## Linea de Tiempo

```
Fase 0 (Estabilizacion)          1 semana
  |
Fase 1 (Calidad y Tests)         2 semanas
  |
Fase 1.5 (QA y Automatizacion)    2 semanas
  |
Fase 2 (Frontend Pulido)         3 semanas
  |
Fase 3 (Funcionalidades Nuevas)  4 semanas
  |
Fase 4 (DevOps)                  2 semanas
  |
Fase 5 (SaaS)                    Continuo
  |
Fase 6 (IA)                      Futuro
```

**Tiempo total estimado hasta Fase 4:** ~14 semanas

---

## Metricas del Proyecto

### Backend (14 Controllers, ~55 Endpoints)

| Modulo | Estado | Endpoints | Archivos Java |
|--------|--------|-----------|---------------|
| Autenticacion | OK | 3 | 8 |
| Proyectos | OK | 7 | 8 |
| Terrenos | OK | 7 | 8 |
| Fases | OK | 5 | 7 |
| Cotizaciones | OK | 4 | 5 |
| Apartados | OK | 5 | 5 |
| Ventas | OK | 5 | 6 |
| Clientes | OK | 6 | 5 |
| Planes de Pago | OK | 7 | 6 |
| Pagos | OK | 1 | 3 |
| Reportes | OK | 3 | 4 |
| Archivos | OK | 6 | 9 |
| Auditoria | OK | 6 | 8 |
| Plano Ingesta | OK | 3 | 5 |

### Frontend (24 Paginas, 12 Servicios, 8 Stores)

| Pagina | Estado |
|--------|--------|
| Login, Register | OK |
| Dashboard | OK |
| Proyectos (List, Form, Plano) | OK |
| PlanoValidator | OK |
| Terrenos (List, Form, Detail) | OK |
| Clientes (List, Form) | OK |
| Cotizaciones (List, Form, Detail) | OK |
| Apartados (List, Form, Detail) | OK |
| Ventas (List, Form, Detail) | OK |
| Pagos (Form) | OK |
| Planes de Pago (List, Detail) | OK |

### Base de Datos (10 Migraciones, 15+ Tablas)

| Migracion | Tablas |
|-----------|--------|
| V1 | tenants |
| V2 | usuarios, roles, permisos, usuarios_roles |
| V3 | proyectos |
| V4 | terrenos (coordenadas_plano JSONB + GIN index) |
| V5 | cotizaciones, apartados, ventas, descuentos |
| V6 | audit_log_simple, audit_log_critica, audit_log_archive |
| V7 | datos iniciales (INSERT) |
| V8 | archivos |
| V9 | clientes |
| V10 | planes_pago, amortizaciones, pagos |

---

## Decisiones Tecnicas Pendientes

| Decision | Opciones | Recomendacion |
|----------|----------|---------------|
| CSS Framework | Tailwind, Material UI, CSS modules | Mantener CSS modules (ya implementado) |
| Testing backend | JUnit+Mockito, Testcontainers | JUnit+Mockito para unit, Testcontainers para integracion |
| Testing frontend | Vitest, Jest | Vitest (mas rapido con Vite) |
| Email transaccional | SMTP directo, SendGrid, AWS SES | SMTP para MVP, SendGrid para produccion |
| Pagos | Stripe, MercadoPago, Conekta | MercadoPago (enfoque Latam) |
| Cache | Redis, Caffeine | Redis para multi-instancia |
| Monitoring | Prometheus+Grafana, Datadog | Prometheus+Grafana (open source) |

---

## Notas Importantes

- **Multi-tenancy:** Validar siempre tenant_id en todas las operaciones
- **Soft Delete:** Nunca eliminar fisicamente, siempre usar deleted=true
- **JSONB:** Usar GIN indexes para busquedas en coordenadas_plano y caracteristicas
- **CV Engine:** Requiere Tesseract con paquete de idioma español instalado
- **Frontend:** Corre en puerto 5173 (Vite dev), se comunica con backend en 8080
- **Docker:** Backend y CV Engine comparten volume `uploads/` para imagenes

---

**Ultima Actualizacion:** 2026-07-29
**Version:** 1.1.0-beta
**Autor:** Kevin
