import { useEffect } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
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
          <div className="brand-lockup">
            <span className="brand-mark"><FaBuilding /></span>
            <div>
              <h2>Terrenos</h2>
              <span className="brand-kicker">Real estate OS</span>
            </div>
          </div>
          <div className="user-chip">
            <span className="user-avatar">{user?.email?.charAt(0).toUpperCase() || 'U'}</span>
            <p className="user-info">{user?.email}</p>
          </div>
        </div>

        <ul className="nav-menu">
          <li>
            <NavLink to="/" end>
              <FaHome /> Dashboard
            </NavLink>
          </li>
          <li>
            <NavLink to="/proyectos" data-testid="nav-proyectos">
              <FaBuilding /> Proyectos
            </NavLink>
          </li>
          <li>
            <NavLink to="/terrenos" data-testid="nav-terrenos">
              <FaMap /> Terrenos
            </NavLink>
          </li>
          <li>
            <NavLink to="/clientes" data-testid="nav-clientes">
              <FaUsers /> Clientes
            </NavLink>
          </li>
          <li>
            <NavLink to="/ventas" data-testid="nav-ventas">
              <FaMoneyBillWave /> Ventas
            </NavLink>
          </li>
          <li>
            <NavLink to="/apartados">
              <FaBookmark /> Apartados
            </NavLink>
          </li>
          <li>
            <NavLink to="/cotizaciones">
              <FaFileInvoiceDollar /> Cotizaciones
            </NavLink>
          </li>
          <li>
            <NavLink to="/planes-pago">
              <FaCalculator /> Planes de Pago
            </NavLink>
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
