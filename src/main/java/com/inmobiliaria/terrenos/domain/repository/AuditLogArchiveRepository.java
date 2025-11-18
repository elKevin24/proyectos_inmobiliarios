package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para archivo de logs
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface AuditLogArchiveRepository extends JpaRepository<AuditLogArchive, Long> {

    /**
     * Busca archivos por tenant
     */
    List<AuditLogArchive> findByTenantIdOrderByFechaOriginalDesc(Long tenantId);

    /**
     * Busca archivos por tipo
     */
    @Query("SELECT a FROM AuditLogArchive a WHERE a.tenantId = :tenantId " +
           "AND a.tipo = :tipo " +
           "ORDER BY a.fechaOriginal DESC")
    List<AuditLogArchive> findByTipo(@Param("tenantId") Long tenantId,
                                      @Param("tipo") String tipo);

    /**
     * Busca archivos por rango de fechas originales
     */
    @Query("SELECT a FROM AuditLogArchive a WHERE a.tenantId = :tenantId " +
           "AND a.fechaOriginal BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fechaOriginal DESC")
    List<AuditLogArchive> findByRangoFechas(@Param("tenantId") Long tenantId,
                                             @Param("fechaInicio") LocalDateTime fechaInicio,
                                             @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Cuenta archivos por tipo
     */
    @Query("SELECT COUNT(a) FROM AuditLogArchive a WHERE a.tenantId = :tenantId " +
           "AND a.tipo = :tipo")
    Long countByTipo(@Param("tenantId") Long tenantId, @Param("tipo") String tipo);
}
