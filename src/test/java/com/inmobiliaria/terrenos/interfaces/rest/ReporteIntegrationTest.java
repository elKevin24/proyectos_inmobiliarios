package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.domain.enums.TipoPrecio;
import com.inmobiliaria.terrenos.infrastructure.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ReporteIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Reportes " + unique);
        req.setEmailEmpresa("reportes-" + unique + "@test.com");
        req.setEmail("admin-reportes-" + unique + "@test.com");
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
    }

    @Test
    void obtenerDashboard_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/dashboard")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProyectos").isNumber())
                .andExpect(jsonPath("$.totalTerrenos").isNumber())
                .andExpect(jsonPath("$.totalVentas").isNumber());
    }

    @Test
    void obtenerEstadisticasPorProyecto_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/proyectos")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void obtenerEstadisticasProyecto_PorId() throws Exception {
        CreateProyectoRequest projReq = new CreateProyectoRequest();
        projReq.setNombre("Proyecto Reportes Test");
        projReq.setDireccion("Direccion 500");
        projReq.setCiudad("Ciudad Test");
        projReq.setEstado("Estado Test");
        projReq.setCodigoPostal("12345");
        projReq.setTipoPrecio(TipoPrecio.FIJO);

        MvcResult result = mockMvc.perform(post("/api/v1/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long proyectoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/reportes/proyectos/" + proyectoId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proyectoId").value(proyectoId));
    }

    @Test
    void obtenerEstadisticasProyecto_NoEncontrado_Retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/proyectos/99999")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/dashboard"))
                .andExpect(status().isForbidden());
    }
}
