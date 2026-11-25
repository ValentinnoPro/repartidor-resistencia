package juego.reglas;

/**
 * Vehículo del cadete. Es abstracto: cada subclase decide cuánto tarda en
 * recorrer una calle y cuánto le afecta la lluvia (polimorfismo: la Partida
 * llama a {@link #tiempoTramo} sin saber qué vehículo es).
 *
 * El factor de mejoras viene del árbol de mejoras del taller.
 */
public abstract class Vehiculo {

    public static final double FACTOR_MEJORAS_MINIMO = 0.3;

    // Atributos
    private final String nombre;
    private double factorMejoras = 1.0;

    protected Vehiculo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("el vehículo necesita nombre");
        }
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public double getFactorMejoras() {
        return factorMejoras;
    }

    public void setFactorMejoras(double factor) {
        if (!(factor >= FACTOR_MEJORAS_MINIMO && factor <= 1.0)) {
            throw new IllegalArgumentException("el factor de mejoras debe estar entre 0.3 y 1.0: " + factor);
        }
        this.factorMejoras = factor;
    }

    /** Minutos de juego que tarda en recorrer una calle de peso base {@code pesoBase}. */
    public abstract double tiempoTramo(double pesoBase, boolean lloviendo);

    /** Frase corta para el menú. */
    public abstract String descripcion();
}
