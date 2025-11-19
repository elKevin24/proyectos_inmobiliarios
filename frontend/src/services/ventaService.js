import api from './api';

const ventaService = {
  // Obtener todas las ventas
  getAll: async (params = {}) => {
    const response = await api.get('/ventas', { params });
    return response.data;
  },

  // Obtener una venta por ID
  getById: async (id) => {
    const response = await api.get(`/ventas/${id}`);
    return response.data;
  },

  // Crear una venta
  create: async (ventaData) => {
    const response = await api.post('/ventas', ventaData);
    return response.data;
  },

  // Actualizar una venta
  update: async (id, ventaData) => {
    const response = await api.put(`/ventas/${id}`, ventaData);
    return response.data;
  },

  // Eliminar una venta
  delete: async (id) => {
    const response = await api.delete(`/ventas/${id}`);
    return response.data;
  },

  // Cancelar una venta
  cancelar: async (id, motivo) => {
    const response = await api.post(`/ventas/${id}/cancelar`, { motivo });
    return response.data;
  },
};

export default ventaService;
