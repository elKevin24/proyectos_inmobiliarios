import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaEdit, FaTrash, FaEye, FaMap } from 'react-icons/fa';
import useProyectoStore from '../store/proyectoStore';
import '../styles/Proyectos.css';

function ProyectosList() {
  const { proyectos, isLoading, error, fetchProyectos, deleteProyecto } = useProyectoStore();
  const [filter, setFilter] = useState('');

  useEffect(() => {
    loadProyectos();
  }, []);

  const loadProyectos = async () => {
    await fetchProyectos();
  };

  const handleDelete = async (id, nombre) => {
    if (window.confirm(`¿Está seguro de eliminar el proyecto "${nombre}"?`)) {
      try {
        await deleteProyecto(id);
      } catch (error) {
        alert('Error al eliminar el proyecto');
      }
    }
  };

  const filteredProyectos = proyectos.filter(proyecto =>
    proyecto.nombre?.toLowerCase().includes(filter.toLowerCase()) ||
    proyecto.ubicacion?.toLowerCase().includes(filter.toLowerCase())
  );

  const getEstadoBadge = (estado) => {
    const badges = {
      PLANIFICACION: 'badge-planificacion',
      EN_VENTA: 'badge-en-venta',
      AGOTADO: 'badge-agotado',
      SUSPENDIDO: 'badge-suspendido',
      CANCELADO: 'badge-cancelado',
    };
    return badges[estado] || 'badge-default';
  };

  const getEstadoLabel = (estado) => {
    const labels = {
      PLANIFICACION: 'Planificación',
      EN_VENTA: 'En Venta',
      AGOTADO: 'Agotado',
      SUSPENDIDO: 'Suspendido',
      CANCELADO: 'Cancelado',
    };
    return labels[estado] || estado;
  };

  return (
    <div className="proyectos-container">
      <div className="page-header">
        <div>
          <h1>Proyectos</h1>
          <p>Gestión de desarrollos inmobiliarios</p>
        </div>
        <Link to="/proyectos/nuevo" className="btn btn-primary">
          <FaPlus /> Nuevo Proyecto
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="filters">
        <input
          type="text"
          placeholder="Buscar por nombre o ubicación..."
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="search-input"
        />
      </div>

      {isLoading ? (
        <div className="loading">Cargando proyectos...</div>
      ) : (
        <div className="proyectos-grid">
          {filteredProyectos.length === 0 ? (
            <div className="empty-state">
              <p>No se encontraron proyectos</p>
              <Link to="/proyectos/nuevo" className="btn btn-primary">
                Crear primer proyecto
              </Link>
            </div>
          ) : (
            filteredProyectos.map((proyecto) => (
              <div key={proyecto.id} className="proyecto-card">
                <div className="proyecto-header">
                  <h3>{proyecto.nombre}</h3>
                  <span className={`badge ${getEstadoBadge(proyecto.estado)}`}>
                    {getEstadoLabel(proyecto.estado)}
                  </span>
                </div>

                <div className="proyecto-info">
                  <div className="info-row">
                    <span className="label">Ubicación:</span>
                    <span className="value">{proyecto.ubicacion || 'N/A'}</span>
                  </div>
                  <div className="info-row">
                    <span className="label">Total Terrenos:</span>
                    <span className="value">{proyecto.totalTerrenos || 0}</span>
                  </div>
                  <div className="info-row">
                    <span className="label">Disponibles:</span>
                    <span className="value disponible">{proyecto.terrenosDisponibles || 0}</span>
                  </div>
                  <div className="info-row">
                    <span className="label">Vendidos:</span>
                    <span className="value vendido">{proyecto.terrenosVendidos || 0}</span>
                  </div>
                  {proyecto.precioBaseM2 && (
                    <div className="info-row">
                      <span className="label">Precio Base/m²:</span>
                      <span className="value precio">
                        ${Number(proyecto.precioBaseM2).toLocaleString('es-MX')}
                      </span>
                    </div>
                  )}
                </div>

                {proyecto.descripcion && (
                  <p className="proyecto-descripcion">{proyecto.descripcion}</p>
                )}

                <div className="proyecto-actions">
                  <Link to={`/proyectos/${proyecto.id}`} className="btn-icon" title="Ver detalles">
                    <FaEye />
                  </Link>
                  <Link to={`/proyectos/${proyecto.id}/plano`} className="btn-icon" title="Ver plano">
                    <FaMap />
                  </Link>
                  <Link to={`/proyectos/${proyecto.id}/editar`} className="btn-icon" title="Editar">
                    <FaEdit />
                  </Link>
                  <button
                    onClick={() => handleDelete(proyecto.id, proyecto.nombre)}
                    className="btn-icon btn-danger"
                    title="Eliminar"
                  >
                    <FaTrash />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default ProyectosList;
