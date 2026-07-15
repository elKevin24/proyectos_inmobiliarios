package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - CRUD Archivos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArchivoE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;
    private Long archivoId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("arch" + System.nanoTime());
            token = getAccessToken(response);
            proyectoId = crearProyecto(token, "Proyecto Archivo E2E");
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/archivos/upload - Subir archivo")
    void subirArchivo_returns201() throws Exception {
        ByteArrayResource fileResource = new ByteArrayResource("Contenido de prueba PDF".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "plano_test.pdf";
            }
        };

        Map<String, String> params = new HashMap<>();
        params.put("tipo", "PLANO_PROYECTO");
        params.put("proyectoId", String.valueOf(proyectoId));
        params.put("descripcion", "Plano de prueba E2E");

        ResponseEntity<JsonNode> response = postMultipartWithAuth(
                "/api/v1/archivos/upload", "file", fileResource, token, params);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        archivoId = response.getBody().get("id").asLong();
        assertNotNull(response.getBody().get("nombreAlmacenado"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/archivos?proyectoId={id} - Listar archivos por proyecto")
    void listarArchivosPorProyecto_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/archivos?proyectoId=" + proyectoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/archivos/galeria/{proyectoId} - Galeria del proyecto")
    void obtenerGaleria_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/archivos/galeria/" + proyectoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/archivos/versiones/{proyectoId} - Versiones de archivo")
    void obtenerVersiones_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/archivos/versiones/" + proyectoId + "?nombreOriginal=plano_test.pdf", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/archivos/{id}/download - Descargar archivo")
    void descargarArchivo_returns200() throws Exception {
        ResponseEntity<byte[]> response = getClient().exchange(
                baseUrl() + "/api/v1/archivos/" + archivoId + "/download",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /api/v1/archivos/{id} - Eliminar archivo")
    void eliminarArchivo_returns204() throws Exception {
        ResponseEntity<Void> response = deleteJson("/api/v1/archivos/" + archivoId, token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
