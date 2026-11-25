package juego.reglas;

/** El pedido pasó su hora límite. */
public class PedidoVencidoException extends Exception {

    public PedidoVencidoException(String mensaje) {
        super(mensaje);
    }
}
