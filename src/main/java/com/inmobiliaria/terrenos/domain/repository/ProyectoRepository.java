package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Proyecto
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    /**
     * Busca todos los proyectos de un tenant
     */
    List<Proyecto> findByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Busca un proyecto por ID y tenant
     */
    Optional<Proyecto> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca proyectos por estado
     */
    List<Proyecto> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoProyecto estado);

    /**
     * Busca proyectos por ciudad
     */
    List<Proyecto> findByTenantIdAndCiudadIgnoreCaseAndDeletedFalse(Long tenantId, String ciudad);

    /**
     * Busca proyectos por estado (ubicación geográfica)
     */
    List<Proyecto> findByTenantIdAndEstadoIgnoreCaseAndDeletedFalse(Long tenantId, String estado);

    /**
     * Busca proyectos con terrenos disponibles
     */
    @Query("SELECT p FROM Proyecto p WHERE p.tenantId = :tenantId " +
           "AND p.terrenosDisponibles > 0 " +
           "AND p.deleted = false " +
           "ORDER BY p.createdAt DESC")
    List<Proyecto> findProyectosConTerrenosDisponibles(@Param("tenantId") Long tenantId);

    /**
     * Busca proyectos activos (en venta)
     */
    @Query("SELECT p FROM Proyecto p WHERE p.tenantId = :tenantId " +
           "AND p.estado = com.inmobiliaria.terrenos.domain.enums.EstadoProyecto.EN_VENTA " +
           "AND p.deleted = false " +
           "ORDER BY p.createdAt DESC")
    List<Proyecto> findProyectosActivos(@Param("tenantId") Long tenantId);

    /**
     * Cuenta proyectos por tenant
     */
    long countByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Verifica si existe un proyecto con el mismo nombre para un tenant
     */
    boolean existsByTenantIdAndNombreIgnoreCaseAndDeletedFalse(Long tenantId, String nombre);

    /**
     * Verifica si existe un proyecto con el mismo nombre excluyendo un ID
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Proyecto p " +
           "WHERE p.tenantId = :tenantId " +
           "AND LOWER(p.nombre) = LOWER(:nombre) " +
           "AND p.id != :excludeId " +
           "AND p.deleted = false")
    boolean existsByNombreExcludingId(@Param("tenantId") Long tenantId,
                                      @Param("nombre") String nombre,
                                      @Param("excludeId") Long excludeId);
}
