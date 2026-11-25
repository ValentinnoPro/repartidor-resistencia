package grafos;

import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Resultado de buscar un camino: la ruta (origen primero), su costo y si
 * existe. Cuando no hay camino se devuelve un resultado "sin camino" explícito
 * (no se usa null ni una excepción silenciosa).
 */
public class ResultadoCamino<T> {

    private final Lista<T> ruta;
    private final double costo;
    private final boolean existe;

    private ResultadoCamino(Lista<T> ruta, double costo, boolean existe) {
        this.ruta = ruta;
        this.costo = costo;
        this.existe = existe;
    }

    public static <T> ResultadoCamino<T> conCamino(Lista<T> ruta, double costo) {
        return new ResultadoCamino<>(ruta, costo, true);
    }

    public static <T> ResultadoCamino<T> sinCamino() {
        return new ResultadoCamino<>(new ListaEnlazada<>(), Double.POSITIVE_INFINITY, false);
    }

    public Lista<T> getRuta() {
        return ruta;
    }

    public double getCosto() {
        return costo;
    }

    public boolean existe() {
        return existe;
    }
}
