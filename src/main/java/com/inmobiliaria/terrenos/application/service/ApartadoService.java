package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.apartado.ApartadoResponse;
import com.inmobiliaria.terrenos.application.dto.apartado.CreateApartadoRequest;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ApartadoMapper;
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
 * Servicio de gestión de apartados
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApartadoService {

    private final ApartadoRepository apartadoRepository;
    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;
    private final ApartadoMapper apartadoMapper;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public List<ApartadoResponse> listarApartados() {
        Long tenantId = getTenantId();
        log.debug("Listando apartados para tenant: {}", tenantId);
        return apartadoMapper.toResponseList(apartadoRepository.findByTenantIdAndDeletedFalse(tenantId));
    }

    @Transactional(readOnly = true)
    public List<ApartadoResponse> listarApartadosVigentes() {
        Long tenantId = getTenantId();
        log.debug("Listando apartados vigentes para tenant: {}", tenantId);
        return apartadoMapper.toResponseList(
                apartadoRepository.findApartadosVigentes(tenantId, LocalDate.now()));
    }

    @Transactional(readOnly = true)
    public List<ApartadoResponse> listarApartadosVencidos() {
        Long tenantId = getTenantId();
        log.debug("Listando apartados vencidos para tenant: {}", tenantId);
        return apartadoMapper.toResponseList(
                apartadoRepository.findApartadosVencidos(tenantId, LocalDate.now()));
    }

    @Transactional(readOnly = true)
    public ApartadoResponse obtenerApartado(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo apartado {} para tenant: {}", id, tenantId);

        Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + id));

        return apartadoMapper.toResponse(apartado);
    }

    @Transactional
    public ApartadoResponse crearApartado(CreateApartadoRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando apartado para terreno {} - Cliente: {}", request.getTerrenoId(), request.getClienteNombre());

        // Validar que el terreno existe y está disponible
        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(request.getTerrenoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + request.getTerrenoId()));

        if (terreno.getEstado() != EstadoTerreno.DISPONIBLE) {
            throw new BusinessException("El terreno no está disponible para apartar. Estado actual: " + terreno.getEstado(),
                    HttpStatus.CONFLICT);
        }

        // Validar monto de apartado
        if (request.getMontoApartado().compareTo(request.getPrecioTotal()) > 0) {
            throw new BusinessException("El monto de apartado no puede ser mayor al precio total", HttpStatus.BAD_REQUEST);
        }

        Apartado apartado = apartadoMapper.toEntity(request);
        apartado.setTenantId(tenantId);
        apartado.setFechaApartado(LocalDate.now());
        apartado.setFechaVencimiento(LocalDate.now().plusDays(request.getDuracionDias()));
        apartado.setEstado(EstadoApartado.VIGENTE);

        // Cambiar estado del terreno a APARTADO
        terreno.setEstado(EstadoTerreno.APARTADO);
        terrenoRepository.save(terreno);

        // Actualizar contadores del proyecto
        actualizarContadoresProyecto(terreno.getProyectoId());

        Apartado apartadoGuardado = apartadoRepository.save(apartado);
        log.info("Apartado creado con id: {}", apartadoGuardado.getId());

        return apartadoMapper.toResponse(apartadoGuardado);
    }

    @Transactional
    public ApartadoResponse cancelarApartado(Long id, String motivo) {
        Long tenantId = getTenantId();
        log.info("Cancelando apartado {} para tenant: {}", id, tenantId);

        Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + id));

        if (apartado.getEstado() != EstadoApartado.VIGENTE) {
            throw new BusinessException("Solo se pueden cancelar apartados vigentes. Estado actual: " + apartado.getEstado(),
                    HttpStatus.CONFLICT);
        }

        apartado.setEstado(EstadoApartado.CANCELADO);
        if (motivo != null) {
            apartado.setObservaciones(apartado.getObservaciones() != null ?
                    apartado.getObservaciones() + "\nMotivo cancelación: " + motivo : "Motivo cancelación: " + motivo);
        }

        // Liberar el terreno
        Terreno terreno = terrenoRepository.findById(apartado.getTerrenoId()).orElse(null);
        if (terreno != null && terreno.getEstado() == EstadoTerreno.APARTADO) {
            terreno.setEstado(EstadoTerreno.DISPONIBLE);
            terrenoRepository.save(terreno);

            // Actualizar contadores del proyecto
            actualizarContadoresProyecto(terreno.getProyectoId());
        }

        Apartado apartadoActualizado = apartadoRepository.save(apartado);
        log.info("Apartado {} cancelado exitosamente", id);

        return apartadoMapper.toResponse(apartadoActualizado);
    }

    @Transactional
    public void eliminarApartado(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando apartado {} para tenant: {}", id, tenantId);

        Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + id));

        // Solo se pueden eliminar apartados cancelados o vencidos
        if (apartado.getEstado() == EstadoApartado.VIGENTE) {
            throw new BusinessException("No se puede eliminar un apartado vigente. Primero debe cancelarlo.",
                    HttpStatus.CONFLICT);
        }

        apartado.setDeleted(true);
        apartadoRepository.save(apartado);
        log.info("Apartado {} eliminado exitosamente", id);
    }

    private void actualizarContadoresProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null || proyecto.getDeleted()) {
            return;
        }

        Long tenantId = proyecto.getTenantId();
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
        log.debug("Contadores del proyecto {} actualizados", proyectoId);
    }
}
