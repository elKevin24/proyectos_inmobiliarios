package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - CRUD Clientes")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClienteCRUDE2ETest extends BaseE2ETest {

    private String token;
    private Long clienteId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("cli" + System.nanoTime());
            token = getAccessToken(response);
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/clientes - Crear cliente")
    void crearCliente_returns201() throws Exception {
        String body = """
                {
                    "nombre": "Juan",
                    "apellido": "Perez",
                    "email": "juan_%d@test.com",
                    "telefono": "4421234567",
                    "ciudad": "Queretaro",
                    "estado": "Queretaro",
                    "rfc": "PEPJ850101ABC"
                }
                """.formatted(System.nanoTime());

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/clientes", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        clienteId = response.getBody().get("id").asLong();
        assertEquals("Juan", response.getBody().get("nombre").asText());
        assertEquals("Perez", response.getBody().get("apellido").asText());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/clientes/{id} - Obtener cliente por ID")
    void obtenerCliente_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/clientes/" + clienteId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(clienteId, response.getBody().get("id").asLong());
        assertEquals("Juan", response.getBody().get("nombre").asText());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/clientes - Listar clientes")
    void listarClientes_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/clientes", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("PUT /api/v1/clientes/{id} - Actualizar cliente")
    void actualizarCliente_returns200() throws Exception {
        String body = """
                {
                    "nombre": "Juan Actualizado",
                    "telefono": "4429999999"
                }
                """;

        ResponseEntity<JsonNode> response = putJson("/api/v1/clientes/" + clienteId, body, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Juan Actualizado", response.getBody().get("nombre").asText());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/clientes/{id}/historial - Obtener historial del cliente")
    void obtenerHistorial_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/clientes/" + clienteId + "/historial", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("cliente"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/clientes - Email duplicado retorna 409")
    void crearCliente_emailDuplicado_returns409() throws Exception {
        String email = "dup_" + System.nanoTime() + "@test.com";
        String body1 = """
                {
                    "nombre": "Primero",
                    "apellido": "Dup",
                    "email": "%s",
                    "telefono": "4421111111"
                }
                """.formatted(email);
        postJsonWithAuth("/api/v1/clientes", body1, token);

        String body2 = """
                {
                    "nombre": "Segundo",
                    "apellido": "Dup",
                    "email": "%s",
                    "telefono": "4422222222"
                }
                """.formatted(email);
        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/clientes", body2, token);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/clientes/99999 - Cliente inexistente retorna 404")
    void obtenerCliente_inexistente_returns404() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/clientes/99999", token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(8)
    @DisplayName("DELETE /api/v1/clientes/{id} - Eliminar cliente")
    void eliminarCliente_returns204() throws Exception {
        ResponseEntity<Void> response = deleteJson("/api/v1/clientes/" + clienteId, token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
