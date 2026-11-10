package com.inmobiliaria.terrenos.application.port.in;

/**
 * Puerto para generación y exportación de documentos PDF (ISP & DIP).
 */
public interface DocumentoPdfPort {
    byte[] generarContratoVentaPdf(Long ventaId);
    byte[] generarEstadoCuentaPdf(Long planPagoId);
}
