package ec.edu.ups.supermaxi.controlador;

import ec.edu.ups.supermaxi.dao.CategoriaDAO;
import ec.edu.ups.supermaxi.dao.IngresoInventarioDAO;
import ec.edu.ups.supermaxi.dao.ProductoDAO;
import ec.edu.ups.supermaxi.dao.ProveedorDAO;
import ec.edu.ups.supermaxi.dao.SucursalDAO;
import ec.edu.ups.supermaxi.modelo.Categoria;
import ec.edu.ups.supermaxi.modelo.ContratoComercial;
import ec.edu.ups.supermaxi.modelo.IngresoInventario;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import ec.edu.ups.supermaxi.modelo.Sucursal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio idempotente encargado de poblar datos iniciales realistas del dominio Supermaxi
 * para demostración del modelo ORM, operaciones CRUD y consultas JPQL paginadas.
 */
public class DatosInicialesService {

    private final CategoriaDAO categoriaDAO;
    private final SucursalDAO sucursalDAO;
    private final ProveedorDAO proveedorDAO;
    private final ProductoDAO productoDAO;
    private final IngresoInventarioDAO ingresoDAO;

    public DatosInicialesService(CategoriaDAO categoriaDAO,
                                 SucursalDAO sucursalDAO,
                                 ProveedorDAO proveedorDAO,
                                 ProductoDAO productoDAO,
                                 IngresoInventarioDAO ingresoDAO) {
        this.categoriaDAO = categoriaDAO;
        this.sucursalDAO = sucursalDAO;
        this.proveedorDAO = proveedorDAO;
        this.productoDAO = productoDAO;
        this.ingresoDAO = ingresoDAO;
    }

