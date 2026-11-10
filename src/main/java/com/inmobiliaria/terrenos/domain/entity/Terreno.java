package com.inmobiliaria.terrenos.domain.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Entidad JPA para Terrenos/Lotes
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "terrenos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted = false")
public class Terreno extends TenantBaseEntity {

    @Column(name = "proyecto_id", nullable = false)
    private Long proyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", insertable = false, updatable = false)
    private Proyecto proyecto;

    @Column(name = "fase_id")
    private Long faseId;

    // Identificación
    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    @Column(length = 50)
    private String manzana;

    // Dimensiones
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal area;

    @Column(precision = 10, scale = 2)
    private BigDecimal frente;

    @Column(precision = 10, scale = 2)
    private BigDecimal fondo;

    // Coordenadas en el plano (JSONB)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coordenadas_plano", columnDefinition = "jsonb")
    private String coordenadasPlano;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String poligono;

    // Precio
    @Column(name = "precio_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "precio_ajuste", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal precioAjuste = BigDecimal.ZERO;

    @Column(name = "precio_multiplicador", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal precioMultiplicador = BigDecimal.ONE;

    @Column(name = "precio_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioFinal;

    // Estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTerreno estado = EstadoTerreno.DISPONIBLE;

    // Información adicional
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String caracteristicas;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @PrePersist
    @PreUpdate
    protected void onTerrenoSave() {
        // Calcular precio final si no está establecido
        if (precioFinal == null) {
            calcularPrecioFinal();
        }
        // Asegurar que caracteristicas sea JSON válido para la columna jsonb
        this.caracteristicas = normalizeJson(this.caracteristicas);
    }

    /**
     * Normaliza el campo caracteristicas para asegurar que se almacene
     * como JSON sintácticamente válido (objeto, array o string delimitado),
     * evitando errores de sintaxis en columnas jsonb de PostgreSQL/H2.
     */
    public static String normalizeJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        // Si ya es un objeto, array o string JSON delimitado, validar su sintaxis
        if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"))
                || (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2)) {
            try {
                JSON_MAPPER.readTree(trimmed);
                return trimmed;
            } catch (Exception ignored) {
                // No es JSON válido a pesar de los delimitadores, se codifica abajo
            }
        }
        // Si es texto plano (p. ej. "Lote E2E Test"), envolverlo como string JSON válido
        try {
            return JSON_MAPPER.writeValueAsString(trimmed);
        } catch (Exception e) {
            return "\"" + trimmed.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
    }

    /**
     * Calcula el precio final: (precioBase + ajuste) × multiplicador
     */
    public void calcularPrecioFinal() {
        BigDecimal base = precioBase != null ? precioBase : BigDecimal.ZERO;
        BigDecimal ajuste = precioAjuste != null ? precioAjuste : BigDecimal.ZERO;
        BigDecimal multiplicador = precioMultiplicador != null ? precioMultiplicador : BigDecimal.ONE;
        this.precioFinal = base.add(ajuste).multiply(multiplicador);
    }
}
