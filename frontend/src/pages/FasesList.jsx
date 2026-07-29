import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  FaPlus, FaEdit, FaTrash,
  FaCalendar, FaArrowLeft
} from 'react-icons/fa';
import useFaseStore from '../store/faseStore';
import useProyectoStore from '../store/proyectoStore';
import '../styles/Fases.css';

function FasesList() {
  const { proyectoId } = useParams();
  const { fases, isLoading, error, fetchFases, deleteFase } = useFaseStore();
  const { selectedProyecto, fetchProyectoById } = useProyectoStore();
  const [showInactivas, setShowInactivas] = useState(false);

  useEffect(() => {
    if (proyectoId) {
      fetchFases({ proyectoId });
      fetchProyectoById(proyectoId);
    }
  }, [proyectoId]);

  const handleDelete = async (id, nombre) => {
    if (window.confirm(`¿Está seguro de eliminar la fase "${nombre}"?`)) {
      try {
        await deleteFase(id);
      } catch (error) {
        alert('Error al eliminar la fase');
      }
    }
  };

  const filteredFases = showInactivas
    ? fases
    : fases.filter(f => f.activa);

  const getProgressColor = (porcentaje) => {
    if (porcentaje >= 80) return '#2ecc71';
    if (porcentaje >= 50) return '#f39c12';
    return '#3498db';
  };

  return (
    <div className="fases-container">
      <div className="page-header">
        <div>
          <Link to="/proyectos" className="back-link">
            <FaArrowLeft /> Volver a Proyectos
          </Link>
          <h1>Fases del Proyecto</h1>
          <p>{selectedProyecto?.nombre || 'Cargando...'}</p>
        </div>
        <div className="header-actions">
          <label className="toggle-label">
            <input
              type="checkbox"
              checked={showInactivas}
              onChange={(e) => setShowInactivas(e.target.checked)}
            />
            Mostrar inactivas
          </label>
          <Link to={`/proyectos/${proyectoId}/fases/nueva`} className="btn btn-primary">
            <FaPlus /> Nueva Fase
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {isLoading ? (
        <div className="loading">Cargando fases...</div>
      ) : (
        <div className="fases-grid">
          {filteredFases.length === 0 ? (
            <div className="empty-state">
              <p>No se encontraron fases para este proyecto</p>
              <Link to={`/proyectos/${proyectoId}/fases/nueva`} className="btn btn-primary">
                Crear primera fase
              </Link>
            </div>
          ) : (
            filteredFases.map((fase) => (
              <div key={fase.id} className={`fase-card ${!fase.activa ? 'fase-inactiva' : ''}`}>
                <div className="fase-header">
                  <div className="fase-numero">Fase {fase.numeroFase}</div>
                  <div className="fase-badges">
                    {fase.activa ? (
                      <span className="badge badge-activa">Activa</span>
                    ) : (
                      <span className="badge badge-inactiva">Inactiva</span>
                    )}
                  </div>
                </div>

                <h3 className="fase-nombre">{fase.nombre}</h3>

                {fase.descripcion && (
                  <p className="fase-descripcion">{fase.descripcion}</p>
                )}

                <div className="fase-stats">
                  <div className="fase-stat">
                    <span className="fase-stat-label">Terrenos</span>
                    <span className="fase-stat-value">{fase.totalTerrenos || 0}</span>
                  </div>
                  <div className="fase-stat">
                    <span className="fase-stat-label">Disponibles</span>
                    <span className="fase-stat-value text-green">{fase.terrenosDisponibles || 0}</span>
                  </div>
                  <div className="fase-stat">
                    <span className="fase-stat-label">Apartados</span>
                    <span className="fase-stat-value text-orange">{fase.terrenosApartados || 0}</span>
                  </div>
                  <div className="fase-stat">
                    <span className="fase-stat-label">Vendidos</span>
                    <span className="fase-stat-value text-purple">{fase.terrenosVendidos || 0}</span>
                  </div>
                </div>

                {fase.totalTerrenos > 0 && (
                  <div className="fase-progress">
                    <div className="progress-bar">
                      <div
                        className="progress-fill"
                        style={{
                          width: `${((fase.terrenosVendidos || 0) / fase.totalTerrenos * 100)}%`,
                          backgroundColor: getProgressColor(fase.terrenosVendidos / fase.totalTerrenos * 100)
                        }}
                      />
                    </div>
                    <span className="progress-text">
                      {((fase.terrenosVendidos || 0) / fase.totalTerrenos * 100).toFixed(0)}% vendido
                    </span>
                  </div>
                )}

                <div className="fase-dates">
                  {fase.fechaInicio && (
                    <div className="fase-date">
                      <FaCalendar /> Inicio: {new Date(fase.fechaInicio).toLocaleDateString('es-MX')}
                    </div>
                  )}
                  {fase.fechaFinEstimada && (
                    <div className="fase-date">
                      <FaCalendar /> Fin estimado: {new Date(fase.fechaFinEstimada).toLocaleDateString('es-MX')}
                    </div>
                  )}
                </div>

                <div className="fase-actions">
                  <Link to={`/proyectos/${proyectoId}/fases/${fase.id}/editar`} className="btn-icon" title="Editar">
                    <FaEdit />
                  </Link>
                  <button
                    onClick={() => handleDelete(fase.id, fase.nombre)}
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

export default FasesList;
