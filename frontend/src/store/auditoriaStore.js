import { create } from 'zustand';
import api from '../services/api';

const useAuditoriaStore = create((set, get) => ({
  logsSimples: [],
  logsCriticos: [],
  historial: [],
  isLoading: false,
  error: null,

  // Obtener logs simples
  fetchLogsSimples: async (params = { page: 0, size: 50 }) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.get('/auditoria/simple', { params });
      set({ logsSimples: response.data, isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar logs simples',
        isLoading: false,
      });
      throw error;
    }
  },

  // Obtener logs críticos
  fetchLogsCriticos: async (params = { page: 0, size: 50 }) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.get('/auditoria/critica', { params });
      set({ logsCriticos: response.data, isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar logs críticos',
        isLoading: false,
      });
      throw error;
    }
  },

  // Obtener historial de un registro específico
  fetchHistorialRegistro: async (tabla, id) => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.get(`/auditoria/registro/${tabla}/${id}`);
      set({ historial: response.data, isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar historial',
        isLoading: false,
      });
      throw error;
    }
  },

  // Archivar logs
  archivarLogs: async () => {
    set({ isLoading: true, error: null });
    try {
      const response = await api.post('/auditoria/archivar');
      set({ isLoading: false });
      return response.data;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al archivar logs',
        isLoading: false,
      });
      throw error;
    }
  },

  clearError: () => set({ error: null }),
}));

export default useAuditoriaStore;
