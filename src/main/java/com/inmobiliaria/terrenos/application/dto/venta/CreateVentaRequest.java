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

    private Long apartadoId;

    @NotBlank(message = "El nombre del comprador es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String compradorNombre;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String compradorEmail;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String compradorTelefono;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String compradorDireccion;

    @Size(max = 20, message = "El RFC no puede exceder 20 caracteres")
    private String compradorRfc;

    @Size(max = 20, message = "El CURP no puede exceder 20 caracteres")
    private String compradorCurp;

    @NotNull(message = "El precio total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio total debe ser mayor a 0")
    private BigDecimal precioTotal;

    @DecimalMin(value = "0.0", message = "El monto del apartado no puede ser negativo")
    private BigDecimal montoApartadoAcreditado;

    @NotNull(message = "El monto final es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto final debe ser mayor a 0")
    private BigDecimal montoFinal;

    @DecimalMin(value = "0.0", message = "El porcentaje de comisión no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje de comisión no puede exceder 100")
    private BigDecimal porcentajeComision;

    @DecimalMin(value = "0.0", message = "El monto de comisión no puede ser negativo")
    private BigDecimal montoComision;

    @NotBlank(message = "La forma de pago es obligatoria")
    @Size(max = 50, message = "La forma de pago no puede exceder 50 caracteres")
    private String formaPago;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
