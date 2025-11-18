package com.inmobiliaria.terrenos.application.dto.auditoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para auditoría crítica
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogCriticaResponse {

    private Long id;
    private Long tenantId;
    private Long usuarioId;
    private String usuarioEmail;
    private String tabla;
    private Long registroId;
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String operacion;
    private String operacionDescripcion;
    private String motivo;
    private String ipAddress;
    private LocalDateTime fecha;
}
