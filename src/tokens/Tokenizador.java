package tokens;

import estructuras.Lista;
import estructuras.ListaEnlazada;

/**
 * Convierte un texto escrito por el jugador ("Sarmiento y 9 de Julio") en una
 * Lista de tokens, usando la coincidencia más larga contra un vocabulario
 * guardado en un Trie.
 *
 * - Se normaliza antes (minúsculas, sin tildes ni signos).
 * - "y", "esq", "esquina" y "con" son separadores y no generan token.
 * - Si en una posición ninguna palabra del vocabulario coincide, se genera un
 *   token desconocido (empieza con {@link #PREFIJO_DESCONOCIDO}) que cubre
 *   hasta el próximo espacio, y se sigue desde ahí.
 */
public class Tokenizador {

    public static final String PREFIJO_DESCONOCIDO = "?";

    private final Trie vocabulario = new Trie();

    /** Agrega una palabra o frase (por ejemplo "9 de julio") al vocabulario. */
    public void agregarAlVocabulario(String palabra) {
        String normalizada = normalizar(palabra).trim();
        if (!normalizada.isEmpty()) {
            vocabulario.insertar(normalizada);
        }
    }

    public Lista<String> tokenizar(String texto) {
        Lista<String> tokens = new ListaEnlazada<>();
        String t = normalizar(texto);
        int i = 0;
        while (i < t.length()) {
            if (t.charAt(i) == ' ') {
                i++;
                continue;
            }
            int finPalabra = i;
            while (finPalabra < t.length() && t.charAt(finPalabra) != ' ') {
                finPalabra++;
            }
            String palabra = t.substring(i, finPalabra);
            if (esSeparador(palabra)) {
                i = finPalabra;
                continue;
            }
            int largo = vocabulario.coincidenciaMasLarga(t, i);
            if (largo > 0) {
                tokens.agregar(t.substring(i, i + largo));
                i += largo;
            } else {
                tokens.agregar(PREFIJO_DESCONOCIDO + palabra);
                i = finPalabra;
            }
        }
        return tokens;
    }

    /**
     * Sugerencias para autocompletar: toma lo que se está escribiendo después
     * del último separador " y " y busca en el Trie las calles que empiezan así.
     */
    public Lista<String> sugerencias(String texto) {
        String t = normalizar(texto);
        int corte = t.lastIndexOf(" y ");
        String fragmento = corte >= 0 ? t.substring(corte + 3) : t;
        int inicio = 0;
        while (inicio < fragmento.length() && fragmento.charAt(inicio) == ' ') {
            inicio++;
        }
        fragmento = fragmento.substring(inicio);
        if (fragmento.isEmpty()) {
            return new ListaEnlazada<>();
        }
        return vocabulario.buscarPorPrefijo(fragmento);
    }

    private boolean esSeparador(String palabra) {
        return palabra.equals("y") || palabra.equals("esq") || palabra.equals("esquina") || palabra.equals("con");
    }

    /** Minúsculas, sin tildes, y todo lo que no sea letra o dígito pasa a espacio. */
    public static String normalizar(String texto) {
        StringBuilder sb = new StringBuilder();
        String minuscula = texto.toLowerCase();
        for (int i = 0; i < minuscula.length(); i++) {
            char c = minuscula.charAt(i);
            switch (c) {
                case 'á': c = 'a'; break;
                case 'é': c = 'e'; break;
                case 'í': c = 'i'; break;
                case 'ó': c = 'o'; break;
                case 'ú':
                case 'ü': c = 'u'; break;
                case 'ñ': c = 'n'; break;
                default: break;
            }
            sb.append(Character.isLetterOrDigit(c) ? c : ' ');
        }
        return sb.toString();
    }
}
