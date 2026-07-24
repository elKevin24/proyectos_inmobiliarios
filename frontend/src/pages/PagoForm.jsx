import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import pagoService from '../services/pagoService';
import useVentaStore from '../store/ventaStore';
import '../styles/Ventas.css';

function PagoForm() {
  const { ventaId } = useParams();
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors } } = useForm();
  const [isLoading, setIsLoading] = useState(false);
  const { selectedVenta, fetchVentaById } = useVentaStore();

  useEffect(() => {
    if (ventaId) {
      fetchVentaById(ventaId).catch(err => {
        console.error('Error fetching venta:', err);
      });
    }
  }, [ventaId, fetchVentaById]);

  const onSubmit = async (data) => {
    if (!selectedVenta?.planPago?.id) {
      alert('Esta venta no tiene un plan de pagos configurado.');
      return;
    }

    try {
      setIsLoading(true);
      const pagoData = {
        planPagoId: selectedVenta.planPago.id,
        montoPagado: parseFloat(data.monto),
        metodoPago: data.metodoPago,
        fechaPago: data.fechaPago,
        referenciaPago: data.referencia || null,
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
            <label htmlFor="referencia">Referencia/Folio *</label>
            <input
              id="referencia"
              type="text"
              {...register('referencia', {
                validate: (value, formValues) => {
                  if ((formValues.metodoPago === 'TRANSFERENCIA' || formValues.metodoPago === 'CHEQUE') && (!value || !value.trim())) {
                    return 'La referencia es obligatoria para transferencias y cheques';
                  }
                  return true;
                }
              })}
              placeholder="Número de referencia o folio"
            />
            {errors.referencia && <span className="error-message">{errors.referencia.message}</span>}
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
