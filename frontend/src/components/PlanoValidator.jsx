import { useState, useRef, useEffect, useCallback } from 'react';
import { FaCheck, FaTrash, FaEye, FaEyeSlash, FaEdit, FaUndo, FaSave } from 'react-icons/fa';
import '../styles/PlanoValidator.css';

const COLORS = {
  stroke: '#2196F3',
  strokeSelected: '#FF5722',
  strokeConfirmed: '#4CAF50',
  fill: 'rgba(33, 150, 243, 0.15)',
  fillSelected: 'rgba(255, 87, 34, 0.2)',
  fillConfirmed: 'rgba(76, 175, 80, 0.15)',
  labelBg: 'rgba(0, 0, 0, 0.7)',
  labelText: '#ffffff',
  deleted: 'rgba(244, 67, 54, 0.3)',
  strokeDeleted: '#F44336',
};

function computeLayout(canvasWidth, imageDims) {
  if (!imageDims) return { scale: 1, offsetX: 0, offsetY: 0, canvasHeight: 600 };
  const padding = 40;
  const availW = canvasWidth - padding * 2;
  const availH = canvasWidth - padding * 2;
  const scaleX = availW / imageDims.width;
  const scaleY = availH / imageDims.height;
  const s = Math.min(scaleX, scaleY, 1);
  const scaledW = imageDims.width * s;
  const scaledH = imageDims.height * s;
  const canvasHeight = Math.max(scaledH + padding * 2, 400);
  return {
    scale: s,
    offsetX: (canvasWidth - scaledW) / 2,
    offsetY: (canvasHeight - scaledH) / 2,
    canvasHeight,
  };
}

