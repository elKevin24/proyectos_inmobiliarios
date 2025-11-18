import { useState, useEffect } from 'react';
import { MapContainer, ImageOverlay, Polygon, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import '../styles/PlanoViewer.css';

// Componente para ajustar el bounds del mapa
function SetBounds({ bounds }) {
  const map = useMap();
  useEffect(() => {
    if (bounds) {
      map.fitBounds(bounds);
    }
  }, [map, bounds]);
  return null;
}

function PlanoViewer({ imageUrl, terrenos = [], onTerrenoClick }) {
  const [imageBounds, setImageBounds] = useState(null);
  const [imageLoaded, setImageLoaded] = useState(false);

  useEffect(() => {
    if (imageUrl) {
      // Cargar la imagen para obtener sus dimensiones
      const img = new Image();
      img.onload = () => {
        // Definir bounds basados en las dimensiones de la imagen
        // Usamos coordenadas arbitrarias centradas en 0,0
        const aspectRatio = img.width / img.height;
        const height = 100;
        const width = height * aspectRatio;

        const bounds = [
          [-height / 2, -width / 2],
          [height / 2, width / 2]
        ];

        setImageBounds(bounds);
        setImageLoaded(true);
      };
      img.src = imageUrl;
    }
  }, [imageUrl]);

  if (!imageUrl || !imageLoaded || !imageBounds) {
    return (
      <div className="plano-viewer-empty">
        <p>No hay plano disponible</p>
      </div>
    );
  }

  // Función para obtener color según estado del terreno
  const getTerrenoColor = (estado) => {
    const colors = {
      DISPONIBLE: '#27ae60',
      VENDIDO: '#e74c3c',
      APARTADO: '#f39c12',
      RESERVADO: '#3498db',
    };
    return colors[estado] || '#95a5a6';
  };

  return (
    <div className="plano-viewer">
      <MapContainer
        center={[0, 0]}
        zoom={1}
        crs={L.CRS.Simple}
        style={{ height: '600px', width: '100%', backgroundColor: '#f5f5f5' }}
      >
        <SetBounds bounds={imageBounds} />

        {/* Imagen del plano como overlay */}
        <ImageOverlay
          url={imageUrl}
          bounds={imageBounds}
          opacity={1}
        />

        {/* Polígonos de terrenos sobre el plano */}
        {terrenos.map((terreno) => (
          terreno.coordenadas && terreno.coordenadas.length >= 3 && (
            <Polygon
              key={terreno.id}
              positions={terreno.coordenadas}
              pathOptions={{
                color: getTerrenoColor(terreno.estado),
                fillColor: getTerrenoColor(terreno.estado),
                fillOpacity: 0.3,
                weight: 2
              }}
              eventHandlers={{
                click: () => onTerrenoClick && onTerrenoClick(terreno)
              }}
            >
              {/* Tooltip con información del terreno */}
              {terreno.numero && (
                <div className="terreno-label">
                  Lote {terreno.numero}
                </div>
              )}
            </Polygon>
          )
        ))}
      </MapContainer>

      {/* Leyenda */}
      <div className="plano-legend">
        <h4>Estado de Terrenos</h4>
        <div className="legend-items">
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#27ae60' }}></span>
            Disponible
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#f39c12' }}></span>
            Apartado
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#e74c3c' }}></span>
            Vendido
          </div>
          <div className="legend-item">
            <span className="legend-color" style={{ backgroundColor: '#3498db' }}></span>
            Reservado
          </div>
        </div>
      </div>
    </div>
  );
}

export default PlanoViewer;
