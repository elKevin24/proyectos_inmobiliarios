# Reporte de Auditoría Integral del Proyecto (`Terrenos SaaS`) — Estado `HEAD` (`62aea59`)

Este documento consolida el análisis exhaustivo de lectura de todo el repositorio tras los commits `5b78b58` (merge de `origin/main`) y `62aea59` (`fix(backend): eliminar metodos duplicados en PagoService/PlanPagoController y habilitar fork javac`).

---

## 1. Lo que ya quedó resuelto en `5b78b58` y `62aea59`

1. **Marcadores de conflicto Git (`<<<<<<< HEAD`) eliminados** en `ApartadoService.java`, `PagoController.java`, `PagoServiceTest.java`, `Layout.jsx`, `package.json` y `ROADMAP.md`.
2. **Métodos duplicados eliminados** en `PagoService.java` y `PlanPagoController.java`.
3. **Dependencias añadidas en `pom.xml`**: `openpdf` (`1.3.30`), `spring-boot-starter-mail` y `spring-boot-starter-thymeleaf`, además de `<fork>true</fork>` en `maven-compiler-plugin`.
4. **Automatizaciones y Fase 8**: `.antigravity/mcp.json`, `.claude/settings.json`, skills, subagentes, ingesta de planos asíncrona con SSE y validación multi-tenant.

---

## 2. Catálogo Exhaustivo de Errores Pendientes en `HEAD` (`62aea59`)

### Categoría A: Pérdida Silenciosa de Datos en Entidades JPA, Mappers y Repositorios (Crítico)

