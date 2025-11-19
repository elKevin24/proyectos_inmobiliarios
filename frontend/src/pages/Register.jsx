import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import useAuthStore from '../store/authStore';
import '../styles/Auth.css';

function Register() {
  const navigate = useNavigate();
  const { register: registerUser, error, isLoading } = useAuthStore();
  const { register, handleSubmit, watch, formState: { errors } } = useForm();
  const [registerError, setRegisterError] = useState('');
  const [success, setSuccess] = useState(false);

  const password = watch('password');

  const onSubmit = async (data) => {
    try {
      setRegisterError('');
      await registerUser({
        email: data.email,
        password: data.password,
        nombre: data.nombre,
        apellidos: data.apellidos,
        telefono: data.telefono,
        nombreEmpresa: data.nombreEmpresa,
      });
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (error) {
      setRegisterError(error.response?.data?.message || 'Error al registrarse');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>Crear Cuenta</h1>
        <p className="auth-subtitle">Regístrate para comenzar a gestionar tus proyectos</p>

        {success && (
          <div className="alert alert-success">
            ¡Registro exitoso! Redirigiendo al login...
          </div>
        )}

        {(registerError || error) && (
          <div className="alert alert-error">
            {registerError || error}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="nombre">Nombre</label>
              <input
                id="nombre"
                type="text"
                {...register('nombre', {
                  required: 'El nombre es requerido'
                })}
                placeholder="Juan"
              />
              {errors.nombre && (
                <span className="error-message">{errors.nombre.message}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="apellidos">Apellidos</label>
              <input
                id="apellidos"
                type="text"
                {...register('apellidos', {
                  required: 'Los apellidos son requeridos'
                })}
                placeholder="Pérez García"
              />
              {errors.apellidos && (
                <span className="error-message">{errors.apellidos.message}</span>
              )}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="email">Correo Electrónico</label>
            <input
              id="email"
              type="email"
              {...register('email', {
                required: 'El correo es requerido',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Correo inválido'
                }
              })}
              placeholder="usuario@ejemplo.com"
            />
            {errors.email && (
              <span className="error-message">{errors.email.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="telefono">Teléfono</label>
            <input
              id="telefono"
              type="tel"
              {...register('telefono', {
                required: 'El teléfono es requerido',
                pattern: {
                  value: /^[0-9]{10}$/,
                  message: 'El teléfono debe tener 10 dígitos'
                }
              })}
              placeholder="5512345678"
            />
            {errors.telefono && (
              <span className="error-message">{errors.telefono.message}</span>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="nombreEmpresa">Nombre de la Empresa</label>
            <input
              id="nombreEmpresa"
              type="text"
              {...register('nombreEmpresa', {
                required: 'El nombre de la empresa es requerido'
              })}
              placeholder="Mi Empresa Inmobiliaria"
            />
            {errors.nombreEmpresa && (
              <span className="error-message">{errors.nombreEmpresa.message}</span>
              )}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="password">Contraseña</label>
              <input
                id="password"
                type="password"
                {...register('password', {
                  required: 'La contraseña es requerida',
                  minLength: {
                    value: 8,
                    message: 'La contraseña debe tener al menos 8 caracteres'
                  }
                })}
                placeholder="••••••••"
              />
              {errors.password && (
                <span className="error-message">{errors.password.message}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirmar Contraseña</label>
              <input
                id="confirmPassword"
                type="password"
                {...register('confirmPassword', {
                  required: 'Confirma tu contraseña',
                  validate: value => value === password || 'Las contraseñas no coinciden'
                })}
                placeholder="••••••••"
              />
              {errors.confirmPassword && (
                <span className="error-message">{errors.confirmPassword.message}</span>
              )}
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading || success}
          >
            {isLoading ? 'Registrando...' : 'Crear Cuenta'}
          </button>
        </form>

        <p className="auth-link">
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión aquí</Link>
        </p>
      </div>
    </div>
  );
}

export default Register;
