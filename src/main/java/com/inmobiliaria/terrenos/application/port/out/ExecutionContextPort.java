package com.inmobiliaria.terrenos.application.port.out;

import java.util.Optional;

/**
 * Puerto de salida para desacoplar el dominio y la aplicación de ThreadLocals,
 * Spring Security y la API Servlet (DIP y SRP).
 */
public interface ExecutionContextPort {
    Long requireTenantId();
    Optional<Long> currentUserId();
    String currentUsernameOrDefault(String fallback);
    Optional<String> clientIpAddress();
    Optional<String> userAgent();
}
