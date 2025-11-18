package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogCritica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para auditoría crítica
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface AuditLogCriticaRepository extends JpaRepository<AuditLogCritica, Long> {

    /**
     * Busca logs por tenant
     */
    List<AuditLogCritica> findByTenantIdOrderByFechaDesc(Long tenantId);

    /**
     * Busca logs por tenant con límite
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "ORDER BY a.fecha DESC LIMIT :limit")
    List<AuditLogCritica> findByTenantIdWithLimit(@Param("tenantId") Long tenantId,
                                                    @Param("limit") Integer limit);

    /**
     * Busca logs por tabla y registro
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.tabla = :tabla " +
           "AND a.registroId = :registroId " +
           "ORDER BY a.fecha DESC")
    List<AuditLogCritica> findByTablaYRegistro(@Param("tenantId") Long tenantId,
                                                 @Param("tabla") String tabla,
                                                 @Param("registroId") Long registroId);

    /**
     * Busca logs por tabla
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.tabla = :tabla " +
           "ORDER BY a.fecha DESC")
    List<AuditLogCritica> findByTabla(@Param("tenantId") Long tenantId,
                                       @Param("tabla") String tabla);

    /**
     * Busca logs por usuario
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.usuarioId = :usuarioId " +
           "ORDER BY a.fecha DESC")
    List<AuditLogCritica> findByUsuarioId(@Param("tenantId") Long tenantId,
                                           @Param("usuarioId") Long usuarioId);

    /**
     * Busca logs por operación
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.operacion = :operacion " +
           "ORDER BY a.fecha DESC")
    List<AuditLogCritica> findByOperacion(@Param("tenantId") Long tenantId,
                                           @Param("operacion") String operacion);

    /**
     * Busca logs por rango de fechas
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fecha DESC")
    List<AuditLogCritica> findByRangoFechas(@Param("tenantId") Long tenantId,
                                             @Param("fechaInicio") LocalDateTime fechaInicio,
                                             @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca logs con filtros múltiples
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND (:usuarioId IS NULL OR a.usuarioId = :usuarioId) " +
           "AND (:tabla IS NULL OR a.tabla = :tabla) " +
           "AND a.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY a.fecha DESC")
    List<AuditLogCritica> findConFiltros(@Param("tenantId") Long tenantId,
                                          @Param("usuarioId") Long usuarioId,
                                          @Param("tabla") String tabla,
                                          @Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca historial completo de cambios de un registro
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.tabla = :tabla " +
           "AND a.registroId = :registroId " +
           "AND a.campo = :campo " +
           "ORDER BY a.fecha ASC")
    List<AuditLogCritica> findHistorialCampo(@Param("tenantId") Long tenantId,
                                              @Param("tabla") String tabla,
                                              @Param("registroId") Long registroId,
                                              @Param("campo") String campo);

    /**
     * Busca logs antiguos (para archivado)
     */
    @Query("SELECT a FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.fecha < :fechaLimite")
    List<AuditLogCritica> findAntiguos(@Param("tenantId") Long tenantId,
                                        @Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Elimina logs antiguos (después de archivar)
     */
    @Modifying
    @Query("DELETE FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.fecha < :fechaLimite")
    int deleteAntiguos(@Param("tenantId") Long tenantId,
                       @Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Cuenta cambios por tabla
     */
    @Query("SELECT COUNT(a) FROM AuditLogCritica a WHERE a.tenantId = :tenantId " +
           "AND a.tabla = :tabla")
    Long countByTabla(@Param("tenantId") Long tenantId, @Param("tabla") String tabla);
}
