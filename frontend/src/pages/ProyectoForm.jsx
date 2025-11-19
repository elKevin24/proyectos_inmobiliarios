import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import useProyectoStore from '../store/proyectoStore';
import ImageUploader from '../components/ImageUploader';
import '../styles/Proyectos.css';

function ProyectoForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const { selectedProyecto, fetchProyectoById, createProyecto, updateProyecto, isLoading } = useProyectoStore();
  const { register, handleSubmit, setValue, formState: { errors } } = useForm();

  const [uploadedFile, setUploadedFile] = useState(null);

  useEffect(() => {
    if (isEdit) {
      loadProyecto();
    }
  }, [id]);

  const loadProyecto = async () => {
    try {
      const proyecto = await fetchProyectoById(id);

      // Llenar el formulario con los datos del proyecto
      setValue('nombre', proyecto.nombre);
      setValue('descripcion', proyecto.descripcion);
      setValue('ubicacion', proyecto.ubicacion);
      setValue('direccion', proyecto.direccion);
      setValue('ciudad', proyecto.ciudad);
      setValue('estado', proyecto.estado);
      setValue('codigoPostal', proyecto.codigoPostal);
      setValue('precioBaseM2', proyecto.precioBaseM2);
      setValue('areaTotal', proyecto.areaTotal);
      setValue('estadoProyecto', proyecto.estado);

      if (proyecto.archivoPlanoUrl) {
        setUploadedFile({ url: proyecto.archivoPlanoUrl });
      }
    } catch (error) {
      console.error('Error loading proyecto:', error);
      alert('Error al cargar el proyecto');
    }
  };

  const onSubmit = async (data) => {
    try {
      const proyectoData = {
        nombre: data.nombre,
        descripcion: data.descripcion,
        ubicacion: data.ubicacion,
        direccion: data.direccion,
        ciudad: data.ciudad,
        estado: data.estado,
        codigoPostal: data.codigoPostal,
        precioBaseM2: parseFloat(data.precioBaseM2 || 0),
        areaTotal: parseFloat(data.areaTotal || 0),
        estado: data.estadoProyecto,
        archivoPlanoId: uploadedFile?.id || null,
      };

      if (isEdit) {
        await updateProyecto(id, proyectoData);
      } else {
        await createProyecto(proyectoData);
      }

      navigate('/proyectos');
    } catch (error) {
      console.error('Error saving proyecto:', error);
      alert('Error al guardar el proyecto');
    }
  };

  return (
    <div className="proyectos-container">
      <div className="page-header">
        <div>
          <h1>{isEdit ? 'Editar Proyecto' : 'Nuevo Proyecto'}</h1>
          <p>{isEdit ? 'Modifica los datos del proyecto' : 'Crea un nuevo desarrollo inmobiliario'}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="form-container">
        <h3>Información General</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="nombre">Nombre del Proyecto *</label>
            <input
              id="nombre"
              type="text"
              {...register('nombre', { required: 'El nombre es requerido' })}
              placeholder="Residencial Las Palmas"
            />
            {errors.nombre && <span className="error-message">{errors.nombre.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="estadoProyecto">Estado del Proyecto *</label>
            <select
              id="estadoProyecto"
              {...register('estadoProyecto', { required: 'El estado es requerido' })}
            >
              <option value="PLANIFICACION">Planificación</option>
              <option value="EN_VENTA">En Venta</option>
              <option value="AGOTADO">Agotado</option>
              <option value="SUSPENDIDO">Suspendido</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
            {errors.estadoProyecto && <span className="error-message">{errors.estadoProyecto.message}</span>}
          </div>
        </div>

        <div className="form-grid single-column">
          <div className="form-group">
            <label htmlFor="descripcion">Descripción</label>
            <textarea
              id="descripcion"
              {...register('descripcion')}
              placeholder="Descripción del proyecto..."
              rows={4}
            />
          </div>
        </div>

        <h3>Ubicación</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="ubicacion">Ubicación General</label>
            <input
              id="ubicacion"
              type="text"
              {...register('ubicacion')}
              placeholder="Zona Norte"
            />
          </div>

          <div className="form-group">
            <label htmlFor="direccion">Dirección</label>
            <input
              id="direccion"
              type="text"
              {...register('direccion')}
              placeholder="Av. Principal #123"
            />
          </div>

          <div className="form-group">
            <label htmlFor="ciudad">Ciudad</label>
            <input
              id="ciudad"
              type="text"
              {...register('ciudad')}
              placeholder="Ciudad de México"
            />
          </div>

          <div className="form-group">
            <label htmlFor="estado">Estado</label>
            <input
              id="estado"
              type="text"
              {...register('estado')}
              placeholder="CDMX"
            />
          </div>

          <div className="form-group">
            <label htmlFor="codigoPostal">Código Postal</label>
            <input
              id="codigoPostal"
              type="text"
              {...register('codigoPostal', {
                pattern: {
                  value: /^\d{5}$/,
                  message: 'El código postal debe tener 5 dígitos'
                }
              })}
              placeholder="01234"
            />
            {errors.codigoPostal && <span className="error-message">{errors.codigoPostal.message}</span>}
          </div>
        </div>

        <h3>Información Comercial</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="precioBaseM2">Precio Base por m²</label>
            <input
              id="precioBaseM2"
              type="number"
              step="0.01"
              {...register('precioBaseM2', {
                min: { value: 0, message: 'El precio debe ser mayor o igual a 0' }
              })}
              placeholder="5000.00"
            />
            {errors.precioBaseM2 && <span className="error-message">{errors.precioBaseM2.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="areaTotal">Área Total (m²)</label>
            <input
              id="areaTotal"
              type="number"
              step="0.01"
              {...register('areaTotal', {
                min: { value: 0, message: 'El área debe ser mayor a 0' }
              })}
              placeholder="10000.00"
            />
            {errors.areaTotal && <span className="error-message">{errors.areaTotal.message}</span>}
          </div>
        </div>

        <h3>Plano del Proyecto</h3>
        <ImageUploader
          onUploadSuccess={setUploadedFile}
          proyectoId={id}
          existingImage={uploadedFile?.url}
        />

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/proyectos')}
            className="btn btn-secondary"
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Guardando...' : (isEdit ? 'Actualizar' : 'Crear')}
          </button>
        </div>
      </form>
    </div>
  );
}

export default ProyectoForm;
