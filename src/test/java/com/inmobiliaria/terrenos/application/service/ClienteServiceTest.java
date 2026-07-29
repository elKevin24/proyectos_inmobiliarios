package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.cliente.ClienteHistorialResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.ClienteResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.CreateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.UpdateClienteRequest;
import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ClienteRepository;
import com.inmobiliaria.terrenos.domain.repository.CotizacionRepository;
import com.inmobiliaria.terrenos.domain.repository.VentaRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ApartadoMapper;
import com.inmobiliaria.terrenos.interfaces.mapper.ClienteMapper;
import com.inmobiliaria.terrenos.interfaces.mapper.CotizacionMapper;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ApartadoRepository apartadoRepository;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private CotizacionMapper cotizacionMapper;

    @Mock
    private ApartadoMapper apartadoMapper;

    @Mock
    private VentaMapper ventaMapper;

    @InjectMocks
    private ClienteService clienteService;

    private final Long tenantId = 1L;
    private Cliente cliente;
    private ClienteResponse clienteResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        cliente = Cliente.builder()
                .id(10L)
                .tenantId(tenantId)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@test.com")
                .telefono("4421234567")
                .estadoCliente(EstadoCliente.PROSPECTO)
                .build();

        clienteResponse = new ClienteResponse();
        clienteResponse.setId(10L);
        clienteResponse.setNombre("Juan");
        clienteResponse.setApellido("Pérez");
        clienteResponse.setEmail("juan@test.com");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listarClientes_Exitoso() {
        when(clienteRepository.findByTenantIdAndDeletedFalse(tenantId)).thenReturn(List.of(cliente));
        when(clienteMapper.toResponseList(any())).thenReturn(List.of(clienteResponse));

        List<ClienteResponse> resultado = clienteService.listarClientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(clienteRepository, times(1)).findByTenantIdAndDeletedFalse(tenantId);
    }

    @Test
    void obtenerCliente_Existente_Exitoso() {
        when(clienteRepository.findByIdAndTenantIdAndDeletedFalse(10L, tenantId)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(clienteResponse);
        when(cotizacionRepository.countByClienteId(10L)).thenReturn(2L);
        when(apartadoRepository.countByClienteId(10L)).thenReturn(1L);
        when(ventaRepository.countByClienteId(10L)).thenReturn(1L);

        ClienteResponse resultado = clienteService.obtenerCliente(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(2, resultado.getTotalCotizaciones());
        assertEquals(1, resultado.getTotalApartados());
        assertEquals(1, resultado.getTotalVentas());
    }

    @Test
    void obtenerCliente_Inexistente_LanzaExcepcion() {
        when(clienteRepository.findByIdAndTenantIdAndDeletedFalse(99L, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.obtenerCliente(99L));
    }

    @Test
    void crearCliente_EmailDuplicado_LanzaExcepcion() {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setEmail("juan@test.com");

        when(clienteRepository.findByTenantIdAndEmailAndDeletedFalse(tenantId, "juan@test.com"))
                .thenReturn(Optional.of(cliente));

        BusinessException exception = assertThrows(BusinessException.class, () -> clienteService.crearCliente(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearCliente_Exitoso() {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setNombre("Juan");
        request.setEmail("nuevo@test.com");

        when(clienteRepository.findByTenantIdAndEmailAndDeletedFalse(tenantId, "nuevo@test.com"))
                .thenReturn(Optional.empty());
        when(clienteMapper.toEntity(request)).thenReturn(cliente);
        when(clienteRepository.save(any())).thenReturn(cliente);
        when(clienteMapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse resultado = clienteService.crearCliente(request);

        assertNotNull(resultado);
        verify(clienteRepository, times(1)).save(any());
    }

    @Test
    void eliminarCliente_ConCotizacionesVigentes_LanzaExcepcion() {
        when(clienteRepository.findByIdAndTenantIdAndDeletedFalse(10L, tenantId)).thenReturn(Optional.of(cliente));
        when(cotizacionRepository.countByClienteIdAndFechaVigenciaAfter(eq(10L), any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> clienteService.eliminarCliente(10L));
        assertTrue(exception.getMessage().contains("cotizaciones vigentes"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void eliminarCliente_Exitoso() {
        when(clienteRepository.findByIdAndTenantIdAndDeletedFalse(10L, tenantId)).thenReturn(Optional.of(cliente));
        when(cotizacionRepository.countByClienteIdAndFechaVigenciaAfter(eq(10L), any())).thenReturn(0L);
        when(apartadoRepository.countByClienteIdAndEstadoVigente(10L)).thenReturn(0L);

        clienteService.eliminarCliente(10L);

        assertTrue(cliente.getDeleted());
        verify(clienteRepository, times(1)).save(cliente);
    }
}
