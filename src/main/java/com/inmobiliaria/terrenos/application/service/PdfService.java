package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.pdf.ContratoVentaPdfData;
import com.inmobiliaria.terrenos.application.dto.pdf.EstadoCuentaPdfData;
import com.inmobiliaria.terrenos.application.port.in.DocumentoPdfPort;
import com.inmobiliaria.terrenos.application.port.out.ExecutionContextPort;
import com.inmobiliaria.terrenos.application.port.out.GeneradorPdfDocumentoPort;
import com.inmobiliaria.terrenos.domain.entity.*;
import com.inmobiliaria.terrenos.domain.repository.*;
import com.inmobiliaria.terrenos.infrastructure.pdf.GeneradorPdfDocumentoAdapter;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la orquestación y generación de documentos PDF.
 * Cumple con SRP (orquestación y validación de datos del dominio) y DIP (delega renderizado a {@link GeneradorPdfDocumentoPort}).
 * Implementa {@link DocumentoPdfPort}.
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 2.0.0
 */
@Service
@Slf4j
public class PdfService implements DocumentoPdfPort {

    private final VentaRepository ventaRepository;
    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final AmortizacionRepository amortizacionRepository;
    private final TenantRepository tenantRepository;
    private final GeneradorPdfDocumentoPort generadorPdfDocumentoPort;
    private final ExecutionContextPort executionContextPort;

    @Autowired
    public PdfService(
            VentaRepository ventaRepository,
            TerrenoRepository terrenoRepository,
            ProyectoRepository proyectoRepository,
            PlanPagoRepository planPagoRepository,
            AmortizacionRepository amortizacionRepository,
            TenantRepository tenantRepository,
            @Autowired(required = false) GeneradorPdfDocumentoPort generadorPdfDocumentoPort,
            @Autowired(required = false) ExecutionContextPort executionContextPort) {
        this.ventaRepository = ventaRepository;
        this.terrenoRepository = terrenoRepository;
        this.proyectoRepository = proyectoRepository;
        this.planPagoRepository = planPagoRepository;
        this.amortizacionRepository = amortizacionRepository;
        this.tenantRepository = tenantRepository;
        this.generadorPdfDocumentoPort = generadorPdfDocumentoPort != null
                ? generadorPdfDocumentoPort
                : new GeneradorPdfDocumentoAdapter();
        this.executionContextPort = executionContextPort;
    }

    public PdfService(
            VentaRepository ventaRepository,
            TerrenoRepository terrenoRepository,
            ProyectoRepository proyectoRepository,
            PlanPagoRepository planPagoRepository,
            AmortizacionRepository amortizacionRepository,
            TenantRepository tenantRepository) {
        this(ventaRepository, terrenoRepository, proyectoRepository, planPagoRepository, amortizacionRepository, tenantRepository, null, null);
    }

    private Long resolveTenantId() {
        if (executionContextPort != null) {
            return executionContextPort.requireTenantId();
        }
        return TenantContext.getTenantId();
    }

    /**
     * Genera el PDF del Contrato de Compraventa de un Terreno.
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] generarContratoVentaPdf(Long ventaId) {
        Long tenantId = resolveTenantId();
        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(ventaId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + ventaId));

        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(venta.getTerrenoId(), tenantId)
                .or(() -> terrenoRepository.findById(venta.getTerrenoId())
                        .filter(t -> t.getTenantId() == null || t.getTenantId().equals(tenantId)))
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con ID: " + venta.getTerrenoId()));

        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(terreno.getProyectoId(), tenantId)
                .or(() -> proyectoRepository.findById(terreno.getProyectoId())
                        .filter(p -> p.getTenantId() == null || p.getTenantId().equals(tenantId)))
                .orElse(null);

        Tenant tenant = tenantId != null ? tenantRepository.findById(tenantId).orElse(null) : null;
        String nombreEmpresa = tenant != null ? tenant.getNombre() : "Inmobiliaria SaaS";

        ContratoVentaPdfData data = ContratoVentaPdfData.builder()
                .nombreEmpresa(nombreEmpresa)
                .folioVenta(venta.getFolio())
                .fechaVenta(venta.getFechaVenta())
                .compradorNombre(venta.getCompradorNombre())
                .compradorRfc(venta.getCompradorRfc())
                .compradorCurp(venta.getCompradorCurp())
                .compradorTelefono(venta.getCompradorTelefono())
                .compradorEmail(venta.getCompradorEmail())
                .compradorDireccion(venta.getCompradorDireccion())
                .proyectoNombre(proyecto != null ? proyecto.getNombre() : "N/A")
                .numeroLote(terreno.getNumeroLote())
                .manzana(terreno.getManzana())
                .areaTerreno(terreno.getArea())
                .precioTotal(venta.getPrecioTotal())
                .montoApartadoAcreditado(venta.getMontoApartadoAcreditado())
                .montoFinal(venta.getMontoFinal())
                .formaPago(venta.getFormaPago())
                .estadoVenta(venta.getEstado() != null ? venta.getEstado().toString() : "N/A")
                .build();

        return generadorPdfDocumentoPort.generarContratoVenta(data);
    }

    /**
     * Genera el PDF del Estado de Cuenta y Tabla de Amortización de un Plan de Pago.
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] generarEstadoCuentaPdf(Long planPagoId) {
        Long tenantId = resolveTenantId();
        PlanPago planPago = planPagoRepository.findByIdAndTenantIdAndDeletedFalse(planPagoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de Pago no encontrado con ID: " + planPagoId));

        List<Amortizacion> amortizaciones = amortizacionRepository.findByPlanPagoId(tenantId, planPagoId);
        Tenant tenant = tenantId != null ? tenantRepository.findById(tenantId).orElse(null) : null;
        String nombreEmpresa = tenant != null ? tenant.getNombre() : "Inmobiliaria SaaS";

        List<EstadoCuentaPdfData.CuotaItem> cuotas = amortizaciones != null
                ? amortizaciones.stream()
                .map(a -> EstadoCuentaPdfData.CuotaItem.builder()
                        .numeroCuota(a.getNumeroCuota())
                        .fechaVencimiento(a.getFechaVencimiento())
                        .capital(a.getCapital())
                        .interes(a.getInteres())
                        .montoCuota(a.getMontoCuota())
                        .montoPagado(a.getMontoPagado())
                        .estado(a.getEstado())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        EstadoCuentaPdfData data = EstadoCuentaPdfData.builder()
                .nombreEmpresa(nombreEmpresa)
                .planPagoId(planPago.getId())
                .ventaId(planPago.getVentaId())
                .montoFinanciado(planPago.getMontoFinanciado())
                .plazoMeses(planPago.getPlazoMeses())
                .numeroPagos(planPago.getNumeroPagos())
                .frecuenciaPago(planPago.getFrecuenciaPago())
                .tasaInteresAnual(planPago.getTasaInteresAnual())
                .montoTotal(planPago.getMontoTotal())
                .cuotas(cuotas)
                .build();

        return generadorPdfDocumentoPort.generarEstadoCuenta(data);
    }
}
