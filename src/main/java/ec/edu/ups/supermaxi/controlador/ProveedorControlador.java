package ec.edu.ups.supermaxi.controlador;

import ec.edu.ups.supermaxi.dao.ContratoComercialDAO;
import ec.edu.ups.supermaxi.dao.ProveedorDAO;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.ContratoComercial;
import ec.edu.ups.supermaxi.modelo.Proveedor;

import java.time.LocalDate;
import java.util.List;

public class ProveedorControlador {

    private final ProveedorDAO proveedorDAO;
    private final ContratoComercialDAO contratoComercialDAO;

    // Constructor compatible con código existente
    public ProveedorControlador(ProveedorDAO proveedorDAO) {
        this(proveedorDAO, null);
    }

    public ProveedorControlador(ProveedorDAO proveedorDAO, ContratoComercialDAO contratoComercialDAO) {
        this.proveedorDAO = proveedorDAO;
        this.contratoComercialDAO = contratoComercialDAO;
    }

    // 1. CREATE: Validar y registrar un nuevo proveedor (compatibilidad)
    public void registrarProveedor(String ruc, String razonSocial, String categoriaSuministro,
                                   String telefono, String correo, String direccion) {
        registrarProveedor(ruc, razonSocial, categoriaSuministro, telefono, correo, direccion,
                null, 30, 0.0, null);
    }

    // 1.1 CREATE con Contrato Comercial 1:1 opcional o completo
    public Proveedor registrarProveedor(String ruc, String razonSocial, String categoriaSuministro,
                                        String telefono, String correo, String direccion,
                                        String numeroContrato, int diasCredito,
                                        double descuentoProntoPago, String condicionesEntrega) {
        validarDatosObligatorios(ruc, razonSocial, categoriaSuministro, correo);
        validarFormatoRuc(ruc);

        Proveedor existente = proveedorDAO.buscarPorRuc(ruc.trim());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe un proveedor registrado con el RUC: " + ruc);
        }

        Proveedor nuevoProveedor = new Proveedor(
                ruc.trim(),
                razonSocial.trim().toUpperCase(),
                categoriaSuministro.trim(),
                telefono != null ? telefono.trim() : "",
                correo.trim().toLowerCase(),
                direccion != null ? direccion.trim() : ""
        );

        if (numeroContrato != null && !numeroContrato.trim().isEmpty()) {
            validarDatosContrato(numeroContrato, diasCredito, descuentoProntoPago, null);
            ContratoComercial contrato = new ContratoComercial(
                    numeroContrato.trim().toUpperCase(),
                    diasCredito,
                    descuentoProntoPago,
                    LocalDate.now(),
                    LocalDate.now().plusYears(1),
                    condicionesEntrega != null ? condicionesEntrega.trim() : "Entrega en bodega central Supermaxi"
            );
            nuevoProveedor.asignarContratoComercial(contrato);
        }

