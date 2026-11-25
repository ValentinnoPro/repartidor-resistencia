package grafos;

import estructuras.Cola;
import estructuras.ColaEnlazada;
import estructuras.Lista;
import estructuras.ListaEnlazada;
import estructuras.Par;

/**
 * Recorridos del grafo. Todos cuestan O(V + E) salvo por la búsqueda lineal en
 * la Lista de visitados (el mapa tiene ~30 vértices, así que no importa).
 */
public final class Recorridos {

    private Recorridos() {
    }

    /** Recorrido en anchura desde el origen, con la Cola propia. */
    public static <T> Lista<T> bfs(Grafo<T> grafo, T origen) {
        verificar(grafo, origen);
        Lista<T> visitados = new ListaEnlazada<>();
        Cola<T> pendientes = new ColaEnlazada<>();
        visitados.agregar(origen);
        pendientes.encolar(origen);
        while (!pendientes.estaVacia()) {
            T actual = pendientes.desencolar();
            Lista<Arista<T>> vecinos = grafo.vecinos(actual);
            for (int i = 0; i < vecinos.tamanio(); i++) {
                T vecino = vecinos.obtener(i).getDestino();
                if (visitados.indiceDe(vecino) < 0) {
                    visitados.agregar(vecino);
                    pendientes.encolar(vecino);
                }
            }
        }
        return visitados;
    }

    /**
     * Esquinas a {@code maxSaltos} cuadras o menos del origen (el origen incluido).
     * En el juego es el radio de cobertura donde pueden aparecer pedidos.
     */
    public static <T> Lista<T> bfsHastaDistancia(Grafo<T> grafo, T origen, int maxSaltos) {
        verificar(grafo, origen);
        Lista<T> visitados = new ListaEnlazada<>();
        Cola<Par<T, Integer>> pendientes = new ColaEnlazada<>();
        visitados.agregar(origen);
        pendientes.encolar(new Par<>(origen, 0));
        while (!pendientes.estaVacia()) {
            Par<T, Integer> actual = pendientes.desencolar();
            if (actual.getSegundo() >= maxSaltos) {
                continue;
            }
            Lista<Arista<T>> vecinos = grafo.vecinos(actual.getPrimero());
            for (int i = 0; i < vecinos.tamanio(); i++) {
                T vecino = vecinos.obtener(i).getDestino();
                if (visitados.indiceDe(vecino) < 0) {
                    visitados.agregar(vecino);
                    pendientes.encolar(new Par<>(vecino, actual.getSegundo() + 1));
                }
            }
        }
        return visitados;
    }

    /** Recorrido en profundidad, recursivo. */
    public static <T> Lista<T> dfs(Grafo<T> grafo, T origen) {
        verificar(grafo, origen);
        Lista<T> visitados = new ListaEnlazada<>();
        dfs(grafo, origen, visitados);
        return visitados;
    }

    private static <T> void dfs(Grafo<T> grafo, T actual, Lista<T> visitados) {
        if (visitados.indiceDe(actual) >= 0) {
            return; // caso base: ya se visitó
        }
        visitados.agregar(actual);
        Lista<Arista<T>> vecinos = grafo.vecinos(actual);
        for (int i = 0; i < vecinos.tamanio(); i++) {
            dfs(grafo, vecinos.obtener(i).getDestino(), visitados);
        }
    }

    /**
     * Ruta con MENOS CUADRAS (saltos) entre dos vértices, ignorando los pesos.
     * El costo del resultado es la cantidad de saltos. Lo usa el "ir hacia"
     * por escrito, que es más simple (y menos eficiente) que el GPS.
     */
    public static <T> ResultadoCamino<T> rutaPorSaltos(Grafo<T> grafo, T origen, T destino) {
        verificar(grafo, origen);
        if (!grafo.existeVertice(destino)) {
            throw new IllegalArgumentException("el destino no existe en el grafo");
        }
        Lista<Par<T, T>> predecesores = new ListaEnlazada<>(); // (vértice, de dónde vino)
        Cola<T> pendientes = new ColaEnlazada<>();
        predecesores.agregar(new Par<>(origen, null));
        pendientes.encolar(origen);
        boolean llego = origen.equals(destino);
        while (!pendientes.estaVacia() && !llego) {
            T actual = pendientes.desencolar();
            Lista<Arista<T>> vecinos = grafo.vecinos(actual);
            for (int i = 0; i < vecinos.tamanio() && !llego; i++) {
                T vecino = vecinos.obtener(i).getDestino();
                if (indicePredecesor(predecesores, vecino) < 0) {
                    predecesores.agregar(new Par<>(vecino, actual));
                    pendientes.encolar(vecino);
                    llego = vecino.equals(destino);
                }
            }
        }
        if (!llego) {
            return ResultadoCamino.sinCamino();
        }
        Lista<T> ruta = new ListaEnlazada<>();
        T paso = destino;
        while (paso != null) {
            ruta.insertar(0, paso);
            paso = predecesores.obtener(indicePredecesor(predecesores, paso)).getSegundo();
        }
        return ResultadoCamino.conCamino(ruta, ruta.tamanio() - 1);
    }

    private static <T> int indicePredecesor(Lista<Par<T, T>> predecesores, T vertice) {
        for (int i = 0; i < predecesores.tamanio(); i++) {
            if (predecesores.obtener(i).getPrimero().equals(vertice)) {
                return i;
            }
        }
        return -1;
    }

    private static <T> void verificar(Grafo<T> grafo, T origen) {
        if (grafo == null || origen == null || !grafo.existeVertice(origen)) {
            throw new IllegalArgumentException("origen inválido");
        }
    }
}
