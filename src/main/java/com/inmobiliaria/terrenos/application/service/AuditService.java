package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.auditoria.*;
import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogArchive;
import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogCritica;
import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogSimple;
import com.inmobiliaria.terrenos.domain.enums.TipoAccionAudit;
import com.inmobiliaria.terrenos.domain.enums.TipoOperacionAudit;
import com.inmobiliaria.terrenos.domain.repository.AuditLogArchiveRepository;
import com.inmobiliaria.terrenos.domain.repository.AuditLogCriticaRepository;
import com.inmobiliaria.terrenos.domain.repository.AuditLogSimpleRepository;
import com.inmobiliaria.terrenos.infrastructure.security.SecurityUtils;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de auditoría
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogSimpleRepository auditLogSimpleRepository;
    private final AuditLogCriticaRepository auditLogCriticaRepository;
    private final AuditLogArchiveRepository auditLogArchiveRepository;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    // ==================== AUDITORÍA SIMPLE ====================

    /**
     * Registra una acción simple en el log de auditoría
     */
    @Transactional
    public void registrarAccionSimple(TipoAccionAudit tipoAccion, String descripcion, Map<String, Object> metadata) {
        Long tenantId = getTenantId();
        String usuarioEmail = SecurityUtils.getCurrentUser();

        HttpServletRequest request = getCurrentRequest();
        String ipAddress = request != null ? getClientIp(request) : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        AuditLogSimple log = AuditLogSimple.builder()
                .tenantId(tenantId)
                .usuarioEmail(usuarioEmail)
                .tipoAccion(tipoAccion)
                .descripcion(descripcion)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .metadata(metadata)
                .build();

        auditLogSimpleRepository.save(log);
        log.debug("Auditoría simple registrada: {}", tipoAccion);
    }

    /**
     * Registra un login exitoso
     */
    @Transactional
    public void registrarLogin(String usuarioEmail, Long usuarioId) {
        Long tenantId = getTenantId();
        HttpServletRequest request = getCurrentRequest();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("usuario_id", usuarioId);
        metadata.put("success", true);

        AuditLogSimple log = AuditLogSimple.builder()
                .tenantId(tenantId)
                .usuarioId(usuarioId)
                .usuarioEmail(usuarioEmail)
                .tipoAccion(TipoAccionAudit.LOGIN)
                .descripcion("Inicio de sesión exitoso")
                .ipAddress(request != null ? getClientIp(request) : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .metadata(metadata)
                .build();

        auditLogSimpleRepository.save(log);
    }

    /**
     * Registra un login fallido
     */
    @Transactional
    public void registrarLoginFallido(String usuarioEmail, String motivo) {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) return; // No podemos registrar sin tenant

            HttpServletRequest request = getCurrentRequest();

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("motivo", motivo);
            metadata.put("success", false);

            AuditLogSimple log = AuditLogSimple.builder()
                    .tenantId(tenantId)
                    .usuarioEmail(usuarioEmail)
                    .tipoAccion(TipoAccionAudit.LOGIN_FAILED)
                    .descripcion("Intento de inicio de sesión fallido: " + motivo)
                    .ipAddress(request != null ? getClientIp(request) : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .metadata(metadata)
                    .build();

            auditLogSimpleRepository.save(log);
        } catch (Exception e) {
            log.error("Error al registrar login fallido: {}", e.getMessage());
        }
    }

    /**
     * Registra un logout
     */
    @Transactional
    public void registrarLogout() {
        Long tenantId = getTenantId();
        String usuarioEmail = SecurityUtils.getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        AuditLogSimple log = AuditLogSimple.builder()
                .tenantId(tenantId)
                .usuarioEmail(usuarioEmail)
                .tipoAccion(TipoAccionAudit.LOGOUT)
                .descripcion("Cierre de sesión")
                .ipAddress(request != null ? getClientIp(request) : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .build();

        auditLogSimpleRepository.save(log);
    }

    // ==================== AUDITORÍA CRÍTICA ====================

    /**
     * Registra un cambio crítico en una entidad
     */
    @Transactional
    public void registrarCambioCritico(String tabla, Long registroId, String campo,
                                        String valorAnterior, String valorNuevo,
                                        TipoOperacionAudit operacion, String motivo) {
        Long tenantId = getTenantId();
        String usuarioEmail = SecurityUtils.getCurrentUser();
        HttpServletRequest request = getCurrentRequest();

        AuditLogCritica log = AuditLogCritica.builder()
                .tenantId(tenantId)
                .usuarioEmail(usuarioEmail)
                .tabla(tabla)
                .registroId(registroId)
                .campo(campo)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .tipoOperacion(operacion)
                .motivo(motivo)
                .ipAddress(request != null ? getClientIp(request) : null)
                .build();

        // Solo guardar si realmente hubo un cambio
        if (log.hasChange()) {
            auditLogCriticaRepository.save(log);
            log.debug("Auditoría crítica registrada: {} - {} #{}", tabla, campo, registroId);
        }
    }

    /**
     * Registra múltiples cambios en una entidad (batch)
     */
    @Transactional
    public void registrarCambiosMultiples(String tabla, Long registroId,
                                           Map<String, String[]> cambios,
                                           TipoOperacionAudit operacion, String motivo) {
        for (Map.Entry<String, String[]> entry : cambios.entrySet()) {
            String campo = entry.getKey();
            String[] valores = entry.getValue(); // [valorAnterior, valorNuevo]

            if (valores.length == 2) {
                registrarCambioCritico(tabla, registroId, campo, valores[0], valores[1], operacion, motivo);
            }
        }
    }

    // ==================== CONSULTAS ====================

    /**
     * Obtiene logs simples con filtros
     */
    @Transactional(readOnly = true)
    public List<AuditLogSimpleResponse> obtenerLogsSimples(AuditFiltrosRequest filtros) {
        Long tenantId = getTenantId();
        LocalDateTime fechaInicio = filtros.getFechaInicio() != null ?
                filtros.getFechaInicio().atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime fechaFin = filtros.getFechaFin() != null ?
                filtros.getFechaFin().atTime(23, 59, 59) : LocalDateTime.now();

        List<AuditLogSimple> logs;

        if (filtros.getUsuarioId() != null || filtros.getAccion() != null) {
            logs = auditLogSimpleRepository.findConFiltros(
                    tenantId,
                    filtros.getUsuarioId(),
                    filtros.getAccion(),
                    fechaInicio,
                    fechaFin
            );
        } else {
            logs = auditLogSimpleRepository.findByRangoFechas(tenantId, fechaInicio, fechaFin);
        }

        // Aplicar límite si existe
        if (filtros.getLimit() != null && filtros.getLimit() > 0) {
            logs = logs.stream().limit(filtros.getLimit()).collect(Collectors.toList());
        }

        return logs.stream()
                .map(this::mapToSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene logs críticos con filtros
     */
    @Transactional(readOnly = true)
    public List<AuditLogCriticaResponse> obtenerLogsCriticos(AuditFiltrosRequest filtros) {
        Long tenantId = getTenantId();
        LocalDateTime fechaInicio = filtros.getFechaInicio() != null ?
                filtros.getFechaInicio().atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime fechaFin = filtros.getFechaFin() != null ?
                filtros.getFechaFin().atTime(23, 59, 59) : LocalDateTime.now();

        List<AuditLogCritica> logs;

        if (filtros.getUsuarioId() != null || filtros.getTabla() != null) {
            logs = auditLogCriticaRepository.findConFiltros(
                    tenantId,
                    filtros.getUsuarioId(),
                    filtros.getTabla(),
                    fechaInicio,
                    fechaFin
            );
        } else {
            logs = auditLogCriticaRepository.findByRangoFechas(tenantId, fechaInicio, fechaFin);
        }

        // Aplicar límite si existe
        if (filtros.getLimit() != null && filtros.getLimit() > 0) {
            logs = logs.stream().limit(filtros.getLimit()).collect(Collectors.toList());
        }

        return logs.stream()
                .map(this::mapToCriticaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene historial de cambios de un registro específico
     */
    @Transactional(readOnly = true)
    public List<AuditLogCriticaResponse> obtenerHistorialRegistro(String tabla, Long registroId) {
        Long tenantId = getTenantId();
        List<AuditLogCritica> logs = auditLogCriticaRepository.findByTablaYRegistro(tenantId, tabla, registroId);

        return logs.stream()
                .map(this::mapToCriticaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene historial de cambios de un campo específico
     */
    @Transactional(readOnly = true)
    public List<AuditLogCriticaResponse> obtenerHistorialCampo(String tabla, Long registroId, String campo) {
        Long tenantId = getTenantId();
        List<AuditLogCritica> logs = auditLogCriticaRepository.findHistorialCampo(tenantId, tabla, registroId, campo);

        return logs.stream()
                .map(this::mapToCriticaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene últimos logins de un usuario
     */
    @Transactional(readOnly = true)
    public List<AuditLogSimpleResponse> obtenerUltimosLogins(Long usuarioId, Integer limit) {
        Long tenantId = getTenantId();
        List<AuditLogSimple> logs = auditLogSimpleRepository.findUltimosLogins(tenantId, usuarioId, limit);

        return logs.stream()
                .map(this::mapToSimpleResponse)
                .collect(Collectors.toList());
    }

    // ==================== ARCHIVADO ====================

    /**
     * Archiva logs antiguos (> 1 año) y los elimina de las tablas principales
     */
    @Transactional
    public int archivarLogsAntiguos() {
        Long tenantId = getTenantId();
        LocalDateTime fechaLimite = LocalDateTime.now().minusYears(1);
        int totalArchivados = 0;

        log.info("Iniciando archivado de logs antiguos para tenant: {}", tenantId);

        // Archivar logs simples
        List<AuditLogSimple> logsSimples = auditLogSimpleRepository.findAntiguos(tenantId, fechaLimite);
        for (AuditLogSimple logSimple : logsSimples) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("accion", logSimple.getAccion());
            datos.put("descripcion", logSimple.getDescripcion());
            datos.put("usuario_email", logSimple.getUsuarioEmail());
            datos.put("ip_address", logSimple.getIpAddress());
            datos.put("metadata", logSimple.getMetadata());

            AuditLogArchive archive = AuditLogArchive.builder()
                    .tenantId(tenantId)
                    .tipo("SIMPLE")
                    .datos(datos)
                    .fechaOriginal(logSimple.getFecha())
                    .build();

            auditLogArchiveRepository.save(archive);
        }
        totalArchivados += logsSimples.size();

        // Archivar logs críticos
        List<AuditLogCritica> logsCriticos = auditLogCriticaRepository.findAntiguos(tenantId, fechaLimite);
        for (AuditLogCritica logCritica : logsCriticos) {
            Map<String, Object> datos = new HashMap<>();
            datos.put("tabla", logCritica.getTabla());
            datos.put("registro_id", logCritica.getRegistroId());
            datos.put("campo", logCritica.getCampo());
            datos.put("valor_anterior", logCritica.getValorAnterior());
            datos.put("valor_nuevo", logCritica.getValorNuevo());
            datos.put("operacion", logCritica.getOperacion());
            datos.put("usuario_email", logCritica.getUsuarioEmail());
            datos.put("motivo", logCritica.getMotivo());

            AuditLogArchive archive = AuditLogArchive.builder()
                    .tenantId(tenantId)
                    .tipo("CRITICA")
                    .datos(datos)
                    .fechaOriginal(logCritica.getFecha())
                    .build();

            auditLogArchiveRepository.save(archive);
        }
        totalArchivados += logsCriticos.size();

        // Eliminar logs archivados
        int deletedSimples = auditLogSimpleRepository.deleteAntiguos(tenantId, fechaLimite);
        int deletedCriticos = auditLogCriticaRepository.deleteAntiguos(tenantId, fechaLimite);

        log.info("Archivado completado: {} logs archivados, {} simples eliminados, {} críticos eliminados",
                totalArchivados, deletedSimples, deletedCriticos);

        return totalArchivados;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private AuditLogSimpleResponse mapToSimpleResponse(AuditLogSimple log) {
        TipoAccionAudit tipoAccion = null;
        String descripcionAccion = null;
        try {
            tipoAccion = TipoAccionAudit.valueOf(log.getAccion());
            descripcionAccion = tipoAccion.getDescripcion();
        } catch (IllegalArgumentException e) {
            // Acción no mapeada, usar el valor del string
        }

        return AuditLogSimpleResponse.builder()
                .id(log.getId())
                .tenantId(log.getTenantId())
                .usuarioId(log.getUsuarioId())
                .usuarioEmail(log.getUsuarioEmail())
                .accion(log.getAccion())
                .accionDescripcion(descripcionAccion)
                .descripcion(log.getDescripcion())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .metadata(log.getMetadata())
                .fecha(log.getFecha())
                .build();
    }

    private AuditLogCriticaResponse mapToCriticaResponse(AuditLogCritica log) {
        TipoOperacionAudit tipoOperacion = null;
        String descripcionOperacion = null;
        try {
            tipoOperacion = TipoOperacionAudit.valueOf(log.getOperacion());
            descripcionOperacion = tipoOperacion.getDescripcion();
        } catch (IllegalArgumentException e) {
            // Operación no mapeada
        }

        return AuditLogCriticaResponse.builder()
                .id(log.getId())
                .tenantId(log.getTenantId())
                .usuarioId(log.getUsuarioId())
                .usuarioEmail(log.getUsuarioEmail())
                .tabla(log.getTabla())
                .registroId(log.getRegistroId())
                .campo(log.getCampo())
                .valorAnterior(log.getValorAnterior())
                .valorNuevo(log.getValorNuevo())
                .operacion(log.getOperacion())
                .operacionDescripcion(descripcionOperacion)
                .motivo(log.getMotivo())
                .ipAddress(log.getIpAddress())
                .fecha(log.getFecha())
                .build();
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

    private String getClientIp(HttpServletRequest request) {
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
                // Tomar la primera IP si hay múltiples
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
