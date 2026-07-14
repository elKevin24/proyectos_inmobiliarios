package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.ProyectoResponse;
import com.inmobiliaria.terrenos.application.dto.proyecto.UpdateProyectoRequest;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ProyectoMapper;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ProyectoMapper proyectoMapper;

    @InjectMocks
    private ProyectoService proyectoService;

    private final Long tenantId = 1L;
    private Proyecto proyecto;
    private ProyectoResponse proyectoResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        proyecto = Proyecto.builder()
                .id(100L)
                .tenantId(tenantId)
                .nombre("Proyecto Residencial")
                .estadoProyecto(EstadoProyecto.PLANIFICACION)
                .totalTerrenos(10)
                .terrenosDisponibles(10)
                .terrenosApartados(0)
                .terrenosVendidos(0)
                .build();

        proyectoResponse = new ProyectoResponse();
        proyectoResponse.setId(100L);
        proyectoResponse.setNombre("Proyecto Residencial");
        proyectoResponse.setEstadoProyecto("PLANIFICACION");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listarProyectos_Exitoso() {
        when(proyectoRepository.findByTenantIdAndDeletedFalse(tenantId))
                .thenReturn(List.of(proyecto));
        when(proyectoMapper.toResponseList(any()))
                .thenReturn(List.of(proyectoResponse));

        List<ProyectoResponse> resultado = proyectoService.listarProyectos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Proyecto Residencial", resultado.get(0).getNombre());
        verify(proyectoRepository, times(1)).findByTenantIdAndDeletedFalse(tenantId);
    }

    @Test
    void obtenerProyecto_Existente_Exitoso() {
        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId))
                .thenReturn(Optional.of(proyecto));
        when(proyectoMapper.toResponse(proyecto))
                .thenReturn(proyectoResponse);

        ProyectoResponse resultado = proyectoService.obtenerProyecto(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        verify(proyectoRepository, times(1)).findByIdAndTenantIdAndDeletedFalse(100L, tenantId);
    }

    @Test
    void obtenerProyecto_Inexistente_LanzaExcepcion() {
        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(999L, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> proyectoService.obtenerProyecto(999L));
    }

    @Test
    void crearProyecto_NombreDuplicado_LanzaExcepcion() {
        CreateProyectoRequest request = new CreateProyectoRequest();
        request.setNombre("Proyecto Residencial");

        when(proyectoRepository.existsByTenantIdAndNombreIgnoreCaseAndDeletedFalse(tenantId, "Proyecto Residencial"))
                .thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> proyectoService.crearProyecto(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(proyectoRepository, never()).save(any());
    }

    @Test
    void crearProyecto_Exitoso() {
        CreateProyectoRequest request = new CreateProyectoRequest();
        request.setNombre("Proyecto Residencial");

        when(proyectoRepository.existsByTenantIdAndNombreIgnoreCaseAndDeletedFalse(tenantId, "Proyecto Residencial"))
                .thenReturn(false);
        when(proyectoMapper.toEntity(request))
                .thenReturn(proyecto);
        when(proyectoRepository.save(proyecto))
                .thenReturn(proyecto);
        when(proyectoMapper.toResponse(proyecto))
                .thenReturn(proyectoResponse);

        ProyectoResponse resultado = proyectoService.crearProyecto(request);

        assertNotNull(resultado);
        assertEquals("Proyecto Residencial", resultado.getNombre());
        verify(proyectoRepository, times(1)).save(proyecto);
    }

    @Test
    void eliminarProyecto_ConTerrenosVendidos_LanzaExcepcion() {
        proyecto.setTerrenosVendidos(5);
        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId))
                .thenReturn(Optional.of(proyecto));

        BusinessException exception = assertThrows(BusinessException.class, () -> proyectoService.eliminarProyecto(100L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(proyectoRepository, never()).save(any());
    }

    @Test
    void eliminarProyecto_Exitoso() {
        proyecto.setTerrenosVendidos(0);
        proyecto.setTerrenosApartados(0);
        when(proyectoRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId))
                .thenReturn(Optional.of(proyecto));

        proyectoService.eliminarProyecto(100L);

        assertTrue(proyecto.getDeleted());
        verify(proyectoRepository, times(1)).save(proyecto);
    }
}
