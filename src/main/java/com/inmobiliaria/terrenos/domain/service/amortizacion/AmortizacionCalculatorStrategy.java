package com.inmobiliaria.terrenos.domain.service.amortizacion;

import com.inmobiliaria.terrenos.domain.entity.Amortizacion;
import com.inmobiliaria.terrenos.domain.entity.PlanPago;

import java.util.List;

/**
 * Estrategia de cálculo de tabla de amortización (Patrón Strategy - OCP).
 */
public interface AmortizacionCalculatorStrategy {
    boolean supports(PlanPago planPago);
    List<Amortizacion> generarTabla(PlanPago planPago);
}
