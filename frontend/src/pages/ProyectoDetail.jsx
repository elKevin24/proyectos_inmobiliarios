import { useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { FaArrowLeft, FaEdit, FaTrash, FaMap, FaLayerGroup, FaPlus } from 'react-icons/fa';
import useProyectoStore from '../store/proyectoStore';
import useFaseStore from '../store/faseStore';
import '../styles/Proyectos.css';

function ProyectoDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const { selectedProyecto, fetchProyectoById, deleteProyecto, isLoading: loadingProyecto } = useProyectoStore();
  const { fases, fetchFases, isLoading: loadingFases } = useFaseStore();

  useEffect(() => {
    loadProyectoData();
  }, [id]);

  const loadProyectoData = async () => {
    try {
      await fetchProyectoById(id);
      await fetchFases({ proyectoId: id });
    } catch (error) {
      console.error('Error loading proyecto details:', error);
      alert('Error al cargar los detalles del proyecto');
    }
  };

  const handleDelete = async () => {
    if (window.confirm(`¿Está seguro de eliminar el proyecto "${selectedProyecto?.nombre}"?`)) {
      try {
        await deleteProyecto(id);
        navigate('/proyectos');
      } catch (error) {
        alert('Error al eliminar el proyecto');
      }
    }
  };

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

  if (loadingProyecto) {
    return (
      <div className="proyectos-container">
        <p>Cargando detalles del proyecto...</p>
      </div>
    );
  }

  if (!selectedProyecto) {
    return (
      <div className="proyectos-container">
        <div className="page-header">
          <button onClick={() => navigate('/proyectos')} className="btn-back">
            <FaArrowLeft /> Volver a Proyectos
          </button>
        </div>
        <p>Proyecto no encontrado</p>
      </div>
    );
  }

  return (
    <div className="proyectos-container">
      <div className="page-header">
        <div>
          <button onClick={() => navigate('/proyectos')} className="btn-back">
            <FaArrowLeft /> Volver a Proyectos
          </button>
          <h1>{selectedProyecto.nombre}</h1>
          <p>Detalles del desarrollo inmobiliario</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <Link to={`/proyectos/${id}/plano`} className="btn btn-secondary">
            <FaMap /> Ver Plano
          </Link>
          <Link to={`/proyectos/${id}/editar`} className="btn btn-primary">
            <FaEdit /> Editar
          </Link>
          <button onClick={handleDelete} className="btn btn-danger">
            <FaTrash /> Eliminar
          </button>
        </div>
      </div>

      <div className="detail-container" style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px', marginTop: '20px' }}>
        <div className="detail-main" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {/* General Information */}
          <div className="info-section" style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
            <h2 style={{ fontSize: '18px', color: '#2c3e50', borderBottom: '2px solid #f0f0f0', paddingBottom: '8px', marginBottom: '15px' }}>
              Información General
            </h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
              <div>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Nombre</span>
                <span style={{ fontSize: '16px', fontWeight: '600', color: '#2c3e50' }}>{selectedProyecto.nombre}</span>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Estado</span>
                <span className={`badge ${getEstadoBadge(selectedProyecto.estado)}`}>
                  {getEstadoLabel(selectedProyecto.estado)}
                </span>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Ubicación</span>
                <span style={{ fontSize: '16px', color: '#2c3e50' }}>{selectedProyecto.ubicacion || 'N/A'}</span>
              </div>
            </div>
            {selectedProyecto.descripcion && (
              <div style={{ marginTop: '15px' }}>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Descripción</span>
                <p style={{ margin: '5px 0 0 0', color: '#555', fontSize: '14px', lineHeight: '1.5' }}>
                  {selectedProyecto.descripcion}
                </p>
              </div>
            )}
          </div>

          {/* Location details */}
          <div className="info-section" style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
            <h2 style={{ fontSize: '18px', color: '#2c3e50', borderBottom: '2px solid #f0f0f0', paddingBottom: '8px', marginBottom: '15px' }}>
              Dirección y Ubicación
            </h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
              <div>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Dirección</span>
                <span style={{ fontSize: '14px', color: '#2c3e50' }}>{selectedProyecto.direccion || 'N/A'}</span>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Ciudad</span>
                <span style={{ fontSize: '14px', color: '#2c3e50' }}>{selectedProyecto.ciudad || 'N/A'}</span>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Estado Federativo</span>
                <span style={{ fontSize: '14px', color: '#2c3e50' }}>{selectedProyecto.estado || 'N/A'}</span>
              </div>
              <div>
                <span style={{ fontSize: '12px', color: '#7f8c8d', display: 'block' }}>Código Postal</span>
                <span style={{ fontSize: '14px', color: '#2c3e50' }}>{selectedProyecto.codigoPostal || 'N/A'}</span>
              </div>
            </div>
          </div>

          {/* Fases List */}
          <div className="info-section" style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #f0f0f0', paddingBottom: '8px', marginBottom: '15px' }}>
              <h2 style={{ fontSize: '18px', color: '#2c3e50', margin: 0 }}>
                <FaLayerGroup /> Fases del Proyecto
              </h2>
              <Link to={`/proyectos/${id}/fases/nueva`} className="btn btn-primary btn-sm" style={{ padding: '6px 12px', fontSize: '12px' }}>
                <FaPlus /> Nueva Fase
              </Link>
            </div>
            {loadingFases ? (
              <p>Cargando fases...</p>
            ) : fases.length === 0 ? (
              <p style={{ color: '#7f8c8d', fontStyle: 'italic' }}>Este proyecto no tiene fases creadas aún.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {fases.map((fase) => (
                  <div key={fase.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', border: '1px solid #f0f0f0', borderRadius: '6px' }}>
                    <div>
                      <h4 style={{ margin: '0 0 4px 0', color: '#2c3e50' }}>{fase.nombre}</h4>
                      <span style={{ fontSize: '12px', color: '#7f8c8d' }}>
                        Terrenos: {fase.totalTerrenos || 0} | Ubicación: {fase.croquisUrl ? 'Tiene mapa' : 'Sin mapa'}
                      </span>
                    </div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <Link to={`/proyectos/${id}/fases/${fase.id}/editar`} className="btn btn-secondary btn-sm" style={{ padding: '4px 8px', fontSize: '12px' }}>
                        Editar
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="detail-sidebar" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Commercial stats */}
          <div className="info-section" style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
            <h2 style={{ fontSize: '18px', color: '#2c3e50', borderBottom: '2px solid #f0f0f0', paddingBottom: '8px', marginBottom: '15px' }}>
              Estadísticas
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #f9f9f9', paddingBottom: '8px' }}>
                <span style={{ color: '#7f8c8d' }}>Terrenos Totales</span>
                <span style={{ fontWeight: '600', color: '#2c3e50' }}>{selectedProyecto.totalTerrenos || 0}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #f9f9f9', paddingBottom: '8px' }}>
                <span style={{ color: '#7f8c8d' }}>Disponibles</span>
                <span style={{ fontWeight: '600', color: '#27ae60' }}>{selectedProyecto.terrenosDisponibles || 0}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid #f9f9f9', paddingBottom: '8px' }}>
                <span style={{ color: '#7f8c8d' }}>Vendidos</span>
                <span style={{ fontWeight: '600', color: '#e74c3c' }}>{selectedProyecto.terrenosVendidos || 0}</span>
              </div>
              {selectedProyecto.precioBaseM2 && (
                <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '8px' }}>
                  <span style={{ color: '#7f8c8d' }}>Precio Base / m²</span>
                  <span style={{ fontWeight: '600', color: '#3498db' }}>
                    ${Number(selectedProyecto.precioBaseM2).toLocaleString('es-MX', { minimumFractionDigits: 2 })}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProyectoDetail;
