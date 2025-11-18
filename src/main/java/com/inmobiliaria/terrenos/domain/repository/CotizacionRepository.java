package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Cotizacion
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    List<Cotizacion> findByTenantIdAndDeletedFalse(Long tenantId);

    Optional<Cotizacion> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    List<Cotizacion> findByTenantIdAndTerrenoIdAndDeletedFalse(Long tenantId, Long terrenoId);

    /**
     * Busca cotizaciones vigentes
     */
    @Query("SELECT c FROM Cotizacion c WHERE c.tenantId = :tenantId " +
           "AND c.fechaVigencia >= :fecha " +
           "AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Cotizacion> findCotizacionesVigentes(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    /**
     * Busca cotizaciones por cliente
     */
    @Query("SELECT c FROM Cotizacion c WHERE c.tenantId = :tenantId " +
           "AND LOWER(c.clienteNombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Cotizacion> findByClienteNombre(@Param("tenantId") Long tenantId, @Param("nombre") String nombre);

    /**
     * Busca cotizaciones por rango de fechas
     */
    @Query("SELECT c FROM Cotizacion c WHERE c.tenantId = :tenantId " +
           "AND c.createdAt BETWEEN :fechaInicio AND :fechaFin " +
           "AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Cotizacion> findByRangoFechas(@Param("tenantId") Long tenantId,
                                        @Param("fechaInicio") LocalDate fechaInicio,
                                        @Param("fechaFin") LocalDate fechaFin);

    long countByTenantIdAndDeletedFalse(Long tenantId);
}
