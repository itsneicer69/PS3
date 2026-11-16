package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.controlador.ProductoControlador;
import ec.edu.ups.supermaxi.controlador.ProveedorControlador;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.Categoria;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import ec.edu.ups.supermaxi.modelo.Sucursal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel para el registro (Create) de nuevos productos en el catálogo de Supermaxi
 * y consulta JPQL paginada con relaciones 1:N (Proveedor, Categoría) y N:M (Sucursales).
 */
public class PanelProductos extends JPanel {

    private final ProductoControlador productoControlador;
    private final ProveedorControlador proveedorControlador;

    private JTextField txtCodigoBarras;
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtStockInicial;
    private JComboBox<Proveedor> cbxProveedor;
    private JComboBox<Categoria> cbxCategoria;
    private JPanel panelSucursalesCheck;
    private final Map<Long, JCheckBox> checksSucursales = new LinkedHashMap<>();

    // Filtros, orden y paginación JPQL
    private JTextField txtFiltroTexto;
    private JComboBox<Categoria> cbxFiltroCategoria;
    private JComboBox<Proveedor> cbxFiltroProveedor;
    private JComboBox<Sucursal> cbxFiltroSucursal;
    private JTextField txtFiltroStockMax;
    private JComboBox<String> cbxOrdenCampo;
    private JComboBox<String> cbxOrdenDir;
    private JComboBox<Integer> cbxTamanioPagina;

    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JLabel lblEstadoPagina;

    private DefaultTableModel modeloTabla;
    private int paginaActual = 0;

