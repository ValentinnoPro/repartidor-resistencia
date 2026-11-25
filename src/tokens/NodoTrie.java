package tokens;

import estructuras.Lista;
import estructuras.ListaEnlazada;

/** Nodo del Trie: un carácter, si termina una palabra y sus hijos en una Lista propia. */
class NodoTrie {

    private final char caracter;
    private boolean finDePalabra;
    private final Lista<NodoTrie> hijos = new ListaEnlazada<>();

    NodoTrie(char caracter) {
        this.caracter = caracter;
    }

    char getCaracter() {
        return caracter;
    }

    boolean esFinDePalabra() {
        return finDePalabra;
    }

    void marcarFinDePalabra() {
        finDePalabra = true;
    }

    int cantidadHijos() {
        return hijos.tamanio();
    }

    NodoTrie hijoEn(int indice) {
        return hijos.obtener(indice);
    }

    /** Busca el hijo con ese carácter (recorre la Lista), o null si no existe. */
    NodoTrie hijo(char c) {
        for (int i = 0; i < hijos.tamanio(); i++) {
            NodoTrie h = hijos.obtener(i);
            if (h.caracter == c) {
                return h;
            }
        }
        return null;
    }

    /** Devuelve el hijo con ese carácter, creándolo si hace falta. */
    NodoTrie hijoOCrear(char c) {
        NodoTrie existente = hijo(c);
        if (existente != null) {
            return existente;
        }
        NodoTrie nuevo = new NodoTrie(c);
        hijos.agregar(nuevo);
        return nuevo;
    }
}
