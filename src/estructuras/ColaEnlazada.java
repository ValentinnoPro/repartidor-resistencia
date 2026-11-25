package estructuras;

/**
 * Cola (FIFO) con nodos enlazados: se encola por el final y se desencola por
 * el frente. Todas las operaciones son O(1).
 *
 * En el juego se usa para los pedidos entrantes (el primero que llegó es el
 * primero que se atiende), para la ruta automática y para los recorridos
 * por niveles (BFS).
 */
public class ColaEnlazada<T> implements Cola<T> {

    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    // Atributos
    private Nodo<T> frente;
    private Nodo<T> fondo;
    private int tamanio;

    @Override
    public void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (fondo == null) {
            frente = nuevo;
        } else {
            fondo.siguiente = nuevo;
        }
        fondo = nuevo;
        tamanio++;
    }

    @Override
    public T desencolar() {
        verificarNoVacia();
        T valor = frente.valor;
        frente = frente.siguiente;
        if (frente == null) {
            fondo = null;
        }
        tamanio--;
        return valor;
    }

    @Override
    public T frente() {
        verificarNoVacia();
        return frente.valor;
    }

    @Override
    public boolean estaVacia() {
        return tamanio == 0;
    }

    @Override
    public int tamanio() {
        return tamanio;
    }

    @Override
    public void vaciar() {
        frente = null;
        fondo = null;
        tamanio = 0;
    }

    private void verificarNoVacia() {
        if (tamanio == 0) {
            throw new IllegalStateException("la cola está vacía");
        }
    }
}
