import api from './api';

const archivoService = {
  upload: async (file, tipoArchivo = 'PLANO_PROYECTO', proyectoId = null, terrenoId = null, descripcion = null) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('tipo', tipoArchivo);
    if (proyectoId) {
      formData.append('proyectoId', proyectoId);
    }
    if (terrenoId) {
      formData.append('terrenoId', terrenoId);
    }
    if (descripcion) {
      formData.append('descripcion', descripcion);
    }

    const response = await api.post('/archivos/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  download: async (archivoId) => {
    const response = await api.get(`/archivos/${archivoId}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  getById: async (archivoId) => {
    const response = await api.get(`/archivos/${archivoId}`);
    return response.data;
  },

  list: async (params = {}) => {
    const response = await api.get('/archivos', { params });
    return response.data;
  },

  getGaleria: async (proyectoId) => {
    const response = await api.get(`/archivos/galeria/${proyectoId}`);
    return response.data;
  },

  getVersiones: async (proyectoId, nombreOriginal) => {
    const response = await api.get(`/archivos/versiones/${proyectoId}`, {
      params: { nombreOriginal },
    });
    return response.data;
  },

  delete: async (archivoId) => {
    const response = await api.delete(`/archivos/${archivoId}`);
    return response.data;
  },

  getDownloadUrl: (archivoId) => {
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
    return `${baseURL}/archivos/${archivoId}/download`;
  },
};

export default archivoService;
