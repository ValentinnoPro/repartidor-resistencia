package juego.reglas;

/** Un tramo recorrido (de una esquina a la vecina). Se apila para poder deshacerlo. */
public class Tramo {

    private final Esquina origen;
    private final Esquina destino;
    private final double minutos;

    public Tramo(Esquina origen, Esquina destino, double minutos) {
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("el tramo necesita origen y destino");
        }
        if (minutos < 0) {
            throw new IllegalArgumentException("la duración no puede ser negativa");
        }
        this.origen = origen;
        this.destino = destino;
        this.minutos = minutos;
    }

    public Esquina getOrigen() {
        return origen;
    }

    public Esquina getDestino() {
        return destino;
    }

    public double getMinutos() {
        return minutos;
    }
}
