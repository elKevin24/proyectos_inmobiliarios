package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
import com.inmobiliaria.terrenos.application.dto.venta.UpdateVentaRequest;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Amortizacion;
import com.inmobiliaria.terrenos.domain.entity.PlanPago;
import com.inmobiliaria.terrenos.domain.entity.Pago;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.entity.Venta;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.AmortizacionRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.PlanPagoRepository;
import com.inmobiliaria.terrenos.domain.repository.PagoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.domain.repository.VentaRepository;
import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.repository.ClienteRepository;
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
    private final PlanPagoRepository planPagoRepository;
    private final AmortizacionRepository amortizacionRepository;
    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository;
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
        log.info("Creando venta para terreno {} - Cliente ID: {}", request.getTerrenoId(), request.getClienteId());

        // Validar que el terreno existe
        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(request.getTerrenoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + request.getTerrenoId()));

        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(request.getClienteId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + request.getClienteId()));

        // Validar estado del terreno
        if (terreno.getEstado() == EstadoTerreno.VENDIDO) {
            throw new BusinessException("El terreno ya ha sido vendido", HttpStatus.CONFLICT);
        }

        Venta venta = ventaMapper.toEntity(request);
        venta.setTenantId(tenantId);
        venta.setFechaVenta(LocalDate.now());
        venta.setEstado(EstadoVenta.PENDIENTE);
        venta.setPrecioTotal(request.getMontoTotal());
        venta.setMontoFinal(request.getMontoFinanciado());
        venta.setMontoApartadoAcreditado(request.getEnganche());
        venta.setFormaPago(request.getMetodoPago());

        // Mapear histórico del comprador
        venta.setCompradorNombre(cliente.getNombreCompleto());
        venta.setCompradorEmail(cliente.getEmail());
        venta.setCompradorTelefono(cliente.getTelefono());
        venta.setCompradorDireccion(cliente.getDireccion());
        venta.setCompradorRfc(cliente.getRfc());
        venta.setCompradorCurp(cliente.getCurp());
        
        // Generar folio automático
        String folio = "V-" + LocalDate.now().getYear() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        venta.setFolio(folio);

        // Obtener usuario actual desde el contexto de seguridad
        Long usuarioId = SecurityUtils.getCurrentUserId().orElse(null);
        venta.setUsuarioId(usuarioId);

        // Si viene de un apartado, marcar el apartado como convertido
        if (request.getApartadoId() != null) {
            Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(request.getApartadoId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + request.getApartadoId()));

            if (apartado.getEstado() != EstadoApartado.ACTIVO) {
                throw new BusinessException("El apartado debe estar vigente para convertirlo en venta", HttpStatus.CONFLICT);
            }

            apartado.setEstado(EstadoApartado.COMPLETADO);
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
    public VentaResponse actualizarVenta(Long id, UpdateVentaRequest request) {
        Long tenantId = getTenantId();
        log.info("Actualizando venta {} para tenant: {}", id, tenantId);

        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        if (venta.getEstado() == EstadoVenta.CANCELADA || venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("No se puede actualizar una venta cancelada o anulada", HttpStatus.CONFLICT);
        }

        if (request.getTerrenoId() != null && !request.getTerrenoId().equals(venta.getTerrenoId())) {
            throw new BusinessException("No se permite cambiar el terreno de una venta existente", HttpStatus.CONFLICT);
        }

        if (request.getApartadoId() != null && !request.getApartadoId().equals(venta.getApartadoId())) {
            throw new BusinessException("No se permite cambiar el apartado de una venta existente", HttpStatus.CONFLICT);
        }

        if (request.getCompradorNombre() != null) {
            venta.setCompradorNombre(request.getCompradorNombre());
        }
        if (request.getCompradorEmail() != null) {
            venta.setCompradorEmail(request.getCompradorEmail());
        }
        if (request.getCompradorTelefono() != null) {
            venta.setCompradorTelefono(request.getCompradorTelefono());
        }
        if (request.getCompradorDireccion() != null) {
            venta.setCompradorDireccion(request.getCompradorDireccion());
        }
        if (request.getCompradorRfc() != null) {
            venta.setCompradorRfc(request.getCompradorRfc());
        }
        if (request.getCompradorCurp() != null) {
            venta.setCompradorCurp(request.getCompradorCurp());
        }
        if (request.getPrecioTotal() != null) {
            venta.setPrecioTotal(request.getPrecioTotal());
        }
        if (request.getMontoApartadoAcreditado() != null) {
            venta.setMontoApartadoAcreditado(request.getMontoApartadoAcreditado());
        }
        if (request.getMontoFinal() != null) {
            venta.setMontoFinal(request.getMontoFinal());
        }
        if (request.getPorcentajeComision() != null) {
            venta.setPorcentajeComision(request.getPorcentajeComision());
        }
        if (request.getMontoComision() != null) {
            venta.setMontoComision(request.getMontoComision());
        }
        if (request.getFormaPago() != null) {
            venta.setFormaPago(request.getFormaPago());
        }
        if (request.getObservaciones() != null) {
            venta.setObservaciones(request.getObservaciones());
        }

        venta.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("SYSTEM"));

        Venta ventaActualizada = ventaRepository.save(venta);
        log.info("Venta {} actualizada exitosamente", id);

        return ventaMapper.toResponse(ventaActualizada);
    }

    @Transactional
    public VentaResponse cambiarEstado(Long id, EstadoVenta nuevoEstado) {
        Long tenantId = getTenantId();
        log.info("Cambiando estado de venta {} a {} para tenant: {}", id, nuevoEstado, tenantId);

        if (nuevoEstado == EstadoVenta.CANCELADA || nuevoEstado == EstadoVenta.ANULADA) {
            String motivo = "Cambio de estado a " + nuevoEstado;
            return cancelarVenta(id, motivo, nuevoEstado);
        }

        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        venta.setEstado(nuevoEstado);
        venta.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("SYSTEM"));
        Venta ventaActualizada = ventaRepository.save(venta);

        log.info("Estado de venta {} cambiado a {}", id, nuevoEstado);
        return ventaMapper.toResponse(ventaActualizada);
    }

    @Transactional
    public VentaResponse cancelarVenta(Long id, String motivo) {
        return cancelarVenta(id, motivo, EstadoVenta.CANCELADA);
    }

    private VentaResponse cancelarVenta(Long id, String motivo, EstadoVenta estadoFinal) {
        Long tenantId = getTenantId();
        log.info("Cancelando venta {} para tenant: {}", id, tenantId);

        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        if (venta.getEstado() == EstadoVenta.CANCELADA || venta.getEstado() == EstadoVenta.ANULADA) {
            if (motivo != null && !motivo.isBlank()) {
                venta.setObservaciones(agregarMotivoCancelacion(venta.getObservaciones(), motivo));
                venta.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("SYSTEM"));
                ventaRepository.save(venta);
            }
            return ventaMapper.toResponse(venta);
        }

        liberarEstructuraFinanciera(tenantId, venta.getId());

        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(venta.getTerrenoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + venta.getTerrenoId()));

        venta.setEstado(estadoFinal);
        venta.setObservaciones(agregarMotivoCancelacion(venta.getObservaciones(), motivo));
        venta.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("SYSTEM"));
        Venta ventaActualizada = ventaRepository.save(venta);

        if (terreno.getEstado() == EstadoTerreno.VENDIDO) {
            terreno.setEstado(EstadoTerreno.DISPONIBLE);
            terrenoRepository.save(terreno);
            actualizarContadoresProyecto(terreno.getProyectoId());
        }

        log.info("Venta {} cancelada exitosamente", id);
        return ventaMapper.toResponse(ventaActualizada);
    }

    @Transactional
    public void eliminarVenta(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando venta {} para tenant: {}", id, tenantId);

        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        // Solo se pueden eliminar ventas canceladas
        if (venta.getEstado() != EstadoVenta.CANCELADA) {
            throw new BusinessException("Solo se pueden eliminar ventas canceladas", HttpStatus.CONFLICT);
        }

        venta.setDeleted(true);
        ventaRepository.save(venta);
        log.info("Venta {} eliminada exitosamente", id);
    }

    private void liberarEstructuraFinanciera(Long tenantId, Long ventaId) {
        planPagoRepository.findByTenantIdAndVentaIdAndDeletedFalse(tenantId, ventaId).ifPresent(planPago -> {
            List<Pago> pagos = pagoRepository.findByPlanPagoId(tenantId, planPago.getId());
            for (Pago pago : pagos) {
                pago.setDeleted(true);
                pago.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("SYSTEM"));
                pagoRepository.save(pago);
            }

            List<Amortizacion> amortizaciones = amortizacionRepository.findByPlanPagoId(tenantId, planPago.getId());
            for (Amortizacion amortizacion : amortizaciones) {
                amortizacion.setDeleted(true);
                amortizacionRepository.save(amortizacion);
            }

            planPago.setDeleted(true);
            planPago.setUpdatedBy(SecurityUtils.getCurrentUsername().orElse("SYSTEM"));
            planPagoRepository.save(planPago);
        });
    }

    private String agregarMotivoCancelacion(String observaciones, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return observaciones;
        }

        String motivoCancelacion = "Motivo cancelación: " + motivo;
        if (observaciones == null || observaciones.isBlank()) {
            return motivoCancelacion;
        }
        return observaciones + "\n" + motivoCancelacion;
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
