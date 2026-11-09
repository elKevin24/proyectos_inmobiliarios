package com.inmobiliaria.terrenos.application.dto.venta;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear una nueva venta
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVentaRequest {

    @NotNull(message = "El terreno es obligatorio")
    private Long terrenoId;

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    private Long apartadoId;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto total debe ser mayor a 0")
    private BigDecimal montoTotal;

    @NotNull(message = "El enganche es obligatorio")
    @DecimalMin(value = "0.0", message = "El enganche no puede ser negativo")
    private BigDecimal enganche;

    @NotNull(message = "El monto financiado es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto financiado no puede ser negativo")
    private BigDecimal montoFinanciado;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String observaciones;

    // Se asume que vendrá la estructura del PlanPagoRequest
    private com.inmobiliaria.terrenos.application.dto.pago.PlanPagoRequest planPago;
}
