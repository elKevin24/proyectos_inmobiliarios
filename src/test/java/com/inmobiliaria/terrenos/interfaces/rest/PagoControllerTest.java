package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.pago.CreatePagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PagoResponse;
import com.inmobiliaria.terrenos.application.service.PagoService;
import com.inmobiliaria.terrenos.domain.enums.MetodoPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PagoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        pagoService = Mockito.mock(PagoService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(new PagoController(pagoService)).build();
    }

    @Test
    void registrarPago_Retorna201() throws Exception {
        CreatePagoRequest request = CreatePagoRequest.builder()
                .planPagoId(100L)
                .montoPagado(BigDecimal.valueOf(2500))
                .fechaPago(LocalDate.now())
                .metodoPago(MetodoPago.EFECTIVO)
                .build();
        PagoResponse response = PagoResponse.builder().id(300L).build();

        when(pagoService.aplicarPago(any(CreatePagoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void obtenerPago_Retorna200() throws Exception {
        when(pagoService.obtenerPago(300L)).thenReturn(PagoResponse.builder().id(300L).build());

        mockMvc.perform(get("/api/v1/pagos/300"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPagosPorVenta_Retorna200() throws Exception {
        when(pagoService.listarPagosPorVenta(200L)).thenReturn(List.of(PagoResponse.builder().id(300L).build()));

        mockMvc.perform(get("/api/v1/pagos/venta/200"))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarPago_Retorna204() throws Exception {
        doNothing().when(pagoService).eliminarPago(300L);

        mockMvc.perform(delete("/api/v1/pagos/300"))
                .andExpect(status().isNoContent());
    }
}
