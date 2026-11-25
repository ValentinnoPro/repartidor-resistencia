package juego.reglas;

/**
 * Una mejora del vehículo (un nodo del árbol de mejoras). El factor de
 * velocidad multiplica el tiempo de cada tramo: 0.9 significa 10 % menos tiempo.
 * La identidad es el nombre.
 */
public class Mejora {

    public static final double FACTOR_MINIMO = 0.5;
    public static final double FACTOR_MAXIMO = 1.0;

    // Atributos
    private final String nombre;
    private final int costo;
    private final double factorVelocidad;
    private boolean comprada;

    // Constructor con validación
    public Mejora(String nombre, int costo, double factorVelocidad, boolean comprada) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("la mejora necesita nombre");
        }
        if (costo < 0) {
            throw new IllegalArgumentException("el costo no puede ser negativo: " + costo);
        }
        if (!(factorVelocidad >= FACTOR_MINIMO && factorVelocidad <= FACTOR_MAXIMO)) {
            throw new IllegalArgumentException("el factor debe estar entre 0.5 y 1.0: " + factorVelocidad);
        }
        this.nombre = nombre.trim();
        this.costo = costo;
        this.factorVelocidad = factorVelocidad;
        this.comprada = comprada;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCosto() {
        return costo;
    }

    public double getFactorVelocidad() {
        return factorVelocidad;
    }

    public boolean isComprada() {
        return comprada;
    }

    /** Marca la mejora como comprada; comprarla dos veces es un error. */
    public void comprar() {
        if (comprada) {
            throw new IllegalStateException("la mejora ya está comprada: " + nombre);
        }
        comprada = true;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return nombre.equals(((Mejora) otro).nombre);
    }

    @Override
    public int hashCode() {
        return nombre.hashCode();
    }
}
