package ec.edu.ups.supermaxi.dao;

import ec.edu.ups.supermaxi.modelo.Sucursal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class SucursalDAO extends AbstractGenericDAO<Sucursal, Long> {

    public SucursalDAO(EntityManager em) {
        super(em, Sucursal.class);
    }

    @Override
    public List<Sucursal> listarTodos() {
        String jpql = "SELECT s FROM Sucursal s ORDER BY s.ciudad ASC, s.nombre ASC";
        TypedQuery<Sucursal> query = em.createQuery(jpql, Sucursal.class);
        return query.getResultList();
    }

    public Sucursal buscarPorCodigo(String codigo) {
        String jpql = "SELECT s FROM Sucursal s WHERE s.codigo = :codigo";
        List<Sucursal> resultados = em.createQuery(jpql, Sucursal.class)
                .setParameter("codigo", codigo)
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public List<Sucursal> listarPorCiudad(String ciudad) {
        String jpql = "SELECT s FROM Sucursal s WHERE LOWER(s.ciudad) LIKE LOWER(:ciudad) ORDER BY s.nombre ASC";
        return em.createQuery(jpql, Sucursal.class)
                .setParameter("ciudad", "%" + ciudad + "%")
                .getResultList();
    }
}
