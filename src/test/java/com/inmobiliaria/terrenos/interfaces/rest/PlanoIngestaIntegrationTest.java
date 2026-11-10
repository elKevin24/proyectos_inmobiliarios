package com.inmobiliaria.terrenos.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.dto.plano.ConfirmarIngestaRequest;
import com.inmobiliaria.terrenos.application.dto.plano.CvEngineResponse;
import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.enums.TipoPrecio;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.BaseIntegrationTest;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para el flujo asíncrono de Ingesta de Planos con SSE
 * y mock del cliente HTTP de CV Engine.
 *
 * @author Kevin
 * @version 1.0.0
 */
@AutoConfigureMockMvc
class PlanoIngestaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private TerrenoRepository terrenoRepository;

    @Autowired
    private PlanoStatusStore statusStore;

    @MockitoBean
    private RestTemplate restTemplate;

    private record TenantSetup(String token, Long proyectoId, Long tenantId) {}

    private TenantSetup registrarTenantYProyecto(String prefijo) throws Exception {
        String unique = prefijo + "_" + UUID.randomUUID().toString().substring(0, 6);
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombreEmpresa("Empresa " + unique);
        registerRequest.setEmailEmpresa("contacto_" + unique.toLowerCase() + "@test.com");
        registerRequest.setEmail("admin_" + unique.toLowerCase() + "@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setNombre("Admin");
        registerRequest.setApellido("Tester");
        registerRequest.setTelefono("1234567890");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse auth = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        String token = "Bearer " + auth.getAccessToken();

        CreateProyectoRequest proyectoRequest = new CreateProyectoRequest();
        proyectoRequest.setNombre("Proyecto Ingesta " + unique);
        proyectoRequest.setDireccion("Avenida Principal 123");
        proyectoRequest.setCiudad("Guadalajara");
        proyectoRequest.setEstado("Jalisco");
        proyectoRequest.setCodigoPostal("44100");
        proyectoRequest.setTipoPrecio(TipoPrecio.FIJO);
        proyectoRequest.setPrecioBase(new BigDecimal("150000.00"));

        MvcResult proyResult = mockMvc.perform(post("/api/v1/proyectos")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proyectoRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> proyMap = objectMapper.readValue(proyResult.getResponse().getContentAsString(), Map.class);
        Long proyectoId = ((Number) proyMap.get("id")).longValue();

        Long tenantId = auth.getUserInfo() != null ? auth.getUserInfo().getTenantId() : 1L;
        return new TenantSetup(token, proyectoId, tenantId);
    }

    @Test
    @DisplayName("Flujo completo: Analizar plano (asíncrono) -> Notificación SSE completado -> Confirmar lotes")
    void flujoCompleto_AnalisisSseYConfirmacion() throws Exception {
        TenantSetup setup = registrarTenantYProyecto("flujo_ok");

        // 1. Configurar Mock de CV Engine Response
        CvEngineResponse mockResponse = new CvEngineResponse();
        mockResponse.setArchivo_procesado("planos/proyecto_" + setup.proyectoId() + "/plano_ok.png");
        mockResponse.setTotal_lotes_detectados(2);
        mockResponse.setTiempo_procesamiento_ms(345.5);

        CvEngineResponse.CvLote lote1 = new CvEngineResponse.CvLote();
        lote1.setId_temporal("L-01");
        lote1.setNumero_lote_detectado("LOTE-101");
        lote1.setArea_detectada("160.00");
        lote1.setConfianza_ocr(96.5);
        lote1.setPoligono(List.of(
                Map.of("x", 0, "y", 0),
                Map.of("x", 100, "y", 0),
                Map.of("x", 100, "y", 100),
                Map.of("x", 0, "y", 100)
        ));

        CvEngineResponse.CvLote lote2 = new CvEngineResponse.CvLote();
        lote2.setId_temporal("L-02");
        lote2.setNumero_lote_detectado("LOTE-102");
        lote2.setArea_detectada("180.50");
        lote2.setConfianza_ocr(91.0);
        lote2.setPoligono(List.of(
                Map.of("x", 100, "y", 0),
                Map.of("x", 200, "y", 0),
                Map.of("x", 200, "y", 100),
                Map.of("x", 100, "y", 100)
        ));

        mockResponse.setLotes(List.of(lote1, lote2));

        when(restTemplate.postForEntity(anyString(), any(), eq(CvEngineResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // 2. Iniciar análisis asíncrono
        Map<String, String> body = Map.of("nombreAlmacenado", "plano_ok.png");
        MvcResult analizarResult = mockMvc.perform(
                        post("/api/v1/proyectos/{id}/planos/analizar", setup.proyectoId())
                                .header("Authorization", setup.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.tareaId").isNotEmpty())
                .andExpect(jsonPath("$.sseUrl").value(containsString("/planos/estado/")))
                .andReturn();

        Map<?, ?> respMap = objectMapper.readValue(analizarResult.getResponse().getContentAsString(), Map.class);
        String tareaId = (String) respMap.get("tareaId");
        assertThat(tareaId).isNotBlank();

        // 3. Esperar que el hilo asíncrono procese la tarea y notifique el evento terminal
        await().atMost(Duration.ofSeconds(5)).until(() -> statusStore.existe(tareaId));

        // 4. Suscribirse a SSE y verificar la entrega del evento 'completado'
        MvcResult sseResult = mockMvc.perform(
                        get("/api/v1/proyectos/{id}/planos/estado/{tareaId}", setup.proyectoId(), tareaId)
                                .header("Authorization", setup.token())
                                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(sseResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("event:completado")))
                .andExpect(content().string(containsString("total_lotes_detectados")))
                .andExpect(content().string(containsString("LOTE-101")));

        // 5. Confirmar ingesta de los lotes detectados
        ConfirmarIngestaRequest confirmarRequest = new ConfirmarIngestaRequest();
        confirmarRequest.setProyectoId(setup.proyectoId());

        ConfirmarIngestaRequest.LoteIngesta ingesta1 = new ConfirmarIngestaRequest.LoteIngesta();
        ingesta1.setNumeroLote("LOTE-101");
        ingesta1.setArea(new BigDecimal("160.00"));
        ingesta1.setCoordenadasPlanoJson("[{\"x\":0,\"y\":0},{\"x\":100,\"y\":0},{\"x\":100,\"y\":100},{\"x\":0,\"y\":100}]");

        ConfirmarIngestaRequest.LoteIngesta ingesta2 = new ConfirmarIngestaRequest.LoteIngesta();
        ingesta2.setNumeroLote("LOTE-102");
        ingesta2.setArea(new BigDecimal("180.50"));
        ingesta2.setCoordenadasPlanoJson("[{\"x\":100,\"y\":0},{\"x\":200,\"y\":0},{\"x\":200,\"y\":100},{\"x\":100,\"y\":100}]");

        confirmarRequest.setLotes(List.of(ingesta1, ingesta2));

        mockMvc.perform(
                        post("/api/v1/proyectos/{id}/planos/confirmar", setup.proyectoId())
                                .header("Authorization", setup.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(confirmarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLotes").value(2))
                .andExpect(jsonPath("$.mensaje").value(containsString("Lotes ingresados exitosamente")));

        // 6. Verificar persistencia en base de datos y contadores del proyecto
        List<Terreno> terrenos = terrenoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(
                setup.tenantId(), setup.proyectoId());
        assertThat(terrenos).hasSize(2);
        assertThat(terrenos.get(0).getNumeroLote()).isEqualTo("LOTE-101");
        assertThat(terrenos.get(0).getEstado()).isEqualTo(EstadoTerreno.DISPONIBLE);
        assertThat(terrenos.get(0).getCoordenadasPlano()).contains("{\"x\":0,\"y\":0}");
        assertThat(terrenos.get(1).getNumeroLote()).isEqualTo("LOTE-102");

        Proyecto proyecto = proyectoRepository.findById(setup.proyectoId()).orElseThrow();
        assertThat(proyecto.getTotalTerrenos()).isEqualTo(2);
        assertThat(proyecto.getTerrenosDisponibles()).isEqualTo(2);
    }

    @Test
    @DisplayName("Error en CV Engine: Emite evento terminal SSE con tipo 'error'")
    void analisisPlano_CvEngineNoDisponible_EmiteEventoErrorSse() throws Exception {
        TenantSetup setup = registrarTenantYProyecto("cv_error");

        // Simular que el motor CV no está disponible (timeout o conexión rechazada)
        when(restTemplate.postForEntity(anyString(), any(), eq(CvEngineResponse.class)))
                .thenThrow(new ResourceAccessException("Conexión rechazada al CV Engine"));

        Map<String, String> body = Map.of("nombreAlmacenado", "plano_caido.png");
        MvcResult analizarResult = mockMvc.perform(
                        post("/api/v1/proyectos/{id}/planos/analizar", setup.proyectoId())
                                .header("Authorization", setup.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isAccepted())
                .andReturn();

        Map<?, ?> respMap = objectMapper.readValue(analizarResult.getResponse().getContentAsString(), Map.class);
        String tareaId = (String) respMap.get("tareaId");

        // Esperar procesamiento asíncrono
        await().atMost(Duration.ofSeconds(5)).until(() -> statusStore.existe(tareaId));

        // Verificar que el canal SSE entrega el evento 'error'
        MvcResult sseResult = mockMvc.perform(
                        get("/api/v1/proyectos/{id}/planos/estado/{tareaId}", setup.proyectoId(), tareaId)
                                .header("Authorization", setup.token())
                                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(sseResult))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("event:error")))
                .andExpect(content().string(containsString("cv-engine esté activo")));
    }

    @Test
    @DisplayName("Aislamiento Multi-tenant: Tenant B no puede acceder a SSE ni confirmar en proyecto de Tenant A")
    void aislamientoMultiTenant_BloqueaAccesoCruzado() throws Exception {
        TenantSetup tenantA = registrarTenantYProyecto("tenantA");
        TenantSetup tenantB = registrarTenantYProyecto("tenantB");

        // Simular tarea iniciada por Tenant A
        String tareaId = UUID.randomUUID().toString();
        statusStore.registrarTareaTenant(tareaId, tenantA.tenantId());
        statusStore.notificar(tareaId, "completado", "{\"total_lotes_detectados\": 1}");

        // Tenant B intenta escuchar SSE de Tenant A -> 403 Forbidden
        mockMvc.perform(
                        get("/api/v1/proyectos/{id}/planos/estado/{tareaId}", tenantA.proyectoId(), tareaId)
                                .header("Authorization", tenantB.token())
                                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isForbidden());

        // Tenant B intenta confirmar lotes en proyecto de Tenant A -> 404 Not Found
        ConfirmarIngestaRequest request = new ConfirmarIngestaRequest();
        ConfirmarIngestaRequest.LoteIngesta lote = new ConfirmarIngestaRequest.LoteIngesta();
        lote.setNumeroLote("HACK-01");
        lote.setArea(new BigDecimal("100.00"));
        request.setLotes(List.of(lote));

        mockMvc.perform(
                        post("/api/v1/proyectos/{id}/planos/confirmar", tenantA.proyectoId())
                                .header("Authorization", tenantB.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Validaciones de entrada: 400 cuando falta nombreAlmacenado o lista de lotes vacía")
    void validacionesEntrada_Retornan400() throws Exception {
        TenantSetup setup = registrarTenantYProyecto("valida");

        // 1. Analizar sin nombreAlmacenado
        mockMvc.perform(
                        post("/api/v1/proyectos/{id}/planos/analizar", setup.proyectoId())
                                .header("Authorization", setup.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("nombreAlmacenado es requerido"));

        // 2. Confirmar con lista vacía
        ConfirmarIngestaRequest request = new ConfirmarIngestaRequest();
        request.setLotes(List.of());

        mockMvc.perform(
                        post("/api/v1/proyectos/{id}/planos/confirmar", setup.proyectoId())
                                .header("Authorization", setup.token())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
