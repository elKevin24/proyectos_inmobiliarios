import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import useVentaStore from '../store/ventaStore';
import useTerrenoStore from '../store/terrenoStore';
import clienteStore from '../store/clienteStore';
import '../styles/Ventas.css';

function VentaForm() {
  const navigate = useNavigate();
  const location = useLocation();
  const { createVenta, isLoading } = useVentaStore();
  const { terrenos, fetchTerrenos } = useTerrenoStore();
  const { clientes, fetchClientes } = clienteStore();
  const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm();

  const [terrenosDisponibles, setTerrenosDisponibles] = useState([]);
  const [selectedTerreno, setSelectedTerreno] = useState(null);

  // Datos del apartado si viene desde conversión
  const apartadoData = location.state || null;

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      await fetchClientes();
      const allTerrenos = await fetchTerrenos();
      // Solo mostrar terrenos disponibles
      const disponibles = allTerrenos.filter(t => t.estado === 'DISPONIBLE');
      setTerrenosDisponibles(disponibles);

      // Si viene desde apartado, pre-llenar campos
      if (apartadoData) {
        if (apartadoData.terrenoId) {
          setValue('terrenoId', apartadoData.terrenoId);
        }
        if (apartadoData.clienteId) {
          setValue('clienteId', apartadoData.clienteId);
        }
        if (apartadoData.montoApartado) {
          setValue('enganche', apartadoData.montoApartado);
        }
      }
    } catch (error) {
      console.error('Error loading data:', error);
    }
  };

  const terrenoId = watch('terrenoId');

  useEffect(() => {
    if (terrenoId) {
      const terreno = terrenosDisponibles.find(t => t.id === parseInt(terrenoId));
      setSelectedTerreno(terreno);
      if (terreno) {
        setValue('montoTotal', terreno.precioFinal);
      }
    }
  }, [terrenoId, terrenosDisponibles]);

  const montoTotal = watch('montoTotal') || 0;
  const enganche = watch('enganche') || 0;
  const montoFinanciado = parseFloat(montoTotal) - parseFloat(enganche);

  const onSubmit = async (data) => {
    try {
      const ventaData = {
        terrenoId: parseInt(data.terrenoId),
        clienteId: parseInt(data.clienteId),
        montoTotal: parseFloat(data.montoTotal),
        enganche: parseFloat(data.enganche),
        montoFinanciado: montoFinanciado,
        metodoPago: data.metodoPago,
        observaciones: data.observaciones || null,
        // Incluir apartadoId si viene desde conversión
        apartadoId: apartadoData?.apartadoId || null,
        // Datos del plan de pago si hay financiamiento
        planPago: montoFinanciado > 0 ? {
          tipoPlan: data.tipoPlan || 'CREDITO',
          numeroCuotas: parseInt(data.numeroCuotas) || 12,
          frecuenciaPago: data.frecuenciaPago || 'MENSUAL',
          tasaInteres: parseFloat(data.tasaInteres) || 0,
          diaPago: parseInt(data.diaPago) || 1
        } : null
      };

      await createVenta(ventaData);
      alert('Venta creada exitosamente');
      navigate('/ventas');
    } catch (error) {
      console.error('Error creating venta:', error);
      alert(error.response?.data?.message || 'Error al crear la venta');
    }
  };

  return (
    <div className="ventas-container">
      <div className="page-header">
        <div>
          <h1>Nueva Venta</h1>
          <p>Registra una nueva transacción de venta</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="form-container">
        <h3>Información de la Venta</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="terrenoId">Terreno *</label>
            <select
              id="terrenoId"
              {...register('terrenoId', { required: 'El terreno es requerido' })}
            >
              <option value="">Seleccionar terreno</option>
              {terrenosDisponibles.map(terreno => (
                <option key={terreno.id} value={terreno.id}>
                  Lote {terreno.numero} - {terreno.area}m² - ${Number(terreno.precioFinal).toLocaleString('es-MX')}
                </option>
              ))}
            </select>
            {errors.terrenoId && <span className="error-message">{errors.terrenoId.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="clienteId">Cliente *</label>
            <select
              id="clienteId"
              {...register('clienteId', { required: 'El cliente es requerido' })}
            >
              <option value="">Seleccionar cliente</option>
              {clientes.map(cliente => (
                <option key={cliente.id} value={cliente.id}>
                  {cliente.nombre} {cliente.apellidos} - {cliente.email}
                </option>
              ))}
            </select>
            {errors.clienteId && <span className="error-message">{errors.clienteId.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="montoTotal">Monto Total *</label>
            <input
              id="montoTotal"
              type="number"
              step="0.01"
              {...register('montoTotal', {
                required: 'El monto total es requerido',
                min: { value: 0, message: 'El monto debe ser mayor a 0' }
              })}
              placeholder="500000.00"
            />
            {errors.montoTotal && <span className="error-message">{errors.montoTotal.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="enganche">Enganche *</label>
            <input
              id="enganche"
              type="number"
              step="0.01"
              {...register('enganche', {
                required: 'El enganche es requerido',
                min: { value: 0, message: 'El enganche debe ser mayor o igual a 0' },
                max: { value: montoTotal, message: 'El enganche no puede ser mayor al monto total' }
              })}
              placeholder="100000.00"
            />
            {errors.enganche && <span className="error-message">{errors.enganche.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="metodoPago">Método de Pago del Enganche *</label>
            <select
              id="metodoPago"
              {...register('metodoPago', { required: 'El método de pago es requerido' })}
            >
              <option value="EFECTIVO">Efectivo</option>
              <option value="TRANSFERENCIA">Transferencia</option>
              <option value="CHEQUE">Cheque</option>
              <option value="TARJETA_CREDITO">Tarjeta de Crédito</option>
              <option value="TARJETA_DEBITO">Tarjeta de Débito</option>
            </select>
            {errors.metodoPago && <span className="error-message">{errors.metodoPago.message}</span>}
          </div>
        </div>

        <div className="monto-info">
          <div className="info-item">
            <span className="label">Monto Total:</span>
            <span className="value">${Number(montoTotal).toLocaleString('es-MX', { minimumFractionDigits: 2 })}</span>
          </div>
          <div className="info-item">
            <span className="label">Enganche:</span>
            <span className="value">${Number(enganche).toLocaleString('es-MX', { minimumFractionDigits: 2 })}</span>
          </div>
          <div className="info-item highlight">
            <span className="label">Monto a Financiar:</span>
            <span className="value">${Number(montoFinanciado).toLocaleString('es-MX', { minimumFractionDigits: 2 })}</span>
          </div>
        </div>

        {montoFinanciado > 0 && (
          <>
            <h3>Plan de Financiamiento</h3>
            <div className="form-grid">
              <div className="form-group">
                <label htmlFor="tipoPlan">Tipo de Plan</label>
                <select
                  id="tipoPlan"
                  {...register('tipoPlan')}
                >
                  <option value="CREDITO">Crédito</option>
                  <option value="APARTADO_CON_FINANCIAMIENTO">Apartado con Financiamiento</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="numeroCuotas">Número de Cuotas</label>
                <input
                  id="numeroCuotas"
                  type="number"
                  {...register('numeroCuotas')}
                  placeholder="12"
                  defaultValue="12"
                />
              </div>

              <div className="form-group">
                <label htmlFor="frecuenciaPago">Frecuencia de Pago</label>
                <select
                  id="frecuenciaPago"
                  {...register('frecuenciaPago')}
                >
                  <option value="SEMANAL">Semanal</option>
                  <option value="QUINCENAL">Quincenal</option>
                  <option value="MENSUAL">Mensual</option>
                  <option value="BIMESTRAL">Bimestral</option>
                  <option value="TRIMESTRAL">Trimestral</option>
                  <option value="SEMESTRAL">Semestral</option>
                  <option value="ANUAL">Anual</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="tasaInteres">Tasa de Interés Anual (%)</label>
                <input
                  id="tasaInteres"
                  type="number"
                  step="0.01"
                  {...register('tasaInteres')}
                  placeholder="10.00"
                  defaultValue="0"
                />
              </div>

              <div className="form-group">
                <label htmlFor="diaPago">Día de Pago</label>
                <input
                  id="diaPago"
                  type="number"
                  min="1"
                  max="31"
                  {...register('diaPago')}
                  placeholder="1"
                  defaultValue="1"
                />
              </div>
            </div>
          </>
        )}

        <div className="form-grid single-column">
          <div className="form-group">
            <label htmlFor="observaciones">Observaciones</label>
            <textarea
              id="observaciones"
              {...register('observaciones')}
              placeholder="Notas adicionales sobre la venta..."
              rows={4}
            />
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/ventas')}
            className="btn btn-secondary"
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Creando...' : 'Crear Venta'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default VentaForm;
