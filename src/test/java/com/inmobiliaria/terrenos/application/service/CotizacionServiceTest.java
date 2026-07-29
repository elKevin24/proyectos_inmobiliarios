package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.cotizacion.CotizacionResponse;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CreateCotizacionRequest;
import com.inmobiliaria.terrenos.domain.entity.Cotizacion;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.repository.CotizacionRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.CotizacionMapper;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private CotizacionMapper cotizacionMapper;

    @InjectMocks
    private CotizacionService cotizacionService;

    private final Long tenantId = 1L;
    private Cotizacion cotizacion;
    private CotizacionResponse cotizacionResponse;
    private Terreno terreno;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        terreno = Terreno.builder()
                .id(200L)
                .tenantId(tenantId)
                .numeroLote("Lote 05")
                .build();

        cotizacion = Cotizacion.builder()
                .id(1L)
                .tenantId(tenantId)
                .terrenoId(200L)
                .clienteId(10L)
                .precioFinal(BigDecimal.valueOf(120000))
                .build();

        cotizacionResponse = new CotizacionResponse();
        cotizacionResponse.setId(1L);
        cotizacionResponse.setClienteNombre("Ana López");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listarCotizaciones_Exitoso() {
        when(cotizacionRepository.findByTenantIdAndDeletedFalse(tenantId)).thenReturn(List.of(cotizacion));
        when(cotizacionMapper.toResponseList(any())).thenReturn(List.of(cotizacionResponse));

        List<CotizacionResponse> resultado = cotizacionService.listarCotizaciones();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cotizacionRepository, times(1)).findByTenantIdAndDeletedFalse(tenantId);
    }

    @Test
    void obtenerCotizacion_Existente_Exitoso() {
        when(cotizacionRepository.findByIdAndTenantIdAndDeletedFalse(1L, tenantId)).thenReturn(Optional.of(cotizacion));
        when(cotizacionMapper.toResponse(cotizacion)).thenReturn(cotizacionResponse);

        CotizacionResponse resultado = cotizacionService.obtenerCotizacion(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerCotizacion_Inexistente_LanzaExcepcion() {
        when(cotizacionRepository.findByIdAndTenantIdAndDeletedFalse(99L, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cotizacionService.obtenerCotizacion(99L));
    }

    @Test
    void crearCotizacion_TerrenoInexistente_LanzaExcepcion() {
        CreateCotizacionRequest request = new CreateCotizacionRequest();
        request.setTerrenoId(999L);

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(999L, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cotizacionService.crearCotizacion(request));
    }

    @Test
    void crearCotizacion_Exitoso() {
        CreateCotizacionRequest request = new CreateCotizacionRequest();
        request.setTerrenoId(200L);
        request.setClienteNombre("Ana López");

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(200L, tenantId)).thenReturn(Optional.of(terreno));
        when(cotizacionMapper.toEntity(request)).thenReturn(cotizacion);
        when(cotizacionRepository.save(any())).thenReturn(cotizacion);
        when(cotizacionMapper.toResponse(cotizacion)).thenReturn(cotizacionResponse);

        CotizacionResponse resultado = cotizacionService.crearCotizacion(request);

        assertNotNull(resultado);
        verify(cotizacionRepository, times(1)).save(any());
    }

    @Test
    void eliminarCotizacion_Exitoso() {
        when(cotizacionRepository.findByIdAndTenantIdAndDeletedFalse(1L, tenantId)).thenReturn(Optional.of(cotizacion));

        cotizacionService.eliminarCotizacion(1L);

        assertTrue(cotizacion.getDeleted());
        verify(cotizacionRepository, times(1)).save(cotizacion);
    }
}