function PlanoValidator({ imageUrl, lotesDetectados = [], onConfirm, onCancel }) {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const [lotes, setLotes] = useState(() =>
    lotesDetectados.map((lote) => ({
      ...lote,
      confirmed: false,
      deleted: false,
      visible: true,
    }))
  );
  const [selectedId, setSelectedId] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editValue, setEditValue] = useState('');
  const [containerWidth, setContainerWidth] = useState(800);
  const [imageDimensions, setImageDimensions] = useState(null);
  const imageRef = useRef(null);
  const layoutRef = useRef({ scale: 1, offsetX: 0, offsetY: 0, canvasHeight: 600 });

  useEffect(() => {
    if (!containerRef.current) return;
    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        const { width } = entry.contentRect;
        setContainerWidth(Math.max(width, 400));
      }
    });
    observer.observe(containerRef.current);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!imageUrl) return;
    const img = new Image();
    img.onload = () => {
      imageRef.current = img;
      setImageDimensions({ width: img.width, height: img.height });
    };
    img.src = imageUrl;
  }, [imageUrl]);

  const drawCanvas = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const layout = computeLayout(containerWidth, imageDimensions);
    layoutRef.current = layout;

    canvas.width = containerWidth;
    canvas.height = layout.canvasHeight;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#f5f5f5';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    if (imageRef.current && imageDimensions) {
      ctx.drawImage(
        imageRef.current,
        layout.offsetX,
        layout.offsetY,
        imageDimensions.width * layout.scale,
        imageDimensions.height * layout.scale
      );
    }

    lotes.forEach((lote) => {
      if (!lote.visible || lote.deleted) return;
      if (!lote.poligono || lote.poligono.length < 3) return;

      const isSelected = lote.id_temporal === selectedId;
      const isConfirmed = lote.confirmed;

      ctx.beginPath();
      const first = lote.poligono[0];
      ctx.moveTo(layout.offsetX + first.x * layout.scale, layout.offsetY + first.y * layout.scale);
      for (let i = 1; i < lote.poligono.length; i++) {
        ctx.lineTo(layout.offsetX + lote.poligono[i].x * layout.scale, layout.offsetY + lote.poligono[i].y * layout.scale);
      }
      ctx.closePath();

      ctx.fillStyle = isSelected ? COLORS.fillSelected : isConfirmed ? COLORS.fillConfirmed : COLORS.fill;
      ctx.fill();

      ctx.strokeStyle = isSelected ? COLORS.strokeSelected : isConfirmed ? COLORS.strokeConfirmed : COLORS.stroke;
      ctx.lineWidth = isSelected ? 3 : 2;
      ctx.stroke();

      lote.poligono.forEach((punto) => {
        ctx.beginPath();
        ctx.arc(layout.offsetX + punto.x * layout.scale, layout.offsetY + punto.y * layout.scale, isSelected ? 5 : 3, 0, Math.PI * 2);
        ctx.fillStyle = isSelected ? COLORS.strokeSelected : COLORS.stroke;
        ctx.fill();
      });

      if (lote.coordenadas_centro) {
        const cx = layout.offsetX + lote.coordenadas_centro.x * layout.scale;
        const cy = layout.offsetY + lote.coordenadas_centro.y * layout.scale;

        const label = lote.numero_lote_detectado || 'N/D';
        ctx.font = 'bold 13px Arial';
        const textWidth = ctx.measureText(label).width;
        const paddingX = 6;
        const boxW = textWidth + paddingX * 2;
        const boxH = 22;

        ctx.fillStyle = COLORS.labelBg;
        ctx.beginPath();
        ctx.roundRect(cx - boxW / 2, cy - boxH / 2, boxW, boxH, 4);
        ctx.fill();

        ctx.fillStyle = COLORS.labelText;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(label, cx, cy);
      }
    });

    lotes.forEach((lote) => {
      if (!lote.deleted || !lote.visible) return;
      if (!lote.poligono || lote.poligono.length < 3) return;

      ctx.beginPath();
      const first = lote.poligono[0];
      ctx.moveTo(layout.offsetX + first.x * layout.scale, layout.offsetY + first.y * layout.scale);
      for (let i = 1; i < lote.poligono.length; i++) {
        ctx.lineTo(layout.offsetX + lote.poligono[i].x * layout.scale, layout.offsetY + lote.poligono[i].y * layout.scale);
      }
      ctx.closePath();

      ctx.fillStyle = COLORS.deleted;
      ctx.fill();
      ctx.strokeStyle = COLORS.strokeDeleted;
      ctx.lineWidth = 1;
      ctx.setLineDash([5, 5]);
      ctx.stroke();
      ctx.setLineDash([]);
    });
  }, [lotes, containerWidth, imageDimensions, selectedId]);

  useEffect(() => {
    drawCanvas();
  }, [drawCanvas]);

  const getCanvasCoords = useCallback((e) => {
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    return {
      x: (e.clientX - rect.left) * (canvas.width / rect.width),
      y: (e.clientY - rect.top) * (canvas.height / rect.height),
    };
  }, []);

  const isPointInPolygon = useCallback((px, py, polygon) => {
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
      const xi = polygon[i].x, yi = polygon[i].y;
      const xj = polygon[j].x, yj = polygon[j].y;
      if ((yi > py) !== (yj > py) && px < ((xj - xi) * (py - yi)) / (yj - yi) + xi) {
        inside = !inside;
      }
    }
    return inside;
  }, []);

  const handleCanvasClick = useCallback((e) => {
    const coords = getCanvasCoords(e);
    const layout = layoutRef.current;
    const canvasX = (coords.x - layout.offsetX) / layout.scale;
    const canvasY = (coords.y - layout.offsetY) / layout.scale;

    let clickedId = null;
    for (const lote of lotes) {
      if (lote.deleted || !lote.visible) continue;
      if (lote.poligono && isPointInPolygon(canvasX, canvasY, lote.poligono)) {
        clickedId = lote.id_temporal;
        break;
      }
    }

    setSelectedId(clickedId);
    setEditingId(null);
  }, [lotes, getCanvasCoords, isPointInPolygon]);

  const handleCanvasDoubleClick = useCallback((e) => {
    const coords = getCanvasCoords(e);
    const layout = layoutRef.current;
    const canvasX = (coords.x - layout.offsetX) / layout.scale;
    const canvasY = (coords.y - layout.offsetY) / layout.scale;

    for (const lote of lotes) {
      if (lote.deleted || !lote.visible) continue;
      if (lote.poligono && isPointInPolygon(canvasX, canvasY, lote.poligono)) {
        setSelectedId(lote.id_temporal);
        setEditingId(lote.id_temporal);
        setEditValue(lote.numero_lote_detectado || '');
        break;
      }
    }
  }, [lotes, getCanvasCoords, isPointInPolygon]);

  const handleConfirmEdit = useCallback(() => {
    if (editingId) {
      setLotes((prev) =>
        prev.map((l) =>
          l.id_temporal === editingId ? { ...l, numero_lote_detectado: editValue } : l
        )
      );
      setEditingId(null);
    }
  }, [editingId, editValue]);

  const handleToggleDelete = useCallback((id) => {
    setLotes((prev) =>
      prev.map((l) => (l.id_temporal === id ? { ...l, deleted: !l.deleted } : l))
    );
    if (selectedId === id) setSelectedId(null);
  }, [selectedId]);

  const handleToggleVisibility = useCallback((id) => {
    setLotes((prev) =>
      prev.map((l) => (l.id_temporal === id ? { ...l, visible: !l.visible } : l))
    );
  }, []);

  const handleToggleConfirm = useCallback((id) => {
    setLotes((prev) =>
      prev.map((l) => (l.id_temporal === id ? { ...l, confirmed: !l.confirmed } : l))
    );
  }, []);

  const handleConfirmAll = useCallback(() => {
    const validLotes = lotes
      .filter((l) => !l.deleted)
      .map(({ numero_lote_detectado, poligono, coordenadas_centro }) => ({
        numeroLote: numero_lote_detectado,
        poligono,
        coordenadasCentro: coordenadas_centro,
      }));
    if (onConfirm) onConfirm(validLotes);
  }, [lotes, onConfirm]);

  const layout = computeLayout(containerWidth, imageDimensions);
  const selectedLote = lotes.find((l) => l.id_temporal === selectedId);
  const activeLotes = lotes.filter((l) => !l.deleted);
  const deletedCount = lotes.filter((l) => l.deleted).length;

  return (
    <div className="plano-validator">
      <div className="validator-canvas-container" ref={containerRef}>
        <canvas
          ref={canvasRef}
          width={containerWidth}
          height={layout.canvasHeight}
          onClick={handleCanvasClick}
          onDoubleClick={handleCanvasDoubleClick}
          className="validator-canvas"
        />
      </div>

      <div className="validator-sidebar">
        <div className="sidebar-header">
          <h3>Lotes Detectados</h3>
          <span className="lote-count">{activeLotes.length} / {lotes.length}</span>
        </div>

        <div className="lote-list">
          {lotes.map((lote) => (
            <div
              key={lote.id_temporal}
              className={`lote-item ${selectedId === lote.id_temporal ? 'selected' : ''} ${lote.deleted ? 'deleted' : ''} ${lote.confirmed ? 'confirmed' : ''}`}
              onClick={() => !lote.deleted && setSelectedId(lote.id_temporal)}
            >
              {editingId === lote.id_temporal ? (
                <div className="lote-edit">
                  <input
                    type="text"
                    value={editValue}
                    onChange={(e) => setEditValue(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleConfirmEdit()}
                    autoFocus
                    className="lote-edit-input"
                  />
                  <button onClick={handleConfirmEdit} className="btn-icon btn-confirm">
                    <FaCheck />
                  </button>
                </div>
              ) : (
                <>
                  <div className="lote-info">
                    <span className="lote-number">{lote.numero_lote_detectado || 'N/D'}</span>
                    {lote.confirmed && <span className="lote-badge confirmed">OK</span>}
                    {lote.deleted && <span className="lote-badge deleted">X</span>}
                  </div>
                  <div className="lote-actions">
                    <button
                      onClick={(e) => { e.stopPropagation(); handleToggleConfirm(lote.id_temporal); }}
                      className="btn-icon"
                      title={lote.confirmed ? 'Marcar como pendiente' : 'Marcar como correcto'}
                    >
                      <FaCheck />
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setSelectedId(lote.id_temporal);
                        setEditingId(lote.id_temporal);
                        setEditValue(lote.numero_lote_detectado || '');
                      }}
                      className="btn-icon"
                      title="Editar numero"
                    >
                      <FaEdit />
                    </button>
                    <button
                      onClick={(e) => { e.stopPropagation(); handleToggleVisibility(lote.id_temporal); }}
                      className="btn-icon"
                      title={lote.visible ? 'Ocultar' : 'Mostrar'}
                    >
                      {lote.visible ? <FaEye /> : <FaEyeSlash />}
                    </button>
                    <button
                      onClick={(e) => { e.stopPropagation(); handleToggleDelete(lote.id_temporal); }}
                      className={`btn-icon ${lote.deleted ? 'btn-undelete' : 'btn-delete'}`}
                      title={lote.deleted ? 'Restaurar' : 'Eliminar'}
                    >
                      {lote.deleted ? <FaUndo /> : <FaTrash />}
                    </button>
                  </div>
                </>
              )}
            </div>
          ))}
        </div>

        {selectedLote && !selectedLote.deleted && (
          <div className="lote-detail">
            <h4>Detalle del Lote</h4>
            <div className="detail-row">
              <span>Numero:</span>
              <strong>{selectedLote.numero_lote_detectado}</strong>
            </div>
            <div className="detail-row">
              <span>Puntos del poligono:</span>
              <strong>{selectedLote.poligono?.length || 0}</strong>
            </div>
            <div className="detail-row">
              <span>Centroide:</span>
              <strong>
                ({selectedLote.coordenadas_centro?.x}, {selectedLote.coordenadas_centro?.y})
              </strong>
            </div>
          </div>
        )}

        {deletedCount > 0 && (
          <div className="deleted-info">
            {deletedCount} lote{deletedCount > 1 ? 's' : ''} eliminado{deletedCount > 1 ? 's' : ''}
          </div>
        )}

        <div className="sidebar-actions">
          <button onClick={onCancel} className="btn btn-secondary">
            Cancelar
          </button>
          <button
            onClick={handleConfirmAll}
            className="btn btn-primary"
            disabled={activeLotes.length === 0}
          >
            <FaSave /> Confirmar ({activeLotes.length} lotes)
          </button>
        </div>
      </div>
    </div>
  );
}

export default PlanoValidator;
