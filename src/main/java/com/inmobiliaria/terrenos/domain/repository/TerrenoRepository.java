package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Terreno
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface TerrenoRepository extends JpaRepository<Terreno, Long> {

    /**
     * Busca todos los terrenos de un tenant
     */
    List<Terreno> findByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Busca un terreno por ID y tenant
     */
    Optional<Terreno> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca terrenos por proyecto
     */
    List<Terreno> findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(Long tenantId, Long proyectoId);

    /**
     * Busca terrenos por fase
     */
    List<Terreno> findByTenantIdAndFaseIdAndDeletedFalseOrderByNumeroLoteAsc(Long tenantId, Long faseId);

    /**
     * Busca terrenos por estado
     */
    List<Terreno> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoTerreno estado);

    /**
     * Busca terrenos por proyecto y estado
     */
    List<Terreno> findByTenantIdAndProyectoIdAndEstadoAndDeletedFalseOrderByNumeroLoteAsc(
            Long tenantId, Long proyectoId, EstadoTerreno estado);

    /**
     * Busca terrenos disponibles de un proyecto
     */
    @Query("SELECT t FROM Terreno t WHERE t.tenantId = :tenantId " +
           "AND t.proyectoId = :proyectoId " +
           "AND t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.DISPONIBLE " +
           "AND t.deleted = false " +
           "ORDER BY t.numeroLote ASC")
    List<Terreno> findTerrenosDisponiblesPorProyecto(@Param("tenantId") Long tenantId,
                                                       @Param("proyectoId") Long proyectoId);

    /**
     * Busca terrenos por manzana
     */
    List<Terreno> findByTenantIdAndProyectoIdAndManzanaAndDeletedFalseOrderByNumeroLoteAsc(
            Long tenantId, Long proyectoId, String manzana);

    /**
     * Busca terrenos por rango de precio
     */
    @Query("SELECT t FROM Terreno t WHERE t.tenantId = :tenantId " +
           "AND t.precioFinal BETWEEN :precioMin AND :precioMax " +
           "AND t.deleted = false " +
           "ORDER BY t.precioFinal ASC")
    List<Terreno> findByRangoPrecio(@Param("tenantId") Long tenantId,
                                     @Param("precioMin") BigDecimal precioMin,
                                     @Param("precioMax") BigDecimal precioMax);

    /**
     * Busca terrenos por rango de área
     */
    @Query("SELECT t FROM Terreno t WHERE t.tenantId = :tenantId " +
           "AND t.area BETWEEN :areaMin AND :areaMax " +
           "AND t.deleted = false " +
           "ORDER BY t.area ASC")
    List<Terreno> findByRangoArea(@Param("tenantId") Long tenantId,
                                   @Param("areaMin") BigDecimal areaMin,
                                   @Param("areaMax") BigDecimal areaMax);

    /**
     * Cuenta terrenos por proyecto
     */
    long countByTenantIdAndProyectoIdAndDeletedFalse(Long tenantId, Long proyectoId);

    /**
     * Cuenta terrenos por fase
     */
    long countByTenantIdAndFaseIdAndDeletedFalse(Long tenantId, Long faseId);

    /**
     * Cuenta terrenos por proyecto y estado
     */
    long countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(Long tenantId, Long proyectoId, EstadoTerreno estado);

    /**
     * Verifica si existe un terreno con el mismo número de lote en un proyecto
     */
    boolean existsByTenantIdAndProyectoIdAndNumeroLoteIgnoreCaseAndDeletedFalse(
            Long tenantId, Long proyectoId, String numeroLote);

    /**
     * Verifica si existe un terreno con el mismo número de lote excluyendo un ID
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Terreno t " +
           "WHERE t.tenantId = :tenantId " +
           "AND t.proyectoId = :proyectoId " +
           "AND LOWER(t.numeroLote) = LOWER(:numeroLote) " +
           "AND t.id != :excludeId " +
           "AND t.deleted = false")
    boolean existsByNumeroLoteExcludingId(@Param("tenantId") Long tenantId,
                                          @Param("proyectoId") Long proyectoId,
                                          @Param("numeroLote") String numeroLote,
                                          @Param("excludeId") Long excludeId);

    /**
     * Obtiene estadísticas de terrenos por proyecto
     */
    @Query("SELECT " +
           "COUNT(t) as total, " +
           "SUM(CASE WHEN t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.DISPONIBLE THEN 1 ELSE 0 END) as disponibles, " +
           "SUM(CASE WHEN t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.APARTADO THEN 1 ELSE 0 END) as apartados, " +
           "SUM(CASE WHEN t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.VENDIDO THEN 1 ELSE 0 END) as vendidos " +
           "FROM Terreno t " +
           "WHERE t.tenantId = :tenantId AND t.proyectoId = :proyectoId AND t.deleted = false")
    Object[] getEstadisticasPorProyecto(@Param("tenantId") Long tenantId, @Param("proyectoId") Long proyectoId);
}
