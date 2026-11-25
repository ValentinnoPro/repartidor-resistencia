package juego.reglas;

import estructuras.Lista;
import estructuras.ListaEnlazada;

/** Los tres textos con los que se entrenan los modelos de Markov. */
public class Corpus {

    private final Lista<String> nombres = new ListaEnlazada<>();
    private final Lista<String> reclamos = new ListaEnlazada<>();
    private final Lista<String> radio = new ListaEnlazada<>();

    public Lista<String> getNombres() {
        return nombres;
    }

    public Lista<String> getReclamos() {
        return reclamos;
    }

    public Lista<String> getRadio() {
        return radio;
    }
}
