package persistencia;

import estructuras.Lista;
import java.io.IOException;
import juego.reglas.Corpus;

/**
 * Lee el corpus de Markov (datos/corpus_clientes.txt). Solo lectura.
 *
 * FORMATO: una entrada por línea, agrupadas en secciones con encabezado
 * (las líneas que empiezan con # son comentarios):
 *
 *   [NOMBRES]     ej.  Lucia Gomez
 *   [RECLAMOS]    ej.  Donde esta mi empanada
 *   [RADIO]       ej.  Atencion: corte de calle en el centro
 *
 * Si falta el archivo se usa un corpus mínimo; una línea fuera de sección se
 * saltea y se anota un ErrorCarga.
 */
public final class ArchivoCorpus {

    private ArchivoCorpus() {
    }

    public static Corpus cargar(String ruta, Lista<ErrorCarga> errores) {
        if (!Lector.existe(ruta)) {
            errores.agregar(new ErrorCarga(ruta, 0, "no existe, se usa un corpus mínimo"));
            return corpusPorDefecto();
        }
        Lista<String> lineas;
        try {
            lineas = Lector.leerLineas(ruta);
        } catch (IOException e) {
            errores.agregar(new ErrorCarga(ruta, 0, "no se pudo leer (" + e.getMessage() + "), se usa un corpus mínimo"));
            return corpusPorDefecto();
        }

        Corpus corpus = new Corpus();
        Lista<String> seccion = null;
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i).trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            if (linea.equals("[NOMBRES]")) {
                seccion = corpus.getNombres();
            } else if (linea.equals("[RECLAMOS]")) {
                seccion = corpus.getReclamos();
            } else if (linea.equals("[RADIO]")) {
                seccion = corpus.getRadio();
            } else if (seccion == null) {
                errores.agregar(new ErrorCarga(ruta, i + 1, "línea fuera de una sección: " + linea));
            } else {
                seccion.agregar(linea);
            }
        }
        // Si una sección quedó vacía el modelo no tendría de qué aprender
        Corpus minimo = corpusPorDefecto();
        if (corpus.getNombres().estaVacia()) {
            errores.agregar(new ErrorCarga(ruta, 0, "sección [NOMBRES] vacía, se usan nombres mínimos"));
            copiar(minimo.getNombres(), corpus.getNombres());
        }
        if (corpus.getReclamos().estaVacia()) {
            errores.agregar(new ErrorCarga(ruta, 0, "sección [RECLAMOS] vacía, se usan reclamos mínimos"));
            copiar(minimo.getReclamos(), corpus.getReclamos());
        }
        if (corpus.getRadio().estaVacia()) {
            errores.agregar(new ErrorCarga(ruta, 0, "sección [RADIO] vacía, se usan avisos mínimos"));
            copiar(minimo.getRadio(), corpus.getRadio());
        }
        return corpus;
    }

    private static void copiar(Lista<String> origen, Lista<String> destino) {
        for (int i = 0; i < origen.tamanio(); i++) {
            destino.agregar(origen.obtener(i));
        }
    }

    public static Corpus corpusPorDefecto() {
        Corpus corpus = new Corpus();
        Lista<String> nombres = corpus.getNombres();
        nombres.agregar("Lucia Gomez");
        nombres.agregar("Mateo Benitez");
        nombres.agregar("Camila Acosta");
        nombres.agregar("Joaquin Romero");
        nombres.agregar("Valentina Silva");
        nombres.agregar("Tomas Fernandez");
        nombres.agregar("Sofia Duarte");
        nombres.agregar("Martin Ledesma");
        Lista<String> reclamos = corpus.getReclamos();
        reclamos.agregar("Donde esta mi empanada");
        reclamos.agregar("Llego frio el pedido");
        reclamos.agregar("Tardaron demasiado");
        reclamos.agregar("Quiero hablar con el gerente");
        reclamos.agregar("Yo pedi sin cebolla");
        Lista<String> radio = corpus.getRadio();
        radio.agregar("Atencion: lluvia fuerte en el centro");
        radio.agregar("Trafico lento sobre la avenida");
        radio.agregar("Hoy se juega el clasico del barrio");
        radio.agregar("Cuidado con las calles anegadas");
        return corpus;
    }
}
