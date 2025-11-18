package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Amortizacion;
import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Amortizacion
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface AmortizacionRepository extends JpaRepository<Amortizacion, Long> {

    /**
     * Busca todas las amortizaciones de un tenant
     */
    List<Amortizacion> findByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Busca una amortización por ID y tenant
     */
    Optional<Amortizacion> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca todas las amortizaciones de un plan de pago ordenadas por número de cuota
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.deleted = false " +
           "ORDER BY a.numeroCuota ASC")
    List<Amortizacion> findByPlanPagoId(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Busca una amortización específica por plan de pago y número de cuota
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.numeroCuota = :numeroCuota " +
           "AND a.deleted = false")
    Optional<Amortizacion> findByPlanPagoIdYNumeroCuota(@Param("tenantId") Long tenantId,
                                                          @Param("planPagoId") Long planPagoId,
                                                          @Param("numeroCuota") Integer numeroCuota);

    /**
     * Busca amortizaciones por estado
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.estado = :estado " +
           "AND a.deleted = false " +
           "ORDER BY a.numeroCuota ASC")
    List<Amortizacion> findByPlanPagoIdYEstado(@Param("tenantId") Long tenantId,
                                                 @Param("planPagoId") Long planPagoId,
                                                 @Param("estado") EstadoAmortizacion estado);

    /**
     * Busca amortizaciones pendientes de un plan de pago
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.estado = 'PENDIENTE' " +
           "AND a.deleted = false " +
           "ORDER BY a.numeroCuota ASC")
    List<Amortizacion> findPendientesByPlanPagoId(@Param("tenantId") Long tenantId,
                                                    @Param("planPagoId") Long planPagoId);

    /**
     * Busca amortizaciones vencidas de un plan de pago
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.estado = 'VENCIDO' " +
           "AND a.deleted = false " +
           "ORDER BY a.fechaVencimiento ASC")
    List<Amortizacion> findVencidasByPlanPagoId(@Param("tenantId") Long tenantId,
                                                  @Param("planPagoId") Long planPagoId);

    /**
     * Busca amortizaciones que vencen en una fecha específica o antes
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.fechaVencimiento <= :fecha " +
           "AND a.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADO') " +
           "AND a.deleted = false " +
           "ORDER BY a.fechaVencimiento ASC")
    List<Amortizacion> findVencidasHastaFecha(@Param("tenantId") Long tenantId,
                                                @Param("planPagoId") Long planPagoId,
                                                @Param("fecha") LocalDate fecha);

    /**
     * Busca las próximas N amortizaciones pendientes
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.estado = 'PENDIENTE' " +
           "AND a.deleted = false " +
           "ORDER BY a.numeroCuota ASC " +
           "LIMIT :limite")
    List<Amortizacion> findProximasPendientes(@Param("tenantId") Long tenantId,
                                                @Param("planPagoId") Long planPagoId,
                                                @Param("limite") Integer limite);

    /**
     * Busca la primera amortización pendiente
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADO') " +
           "AND a.deleted = false " +
           "ORDER BY a.numeroCuota ASC " +
           "LIMIT 1")
    Optional<Amortizacion> findPrimeraAmortizacionPendiente(@Param("tenantId") Long tenantId,
                                                              @Param("planPagoId") Long planPagoId);

    /**
     * Cuenta total de amortizaciones de un plan de pago
     */
    @Query("SELECT COUNT(a) FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.deleted = false")
    Long contarByPlanPagoId(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Cuenta amortizaciones por estado
     */
    @Query("SELECT COUNT(a) FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.estado = :estado " +
           "AND a.deleted = false")
    Long contarByEstado(@Param("tenantId") Long tenantId,
                         @Param("planPagoId") Long planPagoId,
                         @Param("estado") EstadoAmortizacion estado);

    /**
     * Suma total pagado en un plan de pago
     */
    @Query("SELECT COALESCE(SUM(a.montoPagado), 0) FROM Amortizacion a " +
           "WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.deleted = false")
    BigDecimal sumarTotalPagado(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Suma total pendiente en un plan de pago
     */
    @Query("SELECT COALESCE(SUM(a.montoPendiente), 0) FROM Amortizacion a " +
           "WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.deleted = false")
    BigDecimal sumarTotalPendiente(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Suma mora acumulada en un plan de pago
     */
    @Query("SELECT COALESCE(SUM(a.moraAcumulada), 0) FROM Amortizacion a " +
           "WHERE a.tenantId = :tenantId " +
           "AND a.planPagoId = :planPagoId " +
           "AND a.deleted = false")
    BigDecimal sumarMoraAcumulada(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Busca amortizaciones que vencen en los próximos N días (para notificaciones)
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.estado = 'PENDIENTE' " +
           "AND a.fechaVencimiento BETWEEN CURRENT_DATE AND CURRENT_DATE + :dias " +
           "AND a.deleted = false " +
           "ORDER BY a.fechaVencimiento ASC")
    List<Amortizacion> findProximasAVencer(@Param("tenantId") Long tenantId, @Param("dias") Integer dias);

    /**
     * Busca todas las amortizaciones vencidas de un tenant (para proceso batch)
     */
    @Query("SELECT a FROM Amortizacion a WHERE a.tenantId = :tenantId " +
           "AND a.fechaVencimiento < CURRENT_DATE " +
           "AND a.estado IN ('PENDIENTE', 'PARCIALMENTE_PAGADO') " +
           "AND a.deleted = false")
    List<Amortizacion> findTodasVencidas(@Param("tenantId") Long tenantId);
}
