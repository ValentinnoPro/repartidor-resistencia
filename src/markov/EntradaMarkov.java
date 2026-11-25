package markov;

import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Una fila de la tabla de transiciones: un estado (los últimos k caracteres) y
 * la Lista de caracteres que lo siguieron en el corpus. Los sucesores se
 * guardan CON REPETICIÓN: así la frecuencia sale sola al sortear una posición
 * al azar (si "a" siguió 3 veces y "e" 1 vez, "a" sale con 75 %).
 *
 * Se ordena solo por el estado (la clave del ABB).
 */
public class EntradaMarkov implements Comparable<EntradaMarkov> {

    private final String estado;
    private final Lista<Character> sucesores = new ListaEnlazada<>();

    public EntradaMarkov(String estado) {
        if (estado == null || estado.isEmpty()) {
            throw new IllegalArgumentException("el estado no puede ser vacío");
        }
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public Lista<Character> getSucesores() {
        return sucesores;
    }

    public void agregarSucesor(char c) {
        sucesores.agregar(c);
    }

    @Override
    public int compareTo(EntradaMarkov otra) {
        return estado.compareTo(otra.estado);
    }

    @Override
    public boolean equals(Object otro) {
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return estado.equals(((EntradaMarkov) otro).estado);
    }

    @Override
    public int hashCode() {
        return estado.hashCode();
    }
}