    public PanelProductos(ProductoControlador productoControlador, ProveedorControlador proveedorControlador) {
        this.productoControlador = productoControlador;
        this.proveedorControlador = proveedorControlador;
        inicializarComponentes();
        recargarCombosYTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Formulario Create Producto
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(new TitledBorder("Registrar Nuevo Producto en el Catálogo (Create)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCodigoBarras = new JTextField(16);
        txtNombre = new JTextField(22);
        txtPrecio = new JTextField(10);
        txtStockInicial = new JTextField("0", 10);

        cbxProveedor = new JComboBox<>();
        cbxProveedor.setRenderer(VistaUtil.crearRenderizadorProveedor("Seleccione proveedor..."));
        cbxCategoria = new JComboBox<>();
        cbxCategoria.setRenderer(VistaUtil.crearRenderizadorCategoria("Seleccione categoría..."));

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 0, "Código de Barras:", txtCodigoBarras);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 0, "Nombre Producto:", txtNombre);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 1, "Precio Unitario ($):", txtPrecio);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 1, "Stock Inicial:", txtStockInicial);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 2, "Proveedor (N:1):", cbxProveedor);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 2, "Categoría (N:1):", cbxCategoria);

        panelSucursalesCheck = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        panelSucursalesCheck.setBorder(new TitledBorder("Sucursales Habilitadas (Relación N:M)"));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        JButton btnRegistrar = new JButton("Registrar Producto (Create)");
        JButton btnLimpiar = new JButton("Limpiar Formulario");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnLimpiar);

        JPanel panelNorte = new JPanel(new BorderLayout(4, 4));
        panelNorte.add(panelFormulario, BorderLayout.NORTH);
        panelNorte.add(panelSucursalesCheck, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // 2. Filtros JPQL + Tabla + Paginación
        JPanel panelCentro = new JPanel(new BorderLayout(6, 6));
        panelCentro.setBorder(new TitledBorder("Catálogo de Productos (Consulta JPQL con Filtros, Ordenamiento y Paginación)"));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        txtFiltroTexto = new JTextField(11);
        cbxFiltroCategoria = new JComboBox<>();
        cbxFiltroCategoria.setRenderer(VistaUtil.crearRenderizadorCategoria("Todas las categorías"));
        cbxFiltroProveedor = new JComboBox<>();
        cbxFiltroProveedor.setRenderer(VistaUtil.crearRenderizadorProveedor("Todos los proveedores"));
        cbxFiltroSucursal = new JComboBox<>();
        cbxFiltroSucursal.setRenderer(VistaUtil.crearRenderizadorSucursal("Todas las sucursales"));
        txtFiltroStockMax = new JTextField(4);
        cbxOrdenCampo = new JComboBox<>(new String[]{"nombre", "precioUnitario", "stockActual", "codigoBarras", "categoria", "id"});
        cbxOrdenDir = new JComboBox<>(new String[]{"ASC", "DESC"});
        JButton btnConsultar = new JButton("Consultar JPQL");
        JButton btnLimpiarFiltros = new JButton("Ver Todos");

        panelFiltros.add(new JLabel("Buscar:"));
        panelFiltros.add(txtFiltroTexto);
        panelFiltros.add(new JLabel("Cat.:"));
        panelFiltros.add(cbxFiltroCategoria);
        panelFiltros.add(new JLabel("Prov.:"));
        panelFiltros.add(cbxFiltroProveedor);
        panelFiltros.add(new JLabel("Suc.:"));
        panelFiltros.add(cbxFiltroSucursal);
        panelFiltros.add(new JLabel("Stock <=:"));
        panelFiltros.add(txtFiltroStockMax);
        panelFiltros.add(new JLabel("Orden:"));
        panelFiltros.add(cbxOrdenCampo);
        panelFiltros.add(cbxOrdenDir);
        panelFiltros.add(btnConsultar);
        panelFiltros.add(btnLimpiarFiltros);
        panelCentro.add(panelFiltros, BorderLayout.NORTH);

        String[] columnas = {
                "ID", "Código Barras", "Nombre", "Precio ($)", "Stock", "Categoría (1:N)", "Proveedor (1:N)", "Sucursales (N:M)"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaProductos = new JTable(modeloTabla);
        tablaProductos.setRowHeight(24);
        panelCentro.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        cbxTamanioPagina = new JComboBox<>(new Integer[]{5, 10, 20});
        btnAnterior = new JButton("<< Anterior");
        btnSiguiente = new JButton("Siguiente >>");
        lblEstadoPagina = new JLabel("Página 1 de 1");
        panelPaginacion.add(new JLabel("Tamaño página:"));
        panelPaginacion.add(cbxTamanioPagina);
        panelPaginacion.add(btnAnterior);
        panelPaginacion.add(lblEstadoPagina);
        panelPaginacion.add(btnSiguiente);
        panelCentro.add(panelPaginacion, BorderLayout.SOUTH);

        add(panelCentro, BorderLayout.CENTER);

        // Eventos
        btnRegistrar.addActionListener(e -> accionRegistrar());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        btnConsultar.addActionListener(e -> {
            paginaActual = 0;
            recargarTabla();
        });
        btnLimpiarFiltros.addActionListener(e -> {
            txtFiltroTexto.setText("");
            cbxFiltroCategoria.setSelectedIndex(0);
            cbxFiltroProveedor.setSelectedIndex(0);
            cbxFiltroSucursal.setSelectedIndex(0);
            txtFiltroStockMax.setText("");
            cbxOrdenCampo.setSelectedIndex(0);
            cbxOrdenDir.setSelectedIndex(0);
            paginaActual = 0;
            recargarTabla();
        });
        cbxTamanioPagina.addActionListener(e -> {
            paginaActual = 0;
            recargarTabla();
        });
        btnAnterior.addActionListener(e -> {
            if (paginaActual > 0) {
                paginaActual--;
                recargarTabla();
            }
        });
        btnSiguiente.addActionListener(e -> {
            paginaActual++;
            recargarTabla();
        });
    }

    public void recargarCombosYTabla() {
        cargarCombos();
        recargarTabla();
    }

    private void cargarCombos() {
        Proveedor provSel = (Proveedor) cbxProveedor.getSelectedItem();
        Categoria catSel = (Categoria) cbxCategoria.getSelectedItem();

        cbxProveedor.removeAllItems();
        cbxFiltroProveedor.removeAllItems();
        cbxFiltroProveedor.addItem(null);
        for (Proveedor p : proveedorControlador.listarProveedoresActivos()) {
            cbxProveedor.addItem(p);
            cbxFiltroProveedor.addItem(p);
        }
        if (provSel != null) {
            cbxProveedor.setSelectedItem(provSel);
        }

        cbxCategoria.removeAllItems();
        cbxFiltroCategoria.removeAllItems();
        cbxFiltroCategoria.addItem(null);
        for (Categoria c : productoControlador.listarCategorias()) {
            cbxCategoria.addItem(c);
            cbxFiltroCategoria.addItem(c);
        }
        if (catSel != null) {
            cbxCategoria.setSelectedItem(catSel);
        }

        cbxFiltroSucursal.removeAllItems();
        cbxFiltroSucursal.addItem(null);
        panelSucursalesCheck.removeAll();
        checksSucursales.clear();
        for (Sucursal s : productoControlador.listarSucursales()) {
            cbxFiltroSucursal.addItem(s);
            JCheckBox chk = new JCheckBox(s.getNombre() + " (" + s.getCiudad() + ")", true);
            checksSucursales.put(s.getId(), chk);
            panelSucursalesCheck.add(chk);
        }
        panelSucursalesCheck.revalidate();
        panelSucursalesCheck.repaint();
    }

    private void accionRegistrar() {
        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
            int stock = Integer.parseInt(txtStockInicial.getText().trim());
            Proveedor prov = (Proveedor) cbxProveedor.getSelectedItem();
            Categoria cat = (Categoria) cbxCategoria.getSelectedItem();

            List<Long> sucursalIds = new ArrayList<>();
            for (Map.Entry<Long, JCheckBox> entry : checksSucursales.entrySet()) {
                if (entry.getValue().isSelected()) {
                    sucursalIds.add(entry.getKey());
                }
            }

            productoControlador.registrarProducto(
                    txtCodigoBarras.getText(),
                    txtNombre.getText(),
                    precio,
                    stock,
                    prov != null ? prov.getId() : null,
                    cat != null ? cat.getId() : null,
                    sucursalIds
            );

            JOptionPane.showMessageDialog(this,
                    "Producto registrado correctamente en el catálogo de Supermaxi.",
                    "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            paginaActual = 0;
            recargarTabla();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                    "Verifique que el precio unitario y el stock inicial sean números válidos.",
                    "Datos Numéricos Inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error al Registrar Producto", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void recargarTabla() {
        try {
            Categoria cat = (Categoria) cbxFiltroCategoria.getSelectedItem();
            Proveedor prov = (Proveedor) cbxFiltroProveedor.getSelectedItem();
            Sucursal suc = (Sucursal) cbxFiltroSucursal.getSelectedItem();
            Integer stockMax = txtFiltroStockMax.getText().trim().isEmpty()
                    ? null
                    : Integer.parseInt(txtFiltroStockMax.getText().trim());
            int tamanio = (Integer) cbxTamanioPagina.getSelectedItem();

            PaginaResultado<Producto> pagina = productoControlador.consultarProductosPaginado(
                    txtFiltroTexto.getText(),
                    cat != null ? cat.getId() : null,
                    prov != null ? prov.getId() : null,
                    suc != null ? suc.getId() : null,
                    stockMax,
                    null,
                    (String) cbxOrdenCampo.getSelectedItem(),
                    (String) cbxOrdenDir.getSelectedItem(),
                    paginaActual,
                    tamanio
            );

            modeloTabla.setRowCount(0);
            for (Producto prod : pagina.getContenido()) {
                String sucursalesTxt = prod.getSucursales().stream()
                        .map(Sucursal::getNombre)
                        .collect(Collectors.joining(", "));
                modeloTabla.addRow(new Object[]{
                        prod.getId(),
                        prod.getCodigoBarras(),
                        prod.getNombre(),
                        String.format("%.2f", prod.getPrecioUnitario()),
                        prod.getStockActual(),
                        prod.getCategoria() != null ? prod.getCategoria().getNombre() : "Sin categoría",
                        prod.getProveedor() != null ? prod.getProveedor().getRazonSocial() : "-",
                        sucursalesTxt.isEmpty() ? "Sin sucursales" : sucursalesTxt
                });
            }
            VistaUtil.actualizarEstadoPaginacion(pagina, lblEstadoPagina, btnAnterior, btnSiguiente);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                    "El filtro de stock máximo debe ser un número entero.",
                    "Filtro Inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtCodigoBarras.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStockInicial.setText("0");
        if (cbxProveedor.getItemCount() > 0) cbxProveedor.setSelectedIndex(0);
        if (cbxCategoria.getItemCount() > 0) cbxCategoria.setSelectedIndex(0);
        for (JCheckBox chk : checksSucursales.values()) {
            chk.setSelected(true);
        }
    }
}
