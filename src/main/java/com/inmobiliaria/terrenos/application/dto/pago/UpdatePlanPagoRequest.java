package com.inmobiliaria.terrenos.application.dto.pago;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para actualizar un plan de pago
 * Solo permite actualizar campos que no afectan la tabla de amortización
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlanPagoRequest {

    @DecimalMin(value = "0.00", message = "La tasa de mora no puede ser negativa")
    @DecimalMax(value = "20.00", message = "La tasa de mora no puede exceder 20%")
    @Digits(integer = 2, fraction = 2, message = "La tasa de mora debe tener máximo 2 dígitos enteros y 2 decimales")
    private BigDecimal tasaMoraMensual;

    @Min(value = 0, message = "Los días de gracia no pueden ser negativos")
    @Max(value = 90, message = "Los días de gracia no pueden exceder 90 días")
    private Integer diasGracia;

    @Size(max = 5000, message = "Las notas no pueden exceder 5000 caracteres")
    private String notas;
}
