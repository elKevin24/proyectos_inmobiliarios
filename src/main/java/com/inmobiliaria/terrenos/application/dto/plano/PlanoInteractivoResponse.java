package com.inmobiliaria.terrenos.application.dto.plano;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuesta de plano interactivo
 * Contiene la imagen del plano y todos los terrenos con sus coordenadas
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoInteractivoResponse {

    // Información del proyecto
    private Long proyectoId;
    private String proyectoNombre;

    // Plano de fondo
    private Long planoArchivoId;
    private String planoUrl;
    private String planoNombre;

    // Dimensiones del plano (para escalado)
    private Integer anchoPlano;
    private Integer altoPlano;

    // Terrenos con coordenadas
    private List<TerrenoVisualizacionResponse> terrenos;

    // Estadísticas rápidas
    private Integer totalTerrenos;
    private Integer terrenosDisponibles;
    private Integer terrenosApartados;
    private Integer terrenosVendidos;
    private Integer terrenosConCoordenadas;
    private Integer terrenosSinCoordenadas;
}
