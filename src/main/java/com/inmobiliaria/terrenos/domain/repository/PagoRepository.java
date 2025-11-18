package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Pago;
import com.inmobiliaria.terrenos.domain.enums.EstadoPago;
import com.inmobiliaria.terrenos.domain.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Busca todos los pagos de un tenant
     */
    List<Pago> findByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Busca un pago por ID y tenant
     */
    Optional<Pago> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca todos los pagos de un plan de pago ordenados por fecha
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findByPlanPagoId(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Busca pagos de una amortización específica
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.amortizacionId = :amortizacionId " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findByAmortizacionId(@Param("tenantId") Long tenantId, @Param("amortizacionId") Long amortizacionId);

    /**
     * Busca pagos de un cliente
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.clienteId = :clienteId " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findByClienteId(@Param("tenantId") Long tenantId, @Param("clienteId") Long clienteId);

    /**
     * Busca pagos por estado
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.estado = :estado " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findByEstado(@Param("tenantId") Long tenantId, @Param("estado") EstadoPago estado);

    /**
     * Busca pagos aplicados de un plan de pago
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.estado = 'APLICADO' " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findPagosAplicados(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Busca pagos por método de pago
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.metodoPago = :metodoPago " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findByMetodoPago(@Param("tenantId") Long tenantId, @Param("metodoPago") MetodoPago metodoPago);

    /**
     * Busca pagos en un rango de fechas
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findByRangoFechas(@Param("tenantId") Long tenantId,
                                   @Param("fechaInicio") LocalDate fechaInicio,
                                   @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca pagos recientes (últimos N días)
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.deleted = false " +
           "AND p.createdAt >= CURRENT_DATE - :dias " +
           "ORDER BY p.createdAt DESC")
    List<Pago> findPagosRecientes(@Param("tenantId") Long tenantId, @Param("dias") Integer dias);

    /**
     * Busca pagos por usuario que los registró
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.usuarioId = :usuarioId " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> findByUsuarioId(@Param("tenantId") Long tenantId, @Param("usuarioId") Long usuarioId);

    /**
     * Cuenta total de pagos de un plan de pago
     */
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.deleted = false")
    Long contarByPlanPagoId(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Cuenta pagos por estado
     */
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.estado = :estado " +
           "AND p.deleted = false")
    Long contarByEstado(@Param("tenantId") Long tenantId, @Param("estado") EstadoPago estado);

    /**
     * Suma total de pagos aplicados de un plan de pago
     */
    @Query("SELECT COALESCE(SUM(p.montoPagado), 0) FROM Pago p " +
           "WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.estado = 'APLICADO' " +
           "AND p.deleted = false")
    BigDecimal sumarTotalPagado(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Suma total de pagos a capital
     */
    @Query("SELECT COALESCE(SUM(p.montoACapital), 0) FROM Pago p " +
           "WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.estado = 'APLICADO' " +
           "AND p.deleted = false")
    BigDecimal sumarTotalCapital(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Suma total de pagos a interés
     */
    @Query("SELECT COALESCE(SUM(p.montoAInteres), 0) FROM Pago p " +
           "WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.estado = 'APLICADO' " +
           "AND p.deleted = false")
    BigDecimal sumarTotalInteres(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Suma total de pagos a mora
     */
    @Query("SELECT COALESCE(SUM(p.montoAMora), 0) FROM Pago p " +
           "WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.estado = 'APLICADO' " +
           "AND p.deleted = false")
    BigDecimal sumarTotalMora(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);

    /**
     * Suma total de pagos en un rango de fechas (para reportes)
     */
    @Query("SELECT COALESCE(SUM(p.montoPagado), 0) FROM Pago p " +
           "WHERE p.tenantId = :tenantId " +
           "AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin " +
           "AND p.estado = 'APLICADO' " +
           "AND p.deleted = false")
    BigDecimal sumarPagosEnRango(@Param("tenantId") Long tenantId,
                                  @Param("fechaInicio") LocalDate fechaInicio,
                                  @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca pagos por referencia (para búsqueda de comprobantes)
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND LOWER(p.referenciaPago) LIKE LOWER(CONCAT('%', :referencia, '%')) " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC")
    List<Pago> buscarPorReferencia(@Param("tenantId") Long tenantId, @Param("referencia") String referencia);

    /**
     * Busca el último pago registrado para un plan de pago
     */
    @Query("SELECT p FROM Pago p WHERE p.tenantId = :tenantId " +
           "AND p.planPagoId = :planPagoId " +
           "AND p.deleted = false " +
           "ORDER BY p.fechaPago DESC, p.createdAt DESC " +
           "LIMIT 1")
    Optional<Pago> findUltimoPago(@Param("tenantId") Long tenantId, @Param("planPagoId") Long planPagoId);
}
