# Frontend - Sistema de Gestión de Proyectos Inmobiliarios

MVP Frontend desarrollado con React + Vite para el sistema de gestión de proyectos inmobiliarios.

## Características

- ✅ Autenticación (Login/Register)
- ✅ CRUD de Terrenos
- ✅ Visualización y manipulación de mapas con Leaflet
- ✅ Módulo de transacciones (Ventas)
- ✅ Dashboard con estadísticas
- ✅ Diseño responsive
- ✅ Manejo de estado con Zustand
- ✅ Formularios con React Hook Form

## Tecnologías

- **React 18** - Biblioteca de UI
- **Vite** - Build tool y dev server
- **React Router DOM** - Routing
- **Axios** - Cliente HTTP
- **Zustand** - State management
- **React Hook Form** - Manejo de formularios
- **Leaflet** - Mapas interactivos
- **React Leaflet** - Componentes de React para Leaflet
- **React Icons** - Iconos
- **JWT Decode** - Decodificación de tokens JWT

## Estructura del Proyecto

```
frontend/
├── src/
│   ├── components/       # Componentes reutilizables
│   │   ├── Layout.jsx
│   │   ├── MapEditor.jsx
│   │   └── ProtectedRoute.jsx
│   ├── pages/            # Páginas de la aplicación
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── Dashboard.jsx
│   │   ├── TerrenosList.jsx
│   │   ├── TerrenoForm.jsx
│   │   ├── VentasList.jsx
│   │   ├── Proyectos.jsx
│   │   └── Clientes.jsx
│   ├── services/         # Servicios de API
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── terrenoService.js
│   │   ├── proyectoService.js
│   │   ├── ventaService.js
│   │   └── archivoService.js
│   ├── store/            # Estado global (Zustand)
│   │   ├── authStore.js
│   │   └── terrenoStore.js
│   ├── styles/           # Estilos CSS
│   │   ├── Auth.css
│   │   ├── Dashboard.css
│   │   ├── Layout.css
│   │   ├── Map.css
│   │   ├── Terrenos.css
│   │   └── Ventas.css
│   ├── App.jsx
│   ├── App.css
│   └── main.jsx
├── public/
├── .env.example
├── package.json
├── vite.config.js
└── README.md
```

## Instalación

1. Instalar dependencias:
```bash
npm install
```

2. Configurar variables de entorno:
```bash
cp .env.example .env
```

3. Editar `.env` con la URL de tu backend:
```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## Ejecución

### Modo Desarrollo

```bash
npm run dev
```

La aplicación estará disponible en `http://localhost:5173`

### Build para Producción

```bash
npm run build
```

### Preview de Producción

```bash
npm run preview
```

## Uso

### 1. Registro e Inicio de Sesión

- Navega a `/register` para crear una cuenta
- Ingresa con tus credenciales en `/login`
- El token JWT se almacena en localStorage

### 2. Dashboard

- Vista general con estadísticas de terrenos
- Accesos rápidos a funciones principales

### 3. Gestión de Terrenos

- **Listar**: Ver todos los terrenos con filtros
- **Crear**: Agregar nuevos terrenos con mapa interactivo
- **Editar**: Modificar terrenos existentes
- **Eliminar**: Borrar terrenos (soft delete en backend)

### 4. Mapa Interactivo

- Click en "Iniciar Dibujo" para comenzar a dibujar
- Click en el mapa para agregar puntos del polígono
- Mínimo 3 puntos para formar un polígono
- "Completar Polígono" para finalizar
- "Limpiar" para empezar de nuevo

### 5. Transacciones (Ventas)

- Ver listado de ventas
- Filtrar por estado
- Ver detalles de cada venta

## API Endpoints Utilizados

### Autenticación
- `POST /auth/login` - Iniciar sesión
- `POST /auth/register` - Registrar usuario
- `GET /auth/me` - Obtener usuario actual

### Terrenos
- `GET /terrenos` - Listar terrenos
- `GET /terrenos/:id` - Obtener terreno por ID
- `POST /terrenos` - Crear terreno
- `PUT /terrenos/:id` - Actualizar terreno
- `DELETE /terrenos/:id` - Eliminar terreno

### Proyectos
- `GET /proyectos` - Listar proyectos
- `GET /proyectos/:id` - Obtener proyecto por ID

### Ventas
- `GET /ventas` - Listar ventas
- `GET /ventas/:id` - Obtener venta por ID
- `POST /ventas` - Crear venta

### Archivos
- `POST /archivos/upload` - Subir archivo

## Características de Seguridad

- Tokens JWT almacenados en localStorage
- Rutas protegidas con ProtectedRoute
- Interceptor de Axios para agregar token automáticamente
- Redirección automática a login si el token expira
- Validación de formularios con React Hook Form

## Próximas Mejoras

- [ ] Módulo completo de Proyectos
- [ ] Módulo completo de Clientes
- [ ] Vista detallada de terrenos con mapa
- [ ] Formulario de ventas completo
- [ ] Reportes y gráficos
- [ ] Paginación en listados
- [ ] Búsqueda avanzada
- [ ] Carga de imágenes de planos
- [ ] Exportar datos a PDF/Excel
- [ ] Notificaciones en tiempo real

## Licencia

MIT
