package juego.reglas;

/** Una línea de un archivo de datos no tiene el formato esperado. */
public class ArchivoDanadoException extends Exception {

    public ArchivoDanadoException(String mensaje) {
        super(mensaje);
    }
}
