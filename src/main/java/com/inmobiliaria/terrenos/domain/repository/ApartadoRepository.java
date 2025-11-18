package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Apartado
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface ApartadoRepository extends JpaRepository<Apartado, Long> {

    List<Apartado> findByTenantIdAndDeletedFalse(Long tenantId);

    Optional<Apartado> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    List<Apartado> findByTenantIdAndTerrenoIdAndDeletedFalse(Long tenantId, Long terrenoId);

    List<Apartado> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoApartado estado);

    /**
     * Busca apartados vigentes
     */
    @Query("SELECT a FROM Apartado a WHERE a.tenantId = :tenantId " +
           "AND a.estado = com.inmobiliaria.terrenos.domain.enums.EstadoApartado.VIGENTE " +
           "AND a.fechaVencimiento >= :fecha " +
           "AND a.deleted = false " +
           "ORDER BY a.fechaVencimiento ASC")
    List<Apartado> findApartadosVigentes(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    /**
     * Busca apartados vencidos
     */
    @Query("SELECT a FROM Apartado a WHERE a.tenantId = :tenantId " +
           "AND a.estado = com.inmobiliaria.terrenos.domain.enums.EstadoApartado.VIGENTE " +
           "AND a.fechaVencimiento < :fecha " +
           "AND a.deleted = false " +
           "ORDER BY a.fechaVencimiento ASC")
    List<Apartado> findApartadosVencidos(@Param("tenantId") Long tenantId, @Param("fecha") LocalDate fecha);

    /**
     * Busca apartados por cliente
     */
    @Query("SELECT a FROM Apartado a WHERE a.tenantId = :tenantId " +
           "AND LOWER(a.clienteNombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "AND a.deleted = false " +
           "ORDER BY a.createdAt DESC")
    List<Apartado> findByClienteNombre(@Param("tenantId") Long tenantId, @Param("nombre") String nombre);

    long countByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoApartado estado);
}
