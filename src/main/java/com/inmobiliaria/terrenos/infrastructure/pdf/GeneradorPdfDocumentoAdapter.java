package com.inmobiliaria.terrenos.infrastructure.pdf;

import com.inmobiliaria.terrenos.application.dto.pdf.ContratoVentaPdfData;
import com.inmobiliaria.terrenos.application.dto.pdf.EstadoCuentaPdfData;
import com.inmobiliaria.terrenos.application.port.out.GeneradorPdfDocumentoPort;
import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Adaptador de infraestructura que implementa {@link GeneradorPdfDocumentoPort}
 * utilizando la biblioteca OpenPDF / iText para el renderizado gráfico.
 */
@Component
@Slf4j
public class GeneradorPdfDocumentoAdapter implements GeneradorPdfDocumentoPort {

    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(41, 128, 185));
    private static final Font FONT_SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
    private static final Font FONT_TABLE_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generarContratoVenta(ContratoVentaPdfData data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Encabezado
            Paragraph title = new Paragraph("CONTRATO PRIVADO DE COMPRAVENTA", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String empresa = data.getNombreEmpresa() != null ? data.getNombreEmpresa() : "Inmobiliaria SaaS";
            Paragraph subtitle = new Paragraph(empresa.toUpperCase(), FONT_SUBTITLE);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));
            String fechaStr = data.getFechaVenta() != null ? data.getFechaVenta().format(dateFormatter) : "N/A";
            document.add(new Paragraph("Folio de Venta: " + (data.getFolioVenta() != null ? data.getFolioVenta() : "N/A") +
                    " | Fecha: " + fechaStr, FONT_BOLD));

            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------"));

            // Sección 1: Datos del Comprador
            document.add(new Paragraph("1. DATOS DEL COMPRADOR", FONT_SECTION));
            PdfPTable tableComprador = new PdfPTable(2);
            tableComprador.setWidthPercentage(100);
            tableComprador.setSpacingBefore(5f);
            tableComprador.setSpacingAfter(10f);

            addCell(tableComprador, "Nombre Completo:", FONT_BOLD);
            addCell(tableComprador, data.getCompradorNombre() != null ? data.getCompradorNombre() : "N/A", FONT_NORMAL);
            addCell(tableComprador, "RFC:", FONT_BOLD);
            addCell(tableComprador, data.getCompradorRfc() != null ? data.getCompradorRfc() : "N/A", FONT_NORMAL);
            addCell(tableComprador, "CURP:", FONT_BOLD);
            addCell(tableComprador, data.getCompradorCurp() != null ? data.getCompradorCurp() : "N/A", FONT_NORMAL);
            addCell(tableComprador, "Teléfono / Email:", FONT_BOLD);
            addCell(tableComprador, (data.getCompradorTelefono() != null ? data.getCompradorTelefono() : "") + " | " +
                    (data.getCompradorEmail() != null ? data.getCompradorEmail() : ""), FONT_NORMAL);
            addCell(tableComprador, "Dirección:", FONT_BOLD);
            addCell(tableComprador, data.getCompradorDireccion() != null ? data.getCompradorDireccion() : "N/A", FONT_NORMAL);
            document.add(tableComprador);

            // Sección 2: Datos del Terreno y Proyecto
            document.add(new Paragraph("2. ESPECIFICACIONES DEL TERRENO", FONT_SECTION));
            PdfPTable tableTerreno = new PdfPTable(2);
            tableTerreno.setWidthPercentage(100);
            tableTerreno.setSpacingBefore(5f);
            tableTerreno.setSpacingAfter(10f);

            addCell(tableTerreno, "Proyecto / Desarrollo:", FONT_BOLD);
            addCell(tableTerreno, data.getProyectoNombre() != null ? data.getProyectoNombre() : "N/A", FONT_NORMAL);
            addCell(tableTerreno, "Número de Lote:", FONT_BOLD);
            addCell(tableTerreno, data.getNumeroLote() != null ? data.getNumeroLote() : "N/A", FONT_NORMAL);
            addCell(tableTerreno, "Manzana:", FONT_BOLD);
            addCell(tableTerreno, data.getManzana() != null ? data.getManzana() : "N/A", FONT_NORMAL);
            addCell(tableTerreno, "Área Total (m²):", FONT_BOLD);
            addCell(tableTerreno, data.getAreaTerreno() != null ? data.getAreaTerreno() + " m²" : "N/A", FONT_NORMAL);
            document.add(tableTerreno);

            // Sección 3: Condiciones Financieras
            document.add(new Paragraph("3. CONDICIONES ECONÓMICAS Y FORMA DE PAGO", FONT_SECTION));
            PdfPTable tableFinanzas = new PdfPTable(2);
            tableFinanzas.setWidthPercentage(100);
            tableFinanzas.setSpacingBefore(5f);
            tableFinanzas.setSpacingAfter(15f);

            addCell(tableFinanzas, "Precio Pactado de Venta:", FONT_BOLD);
            addCell(tableFinanzas, formatMoney(data.getPrecioTotal()), FONT_NORMAL);

            addCell(tableFinanzas, "Anticipo / Apartado Acreditado:", FONT_BOLD);
            addCell(tableFinanzas, formatMoney(data.getMontoApartadoAcreditado()), FONT_NORMAL);

            addCell(tableFinanzas, "Monto Saldo a Pagar:", FONT_BOLD);
            addCell(tableFinanzas, formatMoney(data.getMontoFinal()), FONT_BOLD);

            addCell(tableFinanzas, "Forma de Pago:", FONT_BOLD);
            addCell(tableFinanzas, data.getFormaPago() != null ? data.getFormaPago() : "De Contado", FONT_NORMAL);

            addCell(tableFinanzas, "Estado de la Venta:", FONT_BOLD);
            addCell(tableFinanzas, data.getEstadoVenta() != null ? data.getEstadoVenta() : "N/A", FONT_NORMAL);
            document.add(tableFinanzas);

            // Firmas
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable tableFirmas = new PdfPTable(2);
            tableFirmas.setWidthPercentage(100);

            PdfPCell cellFirma1 = new PdfPCell(new Paragraph("_____________________________________\nEL VENDEDOR\n" + empresa, FONT_BOLD));
            cellFirma1.setBorder(Rectangle.NO_BORDER);
            cellFirma1.setHorizontalAlignment(Element.ALIGN_CENTER);

            String comprador = data.getCompradorNombre() != null ? data.getCompradorNombre() : "";
            PdfPCell cellFirma2 = new PdfPCell(new Paragraph("_____________________________________\nEL COMPRADOR\n" + comprador, FONT_BOLD));
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
            log.error("Error al generar PDF de contrato de venta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de contrato de venta: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generarEstadoCuenta(EstadoCuentaPdfData data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("ESTADO DE CUENTA Y TABLA DE AMORTIZACIÓN", FONT_TITLE);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String empresa = data.getNombreEmpresa() != null ? data.getNombreEmpresa() : "Inmobiliaria SaaS";
            Paragraph subtitle = new Paragraph(empresa.toUpperCase(), FONT_SUBTITLE);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Plan de Pago ID: #" + data.getPlanPagoId() + " | Venta ID: #" + data.getVentaId(), FONT_BOLD));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------"));

            // Resumen de Plan
            PdfPTable tableResumen = new PdfPTable(2);
            tableResumen.setWidthPercentage(100);
            tableResumen.setSpacingBefore(5f);
            tableResumen.setSpacingAfter(10f);

            addCell(tableResumen, "Monto Financiamiento:", FONT_BOLD);
            addCell(tableResumen, formatMoney(data.getMontoFinanciado()), FONT_NORMAL);

            addCell(tableResumen, "Plazo (Meses) / Frecuencia:", FONT_BOLD);
            Integer cuotasCount = data.getPlazoMeses() != null ? data.getPlazoMeses() : data.getNumeroPagos();
            String frecuencia = data.getFrecuenciaPago() != null ? data.getFrecuenciaPago().name() : "MENSUAL";
            addCell(tableResumen, (cuotasCount != null ? cuotasCount : 0) + " cuotas (" + frecuencia + ")", FONT_NORMAL);

            addCell(tableResumen, "Tasa Interés Anual:", FONT_BOLD);
            addCell(tableResumen, (data.getTasaInteresAnual() != null ? data.getTasaInteresAnual() + "%" : "0%"), FONT_NORMAL);

            addCell(tableResumen, "Total Financiamiento:", FONT_BOLD);
            addCell(tableResumen, formatMoney(data.getMontoTotal()), FONT_BOLD);
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

            if (data.getCuotas() != null) {
                for (EstadoCuentaPdfData.CuotaItem item : data.getCuotas()) {
                    tableCuotas.addCell(createCenterCell(String.valueOf(item.getNumeroCuota())));
                    tableCuotas.addCell(createCenterCell(item.getFechaVencimiento() != null ? item.getFechaVencimiento().format(dateFormatter) : ""));
                    tableCuotas.addCell(createRightCell(formatMoney(item.getCapital())));
                    tableCuotas.addCell(createRightCell(formatMoney(item.getInteres())));
                    tableCuotas.addCell(createRightCell(formatMoney(item.getMontoCuota())));
                    tableCuotas.addCell(createRightCell(formatMoney(item.getMontoPagado())));
                    tableCuotas.addCell(createCenterCell(item.getEstado() != null ? item.getEstado().name() : "PENDIENTE"));

                    if (item.getMontoPagado() != null) {
                        totalPagado = totalPagado.add(item.getMontoPagado());
                    }
                    if (item.getEstado() == EstadoAmortizacion.PENDIENTE) {
                        BigDecimal pagado = item.getMontoPagado() != null ? item.getMontoPagado() : BigDecimal.ZERO;
                        BigDecimal cuota = item.getMontoCuota() != null ? item.getMontoCuota() : BigDecimal.ZERO;
                        totalPendiente = totalPendiente.add(cuota.subtract(pagado));
                    }
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
            log.error("Error al generar PDF de estado de cuenta: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar PDF de estado de cuenta: " + e.getMessage(), e);
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
