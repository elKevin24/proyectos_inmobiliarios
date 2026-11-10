package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.cotizacion.CotizacionResponse;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CreateCotizacionRequest;
import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.entity.Cotizacion;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.repository.ClienteRepository;
import com.inmobiliaria.terrenos.domain.repository.CotizacionRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.CotizacionMapper;
import com.inmobiliaria.terrenos.shared.dto.PagedResponse;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de gestión de cotizaciones
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final TerrenoRepository terrenoRepository;
    private final CotizacionMapper cotizacionMapper;

    @Autowired(required = false)
    private ProyectoRepository proyectoRepository;

    @Autowired(required = false)
    private ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public PagedResponse<CotizacionResponse> listarCotizacionesPaginado(Pageable pageable) {
        Long tenantId = getTenantId();
        Page<Cotizacion> cotizacionesPage = cotizacionRepository.findByTenantIdAndDeletedFalse(tenantId, pageable);
        List<CotizacionResponse> content = enriquecerLista(cotizacionMapper.toResponseList(cotizacionesPage.getContent()));
        return new PagedResponse<>(
                content,
                cotizacionesPage.getNumber(),
                cotizacionesPage.getSize(),
                cotizacionesPage.getTotalElements(),
                cotizacionesPage.getTotalPages(),
                cotizacionesPage.isFirst(),
                cotizacionesPage.isLast(),
                cotizacionesPage.isEmpty()
        );
    }

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public List<CotizacionResponse> listarCotizaciones() {
        Long tenantId = getTenantId();
        log.debug("Listando cotizaciones para tenant: {}", tenantId);
        return enriquecerLista(cotizacionMapper.toResponseList(cotizacionRepository.findByTenantIdAndDeletedFalse(tenantId)));
    }

    @Transactional(readOnly = true)
    public List<CotizacionResponse> listarCotizacionesVigentes() {
        Long tenantId = getTenantId();
        log.debug("Listando cotizaciones vigentes para tenant: {}", tenantId);
        return enriquecerLista(cotizacionMapper.toResponseList(
                cotizacionRepository.findCotizacionesVigentes(tenantId, LocalDate.now())));
    }

    @Transactional(readOnly = true)
    public List<CotizacionResponse> buscarPorCliente(String nombreCliente) {
        Long tenantId = getTenantId();
        log.debug("Buscando cotizaciones del cliente '{}' para tenant: {}", nombreCliente, tenantId);
        return enriquecerLista(cotizacionMapper.toResponseList(
                cotizacionRepository.findByClienteNombre(tenantId, nombreCliente)));
    }

    @Transactional(readOnly = true)
    public CotizacionResponse obtenerCotizacion(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo cotización {} para tenant: {}", id, tenantId);

        Cotizacion cotizacion = cotizacionRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));

        return enriquecer(cotizacionMapper.toResponse(cotizacion));
    }

    @Transactional
    public CotizacionResponse crearCotizacion(CreateCotizacionRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando cotización para terreno {} - Cliente: {}", request.getTerrenoId(), request.getClienteNombre());

        // Validar que el terreno existe y está disponible
        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(request.getTerrenoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + request.getTerrenoId()));

        // Resolver datos del cliente si se envió clienteId y falta clienteNombre
        if ((request.getClienteNombre() == null || request.getClienteNombre().isBlank())
                && request.getClienteId() != null && clienteRepository != null) {
            Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(request.getClienteId(), tenantId)
                    .orElse(null);
            if (cliente != null) {
                request.setClienteNombre(cliente.getNombreCompleto());
                if (request.getClienteEmail() == null) {
                    request.setClienteEmail(cliente.getEmail());
                }
                if (request.getClienteTelefono() == null) {
                    request.setClienteTelefono(cliente.getTelefono());
                }
            }
        }

        Cotizacion cotizacion = cotizacionMapper.toEntity(request);
        cotizacion.setTenantId(tenantId);
        if (cotizacion.getClienteId() == null && request.getClienteId() != null) {
            cotizacion.setClienteId(request.getClienteId());
        }

        Cotizacion cotizacionGuardada = cotizacionRepository.save(cotizacion);
        log.info("Cotización creada con id: {}", cotizacionGuardada.getId());

        CotizacionResponse response = cotizacionMapper.toResponse(cotizacionGuardada);
        if (response != null) {
            if (response.getTerrenoNumeroLote() == null) {
                response.setTerrenoNumeroLote(terreno.getNumeroLote());
            }
            if (response.getTerrenoManzana() == null) {
                response.setTerrenoManzana(terreno.getManzana());
            }
            if (response.getProyectoId() == null) {
                response.setProyectoId(terreno.getProyectoId());
            }
        }
        return enriquecer(response);
    }

    private CotizacionResponse enriquecer(CotizacionResponse response) {
        if (response == null) {
            return null;
        }
        if (response.getTerrenoId() != null &&
                (response.getTerrenoNumeroLote() == null || response.getProyectoId() == null)) {
            if (terrenoRepository != null) {
                terrenoRepository.findById(response.getTerrenoId()).ifPresent(terreno -> {
                    if (response.getTerrenoNumeroLote() == null) {
                        response.setTerrenoNumeroLote(terreno.getNumeroLote());
                    }
                    if (response.getTerrenoManzana() == null) {
                        response.setTerrenoManzana(terreno.getManzana());
                    }
                    if (response.getProyectoId() == null) {
                        response.setProyectoId(terreno.getProyectoId());
                    }
                    if (response.getProyectoNombre() == null && terreno.getProyecto() != null) {
                        response.setProyectoNombre(terreno.getProyecto().getNombre());
                    }
                });
            }
        }
        if (response.getProyectoId() != null && response.getProyectoNombre() == null && proyectoRepository != null) {
            proyectoRepository.findById(response.getProyectoId())
                    .ifPresent(proyecto -> response.setProyectoNombre(proyecto.getNombre()));
        }
        return response;
    }

    private List<CotizacionResponse> enriquecerLista(List<CotizacionResponse> responses) {
        if (responses != null) {
            responses.forEach(this::enriquecer);
        }
        return responses;
    }

    @Transactional
    public void eliminarCotizacion(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando cotización {} para tenant: {}", id, tenantId);

        Cotizacion cotizacion = cotizacionRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));

        cotizacion.softDelete();
        cotizacionRepository.save(cotizacion);
        log.info("Cotización {} eliminada exitosamente", id);
    }
}
