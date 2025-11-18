import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import pagoService from '../services/pagoService';
import '../styles/Ventas.css';

function PagoForm() {
  const { ventaId } = useParams();
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors } } = useForm();
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = async (data) => {
    try {
      setIsLoading(true);
      const pagoData = {
        ventaId: parseInt(ventaId),
        monto: parseFloat(data.monto),
        metodoPago: data.metodoPago,
        fechaPago: data.fechaPago,
        referencia: data.referencia || null,
        observaciones: data.observaciones || null,
      };

      await pagoService.create(pagoData);
      alert('Pago registrado exitosamente');
      navigate(`/ventas/${ventaId}`);
    } catch (error) {
      console.error('Error creating pago:', error);
      alert(error.response?.data?.message || 'Error al registrar el pago');
    } finally {
      setIsLoading(false);
    }
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="ventas-container">
      <div className="page-header">
        <div>
          <h1>Registrar Pago</h1>
          <p>Registra un nuevo pago para esta venta</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="form-container">
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="monto">Monto del Pago *</label>
            <input
              id="monto"
              type="number"
              step="0.01"
              {...register('monto', {
                required: 'El monto es requerido',
                min: { value: 0.01, message: 'El monto debe ser mayor a 0' }
              })}
              placeholder="5000.00"
            />
            {errors.monto && <span className="error-message">{errors.monto.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="metodoPago">Método de Pago *</label>
            <select
              id="metodoPago"
              {...register('metodoPago', { required: 'El método de pago es requerido' })}
            >
              <option value="">Seleccionar método</option>
              <option value="EFECTIVO">Efectivo</option>
              <option value="TRANSFERENCIA">Transferencia</option>
              <option value="CHEQUE">Cheque</option>
              <option value="TARJETA_CREDITO">Tarjeta de Crédito</option>
              <option value="TARJETA_DEBITO">Tarjeta de Débito</option>
            </select>
            {errors.metodoPago && <span className="error-message">{errors.metodoPago.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="fechaPago">Fecha de Pago *</label>
            <input
              id="fechaPago"
              type="date"
              defaultValue={today}
              {...register('fechaPago', { required: 'La fecha es requerida' })}
            />
            {errors.fechaPago && <span className="error-message">{errors.fechaPago.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="referencia">Referencia/Folio</label>
            <input
              id="referencia"
              type="text"
              {...register('referencia')}
              placeholder="Número de referencia o folio"
            />
          </div>
        </div>

        <div className="form-grid single-column">
          <div className="form-group">
            <label htmlFor="observaciones">Observaciones</label>
            <textarea
              id="observaciones"
              {...register('observaciones')}
              placeholder="Notas adicionales sobre el pago..."
              rows={4}
            />
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate(`/ventas/${ventaId}`)}
            className="btn btn-secondary"
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Registrando...' : 'Registrar Pago'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default PagoForm;
