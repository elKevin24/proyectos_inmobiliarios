import api from './api';

const cotizacionService = {
  /**
   * Obtener todas las cotizaciones con filtros opcionales
   * @param {Object} params - Parámetros de filtro { vigentes, cliente }
   */
  getAll: async (params = {}) => {
    const response = await api.get('/cotizaciones', { params });
    return response.data;
  },

  /**
   * Obtener cotización por ID
   * @param {number} id - ID de la cotización
   */
  getById: async (id) => {
    const response = await api.get(`/cotizaciones/${id}`);
    return response.data;
  },

  /**
   * Crear nueva cotización
   * @param {Object} cotizacionData - Datos de la cotización
   */
  create: async (cotizacionData) => {
    const response = await api.post('/cotizaciones', cotizacionData);
    return response.data;
  },

  /**
   * Eliminar cotización
   * @param {number} id - ID de la cotización
   */
  delete: async (id) => {
    const response = await api.delete(`/cotizaciones/${id}`);
    return response.data;
  }
};

export default cotizacionService;
