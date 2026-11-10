import { useEffect } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import {
  FaHome,
  FaMap,
  FaUsers,
  FaMoneyBillWave,
  FaSignOutAlt,
  FaBuilding,
  FaBookmark,
  FaFileInvoiceDollar,
  FaCalculator,
  FaShieldAlt,
  FaSun,
  FaMoon
} from 'react-icons/fa';
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

  const userInitial = user?.nombre?.charAt(0)?.toUpperCase() || user?.email?.charAt(0)?.toUpperCase() || 'U';

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
            <span className="user-avatar">{userInitial}</span>
            <p className="user-info">{user?.email || 'Usuario'}</p>
          </div>
        </div>

        <ul className="nav-menu">
          <li>
            <NavLink to="/" end data-testid="nav-dashboard">
              <FaHome /> <span>Dashboard</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/proyectos" data-testid="nav-proyectos">
              <FaBuilding /> <span>Proyectos</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/terrenos" data-testid="nav-terrenos">
              <FaMap /> <span>Terrenos</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/clientes" data-testid="nav-clientes">
              <FaUsers /> <span>Clientes</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/ventas" data-testid="nav-ventas">
              <FaMoneyBillWave /> <span>Ventas</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/apartados" data-testid="nav-apartados">
              <FaBookmark /> <span>Apartados</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/cotizaciones" data-testid="nav-cotizaciones">
              <FaFileInvoiceDollar /> <span>Cotizaciones</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/planes-pago" data-testid="nav-planes-pago">
              <FaCalculator /> <span>Planes de Pago</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/pagos" data-testid="nav-pagos">
              <FaMoneyBillWave /> <span>Pagos</span>
            </NavLink>
          </li>
          <li>
            <NavLink to="/auditoria" data-testid="nav-auditoria">
              <FaShieldAlt /> <span>Auditoría</span>
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
