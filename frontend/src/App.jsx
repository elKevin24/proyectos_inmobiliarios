import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import useAuthStore from './store/authStore';

// Layout
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';

// Auth Pages
import Login from './pages/Login';
import Register from './pages/Register';

// Main Pages
import Dashboard from './pages/Dashboard';
import TerrenosList from './pages/TerrenosList';
import TerrenoForm from './pages/TerrenoForm';
import TerrenoDetail from './pages/TerrenoDetail';
import VentasList from './pages/VentasList';
import VentaForm from './pages/VentaForm';
import VentaDetail from './pages/VentaDetail';
import PagoForm from './pages/PagoForm';
import ProyectosList from './pages/ProyectosList';
import ProyectoForm from './pages/ProyectoForm';
import ProyectoPlano from './pages/ProyectoPlano';
import ClientesList from './pages/ClientesList';
import ClienteForm from './pages/ClienteForm';

import './App.css';

function App() {
  const { initialize } = useAuthStore();

  useEffect(() => {
    // Initialize auth state from localStorage
    initialize();
  }, [initialize]);

  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected Routes */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />

          {/* Proyectos */}
          <Route path="proyectos" element={<ProyectosList />} />
          <Route path="proyectos/nuevo" element={<ProyectoForm />} />
          <Route path="proyectos/:id/editar" element={<ProyectoForm />} />
          <Route path="proyectos/:id/plano" element={<ProyectoPlano />} />

          {/* Terrenos */}
          <Route path="terrenos" element={<TerrenosList />} />
          <Route path="terrenos/nuevo" element={<TerrenoForm />} />
          <Route path="terrenos/:id" element={<TerrenoDetail />} />
          <Route path="terrenos/:id/editar" element={<TerrenoForm />} />

          {/* Clientes */}
          <Route path="clientes" element={<ClientesList />} />
          <Route path="clientes/nuevo" element={<ClienteForm />} />
          <Route path="clientes/:id/editar" element={<ClienteForm />} />

          {/* Ventas */}
          <Route path="ventas" element={<VentasList />} />
          <Route path="ventas/nueva" element={<VentaForm />} />
          <Route path="ventas/:id" element={<VentaDetail />} />
          <Route path="ventas/:id/pagos/nuevo" element={<PagoForm />} />
        </Route>

        {/* Redirect any unknown route to home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
