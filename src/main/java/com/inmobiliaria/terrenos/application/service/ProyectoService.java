package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.ProyectoResponse;
import com.inmobiliaria.terrenos.application.dto.proyecto.UpdateProyectoRequest;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import com.inmobiliaria.terrenos.domain.enums.TipoPrecio;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ProyectoMapper;
import com.inmobiliaria.terrenos.shared.dto.PagedResponse;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ProyectoException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de gestión de proyectos inmobiliarios
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final ProyectoMapper proyectoMapper;

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
     * Lista todos los proyectos del tenant actual
     */
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarProyectos() {
        Long tenantId = getTenantId();
        log.debug("Listando proyectos para tenant: {}", tenantId);

        List<Proyecto> proyectos = proyectoRepository.findByTenantIdAndDeletedFalse(tenantId);
        return proyectoMapper.toResponseList(proyectos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProyectoResponse> listarProyectosPaginado(EstadoProyecto estado, Boolean disponibles, Boolean activos, Pageable pageable) {
        Long tenantId = getTenantId();
        log.debug("Listando proyectos paginados para tenant: {} con estado: {}, disponibles: {}, activos: {}", tenantId, estado, disponibles, activos);

        Page<Proyecto> proyectosPage;

        if (Boolean.TRUE.equals(activos)) {
            proyectosPage = proyectoRepository.findProyectosActivos(tenantId, pageable);
        } else if (Boolean.TRUE.equals(disponibles)) {
            proyectosPage = proyectoRepository.findProyectosConTerrenosDisponibles(tenantId, pageable);
        } else if (estado != null) {
            proyectosPage = proyectoRepository.findByTenantIdAndEstadoAndDeletedFalse(tenantId, estado, pageable);
        } else {
            proyectosPage = proyectoRepository.findByTenantIdAndDeletedFalse(tenantId, pageable);
        }

        List<ProyectoResponse> content = proyectoMapper.toResponseList(proyectosPage.getContent());
        return new PagedResponse<>(
                content,
                proyectosPage.getNumber(),
                proyectosPage.getSize(),
                proyectosPage.getTotalElements(),
                proyectosPage.getTotalPages(),
                proyectosPage.isFirst(),
                proyectosPage.isLast(),
                proyectosPage.isEmpty()
        );
    }

    /**
     * Obtiene un proyecto por ID
     */
    @Transactional(readOnly = true)
    public ProyectoResponse obtenerProyecto(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo proyecto {} para tenant: {}", id, tenantId);

        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));

        return proyectoMapper.toResponse(proyecto);
    }

    /**
     * Crea un nuevo proyecto
     */
    @Transactional
    public ProyectoResponse crearProyecto(CreateProyectoRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando proyecto '{}' para tenant: {}", request.getNombre(), tenantId);

        // Validar que no exista un proyecto con el mismo nombre
        if (proyectoRepository.existsByTenantIdAndNombreIgnoreCaseAndDeletedFalse(tenantId, request.getNombre())) {
            throw ProyectoException.nombreDuplicado(request.getNombre());
        }

        // Convertir DTO a entidad
        Proyecto proyecto = proyectoMapper.toEntity(request);
        proyecto.setTenantId(tenantId);

        if (request.getEstadoProyecto() == null && request.getEstado() != null) {
            EstadoProyecto parsedEstado = parseEstadoProyecto(request.getEstado());
            if (parsedEstado != null) {
                proyecto.setEstadoProyecto(parsedEstado);
                String estadoGeo = request.getEstadoProvincia() != null && !request.getEstadoProvincia().isBlank()
                        ? request.getEstadoProvincia()
                        : (request.getUbicacion() != null && !request.getUbicacion().isBlank()
                                ? request.getUbicacion()
                                : "Estado");
                proyecto.setEstado(estadoGeo);
            }
        }

        String defaultUbicacion = (request.getUbicacion() != null && !request.getUbicacion().isBlank())
                ? request.getUbicacion()
                : "Sin especificar";

        if (proyecto.getDireccion() == null || proyecto.getDireccion().isBlank()) {
            proyecto.setDireccion(defaultUbicacion);
        }

        if (proyecto.getCiudad() == null || proyecto.getCiudad().isBlank()) {
            proyecto.setCiudad(defaultUbicacion);
        }

        if (proyecto.getEstado() == null || proyecto.getEstado().isBlank()) {
            proyecto.setEstado(request.getEstadoProvincia() != null && !request.getEstadoProvincia().isBlank()
                    ? request.getEstadoProvincia()
                    : defaultUbicacion);
        }

        if (proyecto.getTipoPrecio() == null) {
            proyecto.setTipoPrecio(defaultTipoPrecio());
        }

        if (proyecto.getPrecioBase() == null) {
            proyecto.setPrecioBase(BigDecimal.ZERO);
        }

        // Establecer valores por defecto si no se proporcionaron
        if (proyecto.getEstadoProyecto() == null) {
            proyecto.setEstadoProyecto(EstadoProyecto.PLANIFICACION);
        }

        if (proyecto.getTotalTerrenos() == null) {
            proyecto.setTotalTerrenos(0);
        }

        if (proyecto.getTerrenosDisponibles() == null) {
            proyecto.setTerrenosDisponibles(0);
        }

        if (proyecto.getTerrenosApartados() == null) {
            proyecto.setTerrenosApartados(0);
        }

        if (proyecto.getTerrenosVendidos() == null) {
            proyecto.setTerrenosVendidos(0);
        }

        // Guardar proyecto
        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);
        log.info("Proyecto creado con id: {}", proyectoGuardado.getId());

        return proyectoMapper.toResponse(proyectoGuardado);
    }

    /**
     * Actualiza un proyecto existente
     */
    @Transactional
    public ProyectoResponse actualizarProyecto(Long id, UpdateProyectoRequest request) {
        Long tenantId = getTenantId();
        log.info("Actualizando proyecto {} para tenant: {}", id, tenantId);

        // Buscar proyecto existente
        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));

        // Validar nombre único si se está actualizando
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            if (proyectoRepository.existsByNombreExcludingId(tenantId, request.getNombre(), id)) {
                throw ProyectoException.nombreDuplicado(request.getNombre());
            }
        }

        String estadoGeoPrevio = proyecto.getEstado();

        // Actualizar campos no nulos
        proyectoMapper.updateEntityFromRequest(request, proyecto);

        if (request.getEstadoProyecto() == null && request.getEstado() != null) {
            EstadoProyecto parsedEstado = parseEstadoProyecto(request.getEstado());
            if (parsedEstado != null) {
                proyecto.setEstadoProyecto(parsedEstado);
                String estadoGeo = request.getEstadoProvincia() != null && !request.getEstadoProvincia().isBlank()
                        ? request.getEstadoProvincia()
                        : (request.getUbicacion() != null && !request.getUbicacion().isBlank()
                                ? request.getUbicacion()
                                : (estadoGeoPrevio != null && !estadoGeoPrevio.isBlank() ? estadoGeoPrevio : "Estado"));
                proyecto.setEstado(estadoGeo);
            }
        }

        if (request.getUbicacion() != null && !request.getUbicacion().isBlank()
                && (request.getDireccion() == null || request.getDireccion().isBlank())) {
            proyecto.setDireccion(request.getUbicacion());
        }

        String defaultUbicacion = (request.getUbicacion() != null && !request.getUbicacion().isBlank())
                ? request.getUbicacion()
                : "Sin especificar";

        if (proyecto.getDireccion() == null || proyecto.getDireccion().isBlank()) {
            proyecto.setDireccion(defaultUbicacion);
        }

        if (proyecto.getCiudad() == null || proyecto.getCiudad().isBlank()) {
            proyecto.setCiudad(defaultUbicacion);
        }

        if (proyecto.getEstado() == null || proyecto.getEstado().isBlank()) {
            proyecto.setEstado(defaultUbicacion);
        }

        if (proyecto.getTipoPrecio() == null) {
            proyecto.setTipoPrecio(defaultTipoPrecio());
        }

        if (proyecto.getPrecioBase() == null) {
            proyecto.setPrecioBase(BigDecimal.ZERO);
        }

        // Guardar cambios
        Proyecto proyectoActualizado = proyectoRepository.save(proyecto);
        log.info("Proyecto {} actualizado exitosamente", id);

        return proyectoMapper.toResponse(proyectoActualizado);
    }

    private EstadoProyecto parseEstadoProyecto(String estadoStr) {
        if (estadoStr == null || estadoStr.isBlank()) {
            return null;
        }
        String normalized = estadoStr.trim().toUpperCase();
        for (EstadoProyecto ep : EstadoProyecto.values()) {
            if (ep.name().equalsIgnoreCase(normalized)) {
                return ep;
            }
        }
        return switch (normalized) {
            case "ACTIVO", "EN_VENTA" -> {
                for (EstadoProyecto ep : EstadoProyecto.values()) {
                    if (ep.name().equals("ACTIVO") || ep.name().equals("EN_VENTA")) {
                        yield ep;
                    }
                }
                yield EstadoProyecto.EN_VENTA;
            }
            case "EN_CONSTRUCCION" -> EstadoProyecto.PLANIFICACION;
            case "FINALIZADO", "VENDIDO", "AGOTADO" -> EstadoProyecto.AGOTADO;
            default -> null;
        };
    }

    private TipoPrecio defaultTipoPrecio() {
        for (TipoPrecio tp : TipoPrecio.values()) {
            if (tp.name().equals("POR_M2")) {
                return tp;
            }
        }
        return TipoPrecio.VARIABLE;
    }

    /**
     * Elimina un proyecto (soft delete)
     */
    @Transactional
    public void eliminarProyecto(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando proyecto {} para tenant: {}", id, tenantId);

        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));

        // Validar que no tenga terrenos vendidos o apartados
        if (proyecto.getTerrenosVendidos() != null && proyecto.getTerrenosVendidos() > 0) {
            throw ProyectoException.tieneTerrenosVendidos();
        }

        if (proyecto.getTerrenosApartados() != null && proyecto.getTerrenosApartados() > 0) {
            throw ProyectoException.tieneTerrenosApartados();
        }

        // Soft delete
        proyecto.softDelete();
        proyectoRepository.save(proyecto);

        log.info("Proyecto {} eliminado exitosamente", id);
    }

    /**
     * Lista proyectos por estado
     */
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarProyectosPorEstado(EstadoProyecto estado) {
        Long tenantId = getTenantId();
        log.debug("Listando proyectos con estado {} para tenant: {}", estado, tenantId);

        List<Proyecto> proyectos = proyectoRepository.findByTenantIdAndEstadoAndDeletedFalse(tenantId, estado);
        return proyectoMapper.toResponseList(proyectos);
    }

    /**
     * Lista proyectos con terrenos disponibles
     */
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarProyectosConTerrenosDisponibles() {
        Long tenantId = getTenantId();
        log.debug("Listando proyectos con terrenos disponibles para tenant: {}", tenantId);

        List<Proyecto> proyectos = proyectoRepository.findProyectosConTerrenosDisponibles(tenantId);
        return proyectoMapper.toResponseList(proyectos);
    }

    /**
     * Lista proyectos activos (en venta)
     */
    @Transactional(readOnly = true)
    public List<ProyectoResponse> listarProyectosActivos() {
        Long tenantId = getTenantId();
        log.debug("Listando proyectos activos para tenant: {}", tenantId);

        List<Proyecto> proyectos = proyectoRepository.findProyectosActivos(tenantId);
        return proyectoMapper.toResponseList(proyectos);
    }

    /**
     * Cambia el estado de un proyecto
     */
    @Transactional
    public ProyectoResponse cambiarEstado(Long id, EstadoProyecto nuevoEstado) {
        Long tenantId = getTenantId();
        log.info("Cambiando estado del proyecto {} a {} para tenant: {}", id, nuevoEstado, tenantId);

        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + id));

        // Validaciones de transición de estado
        validarTransicionEstado(proyecto.getEstadoProyecto(), nuevoEstado, proyecto);

        proyecto.setEstadoProyecto(nuevoEstado);
        Proyecto proyectoActualizado = proyectoRepository.save(proyecto);

        log.info("Estado del proyecto {} cambiado a {}", id, nuevoEstado);
        return proyectoMapper.toResponse(proyectoActualizado);
    }

    /**
     * Valida si la transición de estado es válida
     */
    private void validarTransicionEstado(EstadoProyecto estadoActual, EstadoProyecto nuevoEstado, Proyecto proyecto) {
        // No se puede poner en venta si no hay terrenos
        if (nuevoEstado == EstadoProyecto.EN_VENTA &&
                (proyecto.getTotalTerrenos() == null || proyecto.getTotalTerrenos() == 0)) {
            throw ProyectoException.sinTerrenos();
        }

        // No se puede finalizar si hay terrenos apartados
        if (nuevoEstado == EstadoProyecto.AGOTADO &&
                proyecto.getTerrenosApartados() != null && proyecto.getTerrenosApartados() > 0) {
            throw ProyectoException.tieneApartadosActivos();
        }

        // No se puede cancelar si tiene terrenos vendidos
        if (nuevoEstado == EstadoProyecto.CANCELADO &&
                proyecto.getTerrenosVendidos() != null && proyecto.getTerrenosVendidos() > 0) {
            throw new ProyectoException("No se puede cancelar el proyecto con terrenos vendidos");
        }
    }
}
