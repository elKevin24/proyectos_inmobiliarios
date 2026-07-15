package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Planes de Pago")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlanPagoE2ETest extends BaseE2ETest {

    private String token;
    private Long proyectoId;
    private Long terrenoId;
    private Long apartadoId;
    private Long ventaId;
    private Long planPagoId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("planpago" + System.nanoTime());
            token = getAccessToken(response);
            proyectoId = crearProyecto(token, "Proyecto PlanPago E2E");
            terrenoId = crearTerreno(token, proyectoId, "PP-001");
            crearCotizacion();
            crearApartado();
            crearVenta();
        }
    }

    private void crearCotizacion() throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Cliente PlanPago",
                    "precioBase": 400000.00,
                    "precioFinal": 400000.00,
                    "fechaVigencia": "%s"
                }
                """.formatted(terrenoId, LocalDate.now().plusDays(30));
        postJsonWithAuth("/api/v1/cotizaciones", body, token);
    }

    private void crearApartado() throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Cliente PlanPago",
                    "montoApartado": 50000.00,
                    "precioTotal": 400000.00,
                    "duracionDias": 30
                }
                """.formatted(terrenoId);
        ResponseEntity<JsonNode> resp = postJsonWithAuth("/api/v1/apartados", body, token);
        apartadoId = resp.getBody().get("id").asLong();
    }

    private void crearVenta() throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "apartadoId": %d,
                    "compradorNombre": "Cliente PlanPago",
                    "precioTotal": 400000.00,
                    "montoApartadoAcreditado": 50000.00,
                    "montoFinal": 350000.00,
                    "formaPago": "CREDITO_BANCARIO"
                }
                """.formatted(terrenoId, apartadoId);
        ResponseEntity<JsonNode> resp = postJsonWithAuth("/api/v1/ventas", body, token);
        ventaId = resp.getBody().get("id").asLong();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/planes-pago - Crear plan de pago")
    void crearPlanPago_returns201() throws Exception {
        String body = """
                {
                    "ventaId": %d,
                    "tipoPlan": "CREDITO_BANCARIO",
                    "frecuenciaPago": "MENSUAL",
                    "montoTotal": 400000.00,
                    "enganche": 50000.00,
                    "aplicaInteres": true,
                    "tasaInteresAnual": 12.00,
                    "numeroPagos": 12,
                    "plazoMeses": 12,
                    "fechaInicio": "%s",
                    "fechaPrimerPago": "%s"
                }
                """.formatted(ventaId, LocalDate.now(), LocalDate.now().plusMonths(1));

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/planes-pago", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        planPagoId = response.getBody().get("id").asLong();
        assertNotNull(response.getBody().get("totalAmortizaciones"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/planes-pago/{id} - Obtener plan por ID")
    void obtenerPlanPago_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/planes-pago/" + planPagoId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(planPagoId, response.getBody().get("id").asLong());
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/planes-pago/venta/{ventaId} - Obtener plan por venta")
    void obtenerPlanPagoPorVenta_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/planes-pago/venta/" + ventaId, token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(planPagoId, response.getBody().get("id").asLong());
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/v1/planes-pago/{id}/tabla-amortizacion - Tabla de amortizacion")
    void obtenerTablaAmortizacion_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/planes-pago/" + planPagoId + "/tabla-amortizacion", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("amortizaciones"));
        assertTrue(response.getBody().get("amortizaciones").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("GET /api/v1/planes-pago/{id}/estado-cuenta - Estado de cuenta")
    void obtenerEstadoCuenta_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth(
                "/api/v1/planes-pago/" + planPagoId + "/estado-cuenta", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("resumen"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/planes-pago - Plan duplicado retorna 409")
    void crearPlanPago_duplicado_returns409() throws Exception {
        String body = """
                {
                    "ventaId": %d,
                    "tipoPlan": "CONTADO",
                    "frecuenciaPago": "MENSUAL",
                    "montoTotal": 400000.00,
                    "aplicaInteres": false,
                    "numeroPagos": 1,
                    "fechaInicio": "%s",
                    "fechaPrimerPago": "%s"
                }
                """.formatted(ventaId, LocalDate.now(), LocalDate.now().plusMonths(1));

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/planes-pago", body, token);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/v1/planes-pago - Listar planes de pago")
    void listarPlanesPago_returns200() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/planes-pago", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isArray());
        assertTrue(response.getBody().size() >= 1);
    }
}
