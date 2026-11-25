package juego.pantallas;

import java.awt.event.KeyEvent;
import motor.Entrada;

/**
 * Caja de texto sobre la Entrada del kit, que solo informa teclas: acá se
 * convierten las letras, números, espacio y borrar en un texto.
 */
public class CajaTexto {

    private final StringBuilder texto = new StringBuilder();
    private final int largoMaximo;

    public CajaTexto(int largoMaximo) {
        if (largoMaximo <= 0) {
            throw new IllegalArgumentException("el largo máximo debe ser positivo");
        }
        this.largoMaximo = largoMaximo;
    }

    /** Llamar una vez por paso: agrega lo que se tecleó en este paso. */
    public void procesar(Entrada entrada) {
        for (int codigo = KeyEvent.VK_A; codigo <= KeyEvent.VK_Z; codigo++) {
            if (entrada.fuePresionada(codigo)) {
                agregar((char) ('a' + (codigo - KeyEvent.VK_A)));
            }
        }
        for (int codigo = KeyEvent.VK_0; codigo <= KeyEvent.VK_9; codigo++) {
            if (entrada.fuePresionada(codigo)) {
                agregar((char) ('0' + (codigo - KeyEvent.VK_0)));
            }
        }
        if (entrada.fuePresionada(KeyEvent.VK_SPACE)) {
            agregar(' ');
        }
        if (entrada.fuePresionada(KeyEvent.VK_BACK_SPACE) && texto.length() > 0) {
            texto.deleteCharAt(texto.length() - 1);
        }
    }

    private void agregar(char c) {
        if (texto.length() < largoMaximo) {
            texto.append(c);
        }
    }

    public String getTexto() {
        return texto.toString();
    }

    public void vaciar() {
        texto.setLength(0);
    }
}
