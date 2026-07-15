package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Reportes y Dashboard")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReporteE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("repo" + System.nanoTime());
            token = getAccessToken(response);
            proyectoId = crearProyecto(token, "Proyecto Reportes E2E");
            for (int i = 1; i <= 2; i++) {
                crearTerreno(token, proyectoId, "R-" + String.format("%03d", i));
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/reportes/dashboard - Obtener dashboard general")
    void obtenerDashboard_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/reportes/dashboard", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().has("totalProyectos"));
        assertTrue(response.getBody().has("totalTerrenos"));
        assertTrue(response.getBody().has("totalVentas"));
        assertTrue(response.getBody().get("totalProyectos").asInt() >= 1);
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/reportes/proyectos - Estadisticas por proyecto")
    void obtenerEstadisticasPorProyecto_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/reportes/proyectos", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/reportes/proyectos/{id} - Estadisticas de proyecto especifico")
    void obtenerEstadisticasProyecto_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/reportes/proyectos/" + proyectoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().has("proyectoId"));
        assertTrue(response.getBody().has("proyectoNombre"));
        assertTrue(response.getBody().has("totalTerrenos"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/reportes/dashboard sin token retorna error de autenticacion")
    void dashboardSinToken_returnsUnauthorized() throws Exception {
        ResponseEntity<JsonNode> response = getJson("/api/v1/reportes/dashboard");
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/reportes/proyectos/99999 - Proyecto inexistente retorna 404")
    void obtenerEstadisticasProyectoInexistente_returns404() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/reportes/proyectos/99999", token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
