package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.PlanPago;
import com.inmobiliaria.terrenos.domain.enums.TipoPlanPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad PlanPago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {

    /**
     * Busca todos los planes de pago de un tenant
     */
    List<PlanPago> findByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Busca un plan de pago por ID y tenant
     */
    Optional<PlanPago> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca el plan de pago de una venta específica
     */
    Optional<PlanPago> findByTenantIdAndVentaIdAndDeletedFalse(Long tenantId, Long ventaId);

    /**
     * Busca todos los planes de pago de un cliente
     */
    @Query("SELECT pp FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.clienteId = :clienteId " +
           "AND pp.deleted = false " +
           "ORDER BY pp.createdAt DESC")
    List<PlanPago> findByClienteId(@Param("tenantId") Long tenantId, @Param("clienteId") Long clienteId);

    /**
     * Busca planes de pago por tipo
     */
    List<PlanPago> findByTenantIdAndTipoPlanAndDeletedFalse(Long tenantId, TipoPlanPago tipoPlan);

    /**
     * Busca planes de pago con interés
     */
    @Query("SELECT pp FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.aplicaInteres = true " +
           "AND pp.deleted = false")
    List<PlanPago> findPlanesConInteres(@Param("tenantId") Long tenantId);

    /**
     * Busca planes de pago sin interés
     */
    @Query("SELECT pp FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.aplicaInteres = false " +
           "AND pp.deleted = false")
    List<PlanPago> findPlanesSinInteres(@Param("tenantId") Long tenantId);

    /**
     * Busca planes de pago creados en un rango de fechas
     */
    @Query("SELECT pp FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.fechaInicio BETWEEN :fechaInicio AND :fechaFin " +
           "AND pp.deleted = false " +
           "ORDER BY pp.fechaInicio DESC")
    List<PlanPago> findByRangoFechas(@Param("tenantId") Long tenantId,
                                      @Param("fechaInicio") LocalDate fechaInicio,
                                      @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca planes de pago activos (que aún tienen pagos pendientes)
     */
    @Query("SELECT pp FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.fechaUltimoPago >= :fecha " +
           "AND pp.deleted = false " +
           "ORDER BY pp.fechaPrimerPago ASC")
    List<PlanPago> findPlanesActivos(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    /**
     * Busca planes de pago finalizados
     */
    @Query("SELECT pp FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.fechaUltimoPago < :fecha " +
           "AND pp.deleted = false " +
           "ORDER BY pp.fechaUltimoPago DESC")
    List<PlanPago> findPlanesFinalizados(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    /**
     * Cuenta total de planes de pago por tenant
     */
    @Query("SELECT COUNT(pp) FROM PlanPago pp WHERE pp.tenantId = :tenantId AND pp.deleted = false")
    Long contarPlanesPago(@Param("tenantId") Long tenantId);

    /**
     * Cuenta planes de pago por tipo
     */
    @Query("SELECT COUNT(pp) FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.tipoPlan = :tipoPlan " +
           "AND pp.deleted = false")
    Long contarPorTipo(@Param("tenantId") Long tenantId, @Param("tipoPlan") TipoPlanPago tipoPlan);

    /**
     * Verifica si existe un plan de pago para una venta (para validación)
     */
    @Query("SELECT COUNT(pp) > 0 FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.ventaId = :ventaId " +
           "AND pp.deleted = false")
    boolean existePlanParaVenta(@Param("tenantId") Long tenantId, @Param("ventaId") Long ventaId);

    /**
     * Busca planes de pago recientes (últimos N días)
     */
    @Query("SELECT pp FROM PlanPago pp WHERE pp.tenantId = :tenantId " +
           "AND pp.deleted = false " +
           "AND pp.createdAt >= CURRENT_DATE - :dias " +
           "ORDER BY pp.createdAt DESC")
    List<PlanPago> findPlanesRecientes(@Param("tenantId") Long tenantId, @Param("dias") Integer dias);
}