    public void cargarDatosSemilla() {
        Categoria catLacteos = asegurarCategoria("CAT-LAC", "Lácteos y Derivados", "Pasillo 2 - Refrigerados", true);
        Categoria catCarnicos = asegurarCategoria("CAT-CAR", "Cárnicos, Aves y Embutidos", "Pasillo 1 - Frigoríficos", true);
        Categoria catAbarrotes = asegurarCategoria("CAT-ABA", "Abarrotes y Despensa", "Pasillo 5 - Secos", false);
        Categoria catBebidas = asegurarCategoria("CAT-BEB", "Bebidas y Licores", "Pasillo 7 - Bebidas", false);
        Categoria catLimpieza = asegurarCategoria("CAT-LIM", "Limpieza y Hogar", "Pasillo 9 - Hogar", false);

        Sucursal sucBatan = asegurarSucursal("SMX-UIO-BAT", "Supermaxi El Batán", "Quito", "Av. 6 de Diciembre y Eloy Alfaro", false);
        Sucursal sucCumbaya = asegurarSucursal("SMX-UIO-CUM", "Supermaxi Cumbayá", "Quito", "Scala Shopping, Cumbayá", false);
        Sucursal sucMallRio = asegurarSucursal("SMX-CUE-RIO", "Supermaxi Mall del Río", "Cuenca", "Av. Felipe II y Autopista", false);
        Sucursal sucCeibos = asegurarSucursal("SMX-GYE-CEI", "Supermaxi Los Ceibos", "Guayaquil", "Av. del Bombero Km 6", false);
        Sucursal cdAmaguana = asegurarSucursal("CD-AMG-01", "Centro de Distribución Amaguaña", "Quito", "Panamericana Sur Km 18", true);

        Proveedor pronaca = asegurarProveedor(
                "1790012345001", "PRONACA C.A.", "Cárnicos, Aves y Embutidos",
                "022999000", "ventas@pronaca.com", "Av. De los Shyris N36-120, Quito",
                "CTR-SMX-2026-001", 45, 2.5, "Entrega refrigerada diaria 06:00 en CD Amaguaña"
        );
        Proveedor floralp = asegurarProveedor(
                "1790054321001", "FLORALP S.A.", "Lácteos y Derivados",
                "062987654", "pedidos@floralp.com.ec", "Panamericana Norte Km 2, Ibarra",
                "CTR-SMX-2026-002", 30, 1.5, "Cadena de frío continua 2°C - 4°C"
        );
        Proveedor ales = asegurarProveedor(
                "0990011223001", "INDUSTRIAS ALES C.A.", "Abarrotes y Despensa",
                "042598100", "comercial@ales.com.ec", "Vía Daule Km 5.5, Guayaquil",
                "CTR-SMX-2026-003", 60, 3.0, "Paletizado estándar 48 horas tras orden de compra"
        );
        Proveedor toni = asegurarProveedor(
                "1791234567001", "TONI CORP S.A.", "Lácteos y Derivados",
                "042100200", "distribucion@tonicorp.com", "Km 7.5 Vía a Daule, Guayaquil",
                "CTR-SMX-2026-004", 45, 2.0, "Entrega directa a locales principales y CD"
        );
        Proveedor arca = asegurarProveedor(
                "0990022446001", "ARCA CONTINENTAL ECUADOR S.A.", "Bebidas y Licores",
                "023960400", "cuentas@arcacontal.com", "Av. Galo Plaza Lasso, Quito",
                "CTR-SMX-2026-005", 30, 1.0, "Reposición automática semanal en sucursales"
        );

        Producto queso = asegurarProducto("7861000123456", "Queso Mozzarella Floralp 500g", 4.85, 20,
                floralp, catLacteos, List.of(sucBatan, sucCumbaya, sucMallRio, cdAmaguana));
        Producto pollo = asegurarProducto("7861000234567", "Pechuga de Pollo Mr. Pollo 1kg", 5.60, 35,
                pronaca, catCarnicos, List.of(sucBatan, sucCumbaya, sucMallRio, sucCeibos, cdAmaguana));
        Producto salchicha = asegurarProducto("7861000345678", "Salchicha Vienesa Plumrose 400g", 3.25, 18,
                pronaca, catCarnicos, List.of(sucBatan, sucMallRio, cdAmaguana));
        Producto aceite = asegurarProducto("7861000456789", "Aceite Girasol Ales 1L", 3.90, 42,
                ales, catAbarrotes, List.of(sucBatan, sucCumbaya, sucCeibos, cdAmaguana));
        Producto jabon = asegurarProducto("7861000567890", "Detergente en Polvo Deja 2kg", 6.40, 14,
                ales, catLimpieza, List.of(sucBatan, sucMallRio, sucCeibos));
        Producto yogurt = asegurarProducto("7861000678901", "Yogurt Toni Griego Natural 1L", 3.75, 25,
                toni, catLacteos, List.of(sucBatan, sucCumbaya, sucCeibos, cdAmaguana));
        Producto leche = asegurarProducto("7861000789012", "Leche Entera Toni UHT 1L", 1.15, 60,
                toni, catLacteos, List.of(sucBatan, sucCumbaya, sucMallRio, sucCeibos, cdAmaguana));
        Producto agua = asegurarProducto("7861000890123", "Agua Mineral Dasani 1.5L Pack x6", 4.20, 12,
                arca, catBebidas, List.of(sucBatan, sucCumbaya, sucMallRio, sucCeibos));

        asegurarIngreso("GUIA-001-002-000154", 50, 3.40, "Entrega refrigerada en bodega central Supermaxi",
                queso, floralp, cdAmaguana, LocalDateTime.now().minusDays(6));
        asegurarIngreso("GUIA-001-001-000890", 80, 4.10, "Lote avícola fresco certificado",
                pollo, pronaca, cdAmaguana, LocalDateTime.now().minusDays(5));
        asegurarIngreso("GUIA-001-001-000891", 40, 2.30, "Reposición semanal de embutidos Cuenca",
                salchicha, pronaca, sucMallRio, LocalDateTime.now().minusDays(4));
        asegurarIngreso("GUIA-002-004-001203", 120, 2.85, "Ingreso mayorista de despensa Guayaquil",
                aceite, ales, sucCeibos, LocalDateTime.now().minusDays(3));
        asegurarIngreso("GUIA-002-004-001204", 35, 4.75, "Stock promocional fin de mes",
                jabon, ales, sucBatan, LocalDateTime.now().minusDays(2));
        asegurarIngreso("GUIA-003-001-000512", 75, 2.60, "Entrega refrigerada Cumbayá",
                yogurt, toni, sucCumbaya, LocalDateTime.now().minusDays(1));
        asegurarIngreso("GUIA-003-001-000513", 150, 0.85, "Abastecimiento UHT centro de distribución",
                leche, toni, cdAmaguana, LocalDateTime.now().minusHours(12));
        asegurarIngreso("GUIA-004-002-000301", 60, 2.95, "Reposición bebidas El Batán",
                agua, arca, sucBatan, LocalDateTime.now().minusHours(4));
    }

