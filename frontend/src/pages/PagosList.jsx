import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaEye, FaTrash, FaMoneyBillWave } from 'react-icons/fa';
import toast from 'react-hot-toast';
import pagoService from '../services/pagoService';
import '../styles/Ventas.css';

function PagosList() {
  const [pagos, setPagos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filtroMetodo, setFiltroMetodo] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const ITEMS_PER_PAGE = 10;

  const loadPagos = async () => {
    try {
      setLoading(true);
      const data = await pagoService.listarPagos();
      setPagos(data);
    } catch (err) {
      setError('Error al cargar los pagos');
      toast.error('Error al cargar los pagos');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPagos();
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('¿Estás seguro de eliminar este pago?')) return;
    try {
      await pagoService.delete(id);
      setPagos((prev) => prev.filter((p) => p.id !== id));
      toast.success('Pago eliminado correctamente');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al eliminar el pago');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('es-MX', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatCurrency = (amount) => {
    return `$${Number(amount ?? 0).toLocaleString('es-MX', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      APLICADO: 'badge-pagado',
      PENDIENTE: 'badge-pendiente',
      PAGADO: 'badge-pagado',
      CANCELADO: 'badge-cancelado',
      REEMBOLSADO: 'badge-default',
    };
    return badges[estado] || 'badge-default';
  };

  const pagosFiltrados = filtroMetodo
    ? pagos.filter((p) => p.metodoPago === filtroMetodo)
    : pagos;

  const totalPages = Math.ceil(pagosFiltrados.length / ITEMS_PER_PAGE);
  const paginatedPagos = pagosFiltrados.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  return (
    <div className="ventas-container">
      <div className="page-header">
        <div>
          <h1><FaMoneyBillWave /> Pagos</h1>
          <p>{pagos.length} pago{pagos.length !== 1 ? 's' : ''} registrado{pagos.length !== 1 ? 's' : ''}</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Cargando pagos...</p>
        </div>
      ) : (
        <>
          <div className="page-content">
            <div className="filters">
              <div className="filter-group">
                <label htmlFor="filtroMetodo">Método de pago</label>
                <select
                  id="filtroMetodo"
                  value={filtroMetodo}
                  onChange={(e) => {
                    setFiltroMetodo(e.target.value);
                    setCurrentPage(1);
                  }}
                >
                  <option value="">Todos</option>
                  <option value="EFECTIVO">Efectivo</option>
                  <option value="TRANSFERENCIA">Transferencia</option>
                  <option value="CHEQUE">Cheque</option>
                  <option value="TARJETA_CREDITO">Tarjeta de Crédito</option>
                </select>
              </div>
            </div>
          </div>

          <div className="ventas-table-container">
            {paginatedPagos.length === 0 ? (
              <div className="empty-state">
                <p>No se encontraron pagos</p>
              </div>
            ) : (
              <table className="ventas-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Fecha</th>
                    <th>Monto</th>
                    <th>Método</th>
                    <th>Estado</th>
                    <th>Plan Pago ID</th>
                    <th>Referencia</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedPagos.map((pago) => (
                    <tr key={pago.id}>
                      <td>#{pago.id}</td>
                      <td>{formatDate(pago.fechaPago)}</td>
                      <td className="monto">{formatCurrency(pago.montoPagado ?? pago.monto)}</td>
                      <td>{pago.metodoPagoDescripcion || pago.metodoPago || '-'}</td>
                      <td>
                        <span className={`badge ${getEstadoBadge(pago.estado)}`}>
                          {pago.estadoDescripcion || pago.estado}
                        </span>
                      </td>
                      <td>{pago.planPagoId || '-'}</td>
                      <td>{pago.referenciaPago || '-'}</td>
                      <td>
                        <Link
                          to={`/planes-pago/${pago.planPagoId}`}
                          className="btn-icon"
                          title="Ver plan de pago"
                        >
                          <FaEye />
                        </Link>
                        <button
                          className="btn-icon btn-danger"
                          title="Eliminar pago"
                          onClick={() => handleDelete(pago.id)}
                        >
                          <FaTrash />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-secondary"
                onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
              >
                Anterior
              </button>
              <div className="pagination-pages">
                {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
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
                onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
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

export default PagosList;
