package com.inmobiliaria.terrenos.infrastructure.audit;

import java.lang.annotation.*;

/**
 * Anotación para marcar métodos que deben ser auditados
 *
 * @author Kevin
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * Nombre de la tabla/entidad que se está modificando
     */
    String tabla();

    /**
     * Descripción de la operación
     */
    String descripcion() default "";

    /**
     * Si debe auditar cambios campo por campo
     */
    boolean auditarCambios() default false;
}
