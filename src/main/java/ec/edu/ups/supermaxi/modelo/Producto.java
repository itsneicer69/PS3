package ec.edu.ups.supermaxi.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "productos")
public class Producto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras", nullable = false, unique = true, length = 30)
    private String codigoBarras;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "precio_unitario", nullable = false)
    private double precioUnitario;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual;

    // Relación N:1 con Proveedor (Muchos productos son suministrados por un proveedor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    // Relación N:1 con Categoria (Muchos productos pertenecen a una categoría de Supermaxi)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Relación 1:N con IngresoInventario (Un producto recibe múltiples ingresos a bodega)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IngresoInventario> ingresosInventario = new ArrayList<>();

    // Relación N:M con Sucursal (Un producto se comercializa en varias sucursales y cada sucursal ofrece muchos productos)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "producto_sucursal",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "sucursal_id")
    )
    private Set<Sucursal> sucursales = new LinkedHashSet<>();

    public Producto() {
    }

    public Producto(String codigoBarras, String nombre, double precioUnitario, int stockActual, Proveedor proveedor) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.stockActual = stockActual;
        this.proveedor = proveedor;
    }

    public Producto(String codigoBarras, String nombre, double precioUnitario, int stockActual,
                    Proveedor proveedor, Categoria categoria) {
        this(codigoBarras, nombre, precioUnitario, stockActual, proveedor);
        this.categoria = categoria;
    }

    // --- Métodos utilitarios para sincronizar relaciones bidireccionales ---
    public void agregarSucursal(Sucursal sucursal) {
        if (sucursal != null) {
            sucursales.add(sucursal);
            sucursal.getProductos().add(this);
        }
    }

    public void removerSucursal(Sucursal sucursal) {
        if (sucursal != null) {
            sucursales.remove(sucursal);
            sucursal.getProductos().remove(this);
        }
    }

    public void agregarIngresoInventario(IngresoInventario ingreso) {
        if (ingreso != null) {
            ingresosInventario.add(ingreso);
            ingreso.setProducto(this);
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<IngresoInventario> getIngresosInventario() { return ingresosInventario; }
    public void setIngresosInventario(List<IngresoInventario> ingresosInventario) { this.ingresosInventario = ingresosInventario; }

    public Set<Sucursal> getSucursales() { return sucursales; }
    public void setSucursales(Set<Sucursal> sucursales) { this.sucursales = sucursales; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(codigoBarras, producto.codigoBarras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoBarras);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", codigoBarras='" + codigoBarras + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precioUnitario=" + precioUnitario +
                ", stockActual=" + stockActual +
                '}';
    }
}
