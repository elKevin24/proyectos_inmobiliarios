-- ================================================
-- V2: Crear tablas de Usuarios, Roles y Permisos
-- ================================================

-- Tabla de Permisos
CREATE TABLE IF NOT EXISTS permisos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    modulo VARCHAR(100),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    es_sistema BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_roles_tenant_nombre UNIQUE (tenant_id, nombre)
);

-- Tabla intermedia Rol-Permiso
CREATE TABLE IF NOT EXISTS rol_permiso (
    rol_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permiso_id BIGINT NOT NULL REFERENCES permisos(id) ON DELETE CASCADE,
    
    PRIMARY KEY (rol_id, permiso_id)
);

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    avatar_url VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT uk_usuarios_tenant_email UNIQUE (tenant_id, email)
);

-- Tabla intermedia Usuario-Rol
CREATE TABLE IF NOT EXISTS usuario_rol (
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    
    PRIMARY KEY (usuario_id, rol_id)
);

-- Índices
CREATE INDEX idx_roles_tenant_id ON roles(tenant_id);
CREATE INDEX idx_roles_activo ON roles(activo);
CREATE INDEX idx_usuarios_tenant_id ON usuarios(tenant_id);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);

-- Comentarios
COMMENT ON TABLE permisos IS 'Catálogo de permisos del sistema';
COMMENT ON TABLE roles IS 'Roles por tenant con permisos asignados';
COMMENT ON TABLE usuarios IS 'Usuarios del sistema multi-tenant';
COMMENT ON COLUMN roles.es_sistema IS 'Indica si es un rol predefinido del sistema (no editable)';
