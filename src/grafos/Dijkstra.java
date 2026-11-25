package grafos;

import arboles.ABB;
import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Camino de costo mínimo con Dijkstra.
 *
 * La lista de prioridad es un ABB de {@link EntradaDistancia} (ordenado por
 * distancia y luego por índice). Se usa inserción "perezosa": cuando aparece
 * una distancia mejor para un vértice se inserta una entrada NUEVA y, al sacar
 * el mínimo, las entradas viejas (que ya no coinciden con la mejor distancia
 * conocida) se ignoran.
 *
 * Costo: O((V + E) log V) en promedio; O((V + E) V) si el ABB degenera.
 */
public final class Dijkstra {

    // Datos de trabajo de cada vértice (se mutan durante el algoritmo)
    private static class Estado {
        private double distancia = Double.POSITIVE_INFINITY;
        private int previo = -1;
        private boolean cerrado;
    }

    private Dijkstra() {
    }

    public static <T> ResultadoCamino<T> caminoMinimo(Grafo<T> grafo, T origen, T destino) {
        return caminoMinimo(grafo, origen, destino, CalculadorPeso.base());
    }

    public static <T> ResultadoCamino<T> caminoMinimo(Grafo<T> grafo, T origen, T destino,
                                                      CalculadorPeso<T> calculador) {
        int io = grafo.indiceDe(origen);
        int id = grafo.indiceDe(destino);
        if (io < 0 || id < 0) {
            throw new IllegalArgumentException("origen o destino no existen en el grafo");
        }

        Lista<Estado> estados = new ListaEnlazada<>();
        for (int i = 0; i < grafo.cantidadVertices(); i++) {
            estados.agregar(new Estado());
        }
        estados.obtener(io).distancia = 0;

        ABB<EntradaDistancia> prioridad = new ABB<>();
        prioridad.insertar(new EntradaDistancia(io, 0));

        while (!prioridad.estaVacio()) {
            EntradaDistancia menor = prioridad.minimo();
            prioridad.eliminar(menor);
            Estado actual = estados.obtener(menor.getIndice());
            if (actual.cerrado || menor.getDistancia() > actual.distancia) {
                continue; // entrada vieja
            }
            actual.cerrado = true;
            if (menor.getIndice() == id) {
                break;
            }
            T verticeActual = grafo.vertice(menor.getIndice());
            Lista<Arista<T>> aristas = grafo.aristasInternas(menor.getIndice());
            for (int i = 0; i < aristas.tamanio(); i++) {
                Arista<T> arista = aristas.obtener(i);
                int j = grafo.indiceDe(arista.getDestino());
                Estado vecino = estados.obtener(j);
                if (vecino.cerrado) {
                    continue;
                }
                double peso = calculador.peso(verticeActual, arista.getDestino(), arista.getPeso());
                if (peso < 0) {
                    throw new IllegalStateException("Dijkstra no admite pesos negativos");
                }
                double nueva = actual.distancia + peso;
                if (nueva < vecino.distancia) {
                    vecino.distancia = nueva;
                    vecino.previo = menor.getIndice();
                    prioridad.insertar(new EntradaDistancia(j, nueva));
                }
            }
        }

        if (estados.obtener(id).distancia == Double.POSITIVE_INFINITY) {
            return ResultadoCamino.sinCamino();
        }
        Lista<T> ruta = new ListaEnlazada<>();
        int paso = id;
        while (paso != -1) {
            ruta.insertar(0, grafo.vertice(paso));
            paso = estados.obtener(paso).previo;
        }
        return ResultadoCamino.conCamino(ruta, estados.obtener(id).distancia);
    }
}
