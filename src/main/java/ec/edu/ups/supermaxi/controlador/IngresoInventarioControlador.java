package ec.edu.ups.supermaxi.controlador;

import ec.edu.ups.supermaxi.dao.IngresoInventarioDAO;
import ec.edu.ups.supermaxi.dao.ProductoDAO;
import ec.edu.ups.supermaxi.dao.ProveedorDAO;
import ec.edu.ups.supermaxi.dao.SucursalDAO;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.dto.ResumenAbastecimientoDTO;
import ec.edu.ups.supermaxi.modelo.IngresoInventario;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import ec.edu.ups.supermaxi.modelo.Sucursal;

import java.time.LocalDateTime;
import java.util.List;

public class IngresoInventarioControlador {

    private final IngresoInventarioDAO ingresoDAO;
    private final ProductoDAO productoDAO;
    private final ProveedorDAO proveedorDAO;
    private final SucursalDAO sucursalDAO;

    public IngresoInventarioControlador(IngresoInventarioDAO ingresoDAO,
                                        ProductoDAO productoDAO,
                                        ProveedorDAO proveedorDAO,
                                        SucursalDAO sucursalDAO) {
        this.ingresoDAO = ingresoDAO;
        this.productoDAO = productoDAO;
        this.proveedorDAO = proveedorDAO;
        this.sucursalDAO = sucursalDAO;
    }

    /**
     * CREATE: Registra el ingreso de mercadería al inventario de una sucursal/bodega Supermaxi
     * e incrementa transaccionalmente el stock actual del producto.
     */
    public IngresoInventario registrarIngreso(String numeroGuiaRemision,
                                              int cantidadIngresada,
                                              double costoUnitarioCompra,
                                              String observaciones,
                                              Long productoId,
                                              Long proveedorId,
                                              Long sucursalId) {
        if (numeroGuiaRemision == null || numeroGuiaRemision.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de guía de remisión es obligatorio.");
        }
        if (cantidadIngresada <= 0) {
            throw new IllegalArgumentException("La cantidad ingresada debe ser mayor a cero.");
        }
        if (costoUnitarioCompra <= 0) {
            throw new IllegalArgumentException("El costo unitario de compra debe ser mayor a cero.");
        }
        if (productoId == null || productoId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un producto del catálogo.");
        }
        if (proveedorId == null || proveedorId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor válido.");
        }
        if (sucursalId == null || sucursalId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar la sucursal o centro de distribución receptor.");
        }

        Producto producto = productoDAO.buscarPorId(productoId);
        if (producto == null) {
            throw new IllegalArgumentException("El producto seleccionado no existe.");
        }

        Proveedor proveedor = proveedorDAO.buscarPorId(proveedorId);
        if (proveedor == null || !proveedor.isActivo()) {
            throw new IllegalArgumentException("El proveedor seleccionado no existe o está inactivo.");
        }

        Sucursal sucursal = sucursalDAO.buscarPorId(sucursalId);
        if (sucursal == null) {
            throw new IllegalArgumentException("La sucursal receptora no existe.");
        }

        // Coherencia del ingreso: el proveedor debe ser quien abastece el producto
        if (producto.getProveedor() == null || !producto.getProveedor().getId().equals(proveedor.getId())) {
            throw new IllegalArgumentException("El producto \"" + producto.getNombre()
                    + "\" no es abastecido por el proveedor seleccionado.");
        }

        // La sucursal debe comercializar el producto (los centros de distribución pueden recibir cualquiera)
        if (!sucursal.isEsCentroDistribucion() && !producto.getSucursales().contains(sucursal)) {
            throw new IllegalArgumentException("El producto \"" + producto.getNombre()
                    + "\" no se comercializa en la sucursal " + sucursal.getNombre() + ".");
        }

        // Una misma guía de remisión no puede registrar dos veces el mismo producto
        String guiaNormalizada = numeroGuiaRemision.trim().toUpperCase();
        boolean guiaDuplicada = ingresoDAO.buscarPorNumeroGuia(guiaNormalizada).stream()
                .anyMatch(i -> i.getProducto().getId().equals(producto.getId()));
        if (guiaDuplicada) {
            throw new IllegalArgumentException("La guía " + guiaNormalizada
                    + " ya registró un ingreso para este producto.");
        }

        IngresoInventario ingreso = new IngresoInventario(
                guiaNormalizada,
                cantidadIngresada,
                costoUnitarioCompra,
                observaciones != null ? observaciones.trim() : "",
                producto,
                proveedor,
                sucursal
        );

        ingresoDAO.crear(ingreso);
        return ingreso;
    }

    public List<IngresoInventario> listarTodosIngresos() {
        return ingresoDAO.listarTodos();
    }

    public PaginaResultado<IngresoInventario> consultarIngresosPaginado(Long proveedorId,
                                                                        Long productoId,
                                                                        Long sucursalId,
                                                                        Integer cantidadMinima,
                                                                        String numeroGuia,
                                                                        LocalDateTime fechaDesde,
                                                                        LocalDateTime fechaHasta,
                                                                        String campoOrden,
                                                                        String direccionOrden,
                                                                        int pagina,
                                                                        int tamanioPagina) {
        return ingresoDAO.consultarIngresosPaginado(
                proveedorId, productoId, sucursalId, cantidadMinima,
                numeroGuia, fechaDesde, fechaHasta,
                campoOrden, direccionOrden, pagina, tamanioPagina
        );
    }

    public PaginaResultado<ResumenAbastecimientoDTO> consultarResumenAbastecimiento(Boolean soloProveedoresActivos,
                                                                                    Long sucursalId,
                                                                                    Double inversionMinima,
                                                                                    String campoOrden,
                                                                                    String direccionOrden,
                                                                                    int pagina,
                                                                                    int tamanioPagina) {
        return ingresoDAO.consultarResumenAbastecimiento(
                soloProveedoresActivos, sucursalId, inversionMinima,
                campoOrden, direccionOrden, pagina, tamanioPagina
        );
    }
}
