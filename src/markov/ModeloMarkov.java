package markov;

import arboles.ABB;
import estructuras.Lista;
import java.util.Random;

/**
 * Modelo de Markov de orden k sobre caracteres.
 *
 * Entrenamiento: a cada línea se le antepone k marcadores de inicio ('^') y se
 * le agrega un marcador de fin ('$'). Para cada posición se registra
 * "los k caracteres anteriores -> carácter siguiente".
 *
 * La tabla de transiciones es un ABB de {@link EntradaMarkov} ordenado por
 * estado. Un modelo de orden k > 1 tiene dentro un modelo de orden k-1
 * (y ese uno de k-2...) que se entrena a la vez: lo usa la estrategia
 * {@link RetrocesoDeContexto} cuando un estado no tiene sucesores.
 *
 * Costos: entrenar N caracteres O(N log S) (S = estados); generar L
 * caracteres O(L (log S + grado)).
 */
public class ModeloMarkov {

    public static final char INICIO = '^';
    public static final char FIN = '$';
    private static final int INTENTOS = 10;

    // Atributos
    private final int orden;
    private final ABB<EntradaMarkov> tabla = new ABB<>();
    private final ModeloMarkov menor;
    private final EstrategiaSinSucesores estrategia;
    private String valorPorDefecto = "Sin nombre";
    private String advertencia = "";

    // Constructor
    public ModeloMarkov(int orden, EstrategiaSinSucesores estrategia) {
        if (orden < 1 || orden > 6) {
            throw new IllegalArgumentException("el orden debe estar entre 1 y 6: " + orden);
        }
        if (estrategia == null) {
            throw new IllegalArgumentException("falta la estrategia para estados sin sucesores");
        }
        this.orden = orden;
        this.estrategia = estrategia;
        this.menor = orden > 1 ? new ModeloMarkov(orden - 1, estrategia) : null;
    }

    // Configuración y consultas
    public int getOrden() {
        return orden;
    }

    public ModeloMarkov getMenor() {
        return menor;
    }

    public void setValorPorDefecto(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("el valor por defecto no puede ser vacío");
        }
        this.valorPorDefecto = valor;
    }

    /** Aviso de la última generación (corte por largo, estado sin sucesores...). Vacío si todo fue normal. */
    public String getAdvertencia() {
        return advertencia;
    }

    public int cantidadEstados() {
        return tabla.cantidadNodos();
    }

    // Entrenamiento
    public void entrenar(Lista<String> lineas) {
        for (int i = 0; i < lineas.tamanio(); i++) {
            String linea = lineas.obtener(i).trim();
            if (!linea.isEmpty()) {
                entrenarLinea(linea);
            }
        }
    }

    private void entrenarLinea(String linea) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < orden; i++) {
            texto.append(INICIO);
        }
        texto.append(linea).append(FIN);
        String s = texto.toString();
        for (int i = orden; i < s.length(); i++) {
            String estado = s.substring(i - orden, i);
            EntradaMarkov nueva = new EntradaMarkov(estado);
            EntradaMarkov existente = tabla.buscar(nueva);
            if (existente == null) {
                nueva.agregarSucesor(s.charAt(i));
                tabla.insertar(nueva);
            } else {
                existente.agregarSucesor(s.charAt(i));
            }
        }
        if (menor != null) {
            menor.entrenarLinea(linea);
        }
    }

    // Generación
    /** Sortea un sucesor del estado, o devuelve null si el estado no está en la tabla. */
    public Character sucesorDe(String estado, Random azar) {
        EntradaMarkov entrada = tabla.buscar(new EntradaMarkov(estado));
        if (entrada == null) {
            return null;
        }
        Lista<Character> sucesores = entrada.getSucesores();
        return sucesores.obtener(azar.nextInt(sucesores.tamanio()));
    }

    /**
     * Genera una cadena de hasta {@code largoMax} caracteres. Nunca devuelve
     * vacío: reintenta hasta 10 veces y, si no, usa el valor por defecto.
     */
    public String generar(Random azar, int largoMax) {
        if (largoMax < 1) {
            throw new IllegalArgumentException("el largo máximo debe ser positivo");
        }
        for (int intento = 0; intento < INTENTOS; intento++) {
            String cadena = generarUna(azar, largoMax);
            if (!cadena.trim().isEmpty()) {
                return cadena;
            }
        }
        advertencia = "salida vacía: se usó el valor por defecto";
        return valorPorDefecto;
    }

    private String generarUna(Random azar, int largoMax) {
        advertencia = "";
        StringBuilder estado = new StringBuilder();
        for (int i = 0; i < orden; i++) {
            estado.append(INICIO);
        }
        StringBuilder salida = new StringBuilder();
        boolean terminada = false;
        while (salida.length() < largoMax) {
            Character siguiente = sucesorDe(estado.toString(), azar);
            if (siguiente == null) {
                siguiente = estrategia.resolver(this, estado.toString(), azar);
                advertencia = "estado sin sucesores (" + estrategia.descripcion() + ")";
            }
            if (siguiente == FIN) {
                terminada = true;
                break;
            }
            salida.append(siguiente.charValue());
            estado.deleteCharAt(0).append(siguiente.charValue());
        }
        if (!terminada) {
            int ultimoEspacio = salida.lastIndexOf(" ");
            if (ultimoEspacio > 0) {
                salida.setLength(ultimoEspacio);
            }
            salida.append("...");
            advertencia = "cortada por largo máximo";
        }
        return salida.toString();
    }
}
