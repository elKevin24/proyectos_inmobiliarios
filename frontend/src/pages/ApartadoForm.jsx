import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import useApartadoStore from '../store/apartadoStore';
import useTerrenoStore from '../store/terrenoStore';
import useClienteStore from '../store/clienteStore';
import '../styles/Apartados.css';

const ApartadoForm = () => {
  const navigate = useNavigate();
  const { createApartado, isLoading, error } = useApartadoStore();
  const { terrenos, fetchTerrenos } = useTerrenoStore();
  const { clientes, fetchClientes } = useClienteStore();

  const [tipoMonto, setTipoMonto] = useState('porcentaje'); // 'porcentaje' o 'fijo'
  const [terrenoSeleccionado, setTerrenoSeleccionado] = useState(null);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors }
  } = useForm({
    defaultValues: {
      terrenoId: '',
      clienteId: '',
      porcentajeApartado: 10,
      montoApartado: '',
      diasVigencia: 30,
      observaciones: ''
    }
  });

  useEffect(() => {
    // Cargar terrenos disponibles
    fetchTerrenos({ estado: 'DISPONIBLE' });
    // Cargar clientes activos
    fetchClientes({ estado: 'ACTIVO' });
  }, []);

  const terrenoId = watch('terrenoId');
  const porcentajeApartado = watch('porcentajeApartado');

  // Actualizar terreno seleccionado cuando cambia
  useEffect(() => {
    if (terrenoId) {
      const terreno = terrenos.find(t => t.id === parseInt(terrenoId));
      setTerrenoSeleccionado(terreno);

      // Si está en modo porcentaje, calcular monto automáticamente
      if (tipoMonto === 'porcentaje' && terreno) {
        const monto = (terreno.precioFinal * porcentajeApartado) / 100;
        setValue('montoApartado', monto.toFixed(2));
      }
    }
  }, [terrenoId, porcentajeApartado, tipoMonto, terrenos]);

  const onSubmit = async (data) => {
    try {
      const apartadoData = {
        terrenoId: parseInt(data.terrenoId),
        clienteId: parseInt(data.clienteId),
        diasVigencia: parseInt(data.diasVigencia),
        observaciones: data.observaciones || null
      };

      // Enviar monto o porcentaje según el tipo seleccionado
      if (tipoMonto === 'porcentaje') {
        apartadoData.porcentajeApartado = parseFloat(data.porcentajeApartado);
      } else {
        apartadoData.montoApartado = parseFloat(data.montoApartado);
      }

      await createApartado(apartadoData);
      navigate('/apartados');
    } catch (error) {
      console.error('Error al crear apartado:', error);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(amount);
  };

  return (
    <div className="form-container">
      <div className="form-header">
        <h1>Nuevo Apartado</h1>
        <button
          className="btn btn-secondary"
          onClick={() => navigate('/apartados')}
        >
          Cancelar
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="apartado-form">
        {/* Información del Terreno */}
        <div className="form-section">
          <h3>Información del Terreno</h3>

          <div className="form-group">
            <label htmlFor="terrenoId">
              Terreno * <span className="field-hint">(Solo terrenos disponibles)</span>
            </label>
            <select
              id="terrenoId"
              {...register('terrenoId', { required: 'Seleccione un terreno' })}
              className={errors.terrenoId ? 'error' : ''}
            >
              <option value="">Seleccione un terreno</option>
              {terrenos.map((terreno) => (
                <option key={terreno.id} value={terreno.id}>
                  {terreno.numeroLote} - {terreno.proyectoNombre || 'Sin proyecto'} -{' '}
                  {formatCurrency(terreno.precioFinal)}
                </option>
              ))}
            </select>
            {errors.terrenoId && (
              <span className="error-message">{errors.terrenoId.message}</span>
            )}
          </div>

          {terrenoSeleccionado && (
            <div className="info-box">
              <p><strong>Precio del terreno:</strong> {formatCurrency(terrenoSeleccionado.precioFinal)}</p>
              <p><strong>Área:</strong> {terrenoSeleccionado.area} m²</p>
              {terrenoSeleccionado.manzana && (
                <p><strong>Manzana:</strong> {terrenoSeleccionado.manzana}</p>
              )}
            </div>
          )}
        </div>

        {/* Información del Cliente */}
        <div className="form-section">
          <h3>Información del Cliente</h3>

          <div className="form-group">
            <label htmlFor="clienteId">Cliente *</label>
            <select
              id="clienteId"
              {...register('clienteId', { required: 'Seleccione un cliente' })}
              className={errors.clienteId ? 'error' : ''}
            >
              <option value="">Seleccione un cliente</option>
              {clientes.map((cliente) => (
                <option key={cliente.id} value={cliente.id}>
                  {cliente.nombreCompleto} - {cliente.telefono}
                </option>
              ))}
            </select>
            {errors.clienteId && (
              <span className="error-message">{errors.clienteId.message}</span>
            )}
          </div>
        </div>

        {/* Configuración del Apartado */}
        <div className="form-section">
          <h3>Configuración del Apartado</h3>

          <div className="form-group">
            <label>Tipo de Monto</label>
            <div className="radio-group">
              <label className="radio-label">
                <input
                  type="radio"
                  checked={tipoMonto === 'porcentaje'}
                  onChange={() => setTipoMonto('porcentaje')}
                />
                Porcentaje del precio
              </label>
              <label className="radio-label">
                <input
                  type="radio"
                  checked={tipoMonto === 'fijo'}
                  onChange={() => setTipoMonto('fijo')}
                />
                Monto fijo
              </label>
            </div>
          </div>

          {tipoMonto === 'porcentaje' ? (
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="porcentajeApartado">Porcentaje de Apartado * (%)</label>
                <input
                  type="number"
                  id="porcentajeApartado"
                  step="0.01"
                  min="1"
                  max="100"
                  {...register('porcentajeApartado', {
                    required: 'El porcentaje es requerido',
                    min: { value: 1, message: 'Mínimo 1%' },
                    max: { value: 100, message: 'Máximo 100%' }
                  })}
                  className={errors.porcentajeApartado ? 'error' : ''}
                />
                {errors.porcentajeApartado && (
                  <span className="error-message">{errors.porcentajeApartado.message}</span>
                )}
              </div>

              <div className="form-group">
                <label>Monto Calculado</label>
                <input
                  type="text"
                  value={watch('montoApartado') ? formatCurrency(watch('montoApartado')) : '-'}
                  disabled
                  className="calculated-field"
                />
              </div>
            </div>
          ) : (
            <div className="form-group">
              <label htmlFor="montoApartado">Monto de Apartado * ($)</label>
              <input
                type="number"
                id="montoApartado"
                step="0.01"
                min="0"
                {...register('montoApartado', {
                  required: 'El monto es requerido',
                  min: { value: 0, message: 'Monto inválido' }
                })}
                className={errors.montoApartado ? 'error' : ''}
              />
              {errors.montoApartado && (
                <span className="error-message">{errors.montoApartado.message}</span>
              )}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="diasVigencia">
              Días de Vigencia * <span className="field-hint">(1-365 días)</span>
            </label>
            <input
              type="number"
              id="diasVigencia"
              min="1"
              max="365"
              {...register('diasVigencia', {
                required: 'Los días de vigencia son requeridos',
                min: { value: 1, message: 'Mínimo 1 día' },
                max: { value: 365, message: 'Máximo 365 días' }
              })}
              className={errors.diasVigencia ? 'error' : ''}
            />
            {errors.diasVigencia && (
              <span className="error-message">{errors.diasVigencia.message}</span>
            )}
            <small className="field-hint">
              El apartado vencerá automáticamente después de este período
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="observaciones">Observaciones</label>
            <textarea
              id="observaciones"
              rows="4"
              {...register('observaciones')}
              placeholder="Notas adicionales sobre el apartado..."
            />
          </div>
        </div>

        {/* Botones */}
        <div className="form-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => navigate('/apartados')}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Creando...' : 'Crear Apartado'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ApartadoForm;
