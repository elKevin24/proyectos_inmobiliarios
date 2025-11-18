package com.inmobiliaria.terrenos.application.dto.cotizacion;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para crear una nueva cotización
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCotizacionRequest {

    @NotNull(message = "El terreno es obligatorio")
    private Long terrenoId;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String clienteNombre;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String clienteEmail;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String clienteTelefono;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    private BigDecimal descuento;

    @DecimalMin(value = "0.0", message = "El porcentaje de descuento no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje de descuento no puede exceder 100")
    private BigDecimal porcentajeDescuento;

    @NotNull(message = "El precio final es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio final debe ser mayor a 0")
    private BigDecimal precioFinal;

    @NotNull(message = "La fecha de vigencia es obligatoria")
    @Future(message = "La fecha de vigencia debe ser futura")
    private LocalDate fechaVigencia;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
