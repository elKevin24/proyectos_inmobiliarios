package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.CreateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.UpdateClienteRequest;
import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import com.inmobiliaria.terrenos.infrastructure.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ClienteIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Clientes " + unique);
        req.setEmailEmpresa("clientes-" + unique + "@test.com");
        req.setEmail("admin-clientes-" + unique + "@test.com");
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

    private CreateClienteRequest crearClienteBase(String sufijo) {
        return CreateClienteRequest.builder()
                .nombre("Cliente" + sufijo)
                .apellido("Apellido" + sufijo)
                .email("cliente" + sufijo + "@test.com")
                .telefono("5551234567")
                .estadoCliente(EstadoCliente.PROSPECTO)
                .build();
    }

    @Test
    void crearCliente_Exitoso() throws Exception {
        CreateClienteRequest request = crearClienteBase("Nuevo");

        mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("ClienteNuevo"))
                .andExpect(jsonPath("$.email").value("clienteNuevo@test.com"))
                .andExpect(jsonPath("$.estadoCliente").value("PROSPECTO"));
    }

    @Test
    void crearCliente_DatosInvalidos_Retorna400() throws Exception {
        CreateClienteRequest request = CreateClienteRequest.builder()
                .nombre("")
                .telefono("invalid-phone!@#")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/v1/clientes")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarClientes_Exitoso() throws Exception {
        mockMvc.perform(post("/api/v1/clientes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearClienteBase("1"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/clientes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearClienteBase("2"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/clientes").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listarClientes_PorNombre() throws Exception {
        mockMvc.perform(post("/api/v1/clientes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearClienteBase("Busqueda"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/clientes").header("Authorization", token)
                        .param("nombre", "Busqueda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("ClienteBusqueda"));
    }

    @Test
    void obtenerCliente_PorId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/clientes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearClienteBase("Detalle"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long clienteId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/clientes/" + clienteId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ClienteDetalle"));
    }

    @Test
    void obtenerCliente_NoEncontrado_Retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/99999").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarCliente_Exitoso() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/clientes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearClienteBase("Upd"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long clienteId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        UpdateClienteRequest updateReq = UpdateClienteRequest.builder()
                .nombre("ClienteActualizado")
                .build();

        mockMvc.perform(put("/api/v1/clientes/" + clienteId).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ClienteActualizado"));
    }

    @Test
    void eliminarCliente_Exitoso() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/clientes").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearClienteBase("Del"))))
                .andExpect(status().isCreated())
                .andReturn();

        Long clienteId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/v1/clientes/" + clienteId).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/clientes/" + clienteId).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isForbidden());
    }
}
