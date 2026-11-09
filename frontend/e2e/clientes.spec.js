import { test, expect } from '@playwright/test';
import { login, createViaAPI, deleteViaAPI } from './helpers/auth.js';
import { TEST_USER } from './helpers/fixtures.js';

let authToken;

test.beforeAll(async ({ request }) => {
  const response = await request.post('http://localhost:8080/api/v1/auth/login', {
    data: { email: TEST_USER.email, password: TEST_USER.password },
  });
  const body = await response.json();
  authToken = body.access_token;
});

test.describe('Clientes CRUD', () => {
  let createdClientes = [];

  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test.afterEach(async ({ request }) => {
    for (const id of createdClientes) {
      await deleteViaAPI(request, `/clientes/${id}`, authToken).catch(() => {});
    }
    createdClientes = [];
  });

  test('debe mostrar la lista de clientes', async ({ page }) => {
    await page.goto('/clientes');
    await expect(page.locator('h1')).toHaveText('Clientes');
    await expect(page.locator('.search-input')).toBeVisible();
    await expect(page.locator('text=Nuevo Cliente')).toBeVisible();
  });

  test('debe tener filtros de busqueda y estado', async ({ page }) => {
    await page.goto('/clientes');
    await expect(page.locator('.search-input')).toBeVisible();
    await expect(page.locator('.filter-select')).toBeVisible();
  });

  test('debe navegar al formulario de nuevo cliente', async ({ page }) => {
    await page.goto('/clientes');
    await page.click('text=Nuevo Cliente');
    await expect(page).toHaveURL('/clientes/nuevo');
    await expect(page.locator('h1')).toHaveText('Nuevo Cliente');
  });

  test('debe crear un nuevo cliente', async ({ page }) => {
    await page.goto('/clientes/nuevo');

    const uniqueNombre = `Maria E2E ${Date.now()}`;
    await page.fill('#nombre', uniqueNombre);
    await page.fill('#apellidos', 'Lopez Sanchez');
    await page.fill('#email', `maria.test${Date.now()}@example.com`);
    await page.fill('#telefono', '5598765432');
    await page.selectOption('#estadoCliente', 'PROSPECTO');

    await page.click('button[type="submit"]');
    await page.waitForURL('/clientes', { timeout: 10_000 });
    await expect(page).toHaveURL('/clientes');
    await expect(page.locator('.cliente-card').filter({ hasText: uniqueNombre })).toBeVisible();
  });

  test('debe mostrar error al crear cliente sin campos requeridos', async ({ page }) => {
    await page.goto('/clientes/nuevo');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-nombre')).toHaveText('El nombre es requerido');
    await expect(page.getByTestId('error-email')).toHaveText('El email es requerido');
    await expect(page.getByTestId('error-telefono')).toHaveText('El teléfono es requerido');
  });

  test('debe mostrar error con email invalido', async ({ page }) => {
    await page.goto('/clientes/nuevo');
    await page.fill('#nombre', 'Test');
    await page.fill('#apellidos', 'User');
    await page.fill('#email', 'no-es-email');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-email')).toHaveText('Email inválido');
  });

  test('debe mostrar error con telefono invalido', async ({ page }) => {
    await page.goto('/clientes/nuevo');
    await page.fill('#nombre', 'Test');
    await page.fill('#apellidos', 'User');
    await page.fill('#email', 'test@test.com');
    await page.fill('#telefono', '123');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-telefono')).toHaveText('El teléfono debe tener 10 dígitos');
  });

  test('debe filtrar clientes por estado', async ({ page }) => {
    const cliente = await createViaAPI(
      page.request,
      '/clientes',
      {
        nombre: 'Filtro',
        apellidos: 'Prospecto E2E',
        email: `filtro.prospecto${Date.now()}@test.com`,
        telefono: '5512345678',
        estadoCliente: 'PROSPECTO',
      },
      authToken
    );
    createdClientes.push(cliente.id);

    const comprador = await createViaAPI(
      page.request,
      '/clientes',
      {
        nombre: 'Filtro',
        apellidos: 'Comprador E2E',
        email: `filtro.comprador${Date.now()}@test.com`,
        telefono: '5512345678',
        estadoCliente: 'COMPRADOR',
      },
      authToken
    );
    createdClientes.push(comprador.id);

    await page.goto('/clientes');
    await page.selectOption('.filter-select', 'PROSPECTO');
    await expect(page.locator('.cliente-card').filter({ hasText: 'Filtro Prospecto E2E' })).toHaveCount(1);
    await expect(page.locator('.cliente-card').filter({ hasText: 'Filtro Comprador E2E' })).toHaveCount(0);
  });

  test('debe buscar clientes por nombre', async ({ page }) => {
    const cliente = await createViaAPI(
      page.request,
      '/clientes',
      {
        nombre: 'BusquedaUnica',
        apellidos: 'Cliente E2E',
        email: `busqueda.unica${Date.now()}@test.com`,
        telefono: '5512345678',
        estadoCliente: 'PROSPECTO',
      },
      authToken
    );
    createdClientes.push(cliente.id);

    await page.goto('/clientes');
    await page.fill('.search-input', 'BusquedaUnica');
    await expect(page.locator('.cliente-card')).toHaveCount(1);
    await expect(page.locator('.cliente-card')).toContainText('BusquedaUnica Cliente E2E');
  });

  test('debe navegar a editar cliente', async ({ page }) => {
    const cliente = await createViaAPI(
      page.request,
      '/clientes',
      {
        nombre: 'Edit',
        apellidos: 'Me',
        email: `edit.client${Date.now()}@test.com`,
        telefono: '5512345678',
        estadoCliente: 'PROSPECTO',
      },
      authToken
    );
    createdClientes.push(cliente.id);

    await page.goto('/clientes');
    await page.waitForLoadState('networkidle');

    const editBtn = page.locator(`a[href="/clientes/${cliente.id}/editar"]`);
    await expect(editBtn).toBeVisible();
    await editBtn.click();
    await expect(page.locator('h1')).toHaveText('Editar Cliente');
  });

  test('debe cancelar creacion y volver a la lista', async ({ page }) => {
    await page.goto('/clientes/nuevo');
    await page.click('text=Cancelar');
    await expect(page).toHaveURL('/clientes');
  });
});
