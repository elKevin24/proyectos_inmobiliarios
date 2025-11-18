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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de gestión de terrenos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TerrenoService {

    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;
    private final TerrenoMapper terrenoMapper;

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
     * Lista todos los terrenos del tenant
     */
    @Transactional(readOnly = true)
    public List<TerrenoResponse> listarTerrenos() {
        Long tenantId = getTenantId();
        log.debug("Listando terrenos para tenant: {}", tenantId);

        List<Terreno> terrenos = terrenoRepository.findByTenantIdAndDeletedFalse(tenantId);
        return terrenoMapper.toResponseList(terrenos);
    }

    /**
     * Lista terrenos por proyecto
     */
    @Transactional(readOnly = true)
    public List<TerrenoResponse> listarTerrenosPorProyecto(Long proyectoId) {
        Long tenantId = getTenantId();
        log.debug("Listando terrenos del proyecto {} para tenant: {}", proyectoId, tenantId);

        // Verificar que el proyecto existe y pertenece al tenant
        proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + proyectoId));

        List<Terreno> terrenos = terrenoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(tenantId, proyectoId);
        return terrenoMapper.toResponseList(terrenos);
    }

    /**
     * Lista terrenos por estado
     */
    @Transactional(readOnly = true)
    public List<TerrenoResponse> listarTerrenosPorEstado(EstadoTerreno estado) {
        Long tenantId = getTenantId();
        log.debug("Listando terrenos con estado {} para tenant: {}", estado, tenantId);

        List<Terreno> terrenos = terrenoRepository.findByTenantIdAndEstadoAndDeletedFalse(tenantId, estado);
        return terrenoMapper.toResponseList(terrenos);
    }

    /**
     * Lista terrenos disponibles de un proyecto
     */
    @Transactional(readOnly = true)
    public List<TerrenoResponse> listarTerrenosDisponibles(Long proyectoId) {
        Long tenantId = getTenantId();
        log.debug("Listando terrenos disponibles del proyecto {} para tenant: {}", proyectoId, tenantId);

        List<Terreno> terrenos = terrenoRepository.findTerrenosDisponiblesPorProyecto(tenantId, proyectoId);
        return terrenoMapper.toResponseList(terrenos);
    }

    /**
     * Obtiene un terreno por ID
     */
    @Transactional(readOnly = true)
    public TerrenoResponse obtenerTerreno(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo terreno {} para tenant: {}", id, tenantId);

        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + id));

        return terrenoMapper.toResponse(terreno);
    }

    /**
     * Crea un nuevo terreno
     */
    @Transactional
    public TerrenoResponse crearTerreno(CreateTerrenoRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando terreno '{}' para tenant: {}", request.getNumeroLote(), tenantId);

        // Validar que el proyecto existe y pertenece al tenant
        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(request.getProyectoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + request.getProyectoId()));

        // Validar que no exista un terreno con el mismo número de lote en el proyecto
        if (terrenoRepository.existsByTenantIdAndProyectoIdAndNumeroLoteIgnoreCaseAndDeletedFalse(
                tenantId, request.getProyectoId(), request.getNumeroLote())) {
            throw new BusinessException("Ya existe un terreno con el número de lote: " + request.getNumeroLote()
                    + " en este proyecto", HttpStatus.CONFLICT);
        }

        // Convertir DTO a entidad
        Terreno terreno = terrenoMapper.toEntity(request);
        terreno.setTenantId(tenantId);

        // Establecer estado por defecto
        if (terreno.getEstado() == null) {
            terreno.setEstado(EstadoTerreno.DISPONIBLE);
        }

        // Calcular precio final si no se proporcionó
        if (terreno.getPrecioFinal() == null) {
            terreno.setPrecioFinal(calcularPrecioFinal(terreno));
        }

        // Guardar terreno
        Terreno terrenoGuardado = terrenoRepository.save(terreno);

        // Actualizar contadores del proyecto
        actualizarContadoresProyecto(proyecto);

        log.info("Terreno creado con id: {}", terrenoGuardado.getId());
        return terrenoMapper.toResponse(terrenoGuardado);
    }

    /**
     * Actualiza un terreno existente
     */
    @Transactional
    public TerrenoResponse actualizarTerreno(Long id, UpdateTerrenoRequest request) {
        Long tenantId = getTenantId();
        log.info("Actualizando terreno {} para tenant: {}", id, tenantId);

        // Buscar terreno existente
        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + id));

        // Validar número de lote único si se está actualizando
        if (request.getNumeroLote() != null && !request.getNumeroLote().isBlank()) {
            if (terrenoRepository.existsByNumeroLoteExcludingId(
                    tenantId, terreno.getProyectoId(), request.getNumeroLote(), id)) {
                throw new BusinessException("Ya existe otro terreno con el número de lote: " + request.getNumeroLote()
                        + " en este proyecto", HttpStatus.CONFLICT);
            }
        }

        // Actualizar campos no nulos
        terrenoMapper.updateEntityFromRequest(request, terreno);

        // Recalcular precio final si cambió algún componente del precio
        if (request.getPrecioBase() != null || request.getPrecioAjuste() != null
                || request.getPrecioMultiplicador() != null) {
            terreno.setPrecioFinal(calcularPrecioFinal(terreno));
        }

        // Guardar cambios
        Terreno terrenoActualizado = terrenoRepository.save(terreno);
        log.info("Terreno {} actualizado exitosamente", id);

        return terrenoMapper.toResponse(terrenoActualizado);
    }

    /**
     * Elimina un terreno (soft delete)
     */
    @Transactional
    public void eliminarTerreno(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando terreno {} para tenant: {}", id, tenantId);

        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + id));

        // Validar que no esté vendido
        if (terreno.getEstado() == EstadoTerreno.VENDIDO) {
            throw new BusinessException("No se puede eliminar un terreno vendido", HttpStatus.CONFLICT);
        }

        // Validar que no esté en venta o apartado
        if (terreno.getEstado() == EstadoTerreno.EN_VENTA || terreno.getEstado() == EstadoTerreno.APARTADO) {
            throw new BusinessException("No se puede eliminar un terreno que está en venta o apartado", HttpStatus.CONFLICT);
        }

        // Soft delete
        terreno.setDeleted(true);
        terrenoRepository.save(terreno);

        // Actualizar contadores del proyecto
        Proyecto proyecto = proyectoRepository.findById(terreno.getProyectoId()).orElse(null);
        if (proyecto != null) {
            actualizarContadoresProyecto(proyecto);
        }

        log.info("Terreno {} eliminado exitosamente", id);
    }

    /**
     * Cambia el estado de un terreno
     */
    @Transactional
    public TerrenoResponse cambiarEstado(Long id, EstadoTerreno nuevoEstado) {
        Long tenantId = getTenantId();
        log.info("Cambiando estado del terreno {} a {} para tenant: {}", id, nuevoEstado, tenantId);

        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + id));

        EstadoTerreno estadoAnterior = terreno.getEstado();

        // Validar transición de estado
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        terreno.setEstado(nuevoEstado);
        Terreno terrenoActualizado = terrenoRepository.save(terreno);

        // Actualizar contadores del proyecto
        Proyecto proyecto = proyectoRepository.findById(terreno.getProyectoId()).orElse(null);
        if (proyecto != null) {
            actualizarContadoresProyecto(proyecto);
        }

        log.info("Estado del terreno {} cambiado de {} a {}", id, estadoAnterior, nuevoEstado);
        return terrenoMapper.toResponse(terrenoActualizado);
    }

    /**
     * Calcula el precio final del terreno
     */
    private BigDecimal calcularPrecioFinal(Terreno terreno) {
        BigDecimal precioBase = terreno.getPrecioBase() != null ? terreno.getPrecioBase() : BigDecimal.ZERO;
        BigDecimal ajuste = terreno.getPrecioAjuste() != null ? terreno.getPrecioAjuste() : BigDecimal.ZERO;
        BigDecimal multiplicador = terreno.getPrecioMultiplicador() != null ? terreno.getPrecioMultiplicador() : BigDecimal.ONE;

        return precioBase.add(ajuste).multiply(multiplicador);
    }

    /**
     * Valida si la transición de estado es válida
     */
    private void validarTransicionEstado(EstadoTerreno estadoActual, EstadoTerreno nuevoEstado) {
        // No se puede cambiar de vendido a otro estado
        if (estadoActual == EstadoTerreno.VENDIDO && nuevoEstado != EstadoTerreno.VENDIDO) {
            throw new BusinessException("No se puede cambiar el estado de un terreno vendido", HttpStatus.BAD_REQUEST);
        }

        // No se puede vender directamente sin apartar
        if (estadoActual == EstadoTerreno.DISPONIBLE && nuevoEstado == EstadoTerreno.VENDIDO) {
            throw new BusinessException("Un terreno disponible no puede venderse directamente. Primero debe apartarse.", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Actualiza los contadores de terrenos del proyecto
     */
    private void actualizarContadoresProyecto(Proyecto proyecto) {
        Long tenantId = proyecto.getTenantId();
        Long proyectoId = proyecto.getId();

        long totalTerrenos = terrenoRepository.countByTenantIdAndProyectoIdAndDeletedFalse(tenantId, proyectoId);
        long disponibles = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.DISPONIBLE);
        long apartados = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.APARTADO);
        long vendidos = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.VENDIDO);

        proyecto.setTotalTerrenos((int) totalTerrenos);
        proyecto.setTerrenosDisponibles((int) disponibles);
        proyecto.setTerrenosApartados((int) apartados);
        proyecto.setTerrenosVendidos((int) vendidos);

        proyectoRepository.save(proyecto);
        log.debug("Contadores del proyecto {} actualizados: total={}, disponibles={}, apartados={}, vendidos={}",
                proyectoId, totalTerrenos, disponibles, apartados, vendidos);
    }
}
