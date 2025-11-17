package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Permiso
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    /**
     * Busca un permiso por código
     */
    Optional<Permiso> findByCodigo(String codigo);

    /**
     * Busca permisos por módulo
     */
    List<Permiso> findByModulo(String modulo);

    /**
     * Busca permisos por códigos
     */
    List<Permiso> findByCodigoIn(List<String> codigos);
}
