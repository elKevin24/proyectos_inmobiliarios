package com.inmobiliaria.terrenos.domain.enums;

/**
 * Tipos de acciones para auditoría simple
 */
public enum TipoAccionAudit {
    // Autenticación
    LOGIN("Inicio de sesión"),
    LOGOUT("Cierre de sesión"),
    LOGIN_FAILED("Intento de inicio de sesión fallido"),
    REFRESH_TOKEN("Actualización de token"),

    // Exportaciones
    EXPORT_PDF("Exportación a PDF"),
    EXPORT_EXCEL("Exportación a Excel"),
    EXPORT_CSV("Exportación a CSV"),

    // Reportes
    GENERATE_REPORT("Generación de reporte"),
    VIEW_REPORT("Visualización de reporte"),

    // Archivos
    UPLOAD_FILE("Carga de archivo"),
    DOWNLOAD_FILE("Descarga de archivo"),
    DELETE_FILE("Eliminación de archivo"),

    // Consultas masivas
    BULK_QUERY("Consulta masiva de datos"),
    BULK_EXPORT("Exportación masiva de datos"),

    // Configuración
    CHANGE_SETTINGS("Cambio de configuración"),

    // Otros
    OTHER("Otra acción");

    private final String descripcion;

    TipoAccionAudit(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
