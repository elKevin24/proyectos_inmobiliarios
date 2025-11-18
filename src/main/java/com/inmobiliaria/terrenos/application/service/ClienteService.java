package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.apartado.ApartadoResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.ClienteHistorialResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.ClienteResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.CreateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.UpdateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CotizacionResponse;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.entity.Cotizacion;
import com.inmobiliaria.terrenos.domain.entity.Venta;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Servicio de gestión de clientes
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CotizacionRepository cotizacionRepository;
    private final ApartadoRepository apartadoRepository;
    private final VentaRepository ventaRepository;
    private final ClienteMapper clienteMapper;
    private final CotizacionMapper cotizacionMapper;
    private final ApartadoMapper apartadoMapper;
    private final VentaMapper ventaMapper;

    /**
     * Obtiene el tenant_id del contexto actual
     */
    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    /**
     * Lista todos los clientes del tenant
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarClientes() {
        Long tenantId = getTenantId();
        log.debug("Listando clientes para tenant: {}", tenantId);

        List<Cliente> clientes = clienteRepository.findByTenantIdAndDeletedFalse(tenantId);
        return clienteMapper.toResponseList(clientes);
    }

    /**
     * Lista clientes por estado
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarClientesPorEstado(EstadoCliente estado) {
        Long tenantId = getTenantId();
        log.debug("Listando clientes con estado {} para tenant: {}", estado, tenantId);

        List<Cliente> clientes = clienteRepository.findByTenantIdAndEstadoClienteAndDeletedFalse(tenantId, estado);
        return clienteMapper.toResponseList(clientes);
    }

    /**
     * Busca clientes por nombre (búsqueda parcial)
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarClientesPorNombre(String nombre) {
        Long tenantId = getTenantId();
        log.debug("Buscando clientes con nombre '{}' para tenant: {}", nombre, tenantId);

        List<Cliente> clientes = clienteRepository.buscarPorNombre(tenantId, nombre);
        return clienteMapper.toResponseList(clientes);
    }

    /**
     * Busca clientes activos (no inactivos)
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarClientesActivos() {
        Long tenantId = getTenantId();
        log.debug("Listando clientes activos para tenant: {}", tenantId);

        List<Cliente> clientes = clienteRepository.findClientesActivos(tenantId);
        return clienteMapper.toResponseList(clientes);
    }

    /**
     * Obtiene un cliente por ID
     */
    @Transactional(readOnly = true)
    public ClienteResponse obtenerCliente(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo cliente {} para tenant: {}", id, tenantId);

        Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        ClienteResponse response = clienteMapper.toResponse(cliente);

        // Agregar estadísticas
        response.setTotalCotizaciones(cotizacionRepository.countByClienteId(id).intValue());
        response.setTotalApartados(apartadoRepository.countByClienteId(id).intValue());
        response.setTotalVentas(ventaRepository.countByClienteId(id).intValue());

        return response;
    }

    /**
     * Crea un nuevo cliente
     */
    @Transactional
    public ClienteResponse crearCliente(CreateClienteRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando nuevo cliente para tenant: {}", tenantId);

        // Validar email único
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            clienteRepository.findByTenantIdAndEmailAndDeletedFalse(tenantId, request.getEmail())
                    .ifPresent(c -> {
                        throw new BusinessException("Ya existe un cliente con el email: " + request.getEmail());
                    });
        }

        // Validar RFC único
        if (request.getRfc() != null && !request.getRfc().isBlank()) {
            clienteRepository.findByTenantIdAndRfcAndDeletedFalse(tenantId, request.getRfc())
                    .ifPresent(c -> {
                        throw new BusinessException("Ya existe un cliente con el RFC: " + request.getRfc());
                    });
        }

        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setTenantId(tenantId);

        Cliente savedCliente = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con id: {}", savedCliente.getId());

        return clienteMapper.toResponse(savedCliente);
    }

    /**
     * Actualiza un cliente existente
     */
    @Transactional
    public ClienteResponse actualizarCliente(Long id, UpdateClienteRequest request) {
        Long tenantId = getTenantId();
        log.info("Actualizando cliente {} para tenant: {}", id, tenantId);

        Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        // Validar email único (si está cambiando)
        if (request.getEmail() != null && !request.getEmail().equals(cliente.getEmail())) {
            if (clienteRepository.existeEmailEnOtroCliente(tenantId, request.getEmail(), id)) {
                throw new BusinessException("Ya existe otro cliente con el email: " + request.getEmail());
            }
        }

        clienteMapper.updateEntityFromRequest(request, cliente);

        Cliente updatedCliente = clienteRepository.save(cliente);
        log.info("Cliente {} actualizado exitosamente", id);

        return clienteMapper.toResponse(updatedCliente);
    }

    /**
     * Elimina un cliente (soft delete)
     */
    @Transactional
    public void eliminarCliente(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando cliente {} para tenant: {}", id, tenantId);

        Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        // Verificar que no tenga transacciones activas
        long cotizacionesVigentes = cotizacionRepository.countByClienteIdAndFechaVigenciaAfter(id, java.time.LocalDate.now());
        if (cotizacionesVigentes > 0) {
            throw new BusinessException("No se puede eliminar el cliente porque tiene cotizaciones vigentes");
        }

        long apartadosVigentes = apartadoRepository.countByClienteIdAndEstadoVigente(id);
        if (apartadosVigentes > 0) {
            throw new BusinessException("No se puede eliminar el cliente porque tiene apartados vigentes");
        }

        cliente.setDeleted(true);
        clienteRepository.save(cliente);

        log.info("Cliente {} eliminado exitosamente", id);
    }

    /**
     * Obtiene el historial completo de transacciones de un cliente
     */
    @Transactional(readOnly = true)
    public ClienteHistorialResponse obtenerHistorialCliente(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo historial del cliente {} para tenant: {}", id, tenantId);

        Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        // Obtener transacciones
        List<Cotizacion> cotizaciones = cotizacionRepository.findByClienteIdOrderByCreatedAtDesc(id);
        List<Apartado> apartados = apartadoRepository.findByClienteIdOrderByCreatedAtDesc(id);
        List<Venta> ventas = ventaRepository.findByClienteIdOrderByCreatedAtDesc(id);

        // Mapear a DTOs
        List<CotizacionResponse> cotizacionesResponse = cotizacionMapper.toResponseList(cotizaciones);
        List<ApartadoResponse> apartadosResponse = apartadoMapper.toResponseList(apartados);
        List<VentaResponse> ventasResponse = ventaMapper.toResponseList(ventas);

        // Calcular montos
        BigDecimal montoTotalCotizaciones = cotizaciones.stream()
                .map(Cotizacion::getPrecioFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoTotalApartados = apartados.stream()
                .map(Apartado::getMontoApartado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoTotalVentas = ventas.stream()
                .map(Venta::getMontoFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcular tasa de conversión
        BigDecimal tasaConversion = BigDecimal.ZERO;
        if (!cotizaciones.isEmpty()) {
            tasaConversion = BigDecimal.valueOf(ventas.size() * 100.0 / cotizaciones.size())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return ClienteHistorialResponse.builder()
                .cliente(clienteMapper.toResponse(cliente))
                .cotizaciones(cotizacionesResponse)
                .apartados(apartadosResponse)
                .ventas(ventasResponse)
                .totalCotizaciones(cotizaciones.size())
                .totalApartados(apartados.size())
                .totalVentas(ventas.size())
                .montoTotalCotizaciones(montoTotalCotizaciones)
                .montoTotalApartados(montoTotalApartados)
                .montoTotalVentas(montoTotalVentas)
                .tasaConversion(tasaConversion)
                .build();
    }
}
