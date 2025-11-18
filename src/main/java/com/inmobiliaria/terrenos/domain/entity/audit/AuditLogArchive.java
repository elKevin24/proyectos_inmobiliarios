package com.inmobiliaria.terrenos.domain.entity.audit;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidad para archivo de logs antiguos (> 1 año)
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "audit_log_archive")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(nullable = false, length = 20)
    private String tipo; // "SIMPLE" o "CRITICA"

    @Type(JsonBinaryType.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> datos;

    @Column(name = "fecha_original", nullable = false)
    private LocalDateTime fechaOriginal;

    @Column(name = "fecha_archivo", nullable = false)
    @Builder.Default
    private LocalDateTime fechaArchivo = LocalDateTime.now();
}
