package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Auditoria")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditoriaE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("audit" + System.nanoTime());
            token = getAccessToken(response);
            proyectoId = crearProyecto(token, "Proyecto Auditoria E2E");
        }
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/auditoria/simple - Obtener logs simples")
    void obtenerLogsSimples_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/auditoria/simple", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/auditoria/critica - Obtener logs criticos")
    void obtenerLogsCriticos_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/auditoria/critica", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/auditoria/archivar - Archivar logs antiguos")
    void archivarLogs_returns200() throws Exception {
        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/auditoria/archivar", "{}", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/auditoria/registro/{tabla}/{id} - Historial de registro")
    void obtenerHistorialRegistro_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/auditoria/registro/proyectos/" + proyectoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
    }
}
