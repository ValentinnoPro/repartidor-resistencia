package grafos;

import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Grafo dirigido y ponderado con LISTA DE ADYACENCIA sobre la Lista propia.
 *
 * Por qué lista y no matriz: un mapa de calles es un grafo disperso (cada
 * esquina tiene 2 a 4 vecinos, E << V²). La matriz gastaría V² celdas casi
 * vacías; la lista guarda solo las E aristas reales.
 *
 * Una calle de doble mano se guarda como DOS aristas (ida y vuelta); una de
 * una mano, como una sola.
 */
public class Grafo<T> {

    // Cada vértice con sus aristas salientes
    private static class Entrada<T> {
        private final T vertice;
        private final Lista<Arista<T>> aristas = new ListaEnlazada<>();

        private Entrada(T vertice) {
            this.vertice = vertice;
        }
    }

    private final Lista<Entrada<T>> entradas = new ListaEnlazada<>();

    // Vértices
    public void agregarVertice(T vertice) {
        if (vertice == null) {
            throw new IllegalArgumentException("el vértice no puede ser nulo");
        }
        if (indiceDe(vertice) < 0) {
            entradas.agregar(new Entrada<>(vertice));
        }
    }

    public boolean existeVertice(T vertice) {
        return indiceDe(vertice) >= 0;
    }

    public int cantidadVertices() {
        return entradas.tamanio();
    }

    public int indiceDe(T vertice) {
        for (int i = 0; i < entradas.tamanio(); i++) {
            if (entradas.obtener(i).vertice.equals(vertice)) {
                return i;
            }
        }
        return -1;
    }

    public T vertice(int indice) {
        return entradas.obtener(indice).vertice;
    }

    public Lista<T> vertices() {
        Lista<T> copia = new ListaEnlazada<>();
        for (int i = 0; i < entradas.tamanio(); i++) {
            copia.agregar(entradas.obtener(i).vertice);
        }
        return copia;
    }

    // Aristas
    public void agregarArista(T origen, T destino, double peso) {
        Entrada<T> entrada = entradaDe(origen);
        if (!existeVertice(destino)) {
            throw new IllegalArgumentException("el destino no es un vértice del grafo");
        }
        Arista<T> existente = aristaEntre(entrada, destino);
        if (existente != null) {
            existente.setPeso(peso);
        } else {
            entrada.aristas.agregar(new Arista<>(destino, peso));
        }
    }

    public boolean eliminarArista(T origen, T destino) {
        Entrada<T> entrada = entradaDe(origen);
        for (int i = 0; i < entrada.aristas.tamanio(); i++) {
            if (entrada.aristas.obtener(i).getDestino().equals(destino)) {
                entrada.aristas.eliminar(i);
                return true;
            }
        }
        return false;
    }

    public void modificarPeso(T origen, T destino, double peso) {
        Arista<T> arista = aristaEntre(entradaDe(origen), destino);
        if (arista == null) {
            throw new IllegalArgumentException("la arista no existe");
        }
        arista.setPeso(peso);
    }

    public boolean existeArista(T origen, T destino) {
        int i = indiceDe(origen);
        return i >= 0 && aristaEntre(entradas.obtener(i), destino) != null;
    }

    public double getPeso(T origen, T destino) {
        Arista<T> arista = aristaEntre(entradaDe(origen), destino);
        if (arista == null) {
            throw new IllegalArgumentException("la arista no existe");
        }
        return arista.getPeso();
    }

    /** Aristas que salen del vértice (copia: modificarla no altera el grafo). */
    public Lista<Arista<T>> vecinos(T vertice) {
        return copiaDe(entradaDe(vertice).aristas);
    }

    // Acceso por índice, solo para los algoritmos del paquete
    Lista<Arista<T>> aristasInternas(int indice) {
        return entradas.obtener(indice).aristas;
    }

    /** Copia independiente (los vértices se comparten, las aristas se duplican). */
    public Grafo<T> copiar() {
        Grafo<T> copia = new Grafo<>();
        for (int i = 0; i < entradas.tamanio(); i++) {
            copia.agregarVertice(entradas.obtener(i).vertice);
        }
        for (int i = 0; i < entradas.tamanio(); i++) {
            Entrada<T> e = entradas.obtener(i);
            for (int j = 0; j < e.aristas.tamanio(); j++) {
                Arista<T> a = e.aristas.obtener(j);
                copia.agregarArista(e.vertice, a.getDestino(), a.getPeso());
            }
        }
        return copia;
    }

    // Auxiliares
    private Entrada<T> entradaDe(T vertice) {
        int i = indiceDe(vertice);
        if (i < 0) {
            throw new IllegalArgumentException("el vértice no existe en el grafo");
        }
        return entradas.obtener(i);
    }

    private Arista<T> aristaEntre(Entrada<T> entrada, T destino) {
        for (int i = 0; i < entrada.aristas.tamanio(); i++) {
            Arista<T> a = entrada.aristas.obtener(i);
            if (a.getDestino().equals(destino)) {
                return a;
            }
        }
        return null;
    }

    private Lista<Arista<T>> copiaDe(Lista<Arista<T>> original) {
        Lista<Arista<T>> copia = new ListaEnlazada<>();
        for (int i = 0; i < original.tamanio(); i++) {
            copia.agregar(original.obtener(i));
        }
        return copia;
    }
}
