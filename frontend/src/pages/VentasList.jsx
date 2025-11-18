import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaEye } from 'react-icons/fa';
import ventaService from '../services/ventaService';
import '../styles/Ventas.css';

function VentasList() {
  const [ventas, setVentas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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

  return (
    <div className="ventas-container">
      <div className="page-header">
        <div>
          <h1>Ventas</h1>
          <p>Gestión de transacciones</p>
        </div>
        <Link to="/ventas/nueva" className="btn btn-primary">
          <FaPlus /> Nueva Venta
        </Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading">Cargando ventas...</div>
      ) : (
        <div className="ventas-table-container">
          {ventas.length === 0 ? (
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
                {ventas.map((venta) => (
                  <tr key={venta.id}>
                    <td>#{venta.id}</td>
                    <td>{formatDate(venta.fechaVenta)}</td>
                    <td>{venta.clienteNombre || 'N/A'}</td>
                    <td>{venta.terrenoNumero || 'N/A'}</td>
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
      )}
    </div>
  );
}

export default VentasList;
