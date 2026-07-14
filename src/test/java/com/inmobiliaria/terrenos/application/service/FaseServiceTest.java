package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.fase.CreateFaseRequest;
import com.inmobiliaria.terrenos.application.dto.fase.FaseResponse;
import com.inmobiliaria.terrenos.application.dto.fase.UpdateFaseRequest;
import com.inmobiliaria.terrenos.domain.entity.Fase;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.FaseRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.FaseMapper;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FaseServiceTest {

    @Mock
    private FaseRepository faseRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private FaseMapper faseMapper;

    @InjectMocks
    private FaseService faseService;

    private final Long tenantId = 1L;
    private final Long proyectoId = 200L;
    private Fase fase;
    private Proyecto proyecto;
    private FaseResponse faseResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        proyecto = Proyecto.builder()
                .id(proyectoId)
                .tenantId(tenantId)
                .nombre("Proyecto Residencial")
                .build();

        fase = Fase.builder()
                .id(300L)
                .tenantId(tenantId)
                .proyectoId(proyectoId)
                .nombre("Fase 1")
                .numeroFase(1)
                .activa(true)
                .totalTerrenos(0)
                .terrenosDisponibles(0)
                .build();

        faseResponse = new FaseResponse();
        faseResponse.setId(300L);
        faseResponse.setNombre("Fase 1");
        faseResponse.setProyectoId(proyectoId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void obtenerFase_Existente_Exitoso() {
        when(faseRepository.findByIdAndTenantIdAndDeletedFalse(300L, tenantId))
                .thenReturn(Optional.of(fase));
        when(faseMapper.toResponse(fase))
                .thenReturn(faseResponse);

        FaseResponse resultado = faseService.obtenerFase(300L);

        assertNotNull(resultado);
        assertEquals(300L, resultado.getId());
        verify(faseRepository, times(1)).findByIdAndTenantIdAndDeletedFalse(300L, tenantId);
    }

    @Test
    void obtenerFase_Inexistente_LanzaExcepcion() {
        when(faseRepository.findByIdAndTenantIdAndDeletedFalse(999L, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> faseService.obtenerFase(999L));
    }

    @Test
    void crearFase_ProyectoInexistente_LanzaExcepcion() {
        CreateFaseRequest request = new CreateFaseRequest();
        request.setProyectoId(999L);
        request.setNombre("Fase Invalida");

        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(999L, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> faseService.crearFase(request));
        verify(faseRepository, never()).save(any());
    }

    @Test
    void crearFase_NombreDuplicado_LanzaExcepcion() {
        CreateFaseRequest request = new CreateFaseRequest();
        request.setProyectoId(proyectoId);
        request.setNombre("Fase 1");

        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId))
                .thenReturn(Optional.of(proyecto));
        when(faseRepository.existsByTenantIdAndProyectoIdAndNombreIgnoreCaseAndDeletedFalse(tenantId, proyectoId, "Fase 1"))
                .thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> faseService.crearFase(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(faseRepository, never()).save(any());
    }

    @Test
    void crearFase_Exitoso() {
        CreateFaseRequest request = new CreateFaseRequest();
        request.setProyectoId(proyectoId);
        request.setNombre("Fase 1");
        request.setNumeroFase(1);

        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId))
                .thenReturn(Optional.of(proyecto));
        when(faseRepository.existsByTenantIdAndProyectoIdAndNombreIgnoreCaseAndDeletedFalse(tenantId, proyectoId, "Fase 1"))
                .thenReturn(false);
        when(faseRepository.existsByTenantIdAndProyectoIdAndNumeroFaseAndDeletedFalse(tenantId, proyectoId, 1))
                .thenReturn(false);
        when(faseMapper.toEntity(request))
                .thenReturn(fase);
        when(faseRepository.save(fase))
                .thenReturn(fase);
        when(faseMapper.toResponse(fase))
                .thenReturn(faseResponse);

        FaseResponse resultado = faseService.crearFase(request);

        assertNotNull(resultado);
        assertEquals("Fase 1", resultado.getNombre());
        verify(faseRepository, times(1)).save(fase);
    }

    @Test
    void eliminarFase_ConTerrenosVendidos_LanzaExcepcion() {
        when(faseRepository.findByIdAndTenantIdAndDeletedFalse(300L, tenantId))
                .thenReturn(Optional.of(fase));
        when(terrenoRepository.countByTenantIdAndFaseIdAndEstadoAndDeletedFalse(tenantId, 300L, EstadoTerreno.VENDIDO))
                .thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> faseService.eliminarFase(300L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(faseRepository, never()).save(any());
    }

    @Test
    void eliminarFase_Exitoso() {
        when(faseRepository.findByIdAndTenantIdAndDeletedFalse(300L, tenantId))
                .thenReturn(Optional.of(fase));
        when(terrenoRepository.countByTenantIdAndFaseIdAndEstadoAndDeletedFalse(tenantId, 300L, EstadoTerreno.VENDIDO))
                .thenReturn(0L);
        when(terrenoRepository.countByTenantIdAndFaseIdAndEstadoAndDeletedFalse(tenantId, 300L, EstadoTerreno.APARTADO))
                .thenReturn(0L);

        faseService.eliminarFase(300L);

        assertTrue(fase.getDeleted());
        verify(faseRepository, times(1)).save(fase);
    }

    @Test
    void actualizarContadoresFase_Exitoso() {
        when(faseRepository.findById(300L))
                .thenReturn(Optional.of(fase));
        when(terrenoRepository.countByTenantIdAndFaseIdAndDeletedFalse(tenantId, 300L))
                .thenReturn(5L);
        when(terrenoRepository.countByTenantIdAndFaseIdAndEstadoAndDeletedFalse(tenantId, 300L, EstadoTerreno.DISPONIBLE))
                .thenReturn(3L);

        faseService.actualizarContadoresFase(300L);

        assertEquals(5, fase.getTotalTerrenos());
        assertEquals(3, fase.getTerrenosDisponibles());
        verify(faseRepository, times(1)).save(fase);
    }
}
