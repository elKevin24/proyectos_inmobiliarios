import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaEdit, FaTrash, FaEye, FaPhone, FaEnvelope, FaFilePdf, FaFileExcel } from 'react-icons/fa';
import clienteStore from '../store/clienteStore';
import { generarReporteClientes } from '../utils/pdfGenerator';
import { exportarClientesExcel } from '../utils/excelGenerator';
import '../styles/Clientes.css';

function ClientesList() {
  const { clientes, isLoading, error, fetchClientes, deleteCliente } = clienteStore();
  const [filter, setFilter] = useState('');
  const [estadoFilter, setEstadoFilter] = useState('');

  useEffect(() => {
    loadClientes();
  }, [estadoFilter]);

  const loadClientes = async () => {
    const params = {};
    if (estadoFilter) {
      params.estado = estadoFilter;
    }
    await fetchClientes(params);
  };

  const handleDelete = async (id, nombre) => {
    if (window.confirm(`¿Está seguro de eliminar al cliente "${nombre}"?`)) {
      try {
        await deleteCliente(id);
      } catch (error) {
        alert('Error al eliminar el cliente');
      }
    }
  };

  const filteredClientes = clientes.filter(cliente =>
    cliente.nombre?.toLowerCase().includes(filter.toLowerCase()) ||
    cliente.apellidos?.toLowerCase().includes(filter.toLowerCase()) ||
    cliente.email?.toLowerCase().includes(filter.toLowerCase()) ||
    cliente.telefono?.includes(filter)
  );

  const getEstadoBadge = (estado) => {
    const badges = {
      PROSPECTO: 'badge-prospecto',
      INTERESADO: 'badge-interesado',
      COMPRADOR: 'badge-comprador',
      INACTIVO: 'badge-inactivo',
    };
    return badges[estado] || 'badge-default';
  };

  const getEstadoLabel = (estado) => {
    const labels = {
      PROSPECTO: 'Prospecto',
      INTERESADO: 'Interesado',
      COMPRADOR: 'Comprador',
      INACTIVO: 'Inactivo',
    };
    return labels[estado] || estado;
  };

  return (
    <div className="clientes-container">
      <div className="page-header">
        <div>
          <h1>Clientes</h1>
          <p>Gestión de clientes y prospectos</p>
        </div>
        <div className="header-actions">
          <button
            className="btn btn-secondary"
            onClick={() => generarReporteClientes(filteredClientes, 'Reporte de Clientes')}
            disabled={filteredClientes.length === 0}
          >
            <FaFilePdf /> PDF
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => exportarClientesExcel(filteredClientes)}
            disabled={filteredClientes.length === 0}
          >
            <FaFileExcel /> Excel
          </button>
          <Link to="/clientes/nuevo" className="btn btn-primary">
            <FaPlus /> Nuevo Cliente
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="filters">
        <input
          type="text"
          placeholder="Buscar por nombre, email o teléfono..."
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
          <option value="PROSPECTO">Prospecto</option>
          <option value="INTERESADO">Interesado</option>
          <option value="COMPRADOR">Comprador</option>
          <option value="INACTIVO">Inactivo</option>
        </select>
      </div>

      {isLoading ? (
        <div className="loading">Cargando clientes...</div>
      ) : (
        <div className="clientes-grid">
          {filteredClientes.length === 0 ? (
            <div className="empty-state">
              <p>No se encontraron clientes</p>
              <Link to="/clientes/nuevo" className="btn btn-primary">
                Crear primer cliente
              </Link>
            </div>
          ) : (
            filteredClientes.map((cliente) => (
              <div key={cliente.id} className="cliente-card">
                <div className="cliente-header">
                  <div className="cliente-avatar">
                    {cliente.nombre?.charAt(0)}{cliente.apellidos?.charAt(0)}
                  </div>
                  <div className="cliente-name">
                    <h3>{cliente.nombre} {cliente.apellidos}</h3>
                    <span className={`badge ${getEstadoBadge(cliente.estado)}`}>
                      {getEstadoLabel(cliente.estado)}
                    </span>
                  </div>
                </div>

                <div className="cliente-info">
                  {cliente.email && (
                    <div className="info-item">
                      <FaEnvelope />
                      <span>{cliente.email}</span>
                    </div>
                  )}
                  {cliente.telefono && (
                    <div className="info-item">
                      <FaPhone />
                      <span>{cliente.telefono}</span>
                    </div>
                  )}
                  {cliente.rfc && (
                    <div className="info-row">
                      <span className="label">RFC:</span>
                      <span className="value">{cliente.rfc}</span>
                    </div>
                  )}
                  {cliente.curp && (
                    <div className="info-row">
                      <span className="label">CURP:</span>
                      <span className="value">{cliente.curp}</span>
                    </div>
                  )}
                </div>

                <div className="cliente-actions">
                  <Link to={`/clientes/${cliente.id}`} className="btn-icon" title="Ver detalles">
                    <FaEye />
                  </Link>
                  <Link to={`/clientes/${cliente.id}/editar`} className="btn-icon" title="Editar">
                    <FaEdit />
                  </Link>
                  <button
                    onClick={() => handleDelete(cliente.id, `${cliente.nombre} ${cliente.apellidos}`)}
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
      )}
    </div>
  );
}

export default ClientesList;
