package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.CreateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class VentaIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;
    private Long terrenoId;
    private Long clienteId;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Ventas " + unique);
        req.setEmailEmpresa("ventas-" + unique + "@test.com");
        req.setEmail("admin-ventas-" + unique + "@test.com");
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
        projReq.setNombre("Proyecto Ventas Test");
        projReq.setDireccion("Direccion 200");
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
                .numeroLote("VTA-LOT-01")
                .area(new BigDecimal("300.00"))
                .precioBase(new BigDecimal("2500000.00"))
                .build();

        result = mockMvc.perform(post("/api/v1/terrenos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(terrenoReq)))
                .andExpect(status().isCreated())
                .andReturn();

        terrenoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        CreateClienteRequest clienteReq = CreateClienteRequest.builder()
                .nombre("Carlos")
                .apellido("Compra")
                .telefono("5559998877")
                .email("carlos@venta.com")
                .build();
        result = mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteReq)))
                .andExpect(status().isCreated())
                .andReturn();
        clienteId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private CreateVentaRequest crearVentaBase() {
        return CreateVentaRequest.builder()
                .terrenoId(terrenoId)
                .clienteId(clienteId)
                .montoTotal(new BigDecimal("2500000.00"))
                .enganche(new BigDecimal("500000.00"))
                .montoFinanciado(new BigDecimal("2000000.00"))
                .metodoPago("TRANSFERENCIA")
                .build();
    }

    @Test
    void crearVenta_Exitosa() throws Exception {
        CreateVentaRequest request = crearVentaBase();

        mockMvc.perform(post("/api/v1/ventas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.compradorNombre").value("Carlos Compra"))
                .andExpect(jsonPath("$.terrenoId").value(terrenoId));
    }

    @Test
    void crearVenta_DatosInvalidos_Retorna400() throws Exception {
        CreateVentaRequest request = CreateVentaRequest.builder()
                .terrenoId(null)
                .clienteId(null)
                .montoTotal(new BigDecimal("-100"))
                .build();

        mockMvc.perform(post("/api/v1/ventas")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarVentas_Exitoso() throws Exception {
        mockMvc.perform(post("/api/v1/ventas").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearVentaBase())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/ventas").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerVenta_PorId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/ventas").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearVentaBase())))
                .andExpect(status().isCreated())
                .andReturn();

        Long ventaId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/ventas/" + ventaId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compradorNombre").value("Carlos Compra"));
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isForbidden());
    }
}
