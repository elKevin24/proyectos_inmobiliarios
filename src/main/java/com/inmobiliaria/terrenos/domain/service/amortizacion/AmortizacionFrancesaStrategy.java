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
 * Cálculo de amortización con cuota fija e interés (Sistema Francés).
 */
@Component
@Order(10)
public class AmortizacionFrancesaStrategy implements AmortizacionCalculatorStrategy {

    @Override
    public boolean supports(PlanPago planPago) {
        return Boolean.TRUE.equals(planPago.getAplicaInteres()) &&
                planPago.getTasaInteresMensual() != null &&
                planPago.getTasaInteresMensual().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public List<Amortizacion> generarTabla(PlanPago planPago) {
        BigDecimal montoFinanciado = planPago.getMontoFinanciado();
        Integer numeroPagos = planPago.getNumeroPagos();
        BigDecimal tasaMensual = planPago.getTasaInteresMensual();

        List<Amortizacion> amortizaciones = new ArrayList<>();
        BigDecimal saldoRestante = montoFinanciado;
        LocalDate fechaVencimiento = planPago.getFechaPrimerPago();

        BigDecimal cuotaFija = calcularCuotaFija(montoFinanciado, tasaMensual, numeroPagos);

        for (int i = 1; i <= numeroPagos; i++) {
            BigDecimal interes = saldoRestante.multiply(tasaMensual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
            BigDecimal capital = cuotaFija.subtract(interes);

            if (i == numeroPagos) {
                capital = saldoRestante;
            }

            saldoRestante = saldoRestante.subtract(capital);

            Amortizacion amortizacion = Amortizacion.builder()
                    .tenantId(planPago.getTenantId())
                    .planPagoId(planPago.getId())
                    .numeroCuota(i)
                    .capital(capital.setScale(2, RoundingMode.HALF_UP))
                    .interes(interes.setScale(2, RoundingMode.HALF_UP))
                    .montoCuota(cuotaFija.setScale(2, RoundingMode.HALF_UP))
                    .montoPagado(BigDecimal.ZERO)
                    .montoPendiente(cuotaFija.setScale(2, RoundingMode.HALF_UP))
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

    private BigDecimal calcularCuotaFija(BigDecimal principal, BigDecimal tasaMensual, Integer numeroPagos) {
        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(numeroPagos), 2, RoundingMode.HALF_UP);
        }

        BigDecimal i = tasaMensual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal unoPlusI = BigDecimal.ONE.add(i);
        BigDecimal unoPlusIPotenciaN = unoPlusI.pow(numeroPagos);

        BigDecimal numerador = principal.multiply(i).multiply(unoPlusIPotenciaN);
        BigDecimal denominador = unoPlusIPotenciaN.subtract(BigDecimal.ONE);

        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private LocalDate calcularSiguienteFechaVencimiento(LocalDate fechaActual, FrecuenciaPago frecuencia) {
        return fechaActual.plusDays(frecuencia.getDias());
    }
}
