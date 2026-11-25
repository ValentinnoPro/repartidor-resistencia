package arboles;

import estructuras.Cola;
import estructuras.ColaEnlazada;

/**
 * Árbol binario común (sin orden entre los valores).
 *
 * - {@link #insertar(Object)} agrega en el primer lugar libre por niveles
 *   (usa la Cola propia), así el árbol queda lo más completo posible.
 * - Para armar árboles con forma propia (como el árbol de mejoras) están
 *   {@link #insertarIzquierdo} / {@link #insertarDerecho} y {@link #setRaiz}.
 */
public class ArbolBinario<T> extends ArbolBase<T> {

    public void setRaiz(NodoBinario<T> nuevaRaiz) {
        this.raiz = nuevaRaiz;
    }

    @Override
    public void insertar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("no se puede insertar null");
        }
        NodoBinario<T> nuevo = new NodoBinario<>(valor);
        if (raiz == null) {
            raiz = nuevo;
            return;
        }
        Cola<NodoBinario<T>> pendientes = new ColaEnlazada<>();
        pendientes.encolar(raiz);
        while (!pendientes.estaVacia()) {
            NodoBinario<T> actual = pendientes.desencolar();
            if (actual.getIzquierdo() == null) {
                actual.setIzquierdo(nuevo);
                return;
            }
            pendientes.encolar(actual.getIzquierdo());
            if (actual.getDerecho() == null) {
                actual.setDerecho(nuevo);
                return;
            }
            pendientes.encolar(actual.getDerecho());
        }
    }

    public void insertarIzquierdo(T padre, T valor) {
        NodoBinario<T> nodoPadre = nodoPadreLibre(padre);
        if (nodoPadre.getIzquierdo() != null) {
            throw new IllegalStateException("el hijo izquierdo ya existe");
        }
        nodoPadre.setIzquierdo(new NodoBinario<>(valor));
    }

    public void insertarDerecho(T padre, T valor) {
        NodoBinario<T> nodoPadre = nodoPadreLibre(padre);
        if (nodoPadre.getDerecho() != null) {
            throw new IllegalStateException("el hijo derecho ya existe");
        }
        nodoPadre.setDerecho(new NodoBinario<>(valor));
    }

    private NodoBinario<T> nodoPadreLibre(T padre) {
        NodoBinario<T> nodo = buscarNodo(padre);
        if (nodo == null) {
            throw new IllegalArgumentException("el padre no está en el árbol");
        }
        return nodo;
    }

    // Búsqueda en todo el árbol (no hay orden que aprovechar), con caso base explícito
    @Override
    protected NodoBinario<T> buscarNodo(T valor) {
        return buscarNodo(raiz, valor);
    }

    private NodoBinario<T> buscarNodo(NodoBinario<T> nodo, T valor) {
        if (nodo == null) {
            return null;
        }
        if (nodo.getValor().equals(valor)) {
            return nodo;
        }
        NodoBinario<T> enIzquierda = buscarNodo(nodo.getIzquierdo(), valor);
        if (enIzquierda != null) {
            return enIzquierda;
        }
        return buscarNodo(nodo.getDerecho(), valor);
    }
}
