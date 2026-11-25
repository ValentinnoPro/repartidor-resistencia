package grafos;

import estructuras.Cola;
import estructuras.ColaEnlazada;
import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Componentes conexas del grafo tomado como NO dirigido (dos esquinas están
 * conectadas si hay calle entre ellas en cualquier sentido). Costo O(V + E)
 * por la recorrida más el chequeo de aristas entrantes (O(V) por vértice).
 */
public final class Conectividad {

    private Conectividad() {
    }

    public static <T> Lista<Lista<T>> componentesConexas(Grafo<T> grafo) {
        Lista<Lista<T>> componentes = new ListaEnlazada<>();
        Lista<T> yaAsignados = new ListaEnlazada<>();
        for (int i = 0; i < grafo.cantidadVertices(); i++) {
            T vertice = grafo.vertice(i);
            if (yaAsignados.indiceDe(vertice) < 0) {
                Lista<T> componente = componenteDe(grafo, vertice);
                for (int j = 0; j < componente.tamanio(); j++) {
                    yaAsignados.agregar(componente.obtener(j));
                }
                componentes.agregar(componente);
            }
        }
        return componentes;
    }

    /** Esquinas alcanzables desde el vértice ignorando el sentido de las calles. */
    public static <T> Lista<T> componenteDe(Grafo<T> grafo, T origen) {
        Lista<T> visitados = new ListaEnlazada<>();
        Cola<T> pendientes = new ColaEnlazada<>();
        visitados.agregar(origen);
        pendientes.encolar(origen);
        while (!pendientes.estaVacia()) {
            T actual = pendientes.desencolar();
            for (int i = 0; i < grafo.cantidadVertices(); i++) {
                T otro = grafo.vertice(i);
                boolean conectados = grafo.existeArista(actual, otro) || grafo.existeArista(otro, actual);
                if (conectados && visitados.indiceDe(otro) < 0) {
                    visitados.agregar(otro);
                    pendientes.encolar(otro);
                }
            }
        }
        return visitados;
    }
}
