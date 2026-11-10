import { test, expect } from '@playwright/test';
import { login } from './helpers/auth.js';
import { TEST_USER, NEW_USER_DATA } from './helpers/fixtures.js';

test.describe('Autenticacion', () => {
  test.describe('Login', () => {
    test('debe mostrar el formulario de login', async ({ page }) => {
      await page.goto('/login');
      await expect(page.locator('h1')).toHaveText(/[iI]niciar [sS]esi[oó]n/i);
      await expect(page.getByLabel(/[cC]orreo [eE]lectr[oó]nico|Email/i)).toBeVisible();
      await expect(page.locator('#email')).toBeVisible();
      await expect(page.locator('#password')).toBeVisible();
      await expect(page.locator('button[type="submit"]')).toHaveText(/[iI]niciar [sS]esi[oó]n/i);
    });

    test('debe mostrar error con credenciales invalidas', async ({ page }) => {
      await page.goto('/login');
      await page.fill('#email', 'invalido@test.com');
      await page.fill('#password', 'wrongpass');
      await page.click('button[type="submit"]');

      await expect(page.locator('.alert-error')).toBeVisible({ timeout: 5000 });
    });

    test('debe mostrar error de validacion con campos vacios', async ({ page }) => {
      await page.goto('/login');
      await page.click('button[type="submit"]');

      await expect(page.locator('.error-message').first()).toBeVisible();
    });

    test('debe mostrar error de validacion con email invalido', async ({ page }) => {
      await page.goto('/login');
      await page.fill('#email', 'no-es-email');
      await page.click('button[type="submit"]');

      await expect(page.locator('.error-message').first()).toBeVisible();
    });

    test('debe mostrar error con password corta', async ({ page }) => {
      await page.goto('/login');
      await page.fill('#email', 'test@test.com');
      await page.fill('#password', '123');
      await page.click('button[type="submit"]');

      await expect(page.locator('.error-message').first()).toBeVisible();
    });

    test('debe navegar al registro desde login', async ({ page }) => {
      await page.goto('/login');
      await page.locator('a', { hasText: /Reg[ií]strate aqu[ií]/i }).click();
      await expect(page).toHaveURL('/register');
    });

    test('debe login exitoso y redirigir al dashboard', async ({ page }) => {
      await login(page, TEST_USER.email, TEST_USER.password);
      await expect(page).toHaveURL('/');
      await expect(page.locator('.dashboard')).toBeVisible({ timeout: 5000 });
    });
  });

  test.describe('Registro', () => {
    test('debe mostrar el formulario de registro', async ({ page }) => {
      await page.goto('/register');
      await expect(page.locator('h1')).toHaveText('Crear Cuenta');
      await expect(page.locator('#nombre')).toBeVisible();
      await expect(page.locator('#apellidos')).toBeVisible();
      await expect(page.locator('#email')).toBeVisible();
      await expect(page.locator('#telefono')).toBeVisible();
      await expect(page.locator('#nombreEmpresa')).toBeVisible();
      await expect(page.locator('#password')).toBeVisible();
      await expect(page.locator('#confirmPassword')).toBeVisible();
    });

    test('debe mostrar error si los campos requeridos estan vacios', async ({ page }) => {
      await page.goto('/register');
      await page.click('button[type="submit"]');

      const errors = page.locator('.error-message');
      await expect(errors.first()).toBeVisible();
    });

    test('debe mostrar error si las contrasenas no coinciden', async ({ page }) => {
      await page.goto('/register');
      await page.fill('#nombre', 'Test');
      await page.fill('#apellidos', 'User');
      await page.fill('#email', 'test@test.com');
      await page.fill('#telefono', '5512345678');
      await page.fill('#nombreEmpresa', 'Empresa Test');
      await page.fill('#password', 'Password123!');
      await page.fill('#confirmPassword', 'DifferentPass123!');
      await page.click('button[type="submit"]');

      await expect(page.getByText(/Las contrase[ñn]as no coinciden/i)).toBeVisible();
    });

    test('debe mostrar error con telefono invalido', async ({ page }) => {
      await page.goto('/register');
      await page.fill('#nombre', 'Test');
      await page.fill('#apellidos', 'User');
      await page.fill('#email', 'test@test.com');
      await page.fill('#telefono', '123');
      await page.click('button[type="submit"]');

      await expect(page.locator('.error-message').first()).toBeVisible();
    });

    test('debe navegar al login desde registro', async ({ page }) => {
      await page.goto('/register');
      await page.locator('a', { hasText: /Inicia sesi[oó]n aqu[ií]/i }).click();
      await expect(page).toHaveURL('/login');
    });
  });

  test.describe('Logout', () => {
    test('debe cerrar sesion y redirigir al login', async ({ page }) => {
      await login(page, TEST_USER.email, TEST_USER.password);
      await expect(page).toHaveURL('/');

      await page.locator('button', { hasText: /Cerrar Sesi[oó]n/i }).click();
      await expect(page).toHaveURL('/login');
    });
  });
});
