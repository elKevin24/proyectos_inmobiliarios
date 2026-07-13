# Roadmap - Sistema de Gestion Inmobiliaria SaaS

## Estado Actual (Julio 2026)

# 🗺️ Hoja de Ruta - Sistema de Gestión Inmobiliaria SaaS

## 📋 Resumen Ejecutivo

**Estado Actual:** Backend + Frontend Funcional (v1.1.0-beta)
**Progreso General:** █████████░ 92%
**Última Actualización Real:** 2026-07-13

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
- ⏳ Tests unitarios e integración (cobertura 0%)

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

1. **3 entidades Java faltantes:** `Venta.java`, `Cotizacion.java`, `Apartado.java` no existen como archivos en `domain/entity/` aunque son importadas por repositories y services
2. **Auth desubicado:** `AuthController.java`, `AuthService.java`, `UsuarioDTO.java` estan en `backend/src/main/java/` en vez de `src/main/java/` - Maven no los compila
3. **Dependencia faltante:** `hibernate-types-60` no esta en pom.xml pero el codigo la importa
4. **Typo en Proyecto.java:** Campo `itud` deberia ser `longitud` (Double)

---

## Fase 0 — Estabilizacion (URGENTE)

*El backend no compila. Sin esto, nada mas funciona.*

| # | Tarea | Archivos | Esfuerzo |
|---|-------|----------|----------|
| 0.1 | Crear entidad `Venta.java` en `domain/entity/` | 1 archivo nuevo | 20min |
| 0.2 | Crear entidad `Cotizacion.java` en `domain/entity/` | 1 archivo nuevo | 20min |
| 0.3 | Crear entidad `Apartado.java` en `domain/entity/` | 1 archivo nuevo | 20min |
| 0.4 | Mover `AuthController.java` de `backend/` a `src/` | 1 archivo | 10min |
| 0.5 | Mover `AuthService.java` de `backend/` a `src/` | 1 archivo | 10min |
| 0.6 | Mover `UsuarioDTO.java` de `backend/` a `src/` | 1 archivo | 10min |
| 0.7 | Agregar `hibernate-types-60` al `pom.xml` | 1 archivo | 5min |
| 0.8 | Corregir typo `itud` a `longitud` en `Proyecto.java` | 1 linea | 2min |
| 0.9 | Verificar `mvn clean compile` pase sin errores | - | 15min |
| 0.10 | Verificar `docker-compose up` levante todos los servicios | - | 15min |

**Resultado esperado:** Backend compila y levanta. Frontend se conecta correctamente.

---

## Fase 1 — Calidad y Tests

*Proyecto funcional sin bugs, pero sin ninguna prueba.*

| # | Tarea | Detalle | Esfuerzo |
|---|-------|---------|----------|
| 1.1 | Configurar testing backend | H2 para tests unitarios, JUnit 5, Mockito | 2h |
| 1.2 | Tests unitarios de servicios | ProyectoService, TerrenoService, VentaService, ApartadoService | 1 semana |
| 1.3 | Tests de integracion | Flujos end-to-end: crear proyecto > terreno > cotizacion > venta | 1 semana |
| 1.4 | Configurar testing frontend | Vitest + @testing-library/react | 2h |
| 1.5 | Tests de componentes | ImageUploader, PlanoValidator, PlanoViewer | 3 dias |
| 1.6 | Tests de servicios frontend | Mocks de axios, pruebas de auth flow | 2 dias |
| 1.7 | Cobertura minima | Backend: 60%, Frontend: 40% | continua |

---

## Fase 2 — Frontend: Pulir y Completar

*La estructura existe, pero hay componentes incompletos y UX por mejorar.*

| # | Tarea | Detalle | Esfuerzo |
|---|-------|---------|----------|
| 2.1 | `clienteService.js` | Crear service independiente (actualmente solo en store) | 1h |
| 2.2 | Paginacion backend | Agregar paginacion real a todos los endpoints de listado | 3 dias |
| 2.3 | Paginacion frontend | Agregar controles de paginacion en todas las listas | 2 dias |
| 2.4 | Busqueda avanzada | Filtros por fecha, rango de precios, estado en proyectos/terrenos/ventas | 3 dias |
| 2.5 | Formularios de edicion | Verificar que todos los forms de edicion carguen datos correctamente | 2 dias |
| 2.6 | Dashboard con graficas | Agregar recharts o chart.js para metricas visuales | 3 dias |
| 2.7 | Notificaciones toast | Reemplazar alerts nativos por react-hot-toast | 1 dia |
| 2.8 | Responsive design | Verificar que funcione en mobile/tablet | 3 dias |
| 2.9 | Dark mode | Opcional pero valorado | 2 dias |

---

## Fase 3 — Funcionalidades Nuevas

*Nuevos modulos que agregan valor de negocio.*

| # | Modulo | Detalle | Esfuerzo |
|---|--------|---------|----------|
| 3.1 | Notificaciones por email | Confirmacion de venta, recordatorio de pago, vencimiento de apartado | 1 semana |
| 3.2 | Reportes PDF | Contratos de venta, kardex de cliente, estado de cuenta | 1 semana |
| 3.3 | Exportacion masiva | CSV/Excel de todos los terrenos, ventas, clientes | 3 dias |
| 3.4 | Historial de cambios | Audit log visible en UI (quien cambio que y cuando) | 3 dias |
| 3.5 | Panel de administracion | Gestion de usuarios, roles y permisos desde el frontend | 1 semana |
| 3.6 | Busqueda global | Buscar en todos los modulos desde una barra de busqueda | 3 dias |
| 3.7 | Mapa general | Vista de todos los proyectos en un mapa (Leaflet) con marcadores | 3 dias |
| 3.8 | Apartados vencidos | Cron job que cambia estado de apartados vencidos automaticamente | 1 dia |

---

## Fase 4 — DevOps y Calidad

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

**Tiempo total estimado hasta Fase 4:** ~12 semanas

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

**Ultima Actualizacion:** 2026-07-12
**Version:** 1.1.0-beta
**Autor:** Kevin
