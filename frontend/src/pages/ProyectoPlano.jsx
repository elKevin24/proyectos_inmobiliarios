import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaArrowLeft } from 'react-icons/fa';
import useProyectoStore from '../store/proyectoStore';
import useTerrenoStore from '../store/terrenoStore';
import PlanoViewer from '../components/PlanoViewer';
import '../styles/Proyectos.css';

function ProyectoPlano() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { selectedProyecto, fetchProyectoById, isLoading } = useProyectoStore();
  const { terrenos, fetchTerrenos } = useTerrenoStore();
  const [selectedTerreno, setSelectedTerreno] = useState(null);

  useEffect(() => {
    loadData();
  }, [id]);

  const loadData = async () => {
    try {
      await fetchProyectoById(id);
      await fetchTerrenos({ proyectoId: id });
    } catch (error) {
      console.error('Error loading data:', error);
    }
  };

  const handleTerrenoClick = (terreno) => {
    setSelectedTerreno(terreno);
  };

  if (isLoading) {
    return (
      <div className="proyectos-container">
        <p>Cargando...</p>
      </div>
    );
  }

  if (!selectedProyecto) {
    return (
      <div className="proyectos-container">
        <p>Proyecto no encontrado</p>
      </div>
    );
  }

  return (
    <div className="proyectos-container">
      <div className="page-header">
        <div>
          <button onClick={() => navigate('/proyectos')} className="btn-back">
            <FaArrowLeft /> Volver
          </button>
          <h1>Plano: {selectedProyecto.nombre}</h1>
          <p>Visualización de terrenos en el plano del proyecto</p>
        </div>
      </div>

      <div className="plano-container">
        <div className="plano-main">
          {selectedProyecto.archivoPlanoUrl ? (
            <PlanoViewer
              imageUrl={selectedProyecto.archivoPlanoUrl}
              terrenos={terrenos}
              onTerrenoClick={handleTerrenoClick}
            />
          ) : (
            <div className="no-plano">
              <p>Este proyecto no tiene un plano cargado</p>
              <button
                onClick={() => navigate(`/proyectos/${id}/editar`)}
                className="btn btn-primary"
              >
                Subir Plano
              </button>
            </div>
          )}
        </div>

        {selectedTerreno && (
          <div className="terreno-detail-panel">
            <h3>Detalles del Terreno</h3>
            <div className="detail-grid">
              <div className="detail-item">
                <span className="detail-label">Número:</span>
                <span className="detail-value">{selectedTerreno.numero}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Nombre:</span>
                <span className="detail-value">{selectedTerreno.nombre || 'N/A'}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Estado:</span>
                <span className={`badge badge-${selectedTerreno.estado.toLowerCase()}`}>
                  {selectedTerreno.estado}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Área:</span>
                <span className="detail-value">{selectedTerreno.area} m²</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Precio Base:</span>
                <span className="detail-value">
                  ${Number(selectedTerreno.precioBase).toLocaleString('es-MX')}
                </span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Precio Final:</span>
                <span className="detail-value precio-final">
                  ${Number(selectedTerreno.precioFinal).toLocaleString('es-MX')}
                </span>
              </div>
            </div>
            <div className="detail-actions">
              <button
                onClick={() => navigate(`/terrenos/${selectedTerreno.id}/editar`)}
                className="btn btn-primary"
              >
                Editar Terreno
              </button>
              <button
                onClick={() => setSelectedTerreno(null)}
                className="btn btn-secondary"
              >
                Cerrar
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default ProyectoPlano;
