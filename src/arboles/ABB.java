package arboles;

import estructuras.Lista;
import estructuras.ListaEnlazada;
import estructuras.Pila;
import estructuras.PilaEnlazada;

/**
 * Árbol binario de búsqueda: a la izquierda va lo menor y a la derecha lo mayor.
 *
 * DUPLICADOS: el ABB nunca guarda dos elementos "iguales" (compareTo == 0);
 * si llega uno, se ignora. Por eso las clases del dominio (Pedido,
 * EntradaRanking, EntradaDistancia...) desempatan en su compareTo con un
 * criterio extra (id, nombre, fecha) y así nunca chocan dos claves.
 *
 * Costos: buscar, insertar y eliminar son O(h): O(log n) en promedio y O(n)
 * si los datos entran ordenados y el árbol degenera en una lista.
 */
public class ABB<T extends Comparable<T>> extends ArbolBase<T> {

    // Inserción
    @Override
    public void insertar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("no se puede insertar null");
        }
        raiz = insertar(raiz, valor);
    }

    private NodoBinario<T> insertar(NodoBinario<T> nodo, T valor) {
        if (nodo == null) {
            return new NodoBinario<>(valor);
        }
        int comparacion = valor.compareTo(nodo.getValor());
        if (comparacion < 0) {
            nodo.setIzquierdo(insertar(nodo.getIzquierdo(), valor));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertar(nodo.getDerecho(), valor));
        }
        // comparacion == 0: duplicado, se ignora (ver nota de la clase)
        return nodo;
    }

    // Búsqueda
    @Override
    protected NodoBinario<T> buscarNodo(T valor) {
        return buscarNodo(raiz, valor);
    }

    private NodoBinario<T> buscarNodo(NodoBinario<T> nodo, T valor) {
        if (nodo == null) {
            return null;
        }
        int comparacion = valor.compareTo(nodo.getValor());
        if (comparacion == 0) {
            return nodo;
        }
        return comparacion < 0
                ? buscarNodo(nodo.getIzquierdo(), valor)
                : buscarNodo(nodo.getDerecho(), valor);
    }

    // Mínimo y máximo
    public T minimo() {
        if (raiz == null) {
            throw new IllegalStateException("el árbol está vacío");
        }
        return minimoNodo(raiz).getValor();
    }

    public T maximo() {
        if (raiz == null) {
            throw new IllegalStateException("el árbol está vacío");
        }
        NodoBinario<T> actual = raiz;
        while (actual.getDerecho() != null) {
            actual = actual.getDerecho();
        }
        return actual.getValor();
    }

    private NodoBinario<T> minimoNodo(NodoBinario<T> desde) {
        NodoBinario<T> actual = desde;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual;
    }

    // Eliminación: devuelve true si el valor estaba
    public boolean eliminar(T valor) {
        if (valor == null || buscarNodo(valor) == null) {
            return false;
        }
        raiz = eliminar(raiz, valor);
        return true;
    }

    private NodoBinario<T> eliminar(NodoBinario<T> nodo, T valor) {
        if (nodo == null) {
            return null;
        }
        int comparacion = valor.compareTo(nodo.getValor());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminar(nodo.getIzquierdo(), valor));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminar(nodo.getDerecho(), valor));
        } else {
            // Caso 1 y 2: hoja o un solo hijo, el hijo (o null) ocupa su lugar
            if (nodo.getIzquierdo() == null) {
                return nodo.getDerecho();
            }
            if (nodo.getDerecho() == null) {
                return nodo.getIzquierdo();
            }
            // Caso 3: dos hijos, se reemplaza por el sucesor in-orden
            // (el menor del subárbol derecho) y se lo borra de su lugar
            NodoBinario<T> sucesor = minimoNodo(nodo.getDerecho());
            nodo.setValor(sucesor.getValor());
            nodo.setDerecho(eliminar(nodo.getDerecho(), sucesor.getValor()));
        }
        return nodo;
    }

    // In-orden sin recursión: la Pila propia reemplaza a la pila de llamadas
    public Lista<T> inOrdenIterativo() {
        Lista<T> resultado = new ListaEnlazada<>();
        Pila<NodoBinario<T>> pila = new PilaEnlazada<>();
        NodoBinario<T> actual = raiz;
        while (actual != null || !pila.estaVacia()) {
            while (actual != null) {
                pila.apilar(actual);
                actual = actual.getIzquierdo();
            }
            actual = pila.desapilar();
            resultado.agregar(actual.getValor());
            actual = actual.getDerecho();
        }
        return resultado;
    }
}
