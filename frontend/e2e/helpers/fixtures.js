export const TEST_USER = {
  email: 'admin@terrenos.com',
  password: 'admin123',
};

export const NEW_USER_DATA = {
  nombre: 'Juan',
  apellidos: 'Perez Garcia',
  telefono: '5512345678',
  nombreEmpresa: 'Test Inmobiliaria',
  password: 'TestPass123!',
};

export const PROYECTO_DATA = {
  nombre: `Proyecto E2E ${Date.now()}`,
  descripcion: 'Proyecto creado durante pruebas E2E',
  ubicacion: 'Zona Norte',
  direccion: 'Av. Principal #123',
  ciudad: 'Ciudad de Mexico',
  estado: 'CDMX',
  codigoPostal: '01234',
  precioBaseM2: 5000,
  areaTotal: 10000,
  estadoProyecto: 'EN_VENTA',
};

export const TERRENO_DATA = {
  numero: 'E2E-001',
  nombre: 'Lote E2E Test',
  descripcion: 'Terreno creado durante pruebas E2E',
  area: 200,
  precioBase: 500000,
  ajustePrecio: 0,
  multiplicadorPrecio: 1,
  estado: 'DISPONIBLE',
};

export const CLIENTE_DATA = {
  nombre: 'Maria',
  apellidos: 'Lopez Sanchez',
  email: `maria.e2e${Date.now()}@test.com`,
  telefono: '5598765432',
  rfc: 'LOSM800101ABC',
  estadoCliente: 'PROSPECTO',
  direccion: 'Calle Secundaria #456',
  ciudad: 'Ciudad de Mexico',
  estadoResidencia: 'CDMX',
  codigoPostal: '06789',
};

export const COTIZACION_DATA = {
  clienteNombre: 'Cliente Cotizacion E2E',
  clienteEmail: 'cotizacion.e2e@test.com',
  clienteTelefono: '5511112222',
  porcentajeDescuento: 5,
  fechaVigencia: (() => {
    const d = new Date();
    d.setDate(d.getDate() + 30);
    return d.toISOString().split('T')[0];
  })(),
  observaciones: 'Cotizacion de prueba E2E',
};
