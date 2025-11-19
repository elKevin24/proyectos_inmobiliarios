import { create } from 'zustand';
import planPagoService from '../services/planPagoService';

const usePlanPagoStore = create((set, get) => ({
  planesPago: [],
  selectedPlan: null,
  amortizaciones: [],
  estadoCuenta: null,
  isLoading: false,
  error: null,

  /**
   * Obtener todos los planes de pago
   */
  fetchPlanesPago: async () => {
    set({ isLoading: true, error: null });
    try {
      const data = await planPagoService.getAll();
      set({ planesPago: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar planes de pago',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Obtener plan de pago por ID
   */
  fetchPlanPagoById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const data = await planPagoService.getById(id);
      set({ selectedPlan: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar plan de pago',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Obtener tabla de amortización
   */
  fetchAmortizaciones: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const data = await planPagoService.getAmortizaciones(id);
      set({ amortizaciones: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar amortizaciones',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Obtener estado de cuenta
   */
  fetchEstadoCuenta: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const data = await planPagoService.getEstadoCuenta(id);
      set({ estadoCuenta: data, isLoading: false });
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar estado de cuenta',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Crear nuevo plan de pago
   */
  createPlanPago: async (planData) => {
    set({ isLoading: true, error: null });
    try {
      const data = await planPagoService.create(planData);
      set((state) => ({
        planesPago: [...state.planesPago, data],
        isLoading: false
      }));
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear plan de pago',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Actualizar plan de pago
   */
  updatePlanPago: async (id, planData) => {
    set({ isLoading: true, error: null });
    try {
      const data = await planPagoService.update(id, planData);
      set((state) => ({
        planesPago: state.planesPago.map(p => p.id === id ? data : p),
        selectedPlan: data,
        isLoading: false
      }));
      return data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al actualizar plan de pago',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Eliminar plan de pago
   */
  deletePlanPago: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await planPagoService.delete(id);
      set((state) => ({
        planesPago: state.planesPago.filter(p => p.id !== id),
        isLoading: false
      }));
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al eliminar plan de pago',
        isLoading: false
      });
      throw error;
    }
  },

  /**
   * Limpiar plan seleccionado
   */
  clearSelectedPlan: () => {
    set({ selectedPlan: null, amortizaciones: [], estadoCuenta: null });
  },

  /**
   * Limpiar error
   */
  clearError: () => {
    set({ error: null });
  }
}));

export default usePlanPagoStore;
