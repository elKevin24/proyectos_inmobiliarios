import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaEdit, FaTrash, FaEye, FaFilePdf, FaFileExcel } from 'react-icons/fa';
import toast from 'react-hot-toast';
import useTerrenoStore from '../store/terrenoStore';
import { generarReporteTerrenos } from '../utils/pdfGenerator';
import { exportarTerrenosExcel } from '../utils/excelGenerator';
import '../styles/Terrenos.css';

function TerrenosList() {
  const { terrenos, isLoading, error, fetchTerrenos, deleteTerreno } = useTerrenoStore();
  const [filter, setFilter] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;

  useEffect(() => {
    loadTerrenos();
  }, [estadoFilter]);

  // Reset page when filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [filter, estadoFilter]);

  const loadTerrenos = async () => {
    const params = {};
    if (estadoFilter) {
      params.estado = estadoFilter;
    }
    try {
      await fetchTerrenos(params);
    } catch (err) {
      toast.error('Error al cargar terrenos');
    }
  };

  const handleDelete = async (id, nombre) => {
    if (window.confirm(`¿Está seguro de eliminar el terreno "${nombre}"?`)) {
      try {
        await deleteTerreno(id);
        toast.success(`Terreno "${nombre}" eliminado con éxito`);
      } catch (err) {
        toast.error(err.message || 'Error al eliminar el terreno');
      }
    }
  };

  const filteredTerrenos = terrenos.filter(terreno =>
    terreno.nombre?.toLowerCase().includes(filter.toLowerCase()) ||
    terreno.numero?.toLowerCase().includes(filter.toLowerCase())
  );

  const totalPages = Math.ceil(filteredTerrenos.length / ITEMS_PER_PAGE);
  const paginatedTerrenos = filteredTerrenos.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  const getEstadoBadge = (estado) => {
    const badges = {
      DISPONIBLE: 'badge-disponible',
      VENDIDO: 'badge-vendido',
      APARTADO: 'badge-apartado',
      RESERVADO: 'badge-reservado',
    };
    return badges[estado] || 'badge-default';
  };

  return (
    <div className="terrenos-container">
      <div className="page-header">
        <div>
          <h1>Terrenos</h1>
          <p>Gestión de lotes e inmuebles</p>
        </div>
        <div className="header-actions">
          <button
            className="btn btn-secondary"
            onClick={() => generarReporteTerrenos(filteredTerrenos, 'Reporte de Terrenos')}
            disabled={filteredTerrenos.length === 0}
          >
            <FaFilePdf /> PDF
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => exportarTerrenosExcel(filteredTerrenos)}
            disabled={filteredTerrenos.length === 0}
          >
            <FaFileExcel /> Excel
          </button>
          <Link to="/terrenos/nuevo" className="btn btn-primary">
            <FaPlus /> Nuevo Terreno
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="filters">
        <input
          type="text"
          placeholder="Buscar por nombre o número..."
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="search-input"
        />

        <select
          value={estadoFilter}
          onChange={(e) => setEstadoFilter(e.target.value)}
          className="filter-select"
        >
          <option value="">Todos los estados</option>
          <option value="DISPONIBLE">Disponible</option>
          <option value="APARTADO">Apartado</option>
          <option value="VENDIDO">Vendido</option>
          <option value="RESERVADO">Reservado</option>
        </select>
      </div>

      {isLoading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Cargando terrenos...</p>
        </div>
      ) : (
        <>
          <div className="terrenos-grid">
            {paginatedTerrenos.length === 0 ? (
              <div className="empty-state">
                <p>No se encontraron terrenos</p>
                <Link to="/terrenos/nuevo" className="btn btn-primary">
                  Crear primer terreno
                </Link>
              </div>
            ) : (
              paginatedTerrenos.map((terreno) => (
                <div key={terreno.id} className="terreno-card">
                  <div className="terreno-header">
                    <h3>{terreno.nombre || `Lote ${terreno.numero}`}</h3>
                    <span className={`badge ${getEstadoBadge(terreno.estado)}`}>
                      {terreno.estado}
                    </span>
                  </div>

                  <div className="terreno-info">
                    <div className="info-row">
                      <span className="label">Número:</span>
                      <span className="value">{terreno.numero}</span>
                    </div>
                    <div className="info-row">
                      <span className="label">Área:</span>
                      <span className="value">{terreno.area} m²</span>
                    </div>
                    <div className="info-row">
                      <span className="label">Precio Base:</span>
                      <span className="value">
                        ${Number(terreno.precioBase).toLocaleString('es-MX', {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2
                        })}
                      </span>
                    </div>
                    <div className="info-row">
                      <span className="label">Precio Final:</span>
                      <span className="value precio-final">
                        ${Number(terreno.precioFinal).toLocaleString('es-MX', {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2
                        })}
                      </span>
                    </div>
                  </div>

                  <div className="terreno-actions">
                    <Link to={`/terrenos/${terreno.id}`} className="btn-icon" title="Ver detalles">
                      <FaEye />
                    </Link>
                    <Link to={`/terrenos/${terreno.id}/editar`} className="btn-icon" title="Editar">
                      <FaEdit />
                    </Link>
                    <button
                      onClick={() => handleDelete(terreno.id, terreno.nombre || terreno.numero)}
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

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-secondary"
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
              >
                Anterior
              </button>
              <div className="pagination-pages">
                {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                  <button
                    key={page}
                    className={`pagination-page ${currentPage === page ? 'active' : ''}`}
                    onClick={() => setCurrentPage(page)}
                  >
                    {page}
                  </button>
                ))}
              </div>
              <button
                className="btn btn-secondary"
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages}
              >
                Siguiente
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default TerrenosList;
