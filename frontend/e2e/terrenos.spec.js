import { test, expect } from '@playwright/test';
import { login, createViaAPI, deleteViaAPI } from './helpers/auth.js';
import { TEST_USER } from './helpers/fixtures.js';

let authToken;
let proyectoId;
let fixtureTerrenoId;
let fixtureVendidoTerrenoId;

test.beforeAll(async ({ request }) => {
  const response = await request.post('http://localhost:8080/api/v1/auth/login', {
    data: { email: TEST_USER.email, password: TEST_USER.password },
  });
  const body = await response.json();
  authToken = body.access_token;

  const proyecto = await createViaAPI(
    request,
    '/proyectos',
    {
      nombre: `Proyecto Terrenos ${Date.now()}`,
      estado: 'EN_VENTA',
    },
    authToken
  );
  proyectoId = proyecto.id;

  const terreno = await createViaAPI(
    request,
    '/terrenos',
    {
      numero: `F-${Date.now()}`,
      nombre: 'Lote Filtro E2E',
      proyectoId,
      area: 200,
      precioBase: 500000,
      estado: 'DISPONIBLE',
    },
    authToken
  );
  fixtureTerrenoId = terreno.id;

  const vendido = await createViaAPI(
    request,
    '/terrenos',
    {
      numero: `V-${Date.now()}`,
      nombre: 'Lote No Disponible E2E',
      proyectoId,
      area: 200,
      precioBase: 500000,
      estado: 'VENDIDO',
    },
    authToken
  );
  fixtureVendidoTerrenoId = vendido.id;
});

test.afterAll(async ({ request }) => {
  try {
    if (fixtureTerrenoId) await deleteViaAPI(request, `/terrenos/${fixtureTerrenoId}`, authToken);
    if (fixtureVendidoTerrenoId) await deleteViaAPI(request, `/terrenos/${fixtureVendidoTerrenoId}`, authToken);
  } finally {
    if (proyectoId) await deleteViaAPI(request, `/proyectos/${proyectoId}`, authToken).catch(() => {});
  }
});

test.describe('Terrenos CRUD', () => {
  let terrenoId;

  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test('debe mostrar la lista de terrenos', async ({ page }) => {
    await page.goto('/terrenos');
    await expect(page.locator('h1')).toHaveText('Terrenos');
    await expect(page.locator('.search-input')).toBeVisible();
    await expect(page.locator('text=Nuevo Terreno')).toBeVisible();
  });

  test('debe tener filtros de busqueda y estado', async ({ page }) => {
    await page.goto('/terrenos');
    await expect(page.locator('.search-input')).toBeVisible();
    await expect(page.locator('.filter-select')).toBeVisible();
  });

  test('debe navegar al formulario de nuevo terreno', async ({ page }) => {
    await page.goto('/terrenos');
    await page.click('text=Nuevo Terreno');
    await expect(page).toHaveURL('/terrenos/nuevo');
    await expect(page.locator('h1')).toHaveText('Nuevo Terreno');
  });

  test('debe crear un nuevo terreno', async ({ page }) => {
    await page.goto('/terrenos/nuevo');

    const uniqueNumero = `T-${Date.now()}`;
    await page.fill('#numero', uniqueNumero);
    await page.fill('#nombre', 'Lote E2E Test');
    await page.selectOption('#proyectoId', String(proyectoId));
    await page.selectOption('#estado', 'DISPONIBLE');
    await page.fill('#area', '200');
    await page.fill('#precioBase', '500000');
    await page.fill('#ajustePrecio', '0');
    await page.fill('#multiplicadorPrecio', '1');
    await page.fill('#descripcion', 'Terreno de prueba E2E');

    await page.click('button[type="submit"]');
    await page.waitForURL('/terrenos', { timeout: 10_000 });
    await expect(page).toHaveURL('/terrenos');
    await expect(page.locator('.terreno-card').filter({ hasText: uniqueNumero })).toBeVisible();
  });

  test('debe mostrar error al crear terreno sin numero', async ({ page }) => {
    await page.goto('/terrenos/nuevo');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-numero')).toHaveText('El número es requerido');
  });

  test('debe mostrar error con area invalida', async ({ page }) => {
    await page.goto('/terrenos/nuevo');
    await page.fill('#numero', 'T-001');
    await page.fill('#area', '-10');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-area')).toHaveText('El área debe ser mayor a 0');
  });

  test('debe calcular el precio final', async ({ page }) => {
    await page.goto('/terrenos/nuevo');

    await page.fill('#precioBase', '500000');
    await page.fill('#ajustePrecio', '50000');
    await page.fill('#multiplicadorPrecio', '1.1');

    const precioFinal = page.locator('.precio-final-value');
    await expect(precioFinal).toHaveText('$605,000.00');
  });

  test('debe filtrar terrenos por estado', async ({ page }) => {
    await page.goto('/terrenos');
    await page.selectOption('.filter-select', 'DISPONIBLE');
    await expect(page.locator('.terreno-card').filter({ hasText: 'Lote Filtro E2E' })).toHaveCount(1);
    await expect(page.locator('.terreno-card').filter({ hasText: 'Lote No Disponible E2E' })).toHaveCount(0);
  });

  test('debe buscar terrenos por nombre', async ({ page }) => {
    await page.goto('/terrenos');
    await page.fill('.search-input', 'Lote');
    await expect(page.locator('.terreno-card').filter({ hasText: 'Lote Filtro E2E' })).toHaveCount(1);
  });

  test('debe cancelar creacion y volver a la lista', async ({ page }) => {
    await page.goto('/terrenos/nuevo');
    await page.click('text=Cancelar');
    await expect(page).toHaveURL('/terrenos');
  });

  test.afterEach(async ({ request }) => {
    if (terrenoId) {
      await deleteViaAPI(request, `/terrenos/${terrenoId}`, authToken);
      terrenoId = null;
    }
  });
});
