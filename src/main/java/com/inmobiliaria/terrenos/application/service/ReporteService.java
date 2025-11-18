package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.reporte.DashboardResponse;
import com.inmobiliaria.terrenos.application.dto.reporte.ProyectoEstadisticasResponse;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.*;
import com.inmobiliaria.terrenos.domain.repository.*;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de generación de reportes y estadísticas
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteService {

    private final ProyectoRepository proyectoRepository;
    private final TerrenoRepository terrenoRepository;
    private final CotizacionRepository cotizacionRepository;
    private final ApartadoRepository apartadoRepository;
    private final VentaRepository ventaRepository;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    /**
     * Obtiene las estadísticas del dashboard principal
     */
    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard() {
        Long tenantId = getTenantId();
        log.debug("Generando dashboard para tenant: {}", tenantId);

        // Estadísticas de proyectos
        List<Proyecto> proyectos = proyectoRepository.findByTenantIdAndDeletedFalse(tenantId);
        long totalProyectos = proyectos.size();
        long proyectosActivos = proyectos.stream()
                .filter(p -> p.getEstado() == EstadoProyecto.EN_VENTA)
                .count();
        long proyectosFinalizados = proyectos.stream()
                .filter(p -> p.getEstado() == EstadoProyecto.FINALIZADO)
                .count();

        // Estadísticas de terrenos
        List<com.inmobiliaria.terrenos.domain.entity.Terreno> terrenos =
                terrenoRepository.findByTenantIdAndDeletedFalse(tenantId);
        long totalTerrenos = terrenos.size();
        long terrenosDisponibles = terrenos.stream()
                .filter(t -> t.getEstado() == EstadoTerreno.DISPONIBLE)
                .count();
        long terrenosApartados = terrenos.stream()
                .filter(t -> t.getEstado() == EstadoTerreno.APARTADO)
                .count();
        long terrenosVendidos = terrenos.stream()
                .filter(t -> t.getEstado() == EstadoTerreno.VENDIDO)
                .count();

        BigDecimal porcentajeOcupacion = totalTerrenos > 0 ?
                BigDecimal.valueOf((terrenosApartados + terrenosVendidos) * 100.0 / totalTerrenos)
                        .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // Estadísticas de cotizaciones
        long totalCotizaciones = cotizacionRepository.countByTenantIdAndDeletedFalse(tenantId);
        long cotizacionesVigentes = cotizacionRepository
                .findCotizacionesVigentes(tenantId, java.time.LocalDate.now()).size();

        // Estadísticas de apartados
        long totalApartados = apartadoRepository.countByTenantIdAndEstadoAndDeletedFalse(
                tenantId, EstadoApartado.VIGENTE);
        long apartadosVigentes = apartadoRepository
                .findApartadosVigentes(tenantId, java.time.LocalDate.now()).size();
        long apartadosVencidos = apartadoRepository
                .findApartadosVencidos(tenantId, java.time.LocalDate.now()).size();

        // Estadísticas de ventas
        long totalVentas = ventaRepository.countByTenantIdAndDeletedFalse(tenantId);
        long ventasPendientes = ventaRepository.countByTenantIdAndEstadoAndDeletedFalse(
                tenantId, EstadoVenta.PENDIENTE);
        long ventasPagadas = ventaRepository.countByTenantIdAndEstadoAndDeletedFalse(
                tenantId, EstadoVenta.PAGADO);
        BigDecimal montoTotalVentas = ventaRepository.sumTotalVentas(tenantId);
        BigDecimal montoTotalComisiones = ventaRepository.sumTotalComisiones(tenantId);

        // Métricas calculadas
        BigDecimal ticketPromedio = totalVentas > 0 ?
                montoTotalVentas.divide(BigDecimal.valueOf(totalVentas), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal tasaConversion = totalCotizaciones > 0 ?
                BigDecimal.valueOf(totalVentas * 100.0 / totalCotizaciones)
                        .setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return DashboardResponse.builder()
                .totalProyectos(totalProyectos)
                .proyectosActivos(proyectosActivos)
                .proyectosFinalizados(proyectosFinalizados)
                .totalTerrenos(totalTerrenos)
                .terrenosDisponibles(terrenosDisponibles)
                .terrenosApartados(terrenosApartados)
                .terrenosVendidos(terrenosVendidos)
                .porcentajeOcupacion(porcentajeOcupacion)
                .totalCotizaciones(totalCotizaciones)
                .cotizacionesVigentes(cotizacionesVigentes)
                .totalApartados(totalApartados)
                .apartadosVigentes(apartadosVigentes)
                .apartadosVencidos(apartadosVencidos)
                .totalVentas(totalVentas)
                .ventasPendientes(ventasPendientes)
                .ventasPagadas(ventasPagadas)
                .montoTotalVentas(montoTotalVentas)
                .montoTotalComisiones(montoTotalComisiones)
                .ticketPromedio(ticketPromedio)
                .tasaConversion(tasaConversion)
                .build();
    }

    /**
     * Obtiene estadísticas detalladas por proyecto
     */
    @Transactional(readOnly = true)
    public List<ProyectoEstadisticasResponse> obtenerEstadisticasPorProyecto() {
        Long tenantId = getTenantId();
        log.debug("Generando estadísticas por proyecto para tenant: {}", tenantId);

        List<Proyecto> proyectos = proyectoRepository.findByTenantIdAndDeletedFalse(tenantId);
        List<ProyectoEstadisticasResponse> estadisticas = new ArrayList<>();

        for (Proyecto proyecto : proyectos) {
            // Obtener terrenos del proyecto
            List<com.inmobiliaria.terrenos.domain.entity.Terreno> terrenos =
                    terrenoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(
                            tenantId, proyecto.getId());

            int totalTerrenos = terrenos.size();
            int disponibles = (int) terrenos.stream()
                    .filter(t -> t.getEstado() == EstadoTerreno.DISPONIBLE)
                    .count();
            int apartados = (int) terrenos.stream()
                    .filter(t -> t.getEstado() == EstadoTerreno.APARTADO)
                    .count();
            int vendidos = (int) terrenos.stream()
                    .filter(t -> t.getEstado() == EstadoTerreno.VENDIDO)
                    .count();

            // Calcular porcentajes
            BigDecimal porcentajeOcupacion = totalTerrenos > 0 ?
                    BigDecimal.valueOf((apartados + vendidos) * 100.0 / totalTerrenos)
                            .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            BigDecimal porcentajeDisponibilidad = totalTerrenos > 0 ?
                    BigDecimal.valueOf(disponibles * 100.0 / totalTerrenos)
                            .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            // Obtener ventas del proyecto
            List<com.inmobiliaria.terrenos.domain.entity.Venta> ventas =
                    terrenoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(
                                    tenantId, proyecto.getId()).stream()
                            .flatMap(t -> ventaRepository.findByTenantIdAndTerrenoIdAndDeletedFalse(
                                    tenantId, t.getId()).stream())
                            .toList();

            long numeroVentas = ventas.size();
            BigDecimal montoTotalVentas = ventas.stream()
                    .map(com.inmobiliaria.terrenos.domain.entity.Venta::getMontoFinal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal ticketPromedio = numeroVentas > 0 ?
                    montoTotalVentas.divide(BigDecimal.valueOf(numeroVentas), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            estadisticas.add(ProyectoEstadisticasResponse.builder()
                    .proyectoId(proyecto.getId())
                    .proyectoNombre(proyecto.getNombre())
                    .totalTerrenos(totalTerrenos)
                    .terrenosDisponibles(disponibles)
                    .terrenosApartados(apartados)
                    .terrenosVendidos(vendidos)
                    .porcentajeOcupacion(porcentajeOcupacion)
                    .porcentajeDisponibilidad(porcentajeDisponibilidad)
                    .montoTotalVentas(montoTotalVentas)
                    .numeroVentas(numeroVentas)
                    .ticketPromedio(ticketPromedio)
                    .build());
        }

        return estadisticas;
    }

    /**
     * Obtiene estadísticas de un proyecto específico
     */
    @Transactional(readOnly = true)
    public ProyectoEstadisticasResponse obtenerEstadisticasProyecto(Long proyectoId) {
        Long tenantId = getTenantId();
        log.debug("Generando estadísticas del proyecto {} para tenant: {}", proyectoId, tenantId);

        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + proyectoId));

        List<ProyectoEstadisticasResponse> todas = obtenerEstadisticasPorProyecto();
        return todas.stream()
                .filter(e -> e.getProyectoId().equals(proyectoId))
                .findFirst()
                .orElse(ProyectoEstadisticasResponse.builder()
                        .proyectoId(proyectoId)
                        .proyectoNombre(proyecto.getNombre())
                        .totalTerrenos(0)
                        .terrenosDisponibles(0)
                        .terrenosApartados(0)
                        .terrenosVendidos(0)
                        .porcentajeOcupacion(BigDecimal.ZERO)
                        .porcentajeDisponibilidad(BigDecimal.ZERO)
                        .montoTotalVentas(BigDecimal.ZERO)
                        .numeroVentas(0L)
                        .ticketPromedio(BigDecimal.ZERO)
                        .build());
    }
}
