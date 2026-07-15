# Roadmap Pendiente - E2E Tests y Mejoras

## Resumen

**Fecha:** 2026-07-14
**Tarea Principal:** Expandir cobertura E2E de 36 a ~85+ tests
**Progreso Pendiente:** 12 tareas de implementacion

---

## Estado Actual de Tests E2E

| Test | Tests | Estado |
|------|-------|--------|
| AuthE2ETest | 9 | ✅ Completo |
| ProyectoCRUDE2ETest | 7 | ⚠️ Falta error paths |
| TerrenoCRUDE2ETest | 6 | ⚠️ Falta error paths |
| VentaCompletaE2ETest | 10 | ⚠️ Solo flujo con apartado |
| ReporteE2ETest | 4 | ⚠️ Assertions vagos |
| **TOTAL** | **36** | **~43% endpoints, <20% escenarios** |

### Controllers sin tests (0% coverage)

| Controller | Endpoints | Tests |
|-----------|-----------|-------|
| ClienteController | 6 | 0 |
| FaseController | 5 | 0 |
| PlanPagoController | 7 | 0 |
| PagoController | 1 | 0 |
| ArchivoController | 6 | 0 |
| AuditoriaController | 6 | 0 |
| PlanoIngestaController | 3 | 0 (excluido - requiere SSE + CV infra) |

---

## Bugs Encontrados en Revision

| # | Bug | Severidad | Archivo |
|---|-----|-----------|---------|
| 1 | `data-test.sql` tiene `ARCHIVO_SUBIR` pero controller usa `ARCHIVO_CREAR` | Alta | `data-test.sql` |
| 2 | Faltan permissions: `CLIENTE_*`, `PLAN_PAGO_*`, `PAGO_*` | Alta | `data-test.sql` |
| 3 | `BaseE2ETest.setupPermisos()` tiene guard `if (count==0)` que nunca ejecuta el fallback | Media | `BaseE2ETest.java` |

---

## Plan de Implementacion

### Paso 0: Fix Permissions (PREREQUISITO)

**Archivos:** `data-test.sql`, `BaseE2ETest.java`

**Acciones:**
1. Agregar a `data-test.sql`:
   - `CLIENTE_VER`, `CLIENTE_CREAR`, `CLIENTE_EDITAR`, `CLIENTE_ELIMINAR`
   - `PLAN_PAGO_VER`, `PLAN_PAGO_CREAR`, `PLAN_PAGO_EDITAR`, `PLAN_PAGO_ELIMINAR`
   - `PAGO_REGISTRAR`
   - `ARCHIVO_CREAR` (corregir nombre)
2. Agregar los mismos a `BaseE2ETest.setupPermisos()` como fallback
3. Agregar `VENTA_ELIMINAR` a `data-test.sql` (falta)

---

### Paso 1: ClienteCRUDE2ETest (~8 tests)

**Endpoint coverage:** 6/6 (100%)
**Escenarios:** CRUD + 404 + 409 email duplicado + 400 validacion

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 1 | Crear cliente | POST | /api/v1/clientes | 201 |
| 2 | Obtener cliente por ID | GET | /api/v1/clientes/{id} | 200 |
| 3 | Listar clientes | GET | /api/v1/clientes | 200 |
| 4 | Actualizar cliente | PUT | /api/v1/clientes/{id} | 200 |
| 5 | Obtener historial | GET | /api/v1/clientes/{id}/historial | 200 |
| 6 | Email duplicado | POST | /api/v1/clientes | 409 |
| 7 | Cliente inexistente | GET | /api/v1/clientes/99999 | 404 |
| 8 | Eliminar cliente | DELETE | /api/v1/clientes/{id} | 204 |

**Setup:** Requiere tenant registrado. No necesita proyecto.
**Validaciones clave:**
- `email` unique per tenant
- `nombre`, `telefono` required
- `rfc` format validation (12-13 chars)
- Soft delete con `deleted=true`

---

### Paso 2: FaseCRUDE2ETest (~7 tests)

**Endpoint coverage:** 5/5 (100%)
**Escenarios:** CRUD + 404 + 409 nombre duplicado + 409 eliminar con terrenos

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 1 | Crear fase | POST | /api/v1/fases | 201 |
| 2 | Obtener fase por ID | GET | /api/v1/fases/{id} | 200 |
| 3 | Listar fases por proyecto | GET | /api/v1/fases?proyectoId={id} | 200 |
| 4 | Actualizar fase | PUT | /api/v1/fases/{id} | 200 |
| 5 | Nombre duplicado en proyecto | POST | /api/v1/fases | 409 |
| 6 | Fase inexistente | GET | /api/v1/fases/99999 | 404 |
| 7 | Eliminar fase | DELETE | /api/v1/fases/{id} | 204 |

