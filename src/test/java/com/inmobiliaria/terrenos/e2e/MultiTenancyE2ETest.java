package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de aislamiento multi-tenancy.
 *
 * Verifica que cada tenant solo accede a sus propios datos.
 * Requiere 2 tenants simultáneos con datos independientes.
 */
@DisplayName("E2E - Aislamiento Multi-Tenancy")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiTenancyE2ETest extends BaseE2ETest {

    // Tenant A
    private String tokenA;
    private Long proyectoIdA;
    private Long terrenoIdA;
    private Long clienteIdA;

    // Tenant B
    private String tokenB;
    private Long proyectoIdB;

    @BeforeEach
    void setUp() throws Exception {
        if (tokenA == null) {
            long ts = System.nanoTime();

            // Registrar Tenant A
            ResponseEntity<JsonNode> respA = registerTenant("tenantA" + ts);
            assertEquals(HttpStatus.CREATED, respA.getStatusCode(),
                    "Tenant A registro fallo: " + respA.getBody());
            tokenA = getAccessToken(respA);

            // Registrar Tenant B
            ResponseEntity<JsonNode> respB = registerTenant("tenantB" + ts);
            assertEquals(HttpStatus.CREATED, respB.getStatusCode(),
                    "Tenant B registro fallo: " + respB.getBody());
            tokenB = getAccessToken(respB);

            // Crear recursos para Tenant A
            proyectoIdA = crearProyecto(tokenA, "Proyecto Tenant A");
            terrenoIdA  = crearTerreno(tokenA, proyectoIdA, "MT-A-001");
            clienteIdA  = crearCliente(tokenA, "cliente_a_" + ts + "@test.com");

            // Crear recursos para Tenant B
            proyectoIdB = crearProyecto(tokenB, "Proyecto Tenant B");
        }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private Long crearCliente(String token, String email) throws Exception {
        String body = """
                {
                    "nombre": "Cliente",
                    "apellido": "Aislamiento",
                    "email": "%s",
                    "telefono": "4421000000"
                }
                """.formatted(email);
        ResponseEntity<JsonNode> resp = postJsonWithAuth("/api/v1/clientes", body, token);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode(),
                "Crear cliente fallo: " + resp.getBody());
        return resp.getBody().get("id").asLong();
    }

    // ─── tests ────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Tenant B no puede ver proyectos de Tenant A")
    void tenantB_noPuedeVerProyectoDeA() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA, tokenB);

        // Debe retornar 404 (no existe para ese tenant) y no 200
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Tenant B obtuvo acceso al proyecto de Tenant A: " + response.getBody());
    }

    @Test
    @Order(2)
    @DisplayName("Tenant B no puede ver terrenos de Tenant A")
    void tenantB_noPuedeVerTerrenoDeA() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/terrenos/" + terrenoIdA, tokenB);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Tenant B obtuvo acceso al terreno de Tenant A: " + response.getBody());
    }

    @Test
    @Order(3)
    @DisplayName("Tenant B no puede ver clientes de Tenant A")
    void tenantB_noPuedeVerClienteDeA() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/clientes/" + clienteIdA, tokenB);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Tenant B obtuvo acceso al cliente de Tenant A: " + response.getBody());
    }

    @Test
    @Order(4)
    @DisplayName("Listado de proyectos de Tenant B no contiene proyectos de Tenant A")
    void tenantB_listarProyectos_noContieneProyectosDeA() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos", tokenB);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode proyectos = response.getBody();
        assertTrue(proyectos.isArray());

        // Ningún proyecto de la lista debe tener el ID del proyecto A
        for (JsonNode proyecto : proyectos) {
            assertNotEquals(proyectoIdA, proyecto.get("id").asLong(),
                    "Tenant B ve el proyecto de Tenant A en el listado");
        }
        // Tenant B debe ver solo su propio proyecto
        assertTrue(proyectos.size() >= 1,
                "Tenant B no ve sus propios proyectos");
    }

    @Test
    @Order(5)
    @DisplayName("Tenant B no puede modificar proyectos de Tenant A")
    void tenantB_noPuedeModificarProyectoDeA() throws Exception {
        String body = """
                {
                    "nombre": "Proyecto Hackeado",
                    "descripcion": "Intento de modificacion cross-tenant",
                    "direccion": "Av. Hack 0",
                    "ciudad": "Ciudad",
                    "estado": "Estado",
                    "codigoPostal": "76000",
                    "tipoPrecio": "FIJO",
                    "precioBase": 999.99
                }
                """;
        ResponseEntity<JsonNode> response = putJson(
                "/api/v1/proyectos/" + proyectoIdA, body, tokenB);

        // Debe retornar 404 (recurso no encontrado para ese tenant)
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Tenant B pudo modificar proyecto de Tenant A: " + response.getBody());
    }

    @Test
    @Order(6)
    @DisplayName("Tenant B no puede eliminar recursos de Tenant A")
    void tenantB_noPuedeEliminarProyectoDeA() throws Exception {
        ResponseEntity<Void> response = deleteJson(
                "/api/v1/proyectos/" + proyectoIdA, tokenB);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "Tenant B pudo eliminar proyecto de Tenant A");

        // Verificar que el proyecto A sigue existiendo para Tenant A
        ResponseEntity<JsonNode> verify = getJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA, tokenA);
        assertEquals(HttpStatus.OK, verify.getStatusCode(),
                "El proyecto de Tenant A fue eliminado por Tenant B");
    }
}
