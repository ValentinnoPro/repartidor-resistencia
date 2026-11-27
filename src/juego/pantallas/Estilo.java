package juego.pantallas;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import ui.Texto;

/** Paleta y utilidades de dibujo compartidas por todas las pantallas. */
final class Estilo {

    // Paleta
    static final Color FONDO_ARRIBA = new Color(18, 24, 42);
    static final Color FONDO_ABAJO = new Color(10, 13, 24);
    static final Color PANEL = new Color(27, 34, 54);
    static final Color PANEL_CLARO = new Color(38, 47, 74);
    static final Color BORDE = new Color(62, 74, 110);
    static final Color TEXTO = new Color(238, 242, 255);
    static final Color TEXTO_SUAVE = new Color(150, 160, 190);
    static final Color DORADO = new Color(255, 196, 72);
    static final Color VERDE = new Color(96, 224, 150);
    static final Color ROJO = new Color(246, 88, 88);
    static final Color AZUL = new Color(96, 172, 255);
    static final Color NARANJA = new Color(255, 146, 60);

    private Estilo() {
    }

    static Color alfa(Color color, int alfa) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alfa)));
    }

    static Color mezclar(Color a, Color b, double t) {
        double k = Math.max(0, Math.min(1, t));
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * k));
    }

    /** Oscila entre 0 y 1 con la frecuencia dada (ciclos por segundo). */
    static double pulso(double tiempo, double frecuencia) {
        return 0.5 + 0.5 * Math.sin(tiempo * frecuencia * Math.PI * 2);
    }

    static void fondo(Graphics2D g, int ancho, int alto) {
        g.setPaint(new GradientPaint(0, 0, FONDO_ARRIBA, 0, alto, FONDO_ABAJO));
        g.fillRect(0, 0, ancho, alto);
    }

    /** Panel redondeado con sombra suave. */
    static void panel(Graphics2D g, double x, double y, double ancho, double alto, int radio, Color relleno, Color borde) {
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRoundRect((int) x + 2, (int) y + 4, (int) ancho, (int) alto, radio, radio);
        g.setColor(relleno);
        g.fillRoundRect((int) x, (int) y, (int) ancho, (int) alto, radio, radio);
        if (borde != null) {
            g.setColor(borde);
            g.drawRoundRect((int) x, (int) y, (int) ancho, (int) alto, radio, radio);
        }
    }

    // Texto con sombra: se lee bien sobre cualquier fondo
    static void sombreado(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        Texto.izquierda(g, texto, x + 1, y + 2, tamanio, new Color(0, 0, 0, 140));
        Texto.izquierda(g, texto, x, y, tamanio, color);
    }

    static void sombreadoCentrado(Graphics2D g, String texto, double x, double y, int tamanio, Color color) {
        Texto.centrado(g, texto, x + 1, y + 2, tamanio, new Color(0, 0, 0, 140));
        Texto.centrado(g, texto, x, y, tamanio, color);
    }

    static String recortar(String texto, int largo) {
        return texto.length() <= largo ? texto : texto.substring(0, largo - 1) + "…";
    }

    /** Estrella de cinco puntas centrada en (x, y). */
    static void estrella(Graphics2D g, double x, double y, double radio, Color color) {
        java.awt.Polygon p = new java.awt.Polygon();
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? radio : radio * 0.45;
            double a = -Math.PI / 2 + i * Math.PI / 5;
            p.addPoint((int) Math.round(x + Math.cos(a) * r), (int) Math.round(y + Math.sin(a) * r));
        }
        g.setColor(color);
        g.fillPolygon(p);
    }
}
