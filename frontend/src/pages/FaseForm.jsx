import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { FaSave, FaArrowLeft, FaCalendar } from 'react-icons/fa';
import useFaseStore from '../store/faseStore';
import '../styles/Fases.css';

function FaseForm() {
  const { proyectoId, id } = useParams();
  const navigate = useNavigate();
  const { selectedFase, fetchFaseById, createFase, updateFase, clearSelectedFase } = useFaseStore();
  const isEditing = Boolean(id);

  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    numeroFase: '',
    totalTerrenos: '',
    areaTotal: '',
    fechaInicio: '',
    fechaFinEstimada: '',
    observaciones: '',
    activa: true,
  });

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isEditing && id) {
      fetchFaseById(id);
    }
    return () => clearSelectedFase();
  }, [id, isEditing]);

  useEffect(() => {
    if (isEditing && selectedFase) {
      setFormData({
        nombre: selectedFase.nombre || '',
        descripcion: selectedFase.descripcion || '',
        numeroFase: selectedFase.numeroFase || '',
        totalTerrenos: selectedFase.totalTerrenos || '',
        areaTotal: selectedFase.areaTotal || '',
        fechaInicio: selectedFase.fechaInicio || '',
        fechaFinEstimada: selectedFase.fechaFinEstimada || '',
        observaciones: selectedFase.observaciones || '',
        activa: selectedFase.activa ?? true,
      });
    }
  }, [selectedFase, isEditing]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.nombre.trim()) newErrors.nombre = 'El nombre es requerido';
    if (formData.nombre.length > 0 && formData.nombre.length < 3) newErrors.nombre = 'Mínimo 3 caracteres';
    if (formData.numeroFase && formData.numeroFase < 1) newErrors.numeroFase = 'Mínimo 1';
    if (formData.totalTerrenos && formData.totalTerrenos < 0) newErrors.totalTerrenos = 'No puede ser negativo';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    try {
      const payload = {
        ...formData,
        proyectoId: Number(proyectoId),
        numeroFase: formData.numeroFase ? Number(formData.numeroFase) : undefined,
        totalTerrenos: formData.totalTerrenos ? Number(formData.totalTerrenos) : undefined,
        areaTotal: formData.areaTotal ? Number(formData.areaTotal) : undefined,
        fechaInicio: formData.fechaInicio || undefined,
        fechaFinEstimada: formData.fechaFinEstimada || undefined,
      };

      if (isEditing) {
        await updateFase(id, payload);
      } else {
        await createFase(payload);
      }
      navigate(`/proyectos/${proyectoId}/fases`);
    } catch (error) {
      const msg = error.response?.data?.message || 'Error al guardar la fase';
      setErrors({ submit: msg });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fase-form-container">
      <div className="page-header">
        <div>
          <Link to={`/proyectos/${proyectoId}/fases`} className="back-link">
            <FaArrowLeft /> Volver a Fases
          </Link>
          <h1>{isEditing ? 'Editar Fase' : 'Nueva Fase'}</h1>
        </div>
      </div>

      {errors.submit && <div className="alert alert-error">{errors.submit}</div>}

      <form onSubmit={handleSubmit} className="form-container">
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="nombre">Nombre *</label>
            <input
              type="text"
              id="nombre"
              name="nombre"
              value={formData.nombre}
              onChange={handleChange}
              className={errors.nombre ? 'input-error' : ''}
              placeholder="Ej: Fase 1 - Etapa Inicial"
              maxLength={200}
            />
            {errors.nombre && <span className="error-text">{errors.nombre}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="numeroFase">Número de Fase</label>
            <input
              type="number"
              id="numeroFase"
              name="numeroFase"
              value={formData.numeroFase}
              onChange={handleChange}
              className={errors.numeroFase ? 'input-error' : ''}
              placeholder="Ej: 1"
              min="1"
            />
            {errors.numeroFase && <span className="error-text">{errors.numeroFase}</span>}
          </div>

          <div className="form-group full-width">
            <label htmlFor="descripcion">Descripción</label>
            <textarea
              id="descripcion"
              name="descripcion"
              value={formData.descripcion}
              onChange={handleChange}
              placeholder="Descripción de la fase..."
              rows={3}
              maxLength={1000}
            />
          </div>

          <div className="form-group">
            <label htmlFor="totalTerrenos">Total Terrenos</label>
            <input
              type="number"
              id="totalTerrenos"
              name="totalTerrenos"
              value={formData.totalTerrenos}
              onChange={handleChange}
              className={errors.totalTerrenos ? 'input-error' : ''}
              placeholder="Ej: 50"
              min="0"
            />
            {errors.totalTerrenos && <span className="error-text">{errors.totalTerrenos}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="areaTotal">Área Total (m²)</label>
            <input
              type="number"
              id="areaTotal"
              name="areaTotal"
              value={formData.areaTotal}
              onChange={handleChange}
              placeholder="Ej: 10000.00"
              min="0"
              step="0.01"
            />
          </div>

          <div className="form-group">
            <label htmlFor="fechaInicio">Fecha de Inicio</label>
            <input
              type="date"
              id="fechaInicio"
              name="fechaInicio"
              value={formData.fechaInicio}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="fechaFinEstimada">Fecha Fin Estimada</label>
            <input
              type="date"
              id="fechaFinEstimada"
              name="fechaFinEstimada"
              value={formData.fechaFinEstimada}
              onChange={handleChange}
            />
          </div>

          <div className="form-group full-width">
            <label htmlFor="observaciones">Observaciones</label>
            <textarea
              id="observaciones"
              name="observaciones"
              value={formData.observaciones}
              onChange={handleChange}
              placeholder="Notas adicionales..."
              rows={3}
              maxLength={1000}
            />
          </div>

          <div className="form-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="activa"
                checked={formData.activa}
                onChange={handleChange}
              />
              Fase activa
            </label>
          </div>
        </div>

        <div className="form-actions">
          <Link to={`/proyectos/${proyectoId}/fases`} className="btn btn-secondary">
            Cancelar
          </Link>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            <FaSave /> {submitting ? 'Guardando...' : isEditing ? 'Actualizar' : 'Crear Fase'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default FaseForm;
