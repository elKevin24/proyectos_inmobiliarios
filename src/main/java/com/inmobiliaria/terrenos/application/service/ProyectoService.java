package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.ProyectoResponse;
import com.inmobiliaria.terrenos.application.dto.proyecto.UpdateProyectoRequest;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ProyectoMapper;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new BusinessException("Ya existe un proyecto con el nombre: " + request.getNombre(), HttpStatus.CONFLICT);
        }

        // Convertir DTO a entidad
        Proyecto proyecto = proyectoMapper.toEntity(request);
        proyecto.setTenantId(tenantId);

        // Establecer valores por defecto si no se proporcionaron
        if (proyecto.getEstado() == null) {
            proyecto.setEstado(EstadoProyecto.PLANIFICACION);
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
                throw new BusinessException("Ya existe otro proyecto con el nombre: " + request.getNombre(), HttpStatus.CONFLICT);
            }
        }

        // Actualizar campos no nulos
        proyectoMapper.updateEntityFromRequest(request, proyecto);

        // Guardar cambios
        Proyecto proyectoActualizado = proyectoRepository.save(proyecto);
        log.info("Proyecto {} actualizado exitosamente", id);

        return proyectoMapper.toResponse(proyectoActualizado);
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
            throw new BusinessException("No se puede eliminar el proyecto porque tiene terrenos vendidos", HttpStatus.CONFLICT);
        }

        if (proyecto.getTerrenosApartados() != null && proyecto.getTerrenosApartados() > 0) {
            throw new BusinessException("No se puede eliminar el proyecto porque tiene terrenos apartados", HttpStatus.CONFLICT);
        }

        // Soft delete
        proyecto.setDeleted(true);
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
        validarTransicionEstado(proyecto.getEstado(), nuevoEstado, proyecto);

        proyecto.setEstado(nuevoEstado);
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
            throw new BusinessException("No se puede poner en venta un proyecto sin terrenos", HttpStatus.BAD_REQUEST);
        }

        // No se puede finalizar si hay terrenos apartados
        if (nuevoEstado == EstadoProyecto.FINALIZADO &&
                proyecto.getTerrenosApartados() != null && proyecto.getTerrenosApartados() > 0) {
            throw new BusinessException("No se puede finalizar el proyecto con terrenos apartados", HttpStatus.BAD_REQUEST);
        }

        // No se puede cancelar si tiene terrenos vendidos
        if (nuevoEstado == EstadoProyecto.CANCELADO &&
                proyecto.getTerrenosVendidos() != null && proyecto.getTerrenosVendidos() > 0) {
            throw new BusinessException("No se puede cancelar el proyecto con terrenos vendidos", HttpStatus.BAD_REQUEST);
        }
    }
}
