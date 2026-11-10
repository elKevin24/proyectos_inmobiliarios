import { Navigate } from 'react-router-dom';
import useAuthStore, { getInitialAuthState } from '../store/authStore';

function ProtectedRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuthStore();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  // Si el store confirma autenticación, permitir acceso
  if (isAuthenticated) {
    return children;
  }

  // Verificación defensiva directa en caso de desincronización
  const fallbackAuth = getInitialAuthState();
  if (fallbackAuth.isAuthenticated) {
    return children;
  }

  return <Navigate to="/login" replace />;
}

export default ProtectedRoute;
