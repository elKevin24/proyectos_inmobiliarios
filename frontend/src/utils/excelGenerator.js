import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

/**
 * Utilidades para exportación a Excel
 * Reportes Internos: Listados de gestión
 */

// Formatear moneda para Excel
const formatCurrency = (amount) => {
  return Number(amount || 0);
};

// Formatear fecha para Excel
const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('es-MX');
};

/**
 * Crear y descargar archivo Excel
 */
const downloadExcel = (data, sheetName, fileName) => {
  const worksheet = XLSX.utils.json_to_sheet(data);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName);

  // Ajustar anchos de columna
  const maxWidth = 20;
  const colWidths = Object.keys(data[0] || {}).map(() => ({ wch: maxWidth }));
  worksheet['!cols'] = colWidths;

  // Generar archivo
  const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
  saveAs(blob, `${fileName}.xlsx`);
};

/**
 * =====================================================
 * EXPORTACIONES A EXCEL (Internos)
 * =====================================================
 */

/**
 * Exportar Terrenos a Excel
 */
export const exportarTerrenosExcel = (terrenos) => {
  const data = terrenos.map(t => ({
    'ID': t.id,
    'Lote': t.numeroLote,
    'Manzana': t.manzana || '',
    'Proyecto': t.proyectoNombre || '',
    'Área (m²)': t.area,
    'Frente': t.frente || '',
    'Fondo': t.fondo || '',
    'Precio Base': formatCurrency(t.precioBase),
    'Precio Final': formatCurrency(t.precioFinal),
    'Estado': t.estado,
    'Fecha Creación': formatDate(t.createdAt)
  }));

  downloadExcel(data, 'Terrenos', `Terrenos_${new Date().toISOString().split('T')[0]}`);
};

/**
 * Exportar Clientes a Excel
 */
export const exportarClientesExcel = (clientes) => {
  const data = clientes.map(c => ({
    'ID': c.id,
    'Nombre': c.nombre,
    'Apellido': c.apellido,
    'Email': c.email || '',
    'Teléfono': c.telefono,
    'Teléfono Secundario': c.telefonoSecundario || '',
    'RFC': c.rfc || '',
    'CURP': c.curp || '',
    'Dirección': c.direccion || '',
    'Ciudad': c.ciudad || '',
    'Estado': c.estado || '',
    'Estado Cliente': c.estadoCliente,
    'Origen': c.origen || '',
    'Total Ventas': c.totalVentas || 0,
    'Fecha Creación': formatDate(c.createdAt)
  }));

  downloadExcel(data, 'Clientes', `Clientes_${new Date().toISOString().split('T')[0]}`);
};

/**
 * Exportar Ventas a Excel
 */
export const exportarVentasExcel = (ventas) => {
  const data = ventas.map(v => ({
    'ID': v.id,
    'Fecha Venta': formatDate(v.fechaVenta),
    'Comprador': v.compradorNombre,
    'Teléfono': v.compradorTelefono || '',
    'Email': v.compradorEmail || '',
    'Terreno': v.terrenoNumeroLote,
    'Proyecto': v.proyectoNombre || '',
    'Monto Total': formatCurrency(v.montoTotal),
    'Enganche': formatCurrency(v.enganche || v.montoApartadoAcreditado || 0),
    'Monto Final': formatCurrency(v.montoFinal || v.montoTotal),
    'Comisión (%)': v.porcentajeComision || 0,
    'Monto Comisión': formatCurrency(v.montoComision || 0),
    'Forma de Pago': v.formaPago || '',
    'Estado': v.estado,
    'Vendedor': v.usuarioNombre || ''
  }));

  downloadExcel(data, 'Ventas', `Ventas_${new Date().toISOString().split('T')[0]}`);
};

/**
 * Exportar Apartados a Excel
 */
export const exportarApartadosExcel = (apartados) => {
  const data = apartados.map(a => ({
    'ID': a.id,
    'Cliente': a.clienteNombre,
    'Terreno': a.terrenoNumeroLote || a.terrenoId,
    'Monto Apartado': formatCurrency(a.montoApartado),
    'Porcentaje': a.porcentajeApartado,
    'Fecha Apartado': formatDate(a.fechaApartado),
    'Fecha Vencimiento': formatDate(a.fechaVencimiento),
    'Días Vigencia': a.diasVigencia,
    'Estado': a.estado,
    'Observaciones': a.observaciones || ''
  }));

  downloadExcel(data, 'Apartados', `Apartados_${new Date().toISOString().split('T')[0]}`);
};

