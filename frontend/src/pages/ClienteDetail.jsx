import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  FaArrowLeft, FaEdit, FaPhone, FaEnvelope, FaIdCard,
  FaFileInvoiceDollar, FaBookmark, FaMoneyBillWave,
  FaCalendar, FaMapMarkerAlt, FaUser
} from 'react-icons/fa';
import api from '../services/api';
import '../styles/ClienteDetail.css';

function ClienteDetail() {
  const { id } = useParams();
  const [historial, setHistorial] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('cotizaciones');

  useEffect(() => {
    loadHistorial();
  }, [id]);

  const loadHistorial = async () => {
    try {
      const response = await api.get(`/clientes/${id}/historial`);
      setHistorial(response.data);
    } catch (err) {
      setError('Error al cargar el historial del cliente');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0,
    }).format(amount || 0);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    return new Date(dateStr).toLocaleDateString('es-MX');
  };

  const getEstadoBadge = (estado) => {
    const map = {
      PROSPECTO: { class: 'badge-prospecto', label: 'Prospecto' },
      INTERESADO: { class: 'badge-interesado', label: 'Interesado' },
      COMPRADOR: { class: 'badge-comprador', label: 'Comprador' },
      INACTIVO: { class: 'badge-inactivo', label: 'Inactivo' },
      VIGENTE: { class: 'badge-vigente', label: 'Vigente' },
      CONVERTIDO: { class: 'badge-convertido', label: 'Convertido' },
      VENCIDO: { class: 'badge-vencido', label: 'Vencido' },
      PAGADA: { class: 'badge-pagada', label: 'Pagada' },
      PENDIENTE: { class: 'badge-pendiente', label: 'Pendiente' },
      CANCELADA: { class: 'badge-cancelada', label: 'Cancelada' },
    };
    return map[estado] || { class: 'badge-default', label: estado };
  };

  if (loading) {
    return (
      <div className="cliente-detail-container">
        <div className="loading">Cargando historial...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="cliente-detail-container">
        <div className="error-container">
          <p>{error}</p>
          <Link to="/clientes" className="btn btn-primary">Volver a Clientes</Link>
        </div>
      </div>
    );
  }

  const { cliente, cotizaciones, apartados, ventas } = historial;

  return (
    <div className="cliente-detail-container">
      <div className="page-header">
        <div>
          <Link to="/clientes" className="back-link">
            <FaArrowLeft /> Volver a Clientes
          </Link>
          <h1>{cliente.nombreCompleto}</h1>
          <p>Detalle del cliente e historial de transacciones</p>
        </div>
        <div className="header-actions">
          <Link to={`/clientes/${id}/editar`} className="btn btn-primary">
            <FaEdit /> Editar Cliente
          </Link>
        </div>
      </div>

      {/* Cliente Info Card */}
      <div className="cliente-info-card">
        <div className="cliente-avatar-large">
          {cliente.nombre?.charAt(0)}{cliente.apellido?.charAt(0)}
        </div>
        <div className="cliente-info-grid">
          <div className="info-item">
            <FaUser />
            <div>
              <span className="info-label">Nombre completo</span>
              <span className="info-value">{cliente.nombreCompleto}</span>
            </div>
          </div>
          {cliente.email && (
            <div className="info-item">
              <FaEnvelope />
              <div>
                <span className="info-label">Email</span>
                <span className="info-value">{cliente.email}</span>
              </div>
            </div>
          )}
          {cliente.telefono && (
            <div className="info-item">
              <FaPhone />
              <div>
                <span className="info-label">Teléfono</span>
                <span className="info-value">{cliente.telefono}</span>
              </div>
            </div>
          )}
          {cliente.rfc && (
            <div className="info-item">
              <FaIdCard />
              <div>
                <span className="info-label">RFC</span>
                <span className="info-value">{cliente.rfc}</span>
              </div>
            </div>
          )}
          {cliente.curp && (
            <div className="info-item">
              <FaIdCard />
              <div>
                <span className="info-label">CURP</span>
                <span className="info-value">{cliente.curp}</span>
              </div>
            </div>
          )}
          {cliente.estadoCliente && (
            <div className="info-item">
              <FaUser />
              <div>
                <span className="info-label">Estado</span>
                <span className={`badge ${getEstadoBadge(cliente.estadoCliente).class}`}>
                  {getEstadoBadge(cliente.estadoCliente).label}
                </span>
              </div>
            </div>
          )}
          {cliente.origen && (
            <div className="info-item">
              <FaMapMarkerAlt />
              <div>
                <span className="info-label">Origen</span>
                <span className="info-value">{cliente.origenDescripcion || cliente.origen}</span>
              </div>
            </div>
          )}
          {(cliente.direccion || cliente.ciudad) && (
            <div className="info-item">
              <FaMapMarkerAlt />
              <div>
                <span className="info-label">Dirección</span>
                <span className="info-value">
                  {[cliente.direccion, cliente.ciudad, cliente.estado, cliente.codigoPostal]
                    .filter(Boolean).join(', ')}
                </span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Summary Stats */}
      <div className="cliente-stats-row">
        <div className="cliente-stat-card">
          <FaFileInvoiceDollar className="stat-icon-small" />
          <div>
            <span className="stat-number">{historial.totalCotizaciones}</span>
            <span className="stat-label">Cotizaciones</span>
          </div>
        </div>
        <div className="cliente-stat-card">
          <FaBookmark className="stat-icon-small" />
          <div>
            <span className="stat-number">{historial.totalApartados}</span>
            <span className="stat-label">Apartados</span>
          </div>
        </div>
        <div className="cliente-stat-card">
          <FaMoneyBillWave className="stat-icon-small" />
          <div>
            <span className="stat-number">{historial.totalVentas}</span>
            <span className="stat-label">Ventas</span>
          </div>
        </div>
        <div className="cliente-stat-card">
          <FaCalendar className="stat-icon-small" />
          <div>
            <span className="stat-number">{historial.tasaConversion || 0}%</span>
            <span className="stat-label">Conversión</span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="detail-tabs">
        <button
          className={`tab-btn ${activeTab === 'cotizaciones' ? 'active' : ''}`}
          onClick={() => setActiveTab('cotizaciones')}
        >
          <FaFileInvoiceDollar /> Cotizaciones ({cotizaciones?.length || 0})
        </button>
        <button
          className={`tab-btn ${activeTab === 'apartados' ? 'active' : ''}`}
          onClick={() => setActiveTab('apartados')}
        >
          <FaBookmark /> Apartados ({apartados?.length || 0})
        </button>
        <button
          className={`tab-btn ${activeTab === 'ventas' ? 'active' : ''}`}
          onClick={() => setActiveTab('ventas')}
        >
          <FaMoneyBillWave /> Ventas ({ventas?.length || 0})
        </button>
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {activeTab === 'cotizaciones' && (
          <div className="transaction-list">
            {(!cotizaciones || cotizaciones.length === 0) ? (
              <div className="empty-state">No hay cotizaciones registradas</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Proyecto</th>
                    <th>Terreno</th>
                    <th>Monto</th>
                    <th>Estado</th>
                    <th>Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {cotizaciones.map(c => (
                    <tr key={c.id}>
                      <td>
                        <Link to={`/cotizaciones/${c.id}`}>#{c.id}</Link>
                      </td>
                      <td>{c.proyectoNombre || 'N/A'}</td>
                      <td>{c.terrenoNumeroLote || 'N/A'}</td>
                      <td>{formatCurrency(c.montoTotal)}</td>
                      <td>
                        <span className={`badge ${getEstadoBadge(c.estado).class}`}>
                          {getEstadoBadge(c.estado).label}
                        </span>
                      </td>
                      <td>{formatDate(c.fechaCotizacion)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'apartados' && (
          <div className="transaction-list">
            {(!apartados || apartados.length === 0) ? (
              <div className="empty-state">No hay apartados registrados</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Proyecto</th>
                    <th>Terreno</th>
                    <th>Anticipo</th>
                    <th>Estado</th>
                    <th>Fecha Límite</th>
                  </tr>
                </thead>
                <tbody>
                  {apartados.map(a => (
                    <tr key={a.id}>
                      <td>
                        <Link to={`/apartados/${a.id}`}>#{a.id}</Link>
                      </td>
                      <td>{a.proyectoNombre || 'N/A'}</td>
                      <td>{a.terrenoNumeroLote || 'N/A'}</td>
                      <td>{formatCurrency(a.anticipoMonto)}</td>
                      <td>
                        <span className={`badge ${getEstadoBadge(a.estado).class}`}>
                          {getEstadoBadge(a.estado).label}
                        </span>
                      </td>
                      <td>{formatDate(a.fechaLimite)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === 'ventas' && (
          <div className="transaction-list">
            {(!ventas || ventas.length === 0) ? (
              <div className="empty-state">No hay ventas registradas</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Proyecto</th>
                    <th>Terreno</th>
                    <th>Monto Total</th>
                    <th>Estado</th>
                    <th>Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {ventas.map(v => (
                    <tr key={v.id}>
                      <td>
                        <Link to={`/ventas/${v.id}`}>#{v.id}</Link>
                      </td>
                      <td>{v.proyectoNombre || 'N/A'}</td>
                      <td>{v.terrenoNumeroLote || 'N/A'}</td>
                      <td>{formatCurrency(v.montoTotal)}</td>
                      <td>
                        <span className={`badge ${getEstadoBadge(v.estado).class}`}>
                          {getEstadoBadge(v.estado).label}
                        </span>
                      </td>
                      <td>{formatDate(v.fechaVenta)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default ClienteDetail;
