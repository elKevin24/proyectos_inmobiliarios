package com.inmobiliaria.terrenos.application.dto.auditoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta para auditoría simple
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSimpleResponse {

    private Long id;
    private Long tenantId;
    private Long usuarioId;
    private String usuarioEmail;
    private String accion;
    private String accionDescripcion;
    private String descripcion;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> metadata;
    private LocalDateTime fecha;
}
