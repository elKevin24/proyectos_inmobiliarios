import api from './api';

const planPagoService = {
  /**
   * Obtener todos los planes de pago
   */
  getAll: async () => {
    const response = await api.get('/planes-pago');
    return response.data;
  },

  /**
   * Obtener plan de pago por ID
   * @param {number} id - ID del plan
   */
  getById: async (id) => {
    const response = await api.get(`/planes-pago/${id}`);
    return response.data;
  },

  /**
   * Obtener tabla de amortización de un plan
   * @param {number} id - ID del plan
   */
  getAmortizaciones: async (id) => {
    const response = await api.get(`/planes-pago/${id}/amortizaciones`);
    return response.data;
  },

  /**
   * Obtener estado de cuenta de un plan
   * @param {number} id - ID del plan
   */
  getEstadoCuenta: async (id) => {
    const response = await api.get(`/planes-pago/${id}/estado-cuenta`);
    return response.data;
  },

  /**
   * Crear nuevo plan de pago
   * @param {Object} planData - Datos del plan
   */
  create: async (planData) => {
    const response = await api.post('/planes-pago', planData);
    return response.data;
  },

  /**
   * Actualizar plan de pago
   * @param {number} id - ID del plan
   * @param {Object} planData - Datos actualizados
   */
  update: async (id, planData) => {
    const response = await api.put(`/planes-pago/${id}`, planData);
    return response.data;
  },

  /**
   * Eliminar plan de pago
   * @param {number} id - ID del plan
   */
  delete: async (id) => {
    const response = await api.delete(`/planes-pago/${id}`);
    return response.data;
  }
};

export default planPagoService;
