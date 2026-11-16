package ec.edu.ups.supermaxi.dao;

import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductoDAO extends AbstractGenericDAO<Producto, Long> {

    private static final Map<String, String> CAMPOS_ORDEN = Map.of(
            "id", "prod.id",
            "nombre", "prod.nombre",
            "codigoBarras", "prod.codigoBarras",
            "precioUnitario", "prod.precioUnitario",
            "stockActual", "prod.stockActual",
            "categoria", "cat.nombre"
    );

    public ProductoDAO(EntityManager em) {
        super(em, Producto.class);
    }

    @Override
    public List<Producto> listarTodos() {
        String jpql = "SELECT prod FROM Producto prod " +
                "JOIN FETCH prod.proveedor " +
                "LEFT JOIN FETCH prod.categoria " +
                "ORDER BY prod.nombre ASC";
        TypedQuery<Producto> query = em.createQuery(jpql, Producto.class);
        List<Producto> productos = query.getResultList();
        cargarSucursalesDePagina(productos);
        return productos;
    }

    @Override
    public void eliminar(Long id) {
        ejecutarEnTransaccion(() -> {
            em.createQuery("DELETE FROM IngresoInventario i WHERE i.producto.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            em.flush();
            em.clear();

            Producto producto = em.find(Producto.class, id);
            if (producto != null) {
                producto.getSucursales().clear();
                em.remove(producto);
            }
            return null;
        }, "eliminar el producto");
    }

    // =========================================================================
    // CONSULTAS ESPECÍFICAS CON JPQL PARA EL CATÁLOGO DE SUPERMAXI
    // =========================================================================

    public Producto buscarPorCodigoBarras(String codigoBarras) {
        String jpql = "SELECT prod FROM Producto prod " +
                "JOIN FETCH prod.proveedor " +
                "LEFT JOIN FETCH prod.categoria " +
                "LEFT JOIN FETCH prod.sucursales " +
                "WHERE prod.codigoBarras = :codigo";
        TypedQuery<Producto> query = em.createQuery(jpql, Producto.class);
        query.setParameter("codigo", codigoBarras);
        List<Producto> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public List<Producto> listarPorProveedor(Long proveedorId) {
        String jpql = "SELECT prod FROM Producto prod JOIN FETCH prod.proveedor p " +
                "LEFT JOIN FETCH prod.categoria " +
                "WHERE p.id = :provId ORDER BY prod.nombre ASC";
        TypedQuery<Producto> query = em.createQuery(jpql, Producto.class);
        query.setParameter("provId", proveedorId);
        return query.getResultList();
    }

    /**
     * Consulta JPQL 2 (Paginada): Consulta el catálogo de productos con filtros por texto,
     * categoría (1:N), proveedor (1:N), sucursal habilitada (N:M mediante EXISTS), stock máximo
     * y precio máximo, incluyendo ordenamiento dinámico y paginación en base de datos.
     */
    public PaginaResultado<Producto> consultarProductosPaginado(String textoBusqueda,
                                                                 Long categoriaId,
                                                                 Long proveedorId,
                                                                 Long sucursalId,
                                                                 Integer stockMaximo,
                                                                 Double precioMaximo,
                                                                 String campoOrden,
                                                                 String direccionOrden,
                                                                 int pagina,
                                                                 int tamanioPagina) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        Map<String, Object> parametros = new HashMap<>();

        if (tieneTexto(textoBusqueda)) {
            where.append(" AND (LOWER(prod.nombre) LIKE LOWER(:texto) OR prod.codigoBarras LIKE :texto)");
            parametros.put("texto", "%" + textoBusqueda.trim() + "%");
        }
        if (categoriaId != null) {
            where.append(" AND cat.id = :categoriaId");
            parametros.put("categoriaId", categoriaId);
        }
        if (proveedorId != null) {
            where.append(" AND prov.id = :proveedorId");
            parametros.put("proveedorId", proveedorId);
        }
        if (sucursalId != null) {
            where.append(" AND EXISTS (SELECT suc.id FROM prod.sucursales suc WHERE suc.id = :sucursalId)");
            parametros.put("sucursalId", sucursalId);
        }
        if (stockMaximo != null) {
            where.append(" AND prod.stockActual <= :stockMax");
            parametros.put("stockMax", stockMaximo);
        }
        if (precioMaximo != null) {
            where.append(" AND prod.precioUnitario <= :precioMax");
            parametros.put("precioMax", precioMaximo);
        }

        String campoJpql = CAMPOS_ORDEN.getOrDefault(campoOrden, "prod.nombre");
        String dir = normalizarDireccion(direccionOrden);

        String dataJpql = "SELECT prod FROM Producto prod " +
                "JOIN FETCH prod.proveedor prov " +
                "LEFT JOIN FETCH prod.categoria cat" +
                where + " ORDER BY " + campoJpql + " " + dir + ", prod.id ASC";
        String countJpql = "SELECT COUNT(prod) FROM Producto prod " +
                "JOIN prod.proveedor prov " +
                "LEFT JOIN prod.categoria cat" +
                where;

        TypedQuery<Producto> dataQuery = em.createQuery(dataJpql, Producto.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        aplicarParametros(dataQuery, parametros);
        aplicarParametros(countQuery, parametros);

        PaginaResultado<Producto> resultado = ejecutarConsultaPaginada(dataQuery, countQuery, pagina, tamanioPagina);
        cargarSucursalesDePagina(resultado.getContenido());
        return resultado;
    }

    /**
     * Precarga en una sola consulta las sucursales (N:M) de los productos de la página actual,
     * evitando paginación en memoria (HHH90003004) y consultas N+1.
     */
    private void cargarSucursalesDePagina(List<Producto> productos) {
        if (productos == null || productos.isEmpty()) {
            return;
        }
        em.createQuery("SELECT DISTINCT prod FROM Producto prod LEFT JOIN FETCH prod.sucursales WHERE prod IN :productos",
                        Producto.class)
                .setParameter("productos", productos)
                .getResultList();
    }
}
