package persistencia;

import estructuras.Lista;
import grafos.Grafo;
import java.io.IOException;
import juego.reglas.ArchivoDanadoException;
import juego.reglas.Esquina;

/**
 * Lee el mapa de calles (datos/mapa.txt). Solo lectura.
 *
 * FORMATO, una línea por vértice y una por arista (las líneas que empiezan
 * con # son comentarios; los vértices van antes que las aristas):
 *
 *   V;id;calle1;calle2;x;y          ej.  V;1;belgrano;french;90;90
 *   A;origen;destino;minutos        ej.  A;1;2;0.6
 *
 * Una calle de doble mano son dos líneas A (ida y vuelta); una de una mano,
 * una sola. La PRIMERA esquina del archivo es el local (donde arranca el turno).
 *
 * Si el archivo no existe se usa un mapa mínimo; si una línea está dañada se
 * saltea y se anota un ErrorCarga.
 */
public final class ArchivoMapa {

    private ArchivoMapa() {
    }

    public static Grafo<Esquina> cargar(String ruta, Lista<ErrorCarga> errores) {
        if (!Lector.existe(ruta)) {
            errores.agregar(new ErrorCarga(ruta, 0, "no existe, se usa el mapa por defecto"));
            return mapaPorDefecto();
        }
        Lista<String> lineas;
        try {
            lineas = Lector.leerLineas(ruta);
        } catch (IOException e) {
            errores.agregar(new ErrorCarga(ruta, 0, "no se pudo leer (" + e.getMessage() + "), se usa el mapa por defecto"));
            return mapaPorDefecto();
        }

        Grafo<Esquina> grafo = new Grafo<>();
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i).trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            try {
                interpretar(grafo, linea);
            } catch (ArchivoDanadoException | IllegalArgumentException e) {
                errores.agregar(new ErrorCarga(ruta, i + 1, e.getMessage()));
            }
        }
        if (grafo.cantidadVertices() == 0) {
            errores.agregar(new ErrorCarga(ruta, 0, "no tiene esquinas válidas, se usa el mapa por defecto"));
            return mapaPorDefecto();
        }
        return grafo;
    }

    private static void interpretar(Grafo<Esquina> grafo, String linea) throws ArchivoDanadoException {
        String[] partes = linea.split(";");
        if (partes[0].equals("V") && partes.length == 6) {
            grafo.agregarVertice(new Esquina(entero(partes[1]), partes[2], partes[3], entero(partes[4]), entero(partes[5])));
        } else if (partes[0].equals("A") && partes.length == 4) {
            Esquina origen = buscar(grafo, entero(partes[1]));
            Esquina destino = buscar(grafo, entero(partes[2]));
            grafo.agregarArista(origen, destino, decimal(partes[3]));
        } else {
            throw new ArchivoDanadoException("formato desconocido: " + linea);
        }
    }

    private static Esquina buscar(Grafo<Esquina> grafo, int id) throws ArchivoDanadoException {
        Lista<Esquina> esquinas = grafo.vertices();
        for (int i = 0; i < esquinas.tamanio(); i++) {
            if (esquinas.obtener(i).getId() == id) {
                return esquinas.obtener(i);
            }
        }
        throw new ArchivoDanadoException("la esquina " + id + " no está definida");
    }

    private static int entero(String texto) throws ArchivoDanadoException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ArchivoDanadoException("no es un entero: " + texto);
        }
    }

    private static double decimal(String texto) throws ArchivoDanadoException {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            throw new ArchivoDanadoException("no es un número: " + texto);
        }
    }

    /** Mapa mínimo (grilla de 2 x 3, todas de doble mano) para poder jugar sin archivo. */
    public static Grafo<Esquina> mapaPorDefecto() {
        Grafo<Esquina> grafo = new Grafo<>();
        int id = 1;
        for (int f = 0; f < 2; f++) {
            for (int c = 0; c < 3; c++) {
                grafo.agregarVertice(new Esquina(id++, f == 0 ? "belgrano" : "sarmiento",
                        c == 0 ? "french" : (c == 1 ? "ayacucho" : "rawson"), 150 + c * 220, 150 + f * 220));
            }
        }
        Lista<Esquina> v = grafo.vertices();
        for (int f = 0; f < 2; f++) {
            for (int c = 0; c < 3; c++) {
                Esquina e = v.obtener(f * 3 + c);
                if (c < 2) {
                    unir(grafo, e, v.obtener(f * 3 + c + 1));
                }
                if (f < 1) {
                    unir(grafo, e, v.obtener((f + 1) * 3 + c));
                }
            }
        }
        return grafo;
    }

    private static void unir(Grafo<Esquina> grafo, Esquina a, Esquina b) {
        grafo.agregarArista(a, b, 0.6);
        grafo.agregarArista(b, a, 0.6);
    }
}
