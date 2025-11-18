import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { FaArrowLeft, FaEdit, FaTrash, FaMap } from 'react-icons/fa';
import useTerrenoStore from '../store/terrenoStore';
import { MapContainer, TileLayer, Polygon } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import '../styles/Terrenos.css';

function TerrenoDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { selectedTerreno, fetchTerrenoById, deleteTerreno, isLoading } = useTerrenoStore();

  useEffect(() => {
    loadTerreno();
  }, [id]);

  const loadTerreno = async () => {
    try {
      await fetchTerrenoById(id);
    } catch (error) {
      console.error('Error loading terreno:', error);
      alert('Error al cargar el terreno');
    }
  };

  const handleDelete = async () => {
    if (window.confirm(`¿Está seguro de eliminar este terreno?`)) {
      try {
        await deleteTerreno(id);
        navigate('/terrenos');
      } catch (error) {
        alert('Error al eliminar el terreno');
      }
    }
  };

  const getEstadoColor = (estado) => {
    const colors = {
      DISPONIBLE: '#27ae60',
      VENDIDO: '#e74c3c',
      APARTADO: '#f39c12',
      RESERVADO: '#3498db',
    };
    return colors[estado] || '#95a5a6';
  };

  if (isLoading) {
    return (
      <div className="terrenos-container">
        <p>Cargando...</p>
      </div>
    );
  }

  if (!selectedTerreno) {
    return (
      <div className="terrenos-container">
        <p>Terreno no encontrado</p>
      </div>
    );
  }

  const center = selectedTerreno.coordenadas && selectedTerreno.coordenadas.length > 0
    ? [
        selectedTerreno.coordenadas.reduce((sum, coord) => sum + coord[0], 0) / selectedTerreno.coordenadas.length,
        selectedTerreno.coordenadas.reduce((sum, coord) => sum + coord[1], 0) / selectedTerreno.coordenadas.length
      ]
    : [19.4326, -99.1332];

  return (
    <div className="terrenos-container">
      <div className="page-header">
        <div>
          <button onClick={() => navigate('/terrenos')} className="btn-back">
            <FaArrowLeft /> Volver a Terrenos
          </button>
          <h1>{selectedTerreno.nombre || `Lote ${selectedTerreno.numero}`}</h1>
          <p>Detalles del terreno</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <Link to={`/terrenos/${id}/editar`} className="btn btn-primary">
            <FaEdit /> Editar
          </Link>
          <button onClick={handleDelete} className="btn btn-danger">
            <FaTrash /> Eliminar
          </button>
        </div>
      </div>

      <div className="detail-container">
        <div className="detail-main">
          <div className="info-section">
            <h2>Información General</h2>
            <div className="info-grid">
              <div className="info-card">
                <span className="info-label">Número</span>
                <span className="info-value">{selectedTerreno.numero}</span>
              </div>
              <div className="info-card">
                <span className="info-label">Estado</span>
                <span className={`badge badge-${selectedTerreno.estado.toLowerCase()}`}>
                  {selectedTerreno.estado}
                </span>
              </div>
              <div className="info-card">
                <span className="info-label">Área</span>
                <span className="info-value">{selectedTerreno.area} m²</span>
              </div>
              <div className="info-card">
                <span className="info-label">Precio por m²</span>
                <span className="info-value">
                  ${Number(selectedTerreno.precioM2 || 0).toLocaleString('es-MX', {
                    minimumFractionDigits: 2
                  })}
                </span>
              </div>
            </div>
          </div>

          {selectedTerreno.descripcion && (
            <div className="info-section">
              <h2>Descripción</h2>
              <p className="descripcion-text">{selectedTerreno.descripcion}</p>
            </div>
          )}

          <div className="info-section">
            <h2>Información de Precios</h2>
            <div className="info-grid">
              <div className="info-card">
                <span className="info-label">Precio Base</span>
                <span className="info-value">
                  ${Number(selectedTerreno.precioBase).toLocaleString('es-MX', {
                    minimumFractionDigits: 2
                  })}
                </span>
              </div>
              <div className="info-card">
                <span className="info-label">Ajuste de Precio</span>
                <span className="info-value">
                  ${Number(selectedTerreno.ajustePrecio || 0).toLocaleString('es-MX', {
                    minimumFractionDigits: 2
                  })}
                </span>
              </div>
              <div className="info-card">
                <span className="info-label">Multiplicador</span>
                <span className="info-value">
                  {Number(selectedTerreno.multiplicadorPrecio || 1).toFixed(2)}
                </span>
              </div>
              <div className="info-card highlight">
                <span className="info-label">Precio Final</span>
                <span className="info-value precio-final">
                  ${Number(selectedTerreno.precioFinal).toLocaleString('es-MX', {
                    minimumFractionDigits: 2
                  })}
                </span>
              </div>
            </div>
            <div className="precio-formula-display">
              <p>Cálculo: ({Number(selectedTerreno.precioBase).toFixed(2)} + {Number(selectedTerreno.ajustePrecio || 0).toFixed(2)}) × {Number(selectedTerreno.multiplicadorPrecio || 1).toFixed(2)} = ${Number(selectedTerreno.precioFinal).toLocaleString('es-MX', { minimumFractionDigits: 2 })}</p>
            </div>
          </div>

          {selectedTerreno.coordenadas && selectedTerreno.coordenadas.length >= 3 && (
            <div className="info-section">
              <h2>
                <FaMap /> Ubicación en el Mapa
              </h2>
              <div className="map-preview">
                <MapContainer
                  center={center}
                  zoom={15}
                  style={{ height: '400px', width: '100%', borderRadius: '8px' }}
                >
                  <TileLayer
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  />
                  <Polygon
                    positions={selectedTerreno.coordenadas}
                    pathOptions={{
                      color: getEstadoColor(selectedTerreno.estado),
                      fillColor: getEstadoColor(selectedTerreno.estado),
                      fillOpacity: 0.4,
                      weight: 3
                    }}
                  />
                </MapContainer>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default TerrenoDetail;
