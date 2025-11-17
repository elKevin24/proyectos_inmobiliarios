package com.inmobiliaria.terrenos.application.dto.proyecto;

import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import com.inmobiliaria.terrenos.domain.enums.TipoPrecio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para proyectos inmobiliarios
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoResponse {

    private Long id;
    private Long tenantId;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String ciudad;
    private String estado;
    private String codigoPostal;
    private Double latitud;
    private Double longitud;
    private TipoPrecio tipoPrecio;
    private BigDecimal precioBase;
    private BigDecimal precioMaximo;
    private Integer totalTerrenos;
    private Integer terrenosDisponibles;
    private Integer terrenosApartados;
    private Integer terrenosVendidos;
    private EstadoProyecto estadoProyecto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Calcula el porcentaje de ocupación del proyecto
     * @return Porcentaje de terrenos vendidos o apartados
     */
    public Double getPorcentajeOcupacion() {
        if (totalTerrenos == null || totalTerrenos == 0) {
            return 0.0;
        }
        int ocupados = (terrenosApartados != null ? terrenosApartados : 0)
                     + (terrenosVendidos != null ? terrenosVendidos : 0);
        return (ocupados * 100.0) / totalTerrenos;
    }

    /**
     * Calcula el porcentaje de disponibilidad del proyecto
     * @return Porcentaje de terrenos disponibles
     */
    public Double getPorcentajeDisponibilidad() {
        if (totalTerrenos == null || totalTerrenos == 0) {
            return 0.0;
        }
        int disponibles = terrenosDisponibles != null ? terrenosDisponibles : 0;
        return (disponibles * 100.0) / totalTerrenos;
    }
}
