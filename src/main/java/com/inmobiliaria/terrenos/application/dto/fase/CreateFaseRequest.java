package com.inmobiliaria.terrenos.application.dto.fase;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para crear una nueva fase
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFaseRequest {

    @NotNull(message = "El proyecto es obligatorio")
    private Long proyectoId;

    @NotBlank(message = "El nombre de la fase es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @Min(value = 1, message = "El número de fase debe ser al menos 1")
    private Integer numeroFase;

    @Min(value = 0, message = "El total de terrenos no puede ser negativo")
    private Integer totalTerrenos;

    @DecimalMin(value = "0.0", message = "El área total no puede ser negativa")
    private BigDecimal areaTotal;

    private LocalDate fechaInicio;

    private LocalDate fechaFinEstimada;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    private Boolean activa;
}
