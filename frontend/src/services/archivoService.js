import api from './api';

const archivoService = {
  // Subir un archivo (plano/mapa)
  upload: async (file, tipoArchivo = 'PLANO', proyectoId = null) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('tipoArchivo', tipoArchivo);
    if (proyectoId) {
      formData.append('proyectoId', proyectoId);
    }

    const response = await api.post('/archivos/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Descargar un archivo
  download: async (archivoId) => {
    const response = await api.get(`/archivos/${archivoId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Obtener información de un archivo
  getById: async (archivoId) => {
    const response = await api.get(`/archivos/${archivoId}`);
    return response.data;
  },

  // Eliminar un archivo
  delete: async (archivoId) => {
    const response = await api.delete(`/archivos/${archivoId}`);
    return response.data;
  },
};

export default archivoService;
