package grafos;

/**
 * Elemento de la "lista de prioridad" de Dijkstra, que es un ABB. Se ordena
 * por distancia y, si empatan, por el índice del vértice: así nunca hay dos
 * claves iguales en el ABB (ver nota de duplicados en ABB).
 */
class EntradaDistancia implements Comparable<EntradaDistancia> {

    private final int indice;
    private final double distancia;

    EntradaDistancia(int indice, double distancia) {
        this.indice = indice;
        this.distancia = distancia;
    }

    int getIndice() {
        return indice;
    }

    double getDistancia() {
        return distancia;
    }

    @Override
    public int compareTo(EntradaDistancia otra) {
        int porDistancia = Double.compare(distancia, otra.distancia);
        return porDistancia != 0 ? porDistancia : Integer.compare(indice, otra.indice);
    }

    @Override
    public boolean equals(Object otro) {
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return compareTo((EntradaDistancia) otro) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * indice + Double.hashCode(distancia);
    }
}
