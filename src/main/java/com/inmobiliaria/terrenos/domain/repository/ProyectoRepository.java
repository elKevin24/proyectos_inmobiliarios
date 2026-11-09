package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByTenantIdAndDeletedFalse(Long tenantId);

    Page<Proyecto> findByTenantIdAndDeletedFalse(Long tenantId, Pageable pageable);

    Optional<Proyecto> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    List<Proyecto> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoProyecto estado);

    Page<Proyecto> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoProyecto estado, Pageable pageable);

    List<Proyecto> findByTenantIdAndCiudadIgnoreCaseAndDeletedFalse(Long tenantId, String ciudad);

    List<Proyecto> findByTenantIdAndEstadoIgnoreCaseAndDeletedFalse(Long tenantId, String estado);

    @Query("SELECT p FROM Proyecto p WHERE p.tenantId = :tenantId " +
           "AND p.terrenosDisponibles > 0 " +
           "AND p.deleted = false " +
           "ORDER BY p.createdAt DESC")
    List<Proyecto> findProyectosConTerrenosDisponibles(@Param("tenantId") Long tenantId);

    @Query(value = "SELECT p FROM Proyecto p WHERE p.tenantId = :tenantId " +
           "AND p.terrenosDisponibles > 0 " +
           "AND p.deleted = false",
           countQuery = "SELECT COUNT(p) FROM Proyecto p WHERE p.tenantId = :tenantId AND p.terrenosDisponibles > 0 AND p.deleted = false")
    Page<Proyecto> findProyectosConTerrenosDisponibles(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("SELECT p FROM Proyecto p WHERE p.tenantId = :tenantId " +
           "AND p.estadoProyecto = com.inmobiliaria.terrenos.domain.enums.EstadoProyecto.EN_VENTA " +
           "AND p.deleted = false " +
           "ORDER BY p.createdAt DESC")
    List<Proyecto> findProyectosActivos(@Param("tenantId") Long tenantId);

    @Query(value = "SELECT p FROM Proyecto p WHERE p.tenantId = :tenantId " +
           "AND p.estadoProyecto = com.inmobiliaria.terrenos.domain.enums.EstadoProyecto.EN_VENTA " +
           "AND p.deleted = false",
           countQuery = "SELECT COUNT(p) FROM Proyecto p WHERE p.tenantId = :tenantId AND p.estadoProyecto = com.inmobiliaria.terrenos.domain.enums.EstadoProyecto.EN_VENTA AND p.deleted = false")
    Page<Proyecto> findProyectosActivos(@Param("tenantId") Long tenantId, Pageable pageable);

    long countByTenantIdAndDeletedFalse(Long tenantId);

    boolean existsByTenantIdAndNombreIgnoreCaseAndDeletedFalse(Long tenantId, String nombre);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Proyecto p " +
           "WHERE p.tenantId = :tenantId " +
           "AND LOWER(p.nombre) = LOWER(:nombre) " +
           "AND p.id != :excludeId " +
           "AND p.deleted = false")
    boolean existsByNombreExcludingId(@Param("tenantId") Long tenantId,
                                      @Param("nombre") String nombre,
                                      @Param("excludeId") Long excludeId);
}
