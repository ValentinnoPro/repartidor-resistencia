import estructuras.Lista;
import estructuras.ListaEnlazada;
import java.util.Random;
import juego.reglas.Corpus;
import markov.ModeloMarkov;
import markov.RetrocesoDeContexto;
import persistencia.ArchivoCorpus;
import persistencia.ErrorCarga;

/**
 * Genera 10 salidas con k = 1, 2, 3 y 4 para cada sección del corpus, con una
 * semilla fija, para comparar la calidad (se usa en el informe).
 * Se ejecuta con: ejecutar.bat PruebaMarkov
 */
public class PruebaMarkov {

    public static void main(String[] args) {
        Lista<ErrorCarga> errores = new ListaEnlazada<>();
        Corpus corpus = ArchivoCorpus.cargar("datos/corpus_clientes.txt", errores);
        mostrar("NOMBRES", corpus.getNombres(), 24);
        mostrar("RECLAMOS", corpus.getReclamos(), 60);
        mostrar("RADIO", corpus.getRadio(), 70);
    }

    private static void mostrar(String titulo, Lista<String> lineas, int largoMax) {
        System.out.println("=== " + titulo + " (" + lineas.tamanio() + " lineas de corpus)");
        for (int k = 1; k <= 4; k++) {
            ModeloMarkov modelo = new ModeloMarkov(k, new RetrocesoDeContexto());
            modelo.entrenar(lineas);
            Random azar = new Random(42);
            System.out.println("-- k = " + k + "  (" + modelo.cantidadEstados() + " estados)");
            for (int i = 0; i < 10; i++) {
                System.out.println("   " + modelo.generar(azar, largoMax));
            }
        }
    }
}
