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
import VentasList from './pages/VentasList';

// Placeholder pages
import Proyectos from './pages/Proyectos';
import Clientes from './pages/Clientes';

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
          <Route path="proyectos" element={<Proyectos />} />

          {/* Terrenos */}
          <Route path="terrenos" element={<TerrenosList />} />
          <Route path="terrenos/nuevo" element={<TerrenoForm />} />
          <Route path="terrenos/:id/editar" element={<TerrenoForm />} />

          {/* Clientes */}
          <Route path="clientes" element={<Clientes />} />

          {/* Ventas */}
          <Route path="ventas" element={<VentasList />} />
        </Route>

        {/* Redirect any unknown route to home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
