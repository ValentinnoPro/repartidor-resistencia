package estructuras;

/**
 * Pila (LIFO) con nodos enlazados: el tope es el primer nodo.
 * Todas las operaciones son O(1).
 *
 * En el juego se usa para deshacer el último tramo recorrido (se deshace lo
 * último que se hizo) y para el in-orden iterativo del ABB.
 */
public class PilaEnlazada<T> implements Pila<T> {

    private static class Nodo<T> {
        private final T valor;
        private final Nodo<T> debajo;

        private Nodo(T valor, Nodo<T> debajo) {
            this.valor = valor;
            this.debajo = debajo;
        }
    }

    // Atributos
    private Nodo<T> tope;
    private int tamanio;

    @Override
    public void apilar(T elemento) {
        tope = new Nodo<>(elemento, tope);
        tamanio++;
    }

    @Override
    public T desapilar() {
        verificarNoVacia();
        T valor = tope.valor;
        tope = tope.debajo;
        tamanio--;
        return valor;
    }

    @Override
    public T tope() {
        verificarNoVacia();
        return tope.valor;
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
        tope = null;
        tamanio = 0;
    }

    private void verificarNoVacia() {
        if (tamanio == 0) {
            throw new IllegalStateException("la pila está vacía");
        }
    }
}
