package com.inmobiliaria.terrenos.application.dto.apartado;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear un nuevo apartado
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApartadoRequest {

    @NotNull(message = "El terreno es obligatorio")
    private Long terrenoId;

    private Long cotizacionId;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String clienteNombre;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String clienteEmail;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String clienteTelefono;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String clienteDireccion;

    @NotNull(message = "El monto de apartado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    private BigDecimal montoApartado;

    @NotNull(message = "El precio total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio total debe ser mayor a 0")
    private BigDecimal precioTotal;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 día")
    @Max(value = 365, message = "La duración no puede exceder 365 días")
    private Integer duracionDias;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
