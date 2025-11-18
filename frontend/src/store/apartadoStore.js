import { create } from 'zustand';
import apartadoService from '../services/apartadoService';

const useApartadoStore = create((set, get) => ({
  apartados: [],
  selectedApartado: null,
  isLoading: false,
  error: null,

  /**
   * Obtener todos los apartados con filtros
   */
  fetchApartados: async (filters = {}) => {
    set({ isLoading: true, error: null });
    try {
      const data = await apartadoService.getAll(filters);
      set({ apartados: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar apartados',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Obtener apartado por ID
   */
  fetchApartadoById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const data = await apartadoService.getById(id);
      set({ selectedApartado: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar apartado',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Crear nuevo apartado
   */
  createApartado: async (apartadoData) => {
    set({ isLoading: true, error: null });
    try {
      const data = await apartadoService.create(apartadoData);
      set((state) => ({
        apartados: [...state.apartados, data],
        isLoading: false
      }));
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear apartado',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Cancelar apartado
   */
  cancelarApartado: async (id, motivo) => {
    set({ isLoading: true, error: null });
    try {
      const data = await apartadoService.cancelar(id, motivo);
      set((state) => ({
        apartados: state.apartados.map(a => a.id === id ? data : a),
        selectedApartado: data,
        isLoading: false
      }));
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cancelar apartado',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Eliminar apartado
   */
  deleteApartado: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await apartadoService.delete(id);
      set((state) => ({
        apartados: state.apartados.filter(a => a.id !== id),
        isLoading: false
      }));
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al eliminar apartado',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Convertir apartado a venta
   */
  convertirAVenta: async (apartadoId, ventaData) => {
    set({ isLoading: true, error: null });
    try {
      const data = await apartadoService.convertirAVenta(apartadoId, ventaData);
      // Actualizar estado del apartado a CONVERTIDO_A_VENTA
      set((state) => ({
        apartados: state.apartados.map(a =>
          a.id === apartadoId
            ? { ...a, estado: 'CONVERTIDO_A_VENTA' }
            : a
        ),
        isLoading: false
      }));
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al convertir apartado a venta',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Limpiar apartado seleccionado
   */
  clearSelectedApartado: () => {
    set({ selectedApartado: null });
  },

  /**
   * Limpiar error
   */
  clearError: () => {
    set({ error: null });
  }
}));

export default useApartadoStore;
