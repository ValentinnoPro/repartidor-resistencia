package juego.reglas;

/**
 * Una fila del ranking. El orden natural pone primero al MAYOR puntaje y
 * desempata por entregas, nombre y fecha, así el ABB del ranking recorrido
 * en in-orden ya sale del mejor al peor.
 */
public class EntradaRanking implements Comparable<EntradaRanking> {

    public static final int LARGO_MAXIMO_NOMBRE = 12;

    // Atributos
    private final String nombre;
    private final int puntaje;
    private final int entregas;
    private final String fecha;

    // Constructor con validación
    public EntradaRanking(String nombre, int puntaje, int entregas, String fecha) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("el nombre no puede ser vacío");
        }
        if (nombre.contains(";")) {
            throw new IllegalArgumentException("el nombre no puede tener ';'");
        }
        if (entregas < 0) {
            throw new IllegalArgumentException("las entregas no pueden ser negativas");
        }
        if (fecha == null || fecha.length() != 10 || fecha.charAt(4) != '-' || fecha.charAt(7) != '-') {
            throw new IllegalArgumentException("la fecha debe ser AAAA-MM-DD: " + fecha);
        }
        String limpio = nombre.trim();
        this.nombre = limpio.length() > LARGO_MAXIMO_NOMBRE ? limpio.substring(0, LARGO_MAXIMO_NOMBRE) : limpio;
        this.puntaje = puntaje;
        this.entregas = entregas;
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public int getEntregas() {
        return entregas;
    }

    public String getFecha() {
        return fecha;
    }

    @Override
    public int compareTo(EntradaRanking otra) {
        int porPuntaje = Integer.compare(otra.puntaje, puntaje); // mayor primero
        if (porPuntaje != 0) {
            return porPuntaje;
        }
        int porEntregas = Integer.compare(otra.entregas, entregas);
        if (porEntregas != 0) {
            return porEntregas;
        }
        int porNombre = nombre.compareTo(otra.nombre);
        return porNombre != 0 ? porNombre : fecha.compareTo(otra.fecha);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return compareTo((EntradaRanking) otro) == 0;
    }

    @Override
    public int hashCode() {
        return nombre.hashCode() * 31 + puntaje;
    }
}
