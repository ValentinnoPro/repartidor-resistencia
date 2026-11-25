package tokens;

import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Árbol de prefijos (Trie) de caracteres. Cada nodo guarda sus hijos en una
 * Lista propia, así que buscar un hijo cuesta O(σ) (σ = hijos del nodo) y
 * insertar o buscar una palabra de largo m cuesta O(m · σ).
 *
 * En el juego guarda los nombres de las calles: sirve para autocompletar
 * mientras el jugador escribe y para que el Tokenizador reconozca nombres
 * de varias palabras ("9 de julio") como un solo token.
 */
public class Trie {

    private final NodoTrie raiz = new NodoTrie('\0');

    public void insertar(String palabra) {
        if (palabra == null || palabra.isEmpty()) {
            throw new IllegalArgumentException("la palabra no puede ser vacía");
        }
        NodoTrie actual = raiz;
        for (int i = 0; i < palabra.length(); i++) {
            actual = actual.hijoOCrear(palabra.charAt(i));
        }
        actual.marcarFinDePalabra();
    }

    public boolean buscarExacto(String palabra) {
        NodoTrie nodo = nodoDe(palabra);
        return nodo != null && nodo.esFinDePalabra();
    }

    /** Todas las palabras que empiezan con el prefijo (para autocompletar). */
    public Lista<String> buscarPorPrefijo(String prefijo) {
        Lista<String> resultado = new ListaEnlazada<>();
        NodoTrie nodo = nodoDe(prefijo);
        if (nodo != null) {
            recolectar(nodo, new StringBuilder(prefijo), resultado);
        }
        return resultado;
    }

    private void recolectar(NodoTrie nodo, StringBuilder acumulado, Lista<String> salida) {
        if (nodo == null) {
            return;
        }
        if (nodo.esFinDePalabra()) {
            salida.agregar(acumulado.toString());
        }
        for (int i = 0; i < nodo.cantidadHijos(); i++) {
            NodoTrie hijo = nodo.hijoEn(i);
            acumulado.append(hijo.getCaracter());
            recolectar(hijo, acumulado, salida);
            acumulado.setLength(acumulado.length() - 1);
        }
    }

    /**
     * Largo de la palabra más larga del Trie que empieza en {@code desde} dentro
     * del texto y termina en un límite de palabra (fin del texto o un carácter
     * que no es letra ni dígito). Devuelve 0 si no hay coincidencia.
     *
     * El límite de palabra evita que "9" matchee dentro de "99" y hace que
     * "9 de julio" se reconozca entero.
     */
    public int coincidenciaMasLarga(String texto, int desde) {
        int mejor = 0;
        NodoTrie actual = raiz;
        for (int i = desde; i < texto.length(); i++) {
            actual = actual.hijo(texto.charAt(i));
            if (actual == null) {
                break;
            }
            boolean finDeTexto = i + 1 == texto.length();
            boolean limite = finDeTexto || !Character.isLetterOrDigit(texto.charAt(i + 1));
            if (actual.esFinDePalabra() && limite) {
                mejor = i + 1 - desde;
            }
        }
        return mejor;
    }

    private NodoTrie nodoDe(String palabra) {
        NodoTrie actual = raiz;
        for (int i = 0; i < palabra.length() && actual != null; i++) {
            actual = actual.hijo(palabra.charAt(i));
        }
        return actual;
    }
}
