import { test, expect } from '@playwright/test';
import { login, createViaAPI, deleteViaAPI } from './helpers/auth.js';
import { TEST_USER } from './helpers/fixtures.js';

let authToken;
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
    { nombre: `Proyecto Apartado ${Date.now()}`, estado: 'EN_VENTA' },
    authToken
  );

  const terreno = await createViaAPI(
    request,
    '/terrenos',
    {
      numero: `A-${Date.now()}`,
      proyectoId: proyecto.id,
      area: 120,
      precioBase: 400000,
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
      apellidos: 'Apartado Test',
      email: `apartado.test${Date.now()}@test.com`,
      telefono: '5512345678',
      estadoCliente: 'INTERESADO',
    },
    authToken
  );
  clienteId = cliente.id;
});

test.afterAll(async ({ request }) => {
  if (terrenoId) await deleteViaAPI(request, `/terrenos/${terrenoId}`, authToken).catch(() => {});
  if (clienteId) await deleteViaAPI(request, `/clientes/${clienteId}`, authToken).catch(() => {});
});

test.describe('Apartados', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test('debe mostrar la lista de apartados', async ({ page }) => {
    await page.goto('/apartados');
    await expect(page.locator('h1')).toHaveText('Apartados');
    await expect(page.locator('text=Nuevo Apartado')).toBeVisible();
  });

  test('debe navegar al formulario de nuevo apartado', async ({ page }) => {
    await page.goto('/apartados');
    await page.click('text=Nuevo Apartado');
    await expect(page.locator('h1')).toHaveText('Nuevo Apartado');
  });

  test('debe mostrar campos del formulario de apartado', async ({ page }) => {
    await page.goto('/apartados/nuevo');

    await expect(page.locator('#terrenoId')).toBeVisible();
    await expect(page.locator('#clienteId')).toBeVisible();
    await expect(page.locator('#diasVigencia')).toBeVisible();
    await expect(page.locator('#observaciones')).toBeVisible();
  });

  test('debe mostrar radio buttons para tipo de monto', async ({ page }) => {
    await page.goto('/apartados/nuevo');

    const radioGroup = page.locator('.radio-group');
    await expect(radioGroup).toBeVisible();
    await expect(page.locator('text=Porcentaje del precio')).toBeVisible();
    await expect(page.locator('text=Monto fijo')).toBeVisible();
  });

  test('debe mostrar campo de porcentaje por defecto', async ({ page }) => {
    await page.goto('/apartados/nuevo');

    await expect(page.locator('#porcentajeApartado')).toBeVisible();
    await expect(page.locator('#porcentajeApartado')).toHaveValue('10');
  });

  test('debe mostrar error al crear apartado sin campos requeridos', async ({ page }) => {
    await page.goto('/apartados/nuevo');
    await page.click('button[type="submit"]');

    await expect(page.locator('.error-message').first()).toBeVisible();
  });

  test('debe validar dias de vigencia', async ({ page }) => {
    await page.goto('/apartados/nuevo');
    await page.fill('#diasVigencia', '0');
    await page.click('button[type="submit"]');

    await expect(page.locator('.error-message').first()).toBeVisible();
  });

  test('debe cancelar y volver a la lista', async ({ page }) => {
    await page.goto('/apartados/nuevo');
    await page.click('text=Cancelar');
    await expect(page).toHaveURL('/apartados');
  });
});
