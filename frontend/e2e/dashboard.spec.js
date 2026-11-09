import { test, expect } from '@playwright/test';
import { login } from './helpers/auth.js';
import { TEST_USER } from './helpers/fixtures.js';

test.describe('Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test('debe mostrar el dashboard con estadisticas', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.dashboard')).toBeVisible({ timeout: 10_000 });
    await expect(page.getByRole('heading', { name: 'Buenos días, equipo' })).toBeVisible();
    await expect(page.locator('.dashboard-status')).toContainText('Datos actualizados');
  });

  test('debe mostrar secciones de estadisticas', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('text=Proyectos')).toBeVisible();
    await expect(page.locator('text=Inventario de Terrenos')).toBeVisible();
    await expect(page.locator('text=Clientes')).toBeVisible();
    await expect(page.locator('text=Ventas y Finanzas')).toBeVisible();
    await expect(page.locator('text=Apartados')).toBeVisible();
  });

  test('debe mostrar tarjetas de estadisticas', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('text=Total Proyectos')).toBeVisible();
    await expect(page.locator('text=Total Terrenos')).toBeVisible();
    await expect(page.locator('text=Total Clientes')).toBeVisible();
    await expect(page.locator('text=Total Ventas')).toBeVisible();
  });

  test('debe mostrar acciones rapidas', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('text=Acciones Rapidas')).toBeVisible();
    await expect(page.locator('text=Nuevo Proyecto')).toBeVisible();
    await expect(page.locator('text=Agregar Terreno')).toBeVisible();
    await expect(page.locator('text=Nuevo Cliente')).toBeVisible();
  });

  test('debe mostrar navegacion rapida', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    await expect(page.locator('text=Navegacion Rapida')).toBeVisible();
    await expect(page.locator('text=Ver Proyectos')).toBeVisible();
    await expect(page.locator('text=Ver Terrenos')).toBeVisible();
    await expect(page.locator('text=Ver Clientes')).toBeVisible();
    await expect(page.locator('text=Ver Ventas')).toBeVisible();
  });
});

test.describe('Navegacion Sidebar', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test('debe mostrar el sidebar con menu de navegacion', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.sidebar')).toBeVisible();
    await expect(page.locator('text=Dashboard')).toBeVisible();
    await expect(page.locator('text=Proyectos')).toBeVisible();
    await expect(page.locator('text=Terrenos')).toBeVisible();
    await expect(page.locator('text=Clientes')).toBeVisible();
    await expect(page.locator('text=Ventas')).toBeVisible();
    await expect(page.locator('text=Apartados')).toBeVisible();
    await expect(page.locator('text=Cotizaciones')).toBeVisible();
    await expect(page.locator('text=Planes de Pago')).toBeVisible();
  });

  test('debe navegar a proyectos desde el sidebar', async ({ page }) => {
    await page.goto('/');
    await page.locator('.sidebar').locator('a', { hasText: 'Proyectos' }).first().click();
    await expect(page).toHaveURL('/proyectos');
    await expect(page.locator('.sidebar a.active')).toHaveText(/Proyectos/);
  });

  test('debe navegar a terrenos desde el sidebar', async ({ page }) => {
    await page.goto('/');
    await page.locator('.sidebar').locator('a', { hasText: 'Terrenos' }).first().click();
    await expect(page).toHaveURL('/terrenos');
  });

  test('debe navegar a clientes desde el sidebar', async ({ page }) => {
    await page.goto('/');
    await page.locator('.sidebar').locator('a', { hasText: 'Clientes' }).first().click();
    await expect(page).toHaveURL('/clientes');
  });

  test('debe navegar a ventas desde el sidebar', async ({ page }) => {
    await page.goto('/');
    await page.locator('.sidebar').locator('a', { hasText: 'Ventas' }).first().click();
    await expect(page).toHaveURL('/ventas');
  });

  test('debe navegar a apartados desde el sidebar', async ({ page }) => {
    await page.goto('/');
    await page.locator('.sidebar').locator('a', { hasText: 'Apartados' }).first().click();
    await expect(page).toHaveURL('/apartados');
  });

  test('debe navegar a cotizaciones desde el sidebar', async ({ page }) => {
    await page.goto('/');
    await page.locator('.sidebar').locator('a', { hasText: 'Cotizaciones' }).first().click();
    await expect(page).toHaveURL('/cotizaciones');
  });

  test('debe navegar a planes de pago desde el sidebar', async ({ page }) => {
    await page.goto('/');
    await page.locator('.sidebar').locator('a', { hasText: 'Planes de Pago' }).first().click();
    await expect(page).toHaveURL('/planes-pago');
  });

  test('debe mostrar email del usuario en el sidebar', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.user-info')).toContainText(TEST_USER.email);
  });

  test('debe tener boton de cerrar sesion', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.btn-logout')).toBeVisible();
    await expect(page.locator('text=Cerrar Sesion')).toBeVisible();
  });

  test('debe tener boton de cambiar tema', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('.btn-theme-toggle')).toBeVisible();
  });
});

test.describe('Proteccion de rutas', () => {
  test('debe redirigir al login cuando no esta autenticado', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveURL('/login');
  });

  test('debe redirigir rutas protegidas al login', async ({ page }) => {
    const protectedRoutes = [
      '/proyectos',
      '/terrenos',
      '/clientes',
      '/ventas',
      '/apartados',
      '/cotizaciones',
      '/planes-pago',
    ];

    for (const route of protectedRoutes) {
      await page.goto(route);
      await expect(page).toHaveURL('/login');
    }
  });

  test('debe redirigir ruta desconocida al dashboard', async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
    await page.goto('/ruta-que-no-existe');
    await expect(page).toHaveURL('/');
  });
});