**Setup:** Requiere tenant + proyecto.
**Validaciones clave:**
- `nombre` unico por proyecto
- `numeroFase` unico por proyecto (si se provee)
- No se puede eliminar si tiene terrenos vendidos/apartados

---

### Paso 3: PlanPagoE2ETest (~7 tests)

**Endpoint coverage:** 7/7 (100%)
**Escenarios:** Crear plan, obtener por ID/venta, tabla amortizacion, estado cuenta + errores

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 1 | Crear plan de pago | POST | /api/v1/planes-pago | 201 |
| 2 | Obtener plan por ID | GET | /api/v1/planes-pago/{id} | 200 |
| 3 | Obtener plan por venta | GET | /api/v1/planes-pago/venta/{ventaId} | 200 |
| 4 | Obtener tabla amortizacion | GET | /api/v1/planes-pago/{id}/tabla-amortizacion | 200 |
| 5 | Obtener estado de cuenta | GET | /api/v1/planes-pago/{id}/estado-cuenta | 200 |
| 6 | Plan duplicado para venta | POST | /api/v1/planes-pago | 409 |
| 7 | Listar planes | GET | /api/v1/planes-pago | 200 |

**Setup:** Requiere tenant + proyecto + terreno + apartado + venta (cadena completa).
**Validaciones clave:**
- Solo 1 plan por venta
- `montoFinanciado = montoTotal - enganche`
- `fechaPrimerPago > fechaInicio`
- Si `aplicaInteres=true`, `tasaInteresAnual > 0`
- Auto-genera amortizaciones (frances o iguales)

---

### Paso 4: PagoE2ETest (~4 tests)

**Endpoint coverage:** 1/1 (100%)
**Escenarios:** Registrar pago exitoso + errores

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 1 | Registrar pago (efectivo) | POST | /api/v1/pagos | 201 |
| 2 | Plan inexistente | POST | /api/v1/pagos | 404 |
| 3 | Sin cuotas pendientes | POST | /api/v1/pagos | 400 |
| 4 | Pago con transferencia (requiere referencia) | POST | /api/v1/pagos | 201 |

**Setup:** Requiere tenant + proyecto + terreno + apartado + venta + planPago.
**Validaciones clave:**
- `referenciaPago` requerida para `TRANSFERENCIA` y `CHEQUE`
- Aplica prioridad: mora → interes → capital
- Cambia estado amortizacion: PENDIENTE → PAGADO o PARCIALMENTE_PAGADO

---

### Paso 5: ArchivoE2ETest (~6 tests)

**Endpoint coverage:** 6/6 (100%)
**Escenarios:** Upload, download, list, galeria, versiones, delete

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 1 | Subir archivo | POST | /api/v1/archivos/upload | 201 |
| 2 | Descargar archivo | GET | /api/v1/archivos/{id}/download | 200 |
| 3 | Listar archivos por proyecto | GET | /api/v1/archivos?proyectoId={id} | 200 |
| 4 | Galeria del proyecto | GET | /api/v1/archivos/galeria/{proyectoId} | 200 |
| 5 | Versiones de archivo | GET | /api/v1/archivos/versiones/{proyectoId}?nombreOriginal=... | 200 |
| 6 | Eliminar archivo | DELETE | /api/v1/archivos/{id} | 204 |

**Setup:** Requiere tenant + proyecto. Upload usa `multipart/form-data`.
**Notas:**
- Requiente helper `postMultipartWithAuth()` en `BaseE2ETest` (nuevo)
- Usar archivo pequeno (1KB) de tipo `.pdf` o `.txt`
- Validar extensiones permitidas: pdf, png, jpg, jpeg, dwg, dxf, doc, docx
- Validar tamano maximo 10MB

---

### Paso 6: AuditoriaE2ETest (~4 tests)

**Endpoint coverage:** 6/6 (100%)
**Escenarios:** Logs simples, criticos, archivar, historial

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 1 | Obtener logs simples | GET | /api/v1/auditoria/simple | 200 |
| 2 | Obtener logs criticos | GET | /api/v1/auditoria/critica | 200 |
| 3 | Archivar logs antiguos | POST | /api/v1/auditoria/archivar | 200 |
| 4 | Historial de registro | GET | /api/v1/auditoria/registro/{tabla}/{id} | 200 |

