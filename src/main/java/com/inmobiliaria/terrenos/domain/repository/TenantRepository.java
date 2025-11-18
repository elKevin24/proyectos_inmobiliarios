package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Tenant
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Busca un tenant por email
     */
    Optional<Tenant> findByEmail(String email);

    /**
     * Verifica si existe un tenant con ese email
     */
    boolean existsByEmail(String email);

    /**
     * Busca un tenant por RFC
     */
    Optional<Tenant> findByRfc(String rfc);

    /**
     * Verifica si existe un tenant con ese RFC
     */
    boolean existsByRfc(String rfc);

    /**
     * Busca tenants activos
     */
    java.util.List<Tenant> findByActivo(Boolean activo);
}
