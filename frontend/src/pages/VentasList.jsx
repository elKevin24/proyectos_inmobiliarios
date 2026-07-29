import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaEye, FaFilePdf, FaFileExcel } from 'react-icons/fa';
import toast from 'react-hot-toast';
import ventaService from '../services/ventaService';
import { generarReporteVentas } from '../utils/pdfGenerator';
import { exportarVentasExcel } from '../utils/excelGenerator';
import '../styles/Ventas.css';

function VentasList() {
  const [ventas, setVentas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;

  useEffect(() => {
    loadVentas();
  }, []);

  const loadVentas = async () => {
    try {
      setLoading(true);
      const data = await ventaService.getAll();
      setVentas(data);
    } catch (error) {
      setError('Error al cargar las ventas');
      toast.error('Error al cargar las ventas');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      PENDIENTE: 'badge-pendiente',
      PAGADO: 'badge-pagado',
      CANCELADO: 'badge-cancelado',
      CANCELADA: 'badge-cancelado',
    };
    return badges[estado] || 'badge-default';
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const totalPages = Math.ceil(ventas.length / ITEMS_PER_PAGE);
  const paginatedVentas = ventas.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  return (
    <div className="ventas-container">
      <div className="page-header">
        <div>
          <h1>Ventas</h1>
          <p>Gestión de transacciones</p>
        </div>
        <div className="header-actions">
          <button
            className="btn btn-secondary"
            onClick={() => generarReporteVentas(ventas, 'Reporte de Ventas')}
            disabled={ventas.length === 0}
          >
            <FaFilePdf /> PDF
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => exportarVentasExcel(ventas)}
            disabled={ventas.length === 0}
          >
            <FaFileExcel /> Excel
          </button>
          <Link to="/ventas/nueva" className="btn btn-primary">
            <FaPlus /> Nueva Venta
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Cargando ventas...</p>
        </div>
      ) : (
        <>
          <div className="ventas-table-container">
            {paginatedVentas.length === 0 ? (
              <div className="empty-state">
                <p>No se encontraron ventas</p>
                <Link to="/ventas/nueva" className="btn btn-primary">
                  Crear primera venta
                </Link>
              </div>
            ) : (
              <table className="ventas-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Fecha</th>
                    <th>Cliente</th>
                    <th>Terreno</th>
                    <th>Monto Total</th>
                    <th>Enganche</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedVentas.map((venta) => (
                    <tr key={venta.id}>
                      <td>#{venta.id}</td>
                      <td>{formatDate(venta.fechaVenta)}</td>
                      <td>{venta.clienteNombre || 'N/A'}</td>
                      <td>{venta.terrenoNumeroLote || 'N/A'}</td>
                      <td className="monto">
                        ${Number(venta.montoTotal).toLocaleString('es-MX', {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2
                        })}
                      </td>
                      <td className="monto">
                        ${Number(venta.enganche || 0).toLocaleString('es-MX', {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2
                        })}
                      </td>
                      <td>
                        <span className={`badge ${getEstadoBadge(venta.estado)}`}>
                          {venta.estado}
                        </span>
                      </td>
                      <td>
                        <Link to={`/ventas/${venta.id}`} className="btn-icon" title="Ver detalles">
                          <FaEye />
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
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

export default VentasList;
