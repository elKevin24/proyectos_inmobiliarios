package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Flujo Completo de Venta")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VentaCompletaE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;
    private Long terrenoId;
    private Long cotizacionId;
    private Long apartadoId;
    private Long ventaId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("venta" + System.nanoTime());
            token = getAccessToken(response);
            proyectoId = crearProyecto(token, "Proyecto Venta E2E");
            terrenoId = crearTerreno(token, proyectoId, "V-001");
        }
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/cotizaciones - Crear cotizacion")
    void crearCotizacion_returns201() throws Exception {
        String fechaVigencia = LocalDate.now().plusDays(30).toString();

        String body = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Juan Perez Comprador",
                    "clienteEmail": "juan@test.com",
                    "clienteTelefono": "4421234567",
                    "precioBase": 400000.00,
                    "descuento": 10000.00,
                    "porcentajeDescuento": 2.5,
                    "precioFinal": 390000.00,
                    "fechaVigencia": "%s",
                    "observaciones": "Cotizacion de prueba E2E"
                }
                """.formatted(terrenoId, fechaVigencia);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/cotizaciones", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        cotizacionId = response.getBody().get("id").asLong();
        assertEquals(terrenoId, response.getBody().get("terrenoId").asLong());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/cotizaciones - Listar cotizaciones")
    void listarCotizaciones_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/cotizaciones", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/apartados - Crear apartado")
    void crearApartado_returns201() throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "cotizacionId": %d,
                    "clienteNombre": "Juan Perez Comprador",
                    "clienteEmail": "juan@test.com",
                    "clienteTelefono": "4421234567",
                    "montoApartado": 50000.00,
                    "precioTotal": 390000.00,
                    "duracionDias": 30,
                    "observaciones": "Apartado de prueba E2E"
                }
                """.formatted(terrenoId, cotizacionId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/apartados", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        apartadoId = response.getBody().get("id").asLong();
        assertEquals("ACTIVO", response.getBody().get("estado").asText());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/terrenos/{id} - Terreno ahora esta APARTADO")
    void terrenoCambioEstado_apartado() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/terrenos/" + terrenoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("APARTADO", response.getBody().get("estado").asText());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/apartados?vigentes=true - Listar apartados vigentes")
    void listarApartadosVigentes_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/apartados?vigentes=true", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/ventas - Crear venta desde apartado")
    void crearVentaDesdeApartado_returns201() throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "apartadoId": %d,
                    "compradorNombre": "Juan Perez Comprador",
                    "compradorEmail": "juan@test.com",
                    "compradorTelefono": "4421234567",
                    "compradorRfc": "PEPJ850101ABC",
                    "precioTotal": 390000.00,
                    "montoApartadoAcreditado": 50000.00,
                    "montoFinal": 340000.00,
                    "porcentajeComision": 5.00,
                    "montoComision": 19500.00,
                    "formaPago": "CONTADO",
                    "observaciones": "Venta de prueba E2E"
                }
                """.formatted(terrenoId, apartadoId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/ventas", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ventaId = response.getBody().get("id").asLong();
        assertEquals("CONTADO", response.getBody().get("formaPago").asText());
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/terrenos/{id} - Terreno ahora esta VENDIDO")
    void terrenoCambioEstado_vendido() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/terrenos/" + terrenoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("VENDIDO", response.getBody().get("estado").asText());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/ventas - Listar ventas")
    void listarVentas_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/ventas", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/v1/ventas/{id} - Obtener venta por ID")
    void obtenerVenta_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/ventas/" + ventaId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ventaId, response.getBody().get("id").asLong());
    }

    @Test
    @Order(10)
    @DisplayName("PATCH /api/v1/ventas/{id}/estado - Cambiar estado de venta")
    void cambiarEstadoVenta_returns200() throws Exception {
        ResponseEntity<JsonNode> response = patchJson(
                "/api/v1/ventas/" + ventaId + "/estado?estado=PAGADO", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("PAGADO", response.getBody().get("estado").asText());
    }

    @Test
    @Order(11)
    @DisplayName("PUT /api/v1/apartados/{id}/cancelar - Cancelar apartado")
    void cancelarApartado_returns200() throws Exception {
        Long proyectoId2 = crearProyecto(token, "Proyecto Cancelar");
        Long terrenoId2 = crearTerreno(token, proyectoId2, "C-001");

        String cotBody = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Cliente Cancelar",
                    "precioBase": 300000.00,
                    "precioFinal": 300000.00,
                    "fechaVigencia": "%s"
                }
                """.formatted(terrenoId2, java.time.LocalDate.now().plusDays(30));
        postJsonWithAuth("/api/v1/cotizaciones", cotBody, token);

        String aptBody = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Cliente Cancelar",
                    "montoApartado": 30000.00,
                    "precioTotal": 300000.00,
                    "duracionDias": 15
                }
                """.formatted(terrenoId2);
        ResponseEntity<JsonNode> aptResp = postJsonWithAuth("/api/v1/apartados", aptBody, token);
        Long aptId = aptResp.getBody().get("id").asLong();

        String cancelBody = """
                {
                    "motivo": "Cliente se arrepentio"
                }
                """;
        ResponseEntity<JsonNode> response = putJson("/api/v1/apartados/" + aptId + "/cancelar", cancelBody, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CANCELADO", response.getBody().get("estado").asText());
    }

    @Test
    @Order(12)
    @DisplayName("POST /api/v1/ventas - Venta directa sin apartado")
    void crearVentaDirecta_returns201() throws Exception {
        Long proyectoId2 = crearProyecto(token, "Proyecto Venta Directa");
        Long terrenoId2 = crearTerreno(token, proyectoId2, "VD-001");

        String body = """
                {
                    "terrenoId": %d,
                    "compradorNombre": "Cliente Venta Directa",
                    "precioTotal": 450000.00,
                    "montoFinal": 450000.00,
                    "formaPago": "CONTADO"
                }
                """.formatted(terrenoId2);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/ventas", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("id"));
    }

    @Test
    @Order(13)
    @DisplayName("POST /api/v1/apartados - Apartar terreno no disponible retorna 409")
    void apartarTerrenoNoDisponible_returns409() throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Cliente Test 409",
                    "montoApartado": 50000.00,
                    "precioTotal": 400000.00,
                    "duracionDias": 15
                }
                """.formatted(terrenoId);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/apartados", body, token);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}
