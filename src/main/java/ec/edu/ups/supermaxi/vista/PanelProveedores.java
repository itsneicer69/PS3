package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.controlador.ProveedorControlador;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.ContratoComercial;
import ec.edu.ups.supermaxi.modelo.Proveedor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel CRUD completo de Proveedores + Contrato Comercial (1:1) con consulta JPQL paginada.
 */
public class PanelProveedores extends JPanel {

    private final ProveedorControlador controlador;

    private JTextField txtId;
    private JTextField txtRuc;
    private JTextField txtRazonSocial;
    private JComboBox<String> cbxCategoria;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;
    private JCheckBox chkActivo;

    // Campos 1:1 ContratoComercial
    private JTextField txtNumeroContrato;
    private JTextField txtDiasCredito;
    private JTextField txtDescuento;
    private JTextField txtCondiciones;

    // Filtros, ordenamiento y paginación JPQL
    private JTextField txtFiltroTexto;
    private JComboBox<String> cbxFiltroEstado;
    private JTextField txtFiltroCategoria;
    private JTextField txtFiltroMinCredito;
    private JComboBox<String> cbxOrdenCampo;
    private JComboBox<String> cbxOrdenDir;
    private JComboBox<Integer> cbxTamanioPagina;

    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JLabel lblEstadoPagina;

    private JTable tablaProveedores;
    private DefaultTableModel modeloTabla;

    private int paginaActual = 0;

