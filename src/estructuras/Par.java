package estructuras;

/**
 * Par inmutable de dos valores. Sirve para guardar cosas como
 * (esquina, saltos) en una Cola o (nodo, predecesor) en una Lista.
 */
public final class Par<A, B> {

    private final A primero;
    private final B segundo;

    public Par(A primero, B segundo) {
        this.primero = primero;
        this.segundo = segundo;
    }

    public A getPrimero() {
        return primero;
    }

    public B getSegundo() {
        return segundo;
    }
}
