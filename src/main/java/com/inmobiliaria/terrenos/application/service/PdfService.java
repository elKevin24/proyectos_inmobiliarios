package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.domain.entity.*;
import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import com.inmobiliaria.terrenos.domain.repository.*;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de arquitectura para la generación de documentos PDF (Contratos de Compraventa, Estados de Cuenta).
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 1.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final VentaRepository ventaRepository;
    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final AmortizacionRepository amortizacionRepository;
    private final TenantRepository tenantRepository;

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(41, 128, 185));
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera el PDF del Contrato de Compraventa de un Terreno
     */
    @Transactional(readOnly = true)
    public byte[] generarContratoVentaPdf(Long ventaId) {
        Long tenantId = TenantContext.getTenantId();
        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(ventaId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + ventaId));

        Terreno terreno = terrenoRepository.findById(venta.getTerrenoId())
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con ID: " + venta.getTerrenoId()));

        Proyecto proyecto = proyectoRepository.findById(terreno.getProyectoId())
                .orElse(null);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElse(null);

        String nombreEmpresa = tenant != null ? tenant.getNombre() : "Inmobiliaria SaaS";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Encabezado
            Paragraph title = new Paragraph("CONTRATO PRIVADO DE COMPRAVENTA", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph(nombreEmpresa.toUpperCase(), FONT_SUBTITLE);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Folio de Venta: " + venta.getFolio() + " | Fecha: " +
                    (venta.getFechaVenta() != null ? venta.getFechaVenta().format(dateFormatter) : "N/A"), FONT_BOLD));

            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------"));

            // Sección 1: Datos del Comprador
            document.add(new Paragraph("1. DATOS DEL COMPRADOR", FONT_SECTION));
            PdfPTable tableComprador = new PdfPTable(2);
            tableComprador.setWidthPercentage(100);
            tableComprador.setSpacingBefore(5f);
            tableComprador.setSpacingAfter(10f);

            addCell(tableComprador, "Nombre Completo:", FONT_BOLD);
            addCell(tableComprador, venta.getCompradorNombre() != null ? venta.getCompradorNombre() : "N/A", FONT_NORMAL);
            addCell(tableComprador, "RFC:", FONT_BOLD);
            addCell(tableComprador, venta.getCompradorRfc() != null ? venta.getCompradorRfc() : "N/A", FONT_NORMAL);
            addCell(tableComprador, "CURP:", FONT_BOLD);
            addCell(tableComprador, venta.getCompradorCurp() != null ? venta.getCompradorCurp() : "N/A", FONT_NORMAL);
            addCell(tableComprador, "Teléfono / Email:", FONT_BOLD);
            addCell(tableComprador, (venta.getCompradorTelefono() != null ? venta.getCompradorTelefono() : "") + " | " +
                    (venta.getCompradorEmail() != null ? venta.getCompradorEmail() : ""), FONT_NORMAL);
            addCell(tableComprador, "Dirección:", FONT_BOLD);
            addCell(tableComprador, venta.getCompradorDireccion() != null ? venta.getCompradorDireccion() : "N/A", FONT_NORMAL);
            document.add(tableComprador);

            // Sección 2: Datos del Terreno y Proyecto
            document.add(new Paragraph("2. ESPECIFICACIONES DEL TERRENO", FONT_SECTION));
            PdfPTable tableTerreno = new PdfPTable(2);
            tableTerreno.setWidthPercentage(100);
            tableTerreno.setSpacingBefore(5f);
            tableTerreno.setSpacingAfter(10f);

            addCell(tableTerreno, "Proyecto / Desarrollo:", FONT_BOLD);
            addCell(tableTerreno, proyecto != null ? proyecto.getNombre() : "N/A", FONT_NORMAL);
            addCell(tableTerreno, "Número de Lote:", FONT_BOLD);
            addCell(tableTerreno, terreno.getNumeroLote(), FONT_NORMAL);
            addCell(tableTerreno, "Manzana:", FONT_BOLD);
            addCell(tableTerreno, terreno.getManzana() != null ? terreno.getManzana() : "N/A", FONT_NORMAL);
            addCell(tableTerreno, "Área Total (m²):", FONT_BOLD);
            addCell(tableTerreno, terreno.getArea() != null ? terreno.getArea().toString() + " m²" : "N/A", FONT_NORMAL);
            document.add(tableTerreno);

            // Sección 3: Condiciones Financieras
            document.add(new Paragraph("3. CONDICIONES ECONÓMICAS Y FORMA DE PAGO", FONT_SECTION));
            PdfPTable tableFinanzas = new PdfPTable(2);
            tableFinanzas.setWidthPercentage(100);
            tableFinanzas.setSpacingBefore(5f);
            tableFinanzas.setSpacingAfter(15f);

            addCell(tableFinanzas, "Precio Pactado de Venta:", FONT_BOLD);
            addCell(tableFinanzas, formatMoney(venta.getPrecioTotal()), FONT_NORMAL);

            addCell(tableFinanzas, "Anticipo / Apartado Acreditado:", FONT_BOLD);
            addCell(tableFinanzas, formatMoney(venta.getMontoApartadoAcreditado()), FONT_NORMAL);

            addCell(tableFinanzas, "Monto Saldo a Pagar:", FONT_BOLD);
            addCell(tableFinanzas, formatMoney(venta.getMontoFinal()), FONT_BOLD);

            addCell(tableFinanzas, "Forma de Pago:", FONT_BOLD);
            addCell(tableFinanzas, venta.getFormaPago() != null ? venta.getFormaPago() : "De Contado", FONT_NORMAL);

            addCell(tableFinanzas, "Estado de la Venta:", FONT_BOLD);
            addCell(tableFinanzas, venta.getEstado().toString(), FONT_NORMAL);
            document.add(tableFinanzas);

            // Firmas
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable tableFirmas = new PdfPTable(2);
            tableFirmas.setWidthPercentage(100);

            PdfPCell cellFirma1 = new PdfPCell(new Paragraph("_____________________________________\nEL VENDEDOR\n" + nombreEmpresa, FONT_BOLD));
            cellFirma1.setBorder(Rectangle.NO_BORDER);
            cellFirma1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cellFirma2 = new PdfPCell(new Paragraph("_____________________________________\nEL COMPRADOR\n" + venta.getCompradorNombre(), FONT_BOLD));
            cellFirma2.setBorder(Rectangle.NO_BORDER);
            cellFirma2.setHorizontalAlignment(Element.ALIGN_CENTER);

            tableFirmas.addCell(cellFirma1);
            tableFirmas.addCell(cellFirma2);
            document.add(tableFirmas);

            // Pie de página
            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Documento generado automáticamente por el Sistema SaaS Inmobiliario | " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), FONT_SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF de contrato de venta ID {}: {}", ventaId, e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de contrato de venta: " + e.getMessage());
        }
    }

    /**
     * Genera el PDF del Estado de Cuenta y Tabla de Amortización de un Plan de Pago
     */
    @Transactional(readOnly = true)
    public byte[] generarEstadoCuentaPdf(Long planPagoId) {
        Long tenantId = TenantContext.getTenantId();
        PlanPago planPago = planPagoRepository.findByIdAndTenantIdAndDeletedFalse(planPagoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de Pago no encontrado con ID: " + planPagoId));

        List<Amortizacion> amortizaciones = amortizacionRepository.findByPlanPagoId(tenantId, planPagoId);

        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        String nombreEmpresa = tenant != null ? tenant.getNombre() : "Inmobiliaria SaaS";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("ESTADO DE CUENTA Y TABLA DE AMORTIZACIÓN", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph(nombreEmpresa.toUpperCase(), FONT_SUBTITLE);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Plan de Pago ID: #" + planPago.getId() + " | Venta ID: #" + planPago.getVentaId(), FONT_BOLD));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------"));

            // Resumen de Plan
            PdfPTable tableResumen = new PdfPTable(2);
            tableResumen.setWidthPercentage(100);
            tableResumen.setSpacingBefore(5f);
            tableResumen.setSpacingAfter(10f);

            addCell(tableResumen, "Monto Financiamiento:", FONT_BOLD);
            addCell(tableResumen, formatMoney(planPago.getMontoFinanciado()), FONT_NORMAL);
            addCell(tableResumen, "Plazo (Meses) / Frecuencia:", FONT_BOLD);
            addCell(tableResumen, (planPago.getPlazoMeses() != null ? planPago.getPlazoMeses() : planPago.getNumeroPagos()) +
                    " cuotas (" + (planPago.getFrecuenciaPago() != null ? planPago.getFrecuenciaPago() : "MENSUAL") + ")", FONT_NORMAL);
            addCell(tableResumen, "Tasa Interés Anual:", FONT_BOLD);
            addCell(tableResumen, (planPago.getTasaInteresAnual() != null ? planPago.getTasaInteresAnual().toString() + "%" : "0%"), FONT_NORMAL);
            addCell(tableResumen, "Total Financiamiento:", FONT_BOLD);
            addCell(tableResumen, formatMoney(planPago.getMontoTotal()), FONT_BOLD);
            document.add(tableResumen);

            // Tabla de Amortización
            document.add(new Paragraph("DETALLE DE CUOTAS", FONT_SECTION));

            PdfPTable tableCuotas = new PdfPTable(7);
            tableCuotas.setWidthPercentage(100);
            tableCuotas.setWidths(new float[]{1f, 2f, 2f, 2f, 2f, 2f, 2f});
            tableCuotas.setSpacingBefore(5f);
            tableCuotas.setSpacingAfter(15f);

            String[] headers = {"#", "Vencimiento", "Capital", "Interés", "Monto Cuota", "Pagado", "Estado"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FONT_TABLE_HEADER));
                cell.setBackgroundColor(new Color(41, 128, 185));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tableCuotas.addCell(cell);
            }

            BigDecimal totalPagado = BigDecimal.ZERO;
            BigDecimal totalPendiente = BigDecimal.ZERO;

            for (Amortizacion a : amortizaciones) {
                tableCuotas.addCell(createCenterCell(String.valueOf(a.getNumeroCuota())));
                tableCuotas.addCell(createCenterCell(a.getFechaVencimiento() != null ? a.getFechaVencimiento().format(dateFormatter) : ""));
                tableCuotas.addCell(createRightCell(formatMoney(a.getCapital())));
                tableCuotas.addCell(createRightCell(formatMoney(a.getInteres())));
                tableCuotas.addCell(createRightCell(formatMoney(a.getMontoCuota())));
                tableCuotas.addCell(createRightCell(formatMoney(a.getMontoPagado())));
                tableCuotas.addCell(createCenterCell(a.getEstado() != null ? a.getEstado().name() : "PENDIENTE"));

                if (a.getMontoPagado() != null) {
                    totalPagado = totalPagado.add(a.getMontoPagado());
                }
                if (a.getEstado() == EstadoAmortizacion.PENDIENTE) {
                    totalPendiente = totalPendiente.add(a.getMontoCuota().subtract(a.getMontoPagado() != null ? a.getMontoPagado() : BigDecimal.ZERO));
                }
            }

            document.add(tableCuotas);

            // Balance acumulado
            PdfPTable tableBalance = new PdfPTable(2);
            tableBalance.setWidthPercentage(100);

            addCell(tableBalance, "Total Pagado a la Fecha:", FONT_BOLD);
            addCell(tableBalance, formatMoney(totalPagado), FONT_BOLD);
            addCell(tableBalance, "Total Pendiente por Cobrar:", FONT_BOLD);
            addCell(tableBalance, formatMoney(totalPendiente), FONT_BOLD);
            document.add(tableBalance);

            document.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Generado por Sistema SaaS Inmobiliario | Dev A Architect Engine", FONT_SMALL);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando PDF de Estado de Cuenta planPago ID {}: {}", planPagoId, e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de estado de cuenta: " + e.getMessage());
        }
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private PdfPCell createCenterCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FONT_NORMAL));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell createRightCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FONT_NORMAL));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(3f);
        return cell;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return currencyFormat.format(amount);
    }
}
