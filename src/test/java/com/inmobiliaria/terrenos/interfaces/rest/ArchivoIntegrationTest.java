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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ArchivoIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Archivos " + unique);
        req.setEmailEmpresa("archivos-" + unique + "@test.com");
        req.setEmail("admin-archivos-" + unique + "@test.com");
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
    void subirArchivo_PDF_Exitoso() throws Exception {
        byte[] contenido = " contenido de prueba para PDF ".repeat(10).getBytes();

        MockMultipartFile archivo = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                contenido);

        mockMvc.perform(multipart("/api/v1/archivos/upload")
                        .file(archivo)
                        .param("tipo", "PLANO_PROYECTO")
                        .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreOriginal").value("documento.pdf"));
    }

    @Test
    void subirArchivo_ExtensionNoPermitida_Retorna400() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "executable content".getBytes());

        mockMvc.perform(multipart("/api/v1/archivos/upload")
                        .file(archivo)
                        .param("tipo", "PLANO_PROYECTO")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/archivos/proyecto/1/PLANO_PROYECTO"))
                .andExpect(status().isForbidden());
    }
}
