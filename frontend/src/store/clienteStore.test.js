import assert from 'node:assert/strict';
import test, { describe, beforeEach, afterEach, mock } from 'node:test';
import api from '../services/api.js';
import useClienteStore from './clienteStore.js';

describe('Zustand - clienteStore', () => {
  beforeEach(() => {
    // Reset Zustand store state before each test
    useClienteStore.setState({
      clientes: [],
      selectedCliente: null,
      isLoading: false,
      error: null,
    });
  });

  afterEach(() => {
    // Restore mocks after each test
    mock.restoreAll();
  });

  test('fetchClientes should populate clientes array and handle loading state', async () => {
    const mockData = [{ id: 1, nombre: 'Juan' }, { id: 2, nombre: 'Maria' }];
    mock.method(api, 'get', async () => ({ data: mockData }));

    const store = useClienteStore.getState();
    assert.equal(store.clientes.length, 0);

    const promise = store.fetchClientes();
    assert.equal(useClienteStore.getState().isLoading, true);

    const result = await promise;
    assert.equal(result, mockData);
    assert.deepEqual(useClienteStore.getState().clientes, mockData);
    assert.equal(useClienteStore.getState().isLoading, false);
    assert.equal(useClienteStore.getState().error, null);
  });

  test('fetchClientes should handle API errors and set error state', async () => {
    mock.method(api, 'get', async () => {
      const error = new Error('Network Error');
      error.response = { data: { message: 'Fallo al cargar' } };
      throw error;
    });

    const store = useClienteStore.getState();
    
    try {
      await store.fetchClientes();
      assert.fail('Debería haber lanzado un error');
    } catch (e) {
      const state = useClienteStore.getState();
      assert.equal(state.error, 'Fallo al cargar');
      assert.equal(state.isLoading, false);
      assert.equal(state.clientes.length, 0);
    }
  });

  test('createCliente should add new cliente to the state array', async () => {
    const mockNewCliente = { id: 3, nombre: 'Pedro' };
    useClienteStore.setState({ clientes: [{ id: 1, nombre: 'Juan' }] });

    mock.method(api, 'post', async () => ({ data: mockNewCliente }));

    const store = useClienteStore.getState();
    await store.createCliente({ nombre: 'Pedro' });

    const state = useClienteStore.getState();
    assert.equal(state.clientes.length, 2);
    assert.deepEqual(state.clientes[1], mockNewCliente);
  });
});
