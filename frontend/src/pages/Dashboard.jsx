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
  FaExclamationTriangle
} from 'react-icons/fa';
import reporteService from '../services/reporteService';
import StatCard from '../components/StatCard';
import '../styles/Dashboard.css';

function Dashboard() {
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const data = await reporteService.getDashboard();
      setDashboardData(data);
    } catch (error) {
      console.error('Error loading dashboard:', error);
      setError('Error al cargar estadísticas');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    const num = Number(amount);
    const validAmount = isNaN(num) ? 0 : num;
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
      minimumFractionDigits: 0
    }).format(validAmount);
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

  const stats = dashboardData || {};

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <span className="eyebrow">Resumen ejecutivo</span>
          <h1>Buenos días, equipo</h1>
          <p>Una vista clara del rendimiento de tu operación inmobiliaria.</p>
        </div>
        <div className="dashboard-status"><span /> Datos actualizados</div>
      </div>

      {/* Proyectos */}
      <div className="dashboard-section">
        <h2 className="section-title">
          <FaBuilding /> Proyectos
        </h2>
        <div className="stats-grid">
          <StatCard
            title="Total Proyectos"
            value={stats.totalProyectos || 0}
            icon={<FaBuilding />}
            color="blue"
            subtitle={`${stats.proyectosActivos || 0} activos`}
          />
        </div>
      </div>

      {/* Terrenos */}
      <div className="dashboard-section">
        <h2 className="section-title">
          <FaMap /> Inventario de Terrenos
        </h2>
        <div className="stats-grid">
          <StatCard
            title="Total Terrenos"
            value={stats.totalTerrenos || 0}
            icon={<FaMap />}
            color="blue"
          />
          <StatCard
            title="Disponibles"
            value={stats.terrenosDisponibles || 0}
            icon={<FaCheckCircle />}
            color="green"
            subtitle="Listos para venta"
          />
          <StatCard
            title="Apartados"
            value={stats.terrenosApartados || 0}
            icon={<FaBookmark />}
            color="orange"
            subtitle="En reserva"
          />
          <StatCard
            title="Vendidos"
            value={stats.terrenosVendidos || 0}
            icon={<FaMoneyBillWave />}
            color="purple"
            subtitle="Transacciones completadas"
          />
        </div>
      </div>

      {/* Clientes */}
      <div className="dashboard-section">
        <h2 className="section-title">
          <FaUsers /> Clientes
        </h2>
        <div className="stats-grid">
          <StatCard
            title="Total Clientes"
            value={stats.totalClientes || 0}
            icon={<FaUsers />}
            color="teal"
            subtitle={`${stats.clientesActivos || 0} activos`}
          />
        </div>
      </div>

      {/* Ventas y Finanzas */}
      <div className="dashboard-section">
        <h2 className="section-title">
          <FaMoneyBillWave /> Ventas y Finanzas
        </h2>
        <div className="stats-grid">
          <StatCard
            title="Total Ventas"
            value={stats.totalVentas || 0}
            icon={<FaMoneyBillWave />}
            color="green"
            subtitle={`${stats.ventasPagadas || 0} pagadas`}
          />
          <StatCard
            title="Monto Total Ventas"
            value={formatCurrency(stats.montoTotalVentas || 0)}
            icon={<FaMoneyBillWave />}
            color="purple"
          />
          <StatCard
            title="Comisiones Generadas"
            value={formatCurrency(stats.montoTotalComisiones || stats.montoComisiones || 0)}
            icon={<FaChartLine />}
            color="blue"
          />
          <StatCard
            title="Avance de Ventas"
            value={`${Number(stats.porcentajeOcupacion || stats.porcentajeAvanceVentas || 0).toFixed(1)}%`}
            icon={<FaChartLine />}
            color="green"
            subtitle="Del inventario total"
          />
        </div>
      </div>

      {/* Apartados */}
      <div className="dashboard-section">
        <h2 className="section-title">
          <FaBookmark /> Apartados
        </h2>
        <div className="stats-grid">
          <StatCard
            title="Total Apartados"
            value={stats.totalApartados || 0}
            icon={<FaBookmark />}
            color="orange"
          />
          <StatCard
            title="Vigentes"
            value={stats.apartadosVigentes || 0}
            icon={<FaCheckCircle />}
            color="green"
            subtitle="Activos"
          />
          <StatCard
            title="Vencidos"
            value={stats.apartadosVencidos || 0}
            icon={<FaExclamationTriangle />}
            color="red"
            subtitle="Requieren atención"
          />
        </div>
      </div>

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
