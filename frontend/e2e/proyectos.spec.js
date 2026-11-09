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

test.describe('Proyectos CRUD', () => {
  let createdProyectos = [];

  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test.afterEach(async ({ request }) => {
    for (const id of createdProyectos) {
      await deleteViaAPI(request, `/proyectos/${id}`, authToken).catch(() => {});
    }
    createdProyectos = [];
  });

  test('debe mostrar la lista de proyectos', async ({ page }) => {
    await page.goto('/proyectos');
    await expect(page.locator('h1')).toHaveText('Proyectos');
    await expect(page.locator('.search-input')).toBeVisible();
    await expect(page.locator('text=Nuevo Proyecto')).toBeVisible();
  });

  test('debe navegar al formulario de nuevo proyecto', async ({ page }) => {
    await page.goto('/proyectos');
    await page.click('text=Nuevo Proyecto');
    await expect(page).toHaveURL('/proyectos/nuevo');
    await expect(page.locator('h1')).toHaveText('Nuevo Proyecto');
  });

  test('debe crear un nuevo proyecto', async ({ page }) => {
    await page.goto('/proyectos/nuevo');

    const uniqueNombre = `Proyecto Test ${Date.now()}`;
    await page.fill('#nombre', uniqueNombre);
    await page.selectOption('#estadoProyecto', 'PLANIFICACION');
    await page.fill('#descripcion', 'Proyecto de prueba E2E');
    await page.fill('#ubicacion', 'Zona Norte');
    await page.fill('#direccion', 'Av. Principal #123');
    await page.fill('#ciudad', 'Ciudad de Mexico');
    await page.fill('#estado', 'CDMX');
    await page.fill('#codigoPostal', '01234');
    await page.fill('#precioBaseM2', '5000');
    await page.fill('#areaTotal', '10000');

    await page.click('button[type="submit"]');
    await page.waitForURL('/proyectos', { timeout: 10_000 });
    await expect(page).toHaveURL('/proyectos');
    await expect(page.locator('.proyecto-card').filter({ hasText: uniqueNombre })).toBeVisible();
  });

  test('debe mostrar error al crear proyecto sin nombre', async ({ page }) => {
    await page.goto('/proyectos/nuevo');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-nombre')).toHaveText('El nombre es requerido');
  });

  test('debe mostrar error con codigo postal invalido', async ({ page }) => {
    await page.goto('/proyectos/nuevo');
    await page.fill('#nombre', 'Proyecto Test');
    await page.fill('#codigoPostal', '123');
    await page.click('button[type="submit"]');

    await expect(page.getByTestId('error-codigo-postal')).toHaveText('El código postal debe tener 5 dígitos');
  });

  test('debe buscar proyectos por nombre', async ({ page }) => {
    const proyecto = await createViaAPI(
      page.request,
      '/proyectos',
      {
        nombre: `BusquedaUnicaProyecto${Date.now()}`,
        estado: 'PLANIFICACION',
      },
      authToken
    );
    createdProyectos.push(proyecto.id);

    await page.goto('/proyectos');

    const searchInput = page.locator('.search-input');
    await searchInput.fill(proyecto.nombre);

    await expect(page.locator('.proyecto-card')).toHaveCount(1);
    await expect(page.locator('.proyecto-card')).toContainText(proyecto.nombre);
  });

  test('debe filtrar proyectos vacios', async ({ page }) => {
    await page.goto('/proyectos');
    await page.fill('.search-input', 'XYZNOMATCH12345');
    await expect(page.locator('.empty-state')).toBeVisible();
  });

  test('debe navegar a editar proyecto', async ({ page }) => {
    const response = await createViaAPI(
      page.request,
      '/proyectos',
      {
        nombre: `Proyecto Edit ${Date.now()}`,
        estado: 'PLANIFICACION',
        descripcion: 'Para editar',
      },
      authToken
    );
    createdProyectos.push(response.id);

    await page.goto('/proyectos');
    await page.waitForLoadState('networkidle');

    const editBtn = page.locator(`a[href="/proyectos/${response.id}/editar"]`);
    await expect(editBtn).toBeVisible();
    await editBtn.click();
    await expect(page.locator('h1')).toHaveText('Editar Proyecto');
  });

  test('debe cancelar creacion y volver a la lista', async ({ page }) => {
    await page.goto('/proyectos/nuevo');
    await page.click('text=Cancelar');
    await expect(page).toHaveURL('/proyectos');
  });
});
