package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.archivo.ArchivoResponse;
import com.inmobiliaria.terrenos.config.FileStorageProperties;
import com.inmobiliaria.terrenos.domain.entity.Archivo;
import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import com.inmobiliaria.terrenos.domain.repository.ArchivoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ArchivoMapper;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchivoServiceTest {

    @Mock
    private ArchivoRepository archivoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private ArchivoMapper archivoMapper;

    @Mock
    private FileStorageProperties fileStorageProperties;

    @InjectMocks
    private ArchivoService archivoService;

    private final Long tenantId = 1L;
    private Archivo archivo;
    private ArchivoResponse archivoResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        archivo = Archivo.builder()
                .nombreOriginal("plano.pdf")
                .nombreAlmacenado("uuid.pdf")
                .tipo(TipoArchivo.PLANO_PROYECTO)
                .esActivo(true)
                .build();
        archivo.setId(1L);
        archivo.setTenantId(tenantId);

        archivoResponse = new ArchivoResponse();
        archivoResponse.setId(1L);
        archivoResponse.setNombreOriginal("plano.pdf");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listarArchivos_Exitoso() {
        when(archivoRepository.findByTenantIdAndDeletedFalse(tenantId)).thenReturn(List.of(archivo));
        when(archivoMapper.toResponseList(any())).thenReturn(List.of(archivoResponse));

        List<ArchivoResponse> resultado = archivoService.listarArchivos(null, null, null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(archivoRepository, times(1)).findByTenantIdAndDeletedFalse(tenantId);
    }

    @Test
    void eliminarArchivo_Inexistente_LanzaExcepcion() {
        when(archivoRepository.findByIdAndTenantIdAndDeletedFalse(99L, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> archivoService.eliminarArchivo(99L));
    }

    @Test
    void eliminarArchivo_Exitoso() {
        when(archivoRepository.findByIdAndTenantIdAndDeletedFalse(1L, tenantId)).thenReturn(Optional.of(archivo));

        archivoService.eliminarArchivo(1L);

        assertTrue(archivo.getDeleted());
        assertFalse(archivo.getEsActivo());
        verify(archivoRepository, times(1)).save(archivo);
    }
}
