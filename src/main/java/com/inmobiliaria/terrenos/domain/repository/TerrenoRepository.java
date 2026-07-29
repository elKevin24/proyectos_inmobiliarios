package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerrenoRepository extends JpaRepository<Terreno, Long> {

    List<Terreno> findByTenantIdAndDeletedFalse(Long tenantId);

    Page<Terreno> findByTenantIdAndDeletedFalse(Long tenantId, Pageable pageable);

    Optional<Terreno> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    List<Terreno> findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(Long tenantId, Long proyectoId);

    Page<Terreno> findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(Long tenantId, Long proyectoId, Pageable pageable);

    List<Terreno> findByTenantIdAndFaseIdAndDeletedFalseOrderByNumeroLoteAsc(Long tenantId, Long faseId);

    List<Terreno> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoTerreno estado);

    Page<Terreno> findByTenantIdAndEstadoAndDeletedFalse(Long tenantId, EstadoTerreno estado, Pageable pageable);

    List<Terreno> findByTenantIdAndProyectoIdAndEstadoAndDeletedFalseOrderByNumeroLoteAsc(
            Long tenantId, Long proyectoId, EstadoTerreno estado);

    @Query("SELECT t FROM Terreno t WHERE t.tenantId = :tenantId " +
           "AND t.proyectoId = :proyectoId " +
           "AND t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.DISPONIBLE " +
           "AND t.deleted = false " +
           "ORDER BY t.numeroLote ASC")
    List<Terreno> findTerrenosDisponiblesPorProyecto(@Param("tenantId") Long tenantId,
                                                       @Param("proyectoId") Long proyectoId);

    List<Terreno> findByTenantIdAndProyectoIdAndManzanaAndDeletedFalseOrderByNumeroLoteAsc(
            Long tenantId, Long proyectoId, String manzana);

    @Query("SELECT t FROM Terreno t WHERE t.tenantId = :tenantId " +
           "AND t.precioFinal BETWEEN :precioMin AND :precioMax " +
           "AND t.deleted = false " +
           "ORDER BY t.precioFinal ASC")
    List<Terreno> findByRangoPrecio(@Param("tenantId") Long tenantId,
                                      @Param("precioMin") BigDecimal precioMin,
                                      @Param("precioMax") BigDecimal precioMax);

    @Query("SELECT t FROM Terreno t WHERE t.tenantId = :tenantId " +
           "AND t.area BETWEEN :areaMin AND :areaMax " +
           "AND t.deleted = false " +
           "ORDER BY t.area ASC")
    List<Terreno> findByRangoArea(@Param("tenantId") Long tenantId,
                                    @Param("areaMin") BigDecimal areaMin,
                                    @Param("areaMax") BigDecimal areaMax);

    long countByTenantIdAndProyectoIdAndDeletedFalse(Long tenantId, Long proyectoId);

    long countByTenantIdAndFaseIdAndDeletedFalse(Long tenantId, Long faseId);

    long countByTenantIdAndFaseIdAndEstadoAndDeletedFalse(Long tenantId, Long faseId, EstadoTerreno estado);

    long countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(Long tenantId, Long proyectoId, EstadoTerreno estado);

    boolean existsByTenantIdAndProyectoIdAndNumeroLoteIgnoreCaseAndDeletedFalse(
            Long tenantId, Long proyectoId, String numeroLote);

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

    @Query("SELECT " +
           "COUNT(t) as total, " +
           "SUM(CASE WHEN t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.DISPONIBLE THEN 1 ELSE 0 END) as disponibles, " +
           "SUM(CASE WHEN t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.APARTADO THEN 1 ELSE 0 END) as apartados, " +
           "SUM(CASE WHEN t.estado = com.inmobiliaria.terrenos.domain.enums.EstadoTerreno.VENDIDO THEN 1 ELSE 0 END) as vendidos " +
           "FROM Terreno t " +
           "WHERE t.tenantId = :tenantId AND t.proyectoId = :proyectoId AND t.deleted = false")
    Object[] getEstadisticasPorProyecto(@Param("tenantId") Long tenantId, @Param("proyectoId") Long proyectoId);
}
