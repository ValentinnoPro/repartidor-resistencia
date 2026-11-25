package persistencia;

/**
 * Un problema encontrado al leer un archivo: cuál, en qué línea (0 si es del
 * archivo entero) y por qué. El menú los muestra como avisos.
 */
public class ErrorCarga {

    private final String archivo;
    private final int linea;
    private final String motivo;

    public ErrorCarga(String archivo, int linea, String motivo) {
        this.archivo = archivo;
        this.linea = linea;
        this.motivo = motivo;
    }

    public String getArchivo() {
        return archivo;
    }

    public int getLinea() {
        return linea;
    }

    public String getMotivo() {
        return motivo;
    }

    @Override
    public String toString() {
        String nombre = archivo;
        int barra = Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\'));
        if (barra >= 0) {
            nombre = nombre.substring(barra + 1);
        }
        return linea > 0 ? nombre + " línea " + linea + ": " + motivo : nombre + ": " + motivo;
    }
}
