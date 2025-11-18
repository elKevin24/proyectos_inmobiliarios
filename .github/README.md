# 🚀 CI/CD y Desarrollo con GitHub

Este proyecto utiliza **GitHub Actions** para CI/CD automático y **GitHub Codespaces** para desarrollo en la nube.

## 📋 Tabla de Contenidos

1. [GitHub Actions (CI/CD)](#github-actions-cicd)
2. [GitHub Codespaces](#github-codespaces)
3. [Configuración de Secrets](#configuración-de-secrets)
4. [Badges de Estado](#badges-de-estado)

---

## 🔄 GitHub Actions (CI/CD)

### ¿Qué hace automáticamente?

El workflow `.github/workflows/backend-ci.yml` se ejecuta en cada **push** o **pull request** y realiza:

#### ✅ **Job 1: Build and Test**
- Compila el código con Maven
- Ejecuta tests unitarios
- Ejecuta tests de integración con Testcontainers
- Genera reportes de cobertura
- Crea el JAR ejecutable

#### 🔍 **Job 2: Code Quality & Security**
- Ejecuta Checkstyle (validación de estilo de código)
- Escanea vulnerabilidades con OWASP Dependency Check
- Genera reportes de seguridad

#### 🐳 **Job 3: Docker Build**
- Construye la imagen Docker del backend
- (Opcional) Publica a Docker Hub o GitHub Container Registry

#### 📢 **Job 4: Notify**
- Notifica el resultado del build

### Triggers

El workflow se ejecuta cuando:
- Haces **push** a `main`, `develop` o ramas `claude/**`
- Abres un **pull request** a `main` o `develop`
- Solo si hay cambios en `backend/`

### Ver el estado del build

1. Ve a la pestaña **Actions** en GitHub
2. Verás todos los workflows ejecutándose o completados
3. Haz clic en un workflow para ver los detalles

### Ejemplo de uso

```bash
# 1. Haces cambios en el backend
git add backend/
git commit -m "feat: agregar nuevo endpoint"
git push origin main

# 2. GitHub Actions se ejecuta automáticamente
# 3. Recibes notificación si el build falla
# 4. Puedes ver los logs en la pestaña Actions
```

---

## 💻 GitHub Codespaces

### ¿Qué es?

GitHub Codespaces te permite desarrollar **directamente en el navegador** sin necesidad de instalar:
- ❌ Java 21
- ❌ Maven
- ❌ PostgreSQL
- ❌ Docker
- ❌ IDEs

Todo está **pre-configurado** y listo para usar.

### Cómo iniciar un Codespace

#### Opción 1: Desde GitHub.com

1. Ve al repositorio en GitHub
2. Haz clic en el botón verde **Code**
3. Selecciona la pestaña **Codespaces**
4. Haz clic en **Create codespace on main**

#### Opción 2: Desde VS Code Desktop

1. Instala la extensión **GitHub Codespaces**
2. Presiona `Cmd/Ctrl + Shift + P`
3. Escribe: `Codespaces: Create New Codespace`
4. Selecciona el repositorio

### ¿Qué incluye el Codespace?

✅ **Java 21** (Eclipse Temurin)
✅ **Maven 3.9**
✅ **PostgreSQL 16** (ya corriendo)
✅ **pgAdmin** en puerto 5050
✅ **Git** configurado
✅ **Extensiones de VS Code**:
- Java Extension Pack
- Spring Boot Tools
- PostgreSQL Client
- Docker
- GitLens
- REST Client

### Primeros pasos en Codespaces

Una vez iniciado el Codespace:

```bash
# El backend ya está compilado (post-create command)
cd backend

# Ejecutar la aplicación
./mvnw spring-boot:run

# O con Maven
mvn spring-boot:run
```

### Puertos expuestos

El Codespace automáticamente expone:

| Puerto | Servicio | URL |
|--------|----------|-----|
| 8080 | Backend API | `https://your-codespace-8080.preview.app.github.dev` |
| 5432 | PostgreSQL | `localhost:5432` |
| 5050 | pgAdmin | `https://your-codespace-5050.preview.app.github.dev` |

### Conectarse a la base de datos

La extensión **SQLTools** ya está configurada:

1. Haz clic en el ícono de **Database** en la barra lateral
2. Selecciona **PostgreSQL - Terrenos DB**
3. Haz clic en **Connect**
4. ¡Listo! Puedes ejecutar queries directamente

Credenciales:
- **Host**: `postgres`
- **Port**: `5432`
- **Database**: `terrenos_db`
- **Username**: `postgres`
- **Password**: `postgres`

### Ventajas de Codespaces

✅ **Consistencia**: Todos trabajan con el mismo ambiente
✅ **Rapidez**: No necesitas configurar nada localmente
✅ **Acceso desde cualquier lugar**: Solo necesitas un navegador
✅ **Gratis**: 60 horas/mes para cuentas gratuitas, 180 horas/mes para Pro

### Desventajas

❌ **Requiere internet**: No puedes trabajar offline
❌ **Límite de horas**: Gratis solo 60h/mes
❌ **Puede ser más lento**: Depende de tu conexión

---

## 🔐 Configuración de Secrets

Para habilitar features avanzadas (deploy, Docker Hub, etc.), configura estos secrets:

### Cómo agregar Secrets

1. Ve a **Settings** → **Secrets and variables** → **Actions**
2. Haz clic en **New repository secret**
3. Agrega los siguientes secrets:

| Secret Name | Descripción | Ejemplo |
|-------------|-------------|---------|
| `DOCKER_USERNAME` | Usuario de Docker Hub | `mi-usuario` |
| `DOCKER_PASSWORD` | Password o Token de Docker Hub | `dckr_pat_xxx` |
| `JWT_SECRET_PROD` | Secret JWT para producción | (256+ bits) |
| `DATABASE_URL_PROD` | URL de BD producción | `jdbc:postgresql://...` |

### Usar secrets en workflows

```yaml
- name: Login to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKER_USERNAME }}
    password: ${{ secrets.DOCKER_PASSWORD }}
```

---

## 🏅 Badges de Estado

Agrega badges al README principal para mostrar el estado del build:

```markdown
![Backend CI](https://github.com/tu-usuario/proyectos-inmobiliarios/actions/workflows/backend-ci.yml/badge.svg)
```

Se verá así:

![Backend CI](https://github.com/tu-usuario/proyectos-inmobiliarios/actions/workflows/backend-ci.yml/badge.svg)

---

## 📚 Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Codespaces Documentation](https://docs.github.com/en/codespaces)
- [Dev Container Specification](https://containers.dev/)

---

## 🤔 ¿Dudas?

Si tienes problemas con GitHub Actions o Codespaces:

1. Revisa los logs en la pestaña **Actions**
2. Consulta la [documentación oficial](https://docs.github.com)
3. Abre un issue en el repositorio
