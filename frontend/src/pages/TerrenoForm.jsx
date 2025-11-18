import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import useTerrenoStore from '../store/terrenoStore';
import proyectoService from '../services/proyectoService';
import MapEditor from '../components/MapEditor';
import '../styles/Terrenos.css';

function TerrenoForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const { selectedTerreno, fetchTerrenoById, createTerreno, updateTerreno, isLoading } = useTerrenoStore();
  const { register, handleSubmit, setValue, watch, formState: { errors } } = useForm();

  const [proyectos, setProyectos] = useState([]);
  const [coordenadas, setCoordenadas] = useState(null);

  useEffect(() => {
    loadProyectos();
    if (isEdit) {
      loadTerreno();
    }
  }, [id]);

  const loadProyectos = async () => {
    try {
      const data = await proyectoService.getAll();
      setProyectos(data);
    } catch (error) {
      console.error('Error loading proyectos:', error);
    }
  };

  const loadTerreno = async () => {
    try {
      const terreno = await fetchTerrenoById(id);

      // Llenar el formulario con los datos del terreno
      setValue('numero', terreno.numero);
      setValue('nombre', terreno.nombre);
      setValue('descripcion', terreno.descripcion);
      setValue('area', terreno.area);
      setValue('precioBase', terreno.precioBase);
      setValue('ajustePrecio', terreno.ajustePrecio || 0);
      setValue('multiplicadorPrecio', terreno.multiplicadorPrecio || 1);
      setValue('estado', terreno.estado);
      setValue('proyectoId', terreno.proyectoId);

      if (terreno.coordenadas) {
        setCoordenadas(terreno.coordenadas);
      }
    } catch (error) {
      console.error('Error loading terreno:', error);
      alert('Error al cargar el terreno');
    }
  };

  const onSubmit = async (data) => {
    try {
      const terrenoData = {
        ...data,
        area: parseFloat(data.area),
        precioBase: parseFloat(data.precioBase),
        ajustePrecio: parseFloat(data.ajustePrecio || 0),
        multiplicadorPrecio: parseFloat(data.multiplicadorPrecio || 1),
        coordenadas: coordenadas,
      };

      if (isEdit) {
        await updateTerreno(id, terrenoData);
      } else {
        await createTerreno(terrenoData);
      }

      navigate('/terrenos');
    } catch (error) {
      console.error('Error saving terreno:', error);
      alert('Error al guardar el terreno');
    }
  };

  const precioBase = watch('precioBase') || 0;
  const ajustePrecio = watch('ajustePrecio') || 0;
  const multiplicadorPrecio = watch('multiplicadorPrecio') || 1;

  const precioFinal = (parseFloat(precioBase) + parseFloat(ajustePrecio)) * parseFloat(multiplicadorPrecio);

  return (
    <div className="terrenos-container">
      <div className="page-header">
        <div>
          <h1>{isEdit ? 'Editar Terreno' : 'Nuevo Terreno'}</h1>
          <p>{isEdit ? 'Modifica los datos del terreno' : 'Crea un nuevo lote o inmueble'}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="form-container">
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="numero">Número *</label>
            <input
              id="numero"
              type="text"
              {...register('numero', { required: 'El número es requerido' })}
              placeholder="001"
            />
            {errors.numero && <span className="error-message">{errors.numero.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="nombre">Nombre</label>
            <input
              id="nombre"
              type="text"
              {...register('nombre')}
              placeholder="Lote Esquina Norte"
            />
          </div>

          <div className="form-group">
            <label htmlFor="proyectoId">Proyecto *</label>
            <select
              id="proyectoId"
              {...register('proyectoId', { required: 'El proyecto es requerido' })}
            >
              <option value="">Seleccionar proyecto</option>
              {proyectos.map(proyecto => (
                <option key={proyecto.id} value={proyecto.id}>
                  {proyecto.nombre}
                </option>
              ))}
            </select>
            {errors.proyectoId && <span className="error-message">{errors.proyectoId.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="estado">Estado *</label>
            <select
              id="estado"
              {...register('estado', { required: 'El estado es requerido' })}
            >
              <option value="DISPONIBLE">Disponible</option>
              <option value="APARTADO">Apartado</option>
              <option value="VENDIDO">Vendido</option>
              <option value="RESERVADO">Reservado</option>
            </select>
            {errors.estado && <span className="error-message">{errors.estado.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="area">Área (m²) *</label>
            <input
              id="area"
              type="number"
              step="0.01"
              {...register('area', {
                required: 'El área es requerida',
                min: { value: 0.01, message: 'El área debe ser mayor a 0' }
              })}
              placeholder="100.00"
            />
            {errors.area && <span className="error-message">{errors.area.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="precioBase">Precio Base *</label>
            <input
              id="precioBase"
              type="number"
              step="0.01"
              {...register('precioBase', {
                required: 'El precio base es requerido',
                min: { value: 0, message: 'El precio debe ser mayor o igual a 0' }
              })}
              placeholder="500000.00"
            />
            {errors.precioBase && <span className="error-message">{errors.precioBase.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="ajustePrecio">Ajuste de Precio</label>
            <input
              id="ajustePrecio"
              type="number"
              step="0.01"
              {...register('ajustePrecio')}
              placeholder="0.00"
            />
          </div>

          <div className="form-group">
            <label htmlFor="multiplicadorPrecio">Multiplicador de Precio</label>
            <input
              id="multiplicadorPrecio"
              type="number"
              step="0.01"
              {...register('multiplicadorPrecio')}
              placeholder="1.00"
            />
          </div>
        </div>

        <div className="form-grid single-column">
          <div className="form-group">
            <label htmlFor="descripcion">Descripción</label>
            <textarea
              id="descripcion"
              {...register('descripcion')}
              placeholder="Descripción del terreno..."
            />
          </div>
        </div>

        <div className="precio-calculado">
          <h3>Precio Final Calculado:</h3>
          <p className="precio-final-value">
            ${precioFinal.toLocaleString('es-MX', {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2
            })}
          </p>
          <p className="precio-formula">
            ({precioBase} + {ajustePrecio}) × {multiplicadorPrecio}
          </p>
        </div>

        <div className="map-section">
          <h3>Ubicación en el Plano</h3>
          <p className="map-hint">Dibuja el polígono del terreno en el mapa</p>
          <MapEditor
            initialCoordinates={coordenadas}
            onChange={setCoordenadas}
          />
        </div>

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/terrenos')}
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

export default TerrenoForm;
