package com.inmobiliaria.terrenos.application.dto.pdf;

import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import com.inmobiliaria.terrenos.domain.enums.FrecuenciaPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de datos desacoplado para la renderización gráfica del Estado de Cuenta (SRP).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoCuentaPdfData {
    private String nombreEmpresa;
    private Long planPagoId;
    private Long ventaId;
    private BigDecimal montoFinanciado;
    private Integer plazoMeses;
    private Integer numeroPagos;
    private FrecuenciaPago frecuenciaPago;
    private BigDecimal tasaInteresAnual;
    private BigDecimal montoTotal;
    private List<CuotaItem> cuotas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CuotaItem {
        private Integer numeroCuota;
        private LocalDate fechaVencimiento;
        private BigDecimal capital;
        private BigDecimal interes;
        private BigDecimal montoCuota;
        private BigDecimal montoPagado;
        private EstadoAmortizacion estado;
    }
}
