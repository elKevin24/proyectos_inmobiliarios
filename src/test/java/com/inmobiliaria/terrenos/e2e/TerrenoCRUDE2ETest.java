package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - CRUD Terrenos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TerrenoCRUDE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;
    private Long terrenoId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("terr" + System.nanoTime());
            token = getAccessToken(response);
            proyectoId = crearProyecto(token, "Proyecto para Terrenos");
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/terrenos - Crear terreno")
    void crearTerreno_returns201() throws Exception {
        String body = """
                {
                    "proyectoId": %d,
                    "numeroLote": "A-001",
                    "manzana": "A",
                    "area": 200.50,
                    "frente": 10.0,
                    "fondo": 20.05,
                    "precioBase": 500000.00
                }
                """.formatted(proyectoId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/terrenos", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        terrenoId = response.getBody().get("id").asLong();
        assertEquals("A-001", response.getBody().get("numeroLote").asText());
        assertEquals("DISPONIBLE", response.getBody().get("estado").asText());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/terrenos/{id} - Obtener terreno por ID")
    void obtenerTerreno_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/terrenos/" + terrenoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(terrenoId, response.getBody().get("id").asLong());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/terrenos?proyectoId={id} - Listar terrenos por proyecto")
    void listarTerrenosPorProyecto_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/terrenos?proyectoId=" + proyectoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/v1/terrenos/{id} - Actualizar terreno")
    void actualizarTerreno_returns200() throws Exception {
        String body = """
                {
                    "manzana": "B",
                    "area": 250.00,
                    "precioBase": 550000.00,
                    "precioAjuste": 0,
                    "precioMultiplicador": 1.0,
                    "precioFinal": 550000.00
                }
                """;

        ResponseEntity<JsonNode> response = putJson("/api/v1/terrenos/" + terrenoId, body, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("B", response.getBody().get("manzana").asText());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/terrenos/99999 - Terreno inexistente retorna 404")
    void obtenerTerreno_inexistente_returns404() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/terrenos/99999", token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("PUT /api/v1/terrenos/99999 - Actualizar inexistente retorna 404")
    void actualizarTerreno_inexistente_returns404() throws Exception {
        String body = """
                {
                    "manzana": "X",
                    "area": 100.00,
                    "precioBase": 100000.00
                }
                """;
        ResponseEntity<JsonNode> response = putJson("/api/v1/terrenos/99999", body, token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/terrenos - Lote duplicado retorna 409")
    void crearTerreno_loteDuplicado_returns409() throws Exception {
        String body = """
                {
                    "proyectoId": %d,
                    "numeroLote": "A-001",
                    "manzana": "A",
                    "area": 200.00,
                    "precioBase": 400000.00
                }
                """.formatted(proyectoId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/terrenos", body, token);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(8)
    @DisplayName("PATCH /api/v1/terrenos/{id}/estado - Cambiar estado a RESERVADO")
    void cambiarEstado_returns200() throws Exception {
        ResponseEntity<JsonNode> response = patchJson(
                "/api/v1/terrenos/" + terrenoId + "/estado?estado=RESERVADO", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("RESERVADO", response.getBody().get("estado").asText());
    }

    @Test
    @Order(9)
    @DisplayName("DELETE /api/v1/terrenos/{id} - Eliminar terreno en estado RESERVADO")
    void eliminarTerreno_returns204() throws Exception {
        ResponseEntity<Void> response = deleteJson("/api/v1/terrenos/" + terrenoId, token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
