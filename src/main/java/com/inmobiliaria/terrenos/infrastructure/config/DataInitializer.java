package com.inmobiliaria.terrenos.infrastructure.config;

import com.inmobiliaria.terrenos.domain.entity.Permiso;
import com.inmobiliaria.terrenos.domain.entity.Rol;
import com.inmobiliaria.terrenos.domain.entity.Tenant;
import com.inmobiliaria.terrenos.domain.entity.Usuario;
import com.inmobiliaria.terrenos.domain.enums.RolEnum;
import com.inmobiliaria.terrenos.domain.repository.PermisoRepository;
import com.inmobiliaria.terrenos.domain.repository.RolRepository;
import com.inmobiliaria.terrenos.domain.repository.TenantRepository;
import com.inmobiliaria.terrenos.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Inicializador de datos base del sistema para entornos no-test.
 * Garantiza que existan el catálogo completo de permisos, el tenant por defecto,
 * el rol ADMIN con todos los permisos y el usuario administrador inicial.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_TENANT_EMAIL = "contacto@terrenos.com";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@terrenos.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private static final String[][] PERMISOS_SISTEMA = {
            {"PROYECTO_CREAR", "Crear Proyectos", "Permite crear nuevos proyectos", "PROYECTOS"},
            {"PROYECTO_EDITAR", "Editar Proyectos", "Permite editar proyectos existentes", "PROYECTOS"},
            {"PROYECTO_ELIMINAR", "Eliminar Proyectos", "Permite eliminar proyectos", "PROYECTOS"},
            {"PROYECTO_VER", "Ver Proyectos", "Permite ver proyectos", "PROYECTOS"},

            {"FASE_VER", "Ver Fases", "Permite ver fases de proyectos", "FASES"},
            {"FASE_CREAR", "Crear Fases", "Permite crear nuevas fases", "FASES"},
            {"FASE_EDITAR", "Editar Fases", "Permite editar fases existentes", "FASES"},
            {"FASE_ELIMINAR", "Eliminar Fases", "Permite eliminar fases", "FASES"},

            {"TERRENO_CREAR", "Crear Terrenos", "Permite crear nuevos terrenos", "TERRENOS"},
            {"TERRENO_EDITAR", "Editar Terrenos", "Permite editar terrenos existentes", "TERRENOS"},
            {"TERRENO_ELIMINAR", "Eliminar Terrenos", "Permite eliminar terrenos", "TERRENOS"},
            {"TERRENO_VER", "Ver Terrenos", "Permite ver terrenos", "TERRENOS"},

            {"CLIENTE_VER", "Ver Clientes", "Permite ver clientes", "CLIENTES"},
            {"CLIENTE_CREAR", "Crear Clientes", "Permite crear clientes", "CLIENTES"},
            {"CLIENTE_EDITAR", "Editar Clientes", "Permite editar clientes", "CLIENTES"},
            {"CLIENTE_ELIMINAR", "Eliminar Clientes", "Permite eliminar clientes", "CLIENTES"},

            {"COTIZACION_CREAR", "Crear Cotizaciones", "Permite crear cotizaciones", "COTIZACIONES"},
            {"COTIZACION_VER", "Ver Cotizaciones", "Permite ver cotizaciones", "COTIZACIONES"},
            {"COTIZACION_ELIMINAR", "Eliminar Cotizaciones", "Permite eliminar cotizaciones", "COTIZACIONES"},

            {"APARTADO_CREAR", "Crear Apartados", "Permite crear apartados/reservas", "APARTADOS"},
            {"APARTADO_EDITAR", "Editar Apartados", "Permite editar apartados", "APARTADOS"},
            {"APARTADO_CANCELAR", "Cancelar Apartados", "Permite cancelar apartados", "APARTADOS"},
            {"APARTADO_ELIMINAR", "Eliminar Apartados", "Permite eliminar apartados", "APARTADOS"},
            {"APARTADO_VER", "Ver Apartados", "Permite ver apartados", "APARTADOS"},

            {"VENTA_CREAR", "Crear Ventas", "Permite registrar ventas", "VENTAS"},
            {"VENTA_EDITAR", "Editar Ventas", "Permite editar ventas", "VENTAS"},
            {"VENTA_CANCELAR", "Cancelar Ventas", "Permite cancelar ventas", "VENTAS"},
            {"VENTA_ELIMINAR", "Eliminar Ventas", "Permite eliminar ventas", "VENTAS"},
            {"VENTA_VER", "Ver Ventas", "Permite ver ventas", "VENTAS"},

            {"PLAN_PAGO_VER", "Ver Planes de Pago", "Permite ver planes de pago", "PLANES_PAGO"},
            {"PLAN_PAGO_CREAR", "Crear Planes de Pago", "Permite crear planes de pago", "PLANES_PAGO"},
            {"PLAN_PAGO_EDITAR", "Editar Planes de Pago", "Permite editar planes de pago", "PLANES_PAGO"},
            {"PLAN_PAGO_ELIMINAR", "Eliminar Planes de Pago", "Permite eliminar planes de pago", "PLANES_PAGO"},

            {"PAGO_VER", "Ver Pagos", "Permite consultar pagos", "PAGOS"},
            {"PAGO_CREAR", "Crear Pagos", "Permite crear pagos", "PAGOS"},
            {"PAGO_REGISTRAR", "Registrar Pagos", "Permite registrar pagos", "PAGOS"},
            {"PAGO_ELIMINAR", "Eliminar Pagos", "Permite cancelar o eliminar pagos", "PAGOS"},

            {"ARCHIVO_VER", "Ver Archivos", "Permite ver archivos", "ARCHIVOS"},
            {"ARCHIVO_CREAR", "Crear Archivos", "Permite crear archivos", "ARCHIVOS"},
            {"ARCHIVO_SUBIR", "Subir Archivos", "Permite subir archivos", "ARCHIVOS"},
            {"ARCHIVO_ELIMINAR", "Eliminar Archivos", "Permite eliminar archivos", "ARCHIVOS"},

            {"USUARIO_CREAR", "Crear Usuarios", "Permite crear nuevos usuarios", "USUARIOS"},
            {"USUARIO_EDITAR", "Editar Usuarios", "Permite editar usuarios", "USUARIOS"},
            {"USUARIO_ELIMINAR", "Eliminar Usuarios", "Permite eliminar usuarios", "USUARIOS"},
            {"USUARIO_VER", "Ver Usuarios", "Permite ver usuarios", "USUARIOS"},
            {"USUARIO_ASIGNAR_ROLES", "Asignar Roles", "Permite asignar roles a usuarios", "USUARIOS"},

            {"REPORTE_VER", "Ver Reportes", "Acceso general a reportes y dashboard", "REPORTES"},
            {"REPORTE_VENTAS", "Reporte de Ventas", "Acceso al reporte de ventas", "REPORTES"},
            {"REPORTE_DISPONIBILIDAD", "Reporte de Disponibilidad", "Acceso al reporte de disponibilidad", "REPORTES"},
            {"REPORTE_RENTABILIDAD", "Reporte de Rentabilidad", "Acceso al reporte de rentabilidad", "REPORTES"},
            {"REPORTE_COMISIONES", "Reporte de Comisiones", "Acceso al reporte de comisiones", "REPORTES"},
            {"REPORTE_APARTADOS", "Reporte de Apartados", "Acceso al reporte de apartados", "REPORTES"},
            {"REPORTE_INGRESOS", "Reporte de Ingresos", "Acceso al reporte de ingresos vs gastos", "REPORTES"},
            {"REPORTE_ACTIVIDAD", "Reporte de Actividad", "Acceso al reporte de actividad", "REPORTES"},
            {"REPORTE_PRECIOS", "Reporte de Precios", "Acceso al reporte de evolución de precios", "REPORTES"},

            {"AUDITORIA_VER", "Ver Auditoría", "Permite ver los logs de auditoría", "AUDITORIA"},
            {"AUDITORIA_EXPORTAR", "Exportar Auditoría", "Permite exportar logs de auditoría", "AUDITORIA"},

            {"CONFIG_EMPRESA", "Configurar Empresa", "Permite configurar datos de la empresa", "CONFIGURACION"},
            {"CONFIG_PRECIOS", "Configurar Precios", "Permite configurar precios y descuentos", "CONFIGURACION"},
            {"CONFIG_SISTEMA", "Configurar Sistema", "Acceso a configuración del sistema", "CONFIGURACION"},
            {"ADMIN", "Administrador Total", "Acceso administrativo total al tenant", "ADMIN"}
    };

    private final TenantRepository tenantRepository;
    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Verificando datos semilla iniciales del sistema...");

        Set<Permiso> todosLosPermisos = asegurarPermisos();
        Tenant tenant = asegurarTenantPorDefecto();
        Rol rolAdmin = asegurarRolAdmin(tenant.getId(), todosLosPermisos);
        asegurarUsuarioAdmin(tenant.getId(), rolAdmin);

        log.info("Inicialización de datos semilla completada para tenant ID: {}", tenant.getId());
    }

    private Set<Permiso> asegurarPermisos() {
        for (String[] def : PERMISOS_SISTEMA) {
            String codigo = def[0];
            if (permisoRepository.findByCodigo(codigo).isEmpty()) {
                permisoRepository.save(Permiso.builder()
                        .codigo(codigo)
                        .nombre(def[1])
                        .descripcion(def[2])
                        .modulo(def[3])
                        .build());
            }
        }
        List<Permiso> permisos = permisoRepository.findAll();
        return new HashSet<>(permisos);
    }

    private Tenant asegurarTenantPorDefecto() {
        Optional<Usuario> existingAdmin = usuarioRepository.findByEmail(DEFAULT_ADMIN_EMAIL);
        if (existingAdmin.isPresent() && existingAdmin.get().getTenantId() != null) {
            Optional<Tenant> adminTenant = tenantRepository.findById(existingAdmin.get().getTenantId());
            if (adminTenant.isPresent()) {
                return adminTenant.get();
            }
        }

        return tenantRepository.findByEmail(DEFAULT_TENANT_EMAIL)
                .orElseGet(() -> {
                    Tenant nuevoTenant = Tenant.builder()
                            .nombre("Terrenos Inmobiliaria")
                            .email(DEFAULT_TENANT_EMAIL)
                            .telefono("5550000000")
                            .direccion("Av. Principal 100")
                            .razonSocial("Terrenos Inmobiliaria S.A. de C.V.")
                            .activo(true)
                            .maxUsuarios(50)
                            .maxProyectos(20)
                            .build();
                    return tenantRepository.save(nuevoTenant);
                });
    }

    private Rol asegurarRolAdmin(Long tenantId, Set<Permiso> todosLosPermisos) {
        Rol rolAdmin = rolRepository.findByNombreAndTenantId(RolEnum.ADMIN, tenantId)
                .orElseGet(() -> {
                    Rol nuevoRol = Rol.builder()
                            .nombre(RolEnum.ADMIN)
                            .descripcion("Administrador con acceso total")
                            .permisos(new HashSet<>(todosLosPermisos))
                            .build();
                    nuevoRol.setTenantId(tenantId);
                    return rolRepository.save(nuevoRol);
                });

        if (rolAdmin.getPermisos() == null || rolAdmin.getPermisos().size() < todosLosPermisos.size()) {
            rolAdmin.setPermisos(new HashSet<>(todosLosPermisos));
            rolAdmin = rolRepository.save(rolAdmin);
        }
        return rolAdmin;
    }

    private void asegurarUsuarioAdmin(Long tenantId, Rol rolAdmin) {
        usuarioRepository.findByEmail(DEFAULT_ADMIN_EMAIL).ifPresentOrElse(
                usuario -> {
                    boolean modificado = false;
                    if (!Boolean.TRUE.equals(usuario.getActivo())) {
                        usuario.setActivo(true);
                        modificado = true;
                    }
                    if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                        usuario.setRoles(new HashSet<>(Set.of(rolAdmin)));
                        modificado = true;
                    }
                    if (modificado) {
                        usuarioRepository.save(usuario);
                    }
                },
                () -> {
                    Usuario admin = Usuario.builder()
                            .nombre("Admin")
                            .apellido("Sistema")
                            .email(DEFAULT_ADMIN_EMAIL)
                            .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                            .telefono("5550000000")
                            .activo(true)
                            .roles(new HashSet<>(Set.of(rolAdmin)))
                            .build();
                    admin.setTenantId(tenantId);
                    usuarioRepository.save(admin);
                    log.info("Usuario administrador por defecto creado: {}", DEFAULT_ADMIN_EMAIL);
                }
        );
    }
}
