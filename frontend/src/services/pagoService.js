import api from './api';

const pagoService = {
  // Registrar un pago
  create: async (pagoData) => {
    const response = await api.post('/pagos', pagoData);
    return response.data;
  },
};

export default pagoService;
