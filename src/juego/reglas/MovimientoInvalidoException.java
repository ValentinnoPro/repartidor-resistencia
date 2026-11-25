package juego.reglas;

/** Se pidió algo que no se puede hacer ahora (calle inexistente, contramano, deshacer sin historial...). */
public class MovimientoInvalidoException extends Exception {

    public MovimientoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
