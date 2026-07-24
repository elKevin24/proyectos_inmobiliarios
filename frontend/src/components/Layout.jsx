import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import {
  FaHome, FaMap, FaUsers, FaMoneyBillWave, FaSignOutAlt,
  FaBuilding, FaBookmark, FaFileInvoiceDollar, FaCalculator,
  FaShieldAlt, FaChevronDown, FaChevronRight,
  FaBell, FaSun, FaMoon, FaBars, FaTimes
} from 'react-icons/fa';
import { useState, useEffect } from 'react';
import useAuthStore from '../store/authStore';
import api from '../services/api';
import '../styles/Layout.css';

const NAV_GROUPS = [
  {
    label: 'Principal',
    items: [
      { to: '/', icon: <FaHome />, label: 'Dashboard', end: true },
    ],
  },
  {
    label: 'Inventario',
    items: [
      { to: '/proyectos', icon: <FaBuilding />, label: 'Proyectos' },
      { to: '/terrenos', icon: <FaMap />, label: 'Terrenos' },
    ],
  },
  {
    label: 'Clientes y Ventas',
    items: [
      { to: '/clientes', icon: <FaUsers />, label: 'Clientes' },
      { to: '/cotizaciones', icon: <FaFileInvoiceDollar />, label: 'Cotizaciones' },
      { to: '/apartados', icon: <FaBookmark />, label: 'Apartados' },
      { to: '/ventas', icon: <FaMoneyBillWave />, label: 'Ventas' },
    ],
  },
  {
    label: 'Finanzas',
    items: [
      { to: '/planes-pago', icon: <FaCalculator />, label: 'Planes de Pago' },
    ],
  },
  {
    label: 'Administración',
    items: [
      { to: '/auditoria', icon: <FaShieldAlt />, label: 'Auditoría' },
    ],
  },
];

function Layout() {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [collapsed, setCollapsed] = useState({});
  const [notificaciones, setNotificaciones] = useState({ apartadosVencidos: 0 });
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    loadNotificaciones();
    const interval = setInterval(loadNotificaciones, 300000);
    return () => clearInterval(interval);
  }, []);

  const loadNotificaciones = async () => {
    try {
      const response = await api.get('/reportes/dashboard');
      const data = response.data;
      setNotificaciones({
        apartadosVencidos: data?.apartadosVencidos || 0,
      });
    } catch (err) {
      // Silently fail — notifications are non-critical
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(nextTheme);
    document.documentElement.setAttribute('data-theme', nextTheme);
    localStorage.setItem('theme', nextTheme);
  };

  const toggleGroup = (label) =>
    setCollapsed(prev => ({ ...prev, [label]: !prev[label] }));

  // Initials from email or name
  const initials = user?.email
    ? user.email.slice(0, 2).toUpperCase()
    : '??';

  const tenantName = user?.tenantNombre || user?.nombreEmpresa || 'Mi Empresa';

  return (
    <div className="layout">
      {/* Mobile Top Navbar */}
      <header className="mobile-header">
        <button className="menu-toggle" onClick={() => setMobileOpen(!mobileOpen)} title="Menu">
          {mobileOpen ? <FaTimes /> : <FaBars />}
        </button>
        <span className="mobile-brand">Inmobiliaria</span>
        <button className="theme-toggle-btn" onClick={toggleTheme} title="Cambiar tema">
          {theme === 'light' ? <FaMoon /> : <FaSun />}
        </button>
      </header>

      {/* Sidebar Overlay for Mobile */}
      {mobileOpen && <div className="sidebar-overlay" onClick={() => setMobileOpen(false)}></div>}

      <nav className={`sidebar ${mobileOpen ? 'sidebar--open' : ''}`}>
        {/* Header */}
        <div className="sidebar-header">
          <div className="sidebar-brand">
            <div className="brand-icon">🏢</div>
            <div className="brand-text">
              <h2>Inmobiliaria</h2>
              <span className="tenant-name">{tenantName}</span>
            </div>
            {/* Desktop Theme Toggle */}
            <button className="theme-toggle-btn desktop-theme-toggle" onClick={toggleTheme} title="Cambiar tema">
              {theme === 'light' ? <FaMoon /> : <FaSun />}
            </button>
          </div>
          <div className="user-badge">
            <div className="user-avatar">{initials}</div>
            <div className="user-details">
              <span className="user-name">{user?.nombre || 'Usuario'}</span>
              <span className="user-email">{user?.email}</span>
            </div>
            {notificaciones.apartadosVencidos > 0 && (
              <NavLink to="/apartados" className="notification-badge" title="Apartados vencidos" onClick={() => setMobileOpen(false)}>
                <FaBell />
                <span className="notification-count">{notificaciones.apartadosVencidos}</span>
              </NavLink>
            )}
          </div>
        </div>

        {/* Navigation */}
        <nav className="sidebar-nav">
          {NAV_GROUPS.map(group => (
            <div key={group.label} className="nav-group">
              {group.items.length > 1 ? (
                <>
                  <button
                    className="nav-group-toggle"
                    onClick={() => toggleGroup(group.label)}
                  >
                    <span className="group-label">{group.label}</span>
                    <span className="group-chevron">
                      {collapsed[group.label] ? <FaChevronRight /> : <FaChevronDown />}
                    </span>
                  </button>
                  {!collapsed[group.label] && (
                    <ul className="nav-list">
                      {group.items.map(item => (
                        <li key={item.to}>
                          <NavLink
                            to={item.to}
                            end={item.end}
                            className={({ isActive }) =>
                              `nav-link${isActive ? ' nav-link--active' : ''}`
                            }
                            onClick={() => setMobileOpen(false)}
                          >
                            <span className="nav-icon">{item.icon}</span>
                            <span>{item.label}</span>
                          </NavLink>
                        </li>
                      ))}
                    </ul>
                  )}
                </>
              ) : (
                <ul className="nav-list nav-list--flat">
                  {group.items.map(item => (
                    <li key={item.to}>
                      <NavLink
                        to={item.to}
                        end={item.end}
                        className={({ isActive }) =>
                          `nav-link${isActive ? ' nav-link--active' : ''}`
                        }
                        onClick={() => setMobileOpen(false)}
                      >
                        <span className="nav-icon">{item.icon}</span>
                        <span>{item.label}</span>
                      </NavLink>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </nav>

        {/* Footer */}
        <div className="sidebar-footer">
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
