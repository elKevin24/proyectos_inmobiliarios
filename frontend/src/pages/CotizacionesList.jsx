import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaFileExcel } from 'react-icons/fa';
import useCotizacionStore from '../store/cotizacionStore';
import { exportarCotizacionesExcel } from '../utils/excelGenerator';
import '../styles/Cotizaciones.css';

const CotizacionesList = () => {
  const navigate = useNavigate();
  const { cotizaciones, isLoading, error, fetchCotizaciones } = useCotizacionStore();
  const [filter, setFilter] = useState('todas');
  const [searchCliente, setSearchCliente] = useState('');

  useEffect(() => {
    loadCotizaciones();
  }, [filter]);

  const loadCotizaciones = () => {
    const params = {};
    if (filter === 'vigentes') params.vigentes = true;
    fetchCotizaciones(params);
  };

  const handleSearchCliente = () => {
    if (searchCliente.trim()) {
      fetchCotizaciones({ cliente: searchCliente });
    } else {
      loadCotizaciones();
    }
  };

  const isVigente = (fechaVigencia) => {
    if (!fechaVigencia) return false;
    const hoy = new Date();
    const vigencia = new Date(fechaVigencia);
    return vigencia >= hoy;
  };

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

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando cotizaciones...</p>
      </div>
    );
  }

  return (
    <div className="cotizaciones-container">
      <div className="page-header">
        <h1>Cotizaciones</h1>
        <div className="header-actions">
          <button
            className="btn btn-secondary"
            onClick={() => exportarCotizacionesExcel(cotizaciones)}
            disabled={cotizaciones.length === 0}
          >
            <FaFileExcel /> Excel
          </button>
          <button
            className="btn btn-primary"
            onClick={() => navigate('/cotizaciones/nueva')}
          >
            + Nueva Cotización
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      {/* Filtros */}
      <div className="filters-section">
        <div className="filter-group">
          <label>Filtrar por estado:</label>
          <div className="filter-buttons">
            <button
              className={`filter-btn ${filter === 'todas' ? 'active' : ''}`}
              onClick={() => setFilter('todas')}
            >
              Todas
            </button>
            <button
              className={`filter-btn ${filter === 'vigentes' ? 'active' : ''}`}
              onClick={() => setFilter('vigentes')}
            >
              Vigentes
            </button>
          </div>
        </div>

        <div className="filter-group">
          <label>Buscar por cliente:</label>
          <div className="search-box">
            <input
              type="text"
              placeholder="Nombre del cliente..."
              value={searchCliente}
              onChange={(e) => setSearchCliente(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearchCliente()}
            />
            <button
              className="btn btn-secondary"
              onClick={handleSearchCliente}
            >
              Buscar
            </button>
            {searchCliente && (
              <button
                className="btn btn-secondary"
                onClick={() => {
                  setSearchCliente('');
                  loadCotizaciones();
                }}
              >
                Limpiar
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Tabla de cotizaciones */}
      <div className="table-container">
        {cotizaciones.length === 0 ? (
          <div className="empty-state">
            <p>No hay cotizaciones registradas</p>
            <button
              className="btn btn-primary"
              onClick={() => navigate('/cotizaciones/nueva')}
            >
              Crear primera cotización
            </button>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Terreno</th>
                <th>Proyecto</th>
                <th>Precio Base</th>
                <th>Descuento</th>
                <th>Precio Final</th>
                <th>Vigencia</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {cotizaciones.map((cotizacion) => (
                <tr key={cotizacion.id}>
                  <td>
                    <div className="cliente-info">
                      <strong>{cotizacion.clienteNombre}</strong>
                      {cotizacion.clienteTelefono && (
                        <small>{cotizacion.clienteTelefono}</small>
                      )}
                    </div>
                  </td>
                  <td>
                    {cotizacion.terrenoManzana
                      ? `Mz ${cotizacion.terrenoManzana} Lt ${cotizacion.terrenoNumeroLote}`
                      : `Lt ${cotizacion.terrenoNumeroLote}`}
                  </td>
                  <td>{cotizacion.proyectoNombre || '-'}</td>
                  <td className="text-right">
                    {formatCurrency(cotizacion.precioBase)}
                  </td>
                  <td className="text-right">
                    {cotizacion.descuento > 0 ? (
                      <span className="descuento-badge">
                        -{formatCurrency(cotizacion.descuento)}
                        {cotizacion.porcentajeDescuento > 0 && (
                          <small> ({cotizacion.porcentajeDescuento}%)</small>
                        )}
                      </span>
                    ) : (
                      '-'
                    )}
                  </td>
                  <td className="text-right precio-final">
                    {formatCurrency(cotizacion.precioFinal)}
                  </td>
                  <td>{formatDate(cotizacion.fechaVigencia)}</td>
                  <td>
                    <span
                      className={`badge ${
                        isVigente(cotizacion.fechaVigencia)
                          ? 'badge-success'
                          : 'badge-danger'
                      }`}
                    >
                      {isVigente(cotizacion.fechaVigencia) ? 'Vigente' : 'Vencida'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-icon btn-view"
                        onClick={() => navigate(`/cotizaciones/${cotizacion.id}`)}
                        title="Ver detalles"
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
      {cotizaciones.length > 0 && (
        <div className="summary-section">
          <div className="summary-card">
            <span className="summary-label">Total Cotizaciones:</span>
            <span className="summary-value">{cotizaciones.length}</span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Vigentes:</span>
            <span className="summary-value">
              {cotizaciones.filter(c => isVigente(c.fechaVigencia)).length}
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Vencidas:</span>
            <span className="summary-value">
              {cotizaciones.filter(c => !isVigente(c.fechaVigencia)).length}
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

export default CotizacionesList;
