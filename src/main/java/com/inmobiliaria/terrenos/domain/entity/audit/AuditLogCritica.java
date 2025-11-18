package com.inmobiliaria.terrenos.domain.entity.audit;

import com.inmobiliaria.terrenos.domain.enums.TipoOperacionAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para auditoría crítica (cambios en datos importantes)
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "audit_log_critica")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogCritica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_email", length = 255)
    private String usuarioEmail;

    // Tabla y registro afectado
    @Column(nullable = false, length = 100)
    private String tabla;

    @Column(name = "registro_id", nullable = false)
    private Long registroId;

    // Campo modificado
    @Column(nullable = false, length = 100)
    private String campo;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    // Operación
    @Column(nullable = false, length = 20)
    private String operacion;

    @Enumerated(EnumType.STRING)
    @Transient
    private TipoOperacionAudit tipoOperacion;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (tipoOperacion != null) {
            operacion = tipoOperacion.name();
        }
    }

    /**
     * Verifica si hubo cambio real en el valor
     */
    public boolean hasChange() {
        if (valorAnterior == null && valorNuevo == null) {
            return false;
        }
        if (valorAnterior == null || valorNuevo == null) {
            return true;
        }
        return !valorAnterior.equals(valorNuevo);
    }
}
