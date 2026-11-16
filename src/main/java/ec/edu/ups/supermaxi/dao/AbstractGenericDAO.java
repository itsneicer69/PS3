package ec.edu.ups.supermaxi.dao;

import ec.edu.ups.supermaxi.dto.PaginaResultado;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Implementación base reutilizable para los DAOs JPA con soporte de transacciones y paginación.
 */
public abstract class AbstractGenericDAO<T, ID> implements IGenericDAO<T, ID> {

    protected final EntityManager em;
    private final Class<T> entityClass;

    protected AbstractGenericDAO(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    @Override
    public void crear(T entidad) {
        ejecutarEnTransaccion(() -> {
            em.persist(entidad);
            return null;
        }, "registrar " + entityClass.getSimpleName());
    }

    @Override
    public T buscarPorId(ID id) {
        return em.find(entityClass, id);
    }

    @Override
    public T actualizar(T entidad) {
        return ejecutarEnTransaccion(() -> em.merge(entidad), "actualizar " + entityClass.getSimpleName());
    }

    @Override
    public void eliminar(ID id) {
        ejecutarEnTransaccion(() -> {
            T entidad = em.find(entityClass, id);
            if (entidad != null) {
                em.remove(entidad);
            }
            return null;
        }, "eliminar " + entityClass.getSimpleName());
    }

    protected <R> R ejecutarEnTransaccion(Supplier<R> accion, String descripcionOperacion) {
        try {
            em.getTransaction().begin();
            R resultado = accion.get();
            em.getTransaction().commit();
            return resultado;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // El rollback revierte la BD pero no la memoria: las entidades que fallaron (o quedaron
            // modificadas) siguen en el contexto de persistencia del EntityManager compartido y se
            // reintentarían en la próxima transacción. Se limpia para volver a leer desde la BD.
            em.clear();
            throw new RuntimeException("Error al " + descripcionOperacion + " en BD: " + e.getMessage(), e);
        }
    }

    protected <R> PaginaResultado<R> ejecutarConsultaPaginada(TypedQuery<R> dataQuery,
                                                             TypedQuery<Long> countQuery,
                                                             int pagina,
                                                             int tamanioPagina) {
        int paginaSegura = Math.max(0, pagina);
        int tamanioSeguro = Math.max(1, tamanioPagina);

        long totalElementos = countQuery.getSingleResult();
        dataQuery.setFirstResult(paginaSegura * tamanioSeguro);
        dataQuery.setMaxResults(tamanioSeguro);

        List<R> contenido = dataQuery.getResultList();
        return new PaginaResultado<>(contenido, paginaSegura, tamanioSeguro, totalElementos);
    }

    protected static void aplicarParametros(TypedQuery<?> query, Map<String, Object> parametros) {
        for (Map.Entry<String, Object> entry : parametros.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }

    protected static String normalizarDireccion(String direccion) {
        return "DESC".equalsIgnoreCase(direccion) ? "DESC" : "ASC";
    }

    protected static boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
