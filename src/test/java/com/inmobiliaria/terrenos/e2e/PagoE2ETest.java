package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Pagos")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PagoE2ETest extends BaseE2ETest {

    private String token;
    private Long planPagoId;

    @BeforeEach
    void setUp() throws Exception {
        if (token == null) {
            ResponseEntity<JsonNode> response = registerTenant("pago" + System.nanoTime());
            token = getAccessToken(response);
            Long proyectoId = crearProyecto(token, "Proyecto Pago E2E");
            Long terrenoId = crearTerreno(token, proyectoId, "PG-001");
            crearCotizacion(terrenoId);
            Long apartadoId = crearApartado(terrenoId);
            Long ventaId = crearVenta(terrenoId, apartadoId);
            crearPlanPago(ventaId);
        }
    }

    private void crearCotizacion(Long terrenoId) throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Cliente Pago",
                    "precioBase": 400000.00,
                    "precioFinal": 400000.00,
                    "fechaVigencia": "%s"
                }
                """.formatted(terrenoId, LocalDate.now().plusDays(30));
        postJsonWithAuth("/api/v1/cotizaciones", body, token);
    }

    private Long crearApartado(Long terrenoId) throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "clienteNombre": "Cliente Pago",
                    "montoApartado": 50000.00,
                    "precioTotal": 400000.00,
                    "duracionDias": 30
                }
                """.formatted(terrenoId);
        ResponseEntity<JsonNode> resp = postJsonWithAuth("/api/v1/apartados", body, token);
        return resp.getBody().get("id").asLong();
    }

    private Long crearVenta(Long terrenoId, Long apartadoId) throws Exception {
        String body = """
                {
                    "terrenoId": %d,
                    "apartadoId": %d,
                    "compradorNombre": "Cliente Pago",
                    "precioTotal": 400000.00,
                    "montoApartadoAcreditado": 50000.00,
                    "montoFinal": 350000.00,
                    "formaPago": "CREDITO_BANCARIO"
                }
                """.formatted(terrenoId, apartadoId);
        ResponseEntity<JsonNode> resp = postJsonWithAuth("/api/v1/ventas", body, token);
        return resp.getBody().get("id").asLong();
    }

    private void crearPlanPago(Long ventaId) throws Exception {
        String body = """
                {
                    "ventaId": %d,
                    "tipoPlan": "CREDITO_BANCARIO",
                    "frecuenciaPago": "MENSUAL",
                    "montoTotal": 350000.00,
                    "enganche": 0,
                    "aplicaInteres": false,
                    "numeroPagos": 6,
                    "fechaInicio": "%s",
                    "fechaPrimerPago": "%s"
                }
                """.formatted(ventaId, LocalDate.now(), LocalDate.now().plusMonths(1));
        ResponseEntity<JsonNode> resp = postJsonWithAuth("/api/v1/planes-pago", body, token);
        planPagoId = resp.getBody().get("id").asLong();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/pagos - Registrar pago en efectivo")
    void registrarPagoEfectivo_returns201() throws Exception {
        String body = """
                {
                    "planPagoId": %d,
                    "fechaPago": "%s",
                    "montoPagado": 58333.33,
                    "metodoPago": "EFECTIVO"
                }
                """.formatted(planPagoId, LocalDate.now());

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/pagos", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("id"));
        assertNotNull(response.getBody().get("montoPagado"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/pagos - Plan inexistente retorna 404")
    void registrarPago_planInexistente_returns404() throws Exception {
        String body = """
                {
                    "planPagoId": 99999,
                    "fechaPago": "%s",
                    "montoPagado": 10000.00,
                    "metodoPago": "EFECTIVO"
                }
                """.formatted(LocalDate.now());

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/pagos", body, token);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/pagos - Pago con transferencia requiere referencia")
    void pagoTransferenciaSinReferencia_returnsBadRequest() throws Exception {
        String body = """
                {
                    "planPagoId": %d,
                    "fechaPago": "%s",
                    "montoPagado": 58333.33,
                    "metodoPago": "TRANSFERENCIA"
                }
                """.formatted(planPagoId, LocalDate.now());

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/pagos", body, token);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/pagos - Pago con transferencia + referencia es exitoso")
    void pagoTransferenciaConReferencia_returns201() throws Exception {
        String body = """
                {
                    "planPagoId": %d,
                    "fechaPago": "%s",
                    "montoPagado": 58333.33,
                    "metodoPago": "TRANSFERENCIA",
                    "referenciaPago": "TRF-123456"
                }
                """.formatted(planPagoId, LocalDate.now());

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/pagos", body, token);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}
