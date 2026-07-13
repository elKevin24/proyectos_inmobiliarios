import { useState, useRef, useCallback } from 'react';
import { FaUpload, FaTrash, FaImage, FaFileAlt } from 'react-icons/fa';
import archivoService from '../services/archivoService';
import '../styles/ImageUploader.css';

const ALLOWED_TYPES = {
  image: ['image/png', 'image/jpeg', 'image/jpg', 'image/gif', 'image/webp'],
  document: ['application/pdf', 'image/vnd.dwg', 'image/vnd.dxf', 'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
};

const ALL_ALLOWED = [...ALLOWED_TYPES.image, ...ALLOWED_TYPES.document];
const MAX_SIZE = 10 * 1024 * 1024;

function ImageUploader({ onUploadSuccess, onFileSelect, proyectoId, terrenoId, tipoArchivo = 'PLANO_PROYECTO', existingImage, allowDocuments = false }) {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(existingImage || null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const fileInputRef = useRef(null);

  const validateFile = useCallback((file) => {
    const allowed = allowDocuments ? ALL_ALLOWED : ALLOWED_TYPES.image;
    if (!allowed.includes(file.type)) {
      return allowDocuments
        ? 'Formato no permitido. Use: PNG, JPG, GIF, PDF, DWG, DOC, DOCX'
        : 'Por favor selecciona una imagen válida (PNG, JPG, GIF)';
    }
    if (file.size > MAX_SIZE) {
      return 'El archivo no debe superar los 10MB';
    }
    return null;
  }, [allowDocuments]);

  const processFile = useCallback(async (file) => {
    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      return;
    }

    setError(null);
    setSelectedFile(file);

    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onloadend = () => setPreview(reader.result);
      reader.readAsDataURL(file);
    } else {
      setPreview(null);
    }

    if (onFileSelect) {
      onFileSelect(file);
      return;
    }

    try {
      setUploading(true);
      const response = await archivoService.upload(file, tipoArchivo, proyectoId, terrenoId);
      if (onUploadSuccess) {
        onUploadSuccess(response);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Error al subir el archivo');
      setPreview(null);
      setSelectedFile(null);
    } finally {
      setUploading(false);
    }
  }, [validateFile, onFileSelect, onUploadSuccess, tipoArchivo, proyectoId, terrenoId]);

  const handleDragOver = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  }, []);

  const handleDragLeave = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  }, []);

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      processFile(files[0]);
    }
  }, [processFile]);

  const handleFileSelect = useCallback((event) => {
    const file = event.target.files[0];
    if (file) {
      processFile(file);
    }
  }, [processFile]);

  const handleRemove = useCallback(() => {
    setPreview(null);
    setSelectedFile(null);
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
    if (onUploadSuccess) {
      onUploadSuccess(null);
    }
  }, [onUploadSuccess]);

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const getFileIcon = () => {
    if (selectedFile && !selectedFile.type.startsWith('image/')) {
      return <FaFileAlt size={48} />;
    }
    return <FaImage size={48} />;
  };

  return (
    <div className="image-uploader">
      <input
        ref={fileInputRef}
        type="file"
        accept={allowDocuments ? ALL_ALLOWED.join(',') : 'image/*'}
        onChange={handleFileSelect}
        style={{ display: 'none' }}
      />

      {!preview && !selectedFile ? (
        <div
          className={`upload-area ${isDragOver ? 'drag-over' : ''}`}
          onClick={handleClick}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
        >
          {getFileIcon()}
          <p>Arrastra un archivo aquí o haz click para seleccionar</p>
          <span className="upload-hint">
            {allowDocuments
              ? 'PNG, JPG, GIF, PDF, DWG, DOC, DOCX - Máximo 10MB'
              : 'PNG, JPG o GIF - Máximo 10MB'}
          </span>
        </div>
      ) : (
        <div className="preview-area">
          {preview ? (
            <img src={preview} alt="Preview" className="preview-image" />
          ) : selectedFile ? (
            <div className="file-info">
              <FaFileAlt size={48} />
              <p>{selectedFile.name}</p>
              <span>{(selectedFile.size / 1024).toFixed(1)} KB</span>
            </div>
          ) : null}
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