    public PanelProveedores(ProveedorControlador controlador) {
        this.controlador = controlador;
        inicializarComponentes();
        recargarTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Formulario Proveedor + Contrato Comercial (1:1)
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(new TitledBorder("Datos del Proveedor y Contrato Comercial (Relación 1:1)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField(8);
        txtId.setEditable(false);
        txtId.setBackground(new Color(230, 230, 230));
        txtRuc = new JTextField(15);
        txtRazonSocial = new JTextField(20);
        cbxCategoria = new JComboBox<>(new String[]{
                "Lácteos y Derivados",
                "Cárnicos, Aves y Embutidos",
                "Abarrotes y Despensa",
                "Frutas, Verduras y Legumbres",
                "Bebidas y Licores",
                "Limpieza y Hogar",
                "Cuidado Personal",
                "Panadería y Pastelería"
        });
        txtTelefono = new JTextField(15);
        txtCorreo = new JTextField(20);
        txtDireccion = new JTextField(22);
        chkActivo = new JCheckBox("Proveedor Activo", true);

        txtNumeroContrato = new JTextField(15);
        txtDiasCredito = new JTextField("30", 8);
        txtDescuento = new JTextField("1.5", 8);
        txtCondiciones = new JTextField(22);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 0, "ID:", txtId);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 0, "RUC (13 dígitos):", txtRuc);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 1, "Razón Social:", txtRazonSocial);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 1, "Categoría Suministro:", cbxCategoria);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 2, "Teléfono:", txtTelefono);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 2, "Correo Electrónico:", txtCorreo);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 3, "Dirección Bodega:", txtDireccion);
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0.08;
        panelFormulario.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.42;
        panelFormulario.add(chkActivo, gbc);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 4, "N° Contrato (1:1):", txtNumeroContrato);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 4, "Días Crédito:", txtDiasCredito);

        VistaUtil.agregarCampo(panelFormulario, gbc, 0, 5, "Desc. Pronto Pago (%):", txtDescuento);
        VistaUtil.agregarCampo(panelFormulario, gbc, 2, 5, "Condiciones Entrega:", txtCondiciones);

        // 2. Botones CRUD
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        JButton btnRegistrar = new JButton("Registrar (Create)");
        JButton btnActualizar = new JButton("Actualizar (Update)");
        JButton btnBajaLogica = new JButton("Desactivar (Baja Lógica)");
        JButton btnEliminar = new JButton("Eliminar BD (Delete)");
        JButton btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnBajaLogica);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        JPanel panelNorte = new JPanel(new BorderLayout(4, 4));
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // 3. Filtros JPQL + Tabla + Paginación
        JPanel panelCentro = new JPanel(new BorderLayout(6, 6));
        panelCentro.setBorder(new TitledBorder("Consulta JPQL de Proveedores (Filtros, Ordenamiento y Paginación)"));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        txtFiltroTexto = new JTextField(12);
        cbxFiltroEstado = new JComboBox<>(new String[]{"Solo Activos", "Todos", "Solo Inactivos"});
        txtFiltroCategoria = new JTextField(10);
        txtFiltroMinCredito = new JTextField(4);
        cbxOrdenCampo = new JComboBox<>(new String[]{"razonSocial", "ruc", "categoria", "diasCredito", "id"});
        cbxOrdenDir = new JComboBox<>(new String[]{"ASC", "DESC"});
        JButton btnBuscar = new JButton("Consultar JPQL");
        JButton btnResetFiltros = new JButton("Ver Todos");

        panelFiltros.add(new JLabel("RUC/Nombre:"));
        panelFiltros.add(txtFiltroTexto);
        panelFiltros.add(new JLabel("Estado:"));
        panelFiltros.add(cbxFiltroEstado);
        panelFiltros.add(new JLabel("Categoría:"));
        panelFiltros.add(txtFiltroCategoria);
        panelFiltros.add(new JLabel("Crédito >=:"));
        panelFiltros.add(txtFiltroMinCredito);
        panelFiltros.add(new JLabel("Orden:"));
        panelFiltros.add(cbxOrdenCampo);
        panelFiltros.add(cbxOrdenDir);
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnResetFiltros);
        panelCentro.add(panelFiltros, BorderLayout.NORTH);

        String[] columnas = {
                "ID", "RUC", "Razón Social", "Categoría", "Teléfono",
                "Correo", "Dirección", "Activo", "N° Contrato", "Crédito (días)", "Desc. (%)"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProveedores = new JTable(modeloTabla);
        tablaProveedores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProveedores.setRowHeight(24);
        panelCentro.add(new JScrollPane(tablaProveedores), BorderLayout.CENTER);

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
        tablaProveedores.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cargarProveedorSeleccionado();
            }
        });

        btnRegistrar.addActionListener(e -> accionRegistrar());
        btnActualizar.addActionListener(e -> accionActualizar());
        btnBajaLogica.addActionListener(e -> accionDesactivar());
        btnEliminar.addActionListener(e -> accionEliminarFisico());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        btnBuscar.addActionListener(e -> {
            paginaActual = 0;
            recargarTabla();
        });
        btnResetFiltros.addActionListener(e -> {
            txtFiltroTexto.setText("");
            cbxFiltroEstado.setSelectedIndex(0);
            txtFiltroCategoria.setText("");
            txtFiltroMinCredito.setText("");
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

    private void accionRegistrar() {
        try {
            int diasCredito = parsearEnteroOpcional(txtDiasCredito.getText(), 30, "días de crédito");
            double descuento = parsearDoubleOpcional(txtDescuento.getText(), 0.0, "descuento");

            controlador.registrarProveedor(
                    txtRuc.getText(),
                    txtRazonSocial.getText(),
                    cbxCategoria.getSelectedItem().toString(),
                    txtTelefono.getText(),
                    txtCorreo.getText(),
                    txtDireccion.getText(),
                    txtNumeroContrato.getText(),
                    diasCredito,
                    descuento,
                    txtCondiciones.getText()
            );
            JOptionPane.showMessageDialog(this,
                    "Proveedor registrado correctamente en el catálogo de Supermaxi.",
                    "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            paginaActual = 0;
            recargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error al Registrar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionActualizar() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione primero un proveedor de la tabla para poder actualizarlo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Long id = Long.parseLong(txtId.getText());
            int diasCredito = parsearEnteroOpcional(txtDiasCredito.getText(), 30, "días de crédito");
            double descuento = parsearDoubleOpcional(txtDescuento.getText(), 0.0, "descuento");

            controlador.actualizarProveedor(
                    id,
                    txtRuc.getText(),
                    txtRazonSocial.getText(),
                    cbxCategoria.getSelectedItem().toString(),
                    txtTelefono.getText(),
                    txtCorreo.getText(),
                    txtDireccion.getText(),
                    chkActivo.isSelected(),
                    txtNumeroContrato.getText(),
                    diasCredito,
                    descuento,
                    txtCondiciones.getText()
            );
            JOptionPane.showMessageDialog(this,
                    "Proveedor actualizado exitosamente.",
                    "Actualización Exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            recargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error al Actualizar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionDesactivar() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un proveedor de la tabla para desactivarlo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea dar de baja lógica a este proveedor?",
                "Confirmar Desactivación", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                controlador.desactivarProveedor(Long.parseLong(txtId.getText()));
                JOptionPane.showMessageDialog(this, "El proveedor ha sido desactivado.",
                        "Baja Lógica", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                recargarTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void accionEliminarFisico() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un proveedor de la tabla para eliminarlo de la base de datos.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar físicamente este proveedor de la base de datos?\n" +
                        "(Se eliminarán también su contrato, productos e ingresos asociados en cascada)",
                "Confirmar Eliminación Física", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                controlador.eliminarProveedorFisico(Long.parseLong(txtId.getText()));
                JOptionPane.showMessageDialog(this,
                        "Proveedor eliminado definitivamente de la base de datos.",
                        "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                paginaActual = 0;
                recargarTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Error al Eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void recargarTabla() {
        try {
            Boolean estado = switch (cbxFiltroEstado.getSelectedIndex()) {
                case 0 -> Boolean.TRUE;
                case 2 -> Boolean.FALSE;
                default -> null;
            };
            Integer minCredito = null;
            if (!txtFiltroMinCredito.getText().trim().isEmpty()) {
                minCredito = Integer.parseInt(txtFiltroMinCredito.getText().trim());
            }
            int tamanio = (Integer) cbxTamanioPagina.getSelectedItem();

            PaginaResultado<Proveedor> pagina = controlador.consultarProveedoresPaginado(
                    estado,
                    txtFiltroTexto.getText(),
                    txtFiltroCategoria.getText(),
                    minCredito,
                    (String) cbxOrdenCampo.getSelectedItem(),
                    (String) cbxOrdenDir.getSelectedItem(),
                    paginaActual,
                    tamanio
            );

            modeloTabla.setRowCount(0);
            for (Proveedor p : pagina.getContenido()) {
                ContratoComercial c = p.getContratoComercial();
                modeloTabla.addRow(new Object[]{
                        p.getId(),
                        p.getRuc(),
                        p.getRazonSocial(),
                        p.getCategoriaSuministro(),
                        p.getTelefono(),
                        p.getCorreo(),
                        p.getDireccion(),
                        p.isActivo() ? "Sí" : "No",
                        c != null ? c.getNumeroContrato() : "Sin contrato",
                        c != null ? c.getDiasCredito() : "-",
                        c != null ? String.format("%.1f%%", c.getDescuentoProntoPago()) : "-"
                });
            }
            VistaUtil.actualizarEstadoPaginacion(pagina, lblEstadoPagina, btnAnterior, btnSiguiente);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "El filtro de días de crédito debe ser numérico.",
                    "Filtro inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarProveedorSeleccionado() {
        int fila = tablaProveedores.getSelectedRow();
        if (fila >= 0) {
            Long id = Long.parseLong(modeloTabla.getValueAt(fila, 0).toString());
            Proveedor p = controlador.obtenerPorId(id);
            if (p == null) {
                return;
            }
            txtId.setText(String.valueOf(p.getId()));
            txtRuc.setText(p.getRuc());
            txtRazonSocial.setText(p.getRazonSocial());
            cbxCategoria.setSelectedItem(p.getCategoriaSuministro());
            txtTelefono.setText(p.getTelefono() != null ? p.getTelefono() : "");
            txtCorreo.setText(p.getCorreo());
            txtDireccion.setText(p.getDireccion() != null ? p.getDireccion() : "");
            chkActivo.setSelected(p.isActivo());

            ContratoComercial c = p.getContratoComercial();
            if (c != null) {
                txtNumeroContrato.setText(c.getNumeroContrato());
                txtDiasCredito.setText(String.valueOf(c.getDiasCredito()));
                txtDescuento.setText(String.valueOf(c.getDescuentoProntoPago()));
                txtCondiciones.setText(c.getCondicionesEntrega() != null ? c.getCondicionesEntrega() : "");
            } else {
                txtNumeroContrato.setText("");
                txtDiasCredito.setText("30");
                txtDescuento.setText("0.0");
                txtCondiciones.setText("");
            }
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtRuc.setText("");
        txtRazonSocial.setText("");
        cbxCategoria.setSelectedIndex(0);
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        chkActivo.setSelected(true);
        txtNumeroContrato.setText("");
        txtDiasCredito.setText("30");
        txtDescuento.setText("1.5");
        txtCondiciones.setText("");
        tablaProveedores.clearSelection();
    }

    private static int parsearEnteroOpcional(String texto, int defecto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            return defecto;
        }
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El valor de " + campo + " debe ser un número entero.");
        }
    }

    private static double parsearDoubleOpcional(String texto, double defecto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            return defecto;
        }
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El valor de " + campo + " debe ser numérico.");
        }
    }
}
