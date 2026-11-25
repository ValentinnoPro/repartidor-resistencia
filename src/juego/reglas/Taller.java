package juego.reglas;

import arboles.ArbolBinario;
import arboles.NodoBinario;
import estructuras.Lista;

/**
 * El taller: el árbol de mejoras del vehículo y la plata ahorrada.
 *
 * El árbol decide cosas del juego:
 * - solo se puede comprar una mejora si su padre ya está comprado;
 * - el factor de velocidad total (producto de las compradas) cambia el tiempo
 *   de cada tramo, y con él los pesos que usa Dijkstra;
 * - pre-orden para guardar, por niveles para dibujar, in-orden para listar
 *   y post-orden para sumar el costo de una rama.
 */
public class Taller {

    // Atributos
    private final ArbolBinario<Mejora> arbol;
    private int dinero;

    // Constructor con validación
    public Taller(ArbolBinario<Mejora> arbol, int dinero) {
        if (arbol == null || arbol.estaVacio()) {
            throw new IllegalArgumentException("el taller necesita un árbol de mejoras");
        }
        if (dinero < 0) {
            throw new IllegalArgumentException("el dinero no puede ser negativo: " + dinero);
        }
        this.arbol = arbol;
        this.dinero = dinero;
    }

    /** Árbol de mejoras que se usa cuando no hay archivo. */
    public static Taller porDefecto() {
        ArbolBinario<Mejora> arbol = new ArbolBinario<>();
        NodoBinario<Mejora> base = new NodoBinario<>(new Mejora("Base", 0, 1.0, true));
        NodoBinario<Mejora> motor = new NodoBinario<>(new Mejora("Motor", 200, 0.9, false));
        motor.setIzquierdo(new NodoBinario<>(new Mejora("Turbo", 500, 0.8, false)));
        motor.setDerecho(new NodoBinario<>(new Mejora("Frenos", 300, 0.85, false)));
        NodoBinario<Mejora> mochila = new NodoBinario<>(new Mejora("Mochila", 150, 0.95, false));
        mochila.setIzquierdo(new NodoBinario<>(new Mejora("Amortiguadores", 250, 0.9, false)));
        base.setIzquierdo(motor);
        base.setDerecho(mochila);
        arbol.setRaiz(base);
        return new Taller(arbol, 0);
    }

    // Consultas
    public ArbolBinario<Mejora> getArbol() {
        return arbol;
    }

    public int getDinero() {
        return dinero;
    }

    public void depositar(int monto) {
        if (monto < 0) {
            throw new IllegalArgumentException("no se puede depositar un monto negativo");
        }
        dinero += monto;
    }

    /** Producto de los factores de las mejoras compradas (recorrido in-orden), con piso. */
    public double getFactorVelocidad() {
        double factor = 1.0;
        Lista<Mejora> todas = arbol.inOrden();
        for (int i = 0; i < todas.tamanio(); i++) {
            Mejora m = todas.obtener(i);
            if (m.isComprada()) {
                factor *= m.getFactorVelocidad();
            }
        }
        return Math.max(factor, Vehiculo.FACTOR_MEJORAS_MINIMO);
    }

    /** Se puede comprar si no está comprada, alcanza la plata y su padre ya está comprado. */
    public boolean puedeComprar(Mejora mejora) {
        return mejora != null
                && !mejora.isComprada()
                && dinero >= mejora.getCosto()
                && padreComprado(arbol.getRaiz(), mejora);
    }

    // Recursivo: busca el padre de la mejora y mira si está comprado
    private boolean padreComprado(NodoBinario<Mejora> nodo, Mejora hija) {
        if (nodo == null) {
            return false;
        }
        boolean esPadre = (nodo.getIzquierdo() != null && nodo.getIzquierdo().getValor().equals(hija))
                || (nodo.getDerecho() != null && nodo.getDerecho().getValor().equals(hija));
        if (esPadre) {
            return nodo.getValor().isComprada();
        }
        return padreComprado(nodo.getIzquierdo(), hija) || padreComprado(nodo.getDerecho(), hija);
    }

    public void comprar(Mejora mejora) {
        if (!puedeComprar(mejora)) {
            throw new IllegalStateException("no se puede comprar esa mejora ahora");
        }
        dinero -= mejora.getCosto();
        mejora.comprar();
    }

    /** Costo total de una rama (la mejora y todo lo que cuelga de ella), con post-orden. */
    public int costoDeRama(Mejora mejora) {
        NodoBinario<Mejora> nodo = nodoDe(arbol.getRaiz(), mejora);
        if (nodo == null) {
            throw new IllegalArgumentException("la mejora no está en el árbol");
        }
        ArbolBinario<Mejora> rama = new ArbolBinario<>();
        rama.setRaiz(nodo);
        Lista<Mejora> nodos = rama.postOrden();
        int total = 0;
        for (int i = 0; i < nodos.tamanio(); i++) {
            total += nodos.obtener(i).getCosto();
        }
        return total;
    }

    private NodoBinario<Mejora> nodoDe(NodoBinario<Mejora> nodo, Mejora buscada) {
        if (nodo == null) {
            return null;
        }
        if (nodo.getValor().equals(buscada)) {
            return nodo;
        }
        NodoBinario<Mejora> enIzquierda = nodoDe(nodo.getIzquierdo(), buscada);
        return enIzquierda != null ? enIzquierda : nodoDe(nodo.getDerecho(), buscada);
    }
}
