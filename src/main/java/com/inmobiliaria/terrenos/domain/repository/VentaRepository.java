package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Venta;
import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Venta
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByTenantIdAndDeletedFalse(Long tenantId);

    Optional<Venta> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    List<Venta> findByTenantIdAndTerrenoIdAndDeletedFalse(Long tenantId, Long terrenoId);

    List<Venta> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoVenta estado);

    /**
     * Busca ventas por comprador
     */
    @Query("SELECT v FROM Venta v WHERE v.tenantId = :tenantId " +
           "AND LOWER(v.compradorNombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "AND v.deleted = false " +
           "ORDER BY v.fechaVenta DESC")
    List<Venta> findByCompradorNombre(@Param("tenantId") Long tenantId, @Param("nombre") String nombre);

    /**
     * Busca ventas por vendedor
     */
    List<Venta> findByTenantIdAndUsuarioIdAndDeletedFalseOrderByFechaVentaDesc(Long tenantId, Long usuarioId);

    /**
     * Busca ventas por rango de fechas
     */
    @Query("SELECT v FROM Venta v WHERE v.tenantId = :tenantId " +
           "AND v.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
           "AND v.deleted = false " +
           "ORDER BY v.fechaVenta DESC")
    List<Venta> findByRangoFechas(@Param("tenantId") Long tenantId,
                                   @Param("fechaInicio") LocalDate fechaInicio,
                                   @Param("fechaFin") LocalDate fechaFin);

    /**
     * Suma total de ventas por tenant
     */
    @Query("SELECT COALESCE(SUM(v.montoFinal), 0) FROM Venta v " +
           "WHERE v.tenantId = :tenantId " +
           "AND v.deleted = false")
    BigDecimal sumTotalVentas(@Param("tenantId") Long tenantId);

    /**
     * Suma total de comisiones
     */
    @Query("SELECT COALESCE(SUM(v.montoComision), 0) FROM Venta v " +
           "WHERE v.tenantId = :tenantId " +
           "AND v.deleted = false")
    BigDecimal sumTotalComisiones(@Param("tenantId") Long tenantId);

    /**
     * Cuenta ventas por estado
     */
    long countByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoVenta estado);

    long countByTenantIdAndDeletedFalse(Long tenantId);
}