        proveedorDAO.crear(nuevoProveedor);
        return nuevoProveedor;
    }

    // 2. READ: Buscar proveedor por ID
    public Proveedor obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID del proveedor debe ser un número válido mayor a cero.");
        }
        return proveedorDAO.buscarPorId(id);
    }

    // 2.1 READ: Buscar proveedor por RUC
    public Proveedor obtenerPorRuc(String ruc) {
        validarFormatoRuc(ruc);
        return proveedorDAO.buscarPorRuc(ruc.trim());
    }

    // 2.2 READ: Listar todos los proveedores activos
    public List<Proveedor> listarProveedoresActivos() {
        return proveedorDAO.listarActivos();
    }

    // 2.3 READ: Listar todos los proveedores (activos e inactivos)
    public List<Proveedor> listarTodosProveedores() {
        return proveedorDAO.listarTodos();
    }

    // 2.4 READ (JPQL Paginado): Consultar proveedores con filtros, ordenamiento y paginación
    public PaginaResultado<Proveedor> consultarProveedoresPaginado(Boolean activo,
                                                                   String textoBusqueda,
                                                                   String categoria,
                                                                   Integer minDiasCredito,
                                                                   String campoOrden,
                                                                   String direccionOrden,
                                                                   int pagina,
                                                                   int tamanioPagina) {
        return proveedorDAO.consultarProveedoresPaginado(
                activo, textoBusqueda, categoria, minDiasCredito,
                campoOrden, direccionOrden, pagina, tamanioPagina
        );
    }

    // 3. UPDATE: Validar y actualizar los datos de un proveedor existente (compatibilidad)
    public Proveedor actualizarProveedor(Long id, String ruc, String razonSocial, String categoriaSuministro,
                                         String telefono, String correo, String direccion, boolean activo) {
        return actualizarProveedor(id, ruc, razonSocial, categoriaSuministro, telefono, correo, direccion, activo,
                null, 30, 0.0, null);
    }

    // 3.1 UPDATE incluyendo actualización o creación de Contrato Comercial 1:1
    public Proveedor actualizarProveedor(Long id, String ruc, String razonSocial, String categoriaSuministro,
                                         String telefono, String correo, String direccion, boolean activo,
                                         String numeroContrato, int diasCredito,
                                         double descuentoProntoPago, String condicionesEntrega) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Debe especificar un ID válido para actualizar el proveedor.");
        }

        Proveedor proveedorActual = proveedorDAO.buscarPorId(id);
        if (proveedorActual == null) {
            throw new IllegalArgumentException("No se encontró ningún proveedor con el ID: " + id);
        }

        validarDatosObligatorios(ruc, razonSocial, categoriaSuministro, correo);
        validarFormatoRuc(ruc);

        Proveedor proveedorConMismoRuc = proveedorDAO.buscarPorRuc(ruc.trim());
        if (proveedorConMismoRuc != null && !proveedorConMismoRuc.getId().equals(id)) {
            throw new IllegalArgumentException("El RUC " + ruc + " ya está asignado a otro proveedor.");
        }

        // Validar TODO antes de modificar la entidad gestionada: si se modificara primero y luego fallara
        // una validación, los cambios quedarían "sucios" en el EntityManager compartido y se guardarían
        // en la siguiente transacción de cualquier otra operación.
        boolean actualizaContrato = numeroContrato != null && !numeroContrato.trim().isEmpty();
        ContratoComercial contratoExistente = proveedorActual.getContratoComercial();
        if (actualizaContrato) {
            Long contratoIdActual = contratoExistente != null ? contratoExistente.getId() : null;
            validarDatosContrato(numeroContrato, diasCredito, descuentoProntoPago, contratoIdActual);
        }

        proveedorActual.setRuc(ruc.trim());
        proveedorActual.setRazonSocial(razonSocial.trim().toUpperCase());
        proveedorActual.setCategoriaSuministro(categoriaSuministro.trim());
        proveedorActual.setTelefono(telefono != null ? telefono.trim() : "");
        proveedorActual.setCorreo(correo.trim().toLowerCase());
        proveedorActual.setDireccion(direccion != null ? direccion.trim() : "");
        proveedorActual.setActivo(activo);

        if (actualizaContrato) {
            if (contratoExistente == null) {
                ContratoComercial nuevoContrato = new ContratoComercial(
                        numeroContrato.trim().toUpperCase(),
                        diasCredito,
                        descuentoProntoPago,
                        LocalDate.now(),
                        LocalDate.now().plusYears(1),
                        condicionesEntrega != null ? condicionesEntrega.trim() : "Entrega en bodega central Supermaxi"
                );
                proveedorActual.asignarContratoComercial(nuevoContrato);
            } else {
                contratoExistente.setNumeroContrato(numeroContrato.trim().toUpperCase());
                contratoExistente.setDiasCredito(diasCredito);
                contratoExistente.setDescuentoProntoPago(descuentoProntoPago);
                if (condicionesEntrega != null) {
                    contratoExistente.setCondicionesEntrega(condicionesEntrega.trim());
                }
            }
        }

        return proveedorDAO.actualizar(proveedorActual);
    }

    // 4. DELETE (Borrado Lógico)
    public void desactivarProveedor(Long id) {
        Proveedor proveedor = obtenerPorId(id);
        if (proveedor == null) {
            throw new IllegalArgumentException("No existe el proveedor con ID: " + id);
        }
        proveedor.setActivo(false);
        proveedorDAO.actualizar(proveedor);
    }

    // 4.1 DELETE (Borrado Físico)
    public void eliminarProveedorFisico(Long id) {
        Proveedor proveedor = obtenerPorId(id);
        if (proveedor == null) {
            throw new IllegalArgumentException("No existe el proveedor con ID: " + id);
        }
        proveedorDAO.eliminar(id);
    }

    // --- MÉTODOS PRIVADOS DE VALIDACIÓN ---

    private void validarDatosObligatorios(String ruc, String razonSocial, String categoria, String correo) {
        if (ruc == null || ruc.trim().isEmpty()) {
            throw new IllegalArgumentException("El RUC del proveedor es obligatorio.");
        }
        if (razonSocial == null || razonSocial.trim().isEmpty()) {
            throw new IllegalArgumentException("La Razón Social es obligatoria.");
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría de suministro es obligatoria.");
        }
        if (correo == null || !correo.contains("@") || !correo.contains(".")) {
            throw new IllegalArgumentException("Debe ingresar un correo electrónico válido.");
        }
    }

    private void validarFormatoRuc(String ruc) {
        if (ruc == null || !ruc.trim().matches("\\d{13}")) {
            throw new IllegalArgumentException("El RUC debe contener exactamente 13 dígitos numéricos.");
        }
        if (!ruc.trim().endsWith("001")) {
            throw new IllegalArgumentException("El RUC de proveedor debe finalizar en '001'.");
        }
    }

    private void validarDatosContrato(String numeroContrato, int diasCredito,
                                      double descuentoProntoPago, Long contratoIdActual) {
        if (diasCredito < 0 || diasCredito > 365) {
            throw new IllegalArgumentException("Los días de crédito deben estar entre 0 y 365.");
        }
        if (descuentoProntoPago < 0.0 || descuentoProntoPago > 100.0) {
            throw new IllegalArgumentException("El descuento por pronto pago debe estar entre 0% y 100%.");
        }
        if (contratoComercialDAO != null) {
            ContratoComercial existente = contratoComercialDAO.buscarPorNumeroContrato(numeroContrato.trim().toUpperCase());
            if (existente != null && !existente.getId().equals(contratoIdActual)) {
                throw new IllegalArgumentException("Ya existe un contrato comercial con el número: " + numeroContrato);
            }
        }
    }
}
