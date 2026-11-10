package com.inmobiliaria.terrenos.domain.service.amortizacion;

import com.inmobiliaria.terrenos.domain.entity.Amortizacion;
import com.inmobiliaria.terrenos.domain.entity.PlanPago;
import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import com.inmobiliaria.terrenos.domain.enums.FrecuenciaPago;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cálculo de amortización lineal sin interés (Capital distribuido en cuotas iguales).
 */
@Component
@Order(20)
public class AmortizacionLinealStrategy implements AmortizacionCalculatorStrategy {

    @Override
    public boolean supports(PlanPago planPago) {
        return !Boolean.TRUE.equals(planPago.getAplicaInteres()) ||
                planPago.getTasaInteresMensual() == null ||
                planPago.getTasaInteresMensual().compareTo(BigDecimal.ZERO) <= 0;
    }

    @Override
    public List<Amortizacion> generarTabla(PlanPago planPago) {
        BigDecimal montoFinanciado = planPago.getMontoFinanciado();
        Integer numeroPagos = planPago.getNumeroPagos();

        List<Amortizacion> amortizaciones = new ArrayList<>();
        BigDecimal saldoRestante = montoFinanciado;
        LocalDate fechaVencimiento = planPago.getFechaPrimerPago();

        BigDecimal capitalPorCuota = montoFinanciado.divide(new BigDecimal(numeroPagos), 2, RoundingMode.HALF_UP);

        for (int i = 1; i <= numeroPagos; i++) {
            BigDecimal capital = capitalPorCuota;

            if (i == numeroPagos) {
                capital = saldoRestante;
            }

            saldoRestante = saldoRestante.subtract(capital);

            Amortizacion amortizacion = Amortizacion.builder()
                    .tenantId(planPago.getTenantId())
                    .planPagoId(planPago.getId())
                    .numeroCuota(i)
                    .capital(capital.setScale(2, RoundingMode.HALF_UP))
                    .interes(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .montoCuota(capital.setScale(2, RoundingMode.HALF_UP))
                    .montoPagado(BigDecimal.ZERO)
                    .montoPendiente(capital.setScale(2, RoundingMode.HALF_UP))
                    .moraAcumulada(BigDecimal.ZERO)
                    .diasAtraso(0)
                    .fechaVencimiento(fechaVencimiento)
                    .estado(EstadoAmortizacion.PENDIENTE)
                    .saldoRestante(saldoRestante.setScale(2, RoundingMode.HALF_UP))
                    .build();

            amortizaciones.add(amortizacion);
            fechaVencimiento = calcularSiguienteFechaVencimiento(fechaVencimiento, planPago.getFrecuenciaPago());
        }

        return amortizaciones;
    }

    private LocalDate calcularSiguienteFechaVencimiento(LocalDate fechaActual, FrecuenciaPago frecuencia) {
        return fechaActual.plusDays(frecuencia.getDias());
    }
}
