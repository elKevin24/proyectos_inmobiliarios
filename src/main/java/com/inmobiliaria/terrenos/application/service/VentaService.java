package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.entity.Venta;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.domain.repository.VentaRepository;
import com.inmobiliaria.terrenos.infrastructure.security.SecurityUtils;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.VentaMapper;
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
 * Servicio de gestión de ventas
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepository;
    private final TerrenoRepository terrenoRepository;
    private final ApartadoRepository apartadoRepository;
    private final ProyectoRepository proyectoRepository;
    private final VentaMapper ventaMapper;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarVentas() {
        Long tenantId = getTenantId();
        log.debug("Listando ventas para tenant: {}", tenantId);
        return ventaMapper.toResponseList(ventaRepository.findByTenantIdAndDeletedFalse(tenantId));
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listarVentasPorEstado(EstadoVenta estado) {
        Long tenantId = getTenantId();
        log.debug("Listando ventas con estado {} para tenant: {}", estado, tenantId);
        return ventaMapper.toResponseList(ventaRepository.findByTenantIdAndEstadoAndDeletedFalse(tenantId, estado));
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerVenta(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo venta {} para tenant: {}", id, tenantId);

        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        return ventaMapper.toResponse(venta);
    }

    @Transactional
    public VentaResponse crearVenta(CreateVentaRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando venta para terreno {} - Comprador: {}", request.getTerrenoId(), request.getCompradorNombre());

        // Validar que el terreno existe
        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(request.getTerrenoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + request.getTerrenoId()));

        // Validar estado del terreno
        if (terreno.getEstado() == EstadoTerreno.VENDIDO) {
            throw new BusinessException("El terreno ya ha sido vendido", HttpStatus.CONFLICT);
        }

        Venta venta = ventaMapper.toEntity(request);
        venta.setTenantId(tenantId);
        venta.setFechaVenta(LocalDate.now());
        venta.setEstado(EstadoVenta.PENDIENTE);

        // Obtener usuario actual desde el contexto de seguridad
        Long usuarioId = SecurityUtils.getCurrentUserId().orElse(null);
        venta.setUsuarioId(usuarioId);

        // Si viene de un apartado, marcar el apartado como convertido
        if (request.getApartadoId() != null) {
            Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(request.getApartadoId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + request.getApartadoId()));

            if (apartado.getEstado() != EstadoApartado.VIGENTE) {
                throw new BusinessException("El apartado debe estar vigente para convertirlo en venta", HttpStatus.CONFLICT);
            }

            apartado.setEstado(EstadoApartado.CONVERTIDO);
            apartadoRepository.save(apartado);
        }

        // Cambiar estado del terreno a VENDIDO
        terreno.setEstado(EstadoTerreno.VENDIDO);
        terrenoRepository.save(terreno);

        // Actualizar contadores del proyecto
        actualizarContadoresProyecto(terreno.getProyectoId());

        Venta ventaGuardada = ventaRepository.save(venta);
        log.info("Venta creada con id: {}", ventaGuardada.getId());

        return ventaMapper.toResponse(ventaGuardada);
    }

    @Transactional
    public VentaResponse cambiarEstado(Long id, EstadoVenta nuevoEstado) {
        Long tenantId = getTenantId();
        log.info("Cambiando estado de venta {} a {} para tenant: {}", id, nuevoEstado, tenantId);

        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        venta.setEstado(nuevoEstado);
        Venta ventaActualizada = ventaRepository.save(venta);

        log.info("Estado de venta {} cambiado a {}", id, nuevoEstado);
        return ventaMapper.toResponse(ventaActualizada);
    }

    @Transactional
    public void eliminarVenta(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando venta {} para tenant: {}", id, tenantId);

        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        // Solo se pueden eliminar ventas canceladas
        if (venta.getEstado() != EstadoVenta.CANCELADO) {
            throw new BusinessException("Solo se pueden eliminar ventas canceladas", HttpStatus.CONFLICT);
        }

        venta.setDeleted(true);
        ventaRepository.save(venta);
        log.info("Venta {} eliminada exitosamente", id);
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
