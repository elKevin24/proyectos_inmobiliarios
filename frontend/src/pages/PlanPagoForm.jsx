import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import planPagoService from '../services/planPagoService';
import '../styles/PlanesPago.css';

function PlanPagoForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);
  const [loading, setLoading] = useState(false);

  const todayStr = new Date().toISOString().split('T')[0];
  const nextMonthDate = new Date();
  nextMonthDate.setMonth(nextMonthDate.getMonth() + 1);
  const nextMonthStr = nextMonthDate.toISOString().split('T')[0];

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      frecuenciaPago: 'MENSUAL',
      numeroPagos: 12,
      plazoMeses: 12,
      tasaInteresAnual: 0,
      tasaMoraMensual: 0,
      diasGracia: 5,
      enganche: 0,
      fechaInicio: todayStr,
      fechaPrimerPago: nextMonthStr,
    },
  });

  const loadPlan = async () => {
    try {
      setLoading(true);
      const plan = await planPagoService.getById(id);
      reset({
        ventaId: plan.ventaId,
        tipoPlan: plan.tipoPlan,
        frecuenciaPago: plan.frecuenciaPago || 'MENSUAL',
        montoTotal: plan.montoTotal,
        enganche: plan.enganche || 0,
        tasaInteresAnual: plan.tasaInteresAnual || 0,
        numeroPagos: plan.numeroPagos || 12,
        plazoMeses: plan.plazoMeses || 12,
        tasaMoraMensual: plan.tasaMoraMensual || 0,
        diasGracia: plan.diasGracia || 0,
        fechaInicio: plan.fechaInicio || todayStr,
        fechaPrimerPago: plan.fechaPrimerPago || nextMonthStr,
        notas: plan.notas || '',
      });
    } catch {
      toast.error('Error al cargar el plan de pago');
      navigate('/planes-pago');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isEdit) {
      loadPlan();
    }
  }, [id]);

  const onSubmit = async (data) => {
    try {
      setLoading(true);
      const tasaAnual = parseFloat(data.tasaInteresAnual) || 0;

      if (isEdit) {
        await planPagoService.update(id, {
          tasaMoraMensual: parseFloat(data.tasaMoraMensual) || 0,
          diasGracia: parseInt(data.diasGracia, 10) || 0,
          notas: data.notas,
        });
        toast.success('Plan de pago actualizado correctamente');
      } else {
        const payload = {
          ventaId: parseInt(data.ventaId, 10),
          tipoPlan: data.tipoPlan,
          frecuenciaPago: data.frecuenciaPago || 'MENSUAL',
          montoTotal: parseFloat(data.montoTotal),
          enganche: parseFloat(data.enganche) || 0,
          tasaInteresAnual: tasaAnual,
          aplicaInteres: tasaAnual > 0,
          numeroPagos: parseInt(data.numeroPagos, 10) || 12,
          plazoMeses: parseInt(data.plazoMeses, 10) || 12,
          tasaMoraMensual: parseFloat(data.tasaMoraMensual) || 0,
          diasGracia: parseInt(data.diasGracia, 10) || 0,
          fechaInicio: data.fechaInicio,
          fechaPrimerPago: data.fechaPrimerPago,
          notas: data.notas,
        };
        await planPagoService.create(payload);
        toast.success('Plan de pago creado correctamente');
      }
      navigate('/planes-pago');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al guardar el plan de pago');
    } finally {
      setLoading(false);
    }
  };

  if (isEdit && loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Cargando plan de pago...</p>
      </div>
    );
  }

  return (
    <div className="planes-pago-container">
      <div className="page-header">
        <div>
          <h1>{isEdit ? 'Editar Plan de Pago' : 'Nuevo Plan de Pago'}</h1>
          <p>{isEdit ? 'Modifica los parámetros permitidos del plan de pago' : 'Crea un nuevo plan de pago y su tabla de amortización'}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="form-container">
        <h3>Información del Plan</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="ventaId">Venta ID *</label>
            <input
              id="ventaId"
              type="number"
              disabled={isEdit}
              {...register('ventaId', { required: 'El ID de la venta es requerido' })}
              placeholder="1"
            />
            {errors.ventaId && <span className="error-message">{errors.ventaId.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="tipoPlan">Tipo de Plan *</label>
            <select
              id="tipoPlan"
              disabled={isEdit}
              {...register('tipoPlan', { required: 'El tipo de plan es requerido' })}
            >
              <option value="">Seleccionar tipo</option>
              <option value="CONTADO">Contado</option>
              <option value="CREDITO_BANCARIO">Crédito Bancario</option>
              <option value="FINANCIAMIENTO_PROPIO">Financiamiento Propio</option>
              <option value="MIXTO">Mixto</option>
            </select>
            {errors.tipoPlan && <span className="error-message">{errors.tipoPlan.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="frecuenciaPago">Frecuencia de Pago *</label>
            <select
              id="frecuenciaPago"
              disabled={isEdit}
              {...register('frecuenciaPago', { required: 'La frecuencia es requerida' })}
            >
              <option value="MENSUAL">Mensual</option>
              <option value="QUINCENAL">Quincenal</option>
              <option value="SEMANAL">Semanal</option>
              <option value="BIMESTRAL">Bimestral</option>
              <option value="TRIMESTRAL">Trimestral</option>
              <option value="SEMESTRAL">Semestral</option>
              <option value="ANUAL">Anual</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="montoTotal">Monto Total *</label>
            <input
              id="montoTotal"
              type="number"
              step="0.01"
              disabled={isEdit}
              {...register('montoTotal', {
                required: 'El monto total es requerido',
                min: { value: 0.01, message: 'El monto debe ser mayor a 0' },
              })}
              placeholder="500000.00"
            />
            {errors.montoTotal && <span className="error-message">{errors.montoTotal.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="enganche">Enganche</label>
            <input
              id="enganche"
              type="number"
              step="0.01"
              disabled={isEdit}
              {...register('enganche')}
              placeholder="100000.00"
            />
          </div>

          <div className="form-group">
            <label htmlFor="numeroPagos">Número de Pagos *</label>
            <input
              id="numeroPagos"
              type="number"
              min="1"
              max="600"
              disabled={isEdit}
              {...register('numeroPagos', { required: 'El número de pagos es obligatorio' })}
              placeholder="12"
            />
            {errors.numeroPagos && <span className="error-message">{errors.numeroPagos.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="plazoMeses">Plazo (meses)</label>
            <input
              id="plazoMeses"
              type="number"
              min="1"
              max="600"
              disabled={isEdit}
              {...register('plazoMeses')}
              placeholder="12"
            />
          </div>

          <div className="form-group">
            <label htmlFor="tasaInteresAnual">Tasa de Interés Anual (%)</label>
            <input
              id="tasaInteresAnual"
              type="number"
              step="0.01"
              disabled={isEdit}
              {...register('tasaInteresAnual')}
              placeholder="10.00"
            />
          </div>

          <div className="form-group">
            <label htmlFor="tasaMoraMensual">Tasa de Mora Mensual (%)</label>
            <input
              id="tasaMoraMensual"
              type="number"
              step="0.01"
              {...register('tasaMoraMensual')}
              placeholder="2.00"
            />
          </div>

          <div className="form-group">
            <label htmlFor="diasGracia">Días de Gracia</label>
            <input
              id="diasGracia"
              type="number"
              min="0"
              max="90"
              {...register('diasGracia')}
              placeholder="5"
            />
          </div>

          <div className="form-group">
            <label htmlFor="fechaInicio">Fecha de Inicio *</label>
            <input
              id="fechaInicio"
              type="date"
              disabled={isEdit}
              {...register('fechaInicio', { required: 'La fecha de inicio es obligatoria' })}
            />
            {errors.fechaInicio && <span className="error-message">{errors.fechaInicio.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="fechaPrimerPago">Fecha Primer Pago *</label>
            <input
              id="fechaPrimerPago"
              type="date"
              disabled={isEdit}
              {...register('fechaPrimerPago', { required: 'La fecha del primer pago es obligatoria' })}
            />
            {errors.fechaPrimerPago && <span className="error-message">{errors.fechaPrimerPago.message}</span>}
          </div>
        </div>

        <div className="form-grid single-column">
          <div className="form-group">
            <label htmlFor="notas">Notas</label>
            <textarea
              id="notas"
              {...register('notas')}
              placeholder="Observaciones adicionales del plan de pago..."
              rows={4}
            />
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/planes-pago')}
            className="btn btn-secondary"
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
          >
            {loading ? 'Guardando...' : isEdit ? 'Actualizar Plan' : 'Crear Plan'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default PlanPagoForm;
