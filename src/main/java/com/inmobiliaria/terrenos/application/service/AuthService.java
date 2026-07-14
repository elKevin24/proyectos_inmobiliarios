package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.LoginRequest;
import com.inmobiliaria.terrenos.application.dto.auth.RefreshTokenRequest;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.domain.entity.Permiso;
import com.inmobiliaria.terrenos.domain.entity.Rol;
import com.inmobiliaria.terrenos.domain.entity.Tenant;
import com.inmobiliaria.terrenos.domain.entity.Usuario;
import com.inmobiliaria.terrenos.domain.enums.RolEnum;
import com.inmobiliaria.terrenos.domain.repository.PermisoRepository;
import com.inmobiliaria.terrenos.domain.repository.RolRepository;
import com.inmobiliaria.terrenos.domain.repository.TenantRepository;
import com.inmobiliaria.terrenos.domain.repository.UsuarioRepository;
import com.inmobiliaria.terrenos.infrastructure.security.JwtService;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${app.security.jwt.expiration}")
    private Long jwtExpiration;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + request.getEmail()));

        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario desactivado", HttpStatus.FORBIDDEN);
        }

        Tenant tenant = tenantRepository.findById(usuario.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));

        if (!tenant.getActivo()) {
            throw new BusinessException("Empresa desactivada. Contacte al administrador.", HttpStatus.FORBIDDEN);
        }

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String accessToken = jwtService.generateTokenWithTenant(userDetails, tenant.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Login exitoso para usuario: {} de tenant: {}", usuario.getEmail(), tenant.getNombre());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userInfo(AuthResponse.UserInfo.builder()
                        .id(usuario.getId())
                        .nombre(usuario.getNombreCompleto())
                        .email(usuario.getEmail())
                        .tenantId(tenant.getId())
                        .tenantNombre(tenant.getNombre())
                        .build())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registro de nueva empresa: {}", request.getNombreEmpresa());

        if (tenantRepository.existsByEmail(request.getEmailEmpresa())) {
            throw new BusinessException("Ya existe una empresa con ese email", HttpStatus.CONFLICT);
        }

        if (request.getRfc() != null && tenantRepository.existsByRfc(request.getRfc())) {
            throw new BusinessException("Ya existe una empresa con ese RFC", HttpStatus.CONFLICT);
        }

        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Ya existe un usuario con ese email", HttpStatus.CONFLICT);
        }

        Tenant tenant = Tenant.builder()
                .nombre(request.getNombreEmpresa())
                .email(request.getEmailEmpresa())
                .telefono(request.getTelefonoEmpresa())
                .direccion(request.getDireccionEmpresa())
                .razonSocial(request.getRazonSocial())
                .rfc(request.getRfc())
                .activo(true)
                .maxUsuarios(10)
                .maxProyectos(5)
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("Tenant creado con ID: {}", tenant.getId());

        Rol rolAdmin = crearRolAdminParaTenant(tenant.getId());

        Usuario admin = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .activo(true)
                .roles(Set.of(rolAdmin))
                .build();
        admin.setTenantId(tenant.getId());

        admin = usuarioRepository.save(admin);
        log.info("Usuario administrador creado con ID: {} para tenant: {}", admin.getId(), tenant.getId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(admin.getEmail());
        String accessToken = jwtService.generateTokenWithTenant(userDetails, tenant.getId());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Registro completado exitosamente para empresa: {}", tenant.getNombre());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userInfo(AuthResponse.UserInfo.builder()
                        .id(admin.getId())
                        .nombre(admin.getNombreCompleto())
                        .email(admin.getEmail())
                        .tenantId(tenant.getId())
                        .tenantNombre(tenant.getNombre())
                        .build())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Solicitud de refresh token");

        String userEmail = jwtService.extractUsername(request.getRefreshToken());

        if (userEmail == null) {
            throw new BusinessException("Refresh token inválido", HttpStatus.UNAUTHORIZED);
        }

        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario desactivado", HttpStatus.FORBIDDEN);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new BusinessException("Refresh token expirado o inválido", HttpStatus.UNAUTHORIZED);
        }

        Tenant tenant = tenantRepository.findById(usuario.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));

        String newAccessToken = jwtService.generateTokenWithTenant(userDetails, tenant.getId());

        log.info("Access token refrescado para usuario: {}", userEmail);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userInfo(AuthResponse.UserInfo.builder()
                        .id(usuario.getId())
                        .nombre(usuario.getNombreCompleto())
                        .email(usuario.getEmail())
                        .tenantId(tenant.getId())
                        .tenantNombre(tenant.getNombre())
                        .build())
                .build();
    }

    private Rol crearRolAdminParaTenant(Long tenantId) {
        List<Permiso> todosLosPermisos = permisoRepository.findAll();

        Rol rolAdmin = Rol.builder()
                .nombre(RolEnum.ADMIN)
                .descripcion("Administrador con acceso total")
                .permisos(new HashSet<>(todosLosPermisos))
                .build();
        rolAdmin.setTenantId(tenantId);

        return rolRepository.save(rolAdmin);
    }
}
