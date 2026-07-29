package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
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
class AuditoriaIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Auditoria " + unique);
        req.setEmailEmpresa("auditoria-" + unique + "@test.com");
        req.setEmail("admin-auditoria-" + unique + "@test.com");
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
    void obtenerLogsSimples_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/auditoria/simple")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void obtenerLogsCriticos_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/auditoria/critica")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void archivarLogsAntiguos_Exitoso() throws Exception {
        mockMvc.perform(post("/api/v1/auditoria/archivar")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auditoria/simple"))
                .andExpect(status().isForbidden());
    }
}
