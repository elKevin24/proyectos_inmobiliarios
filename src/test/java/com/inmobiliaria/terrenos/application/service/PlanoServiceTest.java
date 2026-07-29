package com.inmobiliaria.terrenos.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.plano.CoordenadasPlano;
import com.inmobiliaria.terrenos.application.dto.plano.PlanoInteractivoResponse;
import com.inmobiliaria.terrenos.domain.entity.Archivo;
import com.inmobiliaria.terrenos.domain.entity.Fase;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import com.inmobiliaria.terrenos.domain.repository.ArchivoRepository;
import com.inmobiliaria.terrenos.domain.repository.FaseRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private ArchivoRepository archivoRepository;

    @Mock
    private FaseRepository faseRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PlanoService planoService;

    private static final Long TENANT_ID = 1L;
    private static final Long PROYECTO_ID = 10L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("obtenerPlanoInteractivo - Exito con PLANO_PROYECTO y cálculos de estadísticas")
    void obtenerPlanoInteractivo_Exito() throws Exception {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(PROYECTO_ID);
        proyecto.setNombre("Residencial Los Olivos");

        Archivo plano = new Archivo();
        plano.setId(100L);
        plano.setEsActivo(true);
        plano.setNombreOriginal("plano_general.png");

        Fase fase = new Fase();
        fase.setId(5L);
        fase.setNombre("Fase 1");

        Terreno t1 = Terreno.builder()
                .tenantId(TENANT_ID)
                .proyectoId(PROYECTO_ID)
                .numeroLote("A-01")
                .estado(EstadoTerreno.DISPONIBLE)
                .faseId(5L)
                .area(new BigDecimal("200.00"))
                .coordenadasPlano("{\"x\":10,\"y\":20}")
                .build();
        t1.setId(1L);

        Terreno t2 = Terreno.builder()
                .tenantId(TENANT_ID)
                .proyectoId(PROYECTO_ID)
                .numeroLote("A-02")
                .estado(EstadoTerreno.APARTADO)
                .area(new BigDecimal("220.00"))
                .coordenadasPlano(null)
                .build();
        t2.setId(2L);

        Terreno t3 = Terreno.builder()
                .tenantId(TENANT_ID)
                .proyectoId(PROYECTO_ID)
                .numeroLote("A-03")
                .estado(EstadoTerreno.VENDIDO)
                .area(new BigDecimal("250.00"))
                .coordenadasPlano("")
                .build();
        t3.setId(3L);

        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(PROYECTO_ID, TENANT_ID))
                .thenReturn(Optional.of(proyecto));
        when(archivoRepository.findByProyectoYTipo(TENANT_ID, PROYECTO_ID, TipoArchivo.PLANO_PROYECTO))
                .thenReturn(List.of(plano));
        when(terrenoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(TENANT_ID, PROYECTO_ID))
                .thenReturn(List.of(t1, t2, t3));
        when(faseRepository.findByTenantIdAndProyectoIdAndDeletedFalse(TENANT_ID, PROYECTO_ID))
                .thenReturn(List.of(fase));

        CoordenadasPlano coords = new CoordenadasPlano();
        when(objectMapper.readValue(eq("{\"x\":10,\"y\":20}"), eq(CoordenadasPlano.class)))
                .thenReturn(coords);

        PlanoInteractivoResponse response = planoService.obtenerPlanoInteractivo(PROYECTO_ID);

        assertThat(response).isNotNull();
        assertThat(response.getProyectoId()).isEqualTo(PROYECTO_ID);
        assertThat(response.getProyectoNombre()).isEqualTo("Residencial Los Olivos");
        assertThat(response.getPlanoArchivoId()).isEqualTo(100L);
        assertThat(response.getTotalTerrenos()).isEqualTo(3);
        assertThat(response.getTerrenosDisponibles()).isEqualTo(1);
        assertThat(response.getTerrenosApartados()).isEqualTo(1);
        assertThat(response.getTerrenosVendidos()).isEqualTo(1);
        assertThat(response.getTerrenosConCoordenadas()).isEqualTo(1);
        assertThat(response.getTerrenosSinCoordenadas()).isEqualTo(2);
        assertThat(response.getTerrenos()).hasSize(3);
        assertThat(response.getTerrenos().get(0).getFaseNombre()).isEqualTo("Fase 1");
    }

    @Test
    @DisplayName("obtenerPlanoInteractivo - Usa IMAGEN_PROYECTO si no existe PLANO_PROYECTO")
    void obtenerPlanoInteractivo_FallbackImagenProyecto() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(PROYECTO_ID);
        proyecto.setNombre("Residencial Los Olivos");

        Archivo imagen = new Archivo();
        imagen.setId(200L);
        imagen.setEsActivo(true);
        imagen.setNombreOriginal("render.png");

        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(PROYECTO_ID, TENANT_ID))
                .thenReturn(Optional.of(proyecto));
        when(archivoRepository.findByProyectoYTipo(TENANT_ID, PROYECTO_ID, TipoArchivo.PLANO_PROYECTO))
                .thenReturn(List.of());
        when(archivoRepository.findByProyectoYTipo(TENANT_ID, PROYECTO_ID, TipoArchivo.IMAGEN_PROYECTO))
                .thenReturn(List.of(imagen));
        when(terrenoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(TENANT_ID, PROYECTO_ID))
                .thenReturn(List.of());
        when(faseRepository.findByTenantIdAndProyectoIdAndDeletedFalse(TENANT_ID, PROYECTO_ID))
                .thenReturn(List.of());

        PlanoInteractivoResponse response = planoService.obtenerPlanoInteractivo(PROYECTO_ID);

        assertThat(response).isNotNull();
        assertThat(response.getPlanoArchivoId()).isEqualTo(200L);
        assertThat(response.getPlanoNombre()).isEqualTo("render.png");
    }

    @Test
    @DisplayName("obtenerPlanoInteractivo - Lanza ResourceNotFoundException si el proyecto no existe")
    void obtenerPlanoInteractivo_ProyectoNoExiste_LanzaExcepcion() {
        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(PROYECTO_ID, TENANT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> planoService.obtenerPlanoInteractivo(PROYECTO_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proyecto no encontrado");
    }

    @Test
    @DisplayName("obtenerPlanoInteractivo - Lanza BusinessException si no hay tenantId en contexto")
    void obtenerPlanoInteractivo_SinTenant_LanzaExcepcion() {
        TenantContext.clear();

        assertThatThrownBy(() -> planoService.obtenerPlanoInteractivo(PROYECTO_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tenant_id");
    }
}
