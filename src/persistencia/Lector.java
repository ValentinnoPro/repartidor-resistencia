package persistencia;

import estructuras.Lista;
import estructuras.ListaEnlazada;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/** Lee un archivo de texto UTF-8 completo y devuelve sus líneas en una Lista. */
final class Lector {

    private Lector() {
    }

    static boolean existe(String ruta) {
        return new File(ruta).isFile();
    }

    static Lista<String> leerLineas(String ruta) throws IOException {
        Lista<String> lineas = new ListaEnlazada<>();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(new FileInputStream(ruta), "UTF-8"))) {
            String linea = lector.readLine();
            while (linea != null) {
                lineas.agregar(linea);
                linea = lector.readLine();
            }
        }
        return lineas;
    }
}
