package com.inmobiliaria.terrenos.application.port.in;

import com.inmobiliaria.terrenos.application.dto.pago.CreatePlanPagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.EstadoCuentaResponse;
import com.inmobiliaria.terrenos.application.dto.pago.PlanPagoResponse;
import com.inmobiliaria.terrenos.application.dto.pago.TablaAmortizacionResponse;
import com.inmobiliaria.terrenos.application.dto.pago.UpdatePlanPagoRequest;

import java.util.List;

/**
 * Interfaz segregada para casos de uso de planes de pago y estado de cuenta (ISP).
 */
public interface PlanPagoUseCase {
    PlanPagoResponse crearPlanPago(CreatePlanPagoRequest request);
    PlanPagoResponse obtenerPlanPago(Long id);
    List<PlanPagoResponse> listarPlanesPago();
    void eliminarPlanPago(Long id);
    PlanPagoResponse obtenerPlanPagoPorVenta(Long ventaId);
    PlanPagoResponse actualizarPlanPago(Long id, UpdatePlanPagoRequest request);
    TablaAmortizacionResponse obtenerTablaAmortizacion(Long planPagoId);
    EstadoCuentaResponse obtenerEstadoCuenta(Long planPagoId);
}
