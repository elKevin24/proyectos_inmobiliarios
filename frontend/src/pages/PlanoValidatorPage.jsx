import { useState, useCallback, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaArrowLeft, FaRobot, FaSpinner, FaExclamationTriangle } from 'react-icons/fa';
import ImageUploader from '../components/ImageUploader';
import PlanoValidator from '../components/PlanoValidator';
import archivoService from '../services/archivoService';
import cvEngineService from '../services/cvEngineService';
import useProyectoStore from '../store/proyectoStore';
import '../styles/Proyectos.css';
import '../styles/PlanoValidatorPage.css';

const STEPS = {
  UPLOAD: 'UPLOAD',
  PROCESSING: 'PROCESSING',
  VALIDATE: 'VALIDATE',
  COMPLETE: 'COMPLETE',
};

function PlanoValidatorPage() {
  const { id: proyectoId } = useParams();
  const navigate = useNavigate();
  const { selectedProyecto, fetchProyectoById } = useProyectoStore();

  const [step, setStep] = useState(STEPS.UPLOAD);
  const [imageUrl, setImageUrl] = useState(null);
  const [lotesDetectados, setLotesDetectados] = useState([]);
  const [error, setError] = useState(null);
  const [processingMessage, setProcessingMessage] = useState('');
  const [validatedLotes, setValidatedLotes] = useState([]);

  useEffect(() => {
    if (proyectoId) {
      fetchProyectoById(proyectoId).catch(() => {});
    }
  }, [proyectoId, fetchProyectoById]);

  const handleUploadSuccess = useCallback(async (archivoResponse) => {
    if (!archivoResponse) {
      setImageUrl(null);
      return;
    }

    setImageUrl(archivoService.getDownloadUrl(archivoResponse.id));
    setError(null);

    setStep(STEPS.PROCESSING);
    setProcessingMessage('Analizando plano con OCR...');

    try {
      const filePath = archivoResponse.nombreAlmacenado;
      const result = await cvEngineService.extractLots(filePath);

      if (result.lotes && result.lotes.length > 0) {
        setLotesDetectados(result.lotes);
        setStep(STEPS.VALIDATE);
      } else {
        setError('No se detectaron lotes en la imagen. Puedes continuar y dibujar los lotes manualmente.');
        setStep(STEPS.VALIDATE);
        setLotesDetectados([]);
      }
    } catch (err) {
      setError(`OCR no disponible: ${err.message}. Puedes validar el plano manualmente.`);
      setStep(STEPS.VALIDATE);
      setLotesDetectados([]);
    }
  }, []);

  const handleConfirmValidation = useCallback((lotesValidados) => {
    setValidatedLotes(lotesValidados);
    setStep(STEPS.COMPLETE);
  }, []);

  const handleCancelValidation = useCallback(() => {
    setLotesDetectados([]);
    setImageUrl(null);
    setError(null);
    setStep(STEPS.UPLOAD);
  }, []);

  return (
    <div className="proyectos-container">
      <div className="page-header">
        <div>
          <button onClick={() => navigate(-1)} className="btn-back">
            <FaArrowLeft /> Volver
          </button>
          <h1>Validar Plano {selectedProyecto ? `- ${selectedProyecto.nombre}` : ''}</h1>
          <p>Sube un plano, detecta lotes automáticamente y valida los resultados</p>
        </div>
      </div>

      <div className="validator-steps">
        <div className={`step ${step === STEPS.UPLOAD ? 'active' : step !== STEPS.UPLOAD ? 'done' : ''}`}>
          <span className="step-number">1</span>
          <span className="step-label">Subir Plano</span>
        </div>
        <div className="step-connector" />
        <div className={`step ${step === STEPS.PROCESSING ? 'active' : step === STEPS.VALIDATE || step === STEPS.COMPLETE ? 'done' : ''}`}>
          <span className="step-number">2</span>
          <span className="step-label">Detectar Lotes</span>
        </div>
        <div className="step-connector" />
        <div className={`step ${step === STEPS.VALIDATE ? 'active' : step === STEPS.COMPLETE ? 'done' : ''}`}>
          <span className="step-number">3</span>
          <span className="step-label">Validar y Corregir</span>
        </div>
      </div>

      {error && (
        <div className="validator-error">
          <FaExclamationTriangle />
          <span>{error}</span>
          <button onClick={() => setError(null)} className="btn-close-error">X</button>
        </div>
      )}

      <div className="validator-content">
        {step === STEPS.UPLOAD && (
          <div className="upload-section">
            <ImageUploader
              onUploadSuccess={handleUploadSuccess}
              proyectoId={proyectoId}
              tipoArchivo="PLANO_PROYECTO"
              allowDocuments={false}
            />
          </div>
        )}

        {step === STEPS.PROCESSING && (
          <div className="processing-section">
            <div className="processing-animation">
              <FaRobot size={64} className="processing-icon" />
              <FaSpinner size={32} className="spinner-icon" />
            </div>
            <h3>Procesando plano...</h3>
            <p>{processingMessage}</p>
            <div className="processing-bar">
              <div className="processing-bar-fill" />
            </div>
          </div>
        )}

        {step === STEPS.VALIDATE && imageUrl && (
          <PlanoValidator
            imageUrl={imageUrl}
            lotesDetectados={lotesDetectados}
            onConfirm={handleConfirmValidation}
            onCancel={handleCancelValidation}
          />
        )}

        {step === STEPS.COMPLETE && (
          <div className="complete-section">
            <div className="complete-icon">
              <FaRobot size={64} />
            </div>
            <h3>Validacion Completada</h3>
            <p>{validatedLotes.length} lote{validatedLotes.length !== 1 ? 's' : ''} validado{validatedLotes.length !== 1 ? 's' : ''} correctamente.</p>
            <div className="complete-actions">
              {proyectoId && (
                <button onClick={() => navigate(`/proyectos/${proyectoId}/plano`)} className="btn btn-primary">
                  Ver Plano del Proyecto
                </button>
              )}
              <button onClick={() => navigate('/proyectos')} className="btn btn-secondary">
                Volver a Proyectos
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default PlanoValidatorPage;
