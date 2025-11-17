package com.inmobiliaria.terrenos.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el registro de una nueva empresa (tenant) con su usuario administrador
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    // Datos de la empresa (Tenant)
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String nombreEmpresa;

    @NotBlank(message = "El email de la empresa es obligatorio")
    @Email(message = "El email de la empresa debe ser válido")
    private String emailEmpresa;

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "El teléfono solo puede contener números, +, -, espacios y paréntesis")
    private String telefonoEmpresa;

    @Size(max = 500, message = "La dirección no debe exceder 500 caracteres")
    private String direccionEmpresa;

    @Size(max = 100, message = "La razón social no debe exceder 100 caracteres")
    private String razonSocial;

    @Size(min = 12, max = 13, message = "El RFC debe tener 12 o 13 caracteres")
    @Pattern(regexp = "^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$", message = "RFC inválido")
    private String rfc;

    // Datos del usuario administrador
    @NotBlank(message = "El nombre del administrador es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido del administrador es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;

    @NotBlank(message = "El email del administrador es obligatorio")
    @Email(message = "El email del administrador debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula y un número"
    )
    private String password;

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "El teléfono solo puede contener números, +, -, espacios y paréntesis")
    private String telefono;
}
