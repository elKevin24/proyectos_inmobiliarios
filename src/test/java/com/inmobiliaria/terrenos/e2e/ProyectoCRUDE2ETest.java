package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - CRUD Proyectos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProyectoCRUDE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("proy" + System.nanoTime());
            token = getAccessToken(response);
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/proyectos - Crear proyecto")
    void crearProyecto_returns201() throws Exception {
        String body = """
                {
                    "nombre": "Proyecto E2E Residencial",
                    "descripcion": "Proyecto de prueba E2E",
                    "direccion": "Av. Principal 123",
                    "ciudad": "Queretaro",
                    "estado": "Queretaro",
                    "codigoPostal": "76000",
                    "latitud": 20.5890,
                    "longitud": -100.3890,
                    "tipoPrecio": "FIJO",
                    "precioBase": 500000.00,
                    "totalTerrenos": 20,
                    "terrenosDisponibles": 20,
                    "estadoProyecto": "PLANIFICACION"
                }
                """;

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/proyectos", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        proyectoId = response.getBody().get("id").asLong();
        assertEquals("Proyecto E2E Residencial", response.getBody().get("nombre").asText());
        assertEquals("PLANIFICACION", response.getBody().get("estadoProyecto").asText());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/proyectos/{id} - Obtener proyecto por ID")
    void obtenerProyecto_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos/" + proyectoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(proyectoId, response.getBody().get("id").asLong());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/proyectos - Listar todos los proyectos")
    void listarProyectos_returns200WithList() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(4)
    @DisplayName("PATCH /api/v1/proyectos/{id}/estado - Cambiar estado a EN_VENTA")
    void cambiarEstado_returns200() throws Exception {
        ResponseEntity<JsonNode> response = patchJson(
                "/api/v1/proyectos/" + proyectoId + "/estado?estado=EN_VENTA", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EN_VENTA", response.getBody().get("estadoProyecto").asText());
    }

    @Test
    @Order(5)
    @DisplayName("PUT /api/v1/proyectos/{id} - Actualizar proyecto")
    void actualizarProyecto_returns200() throws Exception {
        String body = """
                {
                    "nombre": "Proyecto E2E Actualizado",
                    "descripcion": "Descripcion actualizada",
                    "direccion": "Av. Actualizada 456",
                    "ciudad": "Queretaro",
                    "estado": "Queretaro",
                    "codigoPostal": "76001",
                    "tipoPrecio": "FIJO",
                    "precioBase": 600000.00
                }
                """;

        ResponseEntity<JsonNode> response = putJson("/api/v1/proyectos/" + proyectoId, body, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Proyecto E2E Actualizado", response.getBody().get("nombre").asText());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/v1/proyectos/{id} - Obtener proyecto inexistente retorna 404")
    void obtenerProyecto_inexistente_returns404() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos/99999", token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/proyectos - Datos invalidos retorna 400")
    void crearProyecto_datosInvalidos_returns400() throws Exception {
        String body = """
                {
                    "nombre": "AB",
                    "direccion": "",
                    "ciudad": "",
                    "estado": "",
                    "codigoPostal": "123"
                }
                """;

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/proyectos", body, token);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(8)
    @DisplayName("PUT /api/v1/proyectos/99999 - Actualizar inexistente retorna 404")
    void actualizarProyecto_inexistente_returns404() throws Exception {
        String body = """
                {
                    "nombre": "No Existe",
                    "direccion": "X",
                    "ciudad": "X",
                    "estado": "X",
                    "codigoPostal": "76000"
                }
                """;

        ResponseEntity<JsonNode> response = putJson("/api/v1/proyectos/99999", body, token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(9)
    @DisplayName("PATCH /api/v1/proyectos/99999/estado - Proyecto inexistente retorna 404")
    void cambiarEstado_proyectoInexistente_returns404() throws Exception {
        ResponseEntity<JsonNode> response = patchJson(
                "/api/v1/proyectos/99999/estado?estado=EN_VENTA", token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(10)
    @DisplayName("DELETE /api/v1/proyectos/{id} - Eliminar proyecto")
    void eliminarProyecto_returns204() throws Exception {
        ResponseEntity<Void> response = deleteJson("/api/v1/proyectos/" + proyectoId, token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
