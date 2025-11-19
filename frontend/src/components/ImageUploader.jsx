import { useState, useRef } from 'react';
import { FaUpload, FaTrash, FaImage } from 'react-icons/fa';
import archivoService from '../services/archivoService';
import '../styles/ImageUploader.css';

function ImageUploader({ onUploadSuccess, proyectoId, existingImage }) {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(existingImage || null);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const handleFileSelect = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validar tipo de archivo
    if (!file.type.startsWith('image/')) {
      setError('Por favor selecciona una imagen válida');
      return;
    }

    // Validar tamaño (máximo 10MB)
    if (file.size > 10 * 1024 * 1024) {
      setError('La imagen no debe superar los 10MB');
      return;
    }

    // Crear preview local
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreview(reader.result);
    };
    reader.readAsDataURL(file);

    // Subir archivo
    try {
      setUploading(true);
      setError(null);

      const response = await archivoService.upload(file, 'PLANO', proyectoId);

      if (onUploadSuccess) {
        onUploadSuccess(response);
      }
    } catch (error) {
      setError(error.response?.data?.message || 'Error al subir el archivo');
      setPreview(null);
    } finally {
      setUploading(false);
    }
  };

  const handleRemove = () => {
    setPreview(null);
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
    if (onUploadSuccess) {
      onUploadSuccess(null);
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="image-uploader">
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileSelect}
        style={{ display: 'none' }}
      />

      {!preview ? (
        <div className="upload-area" onClick={handleClick}>
          <FaImage size={48} />
          <p>Click para subir un plano o mapa</p>
          <span className="upload-hint">PNG, JPG o GIF - Máximo 10MB</span>
        </div>
      ) : (
        <div className="preview-area">
          <img src={preview} alt="Preview" className="preview-image" />
          <div className="preview-actions">
            <button
              type="button"
              onClick={handleClick}
              className="btn btn-secondary"
              disabled={uploading}
            >
              <FaUpload /> Cambiar
            </button>
            <button
              type="button"
              onClick={handleRemove}
              className="btn btn-danger"
              disabled={uploading}
            >
              <FaTrash /> Eliminar
            </button>
          </div>
        </div>
      )}

      {uploading && (
        <div className="upload-progress">
          <div className="spinner"></div>
          <p>Subiendo archivo...</p>
        </div>
      )}

      {error && (
        <div className="upload-error">
          {error}
        </div>
      )}
    </div>
  );
}

export default ImageUploader;
