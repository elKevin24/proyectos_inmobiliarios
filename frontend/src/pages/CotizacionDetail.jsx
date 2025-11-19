import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import useCotizacionStore from '../store/cotizacionStore';
import { generarCotizacionPDF } from '../utils/pdfGenerator';
import '../styles/Cotizaciones.css';

const CotizacionDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const {
    selectedCotizacion,
    isLoading,
    error,
    fetchCotizacionById,
    deleteCotizacion
  } = useCotizacionStore();

  useEffect(() => {
    fetchCotizacionById(id);
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

  const isVigente = (fechaVigencia) => {
    if (!fechaVigencia) return false;
    const hoy = new Date();
    const vigencia = new Date(fechaVigencia);
    return vigencia >= hoy;
  };

  const getDiasRestantes = (fechaVigencia) => {
    if (!fechaVigencia) return 0;
    const hoy = new Date();
    const vigencia = new Date(fechaVigencia);
    const diffTime = vigencia - hoy;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const handleEliminar = async () => {
    if (window.confirm('¿Está seguro de eliminar esta cotización? Esta acción no se puede deshacer.')) {
      try {
        await deleteCotizacion(id);
        navigate('/cotizaciones');
      } catch (error) {
        console.error('Error al eliminar cotización:', error);
      }
    }
  };

  const handleConvertirAVenta = () => {
    // Redirigir al formulario de venta con datos de la cotización
    navigate('/ventas/nueva', {
      state: {
        cotizacionId: selectedCotizacion.id,
        terrenoId: selectedCotizacion.terrenoId,
        clienteNombre: selectedCotizacion.clienteNombre,
        clienteEmail: selectedCotizacion.clienteEmail,
        clienteTelefono: selectedCotizacion.clienteTelefono,
        precioTotal: selectedCotizacion.precioFinal
      }
    });
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando cotización...</p>
      </div>
    );
  }

  if (error || !selectedCotizacion) {
    return (
      <div className="error-container">
        <p>{error || 'Cotización no encontrada'}</p>
        <button className="btn btn-primary" onClick={() => navigate('/cotizaciones')}>
          Volver a Cotizaciones
        </button>
      </div>
    );
  }

  const vigente = isVigente(selectedCotizacion.fechaVigencia);
  const diasRestantes = getDiasRestantes(selectedCotizacion.fechaVigencia);

  return (
    <div className="detail-container">
      <div className="detail-header">
        <button className="btn-back" onClick={() => navigate('/cotizaciones')}>
          ← Volver
        </button>
        <h1>Detalle de Cotización #{selectedCotizacion.id}</h1>
        <span className={`badge ${vigente ? 'badge-success' : 'badge-danger'}`}>
          {vigente ? 'Vigente' : 'Vencida'}
        </span>
      </div>

      {/* Alerta de vigencia */}
      {vigente && diasRestantes <= 7 && diasRestantes > 0 && (
        <div className="alert alert-warning">
          ⚠️ Esta cotización vence en {diasRestantes} día{diasRestantes !== 1 ? 's' : ''}
        </div>
      )}

      {!vigente && (
        <div className="alert alert-danger">
          ⚠️ Esta cotización ha vencido
        </div>
      )}

      {/* Información del Cliente */}
      <div className="info-section">
        <h2>Información del Cliente</h2>
        <div className="info-grid">
          <div className="info-item">
            <label>Nombre:</label>
            <span>{selectedCotizacion.clienteNombre}</span>
          </div>
          {selectedCotizacion.clienteEmail && (
            <div className="info-item">
              <label>Email:</label>
              <span>{selectedCotizacion.clienteEmail}</span>
            </div>
          )}
          {selectedCotizacion.clienteTelefono && (
            <div className="info-item">
              <label>Teléfono:</label>
              <span>{selectedCotizacion.clienteTelefono}</span>
            </div>
          )}
        </div>
      </div>

      {/* Información del Terreno */}
      <div className="info-section">
        <h2>Información del Terreno</h2>
        <div className="info-grid">
          <div className="info-item">
            <label>Lote:</label>
            <span>{selectedCotizacion.terrenoNumeroLote}</span>
          </div>
          {selectedCotizacion.terrenoManzana && (
            <div className="info-item">
              <label>Manzana:</label>
              <span>{selectedCotizacion.terrenoManzana}</span>
            </div>
          )}
          {selectedCotizacion.proyectoNombre && (
            <div className="info-item">
              <label>Proyecto:</label>
              <span>{selectedCotizacion.proyectoNombre}</span>
            </div>
          )}
        </div>
      </div>

      {/* Información de Precios */}
      <div className="info-section">
        <h2>Información de Precios</h2>
        <div className="precio-breakdown">
          <div className="precio-row">
            <span className="precio-label">Precio Base:</span>
            <span className="precio-value">{formatCurrency(selectedCotizacion.precioBase)}</span>
          </div>

          {selectedCotizacion.descuento > 0 && (
            <>
              <div className="precio-row descuento-row">
                <span className="precio-label">
                  Descuento
                  {selectedCotizacion.porcentajeDescuento > 0 && (
                    <small> ({selectedCotizacion.porcentajeDescuento}%)</small>
                  )}:
                </span>
                <span className="precio-value descuento-value">
                  -{formatCurrency(selectedCotizacion.descuento)}
                </span>
              </div>
              <div className="precio-divider"></div>
            </>
          )}

          <div className="precio-row precio-final-row">
            <span className="precio-label">Precio Final:</span>
            <span className="precio-value precio-final-value">
              {formatCurrency(selectedCotizacion.precioFinal)}
            </span>
          </div>

          {selectedCotizacion.descuento > 0 && (
            <div className="ahorro-box">
              <span>💰 El cliente ahorra: {formatCurrency(selectedCotizacion.descuento)}</span>
            </div>
          )}
        </div>
      </div>

      {/* Información de Vigencia */}
      <div className="info-section">
        <h2>Información de Vigencia</h2>
        <div className="info-grid">
          <div className="info-item">
            <label>Fecha de Creación:</label>
            <span>{formatDate(selectedCotizacion.createdAt)}</span>
          </div>
          <div className="info-item">
            <label>Fecha de Vigencia:</label>
            <span>{formatDate(selectedCotizacion.fechaVigencia)}</span>
          </div>
          {vigente && (
            <div className="info-item">
              <label>Días Restantes:</label>
              <span className={diasRestantes <= 7 ? 'text-danger' : ''}>
                {diasRestantes > 0 ? `${diasRestantes} días` : 'Vence hoy'}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Observaciones */}
      {selectedCotizacion.observaciones && (
        <div className="info-section">
          <h2>Observaciones</h2>
          <p className="observaciones-text">{selectedCotizacion.observaciones}</p>
        </div>
      )}

      {/* Acciones */}
      <div className="actions-section">
        <h2>Acciones</h2>
        <div className="action-buttons-group">
          {vigente && (
            <button
              className="btn btn-primary"
              onClick={handleConvertirAVenta}
            >
              🔄 Convertir a Venta
            </button>
          )}

          <button
            className="btn btn-secondary"
            onClick={() => generarCotizacionPDF(selectedCotizacion)}
          >
            📄 Descargar PDF
          </button>

          <button
            className="btn btn-secondary"
            onClick={() => window.print()}
          >
            🖨️ Imprimir Cotización
          </button>

          <button
            className="btn btn-danger"
            onClick={handleEliminar}
          >
            🗑️ Eliminar
          </button>
        </div>
      </div>
    </div>
  );
};

export default CotizacionDetail;
