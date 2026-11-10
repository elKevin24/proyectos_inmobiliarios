package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.apartado.ApartadoResponse;
import com.inmobiliaria.terrenos.application.dto.apartado.CreateApartadoRequest;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Cliente;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.ClienteRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ApartadoMapper;
import com.inmobiliaria.terrenos.shared.dto.PagedResponse;
import com.inmobiliaria.terrenos.shared.exception.ApartadoException;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio de gestión de apartados
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApartadoService {

    private final ApartadoRepository apartadoRepository;
    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;
    private final ApartadoMapper apartadoMapper;
    private final ProyectoContadorService proyectoContadorService;

    @Autowired(required = false)
    private ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ApartadoResponse> listarApartadosPaginado(Pageable pageable) {
        Long tenantId = getTenantId();
        Page<Apartado> apartadosPage = apartadoRepository.findByTenantIdAndDeletedFalse(tenantId, pageable);
        List<ApartadoResponse> content = enriquecerLista(apartadoMapper.toResponseList(apartadosPage.getContent()));
        return new PagedResponse<>(
                content,
                apartadosPage.getNumber(),
                apartadosPage.getSize(),
                apartadosPage.getTotalElements(),
                apartadosPage.getTotalPages(),
                apartadosPage.isFirst(),
                apartadosPage.isLast(),
                apartadosPage.isEmpty()
        );
    }

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    @Transactional(readOnly = true)
    public List<ApartadoResponse> listarApartados() {
        Long tenantId = getTenantId();
        log.debug("Listando apartados para tenant: {}", tenantId);
        return enriquecerLista(apartadoMapper.toResponseList(apartadoRepository.findByTenantIdAndDeletedFalse(tenantId)));
    }

    @Transactional(readOnly = true)
    public List<ApartadoResponse> listarApartadosVigentes() {
        Long tenantId = getTenantId();
        log.debug("Listando apartados vigentes para tenant: {}", tenantId);
        return enriquecerLista(apartadoMapper.toResponseList(
                apartadoRepository.findApartadosVigentes(tenantId, LocalDate.now())));
    }

    @Transactional(readOnly = true)
    public List<ApartadoResponse> listarApartadosVencidos() {
        Long tenantId = getTenantId();
        log.debug("Listando apartados vencidos para tenant: {}", tenantId);
        return enriquecerLista(apartadoMapper.toResponseList(
                apartadoRepository.findApartadosVencidos(tenantId, LocalDate.now())));
    }

    @Transactional(readOnly = true)
    public ApartadoResponse obtenerApartado(Long id) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo apartado {} para tenant: {}", id, tenantId);

        Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + id));

        return enriquecer(apartadoMapper.toResponse(apartado));
    }

    @Transactional
    public ApartadoResponse crearApartado(CreateApartadoRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando apartado para terreno {} - Cliente: {}", request.getTerrenoId(), request.getClienteNombre());

        // Validar que el terreno existe y está disponible
        Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(request.getTerrenoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + request.getTerrenoId()));

        if (terreno.getEstado() != EstadoTerreno.DISPONIBLE) {
            throw ApartadoException.terrenoNoDisponible(String.valueOf(terreno.getEstado()));
        }

        // Resolver datos del cliente si se envió clienteId y falta clienteNombre
        if ((request.getClienteNombre() == null || request.getClienteNombre().isBlank())
                && request.getClienteId() != null && clienteRepository != null) {
            Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(request.getClienteId(), tenantId)
                    .orElse(null);
            if (cliente != null) {
                request.setClienteNombre(cliente.getNombreCompleto());
                if (request.getClienteEmail() == null) {
                    request.setClienteEmail(cliente.getEmail());
                }
                if (request.getClienteTelefono() == null) {
                    request.setClienteTelefono(cliente.getTelefono());
                }
                if (request.getClienteDireccion() == null) {
                    request.setClienteDireccion(cliente.getDireccion());
                }
            }
        }

        // Resolver precioTotal desde el terreno si no fue enviado
        if (request.getPrecioTotal() == null) {
            BigDecimal precioTerreno = terreno.getPrecioFinal() != null ? terreno.getPrecioFinal() : terreno.getPrecioBase();
            request.setPrecioTotal(precioTerreno != null ? precioTerreno : request.getMontoApartado());
        }

        // Resolver fechas y duracionDias
        LocalDate fechaApartado = request.getFechaApartado() != null ? request.getFechaApartado() : LocalDate.now();
        Integer duracionDias = request.getDuracionDias();
        LocalDate fechaVencimiento = request.getFechaVencimiento();
        if (duracionDias == null) {
            if (fechaVencimiento != null && fechaVencimiento.isAfter(fechaApartado)) {
                duracionDias = (int) ChronoUnit.DAYS.between(fechaApartado, fechaVencimiento);
            } else {
                duracionDias = 30;
            }
            request.setDuracionDias(duracionDias);
        }
        if (fechaVencimiento == null) {
            fechaVencimiento = fechaApartado.plusDays(duracionDias);
        }

        // Validar monto de apartado
        if (request.getPrecioTotal() != null && request.getMontoApartado().compareTo(request.getPrecioTotal()) > 0) {
            throw ApartadoException.montoExcedePrecio();
        }

        Apartado apartado = apartadoMapper.toEntity(request);
        apartado.setTenantId(tenantId);
        if (apartado.getClienteId() == null && request.getClienteId() != null) {
            apartado.setClienteId(request.getClienteId());
        }
        if (apartado.getClienteNombre() == null || apartado.getClienteNombre().isBlank()) {
            apartado.setClienteNombre(request.getClienteNombre() != null && !request.getClienteNombre().isBlank()
                    ? request.getClienteNombre() : "Cliente");
        }
        if (apartado.getClienteTelefono() == null) {
            apartado.setClienteTelefono(request.getClienteTelefono() != null ? request.getClienteTelefono() : "");
        }
        if (apartado.getPrecioTotal() == null) {
            apartado.setPrecioTotal(request.getPrecioTotal());
        }
        if (apartado.getDuracionDias() == null) {
            apartado.setDuracionDias(duracionDias);
        }
        apartado.setFechaApartado(fechaApartado);
        apartado.setFechaVencimiento(fechaVencimiento);
        apartado.setEstado(EstadoApartado.ACTIVO);

        // Cambiar estado del terreno a APARTADO
        terreno.setEstado(EstadoTerreno.APARTADO);
        terrenoRepository.save(terreno);

        // Actualizar contadores del proyecto
        actualizarContadoresProyecto(terreno.getProyectoId());

        Apartado apartadoGuardado = apartadoRepository.save(apartado);
        log.info("Apartado creado con id: {}", apartadoGuardado.getId());

        ApartadoResponse response = apartadoMapper.toResponse(apartadoGuardado);
        if (response != null) {
            if (response.getTerrenoNumeroLote() == null) {
                response.setTerrenoNumeroLote(terreno.getNumeroLote());
            }
            if (response.getTerrenoManzana() == null) {
                response.setTerrenoManzana(terreno.getManzana());
            }
            if (response.getProyectoId() == null) {
                response.setProyectoId(terreno.getProyectoId());
            }
        }
        return enriquecer(response);
    }

    @Transactional
    public ApartadoResponse cancelarApartado(Long id, String motivo) {
        Long tenantId = getTenantId();
        log.info("Cancelando apartado {} para tenant: {}", id, tenantId);

        Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + id));

        if (apartado.getEstado() != EstadoApartado.ACTIVO) {
            throw ApartadoException.soloCancelarVigentes(String.valueOf(apartado.getEstado()));
        }

        apartado.setEstado(EstadoApartado.CANCELADO);
        if (motivo != null) {
            apartado.setObservaciones(apartado.getObservaciones() != null ?
                    apartado.getObservaciones() + "\nMotivo cancelación: " + motivo : "Motivo cancelación: " + motivo);
        }

        // Liberar el terreno
        Terreno terreno = terrenoRepository.findById(apartado.getTerrenoId()).orElse(null);
        if (terreno != null && terreno.getEstado() == EstadoTerreno.APARTADO) {
            terreno.setEstado(EstadoTerreno.DISPONIBLE);
            terrenoRepository.save(terreno);

            // Actualizar contadores del proyecto
            actualizarContadoresProyecto(terreno.getProyectoId());
        }

        Apartado apartadoActualizado = apartadoRepository.save(apartado);
        log.info("Apartado {} cancelado exitosamente", id);

        return enriquecer(apartadoMapper.toResponse(apartadoActualizado));
    }

    private ApartadoResponse enriquecer(ApartadoResponse response) {
        if (response == null) {
            return null;
        }
        if (response.getTerrenoId() != null &&
                (response.getTerrenoNumeroLote() == null || response.getProyectoId() == null)) {
            if (terrenoRepository != null) {
                terrenoRepository.findById(response.getTerrenoId()).ifPresent(terreno -> {
                    if (response.getTerrenoNumeroLote() == null) {
                        response.setTerrenoNumeroLote(terreno.getNumeroLote());
                    }
                    if (response.getTerrenoManzana() == null) {
                        response.setTerrenoManzana(terreno.getManzana());
                    }
                    if (response.getProyectoId() == null) {
                        response.setProyectoId(terreno.getProyectoId());
                    }
                    if (response.getProyectoNombre() == null && terreno.getProyecto() != null) {
                        response.setProyectoNombre(terreno.getProyecto().getNombre());
                    }
                });
            }
        }
        if (response.getProyectoId() != null && response.getProyectoNombre() == null && proyectoRepository != null) {
            proyectoRepository.findById(response.getProyectoId())
                    .ifPresent(proyecto -> response.setProyectoNombre(proyecto.getNombre()));
        }
        return response;
    }

    private List<ApartadoResponse> enriquecerLista(List<ApartadoResponse> responses) {
        if (responses != null) {
            responses.forEach(this::enriquecer);
        }
        return responses;
    }

    @Transactional
    public void eliminarApartado(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando apartado {} para tenant: {}", id, tenantId);

        Apartado apartado = apartadoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartado no encontrado con id: " + id));

        // Solo se pueden eliminar apartados cancelados o vencidos
        if (apartado.getEstado() == EstadoApartado.ACTIVO) {
            throw ApartadoException.noEliminarVigente();
        }

        apartado.softDelete();
        apartadoRepository.save(apartado);
        log.info("Apartado {} eliminado exitosamente", id);
    }

    private void actualizarContadoresProyecto(Long proyectoId) {
        if (proyectoContadorService != null) {
            proyectoContadorService.sincronizarContadores(proyectoId);
            return;
        }
        if (proyectoRepository == null) {
            return;
        }
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null || proyecto.getDeleted()) {
            return;
        }

        Long tenantId = proyecto.getTenantId();
        long totalTerrenos = terrenoRepository.countByTenantIdAndProyectoIdAndDeletedFalse(tenantId, proyectoId);
        long disponibles = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.DISPONIBLE);
        long apartados = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.APARTADO);
        long vendidos = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyectoId, EstadoTerreno.VENDIDO);

        proyecto.setTotalTerrenos((int) totalTerrenos);
        proyecto.setTerrenosDisponibles((int) disponibles);
        proyecto.setTerrenosApartados((int) apartados);
        proyecto.setTerrenosVendidos((int) vendidos);

        proyectoRepository.save(proyecto);
        log.debug("Contadores del proyecto {} actualizados", proyectoId);
    }
}
