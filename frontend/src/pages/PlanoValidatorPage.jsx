import { useState, useCallback, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaArrowLeft, FaRobot, FaSpinner, FaExclamationTriangle, FaCheckCircle } from 'react-icons/fa';
import ImageUploader from '../components/ImageUploader';
import PlanoValidator from '../components/PlanoValidator';
import archivoService from '../services/archivoService';
import api from '../services/api';
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
  const [processingPercent, setProcessingPercent] = useState(0);
  const [validatedLotes, setValidatedLotes] = useState([]);
  const [saving, setSaving] = useState(false);
  const sseRef = useRef(null);

  useEffect(() => {
    if (proyectoId) {
      fetchProyectoById(proyectoId).catch(() => {});
    }
  }, [proyectoId, fetchProyectoById]);

  // Cleanup SSE on unmount
  useEffect(() => {
    return () => sseRef.current?.close();
  }, []);

  const handleUploadSuccess = useCallback(async (archivoResponse) => {
    if (!archivoResponse) {
      setImageUrl(null);
      return;
    }

    setImageUrl(archivoService.getDownloadUrl(archivoResponse.id));
    setError(null);
    setStep(STEPS.PROCESSING);
    setProcessingMessage('Enviando plano al servidor...');
    setProcessingPercent(5);

    try {
      // 1. Llamar al Backend Java (no directo a Python)
      const formData = new FormData();
      // Re-fetch the file from the already-uploaded archive URL to send to analizar
      // El archivo ya está en el servidor; enviamos su nombre almacenado
      const initRes = await api.post(
        `/proyectos/${proyectoId}/planos/analizar`,
        { nombreAlmacenado: archivoResponse.nombreAlmacenado },
        { headers: { 'Content-Type': 'application/json' } }
      );
      const { tareaId, sseUrl } = initRes.data;

      // 2. Suscribirse al canal SSE para recibir progreso en tiempo real
      const token = localStorage.getItem('token');
      const eventSource = new EventSource(
        `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${sseUrl}?token=${token}`
      );
      sseRef.current = eventSource;

      eventSource.addEventListener('progreso', (e) => {
        const data = JSON.parse(e.data);
        setProcessingMessage(data.paso || 'Procesando...');
        setProcessingPercent(data.porcentaje || 0);
      });

      eventSource.addEventListener('completado', (e) => {
        eventSource.close();
        const resultado = JSON.parse(e.data);
        if (resultado.lotes?.length > 0) {
          setLotesDetectados(resultado.lotes);
        } else {
          setError('No se detectaron lotes. Puedes validar el plano manualmente.');
          setLotesDetectados([]);
        }
        setStep(STEPS.VALIDATE);
      });

      eventSource.addEventListener('error', (e) => {
        eventSource.close();
        let msg = 'Error al procesar el plano.';
        try { msg = JSON.parse(e.data)?.mensaje || msg; } catch (_) {}
        setError(`OCR no disponible: ${msg}. Puedes validar el plano manualmente.`);
        setStep(STEPS.VALIDATE);
        setLotesDetectados([]);
      });

      eventSource.onerror = () => {
        eventSource.close();
        setError('Se perdió la conexión con el servidor. Intenta de nuevo.');
        setStep(STEPS.UPLOAD);
      };

    } catch (err) {
      const msg = err.response?.data?.mensaje || err.message;
      setError(`Error al iniciar análisis: ${msg}`);
      setStep(STEPS.UPLOAD);
    }
  }, [proyectoId]);

  const handleConfirmValidation = useCallback(async (lotesValidados) => {
    setSaving(true);
    setError(null);
    try {
      // Llamar al endpoint de confirmación para persistir en BD
      await api.post(`/proyectos/${proyectoId}/planos/confirmar`, {
        proyectoId: Number(proyectoId),
        lotes: lotesValidados.map((l) => ({
          numeroLote: l.numeroLote,
          area: l.area ? Number(l.area) : null,
          coordenadasPlanoJson: JSON.stringify(l.poligono),
        })),
      });
      setValidatedLotes(lotesValidados);
      setStep(STEPS.COMPLETE);
    } catch (err) {
      setError(err.response?.data?.message || 'Error al guardar los lotes. Intenta de nuevo.');
    } finally {
      setSaving(false);
    }
  }, [proyectoId]);

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
              <div
                className="processing-bar-fill"
                style={{ width: `${processingPercent}%`, transition: 'width 0.5s ease' }}
              />
            </div>
            <span className="processing-percent">{processingPercent}%</span>
          </div>
        )}

        {step === STEPS.VALIDATE && imageUrl && (
          <PlanoValidator
            imageUrl={imageUrl}
            lotesDetectados={lotesDetectados}
            onConfirm={handleConfirmValidation}
            onCancel={handleCancelValidation}
            saving={saving}
          />
        )}

        {step === STEPS.COMPLETE && (
          <div className="complete-section">
            <div className="complete-icon">
              <FaCheckCircle size={64} style={{ color: '#4CAF50' }} />
            </div>
            <h3>¡Ingesta Completada!</h3>
            <p>
              <strong>{validatedLotes.length}</strong> lote{validatedLotes.length !== 1 ? 's' : ''} guardado{validatedLotes.length !== 1 ? 's' : ''} en el sistema con estado <strong>DISPONIBLE</strong>.
            </p>
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
