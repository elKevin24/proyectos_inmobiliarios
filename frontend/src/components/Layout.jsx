import { useEffect } from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import { FaHome, FaMap, FaUsers, FaMoneyBillWave, FaSignOutAlt, FaBuilding, FaBookmark, FaFileInvoiceDollar, FaCalculator, FaSun, FaMoon } from 'react-icons/fa';
import useAuthStore from '../store/authStore';
import useThemeStore from '../store/themeStore';
import '../styles/Layout.css';

function Layout() {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const { theme, toggleTheme, initTheme } = useThemeStore();

  useEffect(() => {
    initTheme();
  }, [initTheme]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <nav className="sidebar">
        <div className="sidebar-header">
          <h2>Terrenos SaaS</h2>
          <p className="user-info">{user?.email}</p>
        </div>

        <ul className="nav-menu">
          <li>
            <Link to="/">
              <FaHome /> Dashboard
            </Link>
          </li>
          <li>
            <Link to="/proyectos">
              <FaBuilding /> Proyectos
            </Link>
          </li>
          <li>
            <Link to="/terrenos">
              <FaMap /> Terrenos
            </Link>
          </li>
          <li>
            <Link to="/clientes">
              <FaUsers /> Clientes
            </Link>
          </li>
          <li>
            <Link to="/ventas">
              <FaMoneyBillWave /> Ventas
            </Link>
          </li>
          <li>
            <Link to="/apartados">
              <FaBookmark /> Apartados
            </Link>
          </li>
          <li>
            <Link to="/cotizaciones">
              <FaFileInvoiceDollar /> Cotizaciones
            </Link>
          </li>
          <li>
            <Link to="/planes-pago">
              <FaCalculator /> Planes de Pago
            </Link>
          </li>
        </ul>

        <div className="sidebar-footer">
          <button onClick={toggleTheme} className="btn-theme-toggle" title="Cambiar Tema">
            {theme === 'dark' ? <><FaSun /> Modo Claro</> : <><FaMoon /> Modo Oscuro</>}
          </button>
          <button onClick={handleLogout} className="btn-logout">
            <FaSignOutAlt /> Cerrar Sesión
          </button>
        </div>
      </nav>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;
