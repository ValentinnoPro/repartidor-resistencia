package persistencia;

import estructuras.Lista;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Escritura de texto plano línea por línea. La usan las pruebas (y la demo
 * en vivo) para armar archivos de prueba, incluso dañados a propósito.
 */
public final class ArchivoTexto {

    private ArchivoTexto() {
    }

    public static void escribirLineas(String ruta, Lista<String> lineas) throws IOException {
        Respaldo.asegurarCarpeta(ruta);
        try (BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruta), "UTF-8"))) {
            for (int i = 0; i < lineas.tamanio(); i++) {
                salida.write(lineas.obtener(i));
                salida.newLine();
            }
        }
    }
}
