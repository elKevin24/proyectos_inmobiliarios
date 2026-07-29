package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CreateCotizacionRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
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
class CotizacionIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;
    private Long terrenoId;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Cotizaciones " + unique);
        req.setEmailEmpresa("cotizaciones-" + unique + "@test.com");
        req.setEmail("admin-cotizaciones-" + unique + "@test.com");
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
        projReq.setNombre("Proyecto Cotizaciones Test");
        projReq.setDireccion("Direccion 789");
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
                .numeroLote("COT-LOT-01")
                .area(new BigDecimal("250.00"))
                .precioBase(new BigDecimal("2000000.00"))
                .build();

        result = mockMvc.perform(post("/api/v1/terrenos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(terrenoReq)))
                .andExpect(status().isCreated())
                .andReturn();

        terrenoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private CreateCotizacionRequest crearCotizacionBase() {
        return CreateCotizacionRequest.builder()
                .terrenoId(terrenoId)
                .clienteNombre("Juan Cotizacion")
                .clienteEmail("juan@cotizacion.com")
                .clienteTelefono("5559876543")
                .precioBase(new BigDecimal("2000000.00"))
                .precioFinal(new BigDecimal("1900000.00"))
                .fechaVigencia(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    void crearCotizacion_Exitosa() throws Exception {
        CreateCotizacionRequest request = crearCotizacionBase();

        mockMvc.perform(post("/api/v1/cotizaciones")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.terrenoId").value(terrenoId))
                .andExpect(jsonPath("$.precioFinal").value(1900000.00));
    }

    @Test
    void crearCotizacion_DatosInvalidos_Retorna400() throws Exception {
        CreateCotizacionRequest request = CreateCotizacionRequest.builder()
                .terrenoId(null)
                .clienteNombre("")
                .precioBase(new BigDecimal("-100"))
                .build();

        mockMvc.perform(post("/api/v1/cotizaciones")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarCotizaciones_Exitoso() throws Exception {
        mockMvc.perform(post("/api/v1/cotizaciones").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearCotizacionBase())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cotizaciones").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerCotizacion_PorId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/cotizaciones").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearCotizacionBase())))
                .andExpect(status().isCreated())
                .andReturn();

        Long cotizacionId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/cotizaciones/" + cotizacionId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioFinal").value(1900000.00));
    }

    @Test
    void eliminarCotizacion_Exitoso() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/cotizaciones").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearCotizacionBase())))
                .andExpect(status().isCreated())
                .andReturn();

        Long cotizacionId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/v1/cotizaciones/" + cotizacionId).header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/cotizaciones"))
                .andExpect(status().isForbidden());
    }
}
