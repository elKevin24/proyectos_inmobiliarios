package com.inmobiliaria.terrenos.application.port.out;

import com.inmobiliaria.terrenos.application.dto.pdf.ContratoVentaPdfData;
import com.inmobiliaria.terrenos.application.dto.pdf.EstadoCuentaPdfData;

/**
 * Puerto de salida para desacoplar la generación y renderización visual de documentos PDF (DIP y SRP).
 */
public interface GeneradorPdfDocumentoPort {

    /**
     * Renderiza el binario PDF del Contrato Privado de Compraventa.
     *
     * @param data datos limpios recopilados para el contrato
     * @return bytes del documento PDF generado
     */
    byte[] generarContratoVenta(ContratoVentaPdfData data);

    /**
     * Renderiza el binario PDF del Estado de Cuenta y Tabla de Amortización.
     *
     * @param data datos limpios recopilados para el estado de cuenta
     * @return bytes del documento PDF generado
     */
    byte[] generarEstadoCuenta(EstadoCuentaPdfData data);
}
