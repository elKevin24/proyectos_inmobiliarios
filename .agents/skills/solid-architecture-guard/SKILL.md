---
name: solid-architecture-guard
description: >-
  Guía y estándares de desarrollo estrictos basados en los 5 principios SOLID y Arquitectura Limpia/Hexagonal
  para todo el código backend y frontend. Utilízalo siempre que se creen o modifiquen entidades, servicios,
  controladores, adaptadores o componentes para asegurar código desacoplado, testeable y mantenible.
---

# 🛡️ Skill: SOLID Architecture Guard

Esta Skill establece las directrices obligatorias de diseño y arquitectura de software para todo desarrollo en el proyecto. Ningún código debe introducir dependencias rígidas ni violar los 5 principios SOLID.

---

## 1. Reglas de Oro por Principio

### S — Single Responsibility Principle (SRP)
* **Un único motivo de cambio:**
  * **Controladores (`interfaces/rest`):** Solo validan entrada HTTP, autorizan y delegan en Casos de Uso/Puertos. No contienen lógica de negocio ni cálculos matemáticos.
  * **Servicios de Aplicación (`application/service`):** Orquestan flujos de casos de uso. Delegan cálculos financieros a `domain/service`, almacenamiento físico a `FileStoragePort` y renderizado a `GeneradorPdfDocumentoPort`.
  * **Entidades (`domain/entity`):** Mantienen la consistencia del estado del modelo. No ejecutan persistencia ni invocan servicios externos.
  * **Evitar "God Services":** Si un servicio supera 300 líneas o tiene más de 5 dependencias inyectadas, debe descomponerse en Casos de Uso segregados o componentes especializados.

### O — Open/Closed Principle (OCP)
* **Abierto a la extensión, cerrado a la modificación:**
  * **Prohibidas las cadenas `if/else` o `switch` sobre tipos de comportamiento:**
    * Para diferentes algoritmos o cálculos (ej. tipos de amortización, cálculo de comisiones, pasarelas de pago), usar el **Patrón Strategy** (`AmortizacionCalculatorStrategy`, `PaymentGatewayPort`).
  * **Despacho polimórfico en excepciones:**
    * Todas las excepciones de negocio heredan de `BusinessException`. El manejo HTTP se delega a `GlobalExceptionHandler` sin crear métodos `@ExceptionHandler` redundantes para cada nueva excepción.
  * **Inyección polimórfica de colecciones:** Inyectar `List<StrategyInterface>` para permitir que nuevos componentes se registren automáticamente vía Spring (`@Component`).

### L — Liskov Substitution Principle (LSP)
* **Sustituibilidad estricta de subtipos:**
  * **Entidades Multi-Tenant:**
    * Toda entidad con aislamiento por empresa DEBE extender `TenantBaseEntity` y llevar la anotación `@SQLRestriction("deleted = false")`.
    * Usar siempre el método `entity.softDelete()` para baja lógica, nunca mutar el flag `deleted` manualmente para garantizar consistencia con `deletedAt`.
  * **Contratos de Excepciones:**
    * Ninguna subclase debe alterar el comportamiento esperado ni retornar `null` en colecciones donde la firma promete una lista. Usar `List.of()` o listas vacías.
  * **Invariantes del Dominio:**
    * No puentear transiciones de estado de un agregado desde otro servicio (ej. respetar el ciclo de vida del terreno `DISPONIBLE` $\rightarrow$ `APARTADO` $\rightarrow$ `VENDIDO`).

### I — Interface Segregation Principle (ISP)
* **Interfaces específicas y cohesivas:**
  * Los controladores y adaptadores deben inyectar interfaces de casos de uso acotadas (`PlanPagoUseCase`, `PagoApplicationUseCase`, `DocumentoPdfPort`), **nunca clases de servicio monolíticas**.
  * Ningún cliente debe verse obligado a depender de métodos que no ejecuta.
  * DTOs separados para comandos y consultas cuando los requerimientos de campos difieran (evitar DTOs "mega-híbridos").

### D — Dependency Inversion Principle (DIP)
* **Depender de abstracciones, no de detalles:**
  * La capa de aplicación (`application`) y dominio (`domain`) **NUNCA** debe depender directamente de:
    * Librerías de terceros (ej. iText/OpenPDF, SDKs directos). Usar puertos en `application/port/out/`.
    * APIs de infraestructura o Web (ej. `HttpServletRequest`, `RequestContextHolder`, `ThreadLocal`). Usar `ExecutionContextPort`.
    * Sistema de archivos local (`java.nio.file.Files`). Usar `FileStoragePort`.
  * Inyección estricta por constructor (`@RequiredArgsConstructor` o constructores explícitos), evitando inyecciones por campo con `@Autowired`.

---

## 2. Checklist Obligatorio para Desarrolladores

Antes de dar por completado un cambio o PR, verificar:

- [ ] **SRP:** ¿La clase modificada tiene una sola responsabilidad clara y justificada?
- [ ] **OCP:** Si se agrega una nueva variante o tipo, ¿se puede hacer creando una nueva clase sin modificar el código previo?
- [ ] **LSP:** ¿Las entidades heredan de `TenantBaseEntity` y usan `softDelete()`? ¿Las excepciones de negocio extienden `BusinessException`?
- [ ] **ISP:** ¿El controlador o consumidor depende de una interfaz de caso de uso específica (`port/in` o `port/out`)?
- [ ] **DIP:** ¿Se evitaron llamadas estáticas a contexto HTTP/ThreadLocal o acceso directo a disco/APIs externas en el servicio?
- [ ] **Verificación:** ¿Compila limpiamente con `.\mvnw.cmd test-compile` y pasan todas las pruebas unitarias?
