import jsPDF from 'jspdf';
import 'jspdf-autotable';

/**
 * Utilidades para generación de PDFs
 * - Para Cliente: Cotizaciones, Estados de cuenta
 * - Internos: Reportes administrativos
 */

// Configuración de colores y estilos
const COLORS = {
  primary: [102, 126, 234],    // #667eea
  secondary: [118, 75, 162],   // #764ba2
  success: [39, 174, 96],      // #27ae60
  danger: [231, 76, 60],       // #e74c3c
  warning: [243, 156, 18],     // #f39c12
  dark: [44, 62, 80],          // #2c3e50
  gray: [127, 140, 141],       // #7f8c8d
  light: [236, 240, 241],      // #ecf0f1
};

// Formatear moneda
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency: 'MXN'
  }).format(amount || 0);
};

// Formatear fecha
const formatDate = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('es-MX', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

// Formatear fecha corta
const formatDateShort = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('es-MX');
};

/**
 * =====================================================
 * PDFs PARA CLIENTE
 * =====================================================
 */

/**
 * Generar PDF de Cotización para Cliente
 */
export const generarCotizacionPDF = (cotizacion) => {
  const doc = new jsPDF();
  const pageWidth = doc.internal.pageSize.getWidth();

  // Header con gradiente simulado
  doc.setFillColor(...COLORS.primary);
  doc.rect(0, 0, pageWidth, 45, 'F');

  // Título
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(24);
  doc.setFont('helvetica', 'bold');
  doc.text('COTIZACIÓN', pageWidth / 2, 20, { align: 'center' });

  doc.setFontSize(12);
  doc.setFont('helvetica', 'normal');
  doc.text(`No. ${cotizacion.id}`, pageWidth / 2, 30, { align: 'center' });
  doc.text(`Fecha: ${formatDate(cotizacion.createdAt)}`, pageWidth / 2, 38, { align: 'center' });

  // Información del cliente
  doc.setTextColor(...COLORS.dark);
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.text('Información del Cliente', 20, 60);

  doc.setFontSize(11);
  doc.setFont('helvetica', 'normal');
  doc.text(`Nombre: ${cotizacion.clienteNombre}`, 20, 70);
  if (cotizacion.clienteEmail) {
    doc.text(`Email: ${cotizacion.clienteEmail}`, 20, 78);
  }
  if (cotizacion.clienteTelefono) {
    doc.text(`Teléfono: ${cotizacion.clienteTelefono}`, 20, 86);
  }

  // Información del terreno
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.text('Información del Terreno', 20, 102);

  doc.setFontSize(11);
  doc.setFont('helvetica', 'normal');
  const terrenoId = cotizacion.terrenoManzana
    ? `Manzana ${cotizacion.terrenoManzana}, Lote ${cotizacion.terrenoNumeroLote}`
    : `Lote ${cotizacion.terrenoNumeroLote}`;
  doc.text(`Identificación: ${terrenoId}`, 20, 112);
  if (cotizacion.proyectoNombre) {
    doc.text(`Proyecto: ${cotizacion.proyectoNombre}`, 20, 120);
  }

  // Desglose de precios
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.text('Desglose de Precios', 20, 140);

  // Tabla de precios
  doc.autoTable({
    startY: 148,
    head: [['Concepto', 'Monto']],
    body: [
      ['Precio Base', formatCurrency(cotizacion.precioBase)],
      ...(cotizacion.descuento > 0 ? [[
        `Descuento (${cotizacion.porcentajeDescuento}%)`,
        `- ${formatCurrency(cotizacion.descuento)}`
      ]] : []),
      ['PRECIO FINAL', formatCurrency(cotizacion.precioFinal)]
    ],
    theme: 'grid',
    headStyles: {
      fillColor: COLORS.primary,
      textColor: [255, 255, 255],
      fontStyle: 'bold'
    },
    bodyStyles: {
      textColor: COLORS.dark
    },
    alternateRowStyles: {
      fillColor: [248, 249, 250]
    },
    footStyles: {
      fillColor: COLORS.success,
      textColor: [255, 255, 255],
      fontStyle: 'bold'
    },
    columnStyles: {
      0: { cellWidth: 100 },
      1: { cellWidth: 70, halign: 'right' }
    }
  });

  // Ahorro del cliente
  if (cotizacion.descuento > 0) {
    const finalY = doc.lastAutoTable.finalY + 10;
    doc.setFillColor(...COLORS.success);
    doc.roundedRect(20, finalY, pageWidth - 40, 20, 3, 3, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text(`¡Usted ahorra: ${formatCurrency(cotizacion.descuento)}!`, pageWidth / 2, finalY + 13, { align: 'center' });
  }

  // Vigencia
  const vigenciaY = (cotizacion.descuento > 0 ? doc.lastAutoTable.finalY + 40 : doc.lastAutoTable.finalY + 15);
  doc.setTextColor(...COLORS.dark);
  doc.setFontSize(11);
  doc.setFont('helvetica', 'normal');
  doc.text(`Vigencia de la cotización: ${formatDate(cotizacion.fechaVigencia)}`, 20, vigenciaY);

  // Observaciones
  if (cotizacion.observaciones) {
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('Observaciones', 20, vigenciaY + 15);

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    const splitObservaciones = doc.splitTextToSize(cotizacion.observaciones, pageWidth - 40);
    doc.text(splitObservaciones, 20, vigenciaY + 25);
  }

  // Footer
  const footerY = doc.internal.pageSize.getHeight() - 20;
  doc.setFillColor(...COLORS.light);
  doc.rect(0, footerY - 5, pageWidth, 25, 'F');
  doc.setTextColor(...COLORS.gray);
  doc.setFontSize(9);
  doc.text('Este documento es una cotización y no representa un compromiso de compra.', pageWidth / 2, footerY + 3, { align: 'center' });
  doc.text(`Generado el ${formatDate(new Date())}`, pageWidth / 2, footerY + 10, { align: 'center' });

  // Descargar
  doc.save(`Cotizacion_${cotizacion.id}_${cotizacion.clienteNombre.replace(/\s+/g, '_')}.pdf`);
};

/**
 * Generar PDF de Estado de Cuenta para Cliente
 */
export const generarEstadoCuentaPDF = (planPago, amortizaciones) => {
  const doc = new jsPDF();
  const pageWidth = doc.internal.pageSize.getWidth();

  // Header
  doc.setFillColor(...COLORS.primary);
  doc.rect(0, 0, pageWidth, 45, 'F');

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(24);
  doc.setFont('helvetica', 'bold');
  doc.text('ESTADO DE CUENTA', pageWidth / 2, 20, { align: 'center' });

  doc.setFontSize(12);
  doc.setFont('helvetica', 'normal');
  doc.text(`Plan de Pago #${planPago.id}`, pageWidth / 2, 30, { align: 'center' });
  doc.text(`Fecha: ${formatDate(new Date())}`, pageWidth / 2, 38, { align: 'center' });

  // Resumen del plan
  doc.setTextColor(...COLORS.dark);
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.text('Resumen del Financiamiento', 20, 60);

  // Tabla de resumen
  doc.autoTable({
    startY: 68,
    head: [['Concepto', 'Valor']],
    body: [
      ['Monto Total', formatCurrency(planPago.montoTotal)],
      ['Enganche', formatCurrency(planPago.enganche)],
      ['Monto Financiado', formatCurrency(planPago.montoFinanciado)],
      ['Tasa de Interés Anual', `${planPago.tasaInteresAnual}%`],
      ['Número de Pagos', planPago.numeroPagos],
      ['Total Pagado', formatCurrency(planPago.totalPagado)],
      ['Saldo Pendiente', formatCurrency(planPago.totalPendiente)],
      ['Avance', `${(planPago.porcentajeAvance || 0).toFixed(2)}%`]
    ],
    theme: 'striped',
    headStyles: {
      fillColor: COLORS.primary,
      textColor: [255, 255, 255],
      fontStyle: 'bold'
    },
    columnStyles: {
      0: { cellWidth: 80 },
      1: { cellWidth: 60, halign: 'right' }
    }
  });

  // Tabla de amortización
  const startY = doc.lastAutoTable.finalY + 15;
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.text('Tabla de Amortización', 20, startY);

  const amortData = amortizaciones.map(a => [
    a.numeroAmortizacion,
    formatDateShort(a.fechaVencimiento),
    formatCurrency(a.montoCapital),
    formatCurrency(a.montoInteres),
    formatCurrency(a.montoTotal),
    formatCurrency(a.montoPagado),
    a.estado
  ]);

  doc.autoTable({
    startY: startY + 8,
    head: [['#', 'Vencimiento', 'Capital', 'Interés', 'Total', 'Pagado', 'Estado']],
    body: amortData,
    theme: 'grid',
    headStyles: {
      fillColor: COLORS.secondary,
      textColor: [255, 255, 255],
      fontStyle: 'bold',
      fontSize: 8
    },
    bodyStyles: {
      fontSize: 7
    },
    columnStyles: {
      0: { cellWidth: 12 },
      1: { cellWidth: 25 },
      2: { cellWidth: 28, halign: 'right' },
      3: { cellWidth: 25, halign: 'right' },
      4: { cellWidth: 28, halign: 'right' },
      5: { cellWidth: 28, halign: 'right' },
      6: { cellWidth: 24 }
    },
    didParseCell: function(data) {
      if (data.section === 'body' && data.column.index === 6) {
        const estado = data.cell.raw;
        if (estado === 'PAGADA') {
          data.cell.styles.textColor = COLORS.success;
        } else if (estado === 'VENCIDA') {
          data.cell.styles.textColor = COLORS.danger;
        } else if (estado === 'PENDIENTE') {
          data.cell.styles.textColor = COLORS.warning;
        }
      }
    }
  });

  // Footer
  const footerY = doc.internal.pageSize.getHeight() - 15;
  doc.setTextColor(...COLORS.gray);
  doc.setFontSize(8);
  doc.text('Este es un estado de cuenta informativo. Para cualquier aclaración, comuníquese con nosotros.', pageWidth / 2, footerY, { align: 'center' });

  // Descargar
  doc.save(`EstadoCuenta_Plan_${planPago.id}.pdf`);
};

/**
 * =====================================================
 * PDFs INTERNOS (Administrativos)
 * =====================================================
 */

/**
 * Generar Reporte PDF de Terrenos
 */
export const generarReporteTerrenos = (terrenos, titulo = 'Reporte de Terrenos') => {
  const doc = new jsPDF('landscape');
  const pageWidth = doc.internal.pageSize.getWidth();

  // Header
  doc.setFillColor(...COLORS.dark);
  doc.rect(0, 0, pageWidth, 25, 'F');

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(18);
  doc.setFont('helvetica', 'bold');
  doc.text(titulo.toUpperCase(), pageWidth / 2, 15, { align: 'center' });

  // Subtítulo
  doc.setTextColor(...COLORS.gray);
  doc.setFontSize(10);
  doc.text(`Generado: ${formatDate(new Date())} | Total: ${terrenos.length} terrenos`, 14, 35);

  // Tabla
  const data = terrenos.map(t => [
    t.numeroLote,
    t.manzana || '-',
    t.proyectoNombre || '-',
    `${t.area} m²`,
    formatCurrency(t.precioFinal),
    t.estado
  ]);

  doc.autoTable({
    startY: 40,
    head: [['Lote', 'Manzana', 'Proyecto', 'Área', 'Precio', 'Estado']],
    body: data,
    theme: 'grid',
    headStyles: {
      fillColor: COLORS.primary,
      textColor: [255, 255, 255],
      fontStyle: 'bold'
    },
    didParseCell: function(data) {
      if (data.section === 'body' && data.column.index === 5) {
        const estado = data.cell.raw;
        if (estado === 'DISPONIBLE') {
          data.cell.styles.textColor = COLORS.success;
        } else if (estado === 'VENDIDO') {
          data.cell.styles.textColor = COLORS.danger;
        } else if (estado === 'APARTADO') {
          data.cell.styles.textColor = COLORS.warning;
        }
      }
    }
  });

  // Resumen
  const finalY = doc.lastAutoTable.finalY + 10;
  const disponibles = terrenos.filter(t => t.estado === 'DISPONIBLE').length;
  const vendidos = terrenos.filter(t => t.estado === 'VENDIDO').length;
  const apartados = terrenos.filter(t => t.estado === 'APARTADO').length;

  doc.setTextColor(...COLORS.dark);
  doc.setFontSize(10);
  doc.setFont('helvetica', 'bold');
  doc.text(`Resumen: Disponibles: ${disponibles} | Vendidos: ${vendidos} | Apartados: ${apartados}`, 14, finalY);

  doc.save(`Reporte_Terrenos_${new Date().toISOString().split('T')[0]}.pdf`);
};

/**
 * Generar Reporte PDF de Ventas
 */
export const generarReporteVentas = (ventas, titulo = 'Reporte de Ventas') => {
  const doc = new jsPDF('landscape');
  const pageWidth = doc.internal.pageSize.getWidth();

  // Header
  doc.setFillColor(...COLORS.dark);
  doc.rect(0, 0, pageWidth, 25, 'F');

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(18);
  doc.setFont('helvetica', 'bold');
  doc.text(titulo.toUpperCase(), pageWidth / 2, 15, { align: 'center' });

  // Subtítulo
  doc.setTextColor(...COLORS.gray);
  doc.setFontSize(10);
  const montoTotal = ventas.reduce((sum, v) => sum + Number(v.montoTotal || 0), 0);
  doc.text(`Generado: ${formatDate(new Date())} | Total: ${ventas.length} ventas | Monto: ${formatCurrency(montoTotal)}`, 14, 35);

  // Tabla
  const data = ventas.map(v => [
    v.id,
    formatDateShort(v.fechaVenta),
    v.compradorNombre,
    v.terrenoNumeroLote,
    formatCurrency(v.montoTotal),
    v.estado
  ]);

  doc.autoTable({
    startY: 40,
    head: [['ID', 'Fecha', 'Comprador', 'Terreno', 'Monto', 'Estado']],
    body: data,
    theme: 'grid',
    headStyles: {
      fillColor: COLORS.success,
      textColor: [255, 255, 255],
      fontStyle: 'bold'
    },
    didParseCell: function(data) {
      if (data.section === 'body' && data.column.index === 5) {
        const estado = data.cell.raw;
        if (estado === 'PAGADO') {
          data.cell.styles.textColor = COLORS.success;
        } else if (estado === 'CANCELADO') {
          data.cell.styles.textColor = COLORS.danger;
        } else if (estado === 'PENDIENTE') {
          data.cell.styles.textColor = COLORS.warning;
        }
      }
    }
  });

  doc.save(`Reporte_Ventas_${new Date().toISOString().split('T')[0]}.pdf`);
};

/**
 * Generar Reporte PDF de Clientes
 */
export const generarReporteClientes = (clientes, titulo = 'Reporte de Clientes') => {
  const doc = new jsPDF('landscape');
  const pageWidth = doc.internal.pageSize.getWidth();

  // Header
  doc.setFillColor(...COLORS.dark);
  doc.rect(0, 0, pageWidth, 25, 'F');

  doc.setTextColor(255, 255, 255);
  doc.setFontSize(18);
  doc.setFont('helvetica', 'bold');
  doc.text(titulo.toUpperCase(), pageWidth / 2, 15, { align: 'center' });

  // Subtítulo
  doc.setTextColor(...COLORS.gray);
  doc.setFontSize(10);
  doc.text(`Generado: ${formatDate(new Date())} | Total: ${clientes.length} clientes`, 14, 35);

  // Tabla
  const data = clientes.map(c => [
    c.nombreCompleto || `${c.nombre} ${c.apellido}`,
    c.email || '-',
    c.telefono,
    c.estadoCliente,
    c.totalVentas || 0
  ]);

  doc.autoTable({
    startY: 40,
    head: [['Nombre', 'Email', 'Teléfono', 'Estado', 'Ventas']],
    body: data,
    theme: 'grid',
    headStyles: {
      fillColor: COLORS.secondary,
      textColor: [255, 255, 255],
      fontStyle: 'bold'
    }
  });

  doc.save(`Reporte_Clientes_${new Date().toISOString().split('T')[0]}.pdf`);
};

export default {
  generarCotizacionPDF,
  generarEstadoCuentaPDF,
  generarReporteTerrenos,
  generarReporteVentas,
  generarReporteClientes
};
