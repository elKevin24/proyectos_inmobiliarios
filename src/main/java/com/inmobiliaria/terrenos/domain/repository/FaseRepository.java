package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Fase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Fase
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface FaseRepository extends JpaRepository<Fase, Long> {

    /**
     * Busca todas las fases de un tenant
     */
    List<Fase> findByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Busca una fase por ID y tenant
     */
    Optional<Fase> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca fases por proyecto
     */
    List<Fase> findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroFaseAsc(Long tenantId, Long proyectoId);

    /**
     * Busca fases activas por proyecto
     */
    @Query("SELECT f FROM Fase f WHERE f.tenantId = :tenantId " +
           "AND f.proyectoId = :proyectoId " +
           "AND f.activa = true " +
           "AND f.deleted = false " +
           "ORDER BY f.numeroFase ASC")
    List<Fase> findFasesActivasPorProyecto(@Param("tenantId") Long tenantId, @Param("proyectoId") Long proyectoId);

    /**
     * Busca fases con terrenos disponibles
     */
    @Query("SELECT f FROM Fase f WHERE f.tenantId = :tenantId " +
           "AND f.terrenosDisponibles > 0 " +
           "AND f.activa = true " +
           "AND f.deleted = false " +
           "ORDER BY f.numeroFase ASC")
    List<Fase> findFasesConTerrenosDisponibles(@Param("tenantId") Long tenantId);

    /**
     * Cuenta fases por proyecto
     */
    long countByTenantIdAndProyectoIdAndDeletedFalse(Long tenantId, Long proyectoId);

    /**
     * Verifica si existe una fase con el mismo nombre en un proyecto
     */
    boolean existsByTenantIdAndProyectoIdAndNombreIgnoreCaseAndDeletedFalse(
            Long tenantId, Long proyectoId, String nombre);

    /**
     * Verifica si existe una fase con el mismo número en un proyecto
     */
    boolean existsByTenantIdAndProyectoIdAndNumeroFaseAndDeletedFalse(
            Long tenantId, Long proyectoId, Integer numeroFase);

    /**
     * Verifica si existe una fase con el mismo nombre excluyendo un ID
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Fase f " +
           "WHERE f.tenantId = :tenantId " +
           "AND f.proyectoId = :proyectoId " +
           "AND LOWER(f.nombre) = LOWER(:nombre) " +
           "AND f.id != :excludeId " +
           "AND f.deleted = false")
    boolean existsByNombreExcludingId(@Param("tenantId") Long tenantId,
                                      @Param("proyectoId") Long proyectoId,
                                      @Param("nombre") String nombre,
                                      @Param("excludeId") Long excludeId);

    /**
     * Verifica si existe una fase con el mismo número excluyendo un ID
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Fase f " +
           "WHERE f.tenantId = :tenantId " +
           "AND f.proyectoId = :proyectoId " +
           "AND f.numeroFase = :numeroFase " +
           "AND f.id != :excludeId " +
           "AND f.deleted = false")
    boolean existsByNumeroFaseExcludingId(@Param("tenantId") Long tenantId,
                                          @Param("proyectoId") Long proyectoId,
                                          @Param("numeroFase") Integer numeroFase,
                                          @Param("excludeId") Long excludeId);
}
