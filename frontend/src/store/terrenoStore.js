import { create } from 'zustand';
import terrenoService from '../services/terrenoService';

const useTerrenoStore = create((set, get) => ({
  terrenos: [],
  selectedTerreno: null,
  isLoading: false,
  error: null,
  filters: {
    proyectoId: null,
    estado: null,
  },

  // Obtener todos los terrenos
  fetchTerrenos: async (params = {}) => {
    set({ isLoading: true, error: null });
    try {
      const terrenos = await terrenoService.getAll(params);
      set({ terrenos, isLoading: false });
      return terrenos;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar terrenos',
        isLoading: false
      });
      throw error;
    }
  },

  // Obtener un terreno por ID
  fetchTerrenoById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const terreno = await terrenoService.getById(id);
      set({ selectedTerreno: terreno, isLoading: false });
      return terreno;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar terreno',
        isLoading: false
      });
      throw error;
    }
  },

  // Crear un terreno
  createTerreno: async (terrenoData) => {
    set({ isLoading: true, error: null });
    try {
      const newTerreno = await terrenoService.create(terrenoData);
      set((state) => ({
        terrenos: [...state.terrenos, newTerreno],
        isLoading: false
      }));
      return newTerreno;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear terreno',
        isLoading: false
      });
      throw error;
    }
  },

  // Actualizar un terreno
  updateTerreno: async (id, terrenoData) => {
    set({ isLoading: true, error: null });
    try {
      const updatedTerreno = await terrenoService.update(id, terrenoData);
      set((state) => ({
        terrenos: state.terrenos.map(t => t.id === id ? updatedTerreno : t),
        selectedTerreno: state.selectedTerreno?.id === id ? updatedTerreno : state.selectedTerreno,
        isLoading: false
      }));
      return updatedTerreno;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al actualizar terreno',
        isLoading: false
      });
      throw error;
    }
  },

  // Eliminar un terreno
  deleteTerreno: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await terrenoService.delete(id);
      set((state) => ({
        terrenos: state.terrenos.filter(t => t.id !== id),
        selectedTerreno: state.selectedTerreno?.id === id ? null : state.selectedTerreno,
        isLoading: false
      }));
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al eliminar terreno',
        isLoading: false
      });
      throw error;
    }
  },

  // Filtrar terrenos
  setFilters: (filters) => {
    set({ filters });
  },

  // Clear error
  clearError: () => set({ error: null }),

  // Clear selected terreno
  clearSelectedTerreno: () => set({ selectedTerreno: null }),
}));

export default useTerrenoStore;
