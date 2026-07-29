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

  // Actualizar una venta (alias para compatibilidad con stores)
  update: async (id, ventaData) => {
    const response = await api.patch(`/ventas/${id}/estado`, null, {
      params: { estado: ventaData.estado || 'PENDIENTE' }
    });
    return response.data;
  },

  // Cambiar estado de una venta (PATCH /{id}/estado)
  updateEstado: async (id, estado) => {
    const response = await api.patch(`/ventas/${id}/estado`, null, {
      params: { estado }
    });
    return response.data;
  },

  // Eliminar una venta
  delete: async (id) => {
    const response = await api.delete(`/ventas/${id}`);
    return response.data;
  },

  // Cancelar una venta
  cancelar: async (id) => {
    const response = await api.patch(`/ventas/${id}/estado`, null, {
      params: { estado: 'CANCELADA' }
    });
    return response.data;
  },
};

export default ventaService;
