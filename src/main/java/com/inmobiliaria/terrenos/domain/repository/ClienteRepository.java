package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Cliente
 *
 * @author Kevin
 * @version 1.0.0
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca todos los clientes de un tenant
     */
    List<Cliente> findByTenantIdAndDeletedFalse(Long tenantId);

    /**
     * Busca un cliente por ID y tenant
     */
    Optional<Cliente> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    /**
     * Busca clientes por email
     */
    Optional<Cliente> findByTenantIdAndEmailAndDeletedFalse(Long tenantId, String email);

    /**
     * Busca clientes por teléfono
     */
    List<Cliente> findByTenantIdAndTelefonoAndDeletedFalse(Long tenantId, String telefono);

    /**
     * Busca clientes por estado
     */
    List<Cliente> findByTenantIdAndEstadoClienteAndDeletedFalse(Long tenantId, EstadoCliente estadoCliente);

    /**
     * Busca clientes por nombre (búsqueda parcial)
     */
    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.deleted = false " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    List<Cliente> buscarPorNombre(@Param("tenantId") Long tenantId, @Param("nombre") String nombre);

    /**
     * Busca clientes por RFC
     */
    Optional<Cliente> findByTenantIdAndRfcAndDeletedFalse(Long tenantId, String rfc);

    /**
     * Busca clientes por CURP
     */
    Optional<Cliente> findByTenantIdAndCurpAndDeletedFalse(Long tenantId, String curp);

    /**
     * Busca clientes por ciudad
     */
    List<Cliente> findByTenantIdAndCiudadAndDeletedFalse(Long tenantId, String ciudad);

    /**
     * Busca clientes por origen
     */
    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.origen = :origen " +
           "AND c.deleted = false")
    List<Cliente> findByOrigen(@Param("tenantId") Long tenantId, @Param("origen") String origen);

    /**
     * Verifica si existe un cliente con el email (para validación única)
     */
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.email = :email " +
           "AND c.id != :excludeId " +
           "AND c.deleted = false")
    boolean existeEmailEnOtroCliente(@Param("tenantId") Long tenantId,
                                      @Param("email") String email,
                                      @Param("excludeId") Long excludeId);

    /**
     * Busca clientes compradores (con ventas)
     */
    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.estadoCliente = 'COMPRADOR' " +
           "AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Cliente> findCompradores(@Param("tenantId") Long tenantId);

    /**
     * Busca clientes activos (no inactivos)
     */
    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.estadoCliente != 'INACTIVO' " +
           "AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Cliente> findClientesActivos(@Param("tenantId") Long tenantId);

    /**
     * Busca clientes recientes (últimos N días)
     */
    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.deleted = false " +
           "AND c.createdAt >= CURRENT_DATE - :dias " +
           "ORDER BY c.createdAt DESC")
    List<Cliente> findClientesRecientes(@Param("tenantId") Long tenantId, @Param("dias") Integer dias);

    /**
     * Cuenta total de clientes por tenant
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.tenantId = :tenantId AND c.deleted = false")
    Long contarClientes(@Param("tenantId") Long tenantId);

    /**
     * Cuenta clientes por estado
     */
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.estadoCliente = :estado " +
           "AND c.deleted = false")
    Long contarPorEstado(@Param("tenantId") Long tenantId, @Param("estado") EstadoCliente estado);
}
