package ec.edu.ups.supermaxi.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa el acuerdo o contrato comercial vigente entre Supermaxi y un Proveedor (Relación 1:1).
 */
@Entity
@Table(name = "contratos_comerciales")
public class ContratoComercial implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_contrato", nullable = false, unique = true, length = 40)
    private String numeroContrato;

    @Column(name = "dias_credito", nullable = false)
    private int diasCredito;

    @Column(name = "descuento_pronto_pago", nullable = false)
    private double descuentoProntoPago;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "condiciones_entrega", length = 200)
    private String condicionesEntrega;

    // Relación 1:1 con Proveedor (lado propietario con FK única)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false, unique = true)
    private Proveedor proveedor;

    public ContratoComercial() {
    }

    public ContratoComercial(String numeroContrato, int diasCredito, double descuentoProntoPago,
                             LocalDate fechaInicio, LocalDate fechaFin, String condicionesEntrega) {
        this.numeroContrato = numeroContrato;
        this.diasCredito = diasCredito;
        this.descuentoProntoPago = descuentoProntoPago;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.condicionesEntrega = condicionesEntrega;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        this.diasCredito = diasCredito;
    }

    public double getDescuentoProntoPago() {
        return descuentoProntoPago;
    }

    public void setDescuentoProntoPago(double descuentoProntoPago) {
        this.descuentoProntoPago = descuentoProntoPago;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getCondicionesEntrega() {
        return condicionesEntrega;
    }

    public void setCondicionesEntrega(String condicionesEntrega) {
        this.condicionesEntrega = condicionesEntrega;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContratoComercial that = (ContratoComercial) o;
        return Objects.equals(numeroContrato, that.numeroContrato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroContrato);
    }

    @Override
    public String toString() {
        return "ContratoComercial{" +
                "id=" + id +
                ", numeroContrato='" + numeroContrato + '\'' +
                ", diasCredito=" + diasCredito +
                ", descuentoProntoPago=" + descuentoProntoPago +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                '}';
    }
}
