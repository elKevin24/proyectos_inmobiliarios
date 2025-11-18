import { create } from 'zustand';
import ventaService from '../services/ventaService';

const useVentaStore = create((set, get) => ({
  ventas: [],
  selectedVenta: null,
  isLoading: false,
  error: null,

  // Obtener todas las ventas
  fetchVentas: async (params = {}) => {
    set({ isLoading: true, error: null });
    try {
      const ventas = await ventaService.getAll(params);
      set({ ventas, isLoading: false });
      return ventas;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar ventas',
        isLoading: false
      });
      throw error;
    }
  },

  // Obtener una venta por ID
  fetchVentaById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const venta = await ventaService.getById(id);
      set({ selectedVenta: venta, isLoading: false });
      return venta;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar venta',
        isLoading: false
      });
      throw error;
    }
  },

  // Crear una venta
  createVenta: async (ventaData) => {
    set({ isLoading: true, error: null });
    try {
      const newVenta = await ventaService.create(ventaData);
      set((state) => ({
        ventas: [...state.ventas, newVenta],
        isLoading: false
      }));
      return newVenta;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear venta',
        isLoading: false
      });
      throw error;
    }
  },

  // Actualizar una venta
  updateVenta: async (id, ventaData) => {
    set({ isLoading: true, error: null });
    try {
      const updatedVenta = await ventaService.update(id, ventaData);
      set((state) => ({
        ventas: state.ventas.map(v => v.id === id ? updatedVenta : v),
        selectedVenta: state.selectedVenta?.id === id ? updatedVenta : state.selectedVenta,
        isLoading: false
      }));
      return updatedVenta;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al actualizar venta',
        isLoading: false
      });
      throw error;
    }
  },

  // Cancelar una venta
  cancelarVenta: async (id, motivo) => {
    set({ isLoading: true, error: null });
    try {
      const canceledVenta = await ventaService.cancelar(id, motivo);
      set((state) => ({
        ventas: state.ventas.map(v => v.id === id ? canceledVenta : v),
        selectedVenta: state.selectedVenta?.id === id ? canceledVenta : state.selectedVenta,
        isLoading: false
      }));
      return canceledVenta;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cancelar venta',
        isLoading: false
      });
      throw error;
    }
  },

  // Clear error
  clearError: () => set({ error: null }),

  // Clear selected venta
  clearSelectedVenta: () => set({ selectedVenta: null }),
}));

export default useVentaStore;
