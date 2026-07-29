const toNumber = (value, fallback = 0) => {
  if (value === null || value === undefined) return fallback;
  const parsed = Number.parseFloat(value);
  return Number.isFinite(parsed) ? parsed : fallback;
};

export function calculateTerrenoPrecioFinal({ precioBase, ajustePrecio = 0, multiplicadorPrecio = 1 } = {}) {
  const base = toNumber(precioBase);
  const ajuste = toNumber(ajustePrecio);
  const mult = toNumber(multiplicadorPrecio, 1);
  return (base + ajuste) * mult;
}

export function calculateMontoFinanciado(montoTotal, enganche) {
  const total = toNumber(montoTotal);
  const eng = toNumber(enganche);
  return Math.max(0, total - eng);
}

export function calculateCotizacion({ precioBase, tipoDescuento = 'porcentaje', porcentajeDescuento = 0, descuento = 0 } = {}) {
  const base = Math.max(0, toNumber(precioBase));
  const requestedDiscount = tipoDescuento === 'fijo'
    ? Math.min(base, Math.max(0, toNumber(descuento)))
    : (base * Math.min(100, Math.max(0, toNumber(porcentajeDescuento)))) / 100;
  const finalPrice = Math.max(0, base - requestedDiscount);

  return {
    descuento: requestedDiscount,
    porcentajeDescuento: base === 0 ? 0 : (requestedDiscount / base) * 100,
    precioFinal: finalPrice,
  };
}
