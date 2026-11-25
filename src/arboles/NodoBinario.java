package arboles;

/** Nodo de un árbol binario: un valor y dos hijos (que pueden ser null). */
public class NodoBinario<T> {

    // Atributos
    private T valor;
    private NodoBinario<T> izquierdo;
    private NodoBinario<T> derecho;

    // Constructor
    public NodoBinario(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("el valor de un nodo no puede ser nulo");
        }
        this.valor = valor;
    }

    // Acceso
    public T getValor() {
        return valor;
    }

    public void setValor(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("el valor de un nodo no puede ser nulo");
        }
        this.valor = valor;
    }

    public NodoBinario<T> getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoBinario<T> izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoBinario<T> getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoBinario<T> derecho) {
        this.derecho = derecho;
    }

    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }
}
