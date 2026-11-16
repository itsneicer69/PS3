package ec.edu.ups.supermaxi.dto;

import java.util.Collections;
import java.util.List;

/**
 * Contenedor genérico para resultados paginados de consultas JPQL.
 *
 * @param <T> Tipo de entidad o DTO devuelto por la consulta
 */
public class PaginaResultado<T> {

    private final List<T> contenido;
    private final int paginaActual;
    private final int tamanioPagina;
    private final long totalElementos;
    private final int totalPaginas;

    public PaginaResultado(List<T> contenido, int paginaActual, int tamanioPagina, long totalElementos) {
        this.contenido = contenido != null ? contenido : Collections.emptyList();
        this.paginaActual = Math.max(0, paginaActual);
        this.tamanioPagina = Math.max(1, tamanioPagina);
        this.totalElementos = Math.max(0L, totalElementos);
        this.totalPaginas = (int) Math.ceil((double) this.totalElementos / this.tamanioPagina);
    }

    public List<T> getContenido() {
        return contenido;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public int getTamanioPagina() {
        return tamanioPagina;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public boolean tieneAnterior() {
        return paginaActual > 0;
    }

    public boolean tieneSiguiente() {
        return paginaActual + 1 < totalPaginas;
    }
}
