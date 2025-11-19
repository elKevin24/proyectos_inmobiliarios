import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaFileExcel } from 'react-icons/fa';
import usePlanPagoStore from '../store/planPagoStore';
import { exportarPlanesPagoExcel } from '../utils/excelGenerator';
import '../styles/PlanesPago.css';

const PlanesPagoList = () => {
  const navigate = useNavigate();
  const { planesPago, isLoading, error, fetchPlanesPago } = usePlanPagoStore();

  useEffect(() => {
    fetchPlanesPago();
  }, []);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(amount);
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('es-MX');
  };

  const getTipoPlanLabel = (tipo) => {
    const labels = {
      CONTADO: 'Contado',
      CREDITO: 'Crédito',
      APARTADO_CON_FINANCIAMIENTO: 'Apartado con Financiamiento'
    };
    return labels[tipo] || tipo;
  };

  const getFrecuenciaLabel = (frecuencia) => {
    const labels = {
      SEMANAL: 'Semanal',
      QUINCENAL: 'Quincenal',
      MENSUAL: 'Mensual',
      BIMESTRAL: 'Bimestral',
      TRIMESTRAL: 'Trimestral',
      SEMESTRAL: 'Semestral',
      ANUAL: 'Anual'
    };
    return labels[frecuencia] || frecuencia;
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando planes de pago...</p>
      </div>
    );
  }

  return (
    <div className="planes-pago-container">
      <div className="page-header">
        <h1>Planes de Pago</h1>
        <div className="header-actions">
          <button
            className="btn btn-secondary"
            onClick={() => exportarPlanesPagoExcel(planesPago)}
            disabled={planesPago.length === 0}
          >
            <FaFileExcel /> Excel
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      {/* Tabla de planes */}
      <div className="table-container">
        {planesPago.length === 0 ? (
          <div className="empty-state">
            <p>No hay planes de pago registrados</p>
            <p className="help-text">
              Los planes de pago se crean automáticamente al registrar una venta con financiamiento
            </p>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Tipo de Plan</th>
                <th>Monto Total</th>
                <th>Enganche</th>
                <th>Monto Financiado</th>
                <th>Tasa Interés</th>
                <th>Número de Pagos</th>
                <th>Frecuencia</th>
                <th>Avance</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {planesPago.map((plan) => (
                <tr key={plan.id}>
                  <td>{plan.id}</td>
                  <td>
                    <span className="tipo-plan-badge">
                      {getTipoPlanLabel(plan.tipoPlan)}
                    </span>
                  </td>
                  <td className="text-right">
                    {formatCurrency(plan.montoTotal)}
                  </td>
                  <td className="text-right">
                    {formatCurrency(plan.enganche)}
                  </td>
                  <td className="text-right">
                    {formatCurrency(plan.montoFinanciado)}
                  </td>
                  <td className="text-center">
                    {plan.aplicaInteres ? `${plan.tasaInteresAnual}%` : 'Sin interés'}
                  </td>
                  <td className="text-center">{plan.numeroPagos}</td>
                  <td>{getFrecuenciaLabel(plan.frecuenciaPago)}</td>
                  <td>
                    <div className="progress-container">
                      <div className="progress-bar">
                        <div
                          className="progress-fill"
                          style={{ width: `${plan.porcentajeAvance || 0}%` }}
                        ></div>
                      </div>
                      <span className="progress-text">
                        {(plan.porcentajeAvance || 0).toFixed(1)}%
                      </span>
                    </div>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-icon btn-view"
                        onClick={() => navigate(`/planes-pago/${plan.id}`)}
                        title="Ver detalles y amortización"
                      >
                        👁️
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Resumen */}
      {planesPago.length > 0 && (
        <div className="summary-section">
          <div className="summary-card">
            <span className="summary-label">Total Planes:</span>
            <span className="summary-value">{planesPago.length}</span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Monto Total Financiado:</span>
            <span className="summary-value">
              {formatCurrency(
                planesPago.reduce((sum, p) => sum + Number(p.montoFinanciado || 0), 0)
              )}
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Total Pendiente:</span>
            <span className="summary-value">
              {formatCurrency(
                planesPago.reduce((sum, p) => sum + Number(p.totalPendiente || 0), 0)
              )}
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Avance Promedio:</span>
            <span className="summary-value">
              {(
                planesPago.reduce((sum, p) => sum + Number(p.porcentajeAvance || 0), 0) /
                planesPago.length
              ).toFixed(1)}
              %
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

export default PlanesPagoList;
