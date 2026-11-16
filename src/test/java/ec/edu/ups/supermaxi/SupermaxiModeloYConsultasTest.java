package ec.edu.ups.supermaxi;

import ec.edu.ups.supermaxi.controlador.DatosInicialesService;
import ec.edu.ups.supermaxi.controlador.IngresoInventarioControlador;
import ec.edu.ups.supermaxi.controlador.ProductoControlador;
import ec.edu.ups.supermaxi.controlador.ProveedorControlador;
import ec.edu.ups.supermaxi.dao.*;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.dto.ResumenAbastecimientoDTO;
import ec.edu.ups.supermaxi.modelo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SupermaxiModeloYConsultasTest {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    private static ProveedorDAO proveedorDAO;
    private static ContratoComercialDAO contratoDAO;
    private static CategoriaDAO categoriaDAO;
    private static SucursalDAO sucursalDAO;
    private static ProductoDAO productoDAO;
    private static IngresoInventarioDAO ingresoDAO;

    private static ProveedorControlador proveedorControlador;
    private static ProductoControlador productoControlador;
    private static IngresoInventarioControlador ingresoControlador;

    @BeforeAll
    public static void setUpClass() {
        emf = TestPUHelper.crearEntityManagerFactory();
        em = emf.createEntityManager();

        proveedorDAO = new ProveedorDAO(em);
        contratoDAO = new ContratoComercialDAO(em);
        categoriaDAO = new CategoriaDAO(em);
        sucursalDAO = new SucursalDAO(em);
        productoDAO = new ProductoDAO(em);
        ingresoDAO = new IngresoInventarioDAO(em);

        proveedorControlador = new ProveedorControlador(proveedorDAO, contratoDAO);
        productoControlador = new ProductoControlador(productoDAO, proveedorDAO, categoriaDAO, sucursalDAO);
        ingresoControlador = new IngresoInventarioControlador(ingresoDAO, productoDAO, proveedorDAO, sucursalDAO);

        DatosInicialesService semilla = new DatosInicialesService(
                categoriaDAO, sucursalDAO, proveedorDAO, productoDAO, ingresoDAO);
        semilla.cargarDatosSemilla();
    }

    @AfterAll
    public static void tearDownClass() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Validar relaciones ORM 1:1, 1:N y N:M entre las 6 entidades de Supermaxi")
    public void testRelacionesOrmCompletas() {
        Proveedor pronaca = proveedorDAO.buscarPorRuc("1790012345001");
        assertNotNull(pronaca);

        // Relación 1:1 Proveedor <-> ContratoComercial
        assertNotNull(pronaca.getContratoComercial(), "Debe tener contrato comercial 1:1");
        assertEquals("CTR-SMX-2026-001", pronaca.getContratoComercial().getNumeroContrato());
        assertEquals(pronaca.getId(), pronaca.getContratoComercial().getProveedor().getId());

        // Relación 1:N Proveedor -> Producto y Categoria -> Producto
        List<Producto> productosPronaca = productoDAO.listarPorProveedor(pronaca.getId());
        assertEquals(2, productosPronaca.size());
        Producto pollo = productoDAO.buscarPorCodigoBarras("7861000234567");
        assertNotNull(pollo);
        assertNotNull(pollo.getCategoria());
        assertEquals("CAT-CAR", pollo.getCategoria().getCodigo());

        // Relación N:M Producto <-> Sucursal
        assertEquals(5, pollo.getSucursales().size(), "El pollo debe estar habilitado en las 5 sucursales");
    }

    @Test
    @Order(2)
    @DisplayName("2. CREATE End-to-End: Registrar proveedor, producto en catálogo e ingreso de inventario")
    public void testFlujoCreateCompleto() {
        Proveedor nuevoProv = proveedorControlador.registrarProveedor(
                "1792223334001",
                "CONFITECA C.A.",
                "Abarrotes y Despensa",
                "022670100",
                "ventas@confiteca.com.ec",
                "Av. Maldonado Km 11, Quito",
                "CTR-SMX-2026-006",
                30,
                2.0,
                "Entregas semanales lunes y jueves"
        );
        assertNotNull(nuevoProv.getId());

        Categoria catAbarrotes = categoriaDAO.buscarPorCodigo("CAT-ABA");
        Sucursal sucBatan = sucursalDAO.buscarPorCodigo("SMX-UIO-BAT");
        Sucursal sucCumbaya = sucursalDAO.buscarPorCodigo("SMX-UIO-CUM");
        assertNotNull(catAbarrotes);
        assertNotNull(sucBatan);
        assertNotNull(sucCumbaya);

        Producto nuevoProd = productoControlador.registrarProducto(
                "7861000999888",
                "Chocolate Agogo Confiteca 200g",
                2.95,
                10,
                nuevoProv.getId(),
                catAbarrotes.getId(),
                List.of(sucBatan.getId(), sucCumbaya.getId())
        );
        assertNotNull(nuevoProd.getId());
        assertEquals(10, nuevoProd.getStockActual());
        assertEquals(2, nuevoProd.getSucursales().size());

        IngresoInventario ingreso = ingresoControlador.registrarIngreso(
                "GUIA-005-001-000777",
                40,
                2.10,
                "Primer ingreso lote promocional",
                nuevoProd.getId(),
                nuevoProv.getId(),
                sucBatan.getId()
        );
        assertNotNull(ingreso.getId());

        Producto recargado = productoDAO.buscarPorCodigoBarras("7861000999888");
        assertEquals(50, recargado.getStockActual(), "El ingreso de 40 unidades debe sumar al stock inicial (10+40=50)");
    }

    @Test
    @Order(3)
    @DisplayName("3. Consulta JPQL 1 (Proveedores): Filtros, ordenamiento por días de crédito y paginación")
    public void testConsultaJpqlProveedoresPaginada() {
        PaginaResultado<Proveedor> pag0 = proveedorControlador.consultarProveedoresPaginado(
                Boolean.TRUE, null, null, 30, "diasCredito", "DESC", 0, 3);
        PaginaResultado<Proveedor> pag1 = proveedorControlador.consultarProveedoresPaginado(
                Boolean.TRUE, null, null, 30, "diasCredito", "DESC", 1, 3);

        assertEquals(6L, pag0.getTotalElementos());
        assertEquals(2, pag0.getTotalPaginas());
        assertFalse(pag0.tieneAnterior());
        assertTrue(pag0.tieneSiguiente());
        assertTrue(pag1.tieneAnterior());
        assertFalse(pag1.tieneSiguiente());
        assertEquals(3, pag0.getContenido().size());
        assertEquals(3, pag1.getContenido().size());
        assertNotEquals(pag0.getContenido().get(0).getRuc(), pag1.getContenido().get(0).getRuc());
        assertTrue(pag0.getContenido().get(0).getContratoComercial().getDiasCredito()
                >= pag0.getContenido().get(2).getContratoComercial().getDiasCredito());

        System.out.println("=== RESULTADO CONSULTA JPQL 1: PROVEEDORES ACTIVO + CREDITO >= 30 (ORDEN: diasCredito DESC, PAG 0, TAM 3) ===");
        for (Proveedor p : pag0.getContenido()) {
            System.out.printf("  ID=%d | RUC=%s | %s | Contrato=%s | Crédito=%d días%n",
                    p.getId(), p.getRuc(), p.getRazonSocial(),
                    p.getContratoComercial().getNumeroContrato(),
                    p.getContratoComercial().getDiasCredito());
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. Consulta JPQL 2 (Productos): Filtro por sucursal N:M, stock máximo, ordenamiento y paginación")
    public void testConsultaJpqlProductosPaginada() {
        Sucursal sucBatan = sucursalDAO.buscarPorCodigo("SMX-UIO-BAT");
        assertNotNull(sucBatan);

        PaginaResultado<Producto> pag0 = productoControlador.consultarProductosPaginado(
                null, null, null, sucBatan.getId(), 150, null, "precioUnitario", "DESC", 0, 3);
        PaginaResultado<Producto> pag1 = productoControlador.consultarProductosPaginado(
                null, null, null, sucBatan.getId(), 150, null, "precioUnitario", "DESC", 1, 3);

        assertTrue(pag0.getTotalElementos() >= 6);
        assertEquals(3, pag0.getContenido().size());
        assertFalse(pag1.getContenido().isEmpty());
        assertTrue(pag0.getContenido().get(0).getPrecioUnitario()
                >= pag0.getContenido().get(1).getPrecioUnitario());

        System.out.println("=== RESULTADO CONSULTA JPQL 2: PRODUCTOS EN SUPERMAXI EL BATÁN (ORDEN: precioUnitario DESC, PAG 0, TAM 3) ===");
        for (Producto prod : pag0.getContenido()) {
            System.out.printf("  SKU=%s | %s | $%.2f | Stock=%d | Cat=%s | Prov=%s | Sucursales=%d%n",
                    prod.getCodigoBarras(), prod.getNombre(), prod.getPrecioUnitario(),
                    prod.getStockActual(), prod.getCategoria().getNombre(),
                    prod.getProveedor().getRazonSocial(), prod.getSucursales().size());
        }
    }

    @Test
    @Order(5)
    @DisplayName("5. Consulta JPQL 3 (Ingresos Inventario): Filtro por cantidad mínima, ordenamiento y paginación")
    public void testConsultaJpqlIngresosPaginada() {
        PaginaResultado<IngresoInventario> pag0 = ingresoControlador.consultarIngresosPaginado(
                null, null, null, 50, null, null, null, "cantidadIngresada", "DESC", 0, 3);
        PaginaResultado<IngresoInventario> pag1 = ingresoControlador.consultarIngresosPaginado(
                null, null, null, 50, null, null, null, "cantidadIngresada", "DESC", 1, 3);

        assertTrue(pag0.getTotalElementos() >= 6);
        assertEquals(3, pag0.getContenido().size());
        assertFalse(pag1.getContenido().isEmpty());
        assertTrue(pag0.getContenido().get(0).getCantidadIngresada() >= 50);
        assertTrue(pag0.getContenido().get(0).getCantidadIngresada()
                >= pag0.getContenido().get(2).getCantidadIngresada());

        System.out.println("=== RESULTADO CONSULTA JPQL 3: INGRESOS CANTIDAD >= 50 (ORDEN: cantidadIngresada DESC, PAG 0, TAM 3) ===");
        for (IngresoInventario ing : pag0.getContenido()) {
            System.out.printf("  Guía=%s | %s | Prov=%s | Suc=%s | Cant=%d | Costo=$%.2f%n",
                    ing.getNumeroGuiaRemision(), ing.getProducto().getNombre(),
                    ing.getProveedor().getRazonSocial(), ing.getSucursal().getNombre(),
                    ing.getCantidadIngresada(), ing.getCostoUnitarioCompra());
        }
    }

    @Test
    @Order(6)
    @DisplayName("6. Consulta JPQL 4 (Reporte Agregado DTO): GROUP BY, HAVING, ORDER BY y paginación")
    public void testConsultaJpqlResumenAbastecimientoPaginado() {
        PaginaResultado<ResumenAbastecimientoDTO> pag0 = ingresoControlador.consultarResumenAbastecimiento(
                Boolean.TRUE, null, 150.0, "inversionTotal", "DESC", 0, 3);
        PaginaResultado<ResumenAbastecimientoDTO> pag1 = ingresoControlador.consultarResumenAbastecimiento(
                Boolean.TRUE, null, 150.0, "inversionTotal", "DESC", 1, 3);

        assertTrue(pag0.getTotalElementos() >= 4);
        assertEquals(3, pag0.getContenido().size());
        assertFalse(pag1.getContenido().isEmpty());
        assertTrue(pag0.getContenido().get(0).getInversionTotal() >= 150.0);
        assertTrue(pag0.getContenido().get(0).getInversionTotal()
                >= pag0.getContenido().get(1).getInversionTotal());

        System.out.println("=== RESULTADO CONSULTA JPQL 4: RESUMEN ABASTECIMIENTO HAVING >= $150 (ORDEN: inversionTotal DESC, PAG 0, TAM 3) ===");
        for (ResumenAbastecimientoDTO dto : pag0.getContenido()) {
            System.out.printf("  RUC=%s | %s | Entregas=%d | Unidades=%d | Inversión=$%.2f%n",
                    dto.getRuc(), dto.getRazonSocial(), dto.getTotalEntregas(),
                    dto.getTotalUnidades(), dto.getInversionTotal());
        }
    }

    @Test
    @Order(7)
    @DisplayName("7. Validaciones de negocio y baja lógica en controladores")
    public void testValidacionesYBajaLogica() {
        // RUC duplicado
        assertThrows(IllegalArgumentException.class, () ->
                proveedorControlador.registrarProveedor(
                        "1790012345001", "OTRO", "Abarrotes", "022000111", "a@b.com", "Quito"));

        // Precio inválido y sin sucursales en producto
        Proveedor pronaca = proveedorDAO.buscarPorRuc("1790012345001");
        Categoria cat = categoriaDAO.buscarPorCodigo("CAT-CAR");
        assertThrows(IllegalArgumentException.class, () ->
                productoControlador.registrarProducto(
                        "7869991112223", "Producto Inválido", -5.0, 10,
                        pronaca.getId(), cat.getId(), List.of()));

        // Cantidad cero en ingreso de inventario
        Producto pollo = productoDAO.buscarPorCodigoBarras("7861000234567");
        Sucursal cd = sucursalDAO.buscarPorCodigo("CD-AMG-01");
        assertThrows(IllegalArgumentException.class, () ->
                ingresoControlador.registrarIngreso(
                        "GUIA-ERR-01", 0, 2.0, "", pollo.getId(), pronaca.getId(), cd.getId()));

        // Baja lógica de proveedor y exclusión en consulta JPQL de activos
        Proveedor confiteca = proveedorDAO.buscarPorRuc("1792223334001");
        assertNotNull(confiteca);
        proveedorControlador.desactivarProveedor(confiteca.getId());

        PaginaResultado<Proveedor> soloActivos = proveedorControlador.consultarProveedoresPaginado(
                Boolean.TRUE, "CONFITECA", null, null, "razonSocial", "ASC", 0, 10);
        assertEquals(0L, soloActivos.getTotalElementos(), "Un proveedor inactivo no debe aparecer al filtrar activo=true");

        PaginaResultado<Proveedor> soloInactivos = proveedorControlador.consultarProveedoresPaginado(
                Boolean.FALSE, "CONFITECA", null, null, "razonSocial", "ASC", 0, 10);
        assertEquals(1L, soloInactivos.getTotalElementos(), "Debe encontrarse al filtrar activo=false");
    }

    @Test
    @Order(8)
    @DisplayName("8. UPDATE inválido: una validación fallida no debe dejar cambios pendientes que se guarden después")
    public void testActualizacionInvalidaNoDejaCambiosSucios() {
        Proveedor pronaca = proveedorDAO.buscarPorRuc("1790012345001");
        String razonOriginal = pronaca.getRazonSocial();

        assertThrows(IllegalArgumentException.class, () ->
                proveedorControlador.actualizarProveedor(
                        pronaca.getId(), pronaca.getRuc(), "RAZON MODIFICADA", pronaca.getCategoriaSuministro(),
                        pronaca.getTelefono(), pronaca.getCorreo(), pronaca.getDireccion(), true,
                        "CTR-SMX-2026-001", 45, 150.0, null));

        // Otra operación cualquiera que confirma una transacción en el mismo EntityManager
        sucursalDAO.crear(new Sucursal("SMX-TEST-REG", "Sucursal Regresión", "Quito", "N/A", false));

        em.clear();
        Proveedor recargado = proveedorDAO.buscarPorRuc("1790012345001");
        assertEquals(razonOriginal, recargado.getRazonSocial(),
                "La razón social no debe cambiar si la actualización fue rechazada");
    }

    @Test
    @Order(9)
    @DisplayName("9. El EntityManager compartido debe seguir operativo tras una transacción fallida")
    public void testEntityManagerRecuperaTrasError() {
        assertThrows(RuntimeException.class, () ->
                categoriaDAO.crear(new Categoria("CAT-LAC", "Duplicada", "N/A", false)));

        Categoria nueva = new Categoria("CAT-REG", "Categoría Regresión", "Pasillo 99", false);
        assertDoesNotThrow(() -> categoriaDAO.crear(nueva));
        assertNotNull(categoriaDAO.buscarPorCodigo("CAT-REG"));
    }

    @Test
    @Order(10)
    @DisplayName("10. Ingreso de inventario: proveedor, sucursal y guía deben ser coherentes con el producto")
    public void testValidacionesCoherenciaIngreso() {
        Producto pollo = productoDAO.buscarPorCodigoBarras("7861000234567");   // PRONACA
        Producto jabon = productoDAO.buscarPorCodigoBarras("7861000567890");   // ALES, no se vende en Cumbayá
        Proveedor pronaca = proveedorDAO.buscarPorRuc("1790012345001");
        Proveedor floralp = proveedorDAO.buscarPorRuc("1790054321001");
        Proveedor ales = proveedorDAO.buscarPorRuc("0990011223001");
        Sucursal cd = sucursalDAO.buscarPorCodigo("CD-AMG-01");
        Sucursal cumbaya = sucursalDAO.buscarPorCodigo("SMX-UIO-CUM");

        // Proveedor que no abastece el producto
        assertThrows(IllegalArgumentException.class, () ->
                ingresoControlador.registrarIngreso("GUIA-REG-01", 10, 4.0, "",
                        pollo.getId(), floralp.getId(), cd.getId()));

        // Sucursal donde no se comercializa el producto
        assertThrows(IllegalArgumentException.class, () ->
                ingresoControlador.registrarIngreso("GUIA-REG-02", 10, 4.0, "",
                        jabon.getId(), ales.getId(), cumbaya.getId()));

        // Guía ya usada para el mismo producto (dato semilla)
        assertThrows(IllegalArgumentException.class, () ->
                ingresoControlador.registrarIngreso("GUIA-001-001-000890", 10, 4.0, "",
                        pollo.getId(), pronaca.getId(), cd.getId()));

        // Un centro de distribución puede recibir el producto aunque no lo comercialice
        assertDoesNotThrow(() ->
                ingresoControlador.registrarIngreso("GUIA-REG-03", 5, 4.0, "",
                        jabon.getId(), ales.getId(), cd.getId()));
    }
}
