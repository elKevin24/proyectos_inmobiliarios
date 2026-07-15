package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.infrastructure.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ProyectoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse registrarTenant(String email, String empresa) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombreEmpresa(empresa);
        registerRequest.setEmailEmpresa("contacto_" + empresa.toLowerCase().replace(" ", "") + "@test.com");
        registerRequest.setEmail(email);
        registerRequest.setPassword("Password123!");
        registerRequest.setNombre("Admin");
        registerRequest.setApellido("User");
        registerRequest.setTelefono("1234567890");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseBody, AuthResponse.class);
    }

    @Test
    void testAislamientoMultiTenant() throws Exception {
        // 1. Registrar Tenant A y Tenant B
        AuthResponse authTenantA = registrarTenant("admin@tenant-a.com", "Tenant A");
        AuthResponse authTenantB = registrarTenant("admin@tenant-b.com", "Tenant B");

        String tokenA = "Bearer " + authTenantA.getAccessToken();
        String tokenB = "Bearer " + authTenantB.getAccessToken();

        // 2. Crear un proyecto bajo el contexto de Tenant A
        CreateProyectoRequest createA = new CreateProyectoRequest();
        createA.setNombre("Proyecto Residencial A");
        createA.setDireccion("Direccion A");
        createA.setCiudad("Ciudad A");
        createA.setEstado("Estado A");
        createA.setCodigoPostal("12345");
        createA.setTipoPrecio(com.inmobiliaria.terrenos.domain.enums.TipoPrecio.FIJO);

        mockMvc.perform(post("/api/v1/proyectos")
                        .header("Authorization", tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Proyecto Residencial A"));

        // 3. Listar proyectos con el token de Tenant A (debe retornar 1 proyecto)
        mockMvc.perform(get("/api/v1/proyectos")
                        .header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Proyecto Residencial A"));

        // 4. Listar proyectos con el token de Tenant B (debe retornar 0 proyectos, validando aislamiento multi-tenant)
        mockMvc.perform(get("/api/v1/proyectos")
                        .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 5. Crear un proyecto bajo el contexto de Tenant B
        CreateProyectoRequest createB = new CreateProyectoRequest();
        createB.setNombre("Proyecto Residencial B");
        createB.setDireccion("Direccion B");
        createB.setCiudad("Ciudad B");
        createB.setEstado("Estado B");
        createB.setCodigoPostal("54321");
        createB.setTipoPrecio(com.inmobiliaria.terrenos.domain.enums.TipoPrecio.FIJO);

        mockMvc.perform(post("/api/v1/proyectos")
                        .header("Authorization", tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Proyecto Residencial B"));

        // 6. Listar proyectos con el token de Tenant B (debe retornar 1 proyecto)
        mockMvc.perform(get("/api/v1/proyectos")
                        .header("Authorization", tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Proyecto Residencial B"));
    }
}
