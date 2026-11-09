package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Pagos.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class PagoException extends BusinessException {

    public PagoException(String message) {
        super(message, "PAGO_ERROR", HttpStatus.BAD_REQUEST);
    }

    public PagoException(String message, HttpStatus status) {
        super(message, "PAGO_ERROR", status);
    }

    public PagoException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static PagoException planDuplicado(Long ventaId) {
        return new PagoException(
                "Ya existe un plan de pago para esta venta: " + ventaId,
                "PAGO_PLAN_DUPLICADO",
                HttpStatus.CONFLICT
        );
    }

    public static PagoException sinCuotasPendientes() {
        return new PagoException(
                "No hay cuotas pendientes de pago",
                "PAGO_SIN_CUOTAS_PENDIENTES",
                HttpStatus.BAD_REQUEST
        );
    }

    public static PagoException referenciaRequerida() {
        return new PagoException(
                "La referencia es obligatoria para transferencias y cheques",
                "PAGO_REFERENCIA_REQUERIDA",
                HttpStatus.BAD_REQUEST
        );
    }

    public static PagoException montoExcedeSaldo() {
        return new PagoException(
                "El monto del pago excede el saldo pendiente",
                "PAGO_MONTO_EXCEDE_SALDO",
                HttpStatus.BAD_REQUEST
        );
    }
}
