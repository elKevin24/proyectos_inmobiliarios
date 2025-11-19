import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import clienteStore from '../store/clienteStore';
import '../styles/Clientes.css';

function ClienteForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const { selectedCliente, fetchClienteById, createCliente, updateCliente, isLoading } = clienteStore();
  const { register, handleSubmit, setValue, formState: { errors } } = useForm();

  useEffect(() => {
    if (isEdit) {
      loadCliente();
    }
  }, [id]);

  const loadCliente = async () => {
    try {
      const cliente = await fetchClienteById(id);

      // Llenar el formulario
      Object.keys(cliente).forEach(key => {
        setValue(key, cliente[key]);
      });
    } catch (error) {
      console.error('Error loading cliente:', error);
      alert('Error al cargar el cliente');
    }
  };

  const onSubmit = async (data) => {
    try {
      const clienteData = {
        nombre: data.nombre,
        apellidos: data.apellidos,
        email: data.email,
        telefono: data.telefono,
        rfc: data.rfc || null,
        curp: data.curp || null,
        direccion: data.direccion || null,
        ciudad: data.ciudad || null,
        estado: data.estadoResidencia || null,
        codigoPostal: data.codigoPostal || null,
        fechaNacimiento: data.fechaNacimiento || null,
        ocupacion: data.ocupacion || null,
        estadoCliente: data.estadoCliente,
        notas: data.notas || null,
      };

      if (isEdit) {
        await updateCliente(id, clienteData);
      } else {
        await createCliente(clienteData);
      }

      navigate('/clientes');
    } catch (error) {
      console.error('Error saving cliente:', error);
      alert('Error al guardar el cliente');
    }
  };

  return (
    <div className="clientes-container">
      <div className="page-header">
        <div>
          <h1>{isEdit ? 'Editar Cliente' : 'Nuevo Cliente'}</h1>
          <p>{isEdit ? 'Modifica los datos del cliente' : 'Registra un nuevo cliente o prospecto'}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="form-container">
        <h3>Información Personal</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="nombre">Nombre *</label>
            <input
              id="nombre"
              type="text"
              {...register('nombre', { required: 'El nombre es requerido' })}
              placeholder="Juan"
            />
            {errors.nombre && <span className="error-message">{errors.nombre.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="apellidos">Apellidos *</label>
            <input
              id="apellidos"
              type="text"
              {...register('apellidos', { required: 'Los apellidos son requeridos' })}
              placeholder="Pérez García"
            />
            {errors.apellidos && <span className="error-message">{errors.apellidos.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="email">Email *</label>
            <input
              id="email"
              type="email"
              {...register('email', {
                required: 'El email es requerido',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Email inválido'
                }
              })}
              placeholder="juan.perez@email.com"
            />
            {errors.email && <span className="error-message">{errors.email.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="telefono">Teléfono *</label>
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
            {errors.telefono && <span className="error-message">{errors.telefono.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="fechaNacimiento">Fecha de Nacimiento</label>
            <input
              id="fechaNacimiento"
              type="date"
              {...register('fechaNacimiento')}
            />
          </div>

          <div className="form-group">
            <label htmlFor="estadoCliente">Estado del Cliente *</label>
            <select
              id="estadoCliente"
              {...register('estadoCliente', { required: 'El estado es requerido' })}
            >
              <option value="PROSPECTO">Prospecto</option>
              <option value="INTERESADO">Interesado</option>
              <option value="COMPRADOR">Comprador</option>
              <option value="INACTIVO">Inactivo</option>
            </select>
            {errors.estadoCliente && <span className="error-message">{errors.estadoCliente.message}</span>}
          </div>
        </div>

        <h3>Información Fiscal</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="rfc">RFC</label>
            <input
              id="rfc"
              type="text"
              {...register('rfc', {
                pattern: {
                  value: /^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$/,
                  message: 'RFC inválido'
                }
              })}
              placeholder="XAXX010101000"
              maxLength={13}
            />
            {errors.rfc && <span className="error-message">{errors.rfc.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="curp">CURP</label>
            <input
              id="curp"
              type="text"
              {...register('curp', {
                pattern: {
                  value: /^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9A-Z][0-9]$/,
                  message: 'CURP inválido'
                }
              })}
              placeholder="XAXX010101HDFXXX00"
              maxLength={18}
            />
            {errors.curp && <span className="error-message">{errors.curp.message}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="ocupacion">Ocupación</label>
            <input
              id="ocupacion"
              type="text"
              {...register('ocupacion')}
              placeholder="Ingeniero"
            />
          </div>
        </div>

        <h3>Dirección</h3>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="direccion">Dirección</label>
            <input
              id="direccion"
              type="text"
              {...register('direccion')}
              placeholder="Calle Principal #123"
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
            <label htmlFor="estadoResidencia">Estado</label>
            <input
              id="estadoResidencia"
              type="text"
              {...register('estadoResidencia')}
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

        <h3>Notas</h3>
        <div className="form-grid single-column">
          <div className="form-group">
            <label htmlFor="notas">Notas adicionales</label>
            <textarea
              id="notas"
              {...register('notas')}
              placeholder="Información adicional del cliente..."
              rows={4}
            />
          </div>
        </div>

        <div className="form-actions">
          <button
            type="button"
            onClick={() => navigate('/clientes')}
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

export default ClienteForm;
