package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.entity.Venta;
import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.domain.repository.VentaRepository;
import com.inmobiliaria.terrenos.domain.repository.ClienteRepository;
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
    private ClienteRepository clienteRepository;

    @Mock
    private VentaMapper ventaMapper;

    @InjectMocks
    private VentaService ventaService;

    private final Long tenantId = 1L;
    private Terreno terreno;
    private Venta venta;
    private VentaResponse ventaResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        terreno = Terreno.builder()
                .id(200L)
                .tenantId(tenantId)
                .proyectoId(100L)
                .numeroLote("Lote 01")
                .estado(EstadoTerreno.DISPONIBLE)
                .build();

        terreno = Terreno.builder().id(200L).tenantId(tenantId).estado(EstadoTerreno.DISPONIBLE).proyectoId(100L).build();
        venta = Venta.builder().id(50L).tenantId(tenantId).terrenoId(200L).estado(EstadoVenta.PENDIENTE).precioTotal(new BigDecimal("100000")).build();
        ventaResponse = new VentaResponse();
        ventaResponse.setId(50L);
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
        verify(ventaRepository, times(1)).findByTenantIdAndDeletedFalse(tenantId);
    }

    @Test
    void obtenerVenta_Existente_RetornaVenta() {
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(venta));
        when(ventaMapper.toResponse(venta)).thenReturn(ventaResponse);

        VentaResponse resultado = ventaService.obtenerVenta(50L);

        assertNotNull(resultado);
        assertEquals(50L, resultado.getId());
    }

    @Test
    void crearVenta_TerrenoYaVendido_LanzaExcepcion() {
        terreno.setEstado(EstadoTerreno.VENDIDO);
        CreateVentaRequest request = new CreateVentaRequest();
        request.setTerrenoId(200L);
        request.setClienteId(300L);

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(200L, tenantId)).thenReturn(Optional.of(terreno));
        when(clienteRepository.findByIdAndTenantIdAndDeletedFalse(300L, tenantId)).thenReturn(Optional.of(Cliente.builder().id(300L).build()));

        BusinessException exception = assertThrows(BusinessException.class, () -> ventaService.crearVenta(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("ya ha sido vendido"));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void crearVenta_Exitoso() {
        CreateVentaRequest request = new CreateVentaRequest();
        request.setTerrenoId(200L);
        request.setClienteId(300L);

        Proyecto proyecto = Proyecto.builder().id(100L).tenantId(tenantId).deleted(false).build();
        Cliente cliente = Cliente.builder().id(300L).nombre("Carlos").apellido("Ruiz").build();

        when(terrenoRepository.findByIdAndTenantIdAndDeletedFalse(200L, tenantId)).thenReturn(Optional.of(terreno));
        when(clienteRepository.findByIdAndTenantIdAndDeletedFalse(300L, tenantId)).thenReturn(Optional.of(cliente));
        when(ventaMapper.toEntity(request)).thenReturn(venta);
        when(ventaRepository.save(any())).thenReturn(venta);
        when(ventaMapper.toResponse(venta)).thenReturn(ventaResponse);
        when(proyectoRepository.findById(100L)).thenReturn(Optional.of(proyecto));

        VentaResponse resultado = ventaService.crearVenta(request);

        assertNotNull(resultado);
        assertEquals(EstadoTerreno.VENDIDO, terreno.getEstado());
        verify(terrenoRepository, times(1)).save(terreno);
        verify(ventaRepository, times(1)).save(any());
    }

    @Test
    void eliminarVenta_NoCancelada_LanzaExcepcion() {
        venta.setEstado(EstadoVenta.PENDIENTE);
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(venta));

        BusinessException exception = assertThrows(BusinessException.class, () -> ventaService.eliminarVenta(50L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void eliminarVenta_Cancelada_Exitoso() {
        venta.setEstado(EstadoVenta.CANCELADA);
        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(venta));

        ventaService.eliminarVenta(50L);

        assertTrue(venta.getDeleted());
        verify(ventaRepository, times(1)).save(venta);
    }
}
