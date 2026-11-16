package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.controlador.IngresoInventarioControlador;
import ec.edu.ups.supermaxi.controlador.ProductoControlador;
import ec.edu.ups.supermaxi.controlador.ProveedorControlador;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de Supermaxi que integra todos los módulos ORM JPA:
 * CRUD Proveedores + Contrato (1:1), Create Producto (1:N / N:M),
 * Create Ingreso al Inventario y Reportes JPQL paginados.
 */
public class VentanaPrincipalSupermaxi extends JFrame {

    private final PanelProveedores panelProveedores;
    private final PanelProductos panelProductos;
    private final PanelIngresoInventario panelIngresoInventario;
    private final PanelReportesJPQL panelReportesJPQL;

    public VentanaPrincipalSupermaxi(ProveedorControlador proveedorControlador,
                                     ProductoControlador productoControlador,
                                     IngresoInventarioControlador ingresoControlador) {
        setTitle("Supermaxi - Sistema de Catálogo, Proveedores e Inventario (JPA / Hibernate)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);

        panelProveedores = new PanelProveedores(proveedorControlador);
        panelProductos = new PanelProductos(productoControlador, proveedorControlador);
        panelIngresoInventario = new PanelIngresoInventario(ingresoControlador, productoControlador, proveedorControlador);
        panelReportesJPQL = new PanelReportesJPQL(ingresoControlador, productoControlador);

        JTabbedPane pestanias = new JTabbedPane();
        pestanias.addTab("1. Proveedores y Contrato 1:1 (CRUD)", panelProveedores);
        pestanias.addTab("2. Catálogo de Productos (Create)", panelProductos);
        pestanias.addTab("3. Ingreso al Inventario (Create)", panelIngresoInventario);
        pestanias.addTab("4. Reportes JPQL Paginados", panelReportesJPQL);

        pestanias.addChangeListener(e -> {
            int idx = pestanias.getSelectedIndex();
            switch (idx) {
                case 0 -> panelProveedores.recargarTabla();
                case 1 -> panelProductos.recargarCombosYTabla();
                case 2 -> panelIngresoInventario.recargarCombosYTabla();
                case 3 -> panelReportesJPQL.recargarCombosYReporte();
                default -> {
                }
            }
        });

        setLayout(new BorderLayout());
        add(pestanias, BorderLayout.CENTER);
    }
}
