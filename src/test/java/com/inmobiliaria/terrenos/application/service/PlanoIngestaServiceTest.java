package com.inmobiliaria.terrenos.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.plano.ConfirmarIngestaRequest;
import com.inmobiliaria.terrenos.application.dto.plano.CvEngineResponse;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoIngestaServiceTest {

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PlanoStatusStore statusStore;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PlanoIngestaService planoIngestaService;

    private static final Long TENANT_ID = 100L;
    private static final Long PROYECTO_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("iniciarAnalisisPorNombre - Debe generar tareaId UUID y retornar")
    void iniciarAnalisisPorNombre_Exito() {
        String nombreAlmacenado = "plano-uuid123.png";

        String tareaId = planoIngestaService.iniciarAnalisisPorNombre(PROYECTO_ID, nombreAlmacenado);

        assertThat(tareaId).isNotNull();
        assertThat(tareaId).matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    @Test
    @DisplayName("iniciarAnalisis - Debe validar y guardar archivo JPG correctamente")
    void iniciarAnalisis_ArchivoValido_Exito() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "plano_lote.jpg",
                "image/jpeg",
                "dummy image content".getBytes(StandardCharsets.UTF_8)
        );

        String tareaId = planoIngestaService.iniciarAnalisis(PROYECTO_ID, file);

        assertThat(tareaId).isNotNull();
    }

    @Test
    @DisplayName("iniciarAnalisis - Debe lanzar BusinessException si el archivo está vacío")
    void iniciarAnalisis_ArchivoVacio_LanzaExcepcion() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "plano_vacio.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThatThrownBy(() -> planoIngestaService.iniciarAnalisis(PROYECTO_ID, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("iniciarAnalisis - Debe lanzar BusinessException con extensión no permitida")
    void iniciarAnalisis_ExtensionInvalida_LanzaExcepcion() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script_malicioso.exe",
                "application/x-msdownload",
                "executable payload".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> planoIngestaService.iniciarAnalisis(PROYECTO_ID, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Formato no soportado");
    }

    @Test
    @DisplayName("procesarPlanoAsync - Respuesta exitosa de CvEngine debe notificar completado")
    void procesarPlanoAsync_Exito_NotificaCompletado() throws Exception {
        String tareaId = "tarea-test-123";
        String relativePath = "planos/proyecto_1/plano.png";

        CvEngineResponse responseDto = new CvEngineResponse();
        responseDto.setArchivo_procesado(relativePath);
        responseDto.setTotal_lotes_detectados(5);
        responseDto.setLotes(Collections.emptyList());

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CvEngineResponse.class)))
                .thenReturn(new ResponseEntity<>(responseDto, HttpStatus.OK));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"lotes\":[]}");

        planoIngestaService.procesarPlanoAsync(tareaId, relativePath, TENANT_ID);

        verify(statusStore, times(3)).notificar(eq(tareaId), eq("progreso"), anyString());
        verify(statusStore).notificar(eq(tareaId), eq("completado"), eq("{\"lotes\":[]}"));
    }

    @Test
    @DisplayName("procesarPlanoAsync - Error de cliente HTTP (CV Engine rechaza plano) notifica error")
    void procesarPlanoAsync_HttpClientError_NotificaError() {
        String tareaId = "tarea-error-400";
        String relativePath = "planos/proyecto_1/invalido.png";

        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST, "Bad Request", "{\"detail\":\"Formato no válido\"}".getBytes(), StandardCharsets.UTF_8
        );

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CvEngineResponse.class)))
                .thenThrow(exception);

        planoIngestaService.procesarPlanoAsync(tareaId, relativePath, TENANT_ID);

        verify(statusStore).notificar(eq(tareaId), eq("error"), contains("CV Engine rechazó"));
    }

    @Test
    @DisplayName("procesarPlanoAsync - ResourceAccessException (CV Engine caído) notifica servicio no disponible")
    void procesarPlanoAsync_ResourceAccessException_NotificaError() {
        String tareaId = "tarea-down-500";
        String relativePath = "planos/proyecto_1/plano.png";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(CvEngineResponse.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        planoIngestaService.procesarPlanoAsync(tareaId, relativePath, TENANT_ID);

        verify(statusStore).notificar(eq(tareaId), eq("error"), contains("no está disponible"));
    }

    @Test
    @DisplayName("confirmarIngesta - Guarda terrenos correctamente")
    void confirmarIngesta_Exito() {
        ConfirmarIngestaRequest.LoteIngesta loteDto = new ConfirmarIngestaRequest.LoteIngesta();
        loteDto.setNumeroLote("LOTE-01");
        loteDto.setArea(new BigDecimal("250.50"));
        loteDto.setCoordenadasPlanoJson("{\"poligono\":[]}");

        ConfirmarIngestaRequest request = new ConfirmarIngestaRequest();
        request.setProyectoId(PROYECTO_ID);
        request.setLotes(List.of(loteDto));

        planoIngestaService.confirmarIngesta(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Terreno>> captor = ArgumentCaptor.forClass(List.class);
        verify(terrenoRepository).saveAll(captor.capture());

        List<Terreno> guardados = captor.getValue();
        assertThat(guardados).hasSize(1);
        assertThat(guardados.get(0).getNumeroLote()).isEqualTo("LOTE-01");
        assertThat(guardados.get(0).getTenantId()).isEqualTo(TENANT_ID);
        assertThat(guardados.get(0).getProyectoId()).isEqualTo(PROYECTO_ID);
    }

    @Test
    @DisplayName("confirmarIngesta - Lanza excepción si tenantId es nulo")
    void confirmarIngesta_SinTenant_LanzaExcepcion() {
        TenantContext.clear();

        ConfirmarIngestaRequest request = new ConfirmarIngestaRequest();
        request.setProyectoId(PROYECTO_ID);
        request.setLotes(Collections.emptyList());

        assertThatThrownBy(() -> planoIngestaService.confirmarIngesta(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tenant_id");
    }
}
