import { test, expect } from '@playwright/test';
import { login, createViaAPI, deleteViaAPI } from './helpers/auth.js';
import { TEST_USER } from './helpers/fixtures.js';

let authToken;
let terrenoId;
let proyectoId;

test.beforeAll(async ({ request }) => {
  const response = await request.post('http://localhost:8080/api/v1/auth/login', {
    data: { email: TEST_USER.email, password: TEST_USER.password },
  });
  const body = await response.json();
  authToken = body.access_token;

  const proyecto = await createViaAPI(
    request,
    '/proyectos',
    { nombre: `Proyecto Cotizacion ${Date.now()}`, estado: 'EN_VENTA' },
    authToken
  );
  proyectoId = proyecto.id;

  const terreno = await createViaAPI(
    request,
    '/terrenos',
    {
      numero: `C-${Date.now()}`,
      proyectoId: proyecto.id,
      area: 180,
      precioBase: 700000,
      estado: 'DISPONIBLE',
    },
    authToken
  );
  terrenoId = terreno.id;
});

test.afterAll(async ({ request }) => {
  try {
    if (terrenoId) await deleteViaAPI(request, `/terrenos/${terrenoId}`, authToken).catch(() => {});
  } finally {
    if (proyectoId) await deleteViaAPI(request, `/proyectos/${proyectoId}`, authToken).catch(() => {});
  }
});

test.describe('Cotizaciones', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test('debe mostrar la lista de cotizaciones', async ({ page }) => {
    await page.goto('/cotizaciones');
    await expect(page.locator('h1')).toHaveText(/Cotizaci[oó]n|Cotizaciones/i);
    await expect(page.getByText(/Nueva Cotizaci[oó]n/i).first()).toBeVisible();
  });

  test('debe navegar al formulario de nueva cotizacion', async ({ page }) => {
    await page.goto('/cotizaciones');
    await page.getByText(/Nueva Cotizaci[oó]n/i).first().click();
    await expect(page.locator('h1')).toHaveText(/Nueva Cotizaci[oó]n/i);
  });

  test('debe mostrar campos del formulario de cotizacion', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');

    await expect(page.locator('#terrenoId')).toBeVisible();
    await expect(page.locator('#clienteNombre')).toBeVisible();
    await expect(page.locator('#clienteEmail')).toBeVisible();
    await expect(page.locator('#clienteTelefono')).toBeVisible();
    await expect(page.locator('#precioBase')).toBeVisible();
    await expect(page.locator('#fechaVigencia')).toBeVisible();
  });

  test('debe mostrar radio buttons para tipo de descuento', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');

    await expect(page.getByText('Porcentaje', { exact: true })).toBeVisible();
    await expect(page.locator('text=Monto fijo')).toBeVisible();
  });

  test('debe mostrar campo de porcentaje de descuento por defecto', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');

    await expect(page.locator('#porcentajeDescuento')).toBeVisible();
  });

  test('debe mostrar error al crear cotizacion sin campos requeridos', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-terreno')).toHaveText(/Seleccione un terreno|El terreno es requerido/i);
  });

  test('debe mostrar error con email invalido', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');
    await page.fill('#clienteNombre', 'Cliente Test');
    await page.fill('#clienteEmail', 'no-email');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-cliente-email')).toHaveText(/Email inv[aá]lido|El email no es v[aá]lido/i);
  });

  test('debe calcular precio final con descuento por porcentaje', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');

    await page.selectOption('#terrenoId', String(terrenoId));
    await page.fill('#porcentajeDescuento', '10');

    await expect(page.getByTestId('precio-final')).toHaveValue('630000.00');
  });

  test('debe alternar entre tipos de descuento correctamente sin loop', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');

    // 1. Descuento porcentual
    await page.selectOption('#terrenoId', String(terrenoId));
    await page.fill('#porcentajeDescuento', '10');
    await expect(page.getByTestId('precio-final')).toHaveValue('630000.00');

    // 2. Cambio a descuento fijo
    await page.getByTestId('discount-mode-fixed').check();
    await page.getByTestId('discount-fixed-input').fill('140000');
    // Verifica que el porcentaje calculado se actualice y el precio final baje
    // Con precio base 700000 y descuento 140000, porcentaje es 20%
    await expect(page.locator('.form-group', { hasText: 'Porcentaje Calculado (%)' }).locator('input')).toHaveValue(/20(\.00)?%/);
    await expect(page.getByTestId('precio-final')).toHaveValue('560000.00');

    // 3. Cambio nuevamente a porcentaje
    await page.getByTestId('discount-mode-percent').check();
    await page.getByTestId('discount-percent-input').fill('50');
    // Con precio base 700000 y descuento 50%, descuento es 350000
    // En el modo porcentaje, el descuento calculado está en un input disabled con formato moneda o similar, o podemos checar precioFinal directamente
    await expect(page.getByTestId('precio-final')).toHaveValue('350000.00');
  });

  test('debe calcular descuento fijo', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');
    await page.selectOption('#terrenoId', String(terrenoId));
    await page.getByTestId('discount-mode-fixed').check();
    await page.getByTestId('discount-fixed-input').fill('25000');

    await expect(page.getByTestId('precio-final')).toHaveValue('675000.00');
  });

  test('debe crear una nueva cotizacion', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');

    const uniqueCliente = `Cotizacion Cliente ${Date.now()}`;
    await page.selectOption('#terrenoId', String(terrenoId));
    await page.fill('#clienteNombre', uniqueCliente);

    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const fechaVigenciaStr = tomorrow.toISOString().split('T')[0];
    await page.fill('#fechaVigencia', fechaVigenciaStr);

    await page.click('button[type="submit"]');
    await page.waitForURL('/cotizaciones', { timeout: 10_000 });
    await expect(page).toHaveURL('/cotizaciones');
    await expect(page.locator('.data-table tr, .cotizacion-card').filter({ hasText: uniqueCliente })).toBeVisible();
  });

  test('debe cancelar y volver a la lista', async ({ page }) => {
    await page.goto('/cotizaciones/nueva');
    await page.click('text=Cancelar');
    await expect(page).toHaveURL('/cotizaciones');
  });
});
