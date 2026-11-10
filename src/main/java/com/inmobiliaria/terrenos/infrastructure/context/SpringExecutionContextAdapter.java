package com.inmobiliaria.terrenos.infrastructure.context;

import com.inmobiliaria.terrenos.application.port.out.ExecutionContextPort;
import com.inmobiliaria.terrenos.infrastructure.security.SecurityUtils;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Adaptador de infraestructura que implementa {@link ExecutionContextPort}
 * usando TenantContext, SecurityUtils y RequestContextHolder.
 */
@Component
public class SpringExecutionContextAdapter implements ExecutionContextPort {

    @Override
    public Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    @Override
    public Optional<Long> currentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @Override
    public String currentUsernameOrDefault(String fallback) {
        return SecurityUtils.getCurrentUsername().orElse(fallback);
    }

    @Override
    public Optional<String> clientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Optional.empty();
        }
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };
        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return Optional.of(ip.split(",")[0].trim());
            }
        }
        return Optional.ofNullable(request.getRemoteAddr());
    }

    @Override
    public Optional<String> userAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(request.getHeader("User-Agent"));
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
