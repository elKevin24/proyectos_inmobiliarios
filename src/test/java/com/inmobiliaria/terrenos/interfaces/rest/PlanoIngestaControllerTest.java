package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.plano.ConfirmarIngestaRequest;
import com.inmobiliaria.terrenos.application.service.PlanoIngestaService;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PlanoIngestaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlanoIngestaService planoIngestaService;

    @Mock
    private PlanoStatusStore statusStore;

    @InjectMocks
    private PlanoIngestaController planoIngestaController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final Long PROYECTO_ID = 5L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(planoIngestaController).build();
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("analizarPlano - Retorna HTTP 202 Accepted con tareaId cuando se envía nombreAlmacenado")
    void analizarPlano_Exito() throws Exception {
        String nombreAlmacenado = "plano_uuid_123.jpg";
        String tareaIdMock = "tarea-uuid-456";

        when(planoIngestaService.iniciarAnalisisPorNombre(PROYECTO_ID, nombreAlmacenado))
                .thenReturn(tareaIdMock);

        Map<String, String> body = Map.of("nombreAlmacenado", nombreAlmacenado);

        mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/planos/analizar", PROYECTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.tareaId").value(tareaIdMock))
                .andExpect(jsonPath("$.sseUrl").value("/api/v1/proyectos/5/planos/estado/tarea-uuid-456"));
    }

    @Test
    @DisplayName("analizarPlano - Retorna HTTP 400 Bad Request si falta el nombreAlmacenado")
    void analizarPlano_SinNombre_Retorna400() throws Exception {
        Map<String, String> body = Collections.emptyMap();

        mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/planos/analizar", PROYECTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("nombreAlmacenado es requerido"));
    }

    @Test
    @DisplayName("estadoAnalisis - Suscribe cliente a SSE registrando el emitter en statusStore")
    void estadoAnalisis_RegistraSseEmitter() throws Exception {
        String tareaId = "tarea-sse-789";

        mockMvc.perform(get("/api/v1/proyectos/{proyectoId}/planos/estado/{tareaId}", PROYECTO_ID, tareaId))
                .andExpect(status().isOk());

        verify(statusStore).registrar(eq(tareaId), any(SseEmitter.class));
    }

    @Test
    @DisplayName("confirmarIngesta - Retorna HTTP 200 OK y llama servicio de persistencia")
    void confirmarIngesta_Exito() throws Exception {
        ConfirmarIngestaRequest request = new ConfirmarIngestaRequest();
        request.setLotes(Collections.emptyList());

        mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/planos/confirmar", PROYECTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLotes").value(0));

        verify(planoIngestaService).confirmarIngesta(any(ConfirmarIngestaRequest.class));
    }
}
