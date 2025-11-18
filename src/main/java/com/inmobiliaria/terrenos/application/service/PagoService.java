package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.pago.*;
import com.inmobiliaria.terrenos.domain.entity.*;
import com.inmobiliaria.terrenos.domain.enums.*;
import com.inmobiliaria.terrenos.domain.repository.*;
import com.inmobiliaria.terrenos.infrastructure.security.SecurityUtils;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.*;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de pagos y financiamiento
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final PlanPagoRepository planPagoRepository;
    private final AmortizacionRepository amortizacionRepository;
    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;

    private final PlanPagoMapper planPagoMapper;
    private final AmortizacionMapper amortizacionMapper;
    private final PagoMapper pagoMapper;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    // ==================== GESTIÓN DE PLANES DE PAGO ====================

    @Transactional
    public PlanPagoResponse crearPlanPago(CreatePlanPagoRequest request) {
        Long tenantId = getTenantId();
        log.info("Creando plan de pago para venta: {}", request.getVentaId());

        // Validar que la venta existe y no tiene plan de pago
        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(request.getVentaId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        if (planPagoRepository.existePlanParaVenta(tenantId, request.getVentaId())) {
            throw new BusinessException("Ya existe un plan de pago para esta venta", HttpStatus.CONFLICT);
        }

        // Crear el plan de pago
        PlanPago planPago = planPagoMapper.toEntity(request);
        planPago.setTenantId(tenantId);
        planPago.setClienteId(venta.getClienteId());
        planPago.setCreatedBy(SecurityUtils.getCurrentUser());
        planPago.setUpdatedBy(SecurityUtils.getCurrentUser());

        // Calcular fecha del último pago
        LocalDate fechaUltimoPago = calcularFechaUltimoPago(
                request.getFechaPrimerPago(),
                request.getNumeroPagos(),
                request.getFrecuenciaPago()
        );
        planPago.setFechaUltimoPago(fechaUltimoPago);

        PlanPago planPagoGuardado = planPagoRepository.save(planPago);
        log.info("Plan de pago creado con ID: {}", planPagoGuardado.getId());

        // Generar tabla de amortización
        generarAmortizaciones(planPagoGuardado);

        return convertirAPlanPagoResponse(planPagoGuardado);
    }

    @Transactional(readOnly = true)
    public PlanPagoResponse obtenerPlanPago(Long id) {
        Long tenantId = getTenantId();
        PlanPago planPago = planPagoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de pago no encontrado"));
        return convertirAPlanPagoResponse(planPago);
    }

    @Transactional(readOnly = true)
    public List<PlanPagoResponse> listarPlanesPago() {
        Long tenantId = getTenantId();
        List<PlanPago> planesPago = planPagoRepository.findByTenantIdAndDeletedFalse(tenantId);
        return planesPago.stream()
                .map(this::convertirAPlanPagoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlanPagoResponse obtenerPlanPagoPorVenta(Long ventaId) {
        Long tenantId = getTenantId();
        PlanPago planPago = planPagoRepository.findByTenantIdAndVentaIdAndDeletedFalse(tenantId, ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró plan de pago para esta venta"));
        return convertirAPlanPagoResponse(planPago);
    }

    @Transactional
    public PlanPagoResponse actualizarPlanPago(Long id, UpdatePlanPagoRequest request) {
        Long tenantId = getTenantId();
        PlanPago planPago = planPagoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de pago no encontrado"));

        planPagoMapper.updateEntityFromRequest(request, planPago);
        planPago.setUpdatedBy(SecurityUtils.getCurrentUser());

        PlanPago actualizado = planPagoRepository.save(planPago);
        log.info("Plan de pago actualizado: {}", id);

        return convertirAPlanPagoResponse(actualizado);
    }

    // ==================== GENERACIÓN DE AMORTIZACIONES ====================

    @Transactional
    public void generarAmortizaciones(PlanPago planPago) {
        log.info("Generando amortizaciones para plan de pago: {}", planPago.getId());

        BigDecimal montoFinanciado = planPago.getMontoFinanciado();
        Integer numeroPagos = planPago.getNumeroPagos();
        BigDecimal tasaMensual = planPago.getTasaInteresMensual();
        Boolean aplicaInteres = planPago.getAplicaInteres();

        List<Amortizacion> amortizaciones = new ArrayList<>();
        BigDecimal saldoRestante = montoFinanciado;
        LocalDate fechaVencimiento = planPago.getFechaPrimerPago();

        if (aplicaInteres && tasaMensual.compareTo(BigDecimal.ZERO) > 0) {
            // Amortización francesa (cuota fija con interés)
            BigDecimal cuotaFija = calcularCuotaFija(montoFinanciado, tasaMensual, numeroPagos);

            for (int i = 1; i <= numeroPagos; i++) {
                BigDecimal interes = saldoRestante.multiply(tasaMensual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP));
                BigDecimal capital = cuotaFija.subtract(interes);

                // Ajustar última cuota para eliminar diferencias por redondeo
                if (i == numeroPagos) {
                    capital = saldoRestante;
                }

                saldoRestante = saldoRestante.subtract(capital);

                Amortizacion amortizacion = crearAmortizacion(
                        planPago, i, capital, interes, cuotaFija, fechaVencimiento, saldoRestante
                );
                amortizaciones.add(amortizacion);

                fechaVencimiento = calcularSiguienteFechaVencimiento(fechaVencimiento, planPago.getFrecuenciaPago());
            }
        } else {
            // Sin interés - capital dividido en partes iguales
            BigDecimal capitalPorCuota = montoFinanciado.divide(new BigDecimal(numeroPagos), 2, RoundingMode.HALF_UP);

            for (int i = 1; i <= numeroPagos; i++) {
                BigDecimal capital = capitalPorCuota;

                // Ajustar última cuota
                if (i == numeroPagos) {
                    capital = saldoRestante;
                }

                saldoRestante = saldoRestante.subtract(capital);

                Amortizacion amortizacion = crearAmortizacion(
                        planPago, i, capital, BigDecimal.ZERO, capital, fechaVencimiento, saldoRestante
                );
                amortizaciones.add(amortizacion);

                fechaVencimiento = calcularSiguienteFechaVencimiento(fechaVencimiento, planPago.getFrecuenciaPago());
            }
        }

        amortizacionRepository.saveAll(amortizaciones);
        log.info("Generadas {} amortizaciones", amortizaciones.size());
    }

    private Amortizacion crearAmortizacion(PlanPago planPago, int numeroCuota, BigDecimal capital,
                                            BigDecimal interes, BigDecimal montoCuota,
                                            LocalDate fechaVencimiento, BigDecimal saldoRestante) {
        return Amortizacion.builder()
                .tenantId(planPago.getTenantId())
                .planPagoId(planPago.getId())
                .numeroCuota(numeroCuota)
                .capital(capital.setScale(2, RoundingMode.HALF_UP))
                .interes(interes.setScale(2, RoundingMode.HALF_UP))
                .montoCuota(montoCuota.setScale(2, RoundingMode.HALF_UP))
                .montoPagado(BigDecimal.ZERO)
                .montoPendiente(montoCuota.setScale(2, RoundingMode.HALF_UP))
                .moraAcumulada(BigDecimal.ZERO)
                .diasAtraso(0)
                .fechaVencimiento(fechaVencimiento)
                .estado(EstadoAmortizacion.PENDIENTE)
                .saldoRestante(saldoRestante.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * Calcula la cuota fija usando la fórmula francesa de amortización
     * Cuota = P * [i * (1 + i)^n] / [(1 + i)^n - 1]
     */
    private BigDecimal calcularCuotaFija(BigDecimal principal, BigDecimal tasaMensual, Integer numeroPagos) {
        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(numeroPagos), 2, RoundingMode.HALF_UP);
        }

        BigDecimal i = tasaMensual.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal unoPlusI = BigDecimal.ONE.add(i);
        BigDecimal unoPlusIPotenciaN = unoPlusI.pow(numeroPagos);

        BigDecimal numerador = principal.multiply(i).multiply(unoPlusIPotenciaN);
        BigDecimal denominador = unoPlusIPotenciaN.subtract(BigDecimal.ONE);

        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private LocalDate calcularSiguienteFechaVencimiento(LocalDate fechaActual, FrecuenciaPago frecuencia) {
        return fechaActual.plusDays(frecuencia.getDias());
    }

    private LocalDate calcularFechaUltimoPago(LocalDate fechaPrimerPago, Integer numeroPagos, FrecuenciaPago frecuencia) {
        return fechaPrimerPago.plusDays((long) (numeroPagos - 1) * frecuencia.getDias());
    }

    // ==================== APLICACIÓN DE PAGOS ====================

    @Transactional
    public PagoResponse aplicarPago(CreatePagoRequest request) {
        Long tenantId = getTenantId();
        log.info("Aplicando pago de {} para plan de pago: {}", request.getMontoPagado(), request.getPlanPagoId());

        // Obtener plan de pago
        PlanPago planPago = planPagoRepository.findByIdAndTenantIdAndDeletedFalse(request.getPlanPagoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de pago no encontrado"));

        // Actualizar estados vencidos antes de aplicar el pago
        actualizarEstadosVencidos(planPago);

        // Crear el registro de pago
        Pago pago = pagoMapper.toEntity(request);
        pago.setTenantId(tenantId);
        pago.setClienteId(planPago.getClienteId());
        pago.setEstado(EstadoPago.APLICADO);
        pago.setMontoACapital(BigDecimal.ZERO);
        pago.setMontoAInteres(BigDecimal.ZERO);
        pago.setMontoAMora(BigDecimal.ZERO);
        pago.setUsuarioId(1L); // TODO: Obtener del contexto de seguridad
        pago.setCreatedBy(SecurityUtils.getCurrentUser());
        pago.setUpdatedBy(SecurityUtils.getCurrentUser());

        BigDecimal montoRestante = request.getMontoPagado();

        // Determinar amortizaciones a pagar
        List<Amortizacion> amortizacionesAPagar;
        if (request.getAmortizacionId() != null) {
            // Pago específico a una amortización
            Amortizacion amortizacion = amortizacionRepository.findByIdAndTenantIdAndDeletedFalse(request.getAmortizacionId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Amortización no encontrada"));
            amortizacionesAPagar = List.of(amortizacion);
        } else {
            // Pago automático a las cuotas más antiguas pendientes
            amortizacionesAPagar = amortizacionRepository.findVencidasHastaFecha(
                    tenantId, planPago.getId(), request.getFechaPago()
            );

            // Si no hay vencidas, tomar las pendientes en orden
            if (amortizacionesAPagar.isEmpty()) {
                amortizacionesAPagar = amortizacionRepository.findPendientesByPlanPagoId(tenantId, planPago.getId());
            }
        }

        if (amortizacionesAPagar.isEmpty()) {
            throw new BusinessException("No hay cuotas pendientes de pago", HttpStatus.BAD_REQUEST);
        }

        // Aplicar pago a cada amortización en orden
        for (Amortizacion amortizacion : amortizacionesAPagar) {
            if (montoRestante.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            montoRestante = aplicarPagoAAmortizacion(amortizacion, montoRestante, pago, request.getFechaPago(), planPago);
        }

        // Guardar el pago
        Pago pagoGuardado = pagoRepository.save(pago);
        log.info("Pago aplicado con ID: {}", pagoGuardado.getId());

        return convertirAPagoResponse(pagoGuardado);
    }

    private BigDecimal aplicarPagoAAmortizacion(Amortizacion amortizacion, BigDecimal montoDisponible,
                                                  Pago pago, LocalDate fechaPago, PlanPago planPago) {
        log.debug("Aplicando pago a amortización {}: monto disponible {}", amortizacion.getNumeroCuota(), montoDisponible);

        BigDecimal montoRestante = montoDisponible;

        // Prioridad de pago: 1) Mora, 2) Interés, 3) Capital

        // 1. Pagar mora
        if (amortizacion.getMoraAcumulada().compareTo(BigDecimal.ZERO) > 0 && montoRestante.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pagoMora = montoRestante.min(amortizacion.getMoraAcumulada());
            amortizacion.setMoraAcumulada(amortizacion.getMoraAcumulada().subtract(pagoMora));
            pago.setMontoAMora(pago.getMontoAMora().add(pagoMora));
            montoRestante = montoRestante.subtract(pagoMora);
            log.debug("Pagado mora: {}, restante: {}", pagoMora, montoRestante);
        }

        // 2. Pagar interés
        BigDecimal interesPendiente = amortizacion.getInteres().subtract(
                amortizacion.getMontoPagado().min(amortizacion.getInteres())
        );
        if (interesPendiente.compareTo(BigDecimal.ZERO) > 0 && montoRestante.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pagoInteres = montoRestante.min(interesPendiente);
            pago.setMontoAInteres(pago.getMontoAInteres().add(pagoInteres));
            montoRestante = montoRestante.subtract(pagoInteres);
            log.debug("Pagado interés: {}, restante: {}", pagoInteres, montoRestante);
        }

        // 3. Pagar capital
        if (montoRestante.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pagoCapital = montoRestante.min(amortizacion.getMontoPendiente());
            pago.setMontoACapital(pago.getMontoACapital().add(pagoCapital));
            montoRestante = montoRestante.subtract(pagoCapital);
            log.debug("Pagado capital: {}, restante: {}", pagoCapital, montoRestante);
        }

        // Actualizar la amortizacion
        BigDecimal totalPagadoEnEstaAmortizacion = montoDisponible.subtract(montoRestante);
        amortizacion.setMontoPagado(amortizacion.getMontoPagado().add(totalPagadoEnEstaAmortizacion));
        amortizacion.setMontoPendiente(amortizacion.getMontoCuota().subtract(amortizacion.getMontoPagado()));

        // Actualizar estado
        if (amortizacion.getMontoPendiente().compareTo(BigDecimal.ZERO) <= 0) {
            amortizacion.setEstado(EstadoAmortizacion.PAGADO);
            amortizacion.setFechaPago(fechaPago);
            amortizacion.setDiasAtraso(0);
        } else if (amortizacion.getMontoPagado().compareTo(BigDecimal.ZERO) > 0) {
            amortizacion.setEstado(EstadoAmortizacion.PARCIALMENTE_PAGADO);
        }

        amortizacionRepository.save(amortizacion);

        // Asignar este pago a esta amortización si es el único pago
        if (pago.getAmortizacionId() == null) {
            pago.setAmortizacionId(amortizacion.getId());
        }

        return montoRestante;
    }

    // ==================== CÁLCULO DE MORA ====================

    @Transactional
    public void actualizarEstadosVencidos(PlanPago planPago) {
        log.debug("Actualizando estados vencidos para plan de pago: {}", planPago.getId());

        List<Amortizacion> amortizaciones = amortizacionRepository.findVencidasHastaFecha(
                planPago.getTenantId(), planPago.getId(), LocalDate.now()
        );

        for (Amortizacion amortizacion : amortizaciones) {
            // Calcular días de atraso
            long diasAtraso = ChronoUnit.DAYS.between(amortizacion.getFechaVencimiento(), LocalDate.now());

            // Aplicar días de gracia
            diasAtraso -= planPago.getDiasGracia();
            if (diasAtraso < 0) diasAtraso = 0;

            amortizacion.setDiasAtraso((int) diasAtraso);

            if (diasAtraso > 0) {
                // Calcular mora
                BigDecimal mora = calcularMora(
                        amortizacion.getMontoPendiente(),
                        planPago.getTasaMoraMensual(),
                        (int) diasAtraso
                );
                amortizacion.setMoraAcumulada(mora);

                // Actualizar estado
                if (amortizacion.getEstado() == EstadoAmortizacion.PENDIENTE) {
                    amortizacion.setEstado(EstadoAmortizacion.VENCIDO);
                }
            }

            amortizacionRepository.save(amortizacion);
        }
    }

    /**
     * Calcula la mora basándose en el monto pendiente, tasa mensual y días de atraso
     * Mora = MontoPendiente * (TasaMoraMensual / 30) * DiasAtraso
     */
    private BigDecimal calcularMora(BigDecimal montoPendiente, BigDecimal tasaMoraMensual, int diasAtraso) {
        if (tasaMoraMensual == null || tasaMoraMensual.compareTo(BigDecimal.ZERO) == 0 || diasAtraso <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tasaDiaria = tasaMoraMensual.divide(new BigDecimal("30"), 6, RoundingMode.HALF_UP);
        BigDecimal tasaDecimal = tasaDiaria.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        BigDecimal mora = montoPendiente.multiply(tasaDecimal).multiply(new BigDecimal(diasAtraso));

        return mora.setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== ESTADO DE CUENTA ====================

    @Transactional(readOnly = true)
    public EstadoCuentaResponse obtenerEstadoCuenta(Long planPagoId) {
        Long tenantId = getTenantId();
        log.info("Generando estado de cuenta para plan de pago: {}", planPagoId);

        // Obtener plan de pago
        PlanPago planPago = planPagoRepository.findByIdAndTenantIdAndDeletedFalse(planPagoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de pago no encontrado"));

        // Obtener amortizaciones
        List<Amortizacion> amortizaciones = amortizacionRepository.findByPlanPagoId(tenantId, planPagoId);
        List<AmortizacionResponse> amortizacionesResponse = amortizacionMapper.toResponseList(amortizaciones);

        // Obtener pagos
        List<Pago> pagos = pagoRepository.findByPlanPagoId(tenantId, planPagoId);
        List<PagoResponse> pagosResponse = pagos.stream()
                .map(this::convertirAPagoResponse)
                .collect(Collectors.toList());

        // Obtener información de la venta y cliente
        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(planPago.getVentaId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        String clienteNombre = "";
        String clienteEmail = "";
        String clienteTelefono = "";
        if (planPago.getClienteId() != null) {
            Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(planPago.getClienteId(), tenantId).orElse(null);
            if (cliente != null) {
                clienteNombre = cliente.getNombreCompleto();
                clienteEmail = cliente.getEmail();
                clienteTelefono = cliente.getTelefono();
            }
        }

        String terrenoNumeroLote = "";
        String proyectoNombre = "";
        if (venta.getTerrenoId() != null) {
            Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(venta.getTerrenoId(), tenantId).orElse(null);
            if (terreno != null) {
                terrenoNumeroLote = terreno.getNumeroLote();
                if (terreno.getProyectoId() != null) {
                    Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(terreno.getProyectoId(), tenantId).orElse(null);
                    if (proyecto != null) {
                        proyectoNombre = proyecto.getNombre();
                    }
                }
            }
        }

        // Calcular resumen financiero
        EstadoCuentaResponse.ResumenFinanciero resumen = calcularResumenFinanciero(planPago, amortizaciones, pagos);

        // Obtener próximas amortizaciones (próximas 3 pendientes)
        List<AmortizacionResponse> proximasAmortizaciones = amortizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmortizacion.PENDIENTE)
                .limit(3)
                .map(amortizacionMapper::toResponse)
                .collect(Collectors.toList());

        // Obtener amortizaciones vencidas
        List<AmortizacionResponse> amortizacionesVencidas = amortizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmortizacion.VENCIDO)
                .map(amortizacionMapper::toResponse)
                .collect(Collectors.toList());

        return EstadoCuentaResponse.builder()
                .planPago(convertirAPlanPagoResponse(planPago))
                .clienteId(planPago.getClienteId())
                .clienteNombre(clienteNombre)
                .clienteEmail(clienteEmail)
                .clienteTelefono(clienteTelefono)
                .ventaId(planPago.getVentaId())
                .terrenoNumeroLote(terrenoNumeroLote)
                .proyectoNombre(proyectoNombre)
                .amortizaciones(amortizacionesResponse)
                .pagos(pagosResponse)
                .resumen(resumen)
                .proximasAmortizaciones(proximasAmortizaciones)
                .amortizacionesVencidas(amortizacionesVencidas)
                .build();
    }

    private EstadoCuentaResponse.ResumenFinanciero calcularResumenFinanciero(
            PlanPago planPago, List<Amortizacion> amortizaciones, List<Pago> pagos) {

        // Calcular totales
        BigDecimal totalPagado = amortizaciones.stream()
                .map(Amortizacion::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPendiente = amortizaciones.stream()
                .map(Amortizacion::getMontoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagadoCapital = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.APLICADO)
                .map(Pago::getMontoACapital)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagadoInteres = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.APLICADO)
                .map(Pago::getMontoAInteres)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagadoMora = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.APLICADO)
                .map(Pago::getMontoAMora)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal moraAcumuladaTotal = amortizaciones.stream()
                .map(Amortizacion::getMoraAcumulada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Contar cuotas por estado
        long cuotasPagadas = amortizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmortizacion.PAGADO)
                .count();

        long cuotasPendientes = amortizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmortizacion.PENDIENTE)
                .count();

        long cuotasVencidas = amortizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmortizacion.VENCIDO)
                .count();

        long cuotasParciales = amortizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmortizacion.PARCIALMENTE_PAGADO)
                .count();

        // Próximo vencimiento
        Amortizacion proximaAmortizacion = amortizaciones.stream()
                .filter(a -> a.getEstado() == EstadoAmortizacion.PENDIENTE)
                .findFirst()
                .orElse(null);

        LocalDate proximoVencimiento = null;
        BigDecimal montoProximaCuota = BigDecimal.ZERO;
        Integer diasParaProximoPago = null;

        if (proximaAmortizacion != null) {
            proximoVencimiento = proximaAmortizacion.getFechaVencimiento();
            montoProximaCuota = proximaAmortizacion.getMontoCuota();
            diasParaProximoPago = (int) ChronoUnit.DAYS.between(LocalDate.now(), proximoVencimiento);
        }

        // Días de atraso máximo
        Integer diasAtrasoMaximo = amortizaciones.stream()
                .map(Amortizacion::getDiasAtraso)
                .max(Integer::compareTo)
                .orElse(0);

        // Porcentaje pagado
        BigDecimal montoTotalCuotas = amortizaciones.stream()
                .map(Amortizacion::getMontoCuota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal porcentajePagado = BigDecimal.ZERO;
        if (montoTotalCuotas.compareTo(BigDecimal.ZERO) > 0) {
            porcentajePagado = totalPagado.multiply(new BigDecimal("100"))
                    .divide(montoTotalCuotas, 2, RoundingMode.HALF_UP);
        }

        // Saldo de capital
        BigDecimal saldoCapital = proximaAmortizacion != null ? proximaAmortizacion.getSaldoRestante() : BigDecimal.ZERO;

        // Intereses pendientes
        BigDecimal interesesPendientes = amortizaciones.stream()
                .filter(a -> a.getEstado() != EstadoAmortizacion.PAGADO)
                .map(Amortizacion::getInteres)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAdeudado = totalPendiente.add(moraAcumuladaTotal);

        return EstadoCuentaResponse.ResumenFinanciero.builder()
                .montoTotal(planPago.getMontoTotal())
                .enganche(planPago.getEnganche())
                .montoFinanciado(planPago.getMontoFinanciado())
                .totalPagado(totalPagado)
                .totalPendiente(totalPendiente)
                .porcentajePagado(porcentajePagado)
                .totalPagadoCapital(totalPagadoCapital)
                .totalPagadoInteres(totalPagadoInteres)
                .totalPagadoMora(totalPagadoMora)
                .saldoCapital(saldoCapital)
                .interesesPendientes(interesesPendientes)
                .moraPendiente(moraAcumuladaTotal)
                .totalAdeudado(totalAdeudado)
                .totalCuotas(amortizaciones.size())
                .cuotasPagadas((int) cuotasPagadas)
                .cuotasPendientes((int) cuotasPendientes)
                .cuotasVencidas((int) cuotasVencidas)
                .cuotasParcialesPagadas((int) cuotasParciales)
                .proximoVencimiento(proximoVencimiento)
                .montoproximaCuota(montoProximaCuota)
                .diasParaProximoPago(diasParaProximoPago)
                .diasAtrasoMaximo(diasAtrasoMaximo)
                .moraAcumuladaTotal(moraAcumuladaTotal)
                .estaCorriente(cuotasVencidas == 0)
                .tienePagosVencidos(cuotasVencidas > 0)
                .build();
    }

    // ==================== TABLA DE AMORTIZACIÓN ====================

    @Transactional(readOnly = true)
    public TablaAmortizacionResponse obtenerTablaAmortizacion(Long planPagoId) {
        Long tenantId = getTenantId();

        PlanPago planPago = planPagoRepository.findByIdAndTenantIdAndDeletedFalse(planPagoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de pago no encontrado"));

        List<Amortizacion> amortizaciones = amortizacionRepository.findByPlanPagoId(tenantId, planPagoId);
        List<AmortizacionResponse> amortizacionesResponse = amortizacionMapper.toResponseList(amortizaciones);

        // Calcular totales
        BigDecimal totalCapital = amortizaciones.stream().map(Amortizacion::getCapital).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInteres = amortizaciones.stream().map(Amortizacion::getInteres).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPagos = amortizaciones.stream().map(Amortizacion::getMontoCuota).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPagado = amortizaciones.stream().map(Amortizacion::getMontoPagado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPendiente = amortizaciones.stream().map(Amortizacion::getMontoPendiente).reduce(BigDecimal.ZERO, BigDecimal::add);

        TablaAmortizacionResponse.Totales totales = TablaAmortizacionResponse.Totales.builder()
                .totalCapital(totalCapital)
                .totalInteres(totalInteres)
                .totalPagos(totalPagos)
                .totalPagado(totalPagado)
                .totalPendiente(totalPendiente)
                .build();

        String clienteNombre = "";
        if (planPago.getClienteId() != null) {
            Cliente cliente = clienteRepository.findByIdAndTenantIdAndDeletedFalse(planPago.getClienteId(), tenantId).orElse(null);
            if (cliente != null) {
                clienteNombre = cliente.getNombreCompleto();
            }
        }

        String terrenoNumeroLote = "";
        Venta venta = ventaRepository.findByIdAndTenantIdAndDeletedFalse(planPago.getVentaId(), tenantId).orElse(null);
        if (venta != null && venta.getTerrenoId() != null) {
            Terreno terreno = terrenoRepository.findByIdAndTenantIdAndDeletedFalse(venta.getTerrenoId(), tenantId).orElse(null);
            if (terreno != null) {
                terrenoNumeroLote = terreno.getNumeroLote();
            }
        }

        return TablaAmortizacionResponse.builder()
                .planPagoId(planPagoId)
                .clienteNombre(clienteNombre)
                .terrenoNumeroLote(terrenoNumeroLote)
                .montoFinanciado(planPago.getMontoFinanciado())
                .tasaInteresAnual(planPago.getTasaInteresAnual())
                .numeroPagos(planPago.getNumeroPagos())
                .frecuenciaPago(planPago.getFrecuenciaPago().getDescripcion())
                .amortizaciones(amortizacionesResponse)
                .totales(totales)
                .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private PlanPagoResponse convertirAPlanPagoResponse(PlanPago planPago) {
        PlanPagoResponse response = planPagoMapper.toResponse(planPago);

        // Agregar estadísticas calculadas
        Long tenantId = planPago.getTenantId();
        Long planPagoId = planPago.getId();

        response.setTotalAmortizaciones(amortizacionRepository.contarByPlanPagoId(tenantId, planPagoId).intValue());
        response.setAmortizacionesPagadas(amortizacionRepository.contarByEstado(tenantId, planPagoId, EstadoAmortizacion.PAGADO).intValue());
        response.setAmortizacionesPendientes(amortizacionRepository.contarByEstado(tenantId, planPagoId, EstadoAmortizacion.PENDIENTE).intValue());
        response.setAmortizacionesVencidas(amortizacionRepository.contarByEstado(tenantId, planPagoId, EstadoAmortizacion.VENCIDO).intValue());
        response.setTotalPagado(amortizacionRepository.sumarTotalPagado(tenantId, planPagoId));
        response.setTotalPendiente(amortizacionRepository.sumarTotalPendiente(tenantId, planPagoId));

        // Calcular porcentaje de avance
        BigDecimal totalPagado = response.getTotalPagado();
        BigDecimal montoFinanciado = planPago.getMontoFinanciado();
        if (montoFinanciado.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentaje = totalPagado.multiply(new BigDecimal("100"))
                    .divide(montoFinanciado, 2, RoundingMode.HALF_UP);
            response.setPorcentajeAvance(porcentaje);
        } else {
            response.setPorcentajeAvance(BigDecimal.ZERO);
        }

        return response;
    }

    private PagoResponse convertirAPagoResponse(Pago pago) {
        PagoResponse response = pagoMapper.toResponse(pago);
        // TODO: Agregar nombre de usuario si es necesario
        return response;
    }
}
