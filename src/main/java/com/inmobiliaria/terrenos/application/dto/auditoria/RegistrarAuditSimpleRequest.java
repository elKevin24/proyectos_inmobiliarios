package com.inmobiliaria.terrenos.application.dto.auditoria;

import com.inmobiliaria.terrenos.domain.enums.TipoAccionAudit;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para registrar auditoría simple
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarAuditSimpleRequest {

    @NotNull(message = "El tipo de acción es obligatorio")
    private TipoAccionAudit tipoAccion;

    private String descripcion;
    private Map<String, Object> metadata;
}
