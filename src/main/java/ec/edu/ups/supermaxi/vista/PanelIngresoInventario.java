package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.controlador.IngresoInventarioControlador;
import ec.edu.ups.supermaxi.controlador.ProductoControlador;
import ec.edu.ups.supermaxi.controlador.ProveedorControlador;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.IngresoInventario;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import ec.edu.ups.supermaxi.modelo.Sucursal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel para el registro (Create) de ingresos de productos al inventario de Supermaxi
 * y consulta JPQL paginada del historial de recepciones en bodega.
 */
public class PanelIngresoInventario extends JPanel {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final IngresoInventarioControlador ingresoControlador;
    private final ProductoControlador productoControlador;
    private final ProveedorControlador proveedorControlador;

    private JTextField txtNumeroGuia;
    private JComboBox<Proveedor> cbxProveedor;
    private JComboBox<Producto> cbxProducto;
    private JComboBox<Sucursal> cbxSucursal;
    private JTextField txtCantidad;
    private JTextField txtCostoUnitario;
    private JTextField txtObservaciones;

    // Filtros, ordenamiento y paginación JPQL
    private JTextField txtFiltroGuia;
    private JComboBox<Proveedor> cbxFiltroProveedor;
    private JComboBox<Producto> cbxFiltroProducto;
    private JComboBox<Sucursal> cbxFiltroSucursal;
    private JTextField txtFiltroCantMin;
    private JComboBox<String> cbxOrdenCampo;
    private JComboBox<String> cbxOrdenDir;
    private JComboBox<Integer> cbxTamanioPagina;

    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JLabel lblEstadoPagina;

    private DefaultTableModel modeloTabla;
    private int paginaActual = 0;

