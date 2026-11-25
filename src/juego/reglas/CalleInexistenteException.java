package juego.reglas;

/** El texto escrito no nombra calles del mapa, o las calles no se cruzan. */
public class CalleInexistenteException extends Exception {

    public CalleInexistenteException(String mensaje) {
        super(mensaje);
    }
}
