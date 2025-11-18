package com.inmobiliaria.terrenos.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Utilidades para obtener información del contexto de seguridad
 *
 * @author Kevin
 * @version 1.0.0
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Obtiene el username del usuario actualmente autenticado
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return Optional.of(((UserDetails) principal).getUsername());
        } else if (principal instanceof String) {
            return Optional.of((String) principal);
        }

        return Optional.empty();
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado
     * Nota: En este proyecto, el username es el email del usuario
     */
    public static Optional<Long> getCurrentUserId() {
        // Por ahora retornamos Optional vacío ya que necesitaríamos
        // acceso al UserRepository para obtener el ID desde el email
        // Esto se puede mejorar guardando el userId en el contexto de seguridad
        return Optional.empty();
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.isAuthenticated() &&
               !"anonymousUser".equals(authentication.getPrincipal());
    }
}
