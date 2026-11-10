import { test, expect } from '@playwright/test';
import { login, createViaAPI, deleteViaAPI } from './helpers/auth.js';
import { TEST_USER } from './helpers/fixtures.js';

let authToken;
let proyectoId;
let terrenoId;
let clienteId;

test.beforeAll(async ({ request }) => {
  const response = await request.post('http://localhost:8080/api/v1/auth/login', {
    data: { email: TEST_USER.email, password: TEST_USER.password },
  });
  const body = await response.json();
  authToken = body.access_token;

  const proyecto = await createViaAPI(
    request,
    '/proyectos',
    { nombre: `Proyecto Venta ${Date.now()}`, estado: 'EN_VENTA' },
    authToken
  );
  proyectoId = proyecto.id;

  const terreno = await createViaAPI(
    request,
    '/terrenos',
    {
      numero: `V-${Date.now()}`,
      proyectoId,
      area: 150,
      precioBase: 600000,
      estado: 'DISPONIBLE',
    },
    authToken
  );
  terrenoId = terreno.id;

  const cliente = await createViaAPI(
    request,
    '/clientes',
    {
      nombre: 'Cliente',
      apellidos: 'Venta Test',
      email: `venta.test${Date.now()}@test.com`,
      telefono: '5512345678',
      estadoCliente: 'PROSPECTO',
    },
    authToken
  );
  clienteId = cliente.id;
});

test.afterAll(async ({ request }) => {
  if (terrenoId) await deleteViaAPI(request, `/terrenos/${terrenoId}`, authToken).catch(() => {});
  if (clienteId) await deleteViaAPI(request, `/clientes/${clienteId}`, authToken).catch(() => {});
  if (proyectoId) await deleteViaAPI(request, `/proyectos/${proyectoId}`, authToken).catch(() => {});
});

test.describe('Ventas', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test('debe mostrar la lista de ventas', async ({ page }) => {
    await page.goto('/ventas');
    await expect(page.locator('h1')).toHaveText('Ventas');
    await expect(page.locator('text=Nueva Venta')).toBeVisible();
  });

  test('debe navegar al formulario de nueva venta', async ({ page }) => {
    await page.goto('/ventas');
    await page.click('text=Nueva Venta');
    await expect(page).toHaveURL('/ventas/nueva');
    await expect(page.locator('h1')).toHaveText('Nueva Venta');
  });

  test('debe mostrar campos del formulario de venta', async ({ page }) => {
    await page.goto('/ventas/nueva');

    await expect(page.locator('#terrenoId')).toBeVisible();
    await expect(page.locator('#clienteId')).toBeVisible();
    await expect(page.locator('#montoTotal')).toBeVisible();
    await expect(page.locator('#enganche')).toBeVisible();
    await expect(page.locator('#metodoPago')).toBeVisible();
  });

  test('debe mostrar error al crear venta sin campos requeridos', async ({ page }) => {
    await page.goto('/ventas/nueva');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-terreno')).toHaveText('El terreno es requerido');
  });

  test('debe calcular monto financiado', async ({ page }) => {
    await page.goto('/ventas/nueva');

    await page.fill('#montoTotal', '600000');
    await page.fill('#enganche', '100000');

    const montoInfo = page.locator('.monto-info');
    await expect(montoInfo).toBeVisible();
    await expect(page.locator('text=Monto a Financiar')).toBeVisible();
    await expect(page.locator('.monto-info .highlight .value')).toHaveText(/^\$500,000\.00$/);
  });

  test('debe mostrar plan de financiamiento cuando hay monto a financiar', async ({ page }) => {
    await page.goto('/ventas/nueva');

    await page.fill('#montoTotal', '600000');
    await page.fill('#enganche', '100000');

    await expect(page.getByTestId('plan-financiamiento')).toBeVisible();
    await expect(page.locator('#tipoPlan')).toBeVisible();
    await expect(page.locator('#numeroCuotas')).toBeVisible();
    await expect(page.locator('#frecuenciaPago')).toBeVisible();
  });

  test('debe ocultar plan de financiamiento con enganche total', async ({ page }) => {
    await page.goto('/ventas/nueva');

    await page.fill('#montoTotal', '100000');
    await page.fill('#enganche', '100000');

    await expect(page.getByTestId('plan-financiamiento')).not.toBeVisible();
  });

  test('debe validar que el enganche no supere el monto total', async ({ page }) => {
    await page.goto('/ventas/nueva');
    await page.fill('#montoTotal', '100000');
    await page.fill('#enganche', '100001');
    await page.click('#metodoPago');

    await expect(page.getByTestId('error-enganche')).toHaveText('El enganche no puede ser mayor al monto total');
    await expect(page.getByTestId('plan-financiamiento')).not.toBeVisible();
  });

  test('debe crear una nueva venta con financiamiento', async ({ page }) => {
    await page.goto('/ventas/nueva');

    await page.selectOption('#terrenoId', String(terrenoId));
    await page.selectOption('#clienteId', String(clienteId));
    await page.fill('#enganche', '100000');

    await page.click('button[type="submit"]');
    await page.waitForURL('/ventas', { timeout: 10_000 });
    await expect(page).toHaveURL('/ventas');
    await expect(page.locator('.venta-card, .table').first()).toBeVisible();
  });

  test('debe cancelar creacion y volver a la lista', async ({ page }) => {
    await page.goto('/ventas/nueva');
    await page.click('text=Cancelar');
    await expect(page).toHaveURL('/ventas');
  });
});