    public PanelIngresoInventario(IngresoInventarioControlador ingresoControlador,
                                  ProductoControlador productoControlador,
                                  ProveedorControlador proveedorControlador) {
        this.ingresoControlador = ingresoControlador;
        this.productoControlador = productoControlador;
        this.proveedorControlador = proveedorControlador;
        inicializarComponentes();
        recargarCombosYTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Formulario Create Ingreso al Inventario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(new TitledBorder("Registrar Ingreso de Productos al Inventario (Create)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNumeroGuia = new JTextField(18);
        cbxProveedor = new JComboBox<>();
        cbxProveedor.setRenderer(VistaUtil.crearRenderizadorProveedor("Seleccione proveedor..."));
        cbxProducto = new JComboBox<>();
        cbxProducto.setRenderer(VistaUtil.crearRenderizadorProducto("Seleccione producto..."));
        cbxSucursal = new JComboBox<>();
        cbxSucursal.setRenderer(VistaUtil.crearRenderizadorSucursal("Seleccione sucursal/bodega..."));
        txtCantidad = new JTextField(10);
        txtCostoUnitario = new JTextField(10);
        txtObservaciones = new JTextField(24);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 0, "N° Guía Remisión:", txtNumeroGuia);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 0, "Proveedor:", cbxProveedor);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 1, "Producto:", cbxProducto);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 1, "Sucursal Receptora:", cbxSucursal);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 2, "Cantidad Ingresada:", txtCantidad);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 2, "Costo Unit. Compra ($):", txtCostoUnitario);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 3, "Observaciones:", txtObservaciones);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        JButton btnRegistrar = new JButton("Registrar Ingreso al Inventario (Create)");
        JButton btnLimpiar = new JButton("Limpiar Formulario");
        panelBotones.add(btnRegistrar);
        panelBotones.add(btnLimpiar);

        JPanel panelNorte = new JPanel(new BorderLayout(4, 4));
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // 2. Filtros JPQL + Tabla + Paginación
        JPanel panelCentro = new JPanel(new BorderLayout(6, 6));
        panelCentro.setBorder(new TitledBorder("Historial de Ingresos de Inventario (Consulta JPQL Paginada)"));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        txtFiltroGuia = new JTextField(10);
        cbxFiltroProveedor = new JComboBox<>();
        cbxFiltroProveedor.setRenderer(VistaUtil.crearRenderizadorProveedor("Todos los proveedores"));
        cbxFiltroProducto = new JComboBox<>();
        cbxFiltroProducto.setRenderer(VistaUtil.crearRenderizadorProducto("Todos los productos"));
        cbxFiltroSucursal = new JComboBox<>();
        cbxFiltroSucursal.setRenderer(VistaUtil.crearRenderizadorSucursal("Todas las sucursales"));
        txtFiltroCantMin = new JTextField(4);
        cbxOrdenCampo = new JComboBox<>(new String[]{"fechaIngreso", "cantidadIngresada", "costoUnitarioCompra", "numeroGuiaRemision", "id"});
        cbxOrdenDir = new JComboBox<>(new String[]{"DESC", "ASC"});
        JButton btnConsultar = new JButton("Consultar JPQL");
        JButton btnVerTodos = new JButton("Ver Todos");

        panelFiltros.add(new JLabel("Guía:"));
        panelFiltros.add(txtFiltroGuia);
        panelFiltros.add(new JLabel("Prov.:"));
        panelFiltros.add(cbxFiltroProveedor);
        panelFiltros.add(new JLabel("Prod.:"));
        panelFiltros.add(cbxFiltroProducto);
        panelFiltros.add(new JLabel("Suc.:"));
        panelFiltros.add(cbxFiltroSucursal);
        panelFiltros.add(new JLabel("Cant >=:"));
        panelFiltros.add(txtFiltroCantMin);
        panelFiltros.add(new JLabel("Orden:"));
        panelFiltros.add(cbxOrdenCampo);
        panelFiltros.add(cbxOrdenDir);
        panelFiltros.add(btnConsultar);
        panelFiltros.add(btnVerTodos);
        panelCentro.add(panelFiltros, BorderLayout.NORTH);

        String[] columnas = {
                "ID", "Fecha Ingreso", "N° Guía", "Producto", "Proveedor", "Sucursal Receptora",
                "Cantidad", "Costo Unit. ($)", "Total ($)", "Observaciones"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaIngresos = new JTable(modeloTabla);
        tablaIngresos.setRowHeight(24);
        panelCentro.add(new JScrollPane(tablaIngresos), BorderLayout.CENTER);

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
        btnVerTodos.addActionListener(e -> {
            txtFiltroGuia.setText("");
            cbxFiltroProveedor.setSelectedIndex(0);
            cbxFiltroProducto.setSelectedIndex(0);
            cbxFiltroSucursal.setSelectedIndex(0);
            txtFiltroCantMin.setText("");
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
        cbxProveedor.removeAllItems();
        cbxFiltroProveedor.removeAllItems();
        cbxFiltroProveedor.addItem(null);
        for (Proveedor p : proveedorControlador.listarProveedoresActivos()) {
            cbxProveedor.addItem(p);
            cbxFiltroProveedor.addItem(p);
        }

        cbxProducto.removeAllItems();
        cbxFiltroProducto.removeAllItems();
        cbxFiltroProducto.addItem(null);
        for (Producto prod : productoControlador.listarTodosProductos()) {
            cbxProducto.addItem(prod);
            cbxFiltroProducto.addItem(prod);
        }

        cbxSucursal.removeAllItems();
        cbxFiltroSucursal.removeAllItems();
        cbxFiltroSucursal.addItem(null);
        for (Sucursal s : productoControlador.listarSucursales()) {
            cbxSucursal.addItem(s);
            cbxFiltroSucursal.addItem(s);
        }
    }

    private void accionRegistrar() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double costo = Double.parseDouble(txtCostoUnitario.getText().trim().replace(",", "."));
            Proveedor prov = (Proveedor) cbxProveedor.getSelectedItem();
            Producto prod = (Producto) cbxProducto.getSelectedItem();
            Sucursal suc = (Sucursal) cbxSucursal.getSelectedItem();

            IngresoInventario ingreso = ingresoControlador.registrarIngreso(
                    txtNumeroGuia.getText(),
                    cantidad,
                    costo,
                    txtObservaciones.getText(),
                    prod != null ? prod.getId() : null,
                    prov != null ? prov.getId() : null,
                    suc != null ? suc.getId() : null
            );

            JOptionPane.showMessageDialog(this,
                    "Ingreso registrado correctamente.\nStock actualizado de '" +
                            ingreso.getProducto().getNombre() + "': " +
                            ingreso.getProducto().getStockActual() + " unidades.",
                    "Ingreso Registrado", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            paginaActual = 0;
            recargarCombosYTabla();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                    "Verifique que la cantidad y el costo unitario sean valores numéricos válidos.",
                    "Datos Inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error al Registrar Ingreso", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void recargarTabla() {
        try {
            Proveedor prov = (Proveedor) cbxFiltroProveedor.getSelectedItem();
            Producto prod = (Producto) cbxFiltroProducto.getSelectedItem();
            Sucursal suc = (Sucursal) cbxFiltroSucursal.getSelectedItem();
            Integer cantMin = txtFiltroCantMin.getText().trim().isEmpty()
                    ? null
                    : Integer.parseInt(txtFiltroCantMin.getText().trim());
            int tamanio = (Integer) cbxTamanioPagina.getSelectedItem();

            PaginaResultado<IngresoInventario> pagina = ingresoControlador.consultarIngresosPaginado(
                    prov != null ? prov.getId() : null,
                    prod != null ? prod.getId() : null,
                    suc != null ? suc.getId() : null,
                    cantMin,
                    txtFiltroGuia.getText(),
                    null,
                    null,
                    (String) cbxOrdenCampo.getSelectedItem(),
                    (String) cbxOrdenDir.getSelectedItem(),
                    paginaActual,
                    tamanio
            );

            modeloTabla.setRowCount(0);
            for (IngresoInventario i : pagina.getContenido()) {
                double total = i.getCantidadIngresada() * i.getCostoUnitarioCompra();
                modeloTabla.addRow(new Object[]{
                        i.getId(),
                        i.getFechaIngreso() != null ? i.getFechaIngreso().format(FORMATO_FECHA) : "-",
                        i.getNumeroGuiaRemision(),
                        i.getProducto() != null ? i.getProducto().getNombre() : "-",
                        i.getProveedor() != null ? i.getProveedor().getRazonSocial() : "-",
                        i.getSucursal() != null ? i.getSucursal().getNombre() : "Sin sucursal",
                        i.getCantidadIngresada(),
                        String.format("%.2f", i.getCostoUnitarioCompra()),
                        String.format("%.2f", total),
                        i.getObservaciones() != null ? i.getObservaciones() : ""
                });
            }
            VistaUtil.actualizarEstadoPaginacion(pagina, lblEstadoPagina, btnAnterior, btnSiguiente);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                    "El filtro de cantidad mínima debe ser un número entero.",
                    "Filtro Inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtNumeroGuia.setText("");
        txtCantidad.setText("");
        txtCostoUnitario.setText("");
        txtObservaciones.setText("");
    }
}
