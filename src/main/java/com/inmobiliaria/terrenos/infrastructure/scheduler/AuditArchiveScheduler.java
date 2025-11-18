package com.inmobiliaria.terrenos.infrastructure.scheduler;

import com.inmobiliaria.terrenos.application.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    /**
     * Ejecuta el archivado de logs antiguos cada noche a las 2:00 AM
     * Cron: segundo minuto hora día mes día-semana
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void archivarLogsAutomaticamente() {
        log.info("=== Iniciando proceso automático de archivado de logs ===");

        try {
            // Note: Este proceso se ejecutará para todos los tenants
            // En un ambiente multi-tenant productivo, deberías iterar sobre todos los tenants
            int totalArchivados = auditService.archivarLogsAntiguos();

            log.info("=== Proceso de archivado completado exitosamente ===");
            log.info("Total de logs archivados: {}", totalArchivados);
        } catch (Exception e) {
            log.error("Error durante el proceso automático de archivado: {}", e.getMessage(), e);
        }
    }

    /**
     * Puedes descomentar esto para probar el archivado cada 5 minutos durante desarrollo
     */
    // @Scheduled(fixedDelay = 300000) // Cada 5 minutos
    // public void archivarLogsTest() {
    //     log.info("TEST: Ejecutando archivado de prueba");
    //     archivarLogsAutomaticamente();
    // }
}
