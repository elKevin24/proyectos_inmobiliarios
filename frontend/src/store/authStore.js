import { create } from 'zustand';
import authService from '../services/authService';
import { jwtDecode } from 'jwt-decode';

const useAuthStore = create((set, get) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  // Inicializar el estado desde localStorage
  initialize: () => {
    const token = localStorage.getItem('access_token');
    if (token) {
      try {
        const decoded = jwtDecode(token);
        const currentTime = Date.now() / 1000;

        if (decoded.exp > currentTime) {
          set({
            isAuthenticated: true,
            user: {
              email: decoded.sub,
              tenantId: decoded.tenant_id,
              roles: decoded.roles || [],
            }
          });
        } else {
          // Token expirado
          authService.logout();
          set({ isAuthenticated: false, user: null });
        }
      } catch (error) {
        console.error('Error decoding token:', error);
        authService.logout();
        set({ isAuthenticated: false, user: null });
      }
    }
  },

  // Login
  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.login(email, password);
      const decoded = jwtDecode(response.access_token);

      set({
        isAuthenticated: true,
        user: {
          email: decoded.sub,
          tenantId: decoded.tenant_id,
          roles: decoded.roles || [],
        },
        isLoading: false,
        error: null,
      });

      return response;
    } catch (error) {
      set({
        isLoading: false,
        error: error.response?.data?.message || 'Error al iniciar sesión',
        isAuthenticated: false,
        user: null,
      });
      throw error;
    }
  },

  // Register
  register: async (userData) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authService.register(userData);
      set({ isLoading: false, error: null });
      return response;
    } catch (error) {
      set({
        isLoading: false,
        error: error.response?.data?.message || 'Error al registrarse'
      });
      throw error;
    }
  },

  // Logout
  logout: () => {
    authService.logout();
    set({
      isAuthenticated: false,
      user: null,
      error: null,
    });
  },

  // Clear error
  clearError: () => set({ error: null }),
}));

export default useAuthStore;
