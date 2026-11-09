# 🗺️ Hoja de Ruta y Alcance - Sistema de Gestión Inmobiliaria SaaS

## 📋 Resumen Ejecutivo (Estado Actual)

- **Versión Actual:** `1.6.0-beta` (Última actualización: `2026-11-09`)
- **Progreso del Core MVP + Expansión:** ██████████ 95%
- **Stack Real:** Java 25 + Spring Boot 4.1.0 + PostgreSQL 16 (Flyway V1-V11) + React 19 (Vite 7) + CV Engine (Python FastAPI + OpenCV + Tesseract OCR)

### Módulos Core Implementados (14/14 Backend + 30 Páginas Frontend)
- ✅ **1. Autenticación y Multi-tenancy:** JWT + Refresh Token + aislamiento `tenant_id` (`TenantContext` / `TenantFilter`) + RBAC granular
- ✅ **2. Gestión de Proyectos:** CRUD, estados, contadores automáticos y estadísticas
- ✅ **3. Gestión de Fases:** CRUD por proyecto y métricas de avance
- ✅ **4. Gestión de Terrenos/Lotes:** CRUD, cálculo automático `(base + ajuste) × multiplicador`, polígonos `JSONB` con índices GIN
- ✅ **5. Clientes / CRM:** CRUD, validación RFC/CURP, estados de embudo e historial de conversión
- ✅ **6. Cotizaciones:** Presupuestos con descuentos y control de vigencia
- ✅ **7. Apartados:** Reservas con anticipo, vencimiento y cancelación auditada
- ✅ **8. Ventas:** Conversión desde apartado o directa, cálculo de comisiones y formas de pago
- ✅ **9. Planes de Pago y Amortizaciones:** Generación de tablas de amortización (Francés/Alemán/Lineal) y estados de cuenta
- ✅ **10. Gestión de Pagos y Cobranza:** Registro, aplicación automática a cuotas, consulta por venta y eliminación lógica
- ✅ **11. Reportes y Dashboard:** KPIs ejecutivos, 4 gráficos interactivos (`recharts`), exportación a PDF (`jspdf`) y Excel (`xlsx`)
- ✅ **12. Gestión de Archivos:** Upload multipart, versionamiento automático (`v1, v2...`) y galería
- ✅ **13. Plano Interactivo e Ingesta CV:** Visor Leaflet coloreado por estado + microservicio Python OpenCV/Tesseract con progreso vía **SSE**
- ✅ **14. Auditoría y Cumplimiento:** Logs simples, logs críticos por AOP (valor anterior/nuevo) y archivado anual programado
- ✅ **Calidad y CI/CD:** 174 tests (E2E, integración y unitarios), 25 tests de aislamiento Multi-tenant/RBAC, pipeline GitHub Actions (`ci.yml`)

---

## 🎯 Alcance Pendiente para Cierre de Versión 1.x (Corto Plazo)

Ver detalle técnico en [`ROADMAP_PENDING.md`](./ROADMAP_PENDING.md):

1. **Fase 6 — Testing con Infraestructura Real (1-2 días):**
   - **6.1:** Tests de integración para `PlanoIngestaController` (flujo asíncrono SSE + mock/contenedor `cv-engine`).
   - **6.2:** Migración de suite de integración de H2 a `testcontainers-postgresql` para validar consultas `JSONB` e índices `GIN`.
2. **Fase 9 — Preparación para Producción (5-7 días):**
   - Contenedor `frontend` en `docker-compose.yml`.
   - Observabilidad con Spring Boot Actuator + Micrometer/Prometheus + logs JSON.
   - Caché con Redis para reportes y Rate Limiting por IP/`tenant_id`.

---

## 🚀 Evolución Futura (Backlog Post-MVP / SaaS Comercial)

Funcionalidades fuera del alcance del MVP actual, planificadas para la comercialización pública del SaaS:

### Evolución A — Funcionalidades de Negocio Extendidas
| # | Módulo | Detalle | Esfuerzo |
|---|--------|---------|----------|
| A.1 | Notificaciones por email | Confirmación de venta, recordatorio de pago, vencimiento de apartado (SMTP/SendGrid) | 1 semana |
| A.2 | Panel UI de Usuarios/Roles | Administración visual de empleados, roles y permisos dentro del tenant | 1 semana |
| A.3 | Mapa General Multi-Proyecto | Vista global en Leaflet con marcadores de todos los desarrollos de la inmobiliaria | 3 días |
| A.4 | Cron de Apartados Vencidos | Scheduler que libere automáticamente lotes con apartados expirados | 1 día |

### Evolución B — Escalabilidad Comercial SaaS
| # | Tarea | Detalle | Esfuerzo |
|---|-------|---------|----------|
| B.1 | Onboarding de Tenants | Wizard de primer uso: configurar empresa, logo y moneda | 1 semana |
| B.2 | Planes y Suscripciones | Tiers Free / Pro / Enterprise con límites de proyectos, lotes y usuarios | 2 semanas |
| B.3 | Pasarela de Facturación | Cobro recurrente con Stripe / MercadoPago | 1 semana |
| B.4 | API Pública y Webhooks | Eventos push (`venta.creada`, `pago.recibido`, `apartado.vencido`) | 1 semana |
| B.5 | Internacionalización (i18n) | Soporte multi-idioma (Español / Inglés) en React | 1 semana |

### Evolución C — Inteligencia de Negocio (IA)
| # | Tarea | Detalle | Esfuerzo |
|---|-------|---------|----------|
| C.1 | Predicción y Valuación | Sugerencia de precios por m² según velocidad de absorción y ubicación | 2 semanas |
| C.2 | OCR Especializado | Fine-tuning de detección de planos catastrales complejos | 2 semanas |

---

## Notas Arquitectónicas Clave
- **Multi-tenancy:** Validar siempre `tenant_id` mediante `TenantContext.getCurrentTenant()` en todos los servicios y repositorios.
- **Soft Delete:** Nunca eliminar físicamente; usar siempre `deleted = true` (`@SQLRestriction("deleted = false")`).
- **Inmutabilidad de Migraciones:** No modificar `V1` a `V11`; cualquier cambio de esquema requiere una nueva migración `V12+`.
