package ec.edu.ups.supermaxi.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa una categoría o sección del catálogo de Supermaxi (Relación 1:N con Producto).
 */
@Entity
@Table(name = "categorias")
public class Categoria implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(name = "pasillo", length = 50)
    private String pasillo;

    @Column(name = "requiere_cadena_frio", nullable = false)
    private boolean requiereCadenaFrio;

    // Relación 1:N con Producto (Una categoría agrupa múltiples productos)
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Producto> productos = new ArrayList<>();

    public Categoria() {
    }

    public Categoria(String codigo, String nombre, String pasillo, boolean requiereCadenaFrio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.pasillo = pasillo;
        this.requiereCadenaFrio = requiereCadenaFrio;
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
        producto.setCategoria(this);
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

    public String getPasillo() {
        return pasillo;
    }

    public void setPasillo(String pasillo) {
        this.pasillo = pasillo;
    }

    public boolean isRequiereCadenaFrio() {
        return requiereCadenaFrio;
    }

    public void setRequiereCadenaFrio(boolean requiereCadenaFrio) {
        this.requiereCadenaFrio = requiereCadenaFrio;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return Objects.equals(codigo, categoria.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return nombre + (pasillo != null && !pasillo.isBlank() ? " (" + pasillo + ")" : "");
    }
}
