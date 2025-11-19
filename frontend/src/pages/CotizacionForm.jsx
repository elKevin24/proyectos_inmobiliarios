import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import useCotizacionStore from '../store/cotizacionStore';
import useTerrenoStore from '../store/terrenoStore';
import '../styles/Cotizaciones.css';

const CotizacionForm = () => {
  const navigate = useNavigate();
  const { createCotizacion, isLoading, error } = useCotizacionStore();
  const { terrenos, fetchTerrenos } = useTerrenoStore();

  const [terrenoSeleccionado, setTerrenoSeleccionado] = useState(null);
  const [tipoDescuento, setTipoDescuento] = useState('porcentaje'); // 'porcentaje' o 'fijo'

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors }
  } = useForm({
    defaultValues: {
      terrenoId: '',
      clienteNombre: '',
      clienteEmail: '',
      clienteTelefono: '',
      precioBase: '',
      descuento: 0,
      porcentajeDescuento: 0,
      precioFinal: '',
      fechaVigencia: '',
      observaciones: ''
    }
  });

  useEffect(() => {
    // Cargar terrenos disponibles
    fetchTerrenos({ estado: 'DISPONIBLE' });
  }, []);

  const terrenoId = watch('terrenoId');
  const precioBase = watch('precioBase') || 0;
  const porcentajeDescuento = watch('porcentajeDescuento') || 0;
  const descuento = watch('descuento') || 0;

  // Actualizar terreno seleccionado cuando cambia
  useEffect(() => {
    if (terrenoId) {
      const terreno = terrenos.find(t => t.id === parseInt(terrenoId));
      setTerrenoSeleccionado(terreno);

      if (terreno) {
        setValue('precioBase', terreno.precioFinal);
      }
    }
  }, [terrenoId, terrenos]);

  // Calcular precio final según tipo de descuento
  useEffect(() => {
    let descuentoCalculado = 0;
    let precioFinalCalculado = parseFloat(precioBase) || 0;

    if (tipoDescuento === 'porcentaje' && porcentajeDescuento > 0) {
      descuentoCalculado = (precioFinalCalculado * parseFloat(porcentajeDescuento)) / 100;
      setValue('descuento', descuentoCalculado.toFixed(2));
      precioFinalCalculado = precioFinalCalculado - descuentoCalculado;
    } else if (tipoDescuento === 'fijo' && descuento > 0) {
      descuentoCalculado = parseFloat(descuento);
      const porcentaje = (descuentoCalculado / precioFinalCalculado) * 100;
      setValue('porcentajeDescuento', porcentaje.toFixed(2));
      precioFinalCalculado = precioFinalCalculado - descuentoCalculado;
    }

    setValue('precioFinal', precioFinalCalculado.toFixed(2));
  }, [precioBase, porcentajeDescuento, descuento, tipoDescuento]);

  // Fecha mínima (mañana)
  const getMinDate = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  };

  const onSubmit = async (data) => {
    try {
      const cotizacionData = {
        terrenoId: parseInt(data.terrenoId),
        clienteNombre: data.clienteNombre,
        clienteEmail: data.clienteEmail || null,
        clienteTelefono: data.clienteTelefono || null,
        precioBase: parseFloat(data.precioBase),
        descuento: parseFloat(data.descuento) || 0,
        porcentajeDescuento: parseFloat(data.porcentajeDescuento) || 0,
        precioFinal: parseFloat(data.precioFinal),
        fechaVigencia: data.fechaVigencia,
        observaciones: data.observaciones || null
      };

      await createCotizacion(cotizacionData);
      navigate('/cotizaciones');
    } catch (error) {
      console.error('Error al crear cotización:', error);
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
        <h1>Nueva Cotización</h1>
        <button
          className="btn btn-secondary"
          onClick={() => navigate('/cotizaciones')}
        >
          Cancelar
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="cotizacion-form">
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
                  {formatCurrency(terreno.precioFinal)} - {terreno.area} m²
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
              <p><strong>Proyecto:</strong> {terrenoSeleccionado.proyectoNombre || 'Sin proyecto'}</p>
            </div>
          )}
        </div>

        {/* Información del Cliente */}
        <div className="form-section">
          <h3>Información del Cliente</h3>

          <div className="form-group">
            <label htmlFor="clienteNombre">Nombre del Cliente *</label>
            <input
              type="text"
              id="clienteNombre"
              {...register('clienteNombre', {
                required: 'El nombre es requerido',
                maxLength: { value: 200, message: 'Máximo 200 caracteres' }
              })}
              className={errors.clienteNombre ? 'error' : ''}
            />
            {errors.clienteNombre && (
              <span className="error-message">{errors.clienteNombre.message}</span>
            )}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="clienteEmail">Email</label>
              <input
                type="email"
                id="clienteEmail"
                {...register('clienteEmail', {
                  pattern: {
                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                    message: 'Email inválido'
                  }
                })}
                className={errors.clienteEmail ? 'error' : ''}
              />
              {errors.clienteEmail && (
                <span className="error-message">{errors.clienteEmail.message}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="clienteTelefono">Teléfono</label>
              <input
                type="tel"
                id="clienteTelefono"
                {...register('clienteTelefono', {
                  maxLength: { value: 20, message: 'Máximo 20 caracteres' }
                })}
                className={errors.clienteTelefono ? 'error' : ''}
              />
              {errors.clienteTelefono && (
                <span className="error-message">{errors.clienteTelefono.message}</span>
              )}
            </div>
          </div>
        </div>

        {/* Información de Precios */}
        <div className="form-section">
          <h3>Información de Precios</h3>

          <div className="form-group">
            <label htmlFor="precioBase">Precio Base * ($)</label>
            <input
              type="number"
              id="precioBase"
              step="0.01"
              min="0"
              {...register('precioBase', {
                required: 'El precio base es requerido',
                min: { value: 0, message: 'Precio inválido' }
              })}
              className={errors.precioBase ? 'error calculated-field' : 'calculated-field'}
              disabled
            />
            {errors.precioBase && (
              <span className="error-message">{errors.precioBase.message}</span>
            )}
          </div>

          <div className="form-group">
            <label>Tipo de Descuento</label>
            <div className="radio-group">
              <label className="radio-label">
                <input
                  type="radio"
                  checked={tipoDescuento === 'porcentaje'}
                  onChange={() => setTipoDescuento('porcentaje')}
                />
                Porcentaje
              </label>
              <label className="radio-label">
                <input
                  type="radio"
                  checked={tipoDescuento === 'fijo'}
                  onChange={() => setTipoDescuento('fijo')}
                />
                Monto fijo
              </label>
            </div>
          </div>

          {tipoDescuento === 'porcentaje' ? (
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="porcentajeDescuento">Porcentaje de Descuento (%)</label>
                <input
                  type="number"
                  id="porcentajeDescuento"
                  step="0.01"
                  min="0"
                  max="100"
                  {...register('porcentajeDescuento', {
                    min: { value: 0, message: 'Mínimo 0%' },
                    max: { value: 100, message: 'Máximo 100%' }
                  })}
                  className={errors.porcentajeDescuento ? 'error' : ''}
                />
                {errors.porcentajeDescuento && (
                  <span className="error-message">{errors.porcentajeDescuento.message}</span>
                )}
              </div>

              <div className="form-group">
                <label>Descuento Calculado ($)</label>
                <input
                  type="text"
                  value={formatCurrency(descuento)}
                  disabled
                  className="calculated-field"
                />
              </div>
            </div>
          ) : (
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="descuento">Descuento ($)</label>
                <input
                  type="number"
                  id="descuento"
                  step="0.01"
                  min="0"
                  {...register('descuento', {
                    min: { value: 0, message: 'Mínimo 0' }
                  })}
                  className={errors.descuento ? 'error' : ''}
                />
                {errors.descuento && (
                  <span className="error-message">{errors.descuento.message}</span>
                )}
              </div>

              <div className="form-group">
                <label>Porcentaje Calculado (%)</label>
                <input
                  type="text"
                  value={`${porcentajeDescuento}%`}
                  disabled
                  className="calculated-field"
                />
              </div>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="precioFinal">Precio Final * ($)</label>
            <input
              type="number"
              id="precioFinal"
              step="0.01"
              min="0"
              {...register('precioFinal', {
                required: 'El precio final es requerido',
                min: { value: 0, message: 'Precio inválido' }
              })}
              className={errors.precioFinal ? 'error calculated-field' : 'calculated-field'}
              disabled
            />
            {errors.precioFinal && (
              <span className="error-message">{errors.precioFinal.message}</span>
            )}
          </div>
        </div>

        {/* Configuración de Vigencia */}
        <div className="form-section">
          <h3>Configuración de Vigencia</h3>

          <div className="form-group">
            <label htmlFor="fechaVigencia">
              Fecha de Vigencia * <span className="field-hint">(Hasta cuando es válida)</span>
            </label>
            <input
              type="date"
              id="fechaVigencia"
              min={getMinDate()}
              {...register('fechaVigencia', {
                required: 'La fecha de vigencia es requerida'
              })}
              className={errors.fechaVigencia ? 'error' : ''}
            />
            {errors.fechaVigencia && (
              <span className="error-message">{errors.fechaVigencia.message}</span>
            )}
            <small className="field-hint">
              La cotización expirará después de esta fecha
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="observaciones">Observaciones</label>
            <textarea
              id="observaciones"
              rows="4"
              {...register('observaciones', {
                maxLength: { value: 1000, message: 'Máximo 1000 caracteres' }
              })}
              placeholder="Notas adicionales sobre la cotización..."
              className={errors.observaciones ? 'error' : ''}
            />
            {errors.observaciones && (
              <span className="error-message">{errors.observaciones.message}</span>
            )}
          </div>
        </div>

        {/* Botones */}
        <div className="form-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => navigate('/cotizaciones')}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Creando...' : 'Crear Cotización'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CotizacionForm;
