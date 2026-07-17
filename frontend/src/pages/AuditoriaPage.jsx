import { useEffect, useState } from 'react';
import {
  FaShieldAlt, FaSearch, FaArchive,
  FaUser, FaTable, FaCalendar, FaClock
} from 'react-icons/fa';
import useAuditoriaStore from '../store/auditoriaStore';
import '../styles/Auditoria.css';

function AuditoriaPage() {
  const {
    logsSimples, logsCriticos, historial,
    isLoading, error,
    fetchLogsSimples, fetchLogsCriticos, fetchHistorialRegistro, archivarLogs
  } = useAuditoriaStore();

  const [activeTab, setActiveTab] = useState('simple');
  const [filters, setFilters] = useState({
    usuarioId: '',
    accion: '',
    tabla: '',
    fechaInicio: '',
    fechaFin: '',
    limit: 50,
  });
  const [historialSearch, setHistorialSearch] = useState({ tabla: '', registroId: '' });

  useEffect(() => {
    loadLogs();
  }, [activeTab]);

  const loadLogs = async () => {
    const params = {};
    if (filters.usuarioId) params.usuarioId = filters.usuarioId;
    if (filters.accion) params.accion = filters.accion;
    if (filters.tabla) params.tabla = filters.tabla;
    if (filters.fechaInicio) params.fechaInicio = filters.fechaInicio;
    if (filters.fechaFin) params.fechaFin = filters.fechaFin;
    if (filters.limit) params.limit = filters.limit;

    try {
      if (activeTab === 'simple') {
        await fetchLogsSimples(params);
      } else if (activeTab === 'critica') {
        await fetchLogsCriticos(params);
      }
    } catch (err) {
      // Error handled by store
    }
  };

  const handleHistorialSearch = async () => {
    if (!historialSearch.tabla || !historialSearch.registroId) {
      alert('Ingrese tabla y ID de registro');
      return;
    }
    try {
      await fetchHistorialRegistro(historialSearch.tabla, historialSearch.registroId);
    } catch (err) {
      // Error handled by store
    }
  };

  const handleArchivar = async () => {
    if (window.confirm('¿Archivar logs con más de 1 año de antigüedad?')) {
      try {
        const count = await archivarLogs();
        alert(`Se archivaron ${count} logs`);
        loadLogs();
      } catch (err) {
        alert('Error al archivar logs');
      }
    }
  };

  const formatFecha = (fecha) => {
    if (!fecha) return 'N/A';
    return new Date(fecha).toLocaleString('es-MX', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit'
    });
  };

  const getAccionBadge = (accion) => {
    const map = {
      LOGIN: 'badge-login',
      LOGOUT: 'badge-logout',
      LOGIN_FAILED: 'badge-login-failed',
      CREATE: 'badge-create',
      INSERT: 'badge-create',
      UPDATE: 'badge-update',
      DELETE: 'badge-delete',
    };
    return map[accion] || 'badge-default';
  };

  const renderSimpleLogs = () => (
    <div className="audit-table-container">
      {logsSimples.length === 0 ? (
        <div className="empty-state">No hay logs simples</div>
      ) : (
        <table className="audit-table">
          <thead>
            <tr>
              <th><FaClock /> Fecha</th>
              <th><FaUser /> Usuario</th>
              <th>Acción</th>
              <th>Descripción</th>
              <th>IP</th>
            </tr>
          </thead>
          <tbody>
            {logsSimples.map(log => (
              <tr key={log.id}>
                <td className="fecha-cell">{formatFecha(log.fecha)}</td>
                <td>{log.usuarioEmail || 'N/A'}</td>
                <td>
                  <span className={`badge ${getAccionBadge(log.accion)}`}>
                    {log.accion}
                  </span>
                </td>
                <td className="desc-cell">{log.descripcion || log.accionDescripcion || 'N/A'}</td>
                <td className="ip-cell">{log.ipAddress || 'N/A'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );

  const renderCriticaLogs = () => (
    <div className="audit-table-container">
      {logsCriticos.length === 0 ? (
        <div className="empty-state">No hay logs críticos</div>
      ) : (
        <table className="audit-table">
          <thead>
            <tr>
              <th><FaClock /> Fecha</th>
              <th><FaUser /> Usuario</th>
              <th><FaTable /> Tabla</th>
              <th>Registro ID</th>
              <th>Operación</th>
              <th>Campo</th>
              <th>Valor Anterior</th>
              <th>Valor Nuevo</th>
            </tr>
          </thead>
          <tbody>
            {logsCriticos.map(log => (
              <tr key={log.id}>
                <td className="fecha-cell">{formatFecha(log.fecha)}</td>
                <td>{log.usuarioEmail || 'N/A'}</td>
                <td><span className="badge badge-tabla">{log.tabla}</span></td>
                <td>{log.registroId}</td>
                <td>
                  <span className={`badge ${getAccionBadge(log.operacion)}`}>
                    {log.operacion}
                  </span>
                </td>
                <td>{log.campo || '-'}</td>
                <td className="valor-cell valor-anterior">{log.valorAnterior || '-'}</td>
                <td className="valor-cell valor-nuevo">{log.valorNuevo || '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );

  const renderHistorial = () => (
    <div className="historial-search">
      <div className="search-form">
        <div className="form-group">
          <label>Tabla</label>
          <select
            value={historialSearch.tabla}
            onChange={(e) => setHistorialSearch(prev => ({ ...prev, tabla: e.target.value }))}
          >
            <option value="">Seleccionar tabla...</option>
            <option value="proyectos">Proyectos</option>
            <option value="terrenos">Terrenos</option>
            <option value="clientes">Clientes</option>
            <option value="ventas">Ventas</option>
            <option value="apartados">Apartados</option>
            <option value="cotizaciones">Cotizaciones</option>
            <option value="fases">Fases</option>
          </select>
        </div>
        <div className="form-group">
          <label>ID Registro</label>
          <input
            type="number"
            value={historialSearch.registroId}
            onChange={(e) => setHistorialSearch(prev => ({ ...prev, registroId: e.target.value }))}
            placeholder="Ej: 1"
            min="1"
          />
        </div>
        <button className="btn btn-primary" onClick={handleHistorialSearch}>
          <FaSearch /> Buscar
        </button>
      </div>

      {historial.length > 0 && (
        <div className="audit-table-container">
          <table className="audit-table">
            <thead>
              <tr>
                <th><FaClock /> Fecha</th>
                <th><FaUser /> Usuario</th>
                <th>Campo</th>
                <th>Valor Anterior</th>
                <th>Valor Nuevo</th>
                <th>Operación</th>
              </tr>
            </thead>
            <tbody>
              {historial.map(log => (
                <tr key={log.id}>
                  <td className="fecha-cell">{formatFecha(log.fecha)}</td>
                  <td>{log.usuarioEmail || 'N/A'}</td>
                  <td>{log.campo || '-'}</td>
                  <td className="valor-cell valor-anterior">{log.valorAnterior || '-'}</td>
                  <td className="valor-cell valor-nuevo">{log.valorNuevo || '-'}</td>
                  <td>
                    <span className={`badge ${getAccionBadge(log.operacion)}`}>
                      {log.operacion}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );

  return (
    <div className="auditoria-container">
      <div className="page-header">
        <div>
          <h1><FaShieldAlt /> Auditoría</h1>
          <p>Registro de actividad y cambios del sistema</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={handleArchivar}>
            <FaArchive /> Archivar Logs Antiguos
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Filters (simple & critica tabs) */}
      {activeTab !== 'historial' && (
        <div className="audit-filters">
          <div className="filter-group">
            <label>Usuario ID</label>
            <input
              type="number"
              value={filters.usuarioId}
              onChange={(e) => setFilters(prev => ({ ...prev, usuarioId: e.target.value }))}
              placeholder="ID usuario"
            />
          </div>
          {activeTab === 'simple' && (
            <div className="filter-group">
              <label>Acción</label>
              <select
                value={filters.accion}
                onChange={(e) => setFilters(prev => ({ ...prev, accion: e.target.value }))}
              >
                <option value="">Todas</option>
                <option value="LOGIN">Login</option>
                <option value="LOGOUT">Logout</option>
                <option value="LOGIN_FAILED">Login Fallido</option>
                <option value="EXPORT_PDF">Exportar PDF</option>
                <option value="EXPORT_EXCEL">Exportar Excel</option>
                <option value="UPLOAD_FILE">Subir Archivo</option>
                <option value="DOWNLOAD_FILE">Descargar Archivo</option>
              </select>
            </div>
          )}
          {activeTab === 'critica' && (
            <div className="filter-group">
              <label>Tabla</label>
              <select
                value={filters.tabla}
                onChange={(e) => setFilters(prev => ({ ...prev, tabla: e.target.value }))}
              >
                <option value="">Todas</option>
                <option value="proyectos">Proyectos</option>
                <option value="terrenos">Terrenos</option>
                <option value="clientes">Clientes</option>
                <option value="ventas">Ventas</option>
                <option value="apartados">Apartados</option>
                <option value="cotizaciones">Cotizaciones</option>
              </select>
            </div>
          )}
          <div className="filter-group">
            <label>Desde</label>
            <input
              type="date"
              value={filters.fechaInicio}
              onChange={(e) => setFilters(prev => ({ ...prev, fechaInicio: e.target.value }))}
            />
          </div>
          <div className="filter-group">
            <label>Hasta</label>
            <input
              type="date"
              value={filters.fechaFin}
              onChange={(e) => setFilters(prev => ({ ...prev, fechaFin: e.target.value }))}
            />
          </div>
          <button className="btn btn-primary filter-btn" onClick={loadLogs}>
            <FaSearch /> Filtrar
          </button>
        </div>
      )}

      {/* Tabs */}
      <div className="audit-tabs">
        <button
          className={`tab-btn ${activeTab === 'simple' ? 'active' : ''}`}
          onClick={() => setActiveTab('simple')}
        >
          <FaClock /> Logs Simples
        </button>
        <button
          className={`tab-btn ${activeTab === 'critica' ? 'active' : ''}`}
          onClick={() => setActiveTab('critica')}
        >
          <FaTable /> Logs Críticos
        </button>
        <button
          className={`tab-btn ${activeTab === 'historial' ? 'active' : ''}`}
          onClick={() => setActiveTab('historial')}
        >
          <FaCalendar /> Historial por Registro
        </button>
      </div>

      {/* Content */}
      <div className="audit-content">
        {isLoading ? (
          <div className="loading">Cargando logs...</div>
        ) : (
          <>
            {activeTab === 'simple' && renderSimpleLogs()}
            {activeTab === 'critica' && renderCriticaLogs()}
            {activeTab === 'historial' && renderHistorial()}
          </>
        )}
      </div>
    </div>
  );
}

export default AuditoriaPage;
