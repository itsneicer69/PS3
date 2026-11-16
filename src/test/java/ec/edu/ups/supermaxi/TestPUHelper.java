package ec.edu.ups.supermaxi;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utilidad para seleccionar la Unidad de Persistencia de pruebas.
 * Por defecto usa SupermaxiTestPU (H2 en memoria modo PostgreSQL);
 * puede ejecutarse contra PostgreSQL pasando -Dsupermaxi.pu=SupermaxiPU.
 */
public final class TestPUHelper {

    private TestPUHelper() {
    }

    public static EntityManagerFactory crearEntityManagerFactory() {
        String pu = System.getProperty("supermaxi.pu", "SupermaxiTestPU");
        return Persistence.createEntityManagerFactory(pu);
    }
}
