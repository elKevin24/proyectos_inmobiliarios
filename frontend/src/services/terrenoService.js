import api from './api';

const terrenoService = {
  // Obtener todos los terrenos
  getAll: async (params = {}) => {
    const response = await api.get('/terrenos', { params });
    return response.data;
  },

  // Obtener un terreno por ID
  getById: async (id) => {
    const response = await api.get(`/terrenos/${id}`);
    return response.data;
  },

  // Crear un terreno
  create: async (terrenoData) => {
    const response = await api.post('/terrenos', terrenoData);
    return response.data;
  },

  // Actualizar un terreno
  update: async (id, terrenoData) => {
    const response = await api.put(`/terrenos/${id}`, terrenoData);
    return response.data;
  },

  // Eliminar un terreno
  delete: async (id) => {
    const response = await api.delete(`/terrenos/${id}`);
    return response.data;
  },

  // Filtrar terrenos por proyecto
  getByProyecto: async (proyectoId) => {
    const response = await api.get(`/terrenos/proyecto/${proyectoId}`);
    return response.data;
  },

  // Filtrar terrenos por estado
  getByEstado: async (estado) => {
    const response = await api.get('/terrenos', { params: { estado } });
    return response.data;
  },
};

export default terrenoService;
