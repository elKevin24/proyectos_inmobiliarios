# Documento de Diseño - Sistema SaaS de Gestión de Venta de Terrenos

**Versión:** 1.0  
**Fecha:** 13 de Noviembre de 2025  
**Estado:** Diseño Finalizado  
**Autores:** Kevin (Developer), Equipo de Arquitectura

---

## Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Objetivos del Sistema](#objetivos-del-sistema)
3. [Alcance](#alcance)
4. [Arquitectura de Datos](#arquitectura-de-datos)
5. [Estructura Multi-tenant](#estructura-multi-tenant)
6. [Gestión de Proyectos y Fases](#gestión-de-proyectos-y-fases)
7. [Gestión de Terrenos](#gestión-de-terrenos)
8. [Sistema de Precios](#sistema-de-precios)
9. [Gestión de Transacciones](#gestión-de-transacciones)
10. [Usuarios y Control de Acceso](#usuarios-y-control-de-acceso)
11. [Sistema de Auditoría](#sistema-de-auditoría)
12. [Reportes](#reportes)
13. [Especificaciones Técnicas](#especificaciones-técnicas)
14. [Seguridad](#seguridad)
15. [Escalabilidad y Performance](#escalabilidad-y-performance)

---

## 1. Visión General

### 1.1 Descripción del Sistema

El sistema es una plataforma SaaS (Software as a Service) multi-tenant diseñada para administrar integralmente la venta de terrenos. Permite a inmobiliarias, desarrolladores y arquitectos:

- Cargar y gestionar planos/mapas de proyectos
- Registrar y clasificar terrenos
- Configurar precios con flexibilidad
- Gestionar cotizaciones, apartados (reservas) y ventas
- Generar reportes analíticos
- Mantener auditoría completa de operaciones críticas

### 1.2 Problema que Resuelve

Actualmente, la venta de terrenos requiere:
- Manejo manual de planos
- Registro disperso de transacciones
- Dificultad para consultar disponibilidad en tiempo real
- Falta de trazabilidad de operaciones
- Imposibilidad de generar reportes rápidamente

Este sistema centraliza todo en una plataforma web intuitiva.

### 1.3 Beneficios Esperados

- **Eficiencia:** Reducir tiempo en gestión administrativa
- **Transparencia:** Visibilidad completa de todas las operaciones
- **Confiabilidad:** Auditoría robusta de cambios
- **Escalabilidad:** Múltiples empresas, proyectos, terrenos
- **Control:** Gestión granular de permisos por rol
- **Análisis:** Reportes detallados para toma de decisiones

---

## 2. Objetivos del Sistema

### 2.1 Objetivos Funcionales

**Fase 1 (MVP):**
- Gestión básica de propiedades (terrenos)
- Visualización de planos con Leaflet
- Cotizaciones simples
- Registro de apartados y ventas
- Búsqueda y filtros básicos
- Reportes bajo demanda

**Fase 2:**
- Capas en mapas (servicios, información catastral)
- CRM básico de clientes
- Calendario de eventos
- Integración Google Maps
- Notificaciones en tiempo real

**Fase 3:**
- Integración de pagos online
- API pública para terceros
- Aplicación mobile/PWA
- Análisis avanzado y predicción

---

## 3. Alcance

### 3.1 Funcionalidades Incluidas

#### Gestión de Empresas, Proyectos, Terrenos, Precios
- Sistema multi-tenant completo
- Proyectos con múltiples fases
- Flexibilidad en precios (fijo/variable)
- Descuentos manuales y configurables

#### Gestión de Transacciones
- Cotizaciones, Apartados, Ventas
- Estados claros y auditoría
- Datos completos de cliente
- Comisiones de vendedores

#### Control de Acceso
- 5 roles estándar
- Permisos granulares
- Aislamiento completo por empresa

#### Auditoría Robusta
- Logs simples (logins)
- Logs críticos (cambios datos)
- Retención 1 año + archivo
- Historial completo de cambios

#### Reportes (8 tipos)
- Ventas, Disponibilidad, Rentabilidad
- Comisiones, Apartados, Ingresos
- Actividad, Precios

---

## 4. Estructura Multi-tenant

### 4.1 Principios

1. **Aislamiento Total:** tenant_id en todas las tablas
2. **Validación en Aplicación:** TenantContext + filtros
3. **Error 403:** Intento de acceso a otro tenant
4. **JWT incluye tenant_id**

### 4.2 Flujo

```
Login → JWT(tenant_id) → TenantContext → Filtros por tenant_id
```

---

## 5. Estados de Transacciones

### 5.1 Apartado

- ACTIVO: Vigente dentro del plazo
- COMPLETADO: Se convirtió en venta
- VENCIDO: Plazo expiró
- CANCELADO: Cliente canceló

### 5.2 Venta

- PENDIENTE: Registrada, pago pendiente
- PAGADO: Completada
- CANCELADA: Se revirtió
- ANULADA: Error administrativo

---

## 6. Roles y Permisos

### Roles Estándar

| Rol | Acceso |
|-----|--------|
| ADMIN | Todo su empresa |
| SUPERVISOR | Proyectos asignados |
| VENDEDOR | Crear transacciones |
| SECRETARIA | Lectura, exportar |
| CONTADOR | Finanzas |

---

## 7. Auditoría

### Auditoría Simple
- Logins, exports (opcional)
- 1 año retención

### Auditoría Crítica
- Cambios en precios, transacciones, usuarios
- Valor anterior/nuevo
- Motivo (opcional)
- 1 año BD principal + archivo

---

## 8. Reportes (8 Tipos)

1. **Ventas por Período** - Ingresos, tickets, vendedores
2. **Disponibilidad** - Estado terrenos, apartados próximos a vencer
3. **Rentabilidad** - Márgenes, ganancias
4. **Comisiones** - Por vendedor
5. **Apartados** - Estado, próximos a vencer
6. **Ingresos vs Gastos** - Análisis financiero
7. **Actividad** - Registro de operaciones
8. **Evolución de Precios** - Cambios históricos

Acceso según rol, bajo demanda, Excel/PDF

---

## 9. Especificaciones Técnicas

### Backend
- Spring Boot 3.x + Java 17+
- PostgreSQL 14+
- JWT authentication
- AOP para auditoría
- Flyway para migraciones

### Frontend
- React 18 + Vite
- Zustand (state)
- React Leaflet (mapas)
- Tailwind CSS
- Axios

### Infraestructura
- Docker/Kubernetes
- PostgreSQL con replicación
- S3 para planos
- HTTPS/TLS

---

## 10. Seguridad

- JWT tokens
- Bcrypt contraseñas
- Validación entrada/salida
- GDPR compliance
- Aislamiento multi-tenant

---

## 11. Performance

- Carga página: < 2s
- Carga mapa: < 2s
- Query BD: < 100ms
- API response: < 500ms

Optimizaciones: índices, caché, code splitting, lazy loading

---

## Próximos Pasos

1. Diseño de endpoints REST
2. Diseño de esquema BD
3. Inicio de desarrollo

