package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Builder
@Where(clause = "deleted = false")
public class Terreno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "proyecto_id", nullable = false)
    private Long proyectoId;

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
    @Type(JsonBinaryType.class)
    @Column(name = "coordenadas_plano", columnDefinition = "jsonb")
    private String coordenadasPlano;

    @Type(JsonBinaryType.class)
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

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private String caracteristicas;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Calcular precio final si no está establecido
        if (precioFinal == null) {
            calcularPrecioFinal();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
