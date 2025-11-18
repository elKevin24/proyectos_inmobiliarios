import { create } from 'zustand';
import proyectoService from '../services/proyectoService';

const useProyectoStore = create((set, get) => ({
  proyectos: [],
  selectedProyecto: null,
  isLoading: false,
  error: null,

  // Obtener todos los proyectos
  fetchProyectos: async (params = {}) => {
    set({ isLoading: true, error: null });
    try {
      const proyectos = await proyectoService.getAll(params);
      set({ proyectos, isLoading: false });
      return proyectos;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar proyectos',
        isLoading: false
      });
      throw error;
    }
  },

  // Obtener un proyecto por ID
  fetchProyectoById: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const proyecto = await proyectoService.getById(id);
      set({ selectedProyecto: proyecto, isLoading: false });
      return proyecto;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al cargar proyecto',
        isLoading: false
      });
      throw error;
    }
  },

  // Crear un proyecto
  createProyecto: async (proyectoData) => {
    set({ isLoading: true, error: null });
    try {
      const newProyecto = await proyectoService.create(proyectoData);
      set((state) => ({
        proyectos: [...state.proyectos, newProyecto],
        isLoading: false
      }));
      return newProyecto;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al crear proyecto',
        isLoading: false
      });
      throw error;
    }
  },

  // Actualizar un proyecto
  updateProyecto: async (id, proyectoData) => {
    set({ isLoading: true, error: null });
    try {
      const updatedProyecto = await proyectoService.update(id, proyectoData);
      set((state) => ({
        proyectos: state.proyectos.map(p => p.id === id ? updatedProyecto : p),
        selectedProyecto: state.selectedProyecto?.id === id ? updatedProyecto : state.selectedProyecto,
        isLoading: false
      }));
      return updatedProyecto;
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al actualizar proyecto',
        isLoading: false
      });
      throw error;
    }
  },

  // Eliminar un proyecto
  deleteProyecto: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await proyectoService.delete(id);
      set((state) => ({
        proyectos: state.proyectos.filter(p => p.id !== id),
        selectedProyecto: state.selectedProyecto?.id === id ? null : state.selectedProyecto,
        isLoading: false
      }));
    } catch (error) {
      set({
        error: error.response?.data?.message || 'Error al eliminar proyecto',
        isLoading: false
      });
      throw error;
    }
  },

  // Clear error
  clearError: () => set({ error: null }),

  // Clear selected proyecto
  clearSelectedProyecto: () => set({ selectedProyecto: null }),
}));

export default useProyectoStore;
