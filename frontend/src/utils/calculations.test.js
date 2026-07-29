import assert from 'node:assert/strict';
import test from 'node:test';
import {
  calculateCotizacion,
  calculateMontoFinanciado,
  calculateTerrenoPrecioFinal,
} from './calculations.js';

test('calcula el precio final del terreno con ajuste y multiplicador', () => {
  assert.equal(
    calculateTerrenoPrecioFinal({ precioBase: 500000, ajustePrecio: 50000, multiplicadorPrecio: 1.1 }),
    605000,
  );
});

test('maneja valores nulos e inválidos en el cálculo de precio de terreno', () => {
  assert.equal(
    calculateTerrenoPrecioFinal({ precioBase: '500000', ajustePrecio: null, multiplicadorPrecio: undefined }),
    500000,
  );
  assert.equal(
    calculateTerrenoPrecioFinal({ precioBase: 'invalid', ajustePrecio: 'abc', multiplicadorPrecio: 'xyz' }),
    0,
  );
});

test('calcula el monto financiado correctamente', () => {
  assert.equal(calculateMontoFinanciado(600000, 100000), 500000);
});

test('monto financiado no puede ser negativo cuando enganche > montoTotal', () => {
  assert.equal(calculateMontoFinanciado(100000, 200000), 0);
  assert.equal(calculateMontoFinanciado(null, undefined), 0);
});

test('calcula cotización con descuento porcentual', () => {
  assert.deepEqual(
    calculateCotizacion({ precioBase: 700000, porcentajeDescuento: 10 }),
    { descuento: 70000, porcentajeDescuento: 10, precioFinal: 630000 },
  );
});

test('calcula cotización con descuento fijo', () => {
  assert.deepEqual(
    calculateCotizacion({ precioBase: 700000, tipoDescuento: 'fijo', descuento: 25000 }),
    { descuento: 25000, porcentajeDescuento: 3.571428571428571, precioFinal: 675000 },
  );
});

test('maneja precioBase cero y descuentos mayores al 100% en cotizaciones', () => {
  assert.deepEqual(
    calculateCotizacion({ precioBase: 0, porcentajeDescuento: 50 }),
    { descuento: 0, porcentajeDescuento: 0, precioFinal: 0 },
  );

  // Descuento no puede superar el precio base
  assert.deepEqual(
    calculateCotizacion({ precioBase: 100000, tipoDescuento: 'fijo', descuento: 150000 }),
    { descuento: 100000, porcentajeDescuento: 100, precioFinal: 0 },
  );
});
