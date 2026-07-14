package com.inmobiliaria.terrenos.domain.entity.audit;

import com.inmobiliaria.terrenos.domain.enums.TipoAccionAudit;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// Type import removed

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidad para auditoría simple (logins, exports, acciones generales)
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "audit_log_simple")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSimple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_email", length = 255)
    private String usuarioEmail;

    // Acción
    @Column(nullable = false, length = 100)
    private String accion;

    @Enumerated(EnumType.STRING)
    @Transient
    private TipoAccionAudit tipoAccion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Metadata adicional en JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (tipoAccion != null) {
            accion = tipoAccion.name();
        }
    }
}
