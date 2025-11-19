import { create } from 'zustand';
import api from '../services/api';

const clienteStore = create((set, get) => ({
  clientes: [],
  selectedCliente: null,
  isLoading: false,
  error: null,

  // Obtener todos los clientes
  fetchClientes: async (params = {}) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.get('/clientes', { params });
      set({ clientes: response.data, isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar clientes',
        isLoading: false
      });
      throw error;
    }
  },

  // Obtener un cliente por ID
  fetchClienteById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.get(`/clientes/${id}`);
      set({ selectedCliente: response.data, isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar cliente',
        isLoading: false
      });
      throw error;
    }
  },

  // Crear un cliente
  createCliente: async (clienteData) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.post('/clientes', clienteData);
      set((state) => ({
        clientes: [...state.clientes, response.data],
        isLoading: false
      }));
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear cliente',
        isLoading: false
      });
      throw error;
    }
  },

  // Actualizar un cliente
  updateCliente: async (id, clienteData) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.put(`/clientes/${id}`, clienteData);
      set((state) => ({
        clientes: state.clientes.map(c => c.id === id ? response.data : c),
        selectedCliente: state.selectedCliente?.id === id ? response.data : state.selectedCliente,
        isLoading: false
      }));
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al actualizar cliente',
        isLoading: false
      });
      throw error;
    }
  },

  // Eliminar un cliente
  deleteCliente: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await api.delete(`/clientes/${id}`);
      set((state) => ({
        clientes: state.clientes.filter(c => c.id !== id),
        selectedCliente: state.selectedCliente?.id === id ? null : state.selectedCliente,
        isLoading: false
      }));
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al eliminar cliente',
        isLoading: false
      });
      throw error;
    }
  },

  // Clear error
  clearError: () => set({ error: null }),

  // Clear selected cliente
  clearSelectedCliente: () => set({ selectedCliente: null }),
}));

export default clienteStore;
