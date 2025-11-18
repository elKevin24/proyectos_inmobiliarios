package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogSimple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para auditoría simple
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface AuditLogSimpleRepository extends JpaRepository<AuditLogSimple, Long> {

    /**
     * Busca logs por tenant
     */
    List<AuditLogSimple> findByTenantIdOrderByFechaDesc(Long tenantId);

    /**
     * Busca logs por tenant con límite
     */
    @Query("SELECT a FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "ORDER BY a.fecha DESC LIMIT :limit")
    List<AuditLogSimple> findByTenantIdWithLimit(@Param("tenantId") Long tenantId,
                                                   @Param("limit") Integer limit);

    /**
     * Busca logs por usuario
     */
    @Query("SELECT a FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND a.usuarioId = :usuarioId " +
           "ORDER BY a.fecha DESC")
    List<AuditLogSimple> findByUsuarioId(@Param("tenantId") Long tenantId,
                                          @Param("usuarioId") Long usuarioId);

    /**
     * Busca logs por acción
     */
    @Query("SELECT a FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND a.accion = :accion " +
           "ORDER BY a.fecha DESC")
    List<AuditLogSimple> findByAccion(@Param("tenantId") Long tenantId,
                                       @Param("accion") String accion);

    /**
     * Busca logs por rango de fechas
     */
    @Query("SELECT a FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND a.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fecha DESC")
    List<AuditLogSimple> findByRangoFechas(@Param("tenantId") Long tenantId,
                                            @Param("fechaInicio") LocalDateTime fechaInicio,
                                            @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca logs con filtros múltiples
     */
    @Query("SELECT a FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND (:usuarioId IS NULL OR a.usuarioId = :usuarioId) " +
           "AND (:accion IS NULL OR a.accion = :accion) " +
           "AND a.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fecha DESC")
    List<AuditLogSimple> findConFiltros(@Param("tenantId") Long tenantId,
                                         @Param("usuarioId") Long usuarioId,
                                         @Param("accion") String accion,
                                         @Param("fechaInicio") LocalDateTime fechaInicio,
                                         @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca logs antiguos (para archivado)
     */
    @Query("SELECT a FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND a.fecha < :fechaLimite")
    List<AuditLogSimple> findAntiguos(@Param("tenantId") Long tenantId,
                                       @Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Elimina logs antiguos (después de archivar)
     */
    @Modifying
    @Query("DELETE FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND a.fecha < :fechaLimite")
    int deleteAntiguos(@Param("tenantId") Long tenantId,
                       @Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Cuenta logs por acción
     */
    @Query("SELECT COUNT(a) FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND a.accion = :accion")
    Long countByAccion(@Param("tenantId") Long tenantId, @Param("accion") String accion);

    /**
     * Busca últimos logins de un usuario
     */
    @Query("SELECT a FROM AuditLogSimple a WHERE a.tenantId = :tenantId " +
           "AND a.usuarioId = :usuarioId " +
           "AND a.accion = 'LOGIN' " +
           "ORDER BY a.fecha DESC LIMIT :limit")
    List<AuditLogSimple> findUltimosLogins(@Param("tenantId") Long tenantId,
                                            @Param("usuarioId") Long usuarioId,
                                            @Param("limit") Integer limit);
}
