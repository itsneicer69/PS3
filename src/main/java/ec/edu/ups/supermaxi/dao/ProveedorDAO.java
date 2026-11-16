package ec.edu.ups.supermaxi.dao;

import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.modelo.Producto;
import ec.edu.ups.supermaxi.modelo.Proveedor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProveedorDAO extends AbstractGenericDAO<Proveedor, Long> {

    private static final Map<String, String> CAMPOS_ORDEN = Map.of(
            "id", "p.id",
            "ruc", "p.ruc",
            "razonSocial", "p.razonSocial",
            "categoria", "p.categoriaSuministro",
            "diasCredito", "COALESCE(c.diasCredito, 0)"
    );

    public ProveedorDAO(EntityManager em) {
        super(em, Proveedor.class);
    }

    @Override
    public List<Proveedor> listarTodos() {
        String jpql = "SELECT p FROM Proveedor p LEFT JOIN FETCH p.contratoComercial ORDER BY p.id ASC";
        TypedQuery<Proveedor> query = em.createQuery(jpql, Proveedor.class);
        return query.getResultList();
    }

    @Override
    public void eliminar(Long id) {
        ejecutarEnTransaccion(() -> {
            // 1. Eliminar primero ingresos vinculados al proveedor o a sus productos para evitar error FK (23503)
            em.createQuery("DELETE FROM IngresoInventario i WHERE i.proveedor.id = :id OR i.producto.proveedor.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            em.flush();
            em.clear();

            // 2. Desvincular relaciones N:M de productos del proveedor y eliminar en cascada (productos + contrato 1:1)
            Proveedor proveedor = em.find(Proveedor.class, id);
            if (proveedor != null) {
                for (Producto producto : proveedor.getProductos()) {
                    producto.getSucursales().clear();
                }
                em.remove(proveedor);
            }
            return null;
        }, "eliminar el proveedor");
    }

    // =========================================================================
    // CONSULTAS ESPECÍFICAS CON JPQL PARA EL CASO DE ESTUDIO SUPERMAXI
    // =========================================================================

    public Proveedor buscarPorRuc(String ruc) {
        String jpql = "SELECT p FROM Proveedor p LEFT JOIN FETCH p.contratoComercial WHERE p.ruc = :ruc";
        TypedQuery<Proveedor> query = em.createQuery(jpql, Proveedor.class);
        query.setParameter("ruc", ruc);
        List<Proveedor> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public List<Proveedor> listarActivos() {
        String jpql = "SELECT p FROM Proveedor p LEFT JOIN FETCH p.contratoComercial " +
                "WHERE p.activo = true ORDER BY p.razonSocial ASC";
        TypedQuery<Proveedor> query = em.createQuery(jpql, Proveedor.class);
        return query.getResultList();
    }

    public List<Proveedor> buscarPorCategoria(String categoria) {
        String jpql = "SELECT p FROM Proveedor p LEFT JOIN FETCH p.contratoComercial " +
                "WHERE LOWER(p.categoriaSuministro) LIKE LOWER(:cat) AND p.activo = true ORDER BY p.razonSocial ASC";
        TypedQuery<Proveedor> query = em.createQuery(jpql, Proveedor.class);
        query.setParameter("cat", "%" + categoria + "%");
        return query.getResultList();
    }

    /**
     * Consulta JPQL 1 (Paginada): Busca proveedores con su contrato comercial (1:1) aplicando filtros
     * dinámicos por estado, texto (RUC o razón social), categoría y días mínimos de crédito,
     * además de ordenamiento y paginación.
     */
    public PaginaResultado<Proveedor> consultarProveedoresPaginado(Boolean activo,
                                                                   String textoBusqueda,
                                                                   String categoria,
                                                                   Integer minDiasCredito,
                                                                   String campoOrden,
                                                                   String direccionOrden,
                                                                   int pagina,
                                                                   int tamanioPagina) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        Map<String, Object> parametros = new HashMap<>();

        if (activo != null) {
            where.append(" AND p.activo = :activo");
            parametros.put("activo", activo);
        }
        if (tieneTexto(textoBusqueda)) {
            where.append(" AND (LOWER(p.razonSocial) LIKE LOWER(:texto) OR p.ruc LIKE :texto)");
            parametros.put("texto", "%" + textoBusqueda.trim() + "%");
        }
        if (tieneTexto(categoria)) {
            where.append(" AND LOWER(p.categoriaSuministro) LIKE LOWER(:categoria)");
            parametros.put("categoria", "%" + categoria.trim() + "%");
        }
        if (minDiasCredito != null) {
            where.append(" AND c.diasCredito >= :minDias");
            parametros.put("minDias", minDiasCredito);
        }

        String campoJpql = CAMPOS_ORDEN.getOrDefault(campoOrden, "p.razonSocial");
        String dir = normalizarDireccion(direccionOrden);

        String dataJpql = "SELECT p FROM Proveedor p LEFT JOIN FETCH p.contratoComercial c" +
                where + " ORDER BY " + campoJpql + " " + dir + ", p.id ASC";
        String countJpql = "SELECT COUNT(p) FROM Proveedor p LEFT JOIN p.contratoComercial c" + where;

        TypedQuery<Proveedor> dataQuery = em.createQuery(dataJpql, Proveedor.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        aplicarParametros(dataQuery, parametros);
        aplicarParametros(countQuery, parametros);

        return ejecutarConsultaPaginada(dataQuery, countQuery, pagina, tamanioPagina);
    }
}
