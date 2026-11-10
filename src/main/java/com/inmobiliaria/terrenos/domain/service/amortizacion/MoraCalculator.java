package com.inmobiliaria.terrenos.domain.service.amortizacion;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Componente enfocado exclusivamente en el cálculo de moras (SRP).
 */
@Component
public class MoraCalculator {

    /**
     * Calcula la mora basándose en el monto pendiente, tasa mensual y días de atraso.
     * Mora = MontoPendiente * (TasaMoraMensual / 30) * DiasAtraso
     */
    public BigDecimal calcularMora(BigDecimal montoPendiente, BigDecimal tasaMoraMensual, int diasAtraso) {
        if (tasaMoraMensual == null || tasaMoraMensual.compareTo(BigDecimal.ZERO) <= 0 || diasAtraso <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tasaDiaria = tasaMoraMensual.divide(new BigDecimal("30"), 6, RoundingMode.HALF_UP);
        BigDecimal tasaDecimal = tasaDiaria.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal mora = montoPendiente.multiply(tasaDecimal).multiply(new BigDecimal(diasAtraso));

        return mora.setScale(2, RoundingMode.HALF_UP);
    }
}
