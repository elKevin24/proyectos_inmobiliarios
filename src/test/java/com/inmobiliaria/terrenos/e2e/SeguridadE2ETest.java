package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Seguridad: Multi-tenancy y Aislamiento de Datos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeguridadE2ETest extends BaseE2ETest {

    private String tokenA;
    private String tokenB;
    private Long tenantAProyectoId;
    private Long tenantBProyectoId;

    @BeforeEach
    void setUp() throws Exception {
        if (tokenA == null) {
            ResponseEntity<JsonNode> responseA = registerTenant("secA" + System.nanoTime());
            tokenA = getAccessToken(responseA);
            tenantAProyectoId = crearProyecto(tokenA, "Proyecto Tenant A");
        }
        if (tokenB == null) {
            ResponseEntity<JsonNode> responseB = registerTenant("secB" + System.nanoTime());
            tokenB = getAccessToken(responseB);
            tenantBProyectoId = crearProyecto(tokenB, "Proyecto Tenant B");
        }
    }

    @Test
    @Order(1)
    @DisplayName("Tenant A no puede ver proyectos de Tenant B")
    void tenantA_noPuedeVerProyectosDeTenantB() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/proyectos/" + tenantBProyectoId, tokenA);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("Tenant B no puede ver proyectos de Tenant A")
    void tenantB_noPuedeVerProyectosDeTenantA() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/proyectos/" + tenantAProyectoId, tokenB);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("Tenant A solo ve sus propios proyectos en el listado")
    void tenantASoloVeSusProyectos() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos", tokenA);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());

        for (JsonNode proyecto : response.getBody()) {
            assertNotEquals("Proyecto Tenant B",
                    proyecto.get("nombre").asText(),
                    "Tenant A no debe ver proyectos de Tenant B");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Tenant B solo ve sus propios proyectos en el listado")
    void tenantBSoloVeSusProyectos() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos", tokenB);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());

        for (JsonNode proyecto : response.getBody()) {
            assertNotEquals("Proyecto Tenant A",
                    proyecto.get("nombre").asText(),
                    "Tenant B no debe ver proyectos de Tenant A");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Tenant A no puede ver terrenos de Tenant B")
    void tenantA_noPuedeVerTerrenosDeTenantB() throws Exception {
        Long terrenoB = crearTerreno(tokenB, tenantBProyectoId, "SEC-B-001");

        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/terrenos/" + terrenoB, tokenA);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("Tenant A no puede editar proyectos de Tenant B")
    void tenantA_noPuedeEditarProyectosDeTenantB() throws Exception {
        String body = """
                {
                    "nombre": "HACKED por Tenant A",
                    "direccion": "Direccion modificada",
                    "ciudad": "Ciudad modificada",
                    "estado": "Estado modificado",
                    "codigoPostal": "00000"
                }
                """;

        ResponseEntity<JsonNode> response = putJson(
                "/api/v1/proyectos/" + tenantBProyectoId, body, tokenA);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseEntity<JsonNode> verify = getJsonWithAuth(
                "/api/v1/proyectos/" + tenantBProyectoId, tokenB);
        assertEquals(HttpStatus.OK, verify.getStatusCode());
        assertEquals("Proyecto Tenant B",
                verify.getBody().get("nombre").asText(),
                "El proyecto de Tenant B no debe haber sido modificado");
    }

    @Test
    @Order(7)
    @DisplayName("Tenant A no puede eliminar proyectos de Tenant B")
    void tenantA_noPuedeEliminarProyectosDeTenantB() throws Exception {
        ResponseEntity<Void> response = deleteJson(
                "/api/v1/proyectos/" + tenantBProyectoId, tokenA);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseEntity<JsonNode> verify = getJsonWithAuth(
                "/api/v1/proyectos/" + tenantBProyectoId, tokenB);
        assertEquals(HttpStatus.OK, verify.getStatusCode());
    }

    @Test
    @Order(8)
    @DisplayName("Request sin token retorna 401/403")
    void requestSinToken_retornaError() throws Exception {
        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders());
        ResponseEntity<String> response = getClient().exchange(
                baseUrl() + "/api/v1/proyectos",
                HttpMethod.GET, request, String.class);
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @Order(9)
    @DisplayName("Request con token de Tenant A para datos de Tenant B retorna 404")
    void tokenDeTenantAParaDatosDeTenantB_retorna404() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/proyectos/" + tenantBProyectoId + "/plano-interactivo",
                tokenA);
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @Order(10)
    @DisplayName("Tenant A no puede crear terrenos en proyecto de Tenant B")
    void tenantA_noPuedeCrearTerrenosEnProyectoDeTenantB() throws Exception {
        String body = """
                {
                    "proyectoId": %d,
                    "numeroLote": "HACK-001",
                    "manzana": "H",
                    "area": 200.00,
                    "precioBase": 100000.00
                }
                """.formatted(tenantBProyectoId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/terrenos", body, tokenA);
        assertTrue(response.getStatusCode().is4xxClientError(),
                "Tenant A no debe poder crear terrenos en proyecto de Tenant B");
    }
}
