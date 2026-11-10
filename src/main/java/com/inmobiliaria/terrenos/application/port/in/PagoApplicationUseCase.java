package com.inmobiliaria.terrenos.application.port.in;

import com.inmobiliaria.terrenos.application.dto.pago.CreatePagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PagoResponse;

import java.util.List;

/**
 * Interfaz segregada para casos de uso de operaciones de pago (ISP).
 */
public interface PagoApplicationUseCase {
    PagoResponse aplicarPago(CreatePagoRequest request);
    PagoResponse obtenerPago(Long id);
    List<PagoResponse> listarPagosPorVenta(Long ventaId);
    List<PagoResponse> listarPagos();
    void eliminarPago(Long id);
}
