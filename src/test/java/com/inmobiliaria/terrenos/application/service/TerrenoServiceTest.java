package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.TerrenoResponse;
import com.inmobiliaria.terrenos.application.dto.terreno.UpdateTerrenoRequest;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.TerrenoMapper;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerrenoServiceTest {

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TerrenoMapper terrenoMapper;

    @InjectMocks
    private TerrenoService terrenoService;

    private final Long tenantId = 1L;
    private final Long proyectoId = 200L;
    private Terreno terreno;
    private Proyecto proyecto;
    private TerrenoResponse terrenoResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        proyecto = Proyecto.builder()
                .id(proyectoId)
                .tenantId(tenantId)
                .nombre("Proyecto Residencial")
                .totalTerrenos(0)
                .terrenosDisponibles(0)
                .build();

        terreno = Terreno.builder()
                .id(400L)
                .tenantId(tenantId)
                .proyectoId(proyectoId)
                .numeroLote("Lote 1")
                .manzana("Manzana A")
                .area(new BigDecimal("200.00"))
                .precioBase(new BigDecimal("100000.00"))
                .precioAjuste(BigDecimal.ZERO)
                .precioMultiplicador(BigDecimal.ONE)
                .estado(EstadoTerreno.DISPONIBLE)
                .build();

        terrenoResponse = new TerrenoResponse();
        terrenoResponse.setId(400L);
        terrenoResponse.setNumeroLote("Lote 1");
        terrenoResponse.setEstado(EstadoTerreno.DISPONIBLE);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void obtenerTerreno_Existente_Exitoso() {
        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(400L, tenantId))
                .thenReturn(Optional.of(terreno));
        when(terrenoMapper.toResponse(terreno))
                .thenReturn(terrenoResponse);

        TerrenoResponse resultado = terrenoService.obtenerTerreno(400L);

        assertNotNull(resultado);
        assertEquals(400L, resultado.getId());
        verify(terrenoRepository, times(1)).findByIdAndTenantIdAndDeletedFalse(400L, tenantId);
    }

    @Test
    void obtenerTerreno_Inexistente_LanzaExcepcion() {
        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(999L, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> terrenoService.obtenerTerreno(999L));
    }

    @Test
    void crearTerreno_NombreDuplicado_LanzaExcepcion() {
        CreateTerrenoRequest request = new CreateTerrenoRequest();
        request.setProyectoId(proyectoId);
        request.setNumeroLote("Lote 1");

        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId))
                .thenReturn(Optional.of(proyecto));
        when(terrenoRepository.existsByTenantIdAndProyectoIdAndNumeroLoteIgnoreCaseAndDeletedFalse(tenantId, proyectoId, "Lote 1"))
                .thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> terrenoService.crearTerreno(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(terrenoRepository, never()).save(any());
    }

    @Test
    void crearTerreno_Exitoso() {
        CreateTerrenoRequest request = new CreateTerrenoRequest();
        request.setProyectoId(proyectoId);
        request.setNumeroLote("Lote 1");

        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId))
                .thenReturn(Optional.of(proyecto));
        when(terrenoRepository.existsByTenantIdAndProyectoIdAndNumeroLoteIgnoreCaseAndDeletedFalse(tenantId, proyectoId, "Lote 1"))
                .thenReturn(false);
        when(terrenoMapper.toEntity(request))
                .thenReturn(terreno);
        when(terrenoRepository.save(terreno))
                .thenReturn(terreno);
        when(terrenoMapper.toResponse(terreno))
                .thenReturn(terrenoResponse);

        // Mocks para actualizarContadoresProyecto
        when(terrenoRepository.countByTenantIdAndProyectoIdAndDeletedFalse(tenantId, proyectoId))
                .thenReturn(1L);
        when(terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(tenantId, proyectoId, EstadoTerreno.DISPONIBLE))
                .thenReturn(1L);

        TerrenoResponse resultado = terrenoService.crearTerreno(request);

        assertNotNull(resultado);
        assertEquals("Lote 1", resultado.getNumeroLote());
        assertEquals(new BigDecimal("100000.00"), terreno.getPrecioFinal()); // Validar el cálculo del precio final
        verify(terrenoRepository, times(1)).save(terreno);
        verify(proyectoRepository, times(1)).save(proyecto);
    }

    @Test
    void cambiarEstado_TransicionInvalida_DisponibleAVendido_LanzaExcepcion() {
        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(400L, tenantId))
                .thenReturn(Optional.of(terreno));

        BusinessException exception = assertThrows(BusinessException.class, () -> 
                terrenoService.cambiarEstado(400L, EstadoTerreno.VENDIDO));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(terrenoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_TransicionInvalida_VendidoADisponible_LanzaExcepcion() {
        terreno.setEstado(EstadoTerreno.VENDIDO);
        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(400L, tenantId))
                .thenReturn(Optional.of(terreno));

        BusinessException exception = assertThrows(BusinessException.class, () -> 
                terrenoService.cambiarEstado(400L, EstadoTerreno.DISPONIBLE));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(terrenoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_Exitoso() {
        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(400L, tenantId))
                .thenReturn(Optional.of(terreno));
        when(terrenoRepository.save(terreno))
                .thenReturn(terreno);
        when(terrenoMapper.toResponse(terreno))
                .thenReturn(terrenoResponse);

        // Mocks para actualizarContadoresProyecto
        when(proyectoRepository.findById(proyectoId))
                .thenReturn(Optional.of(proyecto));

        terrenoService.cambiarEstado(400L, EstadoTerreno.APARTADO);

        assertEquals(EstadoTerreno.APARTADO, terreno.getEstado());
        verify(terrenoRepository, times(1)).save(terreno);
        verify(proyectoRepository, times(1)).save(proyecto);
    }
}
