package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.fase.CreateFaseRequest;
import com.inmobiliaria.terrenos.application.dto.fase.UpdateFaseRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class FaseIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String token;
    private Long proyectoId;

    @BeforeEach
    void setUp() throws Exception {
        String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest req = new RegisterRequest();
        req.setNombreEmpresa("Test Fases " + unique);
        req.setEmailEmpresa("fases-" + unique + "@test.com");
        req.setEmail("admin-fases-" + unique + "@test.com");
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
        projReq.setNombre("Proyecto Fases Test");
        projReq.setDireccion("Direccion 123");
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

        proyectoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void crearFase_Exitoso() throws Exception {
        CreateFaseRequest request = CreateFaseRequest.builder()
                .proyectoId(proyectoId)
                .nombre("Fase 1 - Constructora")
                .descripcion("Primera fase del proyecto")
                .numeroFase(1)
                .totalTerrenos(50)
                .activa(true)
                .build();

        mockMvc.perform(post("/api/v1/fases")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Fase 1 - Constructora"))
                .andExpect(jsonPath("$.proyectoId").value(proyectoId))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    void crearFase_DatosInvalidos_Retorna400() throws Exception {
        CreateFaseRequest request = CreateFaseRequest.builder()
                .proyectoId(proyectoId)
                .nombre("AB")
                .build();

        mockMvc.perform(post("/api/v1/fases")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarFases_PorProyecto() throws Exception {
        CreateFaseRequest f1 = CreateFaseRequest.builder()
                .proyectoId(proyectoId).nombre("Fase Alpha").numeroFase(1).activa(true).build();
        CreateFaseRequest f2 = CreateFaseRequest.builder()
                .proyectoId(proyectoId).nombre("Fase Beta").numeroFase(2).activa(false).build();

        mockMvc.perform(post("/api/v1/fases").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/fases").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/fases").header("Authorization", token)
                        .param("proyectoId", proyectoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listarFases_SoloActivas() throws Exception {
        CreateFaseRequest f1 = CreateFaseRequest.builder()
                .proyectoId(proyectoId).nombre("Fase Activa").numeroFase(1).activa(true).build();
        CreateFaseRequest f2 = CreateFaseRequest.builder()
                .proyectoId(proyectoId).nombre("Fase Inactiva").numeroFase(2).activa(false).build();

        mockMvc.perform(post("/api/v1/fases").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/fases").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(f2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/fases").header("Authorization", token)
                        .param("proyectoId", proyectoId.toString())
                        .param("activas", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Fase Activa"));
    }

    @Test
    void obtenerFase_PorId() throws Exception {
        CreateFaseRequest request = CreateFaseRequest.builder()
                .proyectoId(proyectoId).nombre("Fase Buscar").numeroFase(1).activa(true).build();

        MvcResult result = mockMvc.perform(post("/api/v1/fases").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long faseId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/v1/fases/" + faseId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Fase Buscar"));
    }

    @Test
    void obtenerFase_NoEncontrada_Retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/fases/99999").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarFase_Exitoso() throws Exception {
        CreateFaseRequest createReq = CreateFaseRequest.builder()
                .proyectoId(proyectoId).nombre("Fase Original").numeroFase(1).activa(true).build();

        MvcResult result = mockMvc.perform(post("/api/v1/fases").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long faseId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        UpdateFaseRequest updateReq = UpdateFaseRequest.builder()
                .nombre("Fase Actualizada")
                .descripcion("Nueva descripcion")
                .build();

        mockMvc.perform(put("/api/v1/fases/" + faseId).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Fase Actualizada"))
                .andExpect(jsonPath("$.descripcion").value("Nueva descripcion"));
    }

    @Test
    void eliminarFase_Exitoso() throws Exception {
        CreateFaseRequest request = CreateFaseRequest.builder()
                .proyectoId(proyectoId).nombre("Fase Eliminar").numeroFase(1).activa(true).build();

        MvcResult result = mockMvc.perform(post("/api/v1/fases").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long faseId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/v1/fases/" + faseId).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/fases/" + faseId).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinToken_Retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/fases"))
                .andExpect(status().isForbidden());
    }
}
