# ⚡ Guía de Inicio Rápido

Poner el sistema en marcha en menos de 5 minutos.

## 📋 Pre-requisitos

Asegúrate de tener instalado:
- ✅ **Java 21** - [Descargar Adoptium JDK 21](https://adoptium.net/)
- ✅ **Node.js 18+** - [Descargar Node.js](https://nodejs.org/)
- ✅ **Docker Desktop** - [Descargar Docker](https://www.docker.com/products/docker-desktop/)
- ✅ **Git** - [Descargar Git](https://git-scm.com/)

## 🚀 Instalación en 5 Pasos

### 1️⃣ Clonar el Repositorio

```bash
git clone https://github.com/elKevin24/proyectos_inmobiliarios.git
cd proyectos_inmobiliarios
```

### 2️⃣ Configurar Variables de Entorno

**Backend:**
```bash
# Copiar archivo de ejemplo
cp .env.example .env

# No necesitas modificar nada para desarrollo local
# El .env ya viene con valores por defecto
```

**Frontend:**
```bash
cd frontend
cp .env.example .env
# El archivo ya tiene: VITE_API_BASE_URL=http://localhost:8080/api/v1
cd ..
```

### 3️⃣ Levantar PostgreSQL

```bash
docker-compose up -d
```

Espera 10 segundos para que PostgreSQL esté listo.

### 4️⃣ Iniciar el Backend

**Opción A - Ejecutar directamente (Recomendado para desarrollo):**
```bash
./mvnw spring-boot:run
```

**Opción B - Compilar y ejecutar:**
```bash
./mvnw clean package -DskipTests
java -jar target/terrenos-backend-1.0.0-SNAPSHOT.jar
```

**Espera hasta ver:**
```
Started TerrenosBackendApplication in X.XXX seconds
```

### 5️⃣ Iniciar el Frontend

**En una nueva terminal:**
```bash
cd frontend
npm install
npm run dev
```

**Espera hasta ver:**
```
  VITE v5.x.x  ready in XXX ms

  ➜  Local:   http://localhost:5173/
```

## ✅ Verificar Instalación

### Backend (Puerto 8080)

**Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

**Health Check:**
```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{"status":"UP"}
```

### Frontend (Puerto 5173)

**Abrir en navegador:**
```
http://localhost:5173
```

Deberías ver la pantalla de Login.

### PostgreSQL (Puerto 5432)

**pgAdmin:**
```
http://localhost:5050
Usuario: admin@terrenos.com
Password: admin
```

## 🎯 Primeros Pasos

### 1. Registrar tu Primera Empresa

**Desde el Frontend:**
1. Abre `http://localhost:5173`
2. Click en "Regístrate aquí"
3. Llena el formulario:
   - Nombre: Tu nombre
   - Apellidos: Tus apellidos
   - Email: tu@email.com
   - Teléfono: 5512345678
   - Nombre Empresa: Mi Inmobiliaria
   - Contraseña: (mínimo 8 caracteres)

4. Click en "Crear Cuenta"

**O desde la API:**
```bash
curl -X POST http://localhost:8080/api/v1/tenants/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombreEmpresa": "Mi Inmobiliaria",
    "email": "admin@miinmobiliaria.com",
    "password": "Password123!",
    "nombre": "Juan",
    "apellidos": "Pérez",
    "telefono": "5512345678"
  }'
```

### 2. Iniciar Sesión

1. En la pantalla de login, ingresa:
   - Email: tu@email.com
   - Contraseña: tu contraseña

2. Click en "Iniciar Sesión"

### 3. Crear tu Primer Proyecto

1. En el menú lateral, click en "Proyectos"
2. Click en "+ Nuevo Proyecto"
3. Llena el formulario:
   - Nombre: Residencial Las Palmas
   - Estado: En Venta
   - Ubicación: Zona Norte
   - Ciudad: Ciudad de México
   - Precio Base/m²: 5000

4. (Opcional) Sube un plano del proyecto
5. Click en "Crear"

### 4. Agregar Terrenos

1. En el menú lateral, click en "Terrenos"
2. Click en "+ Nuevo Terreno"
3. Llena el formulario:
   - Número: 001
   - Proyecto: Selecciona el proyecto creado
   - Área: 200 m²
   - Precio Base: 500,000
   - Estado: Disponible

4. En el mapa, click en "Iniciar Dibujo"
5. Click en el mapa para dibujar el polígono del terreno (mínimo 3 puntos)
6. Click en "Completar Polígono"
7. Click en "Crear"

### 5. Registrar un Cliente

1. En el menú lateral, click en "Clientes"
2. Click en "+ Nuevo Cliente"
3. Llena el formulario:
   - Nombre: María
   - Apellidos: González
   - Email: maria@email.com
   - Teléfono: 5587654321
   - Estado del Cliente: Interesado

4. Click en "Crear"

### 6. Visualizar el Plano del Proyecto

1. En "Proyectos", click en el icono de mapa 🗺️ del proyecto
2. Verás el plano con todos los terrenos superpuestos
3. Terrenos en verde = Disponibles
4. Click en un terreno para ver sus detalles en el panel lateral

## 🛠️ Comandos Útiles

### Backend

```bash
# Compilar sin tests
./mvnw clean package -DskipTests

# Ejecutar tests
./mvnw test

# Ver logs en tiempo real
./mvnw spring-boot:run | grep -i error

# Limpiar y recompilar
./mvnw clean install
```

### Frontend

```bash
# Instalar dependencias
npm install

# Desarrollo
npm run dev

# Build para producción
npm run build

# Preview del build
npm run preview

# Linting
npm run lint
```

### Docker

```bash
# Ver logs de PostgreSQL
docker-compose logs -f postgres

# Reiniciar PostgreSQL
docker-compose restart postgres

# Detener todo
docker-compose down

# Detener y eliminar volúmenes (⚠️ borra datos)
docker-compose down -v

# Ver estado de contenedores
docker-compose ps
```

### Base de Datos

```bash
# Conectar con psql
docker exec -it proyectos_inmobiliarios_postgres psql -U postgres -d terrenos_db

# Backup de base de datos
docker exec proyectos_inmobiliarios_postgres pg_dump -U postgres terrenos_db > backup.sql

# Restaurar backup
docker exec -i proyectos_inmobiliarios_postgres psql -U postgres terrenos_db < backup.sql
```

## 🐛 Solución de Problemas

### Puerto ya en uso

**Error:** `Port 8080 is already in use`

**Solución:**
```bash
# En Linux/Mac
lsof -ti:8080 | xargs kill -9

# En Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### PostgreSQL no inicia

**Error:** `Connection refused to PostgreSQL`

**Solución:**
```bash
# Verificar que Docker está corriendo
docker ps

# Reiniciar PostgreSQL
docker-compose restart postgres

# Ver logs
docker-compose logs postgres
```

### Frontend no conecta con Backend

**Error:** `Network Error` o `CORS Error`

**Verificar:**
1. Backend está corriendo en `http://localhost:8080`
2. Variable `VITE_API_BASE_URL` en `frontend/.env` es correcta
3. CORS está configurado en el backend (ya viene configurado)

**Reiniciar Frontend:**
```bash
cd frontend
npm run dev
```

### Maven no descarga dependencias

**Error:** `Could not resolve dependencies`

**Solución:**
```bash
# Limpiar cache de Maven
rm -rf ~/.m2/repository

# Recompilar
./mvnw clean install -U
```

### Node_modules corrupto

**Error:** `Module not found`

**Solución:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## 📊 URLs Importantes

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| Frontend | http://localhost:5173 | Registrarse primero |
| Backend API | http://localhost:8080 | - |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |
| pgAdmin | http://localhost:5050 | admin@terrenos.com / admin |
| PostgreSQL | localhost:5432 | postgres / postgres |

## 🎓 Próximos Pasos

1. **Lee la documentación completa**: `README.md`
2. **Explora la arquitectura**: `ARCHITECTURE.md`
3. **Revisa los endpoints**: `BACKEND_API_DOCUMENTATION.md`
4. **Experimenta con la API**: Swagger UI

## 💡 Tips

### Desarrollo Eficiente

1. **Hot Reload Frontend**: Los cambios en React se aplican automáticamente
2. **Spring DevTools**: Reinicio automático del backend al detectar cambios
3. **Logs en Tiempo Real**: Usa `./mvnw spring-boot:run` para ver logs
4. **Depuración**: Usa el navegador DevTools para inspeccionar requests

### Datos de Prueba

**Crear múltiples terrenos rápidamente:**
```bash
# Usa el script de Swagger UI o crea un script bash
# Ver api-examples.http para ejemplos
```

## 🆘 Soporte

¿Necesitas ayuda?

1. **Revisa este documento** primero
2. **Consulta la sección de problemas comunes** arriba
3. **Revisa los logs** del backend y frontend
4. **Abre un issue** en el repositorio de GitHub

---

**¡Listo! Ahora tienes el sistema corriendo. Happy coding! 🚀**
