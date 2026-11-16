package ec.edu.ups.supermaxi.dao;

import ec.edu.ups.supermaxi.modelo.ContratoComercial;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ContratoComercialDAO extends AbstractGenericDAO<ContratoComercial, Long> {

    public ContratoComercialDAO(EntityManager em) {
        super(em, ContratoComercial.class);
    }

    @Override
    public List<ContratoComercial> listarTodos() {
        String jpql = "SELECT c FROM ContratoComercial c JOIN FETCH c.proveedor ORDER BY c.numeroContrato ASC";
        return em.createQuery(jpql, ContratoComercial.class).getResultList();
    }

    public ContratoComercial buscarPorNumeroContrato(String numeroContrato) {
        String jpql = "SELECT c FROM ContratoComercial c WHERE c.numeroContrato = :numero";
        List<ContratoComercial> resultados = em.createQuery(jpql, ContratoComercial.class)
                .setParameter("numero", numeroContrato)
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    public ContratoComercial buscarPorProveedorId(Long proveedorId) {
        String jpql = "SELECT c FROM ContratoComercial c WHERE c.proveedor.id = :provId";
        List<ContratoComercial> resultados = em.createQuery(jpql, ContratoComercial.class)
                .setParameter("provId", proveedorId)
                .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }
}
