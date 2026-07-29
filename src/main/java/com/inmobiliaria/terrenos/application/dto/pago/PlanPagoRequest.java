package com.inmobiliaria.terrenos.application.dto.pago;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para la creación de un plan de pago (financiado)
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPagoRequest {

    @NotBlank(message = "El tipo de plan es obligatorio")
    private String tipoPlan;

    @NotNull(message = "El número de cuotas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 cuota")
    private Integer numeroCuotas;

    @NotBlank(message = "La frecuencia de pago es obligatoria")
    private String frecuenciaPago;

    @NotNull(message = "La tasa de interés es obligatoria")
    private BigDecimal tasaInteres;

    @NotNull(message = "El día de pago es obligatorio")
    @Min(value = 1, message = "El día de pago debe ser entre 1 y 31")
    private Integer diaPago;
}
