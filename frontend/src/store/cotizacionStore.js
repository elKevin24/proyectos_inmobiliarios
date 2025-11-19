import { create } from 'zustand';
import cotizacionService from '../services/cotizacionService';

const useCotizacionStore = create((set, get) => ({
  cotizaciones: [],
  selectedCotizacion: null,
  isLoading: false,
  error: null,

  /**
   * Obtener todas las cotizaciones con filtros
   */
  fetchCotizaciones: async (filters = {}) => {
    set({ isLoading: true, error: null });
    try {
      const data = await cotizacionService.getAll(filters);
      set({ cotizaciones: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar cotizaciones',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Obtener cotización por ID
   */
  fetchCotizacionById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const data = await cotizacionService.getById(id);
      set({ selectedCotizacion: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar cotización',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Crear nueva cotización
   */
  createCotizacion: async (cotizacionData) => {
    set({ isLoading: true, error: null });
    try {
      const data = await cotizacionService.create(cotizacionData);
      set((state) => ({
        cotizaciones: [...state.cotizaciones, data],
        isLoading: false
      }));
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear cotización',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Eliminar cotización
   */
  deleteCotizacion: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await cotizacionService.delete(id);
      set((state) => ({
        cotizaciones: state.cotizaciones.filter(c => c.id !== id),
        isLoading: false
      }));
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al eliminar cotización',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Limpiar cotización seleccionada
   */
  clearSelectedCotizacion: () => {
    set({ selectedCotizacion: null });
  },

  /**
   * Limpiar error
   */
  clearError: () => {
    set({ error: null });
  }
}));

export default useCotizacionStore;
