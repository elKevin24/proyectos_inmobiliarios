package com.inmobiliaria.terrenos.infrastructure.scheduler;

import com.inmobiliaria.terrenos.domain.entity.Apartado;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ApartadoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApartadoSchedulerTest {

    @Mock
    private ApartadoRepository apartadoRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @InjectMocks
    private ApartadoScheduler apartadoScheduler;

    @Test
    @DisplayName("Sin apartados vencidos no debe realizar actualizaciones")
    void testProcesarApartadosVencidos_SinResultados() {
        when(apartadoRepository.findByEstadoAndFechaVencimientoBeforeAndDeletedFalse(eq(EstadoApartado.ACTIVO), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        apartadoScheduler.procesarApartadosVencidos();

        verify(apartadoRepository, never()).save(any());
        verify(terrenoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Expirar apartado vencido y cambiar estado de terreno a DISPONIBLE")
    void testProcesarApartadosVencidos_Exito() {
        Apartado apartado = Apartado.builder()
                .id(100L)
                .tenantId(1L)
                .terrenoId(50L)
                .estado(EstadoApartado.ACTIVO)
                .fechaVencimiento(LocalDate.now().minusDays(2))
                .build();

        Terreno terreno = Terreno.builder()
                .id(50L)
                .numeroLote("A-10")
                .estado(EstadoTerreno.APARTADO)
                .build();

        when(apartadoRepository.findByEstadoAndFechaVencimientoBeforeAndDeletedFalse(eq(EstadoApartado.ACTIVO), any(LocalDate.class)))
                .thenReturn(List.of(apartado));
        when(terrenoRepository.findById(50L)).thenReturn(Optional.of(terreno));

        apartadoScheduler.procesarApartadosVencidos();

        assertEquals(EstadoApartado.VENCIDO, apartado.getEstado());
        assertEquals(EstadoTerreno.DISPONIBLE, terreno.getEstado());
        verify(apartadoRepository, times(1)).save(apartado);
        verify(terrenoRepository, times(1)).save(terreno);
    }
}
