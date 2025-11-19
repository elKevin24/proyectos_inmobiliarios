import { useEffect, useRef, useState } from 'react';
import { MapContainer, TileLayer, Polygon, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import '../styles/Map.css';

// Componente para manejar los clicks en el mapa
function MapClickHandler({ onAddPoint }) {
  useMapEvents({
    click: (e) => {
      onAddPoint([e.latlng.lat, e.latlng.lng]);
    },
  });
  return null;
}

function MapEditor({ initialCoordinates, onChange }) {
  const [coordinates, setCoordinates] = useState(initialCoordinates || []);
  const [isDrawing, setIsDrawing] = useState(false);

  useEffect(() => {
    if (initialCoordinates && initialCoordinates.length > 0) {
      setCoordinates(initialCoordinates);
    }
  }, [initialCoordinates]);

  const handleAddPoint = (point) => {
    if (isDrawing) {
      const newCoordinates = [...coordinates, point];
      setCoordinates(newCoordinates);
      if (onChange) {
        onChange(newCoordinates);
      }
    }
  };

  const handleClearCoordinates = () => {
    setCoordinates([]);
    if (onChange) {
      onChange([]);
    }
  };

  const handleToggleDrawing = () => {
    setIsDrawing(!isDrawing);
  };

  const handleCompletePolygon = () => {
    setIsDrawing(false);
  };

  // Centro por defecto (Ciudad de México)
  const defaultCenter = [19.4326, -99.1332];
  const center = coordinates.length > 0
    ? [
        coordinates.reduce((sum, coord) => sum + coord[0], 0) / coordinates.length,
        coordinates.reduce((sum, coord) => sum + coord[1], 0) / coordinates.length
      ]
    : defaultCenter;

  return (
    <div className="map-editor">
      <div className="map-controls">
        <button
          type="button"
          onClick={handleToggleDrawing}
          className={`btn ${isDrawing ? 'btn-danger' : 'btn-primary'}`}
        >
          {isDrawing ? 'Detener Dibujo' : 'Iniciar Dibujo'}
        </button>

        {coordinates.length > 0 && (
          <>
            <button
              type="button"
              onClick={handleCompletePolygon}
              className="btn btn-success"
              disabled={!isDrawing || coordinates.length < 3}
            >
              Completar Polígono
            </button>
            <button
              type="button"
              onClick={handleClearCoordinates}
              className="btn btn-secondary"
            >
              Limpiar
            </button>
          </>
        )}

        <span className="points-counter">
          Puntos: {coordinates.length}
        </span>
      </div>

      <div className="map-container">
        <MapContainer
          center={center}
          zoom={13}
          style={{ height: '400px', width: '100%' }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {isDrawing && <MapClickHandler onAddPoint={handleAddPoint} />}

          {coordinates.length >= 3 && (
            <Polygon
              positions={coordinates}
              pathOptions={{
                color: '#3498db',
                fillColor: '#3498db',
                fillOpacity: 0.4,
                weight: 2
              }}
            />
          )}

          {coordinates.length === 1 && (
            <Polygon
              positions={coordinates}
              pathOptions={{
                color: '#e74c3c',
                fillColor: '#e74c3c',
                fillOpacity: 0.8,
                weight: 4
              }}
            />
          )}

          {coordinates.length === 2 && (
            <Polygon
              positions={coordinates}
              pathOptions={{
                color: '#f39c12',
                fillColor: '#f39c12',
                fillOpacity: 0.6,
                weight: 3
              }}
            />
          )}
        </MapContainer>
      </div>

      {isDrawing && (
        <div className="map-hint">
          Click en el mapa para agregar puntos al polígono (mínimo 3 puntos)
        </div>
      )}

      {coordinates.length > 0 && (
        <div className="coordinates-display">
          <h4>Coordenadas del Polígono:</h4>
          <div className="coordinates-list">
            {coordinates.map((coord, index) => (
              <div key={index} className="coordinate-item">
                Punto {index + 1}: {coord[0].toFixed(6)}, {coord[1].toFixed(6)}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default MapEditor;
