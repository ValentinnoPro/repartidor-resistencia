package juego.reglas;

/**
 * Una esquina del mapa: el cruce de dos calles con su posición en pantalla.
 * La identidad es el id: dos esquinas con el mismo id son la misma.
 */
public class Esquina {

    // Atributos (todos privados: se validan al construir)
    private final int id;
    private final String calle1;
    private final String calle2;
    private final int x;
    private final int y;

    // Constructor con validación
    public Esquina(int id, String calle1, String calle2, int x, int y) {
        if (id <= 0) {
            throw new IllegalArgumentException("el id debe ser positivo: " + id);
        }
        if (calle1 == null || calle1.trim().isEmpty() || calle2 == null || calle2.trim().isEmpty()) {
            throw new IllegalArgumentException("las dos calles necesitan nombre");
        }
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("las coordenadas no pueden ser negativas");
        }
        this.id = id;
        this.calle1 = calle1.trim().toLowerCase();
        this.calle2 = calle2.trim().toLowerCase();
        this.x = x;
        this.y = y;
    }

    // Consultas
    public int getId() {
        return id;
    }

    public String getCalle1() {
        return calle1;
    }

    public String getCalle2() {
        return calle2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /** True si esta esquina es el cruce de esas dos calles, en cualquier orden. */
    public boolean cruza(String a, String b) {
        return (calle1.equals(a) && calle2.equals(b)) || (calle1.equals(b) && calle2.equals(a));
    }

    /** Nombre para mostrar en pantalla: "Sarmiento y Belgrano". */
    public String getNombre() {
        return mayuscula(calle1) + " y " + mayuscula(calle2);
    }

    private static String mayuscula(String texto) {
        StringBuilder sb = new StringBuilder();
        boolean inicioPalabra = true;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            // "de" va en minúscula: "9 de Julio"
            boolean esDe = texto.startsWith("de ", i);
            sb.append(inicioPalabra && !esDe ? Character.toUpperCase(c) : c);
            inicioPalabra = c == ' ';
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return id == ((Esquina) otro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
