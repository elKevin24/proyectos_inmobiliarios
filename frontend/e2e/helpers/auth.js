import { expect } from '@playwright/test';

const API_URL = process.env.API_URL || 'http://localhost:8080/api/v1';

export async function login(page, email, password) {
  await page.goto('/login');
  await page.waitForLoadState('networkidle');

  await page.fill('#email', email);
  await page.fill('#password', password);
  await page.click('button[type="submit"]');

  await page.waitForURL('/', { timeout: 10_000 });
}

export async function registerUser(page, userData) {
  await page.goto('/register');
  await page.waitForLoadState('networkidle');

  await page.fill('#nombre', userData.nombre);
  await page.fill('#apellidos', userData.apellidos);
  await page.fill('#email', userData.email);
  await page.fill('#telefono', userData.telefono);
  await page.fill('#nombreEmpresa', userData.nombreEmpresa);
  await page.fill('#password', userData.password);
  await page.fill('#confirmPassword', userData.password);

  await page.click('button[type="submit"]');
}

export async function loginViaAPI(request, email, password) {
  const response = await request.post(`${API_URL}/auth/login`, {
    data: { email, password },
  });
  const body = await response.json();
  return body.access_token;
}

export async function setAuthToken(page, token) {
  await page.evaluate((t) => {
    localStorage.setItem('access_token', t);
  }, token);
}

export async function createViaAPI(request, endpoint, data, token) {
  const response = await request.post(`${API_URL}${endpoint}`, {
    data,
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });
  return response.json();
}

export async function deleteViaAPI(request, endpoint, token) {
  const response = await request.delete(`${API_URL}${endpoint}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!response.ok()) {
    throw new Error(`Error deleting ${endpoint}: ${response.status()} ${response.statusText()}`);
  }
  return response;
}

export async function generateUniqueEmail() {
  const timestamp = Date.now();
  return `test${timestamp}@example.com`;
}
