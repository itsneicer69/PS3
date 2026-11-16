package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.Categoria;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import ec.edu.ups.supermaxi.modelo.Sucursal;

import javax.swing.*;
import java.awt.*;

/**
 * Utilidades compartidas de interfaz gráfica Swing (renderizadores, layouts y paginación).
 */
public final class VistaUtil {

    private VistaUtil() {
    }

    public static void agregarCampo(JPanel panel, GridBagConstraints gbc, int col, int fila, String texto, JComponent comp) {
        gbc.gridx = col;
        gbc.gridy = fila;
        gbc.weightx = 0.08;
        panel.add(new JLabel(texto), gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 0.42;
        panel.add(comp, gbc);
    }

    public static ListCellRenderer<Object> crearRenderizadorProveedor(String textoTodos) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(textoTodos);
                } else if (value instanceof Proveedor p) {
                    setText(p.getRazonSocial() + " (" + p.getRuc() + ")");
                }
                return this;
            }
        };
    }

    public static ListCellRenderer<Object> crearRenderizadorCategoria(String textoTodos) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(textoTodos);
                } else if (value instanceof Categoria c) {
                    setText(c.getNombre() + (c.isRequiereCadenaFrio() ? " [Frío]" : ""));
                }
                return this;
            }
        };
    }

    public static ListCellRenderer<Object> crearRenderizadorSucursal(String textoTodos) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(textoTodos);
                } else if (value instanceof Sucursal s) {
                    setText(s.getNombre() + " (" + s.getCiudad() + ")");
                }
                return this;
            }
        };
    }

    public static ListCellRenderer<Object> crearRenderizadorProducto(String textoTodos) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(textoTodos);
                } else if (value instanceof Producto p) {
                    setText(p.getNombre() + " [" + p.getCodigoBarras() + " | Stock: " + p.getStockActual() + "]");
                }
                return this;
            }
        };
    }

    public static void actualizarEstadoPaginacion(PaginaResultado<?> pagina,
                                                  JLabel lblPagina,
                                                  JButton btnAnterior,
                                                  JButton btnSiguiente) {
        int totalPaginas = Math.max(1, pagina.getTotalPaginas());
        int paginaMostrada = pagina.getTotalElementos() == 0 ? 1 : pagina.getPaginaActual() + 1;
        lblPagina.setText(String.format("Página %d de %d (Total registros: %d)",
                paginaMostrada, totalPaginas, pagina.getTotalElementos()));
        btnAnterior.setEnabled(pagina.tieneAnterior());
        btnSiguiente.setEnabled(pagina.tieneSiguiente());
    }
}
