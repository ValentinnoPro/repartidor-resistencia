package estructuras;

/**
 * Lista simplemente enlazada con puntero al último nodo.
 *
 * Por qué enlazada: el juego agrega y quita elementos todo el tiempo (pedidos,
 * aristas, sucesores de Markov) y casi nunca necesita saltar a una posición
 * arbitraria, así que no hace falta redimensionar un arreglo.
 *
 * Costos: agregar al final O(1), insertar/eliminar en el medio O(n),
 * obtener(i) O(n). Para que el recorrido secuencial con un for no sea O(n²)
 * se recuerda el último nodo visitado (caché): pedir i, i+1, i+2... cuesta O(1)
 * cada uno.
 */
public class ListaEnlazada<T> implements Lista<T> {

    // Nodo interno: nadie fuera de la lista lo conoce
    private static class Nodo<T> {
        private final T valor;
        private Nodo<T> siguiente;

        private Nodo(T valor) {
            this.valor = valor;
        }
    }

    // Atributos
    private Nodo<T> primero;
    private Nodo<T> ultimo;
    private int tamanio;

    // Caché del último acceso por índice
    private Nodo<T> cacheNodo;
    private int cacheIndice = -1;

    // Altas
    @Override
    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (primero == null) {
            primero = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamanio++;
    }

    @Override
    public void insertar(int indice, T elemento) {
        if (indice < 0 || indice > tamanio) {
            throw new IndexOutOfBoundsException("índice " + indice + ", tamaño " + tamanio);
        }
        if (indice == tamanio) {
            agregar(elemento);
            return;
        }
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (indice == 0) {
            nuevo.siguiente = primero;
            primero = nuevo;
        } else {
            Nodo<T> anterior = nodoEn(indice - 1);
            nuevo.siguiente = anterior.siguiente;
            anterior.siguiente = nuevo;
        }
        tamanio++;
        invalidarCache();
    }

    // Consultas
    @Override
    public T obtener(int indice) {
        verificarIndice(indice);
        return nodoEn(indice).valor;
    }

    @Override
    public int indiceDe(T elemento) {
        int i = 0;
        for (Nodo<T> actual = primero; actual != null; actual = actual.siguiente) {
            boolean iguales = actual.valor == null ? elemento == null : actual.valor.equals(elemento);
            if (iguales) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int tamanio() {
        return tamanio;
    }

    @Override
    public boolean estaVacia() {
        return tamanio == 0;
    }

    // Bajas
    @Override
    public T eliminar(int indice) {
        verificarIndice(indice);
        T valor;
        if (indice == 0) {
            valor = primero.valor;
            primero = primero.siguiente;
            if (primero == null) {
                ultimo = null;
            }
        } else {
            Nodo<T> anterior = nodoEn(indice - 1);
            Nodo<T> objetivo = anterior.siguiente;
            valor = objetivo.valor;
            anterior.siguiente = objetivo.siguiente;
            if (objetivo == ultimo) {
                ultimo = anterior;
            }
        }
        tamanio--;
        invalidarCache();
        return valor;
    }

    @Override
    public void vaciar() {
        primero = null;
        ultimo = null;
        tamanio = 0;
        invalidarCache();
    }

    // Auxiliares
    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("índice " + indice + ", tamaño " + tamanio);
        }
    }

    private Nodo<T> nodoEn(int indice) {
        Nodo<T> actual;
        int k;
        if (cacheNodo != null && cacheIndice <= indice) {
            actual = cacheNodo;
            k = cacheIndice;
        } else {
            actual = primero;
            k = 0;
        }
        while (k < indice) {
            actual = actual.siguiente;
            k++;
        }
        cacheNodo = actual;
        cacheIndice = indice;
        return actual;
    }

    private void invalidarCache() {
        cacheNodo = null;
        cacheIndice = -1;
    }
}
