package com.inmobiliaria.terrenos.application.dto.pago;

import com.inmobiliaria.terrenos.domain.enums.FrecuenciaPago;
import com.inmobiliaria.terrenos.domain.enums.TipoPlanPago;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para crear un nuevo plan de pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanPagoRequest {

    @NotNull(message = "El ID de venta es obligatorio")
    private Long ventaId;

    @NotNull(message = "El tipo de plan es obligatorio")
    private TipoPlanPago tipoPlan;

    @NotNull(message = "La frecuencia de pago es obligatoria")
    @Builder.Default
    private FrecuenciaPago frecuenciaPago = FrecuenciaPago.MENSUAL;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "El monto total debe tener máximo 13 dígitos enteros y 2 decimales")
    private BigDecimal montoTotal;

    @DecimalMin(value = "0.00", message = "El enganche no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "El enganche debe tener máximo 13 dígitos enteros y 2 decimales")
    @Builder.Default
    private BigDecimal enganche = BigDecimal.ZERO;

    // El monto financiado se calcula automáticamente: montoTotal - enganche

    @DecimalMin(value = "0.00", message = "La tasa de interés no puede ser negativa")
    @DecimalMax(value = "100.00", message = "La tasa de interés no puede exceder 100%")
    @Digits(integer = 2, fraction = 2, message = "La tasa de interés debe tener máximo 2 dígitos enteros y 2 decimales")
    @Builder.Default
    private BigDecimal tasaInteresAnual = BigDecimal.ZERO;

    @NotNull(message = "Debe especificar si aplica interés")
    @Builder.Default
    private Boolean aplicaInteres = false;

    @NotNull(message = "El número de pagos es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 pago")
    @Max(value = 600, message = "El número de pagos no puede exceder 600 (50 años)")
    private Integer numeroPagos;

    @Min(value = 1, message = "El plazo debe ser al menos 1 mes")
    @Max(value = 600, message = "El plazo no puede exceder 600 meses")
    private Integer plazoMeses;

    @DecimalMin(value = "0.00", message = "La tasa de mora no puede ser negativa")
    @DecimalMax(value = "20.00", message = "La tasa de mora no puede exceder 20%")
    @Digits(integer = 2, fraction = 2, message = "La tasa de mora debe tener máximo 2 dígitos enteros y 2 decimales")
    @Builder.Default
    private BigDecimal tasaMoraMensual = BigDecimal.ZERO;

    @Min(value = 0, message = "Los días de gracia no pueden ser negativos")
    @Max(value = 90, message = "Los días de gracia no pueden exceder 90 días")
    @Builder.Default
    private Integer diasGracia = 0;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha del primer pago es obligatoria")
    @Future(message = "La fecha del primer pago debe ser en el futuro")
    private LocalDate fechaPrimerPago;

    @Size(max = 5000, message = "Las notas no pueden exceder 5000 caracteres")
    private String notas;

    /**
     * Validación personalizada: enganche no puede ser mayor que el monto total
     */
    @AssertTrue(message = "El enganche no puede ser mayor que el monto total")
    public boolean isEngancheValido() {
        if (montoTotal == null || enganche == null) {
            return true;
        }
        return enganche.compareTo(montoTotal) <= 0;
    }

    /**
     * Validación personalizada: fecha del primer pago debe ser después de la fecha de inicio
     */
    @AssertTrue(message = "La fecha del primer pago debe ser posterior a la fecha de inicio")
    public boolean isFechaPrimerPagoValida() {
        if (fechaInicio == null || fechaPrimerPago == null) {
            return true;
        }
        return fechaPrimerPago.isAfter(fechaInicio);
    }

    /**
     * Validación personalizada: si aplica interés, la tasa debe ser mayor a 0
     */
    @AssertTrue(message = "Si aplica interés, la tasa de interés debe ser mayor a 0")
    public boolean isTasaInteresValida() {
        if (aplicaInteres == null || tasaInteresAnual == null) {
            return true;
        }
        if (aplicaInteres) {
            return tasaInteresAnual.compareTo(BigDecimal.ZERO) > 0;
        }
        return true;
    }
}
