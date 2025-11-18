package com.inmobiliaria.terrenos.infrastructure.audit;

import com.inmobiliaria.terrenos.application.service.AuditService;
import com.inmobiliaria.terrenos.domain.enums.TipoOperacionAudit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect AOP para auditoría automática
 *
 * @author Kevin
 * @version 1.0.0
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    /**
     * Intercepts métodos anotados con @Auditable
     */
    @AfterReturning(
            pointcut = "@annotation(com.inmobiliaria.terrenos.infrastructure.audit.Auditable)",
            returning = "result"
    )
    public void auditMethod(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            if (auditable != null) {
                String tabla = auditable.tabla();
                String descripcion = auditable.descripcion();

                // Obtener el ID del objeto retornado (si es aplicable)
                Long registroId = extractId(result);

                if (registroId != null) {
                    // Determinar tipo de operación basado en el nombre del método
                    TipoOperacionAudit operacion = determinarOperacion(method.getName());

                    // Registrar en auditoría crítica
                    auditService.registrarCambioCritico(
                            tabla,
                            registroId,
                            "operacion",
                            null,
                            descripcion.isEmpty() ? method.getName() : descripcion,
                            operacion,
                            "Operación automática"
                    );

                    log.debug("Auditoría registrada para {} #{}", tabla, registroId);
                }
            }
        } catch (Exception e) {
            log.error("Error al registrar auditoría AOP: {}", e.getMessage(), e);
            // No lanzar excepción para no interrumpir el flujo normal
        }
    }

    private Long extractId(Object result) {
        if (result == null) {
            return null;
        }

        try {
            // Intentar obtener el ID mediante reflexión
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            return id instanceof Long ? (Long) id : null;
        } catch (Exception e) {
            return null;
        }
    }

    private TipoOperacionAudit determinarOperacion(String methodName) {
        String lowerName = methodName.toLowerCase();

        if (lowerName.contains("crear") || lowerName.contains("create") || lowerName.contains("save")) {
            return TipoOperacionAudit.CREATE;
        } else if (lowerName.contains("actualizar") || lowerName.contains("update") || lowerName.contains("modificar")) {
            return TipoOperacionAudit.UPDATE;
        } else if (lowerName.contains("eliminar") || lowerName.contains("delete") || lowerName.contains("borrar")) {
            return TipoOperacionAudit.DELETE;
        } else if (lowerName.contains("estado") || lowerName.contains("status")) {
            return TipoOperacionAudit.STATUS_CHANGE;
        } else if (lowerName.contains("precio") || lowerName.contains("price")) {
            return TipoOperacionAudit.PRICE_CHANGE;
        } else if (lowerName.contains("asignar") || lowerName.contains("assign")) {
            return TipoOperacionAudit.ASSIGNMENT;
        }

        return TipoOperacionAudit.UPDATE;
    }
}
