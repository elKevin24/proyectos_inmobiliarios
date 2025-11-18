package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.cotizacion.CotizacionResponse;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CreateCotizacionRequest;
import com.inmobiliaria.terrenos.domain.entity.Cotizacion;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.repository.CotizacionRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.CotizacionMapper;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return cotizacionMapper.toResponseList(cotizacionRepository.findByTenantIdAndDeletedFalse(tenantId));
    }

    @Transactional(readOnly = true)
    public List<CotizacionResponse> listarCotizacionesVigentes() {
        Long tenantId = getTenantId();
        log.debug("Listando cotizaciones vigentes para tenant: {}", tenantId);
        return cotizacionMapper.toResponseList(
                cotizacionRepository.findCotizacionesVigentes(tenantId, LocalDate.now()));
    }

    @Transactional(readOnly = true)
    public List<CotizacionResponse> buscarPorCliente(String nombreCliente) {
        Long tenantId = getTenantId();
        log.debug("Buscando cotizaciones del cliente '{}' para tenant: {}", nombreCliente, tenantId);
        return cotizacionMapper.toResponseList(
                cotizacionRepository.findByClienteNombre(tenantId, nombreCliente));
    }

    @Transactional(readOnly = true)
    public CotizacionResponse obtenerCotizacion(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo cotización {} para tenant: {}", id, tenantId);

        Cotizacion cotizacion = cotizacionRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));

        return cotizacionMapper.toResponse(cotizacion);
    }

    @Transactional
    public CotizacionResponse crearCotizacion(CreateCotizacionRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando cotización para terreno {} - Cliente: {}", request.getTerrenoId(), request.getClienteNombre());

        // Validar que el terreno existe y está disponible
        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(request.getTerrenoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + request.getTerrenoId()));

        Cotizacion cotizacion = cotizacionMapper.toEntity(request);
        cotizacion.setTenantId(tenantId);

        Cotizacion cotizacionGuardada = cotizacionRepository.save(cotizacion);
        log.info("Cotización creada con id: {}", cotizacionGuardada.getId());

        return cotizacionMapper.toResponse(cotizacionGuardada);
    }

    @Transactional
    public void eliminarCotizacion(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando cotización {} para tenant: {}", id, tenantId);

        Cotizacion cotizacion = cotizacionRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada con id: " + id));

        cotizacion.setDeleted(true);
        cotizacionRepository.save(cotizacion);
        log.info("Cotización {} eliminada exitosamente", id);
    }
}
