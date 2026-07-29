import assert from 'node:assert/strict';
import test, { describe, beforeEach } from 'node:test';
import api from './api.js';

// Setup global mocks for Node environment
global.localStorage = {
  store: {},
  getItem(key) { return this.store[key] || null; },
  setItem(key, value) { this.store[key] = String(value); },
  removeItem(key) { delete this.store[key]; },
  clear() { this.store = {}; }
};
global.window = { location: { href: '' } };

describe('API Axios Interceptors', () => {
  beforeEach(() => {
    global.localStorage.clear();
    global.window.location.href = '';
  });

  test('request interceptor adds Authorization header if token exists', async () => {
    global.localStorage.setItem('access_token', 'mock_token');
    
    const config = { headers: {} };
    // Simulate interceptor execution
    const requestHandlers = api.interceptors.request.handlers;
    const fulfilledHandler = requestHandlers[0].fulfilled;
    const result = await fulfilledHandler(config);
    
    assert.equal(result.headers.Authorization, 'Bearer mock_token');
  });

  test('request interceptor does not add token if not present', async () => {
    const config = { headers: {} };
    const fulfilledHandler = api.interceptors.request.handlers[0].fulfilled;
    const result = await fulfilledHandler(config);
    
    assert.equal(result.headers.Authorization, undefined);
  });

  test('response interceptor on 401 error clears storage and redirects to login', async () => {
    global.localStorage.setItem('access_token', 'expired_token');
    global.localStorage.setItem('refresh_token', 'expired_refresh');
    
    const errorObj = { response: { status: 401 } };
    const rejectedHandler = api.interceptors.response.handlers[0].rejected;
    
    try {
      await rejectedHandler(errorObj);
      assert.fail('Should throw the error');
    } catch (e) {
      assert.equal(global.localStorage.getItem('access_token'), null);
      assert.equal(global.localStorage.getItem('refresh_token'), null);
      assert.equal(global.window.location.href, '/login');
      assert.equal(e, errorObj);
    }
  });

  test('response interceptor on non-401 error does not redirect', async () => {
    global.localStorage.setItem('access_token', 'valid_token');
    
    const errorObj = { response: { status: 500 } };
    const rejectedHandler = api.interceptors.response.handlers[0].rejected;
    
    try {
      await rejectedHandler(errorObj);
      assert.fail('Should throw the error');
    } catch (e) {
      assert.equal(global.localStorage.getItem('access_token'), 'valid_token');
      assert.equal(global.window.location.href, '');
    }
  });
});
