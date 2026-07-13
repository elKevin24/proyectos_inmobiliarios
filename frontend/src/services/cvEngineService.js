const CV_ENGINE_URL = import.meta.env.VITE_CV_ENGINE_URL || 'http://localhost:8000';

const cvEngineService = {
  extractLots: async (filePath) => {
    const formData = new FormData();
    formData.append('file_path', filePath);

    const response = await fetch(`${CV_ENGINE_URL}/api/cv/extract-lots`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const body = await response.json().catch(() => ({}));
      const msg = body.detail || body.error || `Error HTTP ${response.status}`;
      throw new Error(msg);
    }

    return response.json();
  },

  healthCheck: async () => {
    const response = await fetch(`${CV_ENGINE_URL}/health`);
    if (!response.ok) {
      throw new Error('CV Engine no disponible');
    }
    return response.json();
  },
};

export default cvEngineService;
