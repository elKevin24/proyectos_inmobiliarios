package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.entity.Venta;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.domain.repository.VentaRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.VentaMapper;
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
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private ApartadoRepository apartadoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private VentaMapper ventaMapper;

    @InjectMocks
    private VentaService ventaService;

    private final Long tenantId = 1L;
    private Venta venta;
    private Terreno terreno;
    private Proyecto proyecto;
    private VentaResponse ventaResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        proyecto = Proyecto.builder()
                .id(10L)
                .tenantId(tenantId)
                .nombre("Proyecto Las Lomas")
                .totalTerrenos(5)
                .terrenosDisponibles(5)
                .terrenosApartados(0)
                .terrenosVendidos(0)
                .deleted(false)
                .build();

        terreno = Terreno.builder()
                .id(50L)
                .tenantId(tenantId)
                .proyectoId(10L)
                .numeroLote("15")
                .estado(EstadoTerreno.DISPONIBLE)
                .build();

        venta = Venta.builder()
                .id(100L)
                .tenantId(tenantId)
                .terrenoId(50L)
                .compradorNombre("Juan Pérez")
                .estado(EstadoVenta.PENDIENTE)
                .precioTotal(BigDecimal.valueOf(150000))
                .deleted(false)
                .build();

        ventaResponse = new VentaResponse();
        ventaResponse.setId(100L);
        ventaResponse.setCompradorNombre("Juan Pérez");
        ventaResponse.setEstado(EstadoVenta.PENDIENTE);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listarVentas_Exitoso() {
        when(ventaRepository.findByTenantIdAndDeletedFalse(tenantId)).thenReturn(List.of(venta));
        when(ventaMapper.toResponseList(any())).thenReturn(List.of(ventaResponse));

        List<VentaResponse> resultado = ventaService.listarVentas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getCompradorNombre());
        verify(ventaRepository, times(1)).findByTenantIdAndDeletedFalse(tenantId);
    }

    @Test
    void listarVentasPorEstado_Exitoso() {
        when(ventaRepository.findByTenantIdAndEstadoAndDeletedFalse(tenantId, EstadoVenta.PENDIENTE))
                .thenReturn(List.of(venta));
        when(ventaMapper.toResponseList(any())).thenReturn(List.of(ventaResponse));

        List<VentaResponse> resultado = ventaService.listarVentasPorEstado(EstadoVenta.PENDIENTE);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(ventaRepository, times(1))
                .findByTenantIdAndEstadoAndDeletedFalse(tenantId, EstadoVenta.PENDIENTE);
    }

    @Test
    void obtenerVenta_Existente_Exitoso() {
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId)).thenReturn(Optional.of(venta));
        when(ventaMapper.toResponse(venta)).thenReturn(ventaResponse);

        VentaResponse resultado = ventaService.obtenerVenta(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    @Test
    void obtenerVenta_Inexistente_LanzaExcepcion() {
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(999L, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ventaService.obtenerVenta(999L));
    }

    @Test
    void crearVenta_Exitoso_SinApartado() {
        CreateVentaRequest request = CreateVentaRequest.builder()
                .terrenoId(50L)
                .compradorNombre("Juan Pérez")
                .precioTotal(BigDecimal.valueOf(150000))
                .build();

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(terreno));
        when(ventaMapper.toEntity(request)).thenReturn(venta);
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(ventaRepository.save(venta)).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(ventaResponse);

        VentaResponse resultado = ventaService.crearVenta(request);

        assertNotNull(resultado);
        assertEquals(EstadoTerreno.VENDIDO, terreno.getEstado());
        verify(terrenoRepository, times(1)).save(terreno);
        verify(ventaRepository, times(1)).save(venta);
        verify(proyectoRepository, times(1)).save(proyecto);
    }

    @Test
    void crearVenta_TerrenoYaVendido_LanzaExcepcion() {
        CreateVentaRequest request = CreateVentaRequest.builder()
                .terrenoId(50L)
                .build();
        terreno.setEstado(EstadoTerreno.VENDIDO);

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(terreno));

        BusinessException exception = assertThrows(BusinessException.class, () -> ventaService.crearVenta(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void crearVenta_Exitoso_ConApartadoActivo() {
        CreateVentaRequest request = CreateVentaRequest.builder()
                .terrenoId(50L)
                .apartadoId(200L)
                .compradorNombre("Juan Pérez")
                .build();

        Apartado apartado = Apartado.builder()
                .id(200L)
                .tenantId(tenantId)
                .estado(EstadoApartado.ACTIVO)
                .build();

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(terreno));
        when(apartadoRepository.findByIdAndTenantIdAndDeletedFalse(200L, tenantId)).thenReturn(Optional.of(apartado));
        when(ventaMapper.toEntity(request)).thenReturn(venta);
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        when(ventaRepository.save(venta)).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(ventaResponse);

        VentaResponse resultado = ventaService.crearVenta(request);

        assertNotNull(resultado);
        assertEquals(EstadoApartado.COMPLETADO, apartado.getEstado());
        verify(apartadoRepository, times(1)).save(apartado);
        verify(terrenoRepository, times(1)).save(terreno);
    }

    @Test
    void cambiarEstado_Exitoso() {
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(venta)).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(ventaResponse);

        VentaResponse resultado = ventaService.cambiarEstado(100L, EstadoVenta.PAGADO);

        assertNotNull(resultado);
        assertEquals(EstadoVenta.PAGADO, venta.getEstado());
        verify(ventaRepository, times(1)).save(venta);
    }

    @Test
    void eliminarVenta_Exitoso() {
        venta.setEstado(EstadoVenta.CANCELADA);
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId)).thenReturn(Optional.of(venta));

        ventaService.eliminarVenta(100L);

        assertTrue(venta.getDeleted());
        verify(ventaRepository, times(1)).save(venta);
    }

    @Test
    void eliminarVenta_NoCancelada_LanzaExcepcion() {
        venta.setEstado(EstadoVenta.PENDIENTE);
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId)).thenReturn(Optional.of(venta));

        BusinessException exception = assertThrows(BusinessException.class, () -> ventaService.eliminarVenta(100L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(ventaRepository, never()).save(any());
    }
}