/**
 * Exportar Cotizaciones a Excel
 */
export const exportarCotizacionesExcel = (cotizaciones) => {
  const data = cotizaciones.map(c => ({
    'ID': c.id,
    'Cliente': c.clienteNombre,
    'Email': c.clienteEmail || '',
    'Teléfono': c.clienteTelefono || '',
    'Terreno': c.terrenoNumeroLote,
    'Manzana': c.terrenoManzana || '',
    'Proyecto': c.proyectoNombre || '',
    'Precio Base': formatCurrency(c.precioBase),
    'Descuento': formatCurrency(c.descuento),
    'Descuento (%)': c.porcentajeDescuento || 0,
    'Precio Final': formatCurrency(c.precioFinal),
    'Fecha Vigencia': formatDate(c.fechaVigencia),
    'Fecha Creación': formatDate(c.createdAt)
  }));

  downloadExcel(data, 'Cotizaciones', `Cotizaciones_${new Date().toISOString().split('T')[0]}`);
};

/**
 * Exportar Planes de Pago a Excel
 */
export const exportarPlanesPagoExcel = (planesPago) => {
  const data = planesPago.map(p => ({
    'ID': p.id,
    'Tipo Plan': p.tipoPlan,
    'Frecuencia': p.frecuenciaPago,
    'Monto Total': formatCurrency(p.montoTotal),
    'Enganche': formatCurrency(p.enganche),
    'Monto Financiado': formatCurrency(p.montoFinanciado),
    'Tasa Interés Anual (%)': p.tasaInteresAnual,
    'Número Pagos': p.numeroPagos,
    'Plazo (meses)': p.plazoMeses,
    'Total Pagado': formatCurrency(p.totalPagado),
    'Total Pendiente': formatCurrency(p.totalPendiente),
    'Avance (%)': p.porcentajeAvance || 0,
    'Fecha Inicio': formatDate(p.fechaInicio),
    'Fecha Primer Pago': formatDate(p.fechaPrimerPago),
    'Fecha Último Pago': formatDate(p.fechaUltimoPago)
  }));

  downloadExcel(data, 'PlanesPago', `PlanesPago_${new Date().toISOString().split('T')[0]}`);
};

/**
 * Exportar Amortizaciones a Excel
 */
export const exportarAmortizacionesExcel = (amortizaciones, planId) => {
  const data = amortizaciones.map(a => ({
    'Número': a.numeroAmortizacion,
    'Fecha Vencimiento': formatDate(a.fechaVencimiento),
    'Monto Capital': formatCurrency(a.montoCapital),
    'Monto Interés': formatCurrency(a.montoInteres),
    'Monto Total': formatCurrency(a.montoTotal),
    'Monto Pagado': formatCurrency(a.montoPagado),
    'Saldo Pendiente': formatCurrency(a.saldoPendiente),
    'Fecha Pago': formatDate(a.fechaPago),
    'Estado': a.estado
  }));

  downloadExcel(data, 'Amortizaciones', `Amortizaciones_Plan_${planId}_${new Date().toISOString().split('T')[0]}`);
};

/**
 * Exportar Proyectos a Excel
 */
export const exportarProyectosExcel = (proyectos) => {
  const data = proyectos.map(p => ({
    'ID': p.id,
    'Nombre': p.nombre,
    'Descripción': p.descripcion || '',
    'Dirección': p.direccion || '',
    'Ciudad': p.ciudad || '',
    'Estado Proyecto': p.estadoProyecto,
    'Total Terrenos': p.totalTerrenos || 0,
    'Disponibles': p.terrenosDisponibles || 0,
    'Apartados': p.terrenosApartados || 0,
    'Vendidos': p.terrenosVendidos || 0,
    'Precio Base': formatCurrency(p.precioBase),
    'Tipo Precio': p.tipoPrecio,
    'Fecha Creación': formatDate(p.createdAt)
  }));

  downloadExcel(data, 'Proyectos', `Proyectos_${new Date().toISOString().split('T')[0]}`);
};

export default {
  exportarTerrenosExcel,
  exportarClientesExcel,
  exportarVentasExcel,
  exportarApartadosExcel,
  exportarCotizacionesExcel,
  exportarPlanesPagoExcel,
  exportarAmortizacionesExcel,
  exportarProyectosExcel
};
