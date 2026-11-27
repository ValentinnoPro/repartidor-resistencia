package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import motor.Entrada;
import ui.Texto;

/** Botón rectangular del mouse: se resalta al pasar, se hunde al apretar y puede mostrar su atajo de teclado. */
class Boton {

    private final int x;
    private final int y;
    private final int ancho;
    private final int alto;
    private final String atajo;
    private String texto;
    private Color color;
    private boolean habilitado = true;

    Boton(int x, int y, int ancho, int alto, String texto, String atajo, Color color) {
        if (ancho <= 0 || alto <= 0) {
            throw new IllegalArgumentException("el botón necesita tamaño positivo");
        }
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
        this.texto = texto == null ? "" : texto;
        this.atajo = atajo == null ? "" : atajo;
        this.color = color == null ? Estilo.AZUL : color;
    }

    boolean contiene(int mx, int my) {
        return mx >= x && mx < x + ancho && my >= y && my < y + alto;
    }

    /** True en el paso en que se hizo click sobre un botón habilitado. */
    boolean fuePulsado(Entrada entrada) {
        return habilitado && entrada.huboClick() && contiene(entrada.getMouseX(), entrada.getMouseY());
    }

    void dibujar(Graphics2D g, Entrada entrada) {
        boolean encima = habilitado && contiene(entrada.getMouseX(), entrada.getMouseY());
        boolean hundido = encima && entrada.mouseAbajo();
        int desplazamiento = hundido ? 2 : 0;
        Color base = habilitado ? color : new Color(70, 76, 98);
        Color relleno = encima ? Estilo.mezclar(base, Color.WHITE, 0.18) : base;

        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(x, y + 3, ancho, alto, 12, 12);
        g.setColor(relleno);
        g.fillRoundRect(x, y + desplazamiento, ancho, alto, 12, 12);
        g.setColor(new Color(255, 255, 255, encima ? 90 : 40));
        g.drawRoundRect(x, y + desplazamiento, ancho, alto, 12, 12);

        Color letra = habilitado ? Estilo.mezclar(base, Color.BLACK, 0.78) : new Color(130, 136, 160);
        if (habilitado && base.getRed() + base.getGreen() + base.getBlue() < 300) {
            letra = Color.WHITE;
        }
        int tamanio = alto >= 40 ? 18 : 13;
        double centroY = y + desplazamiento + alto / 2.0 + tamanio * 0.36;
        if (atajo.isEmpty()) {
            Texto.centrado(g, texto, x + ancho / 2.0, centroY, tamanio, letra);
        } else {
            // La tecla se dibuja en una chapita del ancho justo de su texto
            int chico = tamanio - 3;
            g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, chico));
            int chapa = g.getFontMetrics().stringWidth(atajo) + 12;
            int alturaChapa = tamanio + 6;
            double chapaX = x + ancho - chapa - 8;
            Texto.centrado(g, texto, (x + chapaX) / 2.0, centroY, tamanio, letra);
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRoundRect((int) chapaX, y + desplazamiento + (alto - alturaChapa) / 2, chapa, alturaChapa, 6, 6);
            Texto.centrado(g, atajo, chapaX + chapa / 2.0, centroY - 1, chico, letra);
        }
    }

    void setTexto(String texto) {
        this.texto = texto == null ? "" : texto;
    }

    void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    void setColor(Color color) {
        if (color != null) {
            this.color = color;
        }
    }
}
