package arboles;

import estructuras.ColaEnlazada;
import estructuras.Cola;
import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Parte común de todos los árboles binarios: altura, conteos y los cuatro
 * recorridos. Lo que cambia entre un árbol binario "a secas" y un ABB es
 * cómo se inserta y cómo se busca, por eso esos dos métodos son abstractos
 * (polimorfismo: cada subclase los resuelve a su manera).
 *
 * Todos los métodos recursivos tienen caso base explícito (nodo == null).
 * Los recorridos devuelven una Lista y no imprimen nada.
 */
public abstract class ArbolBase<T> {

    // La raíz es protegida: solo los árboles hijos la manipulan
    protected NodoBinario<T> raiz;

    // Operaciones que cada tipo de árbol define a su manera
    public abstract void insertar(T valor);

    protected abstract NodoBinario<T> buscarNodo(T valor);

    // Consultas
    public boolean estaVacio() {
        return raiz == null;
    }

    public NodoBinario<T> getRaiz() {
        return raiz;
    }

    /** Devuelve el valor guardado que es igual a {@code valor}, o null si no está. */
    public T buscar(T valor) {
        NodoBinario<T> nodo = buscarNodo(valor);
        return nodo == null ? null : nodo.getValor();
    }

    public boolean contiene(T valor) {
        return buscarNodo(valor) != null;
    }

    public int altura() {
        return altura(raiz);
    }

    private int altura(NodoBinario<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho()));
    }

    public int cantidadNodos() {
        return cantidadNodos(raiz);
    }

    private int cantidadNodos(NodoBinario<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + cantidadNodos(nodo.getIzquierdo()) + cantidadNodos(nodo.getDerecho());
    }

    public int cantidadHojas() {
        return cantidadHojas(raiz);
    }

    private int cantidadHojas(NodoBinario<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        if (nodo.esHoja()) {
            return 1;
        }
        return cantidadHojas(nodo.getIzquierdo()) + cantidadHojas(nodo.getDerecho());
    }

    // Recorridos en profundidad (recursivos)
    public Lista<T> preOrden() {
        Lista<T> resultado = new ListaEnlazada<>();
        preOrden(raiz, resultado);
        return resultado;
    }

    private void preOrden(NodoBinario<T> nodo, Lista<T> salida) {
        if (nodo == null) {
            return;
        }
        salida.agregar(nodo.getValor());
        preOrden(nodo.getIzquierdo(), salida);
        preOrden(nodo.getDerecho(), salida);
    }

    public Lista<T> inOrden() {
        Lista<T> resultado = new ListaEnlazada<>();
        inOrden(raiz, resultado);
        return resultado;
    }

    private void inOrden(NodoBinario<T> nodo, Lista<T> salida) {
        if (nodo == null) {
            return;
        }
        inOrden(nodo.getIzquierdo(), salida);
        salida.agregar(nodo.getValor());
        inOrden(nodo.getDerecho(), salida);
    }

    public Lista<T> postOrden() {
        Lista<T> resultado = new ListaEnlazada<>();
        postOrden(raiz, resultado);
        return resultado;
    }

    private void postOrden(NodoBinario<T> nodo, Lista<T> salida) {
        if (nodo == null) {
            return;
        }
        postOrden(nodo.getIzquierdo(), salida);
        postOrden(nodo.getDerecho(), salida);
        salida.agregar(nodo.getValor());
    }

    // Recorrido por niveles: usa la Cola propia (FIFO)
    public Lista<T> porNiveles() {
        Lista<T> resultado = new ListaEnlazada<>();
        if (raiz == null) {
            return resultado;
        }
        Cola<NodoBinario<T>> pendientes = new ColaEnlazada<>();
        pendientes.encolar(raiz);
        while (!pendientes.estaVacia()) {
            NodoBinario<T> nodo = pendientes.desencolar();
            resultado.agregar(nodo.getValor());
            if (nodo.getIzquierdo() != null) {
                pendientes.encolar(nodo.getIzquierdo());
            }
            if (nodo.getDerecho() != null) {
                pendientes.encolar(nodo.getDerecho());
            }
        }
        return resultado;
    }
}
