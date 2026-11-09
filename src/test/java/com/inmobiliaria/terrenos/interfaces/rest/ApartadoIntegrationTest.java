package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.apartado.CreateApartadoRequest;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
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
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ApartadoIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;
    private Long terrenoId;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Apartados " + unique);
        req.setEmailEmpresa("apartados-" + unique + "@test.com");
        req.setEmail("admin-apartados-" + unique + "@test.com");
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
        projReq.setNombre("Proyecto Apartados Test");
        projReq.setDireccion("Direccion 100");
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
                .numeroLote("APT-LOT-01")
                .area(new BigDecimal("200.00"))
                .precioBase(new BigDecimal("1800000.00"))
                .build();

        result = mockMvc.perform(post("/api/v1/terrenos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(terrenoReq)))
                .andExpect(status().isCreated())
                .andReturn();

        terrenoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private CreateApartadoRequest crearApartadoBase() {
        return CreateApartadoRequest.builder()
                .terrenoId(terrenoId)
                .clienteNombre("Maria Aparto")
                .clienteEmail("maria@apartado.com")
                .clienteTelefono("5551112233")
                .montoApartado(new BigDecimal("100000.00"))
                .precioTotal(new BigDecimal("1800000.00"))
                .duracionDias(15)
                .build();
    }

    @Test
    void crearApartado_Exitoso() throws Exception {
        CreateApartadoRequest request = crearApartadoBase();

        mockMvc.perform(post("/api/v1/apartados")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montoApartado").value(100000.00))
                .andExpect(jsonPath("$.terrenoId").value(terrenoId));
    }

    @Test
    void crearApartado_DatosInvalidos_Retorna400() throws Exception {
        CreateApartadoRequest request = CreateApartadoRequest.builder()
                .terrenoId(null)
                .clienteNombre("")
                .montoApartado(new BigDecimal("-100"))
                .build();

        mockMvc.perform(post("/api/v1/apartados")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarApartados_Exitoso() throws Exception {
        mockMvc.perform(post("/api/v1/apartados").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearApartadoBase())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/apartados").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerApartado_PorId() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/apartados").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearApartadoBase())))
                .andExpect(status().isCreated())
                .andReturn();

        Long apartadoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/apartados/" + apartadoId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoApartado").value(100000.00));
    }

    @Test
    void cancelarApartado_Exitoso() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/apartados").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearApartadoBase())))
                .andExpect(status().isCreated())
                .andReturn();

        Long apartadoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/v1/apartados/" + apartadoId + "/cancelar")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("motivo", "Cliente cambió de opinión"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/apartados"))
                .andExpect(status().isForbidden());
    }
}
