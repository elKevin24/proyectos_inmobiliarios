import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import usePlanPagoStore from '../store/planPagoStore';
import { generarEstadoCuentaPDF } from '../utils/pdfGenerator';
import { exportarAmortizacionesExcel } from '../utils/excelGenerator';
import '../styles/PlanesPago.css';

const PlanPagoDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const {
    selectedPlan,
    amortizaciones,
    isLoading,
    error,
    fetchPlanPagoById,
    fetchAmortizaciones
  } = usePlanPagoStore();

  const [showAmortizaciones, setShowAmortizaciones] = useState(true);

  useEffect(() => {
    loadPlanData();
  }, [id]);

  const loadPlanData = async () => {
    try {
      await fetchPlanPagoById(id);
      await fetchAmortizaciones(id);
    } catch (error) {
      console.error('Error al cargar plan de pago:', error);
    }
  };

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

  const getEstadoBadge = (estado) => {
    const badges = {
      PENDIENTE: 'badge-warning',
      PAGADA: 'badge-success',
      VENCIDA: 'badge-danger',
      CONDONADA: 'badge-info'
    };
    return badges[estado] || 'badge-secondary';
  };

  const getEstadoLabel = (estado) => {
    const labels = {
      PENDIENTE: 'Pendiente',
      PAGADA: 'Pagada',
      VENCIDA: 'Vencida',
      CONDONADA: 'Condonada'
    };
    return labels[estado] || estado;
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando plan de pago...</p>
      </div>
    );
  }

  if (error || !selectedPlan) {
    return (
      <div className="error-container">
        <p>{error || 'Plan de pago no encontrado'}</p>
        <button className="btn btn-primary" onClick={() => navigate('/planes-pago')}>
          Volver a Planes de Pago
        </button>
      </div>
    );
  }

  return (
    <div className="detail-container">
      <div className="detail-header">
        <button className="btn-back" onClick={() => navigate('/planes-pago')}>
          ← Volver
        </button>
        <h1>Plan de Pago #{selectedPlan.id}</h1>
        <span className="tipo-plan-badge large">
          {getTipoPlanLabel(selectedPlan.tipoPlan)}
        </span>
      </div>

      {/* Información General */}
      <div className="info-section">
        <h2>Información General</h2>
        <div className="info-grid">
          <div className="info-item">
            <label>Tipo de Plan:</label>
            <span>{getTipoPlanLabel(selectedPlan.tipoPlan)}</span>
          </div>
          <div className="info-item">
            <label>Frecuencia de Pago:</label>
            <span>{getFrecuenciaLabel(selectedPlan.frecuenciaPago)}</span>
          </div>
          <div className="info-item">
            <label>Número de Pagos:</label>
            <span>{selectedPlan.numeroPagos}</span>
          </div>
          <div className="info-item">
            <label>Plazo:</label>
            <span>{selectedPlan.plazoMeses} meses</span>
          </div>
          <div className="info-item">
            <label>Fecha de Inicio:</label>
            <span>{formatDate(selectedPlan.fechaInicio)}</span>
          </div>
          <div className="info-item">
            <label>Primer Pago:</label>
            <span>{formatDate(selectedPlan.fechaPrimerPago)}</span>
          </div>
          <div className="info-item">
            <label>Último Pago:</label>
            <span>{formatDate(selectedPlan.fechaUltimoPago)}</span>
          </div>
          <div className="info-item">
            <label>Días de Gracia:</label>
            <span>{selectedPlan.diasGracia} días</span>
          </div>
        </div>
      </div>

      {/* Información Financiera */}
      <div className="info-section">
        <h2>Información Financiera</h2>
        <div className="financial-summary">
          <div className="financial-card">
            <span className="financial-label">Monto Total</span>
            <span className="financial-value">{formatCurrency(selectedPlan.montoTotal)}</span>
          </div>
          <div className="financial-card">
            <span className="financial-label">Enganche</span>
            <span className="financial-value">{formatCurrency(selectedPlan.enganche)}</span>
          </div>
          <div className="financial-card">
            <span className="financial-label">Monto Financiado</span>
            <span className="financial-value">{formatCurrency(selectedPlan.montoFinanciado)}</span>
          </div>
        </div>

        <div className="interest-info">
          <div className="info-row">
            <span>Aplica Interés:</span>
            <span>{selectedPlan.aplicaInteres ? 'Sí' : 'No'}</span>
          </div>
          {selectedPlan.aplicaInteres && (
            <>
              <div className="info-row">
                <span>Tasa Interés Anual:</span>
                <span>{selectedPlan.tasaInteresAnual}%</span>
              </div>
              <div className="info-row">
                <span>Tasa Interés Mensual:</span>
                <span>{selectedPlan.tasaInteresMensual}%</span>
              </div>
            </>
          )}
          <div className="info-row">
            <span>Tasa de Mora Mensual:</span>
            <span>{selectedPlan.tasaMoraMensual}%</span>
          </div>
        </div>
      </div>

      {/* Progreso del Plan */}
      <div className="info-section">
        <h2>Progreso del Plan</h2>
        <div className="progress-detail">
          <div className="progress-bar-large">
            <div
              className="progress-fill-large"
              style={{ width: `${selectedPlan.porcentajeAvance || 0}%` }}
            ></div>
            <span className="progress-text-large">
              {(selectedPlan.porcentajeAvance || 0).toFixed(2)}%
            </span>
          </div>

          <div className="progress-stats">
            <div className="stat-item">
              <label>Total Pagado:</label>
              <span className="stat-value success">
                {formatCurrency(selectedPlan.totalPagado)}
              </span>
            </div>
            <div className="stat-item">
              <label>Total Pendiente:</label>
              <span className="stat-value danger">
                {formatCurrency(selectedPlan.totalPendiente)}
              </span>
            </div>
            <div className="stat-item">
              <label>Amortizaciones Pagadas:</label>
              <span className="stat-value">{selectedPlan.amortizacionesPagadas}</span>
            </div>
            <div className="stat-item">
              <label>Amortizaciones Pendientes:</label>
              <span className="stat-value">{selectedPlan.amortizacionesPendientes}</span>
            </div>
            <div className="stat-item">
              <label>Amortizaciones Vencidas:</label>
              <span className="stat-value danger">{selectedPlan.amortizacionesVencidas}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Tabla de Amortización */}
      <div className="info-section">
        <div className="section-header-with-toggle">
          <h2>Tabla de Amortización</h2>
          <div className="header-actions">
            <button
              className="btn btn-secondary"
              onClick={() => generarEstadoCuentaPDF(selectedPlan, amortizaciones)}
              disabled={amortizaciones.length === 0}
            >
              📄 Estado de Cuenta PDF
            </button>
            <button
              className="btn btn-secondary"
              onClick={() => exportarAmortizacionesExcel(amortizaciones, selectedPlan.id)}
              disabled={amortizaciones.length === 0}
            >
              📊 Exportar Excel
            </button>
            <button
              className="btn btn-secondary"
              onClick={() => setShowAmortizaciones(!showAmortizaciones)}
            >
              {showAmortizaciones ? 'Ocultar' : 'Mostrar'} Tabla
            </button>
          </div>
        </div>

        {showAmortizaciones && (
          <div className="table-container amortizaciones-table">
            {amortizaciones.length === 0 ? (
              <p className="empty-text">No hay amortizaciones generadas</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Vencimiento</th>
                    <th>Capital</th>
                    <th>Interés</th>
                    <th>Monto Total</th>
                    <th>Monto Pagado</th>
                    <th>Saldo Pendiente</th>
                    <th>Fecha de Pago</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {amortizaciones.map((amort) => (
                    <tr key={amort.id} className={amort.estado === 'VENCIDA' ? 'row-vencida' : ''}>
                      <td>{amort.numeroAmortizacion}</td>
                      <td>{formatDate(amort.fechaVencimiento)}</td>
                      <td className="text-right">{formatCurrency(amort.montoCapital)}</td>
                      <td className="text-right">{formatCurrency(amort.montoInteres)}</td>
                      <td className="text-right font-weight-bold">
                        {formatCurrency(amort.montoTotal)}
                      </td>
                      <td className="text-right">{formatCurrency(amort.montoPagado)}</td>
                      <td className="text-right">
                        {formatCurrency(amort.saldoPendiente)}
                      </td>
                      <td>{formatDate(amort.fechaPago)}</td>
                      <td>
                        <span className={`badge ${getEstadoBadge(amort.estado)}`}>
                          {getEstadoLabel(amort.estado)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr className="totals-row">
                    <td colSpan="2"><strong>TOTALES</strong></td>
                    <td className="text-right">
                      <strong>
                        {formatCurrency(
                          amortizaciones.reduce((sum, a) => sum + Number(a.montoCapital || 0), 0)
                        )}
                      </strong>
                    </td>
                    <td className="text-right">
                      <strong>
                        {formatCurrency(
                          amortizaciones.reduce((sum, a) => sum + Number(a.montoInteres || 0), 0)
                        )}
                      </strong>
                    </td>
                    <td className="text-right">
                      <strong>
                        {formatCurrency(
                          amortizaciones.reduce((sum, a) => sum + Number(a.montoTotal || 0), 0)
                        )}
                      </strong>
                    </td>
                    <td className="text-right">
                      <strong>
                        {formatCurrency(
                          amortizaciones.reduce((sum, a) => sum + Number(a.montoPagado || 0), 0)
                        )}
                      </strong>
                    </td>
                    <td colSpan="3"></td>
                  </tr>
                </tfoot>
              </table>
            )}
          </div>
        )}
      </div>

      {/* Notas */}
      {selectedPlan.notas && (
        <div className="info-section">
          <h2>Notas</h2>
          <p className="observaciones-text">{selectedPlan.notas}</p>
        </div>
      )}
    </div>
  );
};

export default PlanPagoDetail;