    private Categoria asegurarCategoria(String codigo, String nombre, String pasillo, boolean frio) {
        Categoria existente = categoriaDAO.buscarPorCodigo(codigo);
        if (existente != null) {
            return existente;
        }
        Categoria nueva = new Categoria(codigo, nombre, pasillo, frio);
        categoriaDAO.crear(nueva);
        return nueva;
    }

    private Sucursal asegurarSucursal(String codigo, String nombre, String ciudad, String direccion, boolean esCentro) {
        Sucursal existente = sucursalDAO.buscarPorCodigo(codigo);
        if (existente != null) {
            return existente;
        }
        Sucursal nueva = new Sucursal(codigo, nombre, ciudad, direccion, esCentro);
        sucursalDAO.crear(nueva);
        return nueva;
    }

    private Proveedor asegurarProveedor(String ruc, String razonSocial, String categoria,
                                        String telefono, String correo, String direccion,
                                        String numeroContrato, int diasCredito,
                                        double descuento, String condiciones) {
        Proveedor existente = proveedorDAO.buscarPorRuc(ruc);
        if (existente != null) {
            if (existente.getContratoComercial() == null) {
                ContratoComercial contrato = new ContratoComercial(
                        numeroContrato, diasCredito, descuento,
                        LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10), condiciones);
                existente.asignarContratoComercial(contrato);
                return proveedorDAO.actualizar(existente);
            }
            return existente;
        }
        Proveedor proveedor = new Proveedor(ruc, razonSocial, categoria, telefono, correo, direccion);
        ContratoComercial contrato = new ContratoComercial(
                numeroContrato, diasCredito, descuento,
                LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(10), condiciones);
        proveedor.asignarContratoComercial(contrato);
        proveedorDAO.crear(proveedor);
        return proveedor;
    }

    private Producto asegurarProducto(String codigoBarras, String nombre, double precio, int stockInicial,
                                      Proveedor proveedor, Categoria categoria, List<Sucursal> sucursales) {
        Producto existente = productoDAO.buscarPorCodigoBarras(codigoBarras);
        if (existente != null) {
            boolean actualizado = false;
            if (existente.getCategoria() == null && categoria != null) {
                existente.setCategoria(categoria);
                actualizado = true;
            }
            if (existente.getSucursales().isEmpty() && sucursales != null) {
                for (Sucursal s : sucursales) {
                    existente.agregarSucursal(s);
                }
                actualizado = true;
            }
            return actualizado ? productoDAO.actualizar(existente) : existente;
        }
        Producto producto = new Producto(codigoBarras, nombre, precio, stockInicial, proveedor, categoria);
        if (sucursales != null) {
            for (Sucursal s : sucursales) {
                producto.agregarSucursal(s);
            }
        }
        productoDAO.crear(producto);
        return producto;
    }

    private void asegurarIngreso(String numeroGuia, int cantidad, double costo, String observaciones,
                                 Producto producto, Proveedor proveedor, Sucursal sucursal, LocalDateTime fecha) {
        if (!ingresoDAO.buscarPorNumeroGuia(numeroGuia).isEmpty()) {
            return;
        }
        IngresoInventario ingreso = new IngresoInventario(
                numeroGuia, cantidad, costo, observaciones, producto, proveedor, sucursal);
        ingreso.setFechaIngreso(fecha);
        ingresoDAO.crear(ingreso);
    }
}
