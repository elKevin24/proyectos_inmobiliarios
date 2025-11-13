package com.inmobiliaria.terrenos.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Roles estándar del sistema.
 * Define los 5 roles principales con sus descripciones.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum RolEnum {

    ADMIN("Administrador", "Control total sobre la configuración de su empresa"),
    SUPERVISOR("Supervisor", "Gestión de Proyectos y Fases asignadas"),
    VENDEDOR("Vendedor", "Creación y seguimiento de transacciones"),
    SECRETARIA("Secretaria", "Funcionalidades de lectura y exportación de datos"),
    CONTADOR("Contador", "Acceso a reportes e información financiera");

    private final String nombre;
    private final String descripcion;
}