1. **[`Cotizacion.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/domain/entity/Cotizacion.java) omite columnas existentes en Flyway (`V5__create_transactions_tables.sql`)**:
   - La tabla `cotizaciones` en [`V5__create_transactions_tables.sql`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/resources/db/migration/V5__create_transactions_tables.sql#L31-L33) define `cliente_nombre VARCHAR(255) NOT NULL`, `cliente_email VARCHAR(255)` y `cliente_telefono VARCHAR(20)`.
   - Tanto [`CreateCotizacionRequest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/application/dto/cotizacion/CreateCotizacionRequest.java#L29-L36) como [`CotizacionResponse.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/application/dto/cotizacion/CotizacionResponse.java#L31-L33) incluyen `clienteNombre`, `clienteEmail` y `clienteTelefono`.
   - Sin embargo, [`Cotizacion.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/domain/entity/Cotizacion.java) no declara estos 3 campos. Como [`CotizacionMapper.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/interfaces/mapper/CotizacionMapper.java) tiene `unmappedTargetPolicy = ReportingPolicy.IGNORE`, **se pierden silenciosamente al guardar** (o provocan violación `NOT NULL` en PostgreSQL sobre `cliente_nombre`) y `CotizacionResponse.getClienteNombre()` siempre devuelve `null`.
   - Además, [`CotizacionRepository.findByClienteNombre`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/domain/repository/CotizacionRepository.java#L44-L48) hace `JOIN Cliente cl WHERE c.clienteId = cl.id`, por lo que devuelve `0` resultados cuando la cotización se crea sin `clienteId` (como hacen [`CotizacionForm.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/CotizacionForm.jsx) y [`CotizacionIntegrationTest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/interfaces/rest/CotizacionIntegrationTest.java)). Debe buscar directamente en `LOWER(c.clienteNombre)` o con `LEFT JOIN Cliente`.

2. **[`Apartado.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/domain/entity/Apartado.java) omite `clienteNombre`, `clienteEmail`, `clienteTelefono`, `clienteDireccion`**:
   - La tabla `apartados` en [`V5__create_transactions_tables.sql`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/resources/db/migration/V5__create_transactions_tables.sql#L65-L68) define `cliente_nombre`, `cliente_email`, `cliente_telefono`, `cliente_direccion`.
   - [`CreateApartadoRequest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/application/dto/apartado/CreateApartadoRequest.java#L30-L40) y [`ApartadoResponse.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/application/dto/apartado/ApartadoResponse.java#L34-L37) tienen esos campos, pero [`Apartado.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/domain/entity/Apartado.java) los omite, perdiendo los datos del cliente y devolviendo `clienteNombre = null`.
   - [`ApartadoRepository.findByClienteNombre`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/domain/repository/ApartadoRepository.java#L60-L64) también hace inner join obligatorio con `Cliente`.

3. **[`ProyectoMapper.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/interfaces/mapper/ProyectoMapper.java#L33) sobrescribe el estado geográfico con el estado del ciclo de vida**:
   - En las líneas 33 y 61 tiene `@Mapping(target = "estado", source = "estadoProyecto")`, lo cual sobrescribe `Proyecto.estado` (ej. `"Querétaro"`) con `"PLANIFICACION"`.

4. **Campos enriquecidos no poblados en `CotizacionResponse`, `ApartadoResponse` y `VentaResponse`**:
   - `terrenoNumeroLote`, `terrenoManzana`, `proyectoId` y `proyectoNombre` quedan en `null` en `CotizacionService`, `ApartadoService` y `VentaService` porque los mappers ignoran relaciones no cargadas.

---

### Categoría B: Errores de Compilación de Tests, Seguridad, RBAC y Contradicciones entre Suites Backend

1. **Falta helper `crearCliente` en [`BaseE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/BaseE2ETest.java)**:
   - Invocado en [`PagoE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/PagoE2ETest.java#L26), [`PlanPagoE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/PlanPagoE2ETest.java#L30) y [`VentaCompletaE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/VentaCompletaE2ETest.java#L31). Sin este método en `BaseE2ETest`, `mvn test-compile` falla.

2. **Inyección obligatoria de `JavaMailSender` en [`EmailService.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/infrastructure/notification/EmailService.java)**:
   - `application-test.yml` no define `spring.mail.host`, por lo que `JavaMailSender` no se autoconfigura en los `@SpringBootTest` y falla el arranque del contexto si no se marca `@Autowired(required = false)` con guarda de nulidad.

3. **Autoridad `ROLE_ADMIN` vs `ADMIN` en [`CustomUserDetailsService.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/infrastructure/security/CustomUserDetailsService.java#L44-L46)**:
   - `CustomUserDetailsService` registra `"ROLE_" + rol.getNombre()` (`ROLE_ADMIN`), mientras que todos los controladores usan `@PreAuthorize("hasAnyAuthority('...', 'ADMIN')")` o `hasAuthority('ADMIN')` (que busca `"ADMIN"` sin prefijo `ROLE_`). Debe registrar tanto `"ROLE_" + rol.getNombre().name()` como `rol.getNombre().name()`.
   - Además, [`DocumentoController.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/interfaces/rest/DocumentoController.java) y [`PasarelaPagoController.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/interfaces/rest/PasarelaPagoController.java) usan `hasAuthority('...')` sin incluir `'ADMIN'`.

4. **Permisos faltantes en [`V7__insert_initial_data.sql`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/resources/db/migration/V7__insert_initial_data.sql), [`data-test.sql`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/resources/data-test.sql), [`BaseE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/BaseE2ETest.java) y [`BaseIntegrationTest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/infrastructure/BaseIntegrationTest.java)**:
   - En `data-test.sql`, `BaseE2ETest` y `BaseIntegrationTest` faltan `PAGO_VER` y `PAGO_ELIMINAR`.
   - En `V7__insert_initial_data.sql` faltan los permisos de fases, clientes, planes de pago, pagos, archivos y reportes.

5. **Contradicción en el formato de respuesta de `GET /api/v1/proyectos`**:
   - [`ProyectoCRUDE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/ProyectoCRUDE2ETest.java#L68-L72) espera un objeto paginado con `.has("content")`.
   - [`ProyectoIntegrationTest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/interfaces/rest/ProyectoIntegrationTest.java#L76-L86), [`MultiTenancyE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/MultiTenancyE2ETest.java#L114-L118), [`SeguridadE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/SeguridadE2ETest.java#L55-L72) y [`proyectoService.js`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/services/proyectoService.js#L5-L8) esperan un arreglo JSON (`isArray()`) o soporte dual.
   - **Nota clave**: `ProyectoCRUDE2ETest`, `MultiTenancyE2ETest` y `SeguridadE2ETest` llaman exactamente a `GET /api/v1/proyectos` sin parámetros usando `TestRestTemplate`. Si `PagedResponse` o `proyectoService.js` no se armonizan con esos 3 tests, al menos uno fallará. En `MultiTenancyE2ETest` y `SeguridadE2ETest` se puede soportar o actualizar el helper, o en `ProyectoController` retornar `PagedResponse` cuando no hay parámetros si actualizamos `ProyectoIntegrationTest`/`MultiTenancyE2ETest`/`SeguridadE2ETest`, **o** retornar `List` cuando no hay `page`/`size` y ajustar `ProyectoCRUDE2ETest` (o desempaquetar en `proyectoService.js`).

6. **Incompatibilidad entre [`PlanoIngestaServiceTest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/application/service/PlanoIngestaServiceTest.java), [`PlanoIngestaControllerTest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/interfaces/rest/PlanoIngestaControllerTest.java) y [`PlanoIngestaE2ETest.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/test/java/com/inmobiliaria/terrenos/e2e/PlanoIngestaE2ETest.java)**:
   - En `PlanoIngestaService`: proteger `if (planoTaskExecutor != null)` (en test unitario no se inyecta ese mock) y verificar `TenantContext.getTenantId() == null` **antes** de `request.getLotes().isEmpty()`.
   - En `PlanoIngestaController`: delegar la validación de `lotes.isEmpty()` al servicio y usar un método `void verificarAccesoTarea(tareaId, tenantId)` en `PlanoStatusStore` + `statusStore.registrar(tareaId, emitter)` para que el `verify(statusStore).registrar(eq(tareaId), any(SseEmitter.class))` de `PlanoIngestaControllerTest` pase sin causar `403 Forbidden` cuando el mock no está configurado.

7. **5 Clases Enum duplicadas en `domain/enums/`**:
   - Existen archivos legacy `*Enum.java` (`EstadoApartadoEnum.java`, `EstadoProyectoEnum.java`, `EstadoTerrenoEnum.java`, `EstadoVentaEnum.java`, `TipoPrecioEnum.java`) sin uso junto a los enums reales (`EstadoApartado.java`, `EstadoProyecto.java`, etc.).

---

### Categoría C: Desajustes de Contrato Fullstack (Backend DTOs vs Frontend React + Playwright)

1. **Módulo de Apartados ([`ApartadosList.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ApartadosList.jsx), [`ApartadoDetail.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ApartadoDetail.jsx), [`ApartadoForm.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ApartadoForm.jsx))**:
   - **Estados incompatibles**: El frontend compara `apartado.estado === 'VIGENTE'` y `'CONVERTIDO_A_VENTA'`, pero el backend devuelve `'ACTIVO'` y `'COMPLETADO'`. Como resultado, en [`ApartadoDetail.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ApartadoDetail.jsx#L128-L129) `puedeConvertir` y `puedeCancelar` son siempre `false` y los botones **"Convertir a Venta"** y **"Cancelar Apartado"** nunca aparecen.
   - **Campos faltantes al crear**: `ApartadoForm.jsx` envía `{ terrenoId, clienteId, montoApartado, fechaApartado, fechaVencimiento, notas }`, mientras que `CreateApartadoRequest` exige `@NotBlank clienteNombre`, `@NotNull precioTotal` y `@NotNull duracionDias`.
   - **Filtro inválido de clientes**: `ApartadoForm.jsx` llama a `fetchClientes({ estado: 'ACTIVO' })`, lo cual lanza `400 Bad Request` porque `ACTIVO` no existe en `EstadoCliente`.
   - **Propiedades de respuesta**: Frontend lee `apartado.porcentajeApartado` y `apartado.diasVigencia`, pero `ApartadoResponse` expone `porcentajePagado` y `duracionDias`.

2. **Módulo de Ventas ([`VentasList.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/VentasList.jsx), [`VentaDetail.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/VentaDetail.jsx), [`VentaForm.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/VentaForm.jsx))**:
   - **Propiedades de respuesta incompatibles**: `VentasList.jsx` y `VentaDetail.jsx` leen `venta.clienteNombre`, `venta.montoTotal`, `venta.enganche`, `venta.montoFinanciado`, `venta.metodoPago` y `venta.terrenoNumero`, pero [`VentaResponse.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/application/dto/venta/VentaResponse.java) sólo tiene `compradorNombre`, `precioTotal`, `montoApartadoAcreditado`, `montoFinal`, `formaPago` y `terrenoNumeroLote`. Esto provoca que la tabla de ventas muestre `$NaN` en Monto Total y `N/A` en Cliente.
   - **Conversión de Apartado a Venta**: En `VentaForm.jsx` línea 32, `allTerrenos.filter(t => t.estado === 'DISPONIBLE')` excluye el terreno si viene desde un apartado (cuyo estado es `APARTADO`).

3. **Módulo de Proyectos ([`ProyectoForm.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ProyectoForm.jsx), [`ProyectosList.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ProyectosList.jsx))**:
   - El frontend envía `{ nombre, descripcion, ubicacion, fechaInicio, fechaFinEstimada, estado, precioBaseM2, areaTotal }` sin `direccion`, `ciudad`, `codigoPostal` ni `tipoPrecio`, y lee `proyecto.precioBaseM2`, `proyecto.ubicacion`, `proyecto.estado`.

4. **Módulo de Terrenos ([`TerrenoForm.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/TerrenoForm.jsx), [`TerrenosList.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/TerrenosList.jsx), [`TerrenoDetail.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/TerrenoDetail.jsx))**:
   - El frontend envía y lee `numero`, `nombre`, `descripcion`, `ajustePrecio`, `multiplicadorPrecio`, mientras que el backend usa `numeroLote`, `caracteristicas`, `observaciones`, `precioAjuste`, `precioMultiplicador`.
   - `TerrenoService.eliminarTerreno` bloquea la eliminación cuando el terreno está en estado `DISPONIBLE` (esperado por `TerrenoIntegrationTest`), pero la UI y `terrenos.spec.js` esperan poder eliminar un terreno recién creado (`?force=true` desde `terrenoService.js`).

5. **Módulo de Clientes ([`ClienteForm.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ClienteForm.jsx), [`ClientesList.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/ClientesList.jsx))**:
   - El frontend usa `apellidos` (plural) y `estado`, mientras que el backend usa `apellido` (singular) y `estadoCliente`.

6. **Módulo de Dashboard ([`Dashboard.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/pages/Dashboard.jsx) vs [`DashboardResponse.java`](file:///d:/Proyectos/proyectos_inmobiliarios/src/main/java/com/inmobiliaria/terrenos/application/dto/reporte/DashboardResponse.java))**:
   - `Dashboard.jsx` lee `stats.montoComisiones` y `stats.porcentajeAvanceVentas`, mientras que `DashboardResponse` tiene `montoTotalComisiones` y `porcentajeOcupacion`.

---

### Categoría D: Configuración y Selectores de Playwright E2E (`frontend/e2e/`)

1. **[`frontend/playwright.config.js`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/playwright.config.js)**:
   - Configurar `channel: 'chrome'` para usar el navegador Chrome instalado localmente sin requerir descarga de binarios Chromium.
2. **Semilla de datos para E2E (`DataInitializer.java`)**:
   - El test E2E de Playwright inicia sesión con `admin@terrenos.com` / `admin123`, usuario que no existe al levantar el backend en blanco.
3. **[`frontend/src/components/Layout.jsx`](file:///d:/Proyectos/proyectos_inmobiliarios/frontend/src/components/Layout.jsx)**:
   - En el commit `5b78b58` se eliminaron los atributos `data-testid="nav-dashboard"`, `data-testid="nav-apartados"`, `data-testid="nav-cotizaciones"`, `data-testid="nav-planes-pago"` y `data-testid="nav-auditoria"`. Conviene restaurarlos para mantener consistencia en pruebas E2E.
4. **Selectores en `frontend/e2e/*.spec.js`**:
   - Ajustar regexes con acentos opcionales en `auth.spec.js`, `dashboard.spec.js`, `cotizaciones.spec.js` y selector específico `.monto-info .highlight .value` en `ventas.spec.js`.
