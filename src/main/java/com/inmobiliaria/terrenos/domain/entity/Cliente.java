package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import com.inmobiliaria.terrenos.domain.enums.OrigenCliente;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para Clientes/Compradores
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    // Información personal
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(name = "telefono_secundario", length = 20)
    private String telefonoSecundario;

    // Dirección
    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String estado;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(length = 100)
    @Builder.Default
    private String pais = "México";

    // Identificación oficial
    @Column(length = 13)
    private String rfc;

    @Column(length = 18)
    private String curp;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    // Información comercial
    @Enumerated(EnumType.STRING)
    private OrigenCliente origen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_cliente", nullable = false, length = 20)
    @Builder.Default
    private EstadoCliente estadoCliente = EstadoCliente.PROSPECTO;

    // Notas y observaciones
    @Column(columnDefinition = "TEXT")
    private String notas;

    // Preferencias del cliente (presupuesto, ubicación, tamaño, etc.)
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private String preferencias;

    // Auditoría
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Obtiene el nombre completo del cliente
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
