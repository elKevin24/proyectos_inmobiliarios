import api from './api';

const pagoService = {
  // Obtener todos los pagos de una venta
  getByVenta: async (ventaId) => {
    const response = await api.get(`/pagos/venta/${ventaId}`);
    return response.data;
  },

  // Obtener un pago por ID
  getById: async (id) => {
    const response = await api.get(`/pagos/${id}`);
    return response.data;
  },

  // Registrar un pago
  create: async (pagoData) => {
    const response = await api.post('/pagos', pagoData);
    return response.data;
  },

  // Actualizar un pago
  update: async (id, pagoData) => {
    const response = await api.put(`/pagos/${id}`, pagoData);
    return response.data;
  },

  // Eliminar un pago
  delete: async (id) => {
    const response = await api.delete(`/pagos/${id}`);
    return response.data;
  },
};

export default pagoService;
