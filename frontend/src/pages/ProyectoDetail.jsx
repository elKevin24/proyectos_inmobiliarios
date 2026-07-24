import { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  FaArrowLeft, FaEdit, FaMap, FaLayerGroup, FaBuilding,
  FaCalendar, FaCheckCircle, FaExclamationTriangle
} from 'react-icons/fa';
import useProyectoStore from '../store/proyectoStore';
import '../styles/Proyectos.css';

function ProyectoDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { selectedProyecto, fetchProyectoById, isLoading } = useProyectoStore();

  useEffect(() => {
    fetchProyectoById(id).catch(() => {});
  }, [id]);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0,
    }).format(amount || 0);
  };

  const getEstadoBadge = (estado) => {
    const map = {
      PLANIFICACION: { class: 'badge-planificacion', label: 'Planificación' },
      EN_VENTA: { class: 'badge-en-venta', label: 'En Venta' },
      AGOTADO: { class: 'badge-agotado', label: 'Agotado' },
      SUSPENDIDO: { class: 'badge-suspendido', label: 'Suspendido' },
      CANCELADO: { class: 'badge-cancelado', label: 'Cancelado' },
    };
    return map[estado] || { class: 'badge-default', label: estado };
  };

  if (isLoading) {
    return (
      <div className="proyectos-container">
        <div className="loading">Cargando proyecto...</div>
      </div>
    );
  }

  if (!selectedProyecto) {
    return (
      <div className="proyectos-container">
        <div className="empty-state">
          <p>Proyecto no encontrado</p>
          <Link to="/proyectos" className="btn btn-primary">Volver a Proyectos</Link>
        </div>
      </div>
    );
  }

  const p = selectedProyecto;
  const badge = getEstadoBadge(p.estado);

  return (
    <div className="proyectos-container">
      <div className="page-header">
        <div>
          <Link to="/proyectos" className="back-link">
            <FaArrowLeft /> Volver a Proyectos
          </Link>
          <h1>{p.nombre}</h1>
          <p>Detalle del proyecto</p>
        </div>
        <div className="header-actions">
          <Link to={`/proyectos/${id}/fases`} className="btn btn-secondary">
            <FaLayerGroup /> Fases
          </Link>
          <Link to={`/proyectos/${id}/plano`} className="btn btn-secondary">
            <FaMap /> Plano
          </Link>
          <Link to={`/proyectos/${id}/editar`} className="btn btn-primary">
            <FaEdit /> Editar
          </Link>
        </div>
      </div>

      {/* Info Card */}
      <div className="proyecto-detail-card">
        <div className="detail-section">
          <h3><FaBuilding /> Información General</h3>
          <div className="detail-grid">
            <div className="detail-item">
              <span className="detail-label">Nombre</span>
              <span className="detail-value">{p.nombre}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Estado</span>
              <span className={`badge ${badge.class}`}>{badge.label}</span>
            </div>
            {p.descripcion && (
              <div className="detail-item detail-item--full">
                <span className="detail-label">Descripción</span>
                <span className="detail-value">{p.descripcion}</span>
              </div>
            )}
          </div>
        </div>

        <div className="detail-section">
          <h3><FaMap /> Ubicación</h3>
          <div className="detail-grid">
            {p.ubicacion && (
              <div className="detail-item">
                <span className="detail-label">Ubicación</span>
                <span className="detail-value">{p.ubicacion}</span>
              </div>
            )}
            {p.direccion && (
              <div className="detail-item">
                <span className="detail-label">Dirección</span>
                <span className="detail-value">{p.direccion}</span>
              </div>
            )}
            {p.ciudad && (
              <div className="detail-item">
                <span className="detail-label">Ciudad</span>
                <span className="detail-value">{p.ciudad}</span>
              </div>
            )}
            {p.estado && (
              <div className="detail-item">
                <span className="detail-label">Estado/Entidad</span>
                <span className="detail-value">{p.estado}</span>
              </div>
            )}
            {p.codigoPostal && (
              <div className="detail-item">
                <span className="detail-label">Código Postal</span>
                <span className="detail-value">{p.codigoPostal}</span>
              </div>
            )}
          </div>
        </div>

        <div className="detail-section">
          <h3><FaCheckCircle /> Inventario</h3>
          <div className="detail-grid">
            <div className="detail-item">
              <span className="detail-label">Total Terrenos</span>
              <span className="detail-value">{p.totalTerrenos || 0}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Disponibles</span>
              <span className="detail-value" style={{ color: '#27ae60' }}>{p.terrenosDisponibles || 0}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Apartados</span>
              <span className="detail-value" style={{ color: '#f39c12' }}>{p.terrenosApartados || 0}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Vendidos</span>
              <span className="detail-value" style={{ color: '#e74c3c' }}>{p.terrenosVendidos || 0}</span>
            </div>
            {p.precioBaseM2 && (
              <div className="detail-item">
                <span className="detail-label">Precio Base/m²</span>
                <span className="detail-value">{formatCurrency(p.precioBaseM2)}</span>
              </div>
            )}
            {p.areaTotal && (
              <div className="detail-item">
                <span className="detail-label">Área Total</span>
                <span className="detail-value">{Number(p.areaTotal).toLocaleString()} m²</span>
              </div>
            )}
          </div>
        </div>

        <div className="detail-section">
          <h3><FaCalendar /> Fechas</h3>
          <div className="detail-grid">
            {p.createdAt && (
              <div className="detail-item">
                <span className="detail-label">Creado</span>
                <span className="detail-value">{new Date(p.createdAt).toLocaleDateString('es-MX')}</span>
              </div>
            )}
            {p.updatedAt && (
              <div className="detail-item">
                <span className="detail-label">Última actualización</span>
                <span className="detail-value">{new Date(p.updatedAt).toLocaleDateString('es-MX')}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProyectoDetail;
