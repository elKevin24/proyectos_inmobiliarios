import { test, expect } from '@playwright/test';
import { login } from './helpers/auth.js';
import { TEST_USER } from './helpers/fixtures.js';

test.describe('Planes de Pago', () => {
  test.beforeEach(async ({ page }) => {
    await login(page, TEST_USER.email, TEST_USER.password);
  });

  test('debe mostrar la lista de planes de pago', async ({ page }) => {
    await page.goto('/planes-pago');
    await expect(page.locator('h1')).toHaveText('Planes de Pago');
  });

  test('debe redirigir al login si no esta autenticado', async ({ page }) => {
    await page.goto('/login');
    await page.evaluate(() => localStorage.clear());
    await page.goto('/planes-pago');
    await expect(page).toHaveURL('/login');
  });
});
