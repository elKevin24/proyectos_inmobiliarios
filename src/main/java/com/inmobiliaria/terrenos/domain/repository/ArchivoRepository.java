package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Archivo;
import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Archivo
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, Long> {

    List<Archivo> findByTenantIdAndDeletedFalse(Long tenantId);

    Optional<Archivo> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca archivos por proyecto
     */
    List<Archivo> findByTenantIdAndProyectoIdAndDeletedFalseOrderByVersionDesc(Long tenantId, Long proyectoId);

    /**
     * Busca archivos por terreno
     */
    List<Archivo> findByTenantIdAndTerrenoIdAndDeletedFalseOrderByVersionDesc(Long tenantId, Long terrenoId);

    /**
     * Busca archivos por venta
     */
    List<Archivo> findByTenantIdAndVentaIdAndDeletedFalseOrderByVersionDesc(Long tenantId, Long ventaId);

    /**
     * Busca archivos por tipo
     */
    List<Archivo> findByTenantIdAndTipoAndDeletedFalseOrderByCreatedAtDesc(Long tenantId, TipoArchivo tipo);

    /**
     * Busca archivos activos por proyecto
     */
    @Query("SELECT a FROM Archivo a WHERE a.tenantId = :tenantId " +
           "AND a.proyectoId = :proyectoId " +
           "AND a.esActivo = true " +
           "AND a.deleted = false " +
           "ORDER BY a.version DESC")
    List<Archivo> findArchivosActivosPorProyecto(@Param("tenantId") Long tenantId, @Param("proyectoId") Long proyectoId);

    /**
     * Busca archivos por proyecto y tipo
     */
    @Query("SELECT a FROM Archivo a WHERE a.tenantId = :tenantId " +
           "AND a.proyectoId = :proyectoId " +
           "AND a.tipo = :tipo " +
           "AND a.deleted = false " +
           "ORDER BY a.version DESC")
    List<Archivo> findByProyectoYTipo(@Param("tenantId") Long tenantId,
                                       @Param("proyectoId") Long proyectoId,
                                       @Param("tipo") TipoArchivo tipo);

    /**
     * Busca la última versión de un archivo por proyecto y tipo
     */
    @Query("SELECT a FROM Archivo a WHERE a.tenantId = :tenantId " +
           "AND a.proyectoId = :proyectoId " +
           "AND a.tipo = :tipo " +
           "AND a.nombreOriginal = :nombreOriginal " +
           "AND a.deleted = false " +
           "ORDER BY a.version DESC " +
           "LIMIT 1")
    Optional<Archivo> findUltimaVersion(@Param("tenantId") Long tenantId,
                                         @Param("proyectoId") Long proyectoId,
                                         @Param("tipo") TipoArchivo tipo,
                                         @Param("nombreOriginal") String nombreOriginal);

    /**
     * Busca todas las versiones de un archivo
     */
    @Query("SELECT a FROM Archivo a WHERE a.tenantId = :tenantId " +
           "AND a.proyectoId = :proyectoId " +
           "AND a.nombreOriginal = :nombreOriginal " +
           "AND a.deleted = false " +
           "ORDER BY a.version DESC")
    List<Archivo> findTodasLasVersiones(@Param("tenantId") Long tenantId,
                                         @Param("proyectoId") Long proyectoId,
                                         @Param("nombreOriginal") String nombreOriginal);

    /**
     * Busca galería de imágenes de un proyecto
     */
    @Query("SELECT a FROM Archivo a WHERE a.tenantId = :tenantId " +
           "AND a.proyectoId = :proyectoId " +
           "AND a.tipo IN (com.inmobiliaria.terrenos.domain.enums.TipoArchivo.IMAGEN_PROYECTO, " +
           "               com.inmobiliaria.terrenos.domain.enums.TipoArchivo.IMAGEN_TERRENO) " +
           "AND a.esActivo = true " +
           "AND a.deleted = false " +
           "ORDER BY a.createdAt DESC")
    List<Archivo> findGaleriaProyecto(@Param("tenantId") Long tenantId, @Param("proyectoId") Long proyectoId);
}
