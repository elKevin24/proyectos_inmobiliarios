import { create } from 'zustand';
import authService from '../services/authService';
import { jwtDecode } from 'jwt-decode';

/**
 * Helper sincrónico para inicializar el estado de autenticación desde localStorage
 */
export const getInitialAuthState = () => {
  try {
    const token = typeof window !== 'undefined' ? localStorage.getItem('access_token') : null;
    if (token) {
      const decoded = jwtDecode(token);
      const currentTime = Date.now() / 1000;

      if (decoded && decoded.exp > currentTime) {
        return {
          isAuthenticated: true,
          user: {
            email: decoded.sub,
            tenantId: decoded.tenant_id,
            roles: decoded.roles || [],
          },
        };
      } else {
        // Token expirado
        authService.logout();
      }
    }
  } catch (error) {
    console.error('Error decoding initial auth token:', error);
    authService.logout();
  }

  return {
    isAuthenticated: false,
    user: null,
  };
};

const initialAuth = getInitialAuthState();

const useAuthStore = create((set) => ({
  user: initialAuth.user,
  isAuthenticated: initialAuth.isAuthenticated,
  isLoading: false,
  error: null,

  // Inicializar el estado desde localStorage
  initialize: () => {
    const state = getInitialAuthState();
    set(state);
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
          nombre: response.user_info?.nombre || '',
          tenantNombre: response.user_info?.tenantNombre || '',
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
