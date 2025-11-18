import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import useApartadoStore from '../store/apartadoStore';
import '../styles/Apartados.css';

const ApartadosList = () => {
  const navigate = useNavigate();
  const { apartados, isLoading, error, fetchApartados } = useApartadoStore();
  const [filter, setFilter] = useState('todos');

  useEffect(() => {
    loadApartados();
  }, [filter]);

  const loadApartados = () => {
    const params = {};
    if (filter === 'vigentes') params.vigentes = true;
    if (filter === 'vencidos') params.vencidos = true;
    fetchApartados(params);
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

  const getDiasRestantes = (fechaVencimiento, estado) => {
    if (estado !== 'VIGENTE') return '-';
    const hoy = new Date();
    const vencimiento = new Date(fechaVencimiento);
    const diffTime = vencimiento - hoy;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays > 0 ? `${diffDays} días` : 'Vencido';
  };

  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando apartados...</p>
      </div>
    );
  }

  return (
    <div className="apartados-container">
      <div className="page-header">
        <h1>Apartados</h1>
        <button
          className="btn btn-primary"
          onClick={() => navigate('/apartados/nuevo')}
        >
          + Nuevo Apartado
        </button>
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
              className={`filter-btn ${filter === 'todos' ? 'active' : ''}`}
              onClick={() => setFilter('todos')}
            >
              Todos
            </button>
            <button
              className={`filter-btn ${filter === 'vigentes' ? 'active' : ''}`}
              onClick={() => setFilter('vigentes')}
            >
              Vigentes
            </button>
            <button
              className={`filter-btn ${filter === 'vencidos' ? 'active' : ''}`}
              onClick={() => setFilter('vencidos')}
            >
              Vencidos
            </button>
          </div>
        </div>
      </div>

      {/* Tabla de apartados */}
      <div className="table-container">
        {apartados.length === 0 ? (
          <div className="empty-state">
            <p>No hay apartados registrados</p>
            <button
              className="btn btn-primary"
              onClick={() => navigate('/apartados/nuevo')}
            >
              Crear primer apartado
            </button>
          </div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Cliente</th>
                <th>Terreno</th>
                <th>Monto Apartado</th>
                <th>Porcentaje</th>
                <th>Fecha Apartado</th>
                <th>Vencimiento</th>
                <th>Días Restantes</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {apartados.map((apartado) => (
                <tr key={apartado.id}>
                  <td>{apartado.clienteNombre || '-'}</td>
                  <td>
                    {apartado.terrenoNumeroLote || apartado.terrenoId}
                  </td>
                  <td className="text-right">
                    {formatCurrency(apartado.montoApartado)}
                  </td>
                  <td className="text-center">
                    {apartado.porcentajeApartado}%
                  </td>
                  <td>{formatDate(apartado.fechaApartado)}</td>
                  <td>{formatDate(apartado.fechaVencimiento)}</td>
                  <td className="text-center">
                    {getDiasRestantes(apartado.fechaVencimiento, apartado.estado)}
                  </td>
                  <td>
                    <span className={`badge ${getEstadoBadgeClass(apartado.estado)}`}>
                      {getEstadoLabel(apartado.estado)}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        className="btn-icon btn-view"
                        onClick={() => navigate(`/apartados/${apartado.id}`)}
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
      {apartados.length > 0 && (
        <div className="summary-section">
          <div className="summary-card">
            <span className="summary-label">Total Apartados:</span>
            <span className="summary-value">{apartados.length}</span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Vigentes:</span>
            <span className="summary-value">
              {apartados.filter(a => a.estado === 'VIGENTE').length}
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Vencidos:</span>
            <span className="summary-value">
              {apartados.filter(a => a.estado === 'VENCIDO').length}
            </span>
          </div>
          <div className="summary-card">
            <span className="summary-label">Convertidos:</span>
            <span className="summary-value">
              {apartados.filter(a => a.estado === 'CONVERTIDO_A_VENTA').length}
            </span>
          </div>
        </div>
      )}
    </div>
  );
};

export default ApartadosList;
