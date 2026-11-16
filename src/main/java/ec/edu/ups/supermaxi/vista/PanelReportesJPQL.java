package ec.edu.ups.supermaxi.vista;

import ec.edu.ups.supermaxi.controlador.IngresoInventarioControlador;
import ec.edu.ups.supermaxi.controlador.ProductoControlador;
import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.dto.ResumenAbastecimientoDTO;
import ec.edu.ups.supermaxi.modelo.Sucursal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel para ejecutar y visualizar la Consulta JPQL Agregada (GROUP BY, HAVING, ORDER BY y Paginación)
 * de resumen de abastecimiento por proveedor en Supermaxi.
 */
public class PanelReportesJPQL extends JPanel {

    private final IngresoInventarioControlador ingresoControlador;
    private final ProductoControlador productoControlador;

    private JCheckBox chkSoloActivos;
    private JComboBox<Sucursal> cbxSucursal;
    private JTextField txtInversionMinima;
    private JComboBox<String> cbxOrdenCampo;
    private JComboBox<String> cbxOrdenDir;
    private JComboBox<Integer> cbxTamanioPagina;

    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JLabel lblEstadoPagina;
    private JTextArea txtConsultaJpql;

    private DefaultTableModel modeloTabla;
    private int paginaActual = 0;

    public PanelReportesJPQL(IngresoInventarioControlador ingresoControlador,
                             ProductoControlador productoControlador) {
        this.ingresoControlador = ingresoControlador;
        this.productoControlador = productoControlador;
        inicializarComponentes();
        recargarCombosYReporte();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panelFiltros.setBorder(new TitledBorder("Filtros y Ordenamiento - Consulta JPQL Agregada (SELECT new DTO)"));

        chkSoloActivos = new JCheckBox("Solo Proveedores Activos", true);
        cbxSucursal = new JComboBox<>();
        cbxSucursal.setRenderer(VistaUtil.crearRenderizadorSucursal("Todas las sucursales"));
        txtInversionMinima = new JTextField(7);
        cbxOrdenCampo = new JComboBox<>(new String[]{"inversionTotal", "totalUnidades", "totalEntregas", "razonSocial"});
        cbxOrdenDir = new JComboBox<>(new String[]{"DESC", "ASC"});
        JButton btnEjecutar = new JButton("Ejecutar Consulta JPQL");
        JButton btnRestablecer = new JButton("Restablecer");

        panelFiltros.add(chkSoloActivos);
        panelFiltros.add(new JLabel("Sucursal:"));
        panelFiltros.add(cbxSucursal);
        panelFiltros.add(new JLabel("Inversión >= ($):"));
        panelFiltros.add(txtInversionMinima);
        panelFiltros.add(new JLabel("Ordenar por:"));
        panelFiltros.add(cbxOrdenCampo);
        panelFiltros.add(cbxOrdenDir);
        panelFiltros.add(btnEjecutar);
        panelFiltros.add(btnRestablecer);

        add(panelFiltros, BorderLayout.NORTH);

        String[] columnas = {
                "ID Prov.", "RUC", "Razón Social", "Categoría Suministro",
                "Total Entregas", "Unidades Ingresadas", "Inversión Total ($)"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tablaResumen = new JTable(modeloTabla);
        tablaResumen.setRowHeight(24);

        txtConsultaJpql = new JTextArea(5, 80);
        txtConsultaJpql.setEditable(false);
        txtConsultaJpql.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtConsultaJpql.setBackground(new Color(245, 247, 250));
        JScrollPane scrollJpql = new JScrollPane(txtConsultaJpql);
        scrollJpql.setBorder(new TitledBorder("Referencia de Sentencia JPQL Ejecutada"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tablaResumen), scrollJpql);
        splitPane.setResizeWeight(0.72);
        add(splitPane, BorderLayout.CENTER);

        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        cbxTamanioPagina = new JComboBox<>(new Integer[]{3, 5, 10});
        btnAnterior = new JButton("<< Anterior");
        btnSiguiente = new JButton("Siguiente >>");
        lblEstadoPagina = new JLabel("Página 1 de 1");
        panelPaginacion.add(new JLabel("Tamaño página:"));
        panelPaginacion.add(cbxTamanioPagina);
        panelPaginacion.add(btnAnterior);
        panelPaginacion.add(lblEstadoPagina);
        panelPaginacion.add(btnSiguiente);
        add(panelPaginacion, BorderLayout.SOUTH);

        btnEjecutar.addActionListener(e -> {
            paginaActual = 0;
            recargarReporte();
        });
        btnRestablecer.addActionListener(e -> {
            chkSoloActivos.setSelected(true);
            cbxSucursal.setSelectedIndex(0);
            txtInversionMinima.setText("");
            cbxOrdenCampo.setSelectedIndex(0);
            cbxOrdenDir.setSelectedIndex(0);
            paginaActual = 0;
            recargarReporte();
        });
        cbxTamanioPagina.addActionListener(e -> {
            paginaActual = 0;
            recargarReporte();
        });
        btnAnterior.addActionListener(e -> {
            if (paginaActual > 0) {
                paginaActual--;
                recargarReporte();
            }
        });
        btnSiguiente.addActionListener(e -> {
            paginaActual++;
            recargarReporte();
        });
    }

    public void recargarCombosYReporte() {
        cbxSucursal.removeAllItems();
        cbxSucursal.addItem(null);
        for (Sucursal s : productoControlador.listarSucursales()) {
            cbxSucursal.addItem(s);
        }
        recargarReporte();
    }

    public void recargarReporte() {
        try {
            Boolean soloActivos = chkSoloActivos.isSelected() ? Boolean.TRUE : null;
            Sucursal sucursal = (Sucursal) cbxSucursal.getSelectedItem();
            Double inversionMin = txtInversionMinima.getText().trim().isEmpty()
                    ? null
                    : Double.parseDouble(txtInversionMinima.getText().trim().replace(",", "."));
            int tamanio = (Integer) cbxTamanioPagina.getSelectedItem();
            String campoOrden = (String) cbxOrdenCampo.getSelectedItem();
            String dirOrden = (String) cbxOrdenDir.getSelectedItem();

            PaginaResultado<ResumenAbastecimientoDTO> pagina = ingresoControlador.consultarResumenAbastecimiento(
                    soloActivos,
                    sucursal != null ? sucursal.getId() : null,
                    inversionMin,
                    campoOrden,
                    dirOrden,
                    paginaActual,
                    tamanio
            );

            modeloTabla.setRowCount(0);
            for (ResumenAbastecimientoDTO dto : pagina.getContenido()) {
                modeloTabla.addRow(new Object[]{
                        dto.getProveedorId(),
                        dto.getRuc(),
                        dto.getRazonSocial(),
                        dto.getCategoriaSuministro(),
                        dto.getTotalEntregas(),
                        dto.getTotalUnidades(),
                        String.format("%.2f", dto.getInversionTotal())
                });
            }

            VistaUtil.actualizarEstadoPaginacion(pagina, lblEstadoPagina, btnAnterior, btnSiguiente);
            actualizarTextoJpql(soloActivos, sucursal, inversionMin, campoOrden, dirOrden, pagina);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                    "La inversión mínima debe ser un valor numérico.",
                    "Filtro Inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarTextoJpql(Boolean soloActivos, Sucursal sucursal, Double inversionMin,
                                     String campoOrden, String dirOrden, PaginaResultado<?> pagina) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT new ec.edu.ups.supermaxi.dto.ResumenAbastecimientoDTO(\n");
        sb.append("  prov.id, prov.ruc, prov.razonSocial, prov.categoriaSuministro,\n");
        sb.append("  COUNT(i.id), SUM(i.cantidadIngresada), SUM(i.cantidadIngresada * i.costoUnitarioCompra))\n");
        sb.append("FROM IngresoInventario i JOIN i.proveedor prov WHERE 1=1");
        if (soloActivos != null) sb.append(" AND prov.activo = true");
        if (sucursal != null) sb.append(" AND i.sucursal.id = ").append(sucursal.getId());
        sb.append("\nGROUP BY prov.id, prov.ruc, prov.razonSocial, prov.categoriaSuministro");
        if (inversionMin != null) {
            sb.append("\nHAVING SUM(i.cantidadIngresada * i.costoUnitarioCompra) >= ").append(inversionMin);
        }
        sb.append("\nORDER BY ").append(campoOrden).append(" ").append(dirOrden);
        sb.append("  |  setFirstResult(").append(pagina.getPaginaActual() * pagina.getTamanioPagina())
                .append("), setMaxResults(").append(pagina.getTamanioPagina()).append(")");
        txtConsultaJpql.setText(sb.toString());
    }
}
