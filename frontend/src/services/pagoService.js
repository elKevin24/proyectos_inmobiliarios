import api from './api';

const pagoService = {
  // Listar todos los pagos
  listarPagos: async () => {
    const response = await api.get('/pagos');
    return response.data;
  },

  // Registrar un pago
  create: async (pagoData) => {
    const response = await api.post('/pagos', pagoData);
    return response.data;
  },

  // Obtener pago por ID
  getById: async (id) => {
    const response = await api.get(`/pagos/${id}`);
    return response.data;
  },

  // Obtener pagos por venta
  getByVentaId: async (ventaId) => {
    const response = await api.get(`/pagos/venta/${ventaId}`);
    return response.data;
  },

  // Eliminar pago
  delete: async (id) => {
    const response = await api.delete(`/pagos/${id}`);
    return response.data;
  },
};

export default pagoService;
