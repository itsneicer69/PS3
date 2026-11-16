package ec.edu.ups.supermaxi;

import ec.edu.ups.supermaxi.dao.ProductoDAO;
import ec.edu.ups.supermaxi.dao.ProveedorDAO;
import ec.edu.ups.supermaxi.dao.SucursalDAO;
import ec.edu.ups.supermaxi.dao.IngresoInventarioDAO;
import ec.edu.ups.supermaxi.modelo.ContratoComercial;
import ec.edu.ups.supermaxi.modelo.IngresoInventario;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import ec.edu.ups.supermaxi.modelo.Sucursal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProveedorDAOTest {

    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static ProveedorDAO proveedorDAO;
    private static ProductoDAO productoDAO;
    private static SucursalDAO sucursalDAO;
    private static IngresoInventarioDAO ingresoDAO;

    private static Long idProveedorPrueba;
    private static final String RUC_PRUEBA = "1799988877001";

    @BeforeAll
    public static void setUpClass() {
        emf = TestPUHelper.crearEntityManagerFactory();
        em = emf.createEntityManager();
        proveedorDAO = new ProveedorDAO(em);
        productoDAO = new ProductoDAO(em);
        sucursalDAO = new SucursalDAO(em);
        ingresoDAO = new IngresoInventarioDAO(em);

        Proveedor previo = proveedorDAO.buscarPorRuc(RUC_PRUEBA);
        if (previo != null) {
            proveedorDAO.eliminar(previo.getId());
        }
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
    @DisplayName("1. CREATE: Debería registrar un nuevo proveedor con Contrato Comercial 1:1 en Supermaxi")
    public void testCrearProveedor() {
        Proveedor nuevo = new Proveedor(
                RUC_PRUEBA,
                "LA FAVORITA DISTRIBUIDORA S.A.",
                "Abarrotes y Despensa",
                "022444555",
                "contacto@favorita.com",
                "Av. General Enríquez, Sangolquí"
        );
        ContratoComercial contrato = new ContratoComercial(
                "CTR-TEST-2026-999",
                45,
                2.5,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                "Entrega en Centro de Distribución Amaguaña"
        );
        nuevo.asignarContratoComercial(contrato);

        proveedorDAO.crear(nuevo);

        assertNotNull(nuevo.getId(), "El ID generado por JPA no debe ser nulo tras persistir");
        assertNotNull(nuevo.getContratoComercial().getId(), "El contrato 1:1 debe persistirse en cascada");
        idProveedorPrueba = nuevo.getId();
    }

    @Test
    @Order(2)
    @DisplayName("2. READ: Debería buscar el proveedor por ID y por RUC con JPQL incluyendo su Contrato 1:1")
    public void testBuscarProveedor() {
        Proveedor porId = proveedorDAO.buscarPorId(idProveedorPrueba);
        assertNotNull(porId, "El proveedor debe existir al buscar por ID");
        assertEquals("LA FAVORITA DISTRIBUIDORA S.A.", porId.getRazonSocial());

        Proveedor porRuc = proveedorDAO.buscarPorRuc(RUC_PRUEBA);
        assertNotNull(porRuc, "El proveedor debe existir al buscar por RUC mediante JPQL");
        assertEquals(idProveedorPrueba, porRuc.getId());
        assertNotNull(porRuc.getContratoComercial());
        assertEquals("CTR-TEST-2026-999", porRuc.getContratoComercial().getNumeroContrato());
    }

    @Test
    @Order(3)
    @DisplayName("3. READ (JPQL): Debería listar los proveedores activos")
    public void testListarProveedoresActivos() {
        List<Proveedor> activos = proveedorDAO.listarActivos();
        assertFalse(activos.isEmpty(), "La lista de proveedores activos no debe estar vacía");
        assertTrue(activos.stream().anyMatch(p -> RUC_PRUEBA.equals(p.getRuc())),
                "El proveedor de prueba debe figurar entre los activos");
    }

    @Test
    @Order(4)
    @DisplayName("4. UPDATE: Debería actualizar el teléfono, dirección y días de crédito del contrato 1:1")
    public void testActualizarProveedor() {
        Proveedor proveedor = proveedorDAO.buscarPorId(idProveedorPrueba);
        assertNotNull(proveedor);

        proveedor.setTelefono("023888999");
        proveedor.setDireccion("Parque Industrial Sur, Quito");
        proveedor.getContratoComercial().setDiasCredito(60);

        Proveedor actualizado = proveedorDAO.actualizar(proveedor);

        assertEquals("023888999", actualizado.getTelefono());
        assertEquals("Parque Industrial Sur, Quito", actualizado.getDireccion());
        assertEquals(60, actualizado.getContratoComercial().getDiasCredito());
    }

    @Test
    @Order(5)
    @DisplayName("5. DELETE: Debería eliminar físicamente un proveedor aún teniendo productos, sucursales N:M e ingresos asociados")
    public void testEliminarProveedor() {
        Proveedor proveedor = proveedorDAO.buscarPorId(idProveedorPrueba);
        assertNotNull(proveedor);

        Sucursal suc = new Sucursal("SMX-TEST-01", "Supermaxi Prueba", "Quito", "Av. Shyris", false);
        sucursalDAO.crear(suc);

        Producto prod = new Producto("7869999000111", "Arroz Supermaxi Test 2kg", 2.40, 10, proveedor);
        prod.agregarSucursal(suc);
        productoDAO.crear(prod);

        IngresoInventario ingreso = new IngresoInventario(
                "GUIA-TEST-001", 25, 1.80, "Ingreso de prueba", prod, proveedor, suc);
        ingresoDAO.crear(ingreso);

        proveedorDAO.eliminar(idProveedorPrueba);

        Proveedor eliminado = proveedorDAO.buscarPorId(idProveedorPrueba);
        assertNull(eliminado, "El proveedor ya no debe existir en la base de datos tras ser eliminado");
        assertNull(productoDAO.buscarPorCodigoBarras("7869999000111"),
                "Los productos asociados deben haberse eliminado en cascada");

        // Limpieza de la sucursal de prueba
        sucursalDAO.eliminar(suc.getId());
    }
}
