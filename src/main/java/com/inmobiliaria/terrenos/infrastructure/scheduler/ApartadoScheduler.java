package com.inmobiliaria.terrenos.infrastructure.scheduler;

import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler para verificar y expirar automáticamente apartados cuya fecha de vencimiento ha pasado.
 * Libera la disponibilidad de los terrenos asociados.
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 1.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApartadoScheduler {

    private final ApartadoRepository apartadoRepository;
    private final TerrenoRepository terrenoRepository;

    /**
     * Revisa cada hora si existen apartados vencidos y los marca como VENCIDO,
     * liberando los terrenos para ser apartados o vendidos de nuevo.
     */
    @Scheduled(cron = "${app.scheduling.apartados-cron:0 0 * * * *}")
    @Transactional
    public void procesarApartadosVencidos() {
        log.info("=== [Dev A] Ejecutando scheduler de vencimiento de apartados ===");

        LocalDate hoy = LocalDate.now();
        List<Apartado> apartadosVencidos = apartadoRepository
                .findByEstadoAndFechaVencimientoBeforeAndDeletedFalse(EstadoApartado.ACTIVO, hoy);

        if (apartadosVencidos.isEmpty()) {
            log.info("No se encontraron apartados vencidos pendientes de procesar.");
            return;
        }

        int apartadosProcesados = 0;
        for (Apartado apartado : apartadosVencidos) {
            try {
                apartado.setEstado(EstadoApartado.VENCIDO);
                apartado.setMotivoCancelacion("Expiración automática por fecha de vencimiento alcanzada (" + apartado.getFechaVencimiento() + ")");
                apartadoRepository.save(apartado);

                // Liberar el terreno asociado si aún figura como APARTADO
                if (apartado.getTerrenoId() != null) {
                    terrenoRepository.findById(apartado.getTerrenoId()).ifPresent(terreno -> {
                        if (terreno.getEstado() == EstadoTerreno.APARTADO) {
                            terreno.setEstado(EstadoTerreno.DISPONIBLE);
                            terrenoRepository.save(terreno);
                            log.info("Terreno ID {} (Lote {}) liberado a DISPONIBLE por vencimiento de apartado ID {}",
                                    terreno.getId(), terreno.getNumeroLote(), apartado.getId());
                        }
                    });
                }
                apartadosProcesados++;
            } catch (Exception e) {
                log.error("Error procesando vencimiento de apartado ID {}: {}", apartado.getId(), e.getMessage(), e);
            }
        }

        log.info("=== [Dev A] Vencimiento de apartados finalizado. Total procesados: {} ===", apartadosProcesados);
    }
}
