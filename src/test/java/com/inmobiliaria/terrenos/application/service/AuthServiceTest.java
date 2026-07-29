package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.LoginRequest;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.domain.entity.Rol;
import com.inmobiliaria.terrenos.domain.entity.Tenant;
import com.inmobiliaria.terrenos.domain.entity.Usuario;
import com.inmobiliaria.terrenos.domain.repository.PermisoRepository;
import com.inmobiliaria.terrenos.domain.repository.RolRepository;
import com.inmobiliaria.terrenos.domain.repository.TenantRepository;
import com.inmobiliaria.terrenos.domain.repository.UsuarioRepository;
import com.inmobiliaria.terrenos.infrastructure.security.JwtService;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private Tenant tenant;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .nombre("Empresa Test")
                .activo(true)
                .build();
        tenant.setId(1L);

        usuario = Usuario.builder()
                .nombre("Carlos")
                .apellido("Pérez")
                .email("admin@test.com")
                .password("encoded_pass")
                .activo(true)
                .build();
        usuario.setId(10L);
        usuario.setTenantId(1L);

        userDetails = User.withUsername("admin@test.com")
                .password("encoded_pass")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void login_UsuarioDesactivado_LanzaExcepcion() {
        usuario.setActivo(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("123456");

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void login_TenantDesactivado_LanzaExcepcion() {
        tenant.setActivo(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("123456");

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    void login_Exitoso() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("123456");

        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(userDetailsService.loadUserByUsername("admin@test.com")).thenReturn(userDetails);
        when(jwtService.generateTokenWithTenant(userDetails, 1L)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        assertEquals(10L, response.getUserInfo().getId());
    }

    @Test
    void register_EmailEmpresaDuplicado_LanzaExcepcion() {
        RegisterRequest request = new RegisterRequest();
        request.setEmailEmpresa("empresa@test.com");

        when(tenantRepository.existsByEmail("empresa@test.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.register(request));
    }

    @Test
    void register_Exitoso() {
        RegisterRequest request = new RegisterRequest();
        request.setNombreEmpresa("Nueva Empresa");
        request.setEmailEmpresa("nueva@test.com");
        request.setNombre("Admin");
        request.setApellido("User");
        request.setEmail("admin@nueva.com");
        request.setPassword("pass123");

        Rol rolAdmin = Rol.builder().build();
        rolAdmin.setId(100L);

        when(tenantRepository.existsByEmail("nueva@test.com")).thenReturn(false);
        when(usuarioRepository.findByEmail("admin@nueva.com")).thenReturn(Optional.empty());
        when(tenantRepository.save(any())).thenReturn(tenant);
        when(permisoRepository.findAll()).thenReturn(Collections.emptyList());
        when(rolRepository.save(any())).thenReturn(rolAdmin);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(userDetailsService.loadUserByUsername("admin@test.com")).thenReturn(userDetails);
        when(jwtService.generateTokenWithTenant(userDetails, 1L)).thenReturn("access_token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(tenantRepository, times(1)).save(any());
        verify(usuarioRepository, times(1)).save(any());
    }
}
