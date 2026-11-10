package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio centralizado para recalcular y sincronizar contadores de Proyecto (SRP & DRY).
 * Elimina la duplicación de código en TerrenoService, ApartadoService, VentaService y PlanoIngestaService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProyectoContadorService {

    private final ProyectoRepository proyectoRepository;
    private final TerrenoRepository terrenoRepository;

    @Transactional
    public void sincronizarContadores(Long proyectoId) {
        if (proyectoId == null || proyectoRepository == null) {
            return;
        }
        proyectoRepository.findById(proyectoId)
                .filter(p -> p.getDeleted() == null || !p.getDeleted())
                .ifPresent(this::sincronizarContadores);
    }

    @Transactional
    public void sincronizarContadores(Proyecto proyecto) {
        if (proyecto == null || terrenoRepository == null) {
            return;
        }
        Long tenantId = proyecto.getTenantId();
        Long proyectoId = proyecto.getId();

        long totalTerrenos = terrenoRepository.countByTenantIdAndProyectoIdAndDeletedFalse(tenantId, proyectoId);
        long disponibles = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.DISPONIBLE);
        long apartados = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.APARTADO);
        long vendidos = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.VENDIDO);

        proyecto.setTotalTerrenos((int) totalTerrenos);
        proyecto.setTerrenosDisponibles((int) disponibles);
        proyecto.setTerrenosApartados((int) apartados);
        proyecto.setTerrenosVendidos((int) vendidos);

        proyectoRepository.save(proyecto);
        log.debug("Contadores sincronizados para proyecto {}: total={}, disp={}, apart={}, vend={}",
                proyectoId, totalTerrenos, disponibles, apartados, vendidos);
    }
}
