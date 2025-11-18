import api from './api';

const reporteService = {
  /**
   * Obtener estadísticas del dashboard
   */
  getDashboard: async () => {
    const response = await api.get('/reportes/dashboard');
    return response.data;
  },

  /**
   * Obtener estadísticas de todos los proyectos
   */
  getProyectosEstadisticas: async () => {
    const response = await api.get('/reportes/proyectos');
    return response.data;
  },

  /**
   * Obtener estadísticas de un proyecto específico
   * @param {number} id - ID del proyecto
   */
  getProyectoEstadisticas: async (id) => {
    const response = await api.get(`/reportes/proyectos/${id}`);
    return response.data;
  }
};

export default reporteService;
