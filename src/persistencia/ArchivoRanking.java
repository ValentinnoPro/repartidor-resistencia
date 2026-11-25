package persistencia;

import arboles.ABB;
import estructuras.Lista;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import juego.reglas.ArchivoDanadoException;
import juego.reglas.EntradaRanking;

/**
 * Lee y guarda el ranking (datos/ranking.txt).
 *
 * FORMATO: una línea por jugador (las que empiezan con # son comentarios):
 *
 *   nombre;puntaje;entregas;fecha(AAAA-MM-DD)     ej.  Lucia;2350;14;2026-11-25
 *
 * Se guarda en el orden del ABB (mejor puntaje primero). Antes de sobrescribir
 * se copia el archivo anterior a ranking.txt.bak. Si falta el archivo, el
 * ranking arranca vacío; una línea dañada se saltea y se anota un ErrorCarga.
 */
public final class ArchivoRanking {

    private ArchivoRanking() {
    }

    public static ABB<EntradaRanking> cargar(String ruta, Lista<ErrorCarga> errores) {
        ABB<EntradaRanking> ranking = new ABB<>();
        if (!Lector.existe(ruta)) {
            errores.agregar(new ErrorCarga(ruta, 0, "no existe, el ranking arranca vacío"));
            return ranking;
        }
        Lista<String> lineas;
        try {
            lineas = Lector.leerLineas(ruta);
        } catch (IOException e) {
            errores.agregar(new ErrorCarga(ruta, 0, "no se pudo leer (" + e.getMessage() + "), el ranking arranca vacío"));
            return ranking;
        }
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i).trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            try {
                ranking.insertar(interpretar(linea));
            } catch (ArchivoDanadoException | IllegalArgumentException e) {
                errores.agregar(new ErrorCarga(ruta, i + 1, e.getMessage()));
            }
        }
        return ranking;
    }

    private static EntradaRanking interpretar(String linea) throws ArchivoDanadoException {
        String[] partes = linea.split(";");
        if (partes.length != 4) {
            throw new ArchivoDanadoException("se esperaban 4 campos: " + linea);
        }
        try {
            return new EntradaRanking(partes[0], Integer.parseInt(partes[1].trim()),
                    Integer.parseInt(partes[2].trim()), partes[3].trim());
        } catch (NumberFormatException e) {
            throw new ArchivoDanadoException("número inválido: " + linea);
        }
    }

    /** Guarda sin propagar IOException: si falla, anota un ErrorCarga y devuelve false. */
    public static boolean guardar(String ruta, ABB<EntradaRanking> ranking, Lista<ErrorCarga> errores) {
        try {
            guardar(ruta, ranking);
            return true;
        } catch (IOException e) {
            errores.agregar(new ErrorCarga(ruta, 0, "no se pudo guardar: " + e.getMessage()));
            return false;
        }
    }

    public static void guardar(String ruta, ABB<EntradaRanking> ranking) throws IOException {
        Respaldo.asegurarCarpeta(ruta);
        Respaldo.copiar(ruta);
        try (BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruta), "UTF-8"))) {
            salida.write("# nombre;puntaje;entregas;fecha(AAAA-MM-DD)");
            salida.newLine();
            Lista<EntradaRanking> filas = ranking.inOrden();
            for (int i = 0; i < filas.tamanio(); i++) {
                EntradaRanking e = filas.obtener(i);
                salida.write(e.getNombre() + ";" + e.getPuntaje() + ";" + e.getEntregas() + ";" + e.getFecha());
                salida.newLine();
            }
        }
    }
}
