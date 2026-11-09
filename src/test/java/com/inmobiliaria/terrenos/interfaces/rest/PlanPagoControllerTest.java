package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.pago.EstadoCuentaResponse;
import com.inmobiliaria.terrenos.application.dto.pago.PlanPagoResponse;
import com.inmobiliaria.terrenos.application.dto.pago.TablaAmortizacionResponse;
import com.inmobiliaria.terrenos.application.service.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlanPagoControllerTest {

    private MockMvc mockMvc;
    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        pagoService = Mockito.mock(PagoService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PlanPagoController(pagoService)).build();
    }

    @Test
    void listarPlanesPago_Retorna200() throws Exception {
        when(pagoService.listarPlanesPago()).thenReturn(List.of(PlanPagoResponse.builder().id(100L).build()));

        mockMvc.perform(get("/api/v1/planes-pago"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerPlanPago_Retorna200() throws Exception {
        when(pagoService.obtenerPlanPago(100L)).thenReturn(PlanPagoResponse.builder().id(100L).build());

        mockMvc.perform(get("/api/v1/planes-pago/100"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerPlanPagoPorVenta_Retorna200() throws Exception {
        when(pagoService.obtenerPlanPagoPorVenta(200L)).thenReturn(PlanPagoResponse.builder().id(100L).build());

        mockMvc.perform(get("/api/v1/planes-pago/venta/200"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTablaAmortizacion_Retorna200() throws Exception {
        when(pagoService.obtenerTablaAmortizacion(100L)).thenReturn(TablaAmortizacionResponse.builder().planPagoId(100L).build());

        mockMvc.perform(get("/api/v1/planes-pago/100/tabla-amortizacion"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerEstadoCuenta_Retorna200() throws Exception {
        when(pagoService.obtenerEstadoCuenta(100L)).thenReturn(EstadoCuentaResponse.builder().ventaId(200L).build());

        mockMvc.perform(get("/api/v1/planes-pago/100/estado-cuenta"))
                .andExpect(status().isOk());
    }
}
