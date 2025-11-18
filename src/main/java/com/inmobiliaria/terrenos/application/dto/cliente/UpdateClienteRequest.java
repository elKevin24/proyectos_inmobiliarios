package com.inmobiliaria.terrenos.application.dto.cliente;

import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import com.inmobiliaria.terrenos.domain.enums.OrigenCliente;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para actualizar un cliente existente
 * Todos los campos son opcionales
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClienteRequest {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    private String apellido;

    @Email(message = "El email debe ser válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "El teléfono solo puede contener números, +, -, espacios y paréntesis")
    private String telefono;

    @Size(max = 20, message = "El teléfono secundario no puede exceder 20 caracteres")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "El teléfono solo puede contener números, +, -, espacios y paréntesis")
    private String telefonoSecundario;

    // Dirección
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String ciudad;

    @Size(max = 100, message = "El estado no puede exceder 100 caracteres")
    private String estado;

    @Size(max = 10, message = "El código postal no puede exceder 10 caracteres")
    private String codigoPostal;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String pais;

    // Identificación oficial
    @Size(min = 12, max = 13, message = "El RFC debe tener entre 12 y 13 caracteres")
    @Pattern(regexp = "^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$", message = "El RFC no tiene un formato válido")
    private String rfc;

    @Size(min = 18, max = 18, message = "El CURP debe tener 18 caracteres")
    @Pattern(regexp = "^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9]{2}$", message = "El CURP no tiene un formato válido")
    private String curp;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    // Información comercial
    private OrigenCliente origen;

    private EstadoCliente estadoCliente;

    // Notas
    @Size(max = 5000, message = "Las notas no pueden exceder 5000 caracteres")
    private String notas;

    // Preferencias (JSON string)
    private String preferencias;
}
