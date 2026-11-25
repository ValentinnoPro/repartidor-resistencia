package grafos;

/** Arista dirigida hacia {@code destino} con un peso (minutos de juego) no negativo. */
public class Arista<T> {

    private final T destino;
    private double peso;

    public Arista(T destino, double peso) {
        if (destino == null) {
            throw new IllegalArgumentException("el destino no puede ser nulo");
        }
        this.destino = destino;
        setPeso(peso);
    }

    public T getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        if (Double.isNaN(peso) || peso < 0) {
            throw new IllegalArgumentException("el peso no puede ser negativo: " + peso);
        }
        this.peso = peso;
    }
}
