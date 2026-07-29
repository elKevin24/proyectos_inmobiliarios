import { create } from 'zustand';
import api from '../services/api';

const useFaseStore = create((set, get) => ({
  fases: [],
  selectedFase: null,
  isLoading: false,
  error: null,

  // Obtener fases (filtradas por proyecto si se pasa proyectoId)
  fetchFases: async (params = {}) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.get('/fases', { params });
      set({ fases: response.data, isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar fases',
        isLoading: false,
      });
      throw error;
    }
  },

  // Obtener una fase por ID
  fetchFaseById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.get(`/fases/${id}`);
      set({ selectedFase: response.data, isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar fase',
        isLoading: false,
      });
      throw error;
    }
  },

  // Crear una fase
  createFase: async (data) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.post('/fases', data);
      const newFase = response.data;
      set((state) => ({
        fases: [...state.fases, newFase],
        isLoading: false,
      }));
      return newFase;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear fase',
        isLoading: false,
      });
      throw error;
    }
  },

  // Actualizar una fase
  updateFase: async (id, data) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.put(`/fases/${id}`, data);
      const updatedFase = response.data;
      set((state) => ({
        fases: state.fases.map((f) => (f.id === id ? updatedFase : f)),
        selectedFase: state.selectedFase?.id === id ? updatedFase : state.selectedFase,
        isLoading: false,
      }));
      return updatedFase;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al actualizar fase',
        isLoading: false,
      });
      throw error;
    }
  },

  // Eliminar una fase
  deleteFase: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await api.delete(`/fases/${id}`);
      set((state) => ({
        fases: state.fases.filter((f) => f.id !== id),
        selectedFase: state.selectedFase?.id === id ? null : state.selectedFase,
        isLoading: false,
      }));
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al eliminar fase',
        isLoading: false,
      });
      throw error;
    }
  },

  clearError: () => set({ error: null }),
  clearSelectedFase: () => set({ selectedFase: null }),
}));

export default useFaseStore;
