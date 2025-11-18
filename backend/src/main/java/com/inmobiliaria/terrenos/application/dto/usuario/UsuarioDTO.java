package com.inmobiliaria.terrenos.application.dto.usuario;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para representar un Usuario en las respuestas
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private Boolean activo;
    private LocalDateTime ultimoAcceso;
    private Long tenantId;
    private String tenantNombre;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
