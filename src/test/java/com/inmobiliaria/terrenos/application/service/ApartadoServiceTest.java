package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.apartado.ApartadoResponse;
import com.inmobiliaria.terrenos.application.dto.apartado.CreateApartadoRequest;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ApartadoMapper;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApartadoServiceTest {

    @Mock
    private ApartadoRepository apartadoRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private ApartadoMapper apartadoMapper;

    @InjectMocks
    private ApartadoService apartadoService;

    private final Long tenantId = 1L;
    private final Long proyectoId = 200L;
    private final Long terrenoId = 400L;
    private Apartado apartado;
    private Terreno terreno;
    private Proyecto proyecto;
    private ApartadoResponse apartadoResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        proyecto = Proyecto.builder()
                .id(proyectoId)
                .tenantId(tenantId)
                .nombre("Proyecto Residencial")
                .build();

        terreno = Terreno.builder()
                .id(terrenoId)
                .tenantId(tenantId)
                .proyectoId(proyectoId)
                .numeroLote("Lote 1")
                .estado(EstadoTerreno.DISPONIBLE)
                .build();

        apartado = Apartado.builder()
                .id(500L)
                .tenantId(tenantId)
                .terrenoId(terrenoId)
                .montoApartado(new BigDecimal("5000.00"))
                .precioTotal(new BigDecimal("100000.00"))
                .fechaApartado(LocalDate.now())
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .estado(EstadoApartado.ACTIVO)
                .build();

        apartadoResponse = new ApartadoResponse();
        apartadoResponse.setId(500L);
        apartadoResponse.setTerrenoId(terrenoId);
        apartadoResponse.setEstado(EstadoApartado.ACTIVO);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void crearApartado_MontoMayorQuePrecio_LanzaExcepcion() {
        CreateApartadoRequest request = new CreateApartadoRequest();
        request.setTerrenoId(terrenoId);
        request.setMontoApartado(new BigDecimal("120000.00"));
        request.setPrecioTotal(new BigDecimal("100000.00"));

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(terrenoId, tenantId))
                .thenReturn(Optional.of(terreno));

        BusinessException exception = assertThrows(BusinessException.class, () -> apartadoService.crearApartado(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(apartadoRepository, never()).save(any());
    }

    @Test
    void crearApartado_TerrenoNoDisponible_LanzaExcepcion() {
        CreateApartadoRequest request = new CreateApartadoRequest();
        request.setTerrenoId(terrenoId);
        request.setMontoApartado(new BigDecimal("5000.00"));
        request.setPrecioTotal(new BigDecimal("100000.00"));

        terreno.setEstado(EstadoTerreno.VENDIDO);
        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(terrenoId, tenantId))
                .thenReturn(Optional.of(terreno));

        BusinessException exception = assertThrows(BusinessException.class, () -> apartadoService.crearApartado(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(apartadoRepository, never()).save(any());
    }

    @Test
    void crearApartado_Exitoso() {
        CreateApartadoRequest request = new CreateApartadoRequest();
        request.setTerrenoId(terrenoId);
        request.setMontoApartado(new BigDecimal("5000.00"));
        request.setPrecioTotal(new BigDecimal("100000.00"));
        request.setDuracionDias(30);

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(terrenoId, tenantId))
                .thenReturn(Optional.of(terreno));
        when(apartadoMapper.toEntity(request))
                .thenReturn(apartado);
        when(apartadoRepository.save(apartado))
                .thenReturn(apartado);
        when(apartadoMapper.toResponse(apartado))
                .thenReturn(apartadoResponse);

        // Mock para actualizarContadoresProyecto
        when(proyectoRepository.findById(proyectoId))
                .thenReturn(Optional.of(proyecto));

        ApartadoResponse resultado = apartadoService.crearApartado(request);

        assertNotNull(resultado);
        assertEquals(EstadoTerreno.APARTADO, terreno.getEstado()); // Valida que el terreno cambie a APARTADO
        verify(terrenoRepository, times(1)).save(terreno);
        verify(apartadoRepository, times(1)).save(apartado);
        verify(proyectoRepository, times(1)).save(proyecto);
    }

    @Test
    void cancelarApartado_Inactivo_LanzaExcepcion() {
        apartado.setEstado(EstadoApartado.CANCELADO);
        when(apartadoRepository.findByIdAndTenantIdAndDeletedFalse(500L, tenantId))
                .thenReturn(Optional.of(apartado));

        BusinessException exception = assertThrows(BusinessException.class, () -> 
                apartadoService.cancelarApartado(500L, "Motivo"));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(apartadoRepository, never()).save(apartado);
    }

    @Test
    void cancelarApartado_Exitoso() {
        terreno.setEstado(EstadoTerreno.APARTADO);
        apartado.setTerrenoId(terrenoId);

        when(apartadoRepository.findByIdAndTenantIdAndDeletedFalse(500L, tenantId))
                .thenReturn(Optional.of(apartado));
        when(terrenoRepository.findById(terrenoId))
                .thenReturn(Optional.of(terreno));
        when(apartadoRepository.save(apartado))
                .thenReturn(apartado);
        when(apartadoMapper.toResponse(apartado))
                .thenReturn(apartadoResponse);

        // Mock para actualizarContadoresProyecto
        when(proyectoRepository.findById(proyectoId))
                .thenReturn(Optional.of(proyecto));

        apartadoService.cancelarApartado(500L, "Cliente cancelo");

        assertEquals(EstadoApartado.CANCELADO, apartado.getEstado());
        assertEquals(EstadoTerreno.DISPONIBLE, terreno.getEstado()); // Valida que el terreno se libere a DISPONIBLE
        verify(terrenoRepository, times(1)).save(terreno);
        verify(apartadoRepository, times(1)).save(apartado);
        verify(proyectoRepository, times(1)).save(proyecto);
    }

    @Test
    void eliminarApartado_Vigente_LanzaExcepcion() {
        apartado.setEstado(EstadoApartado.ACTIVO);
        when(apartadoRepository.findByIdAndTenantIdAndDeletedFalse(500L, tenantId))
                .thenReturn(Optional.of(apartado));

        BusinessException exception = assertThrows(BusinessException.class, () -> 
                apartadoService.eliminarApartado(500L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(apartadoRepository, never()).save(apartado);
    }

    @Test
    void eliminarApartado_Exitoso() {
        apartado.setEstado(EstadoApartado.CANCELADO);
        when(apartadoRepository.findByIdAndTenantIdAndDeletedFalse(500L, tenantId))
                .thenReturn(Optional.of(apartado));

        apartadoService.eliminarApartado(500L);

        assertTrue(apartado.getDeleted());
        verify(apartadoRepository, times(1)).save(apartado);
    }
}
