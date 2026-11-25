package persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Copia de seguridad: antes de sobrescribir un archivo se copia a
 * "archivo.bak". Se copia línea por línea con try-with-resources (sin buffers
 * de bytes, porque los arreglos están prohibidos fuera de los TADs).
 */
public final class Respaldo {

    private Respaldo() {
    }

    /** Copia el archivo a ruta + ".bak". Si el archivo no existe no hace nada. */
    public static void copiar(String ruta) throws IOException {
        if (!new File(ruta).isFile()) {
            return;
        }
        try (BufferedReader origen = new BufferedReader(new InputStreamReader(new FileInputStream(ruta), "UTF-8"));
             BufferedWriter destino = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruta + ".bak"), "UTF-8"))) {
            String linea = origen.readLine();
            while (linea != null) {
                destino.write(linea);
                destino.newLine();
                linea = origen.readLine();
            }
        }
    }

    public static boolean existe(String ruta) {
        return new File(ruta).isFile();
    }

    /** Borra un archivo si existe (lo usan las pruebas para limpiar). */
    public static void borrar(String ruta) {
        File archivo = new File(ruta);
        if (archivo.isFile()) {
            archivo.delete();
        }
    }

    /** Crea la carpeta del archivo si hace falta. */
    static void asegurarCarpeta(String ruta) {
        File padre = new File(ruta).getAbsoluteFile().getParentFile();
        if (padre != null && !padre.exists()) {
            padre.mkdirs();
        }
    }
}
