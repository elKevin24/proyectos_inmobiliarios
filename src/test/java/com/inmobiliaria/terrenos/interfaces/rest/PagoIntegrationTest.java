package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.CreateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.pago.CreatePagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.CreatePlanPagoRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
import com.inmobiliaria.terrenos.domain.enums.MetodoPago;
import com.inmobiliaria.terrenos.domain.enums.TipoPlanPago;
import com.inmobiliaria.terrenos.domain.enums.TipoPrecio;
import com.inmobiliaria.terrenos.infrastructure.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class PagoIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;
    private Long planPagoId;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Pagos " + unique);
        req.setEmailEmpresa("pagos-" + unique + "@test.com");
        req.setEmail("admin-pagos-" + unique + "@test.com");
        req.setPassword("Password123!");
        req.setNombre("Admin");
        req.setApellido("Test");
        req.setTelefono("1234567890");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        token = "Bearer " + auth.getAccessToken();

        CreateProyectoRequest projReq = new CreateProyectoRequest();
        projReq.setNombre("Proyecto Pagos Test");
        projReq.setDireccion("Direccion 400");
        projReq.setCiudad("Ciudad Test");
        projReq.setEstado("Estado Test");
        projReq.setCodigoPostal("12345");
        projReq.setTipoPrecio(TipoPrecio.FIJO);

        result = mockMvc.perform(post("/api/v1/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long proyectoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        CreateTerrenoRequest terrenoReq = CreateTerrenoRequest.builder()
                .proyectoId(proyectoId)
                .numeroLote("PG-LOT-01")
                .area(new BigDecimal("200.00"))
                .precioBase(new BigDecimal("2000000.00"))
                .build();

        result = mockMvc.perform(post("/api/v1/terrenos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(terrenoReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long terrenoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        CreateClienteRequest clienteReq = CreateClienteRequest.builder()
                .nombre("Ana")
                .apellido("Pago")
                .telefono("5556667788")
                .email("ana@pago.com")
                .build();
        result = mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteReq)))
                .andExpect(status().isCreated())
                .andReturn();
        Long clienteId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        CreateVentaRequest ventaReq = CreateVentaRequest.builder()
                .terrenoId(terrenoId)
                .clienteId(clienteId)
                .montoTotal(new BigDecimal("2000000.00"))
                .enganche(new BigDecimal("500000.00"))
                .montoFinanciado(new BigDecimal("1500000.00"))
                .metodoPago("TRANSFERENCIA")
                .build();

        result = mockMvc.perform(post("/api/v1/ventas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long ventaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        CreatePlanPagoRequest planReq = CreatePlanPagoRequest.builder()
                .ventaId(ventaId)
                .tipoPlan(TipoPlanPago.FINANCIAMIENTO_PROPIO)
                .montoTotal(new BigDecimal("2000000.00"))
                .enganche(new BigDecimal("300000.00"))
                .aplicaInteres(false)
                .numeroPagos(12)
                .plazoMeses(12)
                .tasaMoraMensual(new BigDecimal("2.00"))
                .diasGracia(5)
                .fechaInicio(LocalDate.now().plusDays(1))
                .fechaPrimerPago(LocalDate.now().plusDays(31))
                .build();

        result = mockMvc.perform(post("/api/v1/planes-pago")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planReq)))
                .andExpect(status().isCreated())
                .andReturn();

        planPagoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void registrarPago_Exitoso() throws Exception {
        CreatePagoRequest request = CreatePagoRequest.builder()
                .planPagoId(planPagoId)
                .fechaPago(LocalDate.now())
                .montoPagado(new BigDecimal("150000.00"))
                .metodoPago(MetodoPago.EFECTIVO)
                .build();

        mockMvc.perform(post("/api/v1/pagos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montoPagado").value(150000.00))
                .andExpect(jsonPath("$.metodoPago").value("EFECTIVO"));
    }

    @Test
    void registrarPago_Transferencia_ConReferencia() throws Exception {
        CreatePagoRequest request = CreatePagoRequest.builder()
                .planPagoId(planPagoId)
                .fechaPago(LocalDate.now())
                .montoPagado(new BigDecimal("200000.00"))
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .referenciaPago("REF-12345")
                .build();

        mockMvc.perform(post("/api/v1/pagos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metodoPago").value("TRANSFERENCIA"));
    }

    @Test
    void registrarPago_Transferencia_SinReferencia_Retorna400() throws Exception {
        CreatePagoRequest request = CreatePagoRequest.builder()
                .planPagoId(planPagoId)
                .fechaPago(LocalDate.now())
                .montoPagado(new BigDecimal("200000.00"))
                .metodoPago(MetodoPago.TRANSFERENCIA)
                .build();

        mockMvc.perform(post("/api/v1/pagos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarPago_DatosInvalidos_Retorna400() throws Exception {
        CreatePagoRequest request = CreatePagoRequest.builder()
                .planPagoId(null)
                .montoPagado(new BigDecimal("-100"))
                .metodoPago(null)
                .build();

        mockMvc.perform(post("/api/v1/pagos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
