package com.inmobiliaria.terrenos.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Devuelve respuestas JSON consistentes para todos los errores.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Error de validación en los datos enviados")
                .path(request.getDescription(false).replace("uri=", ""))
                .details(errors)
                .build();

        log.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de recurso no encontrado.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones de tenant no encontrado.
     */
    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTenantNotFoundException(
            TenantNotFoundException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("Acceso denegado: " + ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        log.error("Tenant not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja excepciones de Clientes.
     */
    @ExceptionHandler(ClienteException.class)
    public ResponseEntity<ErrorResponse> handleClienteException(
            ClienteException ex, WebRequest request) {
        return buildModuleError(ex, "Cliente", request);
    }

    /**
     * Maneja excepciones de Ventas.
     */
    @ExceptionHandler(VentaException.class)
    public ResponseEntity<ErrorResponse> handleVentaException(
            VentaException ex, WebRequest request) {
        return buildModuleError(ex, "Venta", request);
    }

    /**
     * Maneja excepciones de Pagos.
     */
    @ExceptionHandler(PagoException.class)
    public ResponseEntity<ErrorResponse> handlePagoException(
            PagoException ex, WebRequest request) {
        return buildModuleError(ex, "Pago", request);
    }

    /**
     * Maneja excepciones de Proyectos.
     */
    @ExceptionHandler(ProyectoException.class)
    public ResponseEntity<ErrorResponse> handleProyectoException(
            ProyectoException ex, WebRequest request) {
        return buildModuleError(ex, "Proyecto", request);
    }

    /**
     * Maneja excepciones de Terrenos.
     */
    @ExceptionHandler(TerrenoException.class)
    public ResponseEntity<ErrorResponse> handleTerrenoException(
            TerrenoException ex, WebRequest request) {
        return buildModuleError(ex, "Terreno", request);
    }

    /**
     * Maneja excepciones de Apartados.
     */
    @ExceptionHandler(ApartadoException.class)
    public ResponseEntity<ErrorResponse> handleApartadoException(
            ApartadoException ex, WebRequest request) {
        return buildModuleError(ex, "Apartado", request);
    }

    /**
     * Maneja excepciones de Fases.
     */
    @ExceptionHandler(FaseException.class)
    public ResponseEntity<ErrorResponse> handleFaseException(
            FaseException ex, WebRequest request) {
        return buildModuleError(ex, "Fase", request);
    }

    /**
     * Maneja excepciones de Cotizaciones.
     */
    @ExceptionHandler(CotizacionException.class)
    public ResponseEntity<ErrorResponse> handleCotizacionException(
            CotizacionException ex, WebRequest request) {
        return buildModuleError(ex, "Cotizacion", request);
    }

    /**
     * Maneja excepciones de Archivos.
     */
    @ExceptionHandler(ArchivoException.class)
    public ResponseEntity<ErrorResponse> handleArchivoException(
            ArchivoException ex, WebRequest request) {
        return buildModuleError(ex, "Archivo", request);
    }

    /**
     * Maneja excepciones de Autenticación.
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(
            AuthException ex, WebRequest request) {
        return buildModuleError(ex, "Auth", request);
    }

    /**
     * Maneja excepciones de Planos.
     */
    @ExceptionHandler(PlanoException.class)
    public ResponseEntity<ErrorResponse> handlePlanoException(
            PlanoException ex, WebRequest request) {
        return buildModuleError(ex, "Plano", request);
    }

    /**
     * Maneja excepciones de Auditoría.
     */
    @ExceptionHandler(AuditException.class)
    public ResponseEntity<ErrorResponse> handleAuditException(
            AuditException ex, WebRequest request) {
        return buildModuleError(ex, "Audit", request);
    }

    /**
     * Maneja excepciones de Reportes.
     */
    @ExceptionHandler(ReporteException.class)
    public ResponseEntity<ErrorResponse> handleReporteException(
            ReporteException ex, WebRequest request) {
        return buildModuleError(ex, "Reporte", request);
    }

    /**
     * Maneja excepciones de lógica de negocio genéricas.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        return buildModuleError(ex, "Business", request);
    }

    private ResponseEntity<ErrorResponse> buildModuleError(
            BusinessException ex, String module, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getCode())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        log.warn("[{}] {}", module, ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    /**
     * Maneja errores de autenticación.
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Credenciales inválidas")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja errores de acceso denegado.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("No tiene permisos para acceder a este recurso")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja todas las demás excepciones no capturadas.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocurrió un error inesperado. Por favor contacte al administrador.")
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
