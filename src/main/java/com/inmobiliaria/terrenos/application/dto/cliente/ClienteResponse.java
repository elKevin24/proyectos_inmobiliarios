package com.inmobiliaria.terrenos.application.dto.cliente;

import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import com.inmobiliaria.terrenos.domain.enums.OrigenCliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de cliente
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private Long id;

    // Información personal
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String telefonoSecundario;

    // Dirección
    private String direccion;
    private String ciudad;
    private String estado;
    private String codigoPostal;
    private String pais;

    // Identificación oficial
    private String rfc;
    private String curp;
    private LocalDate fechaNacimiento;

    // Información comercial
    private OrigenCliente origen;
    private String origenDescripcion;
    private EstadoCliente estadoCliente;
    private String estadoClienteDescripcion;

    // Notas
    private String notas;
    private String preferencias;

    // Estadísticas (opcional, calculadas al solicitar)
    private Integer totalCotizaciones;
    private Integer totalApartados;
    private Integer totalVentas;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Helper para obtener nombre completo
     */
    public String getNombreCompleto() {
        if (nombreCompleto != null) {
            return nombreCompleto;
        }
        return nombre + " " + apellido;
    }
}
