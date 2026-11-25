package persistencia;

import arboles.ArbolBinario;
import arboles.NodoBinario;
import estructuras.Lista;
import estructuras.ListaEnlazada;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import juego.reglas.ArchivoDanadoException;
import juego.reglas.Mejora;
import juego.reglas.Taller;

/**
 * Lee y guarda el taller (datos/mejoras.txt).
 *
 * FORMATO: el árbol de mejoras en PRE-ORDEN, una línea por nodo, con una línea
 * que solo dice # para cada hijo vacío. Opcionalmente la línea @dinero;N
 * guarda la plata ahorrada. Las demás líneas que empiezan con # son comentarios.
 *
 *   # nombre;costo;factorVelocidad;comprada
 *   @dinero;350
 *   Base;0;1.0;true
 *   Motor;200;0.9;false
 *   ...
 *   #
 *
 * Se reconstruye recursivamente: si la línea es #, el subárbol es vacío; si
 * no, se crea el nodo y después se arma el subárbol izquierdo y el derecho.
 * Una línea dañada se reporta y se toma como nodo vacío (sus dos subárboles
 * igual se leen, para no desalinear el resto).
 */
public final class ArchivoMejoras {

    private ArchivoMejoras() {
    }

    // Posición de lectura compartida entre las llamadas recursivas
    private static class Cursor {
        private int posicion;
    }

    public static Taller cargar(String ruta, Lista<ErrorCarga> errores) {
        if (!Lector.existe(ruta)) {
            errores.agregar(new ErrorCarga(ruta, 0, "no existe, se usa el árbol de mejoras por defecto"));
            return Taller.porDefecto();
        }
        Lista<String> crudas;
        try {
            crudas = Lector.leerLineas(ruta);
        } catch (IOException e) {
            errores.agregar(new ErrorCarga(ruta, 0, "no se pudo leer (" + e.getMessage() + "), se usa el árbol por defecto"));
            return Taller.porDefecto();
        }

        // Se separan las líneas de datos (con su número original)
        Lista<String> datos = new ListaEnlazada<>();
        Lista<Integer> numeros = new ListaEnlazada<>();
        int dinero = 0;
        for (int i = 0; i < crudas.tamanio(); i++) {
            String linea = crudas.obtener(i).trim();
            if (linea.isEmpty() || (linea.startsWith("#") && !linea.equals("#"))) {
                continue;
            }
            if (linea.startsWith("@dinero;")) {
                try {
                    dinero = Math.max(0, Integer.parseInt(linea.substring(8).trim()));
                } catch (NumberFormatException e) {
                    errores.agregar(new ErrorCarga(ruta, i + 1, "dinero inválido: " + linea));
                }
                continue;
            }
            datos.agregar(linea);
            numeros.agregar(i + 1);
        }

        NodoBinario<Mejora> raiz = construir(datos, numeros, new Cursor(), ruta, errores);
        if (raiz == null) {
            errores.agregar(new ErrorCarga(ruta, 0, "no tiene un árbol válido, se usa el árbol por defecto"));
            Taller porDefecto = Taller.porDefecto();
            porDefecto.depositar(dinero);
            return porDefecto;
        }
        ArbolBinario<Mejora> arbol = new ArbolBinario<>();
        arbol.setRaiz(raiz);
        return new Taller(arbol, dinero);
    }

    // Reconstrucción recursiva en pre-orden
    private static NodoBinario<Mejora> construir(Lista<String> datos, Lista<Integer> numeros, Cursor cursor,
                                                 String ruta, Lista<ErrorCarga> errores) {
        if (cursor.posicion >= datos.tamanio()) {
            return null; // caso base: se acabaron las líneas
        }
        String linea = datos.obtener(cursor.posicion);
        int numero = numeros.obtener(cursor.posicion);
        cursor.posicion++;
        if (linea.equals("#")) {
            return null; // caso base: subárbol vacío
        }
        Mejora mejora = null;
        try {
            mejora = interpretar(linea);
        } catch (ArchivoDanadoException | IllegalArgumentException e) {
            errores.agregar(new ErrorCarga(ruta, numero, e.getMessage()));
        }
        NodoBinario<Mejora> izquierdo = construir(datos, numeros, cursor, ruta, errores);
        NodoBinario<Mejora> derecho = construir(datos, numeros, cursor, ruta, errores);
        if (mejora == null) {
            return null;
        }
        NodoBinario<Mejora> nodo = new NodoBinario<>(mejora);
        nodo.setIzquierdo(izquierdo);
        nodo.setDerecho(derecho);
        return nodo;
    }

    private static Mejora interpretar(String linea) throws ArchivoDanadoException {
        String[] partes = linea.split(";");
        if (partes.length != 4) {
            throw new ArchivoDanadoException("se esperaban 4 campos: " + linea);
        }
        try {
            return new Mejora(partes[0], Integer.parseInt(partes[1].trim()),
                    Double.parseDouble(partes[2].trim()), Boolean.parseBoolean(partes[3].trim()));
        } catch (NumberFormatException e) {
            throw new ArchivoDanadoException("número inválido: " + linea);
        }
    }

    /** Guarda sin propagar IOException: si falla, anota un ErrorCarga y devuelve false. */
    public static boolean guardar(String ruta, Taller taller, Lista<ErrorCarga> errores) {
        try {
            guardar(ruta, taller);
            return true;
        } catch (IOException e) {
            errores.agregar(new ErrorCarga(ruta, 0, "no se pudo guardar: " + e.getMessage()));
            return false;
        }
    }

    // Guardado (con copia de seguridad previa)
    public static void guardar(String ruta, Taller taller) throws IOException {
        Respaldo.asegurarCarpeta(ruta);
        Respaldo.copiar(ruta);
        try (BufferedWriter salida = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ruta), "UTF-8"))) {
            salida.write("# nombre;costo;factorVelocidad;comprada  (pre-orden, # = vacío)");
            salida.newLine();
            salida.write("@dinero;" + taller.getDinero());
            salida.newLine();
            escribir(salida, taller.getArbol().getRaiz());
        }
    }

    private static void escribir(BufferedWriter salida, NodoBinario<Mejora> nodo) throws IOException {
        if (nodo == null) {
            salida.write("#");
            salida.newLine();
            return;
        }
        Mejora m = nodo.getValor();
        salida.write(m.getNombre() + ";" + m.getCosto() + ";" + m.getFactorVelocidad() + ";" + m.isComprada());
        salida.newLine();
        escribir(salida, nodo.getIzquierdo());
        escribir(salida, nodo.getDerecho());
    }
}
