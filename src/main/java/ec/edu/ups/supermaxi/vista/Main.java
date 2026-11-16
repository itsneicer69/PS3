package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.controlador.DatosInicialesService;
import ec.edu.ups.supermaxi.controlador.IngresoInventarioControlador;
import ec.edu.ups.supermaxi.controlador.ProductoControlador;
import ec.edu.ups.supermaxi.controlador.ProveedorControlador;
import ec.edu.ups.supermaxi.dao.CategoriaDAO;
import ec.edu.ups.supermaxi.dao.ContratoComercialDAO;
import ec.edu.ups.supermaxi.dao.IngresoInventarioDAO;
import ec.edu.ups.supermaxi.dao.ProductoDAO;
import ec.edu.ups.supermaxi.dao.ProveedorDAO;
import ec.edu.ups.supermaxi.dao.SucursalDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    private static EntityManagerFactory emf;
    private static EntityManager em;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(Main::iniciarAplicacion);
    }

    private static void iniciarAplicacion() {
        try {
            emf = Persistence.createEntityManagerFactory("SupermaxiPU");
            em = emf.createEntityManager();

            ProveedorDAO proveedorDAO = new ProveedorDAO(em);
            ContratoComercialDAO contratoDAO = new ContratoComercialDAO(em);
            CategoriaDAO categoriaDAO = new CategoriaDAO(em);
            SucursalDAO sucursalDAO = new SucursalDAO(em);
            ProductoDAO productoDAO = new ProductoDAO(em);
            IngresoInventarioDAO ingresoDAO = new IngresoInventarioDAO(em);

            DatosInicialesService datosIniciales = new DatosInicialesService(
                    categoriaDAO, sucursalDAO, proveedorDAO, productoDAO, ingresoDAO
            );
            datosIniciales.cargarDatosSemilla();

            ProveedorControlador proveedorControlador = new ProveedorControlador(proveedorDAO, contratoDAO);
            ProductoControlador productoControlador = new ProductoControlador(
                    productoDAO, proveedorDAO, categoriaDAO, sucursalDAO
            );
            IngresoInventarioControlador ingresoControlador = new IngresoInventarioControlador(
                    ingresoDAO, productoDAO, proveedorDAO, sucursalDAO
            );

            VentanaPrincipalSupermaxi ventana = new VentanaPrincipalSupermaxi(
                    proveedorControlador, productoControlador, ingresoControlador
            );
            ventana.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    cerrarConexionJPA();
                }
            });
            ventana.setVisible(true);

        } catch (Exception e) {
            System.err.println("Error al iniciar JPA: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al conectar con la Unidad de Persistencia JPA:\n" + e.getMessage(),
                    "Error de Inicialización",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void cerrarConexionJPA() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
