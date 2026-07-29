package com.inmobiliaria.terrenos.infrastructure.scheduler;

import com.inmobiliaria.terrenos.application.service.AuditService;
import com.inmobiliaria.terrenos.domain.entity.Tenant;
import com.inmobiliaria.terrenos.domain.repository.TenantRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler para archivado automático de logs de auditoría
 *
 * @author Kevin
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditArchiveScheduler {

    private final AuditService auditService;
    private final TenantRepository tenantRepository;

    /**
     * Ejecuta el archivado de logs antiguos cada noche a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void archivarLogsAutomaticamente() {
        log.info("=== Iniciando proceso automático de archivado de logs ===");

        List<Tenant> tenants = tenantRepository.findByActivo(true);
        int totalArchivados = 0;

        for (Tenant tenant : tenants) {
            try {
                TenantContext.setTenantId(tenant.getId());
                int archivados = auditService.archivarLogsAntiguos();
                totalArchivados += archivados;
                log.debug("Tenant {}: {} logs archivados", tenant.getId(), archivados);
            } catch (Exception e) {
                log.error("Error archivando logs para tenant {}: {}", tenant.getId(), e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }

        log.info("=== Proceso de archivado completado: {} logs archivados en {} tenants ===",
                totalArchivados, tenants.size());
    }

    /**
     * Prueba: ejecutar cada 5 minutos durante desarrollo
     */
    // @Scheduled(fixedDelay = 300000)
    // public void archivarLogsTest() {
    //     log.info("TEST: Ejecutando archivado de prueba");
    //     archivarLogsAutomaticamente();
    // }
}
