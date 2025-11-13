package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 * 
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email (ignorando tenant por ser pre-autenticación)
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por email y tenant_id
     */
    Optional<Usuario> findByEmailAndTenantId(String email, Long tenantId);

    /**
     * Busca todos los usuarios de un tenant
     */
    List<Usuario> findByTenantId(Long tenantId);

    /**
     * Busca usuarios activos de un tenant
     */
    List<Usuario> findByTenantIdAndActivo(Long tenantId, Boolean activo);

    /**
     * Verifica si existe un usuario con ese email en el tenant
     */
    boolean existsByEmailAndTenantId(String email, Long tenantId);

    /**
     * Busca usuarios por rol
     */
    @Query("SELECT DISTINCT u FROM Usuario u JOIN u.roles r WHERE u.tenantId = :tenantId AND r.nombre = :rolNombre")
    List<Usuario> findByTenantIdAndRol(@Param("tenantId") Long tenantId, @Param("rolNombre") String rolNombre);

    /**
     * Cuenta usuarios activos por tenant
     */
    long countByTenantIdAndActivo(Long tenantId, Boolean activo);
}
