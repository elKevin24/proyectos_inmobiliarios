import api from './api';

const proyectoService = {
  // Obtener todos los proyectos
  getAll: async (params = {}) => {
    const response = await api.get('/proyectos', { params });
    return response.data;
  },

  // Obtener un proyecto por ID
  getById: async (id) => {
    const response = await api.get(`/proyectos/${id}`);
    return response.data;
  },

  // Crear un proyecto
  create: async (proyectoData) => {
    const response = await api.post('/proyectos', proyectoData);
    return response.data;
  },

  // Actualizar un proyecto
  update: async (id, proyectoData) => {
    const response = await api.put(`/proyectos/${id}`, proyectoData);
    return response.data;
  },

  // Eliminar un proyecto
  delete: async (id) => {
    const response = await api.delete(`/proyectos/${id}`);
    return response.data;
  },

  // Obtener estadísticas de un proyecto
  getEstadisticas: async (id) => {
    const response = await api.get(`/proyectos/${id}/estadisticas`);
    return response.data;
  },
};

export default proyectoService;
