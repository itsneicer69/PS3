package ec.edu.ups.supermaxi.controlador;

import ec.edu.ups.supermaxi.dao.CategoriaDAO;
import ec.edu.ups.supermaxi.dao.ProductoDAO;
import ec.edu.ups.supermaxi.dao.ProveedorDAO;
import ec.edu.ups.supermaxi.dao.SucursalDAO;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.Categoria;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import ec.edu.ups.supermaxi.modelo.Sucursal;

import java.util.List;

public class ProductoControlador {

    private final ProductoDAO productoDAO;
    private final ProveedorDAO proveedorDAO;
    private final CategoriaDAO categoriaDAO;
    private final SucursalDAO sucursalDAO;

    public ProductoControlador(ProductoDAO productoDAO,
                               ProveedorDAO proveedorDAO,
                               CategoriaDAO categoriaDAO,
                               SucursalDAO sucursalDAO) {
        this.productoDAO = productoDAO;
        this.proveedorDAO = proveedorDAO;
        this.categoriaDAO = categoriaDAO;
        this.sucursalDAO = sucursalDAO;
    }

    /**
     * CREATE: Registra un nuevo producto en el catálogo de Supermaxi, asignando su Proveedor (N:1),
     * Categoría (N:1) y las Sucursales donde se comercializa (N:M).
     */
    public Producto registrarProducto(String codigoBarras,
                                    String nombre,
                                    double precioUnitario,
                                    int stockInicial,
                                    Long proveedorId,
                                    Long categoriaId,
                                    List<Long> sucursalIds) {
        if (codigoBarras == null || !codigoBarras.trim().matches("[A-Za-z0-9\\-]{6,30}")) {
            throw new IllegalArgumentException("El código de barras debe tener entre 6 y 30 caracteres alfanuméricos.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero.");
        }
        if (stockInicial < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo.");
        }
        if (proveedorId == null || proveedorId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor válido.");
        }
        if (categoriaId == null || categoriaId <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una categoría del catálogo.");
        }
        if (sucursalIds == null || sucursalIds.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una sucursal donde se comercializará el producto.");
        }

        String codigoLimpio = codigoBarras.trim().toUpperCase();
        if (productoDAO.buscarPorCodigoBarras(codigoLimpio) != null) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + codigoLimpio);
        }

        Proveedor proveedor = proveedorDAO.buscarPorId(proveedorId);
        if (proveedor == null || !proveedor.isActivo()) {
            throw new IllegalArgumentException("El proveedor seleccionado no existe o está inactivo.");
        }

        Categoria categoria = categoriaDAO.buscarPorId(categoriaId);
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría seleccionada no existe.");
        }

        Producto producto = new Producto(
                codigoLimpio,
                nombre.trim(),
                precioUnitario,
                stockInicial,
                proveedor,
                categoria
        );

        for (Long sucursalId : sucursalIds) {
            Sucursal sucursal = sucursalDAO.buscarPorId(sucursalId);
            if (sucursal != null) {
                producto.agregarSucursal(sucursal);
            }
        }
        if (producto.getSucursales().isEmpty()) {
            throw new IllegalArgumentException("Ninguna de las sucursales seleccionadas es válida.");
        }

        productoDAO.crear(producto);
        return producto;
    }

    public List<Producto> listarTodosProductos() {
        return productoDAO.listarTodos();
    }

    public List<Categoria> listarCategorias() {
        return categoriaDAO.listarTodos();
    }

    public List<Sucursal> listarSucursales() {
        return sucursalDAO.listarTodos();
    }

    public PaginaResultado<Producto> consultarProductosPaginado(String textoBusqueda,
                                                                Long categoriaId,
                                                                Long proveedorId,
                                                                Long sucursalId,
                                                                Integer stockMaximo,
                                                                Double precioMaximo,
                                                                String campoOrden,
                                                                String direccionOrden,
                                                                int pagina,
                                                                int tamanioPagina) {
        return productoDAO.consultarProductosPaginado(
                textoBusqueda, categoriaId, proveedorId, sucursalId,
                stockMaximo, precioMaximo, campoOrden, direccionOrden, pagina, tamanioPagina
        );
    }
}
