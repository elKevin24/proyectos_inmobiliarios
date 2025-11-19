import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { FaArrowLeft, FaMoneyBillWave, FaBan } from 'react-icons/fa';
import useVentaStore from '../store/ventaStore';
import pagoService from '../services/pagoService';
import '../styles/Ventas.css';

function VentaDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { selectedVenta, fetchVentaById, cancelarVenta, isLoading } = useVentaStore();
  const [pagos, setPagos] = useState([]);
  const [loadingPagos, setLoadingPagos] = useState(false);

  useEffect(() => {
    loadVenta();
    loadPagos();
  }, [id]);

  const loadVenta = async () => {
    try {
      await fetchVentaById(id);
    } catch (error) {
      console.error('Error loading venta:', error);
      alert('Error al cargar la venta');
    }
  };

  const loadPagos = async () => {
    try {
      setLoadingPagos(true);
      const data = await pagoService.getByVenta(id);
      setPagos(data);
    } catch (error) {
      console.error('Error loading pagos:', error);
    } finally {
      setLoadingPagos(false);
    }
  };

  const handleCancelar = async () => {
    const motivo = prompt('Ingrese el motivo de cancelación:');
    if (!motivo) return;

    if (window.confirm('¿Está seguro de cancelar esta venta?')) {
      try {
        await cancelarVenta(id, motivo);
        alert('Venta cancelada exitosamente');
        loadVenta();
      } catch (error) {
        alert('Error al cancelar la venta');
      }
    }
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      PENDIENTE: 'badge-pendiente',
      PAGADO: 'badge-pagado',
      CANCELADO: 'badge-cancelado',
    };
    return badges[estado] || 'badge-default';
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const totalPagado = pagos.reduce((sum, pago) => sum + Number(pago.monto || 0), 0);
  const saldoPendiente = selectedVenta ? Number(selectedVenta.montoTotal) - totalPagado : 0;

  if (isLoading) {
    return (
      <div className="ventas-container">
        <p>Cargando...</p>
      </div>
    );
  }

  if (!selectedVenta) {
    return (
      <div className="ventas-container">
        <p>Venta no encontrada</p>
      </div>
    );
  }

  return (
    <div className="ventas-container">
      <div className="page-header">
        <div>
          <button onClick={() => navigate('/ventas')} className="btn-back">
            <FaArrowLeft /> Volver a Ventas
          </button>
          <h1>Venta #{selectedVenta.id}</h1>
          <p>Detalles de la transacción</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          {selectedVenta.estado !== 'CANCELADO' && (
            <>
              <Link to={`/ventas/${id}/pagos/nuevo`} className="btn btn-primary">
                <FaMoneyBillWave /> Registrar Pago
              </Link>
              <button onClick={handleCancelar} className="btn btn-danger">
                <FaBan /> Cancelar Venta
              </button>
            </>
          )}
        </div>
      </div>

      <div className="detail-container">
        <div className="detail-main">
          <div className="info-section">
            <h2>Información General</h2>
            <div className="info-grid">
              <div className="info-card">
                <span className="info-label">Estado</span>
                <span className={`badge ${getEstadoBadge(selectedVenta.estado)}`}>
                  {selectedVenta.estado}
                </span>
              </div>
              <div className="info-card">
                <span className="info-label">Fecha de Venta</span>
                <span className="info-value">{formatDate(selectedVenta.fechaVenta)}</span>
              </div>
              <div className="info-card">
                <span className="info-label">Cliente</span>
                <span className="info-value">{selectedVenta.clienteNombre || 'N/A'}</span>
              </div>
              <div className="info-card">
                <span className="info-label">Terreno</span>
                <span className="info-value">Lote {selectedVenta.terrenoNumero || 'N/A'}</span>
              </div>
            </div>
          </div>

          <div className="info-section">
            <h2>Información Financiera</h2>
            <div className="info-grid">
              <div className="info-card">
                <span className="info-label">Monto Total</span>
                <span className="info-value">
                  ${Number(selectedVenta.montoTotal).toLocaleString('es-MX', {
                    minimumFractionDigits: 2
                  })}
                </span>
              </div>
              <div className="info-card">
                <span className="info-label">Enganche</span>
                <span className="info-value">
                  ${Number(selectedVenta.enganche || 0).toLocaleString('es-MX', {
                    minimumFractionDigits: 2
                  })}
                </span>
              </div>
              <div className="info-card">
                <span className="info-label">Monto Financiado</span>
                <span className="info-value">
                  ${Number(selectedVenta.montoFinanciado || 0).toLocaleString('es-MX', {
                    minimumFractionDigits: 2
                  })}
                </span>
              </div>
              <div className="info-card">
                <span className="info-label">Método de Pago</span>
                <span className="info-value">{selectedVenta.metodoPago || 'N/A'}</span>
              </div>
            </div>
          </div>

          {selectedVenta.planPago && (
            <div className="info-section">
              <h2>Plan de Financiamiento</h2>
              <div className="info-grid">
                <div className="info-card">
                  <span className="info-label">Tipo de Plan</span>
                  <span className="info-value">{selectedVenta.planPago.tipoPlan}</span>
                </div>
                <div className="info-card">
                  <span className="info-label">Número de Cuotas</span>
                  <span className="info-value">{selectedVenta.planPago.numeroCuotas}</span>
                </div>
                <div className="info-card">
                  <span className="info-label">Frecuencia</span>
                  <span className="info-value">{selectedVenta.planPago.frecuenciaPago}</span>
                </div>
                <div className="info-card">
                  <span className="info-label">Tasa de Interés</span>
                  <span className="info-value">{selectedVenta.planPago.tasaInteres}% anual</span>
                </div>
              </div>
            </div>
          )}

          <div className="info-section">
            <h2>
              <FaMoneyBillWave /> Historial de Pagos
            </h2>

            <div className="pagos-summary">
              <div className="summary-item">
                <span className="label">Total Pagado:</span>
                <span className="value pagado">${totalPagado.toLocaleString('es-MX', { minimumFractionDigits: 2 })}</span>
              </div>
              <div className="summary-item">
                <span className="label">Saldo Pendiente:</span>
                <span className="value pendiente">${saldoPendiente.toLocaleString('es-MX', { minimumFractionDigits: 2 })}</span>
              </div>
            </div>

            {loadingPagos ? (
              <p>Cargando pagos...</p>
            ) : pagos.length === 0 ? (
              <div className="empty-state-small">
                <p>No hay pagos registrados</p>
                {selectedVenta.estado !== 'CANCELADO' && (
                  <Link to={`/ventas/${id}/pagos/nuevo`} className="btn btn-primary">
                    Registrar Primer Pago
                  </Link>
                )}
              </div>
            ) : (
              <table className="pagos-table">
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Monto</th>
                    <th>Método</th>
                    <th>Referencia</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {pagos.map((pago) => (
                    <tr key={pago.id}>
                      <td>{formatDate(pago.fechaPago)}</td>
                      <td className="monto">
                        ${Number(pago.monto).toLocaleString('es-MX', {
                          minimumFractionDigits: 2
                        })}
                      </td>
                      <td>{pago.metodoPago}</td>
                      <td>{pago.referencia || '-'}</td>
                      <td>
                        <span className="badge badge-pagado">
                          {pago.estado || 'PAGADO'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {selectedVenta.observaciones && (
            <div className="info-section">
              <h2>Observaciones</h2>
              <p className="descripcion-text">{selectedVenta.observaciones}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default VentaDetail;
