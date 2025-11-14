package com.inmobiliaria.terrenos.infrastructure.security;

import com.inmobiliaria.terrenos.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * Implementación personalizada de UserDetailsService para Spring Security
 * 
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario desactivado: " + email);
        }

        // Construir las authorities desde los roles y permisos
        var authorities = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .map(permiso -> new SimpleGrantedAuthority(permiso.getCodigo()))
                .collect(Collectors.toSet());

        // Agregar también los roles como authorities
        usuario.getRoles().forEach(rol -> 
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre())));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }
}
