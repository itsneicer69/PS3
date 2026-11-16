package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.controlador.ProveedorControlador;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana independiente para la gestión CRUD de Proveedores (mantiene compatibilidad directa).
 */
public class VentanaProveedor extends JFrame {

    private final PanelProveedores panelProveedores;

    public VentanaProveedor(ProveedorControlador controlador) {
        setTitle("Supermaxi - Gestión de Proveedores (CRUD JPA)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 680);
        setLocationRelativeTo(null);

        this.panelProveedores = new PanelProveedores(controlador);
        setLayout(new BorderLayout());
        add(panelProveedores, BorderLayout.CENTER);
    }

    public void cargarTablaProveedores() {
        panelProveedores.recargarTabla();
    }
}
