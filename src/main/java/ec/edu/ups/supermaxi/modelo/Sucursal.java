package ec.edu.ups.supermaxi.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Representa un local o centro de distribución de Supermaxi.
 * Mantiene relación N:M con Producto y 1:N con IngresoInventario.
 */
@Entity
@Table(name = "sucursales")
public class Sucursal implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ciudad", nullable = false, length = 60)
    private String ciudad;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "es_centro_distribucion", nullable = false)
    private boolean esCentroDistribucion;

    // Relación N:M con Producto (Una sucursal comercializa múltiples productos)
    @ManyToMany(mappedBy = "sucursales", fetch = FetchType.LAZY)
    private Set<Producto> productos = new LinkedHashSet<>();

    // Relación 1:N con IngresoInventario (Una sucursal/bodega recibe múltiples ingresos)
    @OneToMany(mappedBy = "sucursal", fetch = FetchType.LAZY)
    private List<IngresoInventario> ingresosInventario = new ArrayList<>();

    public Sucursal() {
    }

    public Sucursal(String codigo, String nombre, String ciudad, String direccion, boolean esCentroDistribucion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.direccion = direccion;
        this.esCentroDistribucion = esCentroDistribucion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isEsCentroDistribucion() {
        return esCentroDistribucion;
    }

    public void setEsCentroDistribucion(boolean esCentroDistribucion) {
        this.esCentroDistribucion = esCentroDistribucion;
    }

    public Set<Producto> getProductos() {
        return productos;
    }

    public void setProductos(Set<Producto> productos) {
        this.productos = productos;
    }

    public List<IngresoInventario> getIngresosInventario() {
        return ingresosInventario;
    }

    public void setIngresosInventario(List<IngresoInventario> ingresosInventario) {
        this.ingresosInventario = ingresosInventario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sucursal sucursal = (Sucursal) o;
        return Objects.equals(codigo, sucursal.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return nombre + " - " + ciudad;
    }
}
