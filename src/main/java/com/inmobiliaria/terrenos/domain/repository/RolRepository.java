package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Rol;
import com.inmobiliaria.terrenos.domain.enums.RolEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Rol
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por nombre y tenant
     */
    Optional<Rol> findByNombreAndTenantId(RolEnum nombre, Long tenantId);

    /**
     * Busca todos los roles de un tenant
     */
    List<Rol> findByTenantId(Long tenantId);

    /**
     * Verifica si existe un rol con ese nombre en el tenant
     */
    boolean existsByNombreAndTenantId(RolEnum nombre, Long tenantId);
}
