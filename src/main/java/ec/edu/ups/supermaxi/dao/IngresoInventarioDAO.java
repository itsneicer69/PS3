package ec.edu.ups.supermaxi.dao;

import ec.edu.ups.supermaxi.dto.PaginaResultado;
import ec.edu.ups.supermaxi.dto.ResumenAbastecimientoDTO;
import ec.edu.ups.supermaxi.modelo.IngresoInventario;
import ec.edu.ups.supermaxi.modelo.Producto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngresoInventarioDAO extends AbstractGenericDAO<IngresoInventario, Long> {

    private static final Map<String, String> CAMPOS_ORDEN_INGRESO = Map.of(
            "id", "i.id",
            "fechaIngreso", "i.fechaIngreso",
            "cantidadIngresada", "i.cantidadIngresada",
            "costoUnitarioCompra", "i.costoUnitarioCompra",
            "numeroGuiaRemision", "i.numeroGuiaRemision"
    );

    private static final Map<String, String> CAMPOS_ORDEN_RESUMEN = Map.of(
            "inversionTotal", "COALESCE(SUM(i.cantidadIngresada * i.costoUnitarioCompra), 0.0)",
            "totalUnidades", "COALESCE(SUM(i.cantidadIngresada), 0L)",
            "totalEntregas", "COUNT(i.id)",
            "razonSocial", "prov.razonSocial"
    );

    public IngresoInventarioDAO(EntityManager em) {
        super(em, IngresoInventario.class);
    }

    // 1. CREATE: Registrar el ingreso de productos al inventario y sumar el stock automáticamente
    @Override
    public void crear(IngresoInventario ingreso) {
        ejecutarEnTransaccion(() -> {
            em.persist(ingreso);

            Producto producto = ingreso.getProducto();
            if (producto != null) {
                if (producto.getId() != null) {
                    Producto managed = em.find(Producto.class, producto.getId());
                    if (managed != null) {
                        managed.setStockActual(managed.getStockActual() + ingreso.getCantidadIngresada());
                        ingreso.setProducto(managed);
                    }
                } else {
                    producto.setStockActual(producto.getStockActual() + ingreso.getCantidadIngresada());
                    em.merge(producto);
                }
            }
            return null;
        }, "registrar el ingreso al inventario");
    }

    @Override
    public List<IngresoInventario> listarTodos() {
        String jpql = "SELECT i FROM IngresoInventario i " +
                "JOIN FETCH i.producto " +
                "JOIN FETCH i.proveedor " +
                "LEFT JOIN FETCH i.sucursal " +
                "ORDER BY i.fechaIngreso DESC, i.id DESC";
        return em.createQuery(jpql, IngresoInventario.class).getResultList();
    }

    // 4. DELETE: Eliminar un registro de ingreso y revertir la cantidad del stock del producto
    @Override
    public void eliminar(Long id) {
        ejecutarEnTransaccion(() -> {
            IngresoInventario ingreso = em.find(IngresoInventario.class, id);
            if (ingreso != null) {
                Producto producto = ingreso.getProducto();
                if (producto != null) {
                    int nuevoStock = Math.max(0, producto.getStockActual() - ingreso.getCantidadIngresada());
                    producto.setStockActual(nuevoStock);
                    em.merge(producto);
                }
                em.remove(ingreso);
            }
            return null;
        }, "eliminar el ingreso de inventario");
    }

    // =========================================================================
    // CONSULTAS ESPECÍFICAS CON JPQL PARA BODEGA / INVENTARIO DE SUPERMAXI
    // =========================================================================

    public List<IngresoInventario> listarPorProveedor(Long proveedorId) {
        String jpql = "SELECT i FROM IngresoInventario i JOIN FETCH i.producto prod JOIN FETCH i.proveedor prov " +
                "LEFT JOIN FETCH i.sucursal WHERE prov.id = :provId ORDER BY i.fechaIngreso DESC, i.id DESC";
        return em.createQuery(jpql, IngresoInventario.class)
                .setParameter("provId", proveedorId)
                .getResultList();
    }

    public List<IngresoInventario> listarPorProducto(Long productoId) {
        String jpql = "SELECT i FROM IngresoInventario i JOIN FETCH i.producto prod " +
                "JOIN FETCH i.proveedor LEFT JOIN FETCH i.sucursal " +
                "WHERE prod.id = :prodId ORDER BY i.fechaIngreso DESC, i.id DESC";
        return em.createQuery(jpql, IngresoInventario.class)
                .setParameter("prodId", productoId)
                .getResultList();
    }

    public List<IngresoInventario> buscarPorNumeroGuia(String numeroGuia) {
        String jpql = "SELECT i FROM IngresoInventario i JOIN FETCH i.producto JOIN FETCH i.proveedor " +
                "LEFT JOIN FETCH i.sucursal WHERE i.numeroGuiaRemision = :guia";
        return em.createQuery(jpql, IngresoInventario.class)
                .setParameter("guia", numeroGuia)
                .getResultList();
    }

    /**
     * Consulta JPQL 3 (Paginada): Consulta el historial de ingresos al inventario aplicando filtros por
     * proveedor, producto, sucursal receptora, cantidad mínima, guía de remisión y rango de fechas,
     * con ordenamiento dinámico y paginación.
     */
    public PaginaResultado<IngresoInventario> consultarIngresosPaginado(Long proveedorId,
                                                                        Long productoId,
                                                                        Long sucursalId,
                                                                        Integer cantidadMinima,
                                                                        String numeroGuia,
                                                                        LocalDateTime fechaDesde,
                                                                        LocalDateTime fechaHasta,
                                                                        String campoOrden,
                                                                        String direccionOrden,
                                                                        int pagina,
                                                                        int tamanioPagina) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        Map<String, Object> parametros = new HashMap<>();

        if (proveedorId != null) {
            where.append(" AND prov.id = :proveedorId");
            parametros.put("proveedorId", proveedorId);
        }
        if (productoId != null) {
            where.append(" AND prod.id = :productoId");
            parametros.put("productoId", productoId);
        }
        if (sucursalId != null) {
            where.append(" AND suc.id = :sucursalId");
            parametros.put("sucursalId", sucursalId);
        }
        if (cantidadMinima != null) {
            where.append(" AND i.cantidadIngresada >= :cantidadMin");
            parametros.put("cantidadMin", cantidadMinima);
        }
        if (tieneTexto(numeroGuia)) {
            where.append(" AND LOWER(i.numeroGuiaRemision) LIKE LOWER(:guia)");
            parametros.put("guia", "%" + numeroGuia.trim() + "%");
        }
        if (fechaDesde != null) {
            where.append(" AND i.fechaIngreso >= :fechaDesde");
            parametros.put("fechaDesde", fechaDesde);
        }
        if (fechaHasta != null) {
            where.append(" AND i.fechaIngreso <= :fechaHasta");
            parametros.put("fechaHasta", fechaHasta);
        }

        String campoJpql = CAMPOS_ORDEN_INGRESO.getOrDefault(campoOrden, "i.fechaIngreso");
        String dir = normalizarDireccion(direccionOrden);

        String dataJpql = "SELECT i FROM IngresoInventario i " +
                "JOIN FETCH i.producto prod " +
                "JOIN FETCH i.proveedor prov " +
                "LEFT JOIN FETCH i.sucursal suc" +
                where + " ORDER BY " + campoJpql + " " + dir + ", i.id DESC";
        String countJpql = "SELECT COUNT(i) FROM IngresoInventario i " +
                "JOIN i.producto prod " +
                "JOIN i.proveedor prov " +
                "LEFT JOIN i.sucursal suc" +
                where;

        TypedQuery<IngresoInventario> dataQuery = em.createQuery(dataJpql, IngresoInventario.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        aplicarParametros(dataQuery, parametros);
        aplicarParametros(countQuery, parametros);

        return ejecutarConsultaPaginada(dataQuery, countQuery, pagina, tamanioPagina);
    }

    /**
     * Consulta JPQL 4 (Agregación + Proyección DTO + Paginación): Reporte gerencial de abastecimiento
     * agrupado por proveedor con filtros WHERE y HAVING, ordenamiento dinámico y paginación.
     */
    public PaginaResultado<ResumenAbastecimientoDTO> consultarResumenAbastecimiento(Boolean soloProveedoresActivos,
                                                                                    Long sucursalId,
                                                                                    Double inversionMinima,
                                                                                    String campoOrden,
                                                                                    String direccionOrden,
                                                                                    int pagina,
                                                                                    int tamanioPagina) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        Map<String, Object> parametros = new HashMap<>();

        if (soloProveedoresActivos != null) {
            where.append(" AND prov.activo = :activo");
            parametros.put("activo", soloProveedoresActivos);
        }
        if (sucursalId != null) {
            where.append(" AND i.sucursal.id = :sucursalId");
            parametros.put("sucursalId", sucursalId);
        }

        StringBuilder having = new StringBuilder();
        if (inversionMinima != null) {
            having.append(" HAVING COALESCE(SUM(i.cantidadIngresada * i.costoUnitarioCompra), 0.0) >= :inversionMin");
            parametros.put("inversionMin", inversionMinima);
        }

        String campoJpql = CAMPOS_ORDEN_RESUMEN.getOrDefault(
                campoOrden, "COALESCE(SUM(i.cantidadIngresada * i.costoUnitarioCompra), 0.0)");
        String dir = normalizarDireccion(direccionOrden);

        String dataJpql = "SELECT new ec.edu.ups.supermaxi.dto.ResumenAbastecimientoDTO(" +
                "prov.id, prov.ruc, prov.razonSocial, prov.categoriaSuministro, " +
                "COUNT(i.id), COALESCE(SUM(i.cantidadIngresada), 0L), " +
                "COALESCE(SUM(i.cantidadIngresada * i.costoUnitarioCompra), 0.0)) " +
                "FROM IngresoInventario i JOIN i.proveedor prov" +
                where +
                " GROUP BY prov.id, prov.ruc, prov.razonSocial, prov.categoriaSuministro" +
                having +
                " ORDER BY " + campoJpql + " " + dir + ", prov.id ASC";

        // Conteo exacto de grupos que cumplen WHERE y HAVING
        TypedQuery<ResumenAbastecimientoDTO> dataQuery = em.createQuery(dataJpql, ResumenAbastecimientoDTO.class);
        aplicarParametros(dataQuery, parametros);

        String countJpql;
        if (inversionMinima != null) {
            StringBuilder countSubJpql = new StringBuilder(
                    "SELECT prov.id FROM IngresoInventario i JOIN i.proveedor prov" +
                            where + " GROUP BY prov.id" + having);
            TypedQuery<Long> groupIdsQuery = em.createQuery(countSubJpql.toString(), Long.class);
            aplicarParametros(groupIdsQuery, parametros);
            long totalGrupos = groupIdsQuery.getResultList().size();

            int paginaSegura = Math.max(0, pagina);
            int tamanioSeguro = Math.max(1, tamanioPagina);
            dataQuery.setFirstResult(paginaSegura * tamanioSeguro);
            dataQuery.setMaxResults(tamanioSeguro);
            return new PaginaResultado<>(dataQuery.getResultList(), paginaSegura, tamanioSeguro, totalGrupos);
        } else {
            countJpql = "SELECT COUNT(DISTINCT prov.id) FROM IngresoInventario i JOIN i.proveedor prov" + where;
            TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
            aplicarParametros(countQuery, parametros);
            return ejecutarConsultaPaginada(dataQuery, countQuery, pagina, tamanioPagina);
        }
    }
}
