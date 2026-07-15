package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - CRUD Fases")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FaseCRUDE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;
    private Long faseId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("fase" + System.nanoTime());
            token = getAccessToken(response);
            proyectoId = crearProyecto(token, "Proyecto Fases E2E");
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/fases - Crear fase")
    void crearFase_returns201() throws Exception {
        String body = """
                {
                    "proyectoId": %d,
                    "nombre": "Fase 1 - Habilitacion",
                    "descripcion": "Fase inicial del proyecto",
                    "numeroFase": 1,
                    "totalTerrenos": 0,
                    "activa": true
                }
                """.formatted(proyectoId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/fases", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        faseId = response.getBody().get("id").asLong();
        assertEquals("Fase 1 - Habilitacion", response.getBody().get("nombre").asText());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/fases/{id} - Obtener fase por ID")
    void obtenerFase_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/fases/" + faseId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(faseId, response.getBody().get("id").asLong());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/fases?proyectoId={id} - Listar fases por proyecto")
    void listarFasesPorProyecto_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/fases?proyectoId=" + proyectoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/v1/fases/{id} - Actualizar fase")
    void actualizarFase_returns200() throws Exception {
        String body = """
                {
                    "nombre": "Fase 1 - Habilitacion v2",
                    "descripcion": "Descripcion actualizada"
                }
                """;

        ResponseEntity<JsonNode> response = putJson("/api/v1/fases/" + faseId, body, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fase 1 - Habilitacion v2", response.getBody().get("nombre").asText());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/fases - Nombre duplicado retorna 409")
    void crearFase_nombreDuplicado_returns409() throws Exception {
        String body = """
                {
                    "proyectoId": %d,
                    "nombre": "Fase 1 - Habilitacion v2",
                    "numeroFase": 99
                }
                """.formatted(proyectoId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/fases", body, token);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/fases/99999 - Fase inexistente retorna 404")
    void obtenerFase_inexistente_returns404() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/fases/99999", token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("DELETE /api/v1/fases/{id} - Eliminar fase")
    void eliminarFase_returns204() throws Exception {
        ResponseEntity<Void> response = deleteJson("/api/v1/fases/" + faseId, token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
