package verificador;

import estructuras.ColaEnlazada;
import estructuras.ListaEnlazada;
import estructuras.PilaEnlazada;

public class PruebaMisEstructuras {

    // Punto de entrada
    public static void main(String[] args) {

        // Lista
        VerificadorEstructuras.verificarLista(new ListaEnlazada<Integer>());

        // Pila
        VerificadorEstructuras.verificarPila(new PilaEnlazada<Integer>());

        // Cola
        VerificadorEstructuras.verificarCola(new ColaEnlazada<Integer>());
    }
}
