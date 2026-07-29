package com.inmobiliaria.terrenos.domain.repository;

import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByTenantIdAndDeletedFalse(Long tenantId);

    Page<Cliente> findByTenantIdAndDeletedFalse(Long tenantId, Pageable pageable);

    Optional<Cliente> findByIdAndTenantIdAndDeletedFalse(Long id, Long tenantId);

    Optional<Cliente> findByTenantIdAndEmailAndDeletedFalse(Long tenantId, String email);

    List<Cliente> findByTenantIdAndTelefonoAndDeletedFalse(Long tenantId, String telefono);

    List<Cliente> findByTenantIdAndEstadoClienteAndDeletedFalse(Long tenantId, EstadoCliente estadoCliente);

    Page<Cliente> findByTenantIdAndEstadoClienteAndDeletedFalse(Long tenantId, EstadoCliente estadoCliente, Pageable pageable);

    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.deleted = false " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    List<Cliente> buscarPorNombre(@Param("tenantId") Long tenantId, @Param("nombre") String nombre);

    Optional<Cliente> findByTenantIdAndRfcAndDeletedFalse(Long tenantId, String rfc);

    Optional<Cliente> findByTenantIdAndCurpAndDeletedFalse(Long tenantId, String curp);

    List<Cliente> findByTenantIdAndCiudadAndDeletedFalse(Long tenantId, String ciudad);

    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.origen = :origen " +
           "AND c.deleted = false")
    List<Cliente> findByOrigen(@Param("tenantId") Long tenantId, @Param("origen") String origen);

    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.email = :email " +
           "AND c.id != :excludeId " +
           "AND c.deleted = false")
    boolean existeEmailEnOtroCliente(@Param("tenantId") Long tenantId,
                                      @Param("email") String email,
                                      @Param("excludeId") Long excludeId);

    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.estadoCliente = 'COMPRADOR' " +
           "AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Cliente> findCompradores(@Param("tenantId") Long tenantId);

    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.estadoCliente != 'INACTIVO' " +
           "AND c.deleted = false " +
           "ORDER BY c.createdAt DESC")
    List<Cliente> findClientesActivos(@Param("tenantId") Long tenantId);

    @Query("SELECT c FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.deleted = false " +
           "AND c.createdAt >= :fechaLimite " +
           "ORDER BY c.createdAt DESC")
    List<Cliente> findClientesRecientes(@Param("tenantId") Long tenantId, @Param("fechaLimite") java.time.LocalDateTime fechaLimite);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.tenantId = :tenantId AND c.deleted = false")
    Long contarClientes(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.tenantId = :tenantId " +
           "AND c.estadoCliente = :estado " +
           "AND c.deleted = false")
    Long contarPorEstado(@Param("tenantId") Long tenantId, @Param("estado") EstadoCliente estado);
}
