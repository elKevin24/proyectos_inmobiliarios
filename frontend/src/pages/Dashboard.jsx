import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  FaMap,
  FaMoneyBillWave,
  FaUsers,
  FaChartLine,
  FaBuilding,
  FaBookmark,
  FaFileInvoiceDollar,
  FaCheckCircle,
  FaExclamationTriangle,
  FaClock,
  FaArrowRight
} from 'react-icons/fa';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar
} from 'recharts';
import toast from 'react-hot-toast';
import reporteService from '../services/reporteService';
import ventaService from '../services/ventaService';
import apartadoService from '../services/apartadoService';
import StatCard from '../components/StatCard';
import '../styles/Dashboard.css';

const PIE_COLORS = ['#2ecc71', '#f1c40f', '#9b59b6'];

function Dashboard() {
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [ventasRecientes, setVentasRecientes] = useState([]);
  const [todasLasVentas, setTodasLasVentas] = useState([]);
  const [apartadosPorVencer, setApartadosPorVencer] = useState([]);
  const [proyectosEstadisticas, setProyectosEstadisticas] = useState([]);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const data = await reporteService.getDashboard();
      setDashboardData(data);

      // Load recent sales, expiring apartados, and project statistics in parallel
      const [ventasData, apartadosData, statsData] = await Promise.allSettled([
        ventaService.getAll(),
        apartadoService.getAll(),
        reporteService.getProyectosEstadisticas()
      ]);

      if (ventasData.status === 'fulfilled') {
        const ventas = Array.isArray(ventasData.value) ? ventasData.value : [];
        setTodasLasVentas(ventas);
        setVentasRecientes(ventas.slice(0, 5));
      }

      if (apartadosData.status === 'fulfilled') {
        const apartados = Array.isArray(apartadosData.value) ? apartadosData.value : [];
        const now = new Date();
        const threeDays = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000);
        const porVencer = apartados.filter(a =>
          a.estado === 'VIGENTE' &&
          a.fechaLimite &&
          new Date(a.fechaLimite) <= threeDays &&
          new Date(a.fechaLimite) >= now
        );
        setApartadosPorVencer(porVencer);
      }

      if (statsData.status === 'fulfilled') {
        setProyectosEstadisticas(Array.isArray(statsData.value) ? statsData.value : []);
      }
    } catch (error) {
      console.error('Error loading dashboard:', error);
      setError('Error al cargar estadísticas');
      toast.error('Error al cargar estadísticas');
    } finally {
      setLoading(false);
    }
  };

  const getVentasMensualesData = (todasLasVentas) => {
    const grouped = {};
    todasLasVentas.forEach(v => {
      if (!v.fechaVenta) return;
      const date = new Date(v.fechaVenta);
      const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      const label = date.toLocaleDateString('es-MX', { year: 'numeric', month: 'short' });
      if (!grouped[key]) {
        grouped[key] = { key, label, total: 0 };
      }
      grouped[key].total += Number(v.montoTotal || 0);
    });
    return Object.values(grouped)
      .sort((a, b) => a.key.localeCompare(b.key))
      .map(item => ({ name: item.label, total: item.total }));
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0
    }).format(amount);
  };

  if (loading) {
    return (
      <div className="dashboard">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Cargando estadísticas...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard">
        <div className="error-container">
          <p>{error}</p>
          <button className="btn btn-primary" onClick={loadDashboardData}>
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  const inventoryData = [
    { name: 'Disponibles', value: dashboardData?.terrenosDisponibles || 0 },
    { name: 'Apartados', value: dashboardData?.terrenosApartados || 0 },
    { name: 'Vendidos', value: dashboardData?.terrenosVendidos || 0 }
  ];

  const salesHistory = getVentasMensualesData(todasLasVentas);

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <p>Resumen general del sistema inmobiliario</p>
      </div>

      {/* Proyectos, Terrenos, Clientes Cards */}
      <div className="dashboard-section">
        <div className="stats-grid">
          <StatCard
            title="Proyectos Activos"
            value={dashboardData?.totalProyectos || 0}
            icon="🏢"
            color="blue"
            subtitle={`${dashboardData?.proyectosActivos || 0} activos`}
          />
          <StatCard
            title="Total Terrenos"
            value={dashboardData?.totalTerrenos || 0}
            icon="📍"
            color="blue"
            subtitle="Inventario total"
          />
          <StatCard
            title="Total Clientes"
            value={dashboardData?.totalClientes || 0}
            icon="👥"
            color="teal"
            subtitle={`${dashboardData?.clientesActivos || 0} activos`}
          />
          <StatCard
            title="Avance de Ventas"
            value={`${(dashboardData?.porcentajeAvanceVentas || 0).toFixed(1)}%`}
            icon="📈"
            color="green"
            subtitle="Del inventario total"
          />
        </div>
      </div>

      {/* Ventas y Finanzas */}
      <div className="dashboard-section">
        <h2 className="section-title">
          <FaMoneyBillWave /> Finanzas de Ventas
        </h2>
        <div className="stats-grid">
          <StatCard
            title="Total Ventas"
            value={dashboardData?.totalVentas || 0}
            icon="💵"
            color="green"
            subtitle={`${dashboardData?.ventasPagadas || 0} pagadas`}
          />
          <StatCard
            title="Monto Total Ventas"
            value={formatCurrency(dashboardData?.montoTotalVentas || 0)}
            icon="💰"
            color="purple"
          />
          <StatCard
            title="Comisiones Generadas"
            value={formatCurrency(dashboardData?.montoComisiones || dashboardData?.montoTotalComisiones || 0)}
            icon="📊"
            color="blue"
          />
          <StatCard
            title="Ticket Promedio"
            value={formatCurrency(dashboardData?.ticketPromedio || 0)}
            icon="🎟️"
            color="orange"
          />
        </div>
      </div>

      {/* SECCIÓN DE GRÁFICOS */}
      <div className="dashboard-section">
        <h2 className="section-title">
          <FaChartLine /> Métricas y Análisis Visual
        </h2>
        <div className="charts-grid">
          <div className="chart-card">
            <h3>Historial de Ventas Mensuales</h3>
            <div className="chart-container">
              {salesHistory.length === 0 ? (
                <div className="empty-chart">Sin historial de ventas disponible</div>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={salesHistory} margin={{ top: 10, right: 30, left: 10, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)" />
                    <XAxis dataKey="name" stroke="var(--text-muted)" />
                    <YAxis tickFormatter={(v) => `$${v.toLocaleString('es-MX')}`} stroke="var(--text-muted)" />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border-color)', color: 'var(--text-main)' }}
                      formatter={(v) => [`$${Number(v).toLocaleString('es-MX')}`, 'Monto Ventas']}
                    />
                    <Legend />
                    <Line type="monotone" dataKey="total" stroke="#3498db" strokeWidth={3} activeDot={{ r: 8 }} name="Monto total" />
                  </LineChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          <div className="chart-card">
            <h3>Distribución General de Inventario</h3>
            <div className="chart-container">
              {dashboardData?.totalTerrenos === 0 ? (
                <div className="empty-chart">Sin terrenos registrados</div>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={inventoryData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    >
                      {inventoryData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip
                      contentStyle={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border-color)', color: 'var(--text-main)' }}
                      formatter={(v) => [v, 'Terrenos']}
                    />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          <div className="chart-card">
            <h3>Estado de Lotes por Proyecto</h3>
            <div className="chart-container">
              {proyectosEstadisticas.length === 0 ? (
                <div className="empty-chart">Sin datos de proyectos</div>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={proyectosEstadisticas} margin={{ top: 10, right: 30, left: 10, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)" />
                    <XAxis dataKey="proyectoNombre" stroke="var(--text-muted)" />
                    <YAxis stroke="var(--text-muted)" />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border-color)', color: 'var(--text-main)' }}
                    />
                    <Legend />
                    <Bar dataKey="terrenosDisponibles" fill="#2ecc71" name="Disponibles" stackId="a" />
                    <Bar dataKey="terrenosApartados" fill="#f1c40f" name="Apartados" stackId="a" />
                    <Bar dataKey="terrenosVendidos" fill="#9b59b6" name="Vendidos" stackId="a" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          <div className="chart-card">
            <h3>Ticket Promedio por Proyecto</h3>
            <div className="chart-container">
              {proyectosEstadisticas.length === 0 ? (
                <div className="empty-chart">Sin datos de proyectos</div>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={proyectosEstadisticas} margin={{ top: 10, right: 30, left: 10, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="var(--border-color)" />
                    <XAxis dataKey="proyectoNombre" stroke="var(--text-muted)" />
                    <YAxis tickFormatter={(v) => `$${v.toLocaleString('es-MX')}`} stroke="var(--text-muted)" />
                    <Tooltip
                      contentStyle={{ backgroundColor: 'var(--bg-card)', borderColor: 'var(--border-color)', color: 'var(--text-main)' }}
                      formatter={(v) => [`$${Number(v).toLocaleString('es-MX')}`, 'Ticket Promedio']}
                    />
                    <Legend />
                    <Bar dataKey="ticketPromedio" fill="#e74c3c" name="Valor Promedio" />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Alertas de Apartados por Vencer */}
      {apartadosPorVencer.length > 0 && (
        <div className="dashboard-alerts">
          <h2 className="section-title">
            <FaExclamationTriangle /> Alertas - Apartados por Vencer
          </h2>
          <div className="alerts-list">
            {apartadosPorVencer.map(a => (
              <div key={a.id} className="alert-card alert-warning">
                <FaClock className="alert-icon" />
                <div className="alert-content">
                  <span className="alert-title">
                    Apartado #{a.id} — {a.clienteNombre || 'Cliente'}
                  </span>
                  <span className="alert-subtitle">
                    Vence: {new Date(a.fechaLimite).toLocaleDateString('es-MX')} — {a.terrenoNumeroLote || 'Terreno'}
                  </span>
                </div>
                <Link to={`/apartados/${a.id}`} className="btn btn-sm btn-secondary">
                  Ver <FaArrowRight />
                </Link>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Ventas Recientes */}
      {ventasRecientes.length > 0 && (
        <div className="dashboard-section">
          <h2 className="section-title">
            <FaMoneyBillWave /> Últimas Ventas
          </h2>
          <div className="recent-table-container">
            <table className="recent-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Proyecto</th>
                  <th>Terreno</th>
                  <th>Monto</th>
                  <th>Estado</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                {ventasRecientes.map(venta => (
                  <tr key={venta.id}>
                    <td>
                      <Link to={`/ventas/${venta.id}`}>#{venta.id}</Link>
                    </td>
                    <td>{venta.proyectoNombre || 'N/A'}</td>
                    <td>{venta.terrenoNumeroLote || 'N/A'}</td>
                    <td className="monto-cell">{formatCurrency(venta.montoTotal)}</td>
                    <td>
                      <span className={`badge badge-${venta.estado?.toLowerCase() || 'default'}`}>
                        {venta.estado}
                      </span>
                    </td>
                    <td>{venta.fechaVenta ? new Date(venta.fechaVenta).toLocaleDateString('es-MX') : 'N/A'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="section-footer">
            <Link to="/ventas" className="link-more">
              Ver todas las ventas <FaArrowRight />
            </Link>
          </div>
        </div>
      )}

      {/* Acciones Rápidas */}
      <div className="quick-actions">
        <h2 className="section-title">Acciones Rápidas</h2>
        <div className="action-buttons">
          <Link to="/proyectos/nuevo" className="action-btn action-btn-blue">
            <FaBuilding /> Nuevo Proyecto
          </Link>
          <Link to="/terrenos/nuevo" className="action-btn action-btn-green">
            <FaMap /> Agregar Terreno
          </Link>
          <Link to="/clientes/nuevo" className="action-btn action-btn-teal">
            <FaUsers /> Nuevo Cliente
          </Link>
          <Link to="/cotizaciones/nueva" className="action-btn action-btn-purple">
            <FaFileInvoiceDollar /> Nueva Cotización
          </Link>
          <Link to="/apartados/nuevo" className="action-btn action-btn-orange">
            <FaBookmark /> Nuevo Apartado
          </Link>
          <Link to="/ventas/nueva" className="action-btn action-btn-success">
            <FaMoneyBillWave /> Nueva Venta
          </Link>
        </div>
      </div>

      {/* Enlaces de Navegación Rápida */}
      <div className="quick-links">
        <h2 className="section-title">Navegación Rápida</h2>
        <div className="links-grid">
          <Link to="/proyectos" className="quick-link">
            <FaBuilding />
            <span>Ver Proyectos</span>
          </Link>
          <Link to="/terrenos" className="quick-link">
            <FaMap />
            <span>Ver Terrenos</span>
          </Link>
          <Link to="/clientes" className="quick-link">
            <FaUsers />
            <span>Ver Clientes</span>
          </Link>
          <Link to="/ventas" className="quick-link">
            <FaMoneyBillWave />
            <span>Ver Ventas</span>
          </Link>
          <Link to="/apartados" className="quick-link">
            <FaBookmark />
            <span>Ver Apartados</span>
          </Link>
          <Link to="/cotizaciones" className="quick-link">
            <FaFileInvoiceDollar />
            <span>Ver Cotizaciones</span>
          </Link>
          <Link to="/planes-pago" className="quick-link">
            <FaChartLine />
            <span>Planes de Pago</span>
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
