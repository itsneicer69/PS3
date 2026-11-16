package ec.edu.ups.supermaxi.dao;

import ec.edu.ups.supermaxi.modelo.Categoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class CategoriaDAO extends AbstractGenericDAO<Categoria, Long> {

    public CategoriaDAO(EntityManager em) {
        super(em, Categoria.class);
    }

    @Override
    public List<Categoria> listarTodos() {
        String jpql = "SELECT c FROM Categoria c ORDER BY c.nombre ASC";
        TypedQuery<Categoria> query = em.createQuery(jpql, Categoria.class);
        return query.getResultList();
    }

    public Categoria buscarPorCodigo(String codigo) {
        String jpql = "SELECT c FROM Categoria c WHERE c.codigo = :codigo";
        List<Categoria> resultados = em.createQuery(jpql, Categoria.class)
                .setParameter("codigo", codigo)
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public Categoria buscarPorNombre(String nombre) {
        String jpql = "SELECT c FROM Categoria c WHERE LOWER(c.nombre) = LOWER(:nombre)";
        List<Categoria> resultados = em.createQuery(jpql, Categoria.class)
                .setParameter("nombre", nombre)
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }
}
