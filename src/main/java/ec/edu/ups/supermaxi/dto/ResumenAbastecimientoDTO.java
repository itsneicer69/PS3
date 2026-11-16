package ec.edu.ups.supermaxi.dto;

/**
 * DTO para el reporte agregado de abastecimiento por proveedor mediante proyección JPQL (SELECT new).
 */
public class ResumenAbastecimientoDTO {

    private final Long proveedorId;
    private final String ruc;
    private final String razonSocial;
    private final String categoriaSuministro;
    private final long totalEntregas;
    private final long totalUnidades;
    private final double inversionTotal;

    public ResumenAbastecimientoDTO(Long proveedorId, String ruc, String razonSocial, String categoriaSuministro,
                                    Long totalEntregas, Long totalUnidades, Double inversionTotal) {
        this.proveedorId = proveedorId;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.categoriaSuministro = categoriaSuministro;
        this.totalEntregas = totalEntregas != null ? totalEntregas : 0L;
        this.totalUnidades = totalUnidades != null ? totalUnidades : 0L;
        this.inversionTotal = inversionTotal != null ? inversionTotal : 0.0;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public String getRuc() {
        return ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getCategoriaSuministro() {
        return categoriaSuministro;
    }

    public long getTotalEntregas() {
        return totalEntregas;
    }

    public long getTotalUnidades() {
        return totalUnidades;
    }

    public double getInversionTotal() {
        return inversionTotal;
    }

    @Override
    public String toString() {
        return "ResumenAbastecimientoDTO{" +
                "ruc='" + ruc + '\'' +
                ", razonSocial='" + razonSocial + '\'' +
                ", totalEntregas=" + totalEntregas +
                ", totalUnidades=" + totalUnidades +
                ", inversionTotal=" + inversionTotal +
                '}';
    }
}