**Setup:** Requiere tenant + operaciones previas que generen logs.
**Notas:**
- Solo accesible con `ADMIN` authority
- `archivarLogsAntiguos()` busca registros > 1 anio (puede retornar 0 en tests)

---

### Paso 7: Mejorar ProyectoCRUDE2ETest (+3 tests)

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 8 | PUT proyecto inexistente | PUT | /api/v1/proyectos/99999 | 404 |
| 9 | POST datos invalidos (nombre corto) | POST | /api/v1/proyectos | 400 |
| 10 | Transicion de estado invalida | PATCH | /api/v1/proyectos/{id}/estado?estado=... | 400 |

---

### Paso 8: Mejorar TerrenoCRUDE2ETest (+3 tests)

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 7 | GET terreno inexistente | GET | /api/v1/terrenos/99999 | 404 |
| 8 | PUT terreno inexistente | PUT | /api/v1/terrenos/99999 | 404 |
| 9 | POST lote duplicado en mismo proyecto | POST | /api/v1/terrenos | 409 |

---

### Paso 9: Mejorar ReporteE2ETest (+2 tests)

| # | Test | Metodo | Endpoint | Assertion |
|---|------|--------|----------|-----------|
| 5 | Dashboard tiene campos esperados | GET | /api/v1/reportes/dashboard | Verificar `totalProyectos`, `totalTerrenos`, `porcentajeOcupacion` |
| 6 | Stats de proyecto especifico | GET | /api/v1/reportes/proyectos/{id} | Verificar `proyectoId`, `nombre`, `totalTerrenos` |

---

### Paso 10: Mejorar VentaCompletaE2ETest (+3 tests)

| # | Test | Metodo | Endpoint | Status Code |
|---|------|--------|----------|-------------|
| 11 | Cancelar apartado | PUT | /api/v1/apartados/{id}/cancelar | 200 |
| 12 | Venta directa (sin apartado) | POST | /api/v1/ventas | 201 |
| 13 | Apartar terreno no disponible | POST | /api/v1/apartados | 409 |

---

### Paso 11: Ejecutar suite completa

- `mvn test` con todos los E2E tests
- Verificar 85+ tests, 0 failures
- `mvn clean package -DskipTests` para verificar compilacion

---

## Estimacion de Tests Finales

| Test Class | Tests Actuales | Tests Nuevos | Total |
|-----------|---------------|-------------|-------|
| AuthE2ETest | 9 | 0 | 9 |
| ProyectoCRUDE2ETest | 7 | +3 | 10 |
| TerrenoCRUDE2ETest | 6 | +3 | 9 |
| VentaCompletaE2ETest | 10 | +3 | 13 |
| ReporteE2ETest | 4 | +2 | 6 |
| ClienteCRUDE2ETest | 0 | +8 | 8 |
| FaseCRUDE2ETest | 0 | +7 | 7 |
| PlanPagoE2ETest | 0 | +7 | 7 |
| PagoE2ETest | 0 | +4 | 4 |
| ArchivoE2ETest | 0 | +6 | 6 |
| AuditoriaE2ETest | 0 | +4 | 4 |
| **TOTAL** | **36** | **+40** | **~83** |

### Coverage Final Estimada

| Metrica | Antes | Despues |
|---------|-------|---------|
| Controllers testeados | 5/14 (36%) | 11/14 (79%) |
| Endpoints cubiertos | ~29/67 (43%) | ~55/67 (82%) |
| Happy path coverage | ~20% | ~80% |
| Error path coverage | ~5% | ~50% |
| Total tests | 36 | ~83 |

### Exclusiones

- **PlanoIngestaController** (3 endpoints): Requiere SSE + CV analysis infra. Testing E2E requiere mock del servicio de analisis de planos o infraestructura real de OpenCV + Tesseract. **Pendiente para fase de testing de integracion con Docker.**
- **Multi-tenancy isolation tests**: Requieren 2 tenants simultaneos en el mismo test. **Pendiente para fase de testing de seguridad.**
- **RBAC tests (403)**: Requieren usuarios con roles restrictivos (no ADMIN). **Pendiente para fase de testing de seguridad.**
- **CORS tests**: Requieren requests cross-origin. **Pendiente para fase de testing de integracion.**

---

## Dependencias

```
Paso 0 (Permissions) ──┬──> Paso 1 (Cliente)
                        ├──> Paso 2 (Fase)
                        ├──> Paso 3 (PlanPago) ──> Paso 4 (Pago)
                        ├──> Paso 5 (Archivo)
                        └──> Paso 6 (Auditoria)
                        
Paso 7-10 (Mejoras) ──> Paso 11 (Suite completa)
```
