package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.fase.CreateFaseRequest;
import com.inmobiliaria.terrenos.application.dto.fase.FaseResponse;
import com.inmobiliaria.terrenos.application.dto.fase.UpdateFaseRequest;
import com.inmobiliaria.terrenos.domain.entity.Fase;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.FaseRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.FaseMapper;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de gestión de fases de proyectos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FaseService {

    private final FaseRepository faseRepository;
    private final ProyectoRepository proyectoRepository;
    private final TerrenoRepository terrenoRepository;
    private final FaseMapper faseMapper;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public List<FaseResponse> listarFases() {
        Long tenantId = getTenantId();
        log.debug("Listando fases para tenant: {}", tenantId);
        return faseMapper.toResponseList(faseRepository.findByTenantIdAndDeletedFalse(tenantId));
    }

    @Transactional(readOnly = true)
    public List<FaseResponse> listarFasesPorProyecto(Long proyectoId) {
        Long tenantId = getTenantId();
        log.debug("Listando fases del proyecto {} para tenant: {}", proyectoId, tenantId);

        proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + proyectoId));

        return faseMapper.toResponseList(
                faseRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroFaseAsc(tenantId, proyectoId));
    }

    @Transactional(readOnly = true)
    public List<FaseResponse> listarFasesActivas(Long proyectoId) {
        Long tenantId = getTenantId();
        log.debug("Listando fases activas del proyecto {} para tenant: {}", proyectoId, tenantId);
        return faseMapper.toResponseList(
                faseRepository.findFasesActivasPorProyecto(tenantId, proyectoId));
    }

    @Transactional(readOnly = true)
    public FaseResponse obtenerFase(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo fase {} para tenant: {}", id, tenantId);

        Fase fase = faseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fase no encontrada con id: " + id));

        return faseMapper.toResponse(fase);
    }

    @Transactional
    public FaseResponse crearFase(CreateFaseRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando fase '{}' para tenant: {}", request.getNombre(), tenantId);

        // Validar que el proyecto existe
        proyectoRepository.findByIdAndTenantIdAndDeletedFalse(request.getProyectoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + request.getProyectoId()));

        // Validar nombre único
        if (faseRepository.existsByTenantIdAndProyectoIdAndNombreIgnoreCaseAndDeletedFalse(
                tenantId, request.getProyectoId(), request.getNombre())) {
            throw new BusinessException("Ya existe una fase con el nombre: " + request.getNombre()
                    + " en este proyecto", HttpStatus.CONFLICT);
        }

        // Validar número de fase único si se proporcionó
        if (request.getNumeroFase() != null &&
                faseRepository.existsByTenantIdAndProyectoIdAndNumeroFaseAndDeletedFalse(
                        tenantId, request.getProyectoId(), request.getNumeroFase())) {
            throw new BusinessException("Ya existe una fase con el número: " + request.getNumeroFase()
                    + " en este proyecto", HttpStatus.CONFLICT);
        }

        Fase fase = faseMapper.toEntity(request);
        fase.setTenantId(tenantId);

        // Valores por defecto
        if (fase.getActiva() == null) {
            fase.setActiva(true);
        }
        if (fase.getTotalTerrenos() == null) {
            fase.setTotalTerrenos(0);
        }
        if (fase.getTerrenosDisponibles() == null) {
            fase.setTerrenosDisponibles(0);
        }
        if (fase.getTerrenosApartados() == null) {
            fase.setTerrenosApartados(0);
        }
        if (fase.getTerrenosVendidos() == null) {
            fase.setTerrenosVendidos(0);
        }

        Fase faseGuardada = faseRepository.save(fase);
        log.info("Fase creada con id: {}", faseGuardada.getId());

        return faseMapper.toResponse(faseGuardada);
    }

    @Transactional
    public FaseResponse actualizarFase(Long id, UpdateFaseRequest request) {
        Long tenantId = getTenantId();
        log.info("Actualizando fase {} para tenant: {}", id, tenantId);

        Fase fase = faseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fase no encontrada con id: " + id));

        // Validar nombre único si se actualiza
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            if (faseRepository.existsByNombreExcludingId(tenantId, fase.getProyectoId(), request.getNombre(), id)) {
                throw new BusinessException("Ya existe otra fase con el nombre: " + request.getNombre()
                        + " en este proyecto", HttpStatus.CONFLICT);
            }
        }

        // Validar número único si se actualiza
        if (request.getNumeroFase() != null) {
            if (faseRepository.existsByNumeroFaseExcludingId(
                    tenantId, fase.getProyectoId(), request.getNumeroFase(), id)) {
                throw new BusinessException("Ya existe otra fase con el número: " + request.getNumeroFase()
                        + " en este proyecto", HttpStatus.CONFLICT);
            }
        }

        faseMapper.updateEntityFromRequest(request, fase);
        Fase faseActualizada = faseRepository.save(fase);
        log.info("Fase {} actualizada exitosamente", id);

        return faseMapper.toResponse(faseActualizada);
    }

    @Transactional
    public void eliminarFase(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando fase {} para tenant: {}", id, tenantId);

        Fase fase = faseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fase no encontrada con id: " + id));

        // Validar que no tenga terrenos vendidos o apartados
        if (fase.getTerrenosVendidos() != null && fase.getTerrenosVendidos() > 0) {
            throw new BusinessException("No se puede eliminar la fase porque tiene terrenos vendidos", HttpStatus.CONFLICT);
        }
        if (fase.getTerrenosApartados() != null && fase.getTerrenosApartados() > 0) {
            throw new BusinessException("No se puede eliminar la fase porque tiene terrenos apartados", HttpStatus.CONFLICT);
        }

        fase.setDeleted(true);
        faseRepository.save(fase);
        log.info("Fase {} eliminada exitosamente", id);
    }

    @Transactional
    public void actualizarContadoresFase(Long faseId) {
        Fase fase = faseRepository.findById(faseId).orElse(null);
        if (fase == null || fase.getDeleted()) {
            return;
        }

        Long tenantId = fase.getTenantId();
        long totalTerrenos = terrenoRepository.countByTenantIdAndFaseIdAndDeletedFalse(tenantId, faseId);
        long disponibles = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, fase.getProyectoId(), EstadoTerreno.DISPONIBLE);
        long apartados = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, fase.getProyectoId(), EstadoTerreno.APARTADO);
        long vendidos = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, fase.getProyectoId(), EstadoTerreno.VENDIDO);

        fase.setTotalTerrenos((int) totalTerrenos);
        fase.setTerrenosDisponibles((int) disponibles);
        fase.setTerrenosApartados((int) apartados);
        fase.setTerrenosVendidos((int) vendidos);

        faseRepository.save(fase);
        log.debug("Contadores de fase {} actualizados", faseId);
    }
}
