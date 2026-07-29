package com.inmobiliaria.terrenos.infrastructure.scheduler;

import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Tenant;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TenantRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApartadoVencimientoScheduler {

    private final TenantRepository tenantRepository;
    private final ApartadoRepository apartadoRepository;
    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;

    @Scheduled(cron = "0 0 1 * * *")
    public void procesarApartadosVencidos() {
        log.info("=== Iniciando procesamiento de apartados vencidos ===");

        List<Tenant> tenants = tenantRepository.findByActivo(true);
        int totalProcesados = 0;

        for (Tenant tenant : tenants) {
            try {
                TenantContext.setTenantId(tenant.getId());
                int procesados = procesarApartadosPorTenant(tenant.getId());
                totalProcesados += procesados;
            } catch (Exception e) {
                log.error("Error procesando apartados para tenant {}: {}", tenant.getId(), e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }

        log.info("=== Procesamiento completado: {} apartados vencidos procesados en {} tenants ===",
                totalProcesados, tenants.size());
    }

    @Transactional
    protected int procesarApartadosPorTenant(Long tenantId) {
        LocalDate hoy = LocalDate.now();
        List<Apartado> vencidos = apartadoRepository.findApartadosVencidos(tenantId, hoy);

        if (vencidos.isEmpty()) {
            log.debug("No hay apartados vencidos para tenant: {}", tenantId);
            return 0;
        }

        log.info("Procesando {} apartados vencidos para tenant: {}", vencidos.size(), tenantId);

        for (Apartado apartado : vencidos) {
            try {
                apartado.setEstado(EstadoApartado.VENCIDO);

                Terreno terreno = terrenoRepository.findById(apartado.getTerrenoId()).orElse(null);
                if (terreno != null && terreno.getEstado() == EstadoTerreno.APARTADO) {
                    terreno.setEstado(EstadoTerreno.DISPONIBLE);
                    terrenoRepository.save(terreno);

                    actualizarContadoresProyecto(terreno.getProyectoId());
                }

                apartadoRepository.save(apartado);
                log.debug("Apartado {} vencido, terreno {} liberado", apartado.getId(), apartado.getTerrenoId());
            } catch (Exception e) {
                log.error("Error procesando apartado {}: {}", apartado.getId(), e.getMessage());
            }
        }

        return vencidos.size();
    }

    private void actualizarContadoresProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null || proyecto.getDeleted()) return;

        Long tenantId = proyecto.getTenantId();
        long total = terrenoRepository.countByTenantIdAndProyectoIdAndDeletedFalse(tenantId, proyectoId);
        long disponibles = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.DISPONIBLE);
        long apartados = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.APARTADO);
        long vendidos = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.VENDIDO);

        proyecto.setTotalTerrenos((int) total);
        proyecto.setTerrenosDisponibles((int) disponibles);
        proyecto.setTerrenosApartados((int) apartados);
        proyecto.setTerrenosVendidos((int) vendidos);

        proyectoRepository.save(proyecto);
    }
}
