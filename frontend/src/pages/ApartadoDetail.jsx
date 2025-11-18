import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import useApartadoStore from '../store/apartadoStore';
import '../styles/Apartados.css';

const ApartadoDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const {
    selectedApartado,
    isLoading,
    error,
    fetchApartadoById,
    cancelarApartado,
    deleteApartado
  } = useApartadoStore();

  const [showCancelarModal, setShowCancelarModal] = useState(false);
  const [showConvertirModal, setShowConvertirModal] = useState(false);
  const [motivoCancelacion, setMotivoCancelacion] = useState('');

  useEffect(() => {
    fetchApartadoById(id);
  }, [id]);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const getEstadoBadgeClass = (estado) => {
    const classes = {
      VIGENTE: 'badge-success',
      VENCIDO: 'badge-danger',
      CONVERTIDO_A_VENTA: 'badge-info',
      CANCELADO: 'badge-warning'
    };
    return classes[estado] || 'badge-secondary';
  };

  const getEstadoLabel = (estado) => {
    const labels = {
      VIGENTE: 'Vigente',
      VENCIDO: 'Vencido',
      CONVERTIDO_A_VENTA: 'Convertido a Venta',
      CANCELADO: 'Cancelado'
    };
    return labels[estado] || estado;
  };

  const getDiasRestantes = (fechaVencimiento) => {
    const hoy = new Date();
    const vencimiento = new Date(fechaVencimiento);
    const diffTime = vencimiento - hoy;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const handleCancelar = async () => {
    if (!motivoCancelacion.trim()) {
      alert('Debe proporcionar un motivo de cancelación');
      return;
    }

    try {
      await cancelarApartado(id, motivoCancelacion);
      setShowCancelarModal(false);
      alert('Apartado cancelado exitosamente');
    } catch (error) {
      console.error('Error al cancelar apartado:', error);
    }
  };

  const handleEliminar = async () => {
    if (window.confirm('¿Está seguro de eliminar este apartado? Esta acción no se puede deshacer.')) {
      try {
        await deleteApartado(id);
        navigate('/apartados');
      } catch (error) {
        console.error('Error al eliminar apartado:', error);
      }
    }
  };

  const handleConvertirAVenta = () => {
    // Redirigir al formulario de venta con datos del apartado
    navigate('/ventas/nueva', {
      state: {
        apartadoId: selectedApartado.id,
        terrenoId: selectedApartado.terrenoId,
        clienteId: selectedApartado.clienteId,
        montoApartado: selectedApartado.montoApartado
      }
    });
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando apartado...</p>
      </div>
    );
  }

  if (error || !selectedApartado) {
    return (
      <div className="error-container">
        <p>{error || 'Apartado no encontrado'}</p>
        <button className="btn btn-primary" onClick={() => navigate('/apartados')}>
          Volver a Apartados
        </button>
      </div>
    );
  }

  const diasRestantes = getDiasRestantes(selectedApartado.fechaVencimiento);
  const puedeConvertir = selectedApartado.estado === 'VIGENTE';
  const puedeCancelar = ['VIGENTE', 'VENCIDO'].includes(selectedApartado.estado);

  return (
    <div className="detail-container">
      <div className="detail-header">
        <button className="btn-back" onClick={() => navigate('/apartados')}>
          ← Volver
        </button>
        <h1>Detalle del Apartado #{selectedApartado.id}</h1>
        <span className={`badge ${getEstadoBadgeClass(selectedApartado.estado)}`}>
          {getEstadoLabel(selectedApartado.estado)}
        </span>
      </div>

      {/* Alerta de vencimiento */}
      {selectedApartado.estado === 'VIGENTE' && diasRestantes <= 7 && diasRestantes > 0 && (
        <div className="alert alert-warning">
          ⚠️ Este apartado vence en {diasRestantes} día{diasRestantes !== 1 ? 's' : ''}
        </div>
      )}

      {selectedApartado.estado === 'VENCIDO' && (
        <div className="alert alert-danger">
          ⚠️ Este apartado ha vencido
        </div>
      )}

      {/* Información General */}
      <div className="info-section">
        <h2>Información General</h2>
        <div className="info-grid">
          <div className="info-item">
            <label>Cliente:</label>
            <span>{selectedApartado.clienteNombre || '-'}</span>
          </div>
          <div className="info-item">
            <label>Terreno:</label>
            <span>{selectedApartado.terrenoNumeroLote || `ID: ${selectedApartado.terrenoId}`}</span>
          </div>
          <div className="info-item">
            <label>Fecha de Apartado:</label>
            <span>{formatDate(selectedApartado.fechaApartado)}</span>
          </div>
          <div className="info-item">
            <label>Fecha de Vencimiento:</label>
            <span>{formatDate(selectedApartado.fechaVencimiento)}</span>
          </div>
          <div className="info-item">
            <label>Días de Vigencia:</label>
            <span>{selectedApartado.diasVigencia} días</span>
          </div>
          {selectedApartado.estado === 'VIGENTE' && (
            <div className="info-item">
              <label>Días Restantes:</label>
              <span className={diasRestantes <= 7 ? 'text-danger' : ''}>
                {diasRestantes > 0 ? `${diasRestantes} días` : 'Vencido'}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Información Financiera */}
      <div className="info-section">
        <h2>Información Financiera</h2>
        <div className="monto-info-apartado">
          <div className="monto-card">
            <span className="monto-label">Monto de Apartado</span>
            <span className="monto-value">{formatCurrency(selectedApartado.montoApartado)}</span>
          </div>
          <div className="monto-card">
            <span className="monto-label">Porcentaje</span>
            <span className="monto-value">{selectedApartado.porcentajeApartado}%</span>
          </div>
        </div>
      </div>

      {/* Observaciones */}
      {selectedApartado.observaciones && (
        <div className="info-section">
          <h2>Observaciones</h2>
          <p className="observaciones-text">{selectedApartado.observaciones}</p>
        </div>
      )}

      {/* Motivo de Cancelación */}
      {selectedApartado.estado === 'CANCELADO' && selectedApartado.motivo && (
        <div className="info-section">
          <h2>Motivo de Cancelación</h2>
          <p className="observaciones-text">{selectedApartado.motivo}</p>
        </div>
      )}

      {/* Acciones */}
      <div className="actions-section">
        <h2>Acciones</h2>
        <div className="action-buttons-group">
          {puedeConvertir && (
            <button
              className="btn btn-primary"
              onClick={handleConvertirAVenta}
            >
              🔄 Convertir a Venta
            </button>
          )}

          {puedeCancelar && (
            <button
              className="btn btn-warning"
              onClick={() => setShowCancelarModal(true)}
            >
              ✖️ Cancelar Apartado
            </button>
          )}

          <button
            className="btn btn-danger"
            onClick={handleEliminar}
          >
            🗑️ Eliminar
          </button>
        </div>
      </div>

      {/* Modal de Cancelación */}
      {showCancelarModal && (
        <div className="modal-overlay" onClick={() => setShowCancelarModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Cancelar Apartado</h2>
            <p>¿Está seguro de cancelar este apartado?</p>

            <div className="form-group">
              <label htmlFor="motivo">Motivo de Cancelación *</label>
              <textarea
                id="motivo"
                rows="4"
                value={motivoCancelacion}
                onChange={(e) => setMotivoCancelacion(e.target.value)}
                placeholder="Ingrese el motivo de la cancelación..."
              />
            </div>

            <div className="modal-actions">
              <button
                className="btn btn-secondary"
                onClick={() => setShowCancelarModal(false)}
              >
                Cerrar
              </button>
              <button
                className="btn btn-primary"
                onClick={handleCancelar}
                disabled={!motivoCancelacion.trim()}
              >
                Confirmar Cancelación
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ApartadoDetail;
