import api from './api';

const apartadoService = {
  /**
   * Obtener todos los apartados con filtros opcionales
   * @param {Object} params - Parámetros de filtro { vigentes, vencidos }
   */
  getAll: async (params = {}) => {
    const response = await api.get('/apartados', { params });
    return response.data;
  },

  /**
   * Obtener apartado por ID
   * @param {number} id - ID del apartado
   */
  getById: async (id) => {
    const response = await api.get(`/apartados/${id}`);
    return response.data;
  },

  /**
   * Crear nuevo apartado
   * @param {Object} apartadoData - Datos del apartado
   */
  create: async (apartadoData) => {
    const response = await api.post('/apartados', apartadoData);
    return response.data;
  },

  /**
   * Cancelar apartado
   * @param {number} id - ID del apartado
   * @param {string} motivo - Motivo de cancelación
   */
  cancelar: async (id, motivo) => {
    const response = await api.put(`/apartados/${id}/cancelar`, { motivo });
    return response.data;
  },

  /**
   * Eliminar apartado
   * @param {number} id - ID del apartado
   */
  delete: async (id) => {
    const response = await api.delete(`/apartados/${id}`);
    return response.data;
  },

  /**
   * Convertir apartado a venta
   * @param {number} apartadoId - ID del apartado
   * @param {Object} ventaData - Datos adicionales de la venta
   */
  convertirAVenta: async (apartadoId, ventaData) => {
    // Esta operación se hace creando una venta con el apartadoId
    const response = await api.post('/ventas', {
      ...ventaData,
      apartadoId: apartadoId
    });
    return response.data;
  }
};

export default apartadoService;
