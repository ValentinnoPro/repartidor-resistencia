package juego.pantallas;

/** Utilidades de formato para los textos de pantalla. */
final class Formato {

    private Formato() {
    }

    /** Minutos de juego (con decimales) como "mm:ss". */
    static String reloj(double minutos) {
        int segundos = (int) Math.round(Math.max(0, minutos) * 60);
        int m = segundos / 60;
        int s = segundos % 60;
        return (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    static String mayuscula(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
