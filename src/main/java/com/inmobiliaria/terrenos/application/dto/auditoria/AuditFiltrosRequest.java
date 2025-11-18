package com.inmobiliaria.terrenos.application.dto.auditoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para filtros de consulta de auditoría
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditFiltrosRequest {

    private Long usuarioId;
    private String tabla;
    private String accion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer limit;
}
