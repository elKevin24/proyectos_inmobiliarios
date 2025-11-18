import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaMap, FaMoneyBillWave, FaUsers, FaChartLine } from 'react-icons/fa';
import terrenoService from '../services/terrenoService';
import '../styles/Dashboard.css';

function Dashboard() {
  const [stats, setStats] = useState({
    totalTerrenos: 0,
    disponibles: 0,
    vendidos: 0,
    apartados: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const terrenos = await terrenoService.getAll();

      const stats = {
        totalTerrenos: terrenos.length,
        disponibles: terrenos.filter(t => t.estado === 'DISPONIBLE').length,
        vendidos: terrenos.filter(t => t.estado === 'VENDIDO').length,
        apartados: terrenos.filter(t => t.estado === 'APARTADO').length,
      };

      setStats(stats);
    } catch (error) {
      console.error('Error loading stats:', error);
    } finally {
      setLoading(false);
    }
  };

  const cards = [
    {
      title: 'Total Terrenos',
      value: stats.totalTerrenos,
      icon: <FaMap />,
      color: '#3498db',
      link: '/terrenos'
    },
    {
      title: 'Disponibles',
      value: stats.disponibles,
      icon: <FaChartLine />,
      color: '#2ecc71',
      link: '/terrenos?estado=DISPONIBLE'
    },
    {
      title: 'Vendidos',
      value: stats.vendidos,
      icon: <FaMoneyBillWave />,
      color: '#e74c3c',
      link: '/ventas'
    },
    {
      title: 'Apartados',
      value: stats.apartados,
      icon: <FaUsers />,
      color: '#f39c12',
      link: '/terrenos?estado=APARTADO'
    },
  ];

  if (loading) {
    return (
      <div className="dashboard">
        <h1>Dashboard</h1>
        <p>Cargando estadísticas...</p>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <p>Resumen de tu sistema inmobiliario</p>
      </div>

      <div className="stats-grid">
        {cards.map((card, index) => (
          <Link to={card.link} key={index} className="stat-card" style={{ borderTopColor: card.color }}>
            <div className="stat-icon" style={{ backgroundColor: card.color }}>
              {card.icon}
            </div>
            <div className="stat-content">
              <h3>{card.title}</h3>
              <p className="stat-value">{card.value}</p>
            </div>
          </Link>
        ))}
      </div>

      <div className="quick-actions">
        <h2>Acciones Rápidas</h2>
        <div className="action-buttons">
          <Link to="/terrenos/nuevo" className="action-btn">
            Agregar Terreno
          </Link>
          <Link to="/ventas/nueva" className="action-btn">
            Nueva Venta
          </Link>
          <Link to="/clientes/nuevo" className="action-btn">
            Agregar Cliente
          </Link>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
