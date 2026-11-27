package juego.pantallas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Dibuja al cadete vista desde arriba, mirando hacia donde avanza. Usa el
 * mismo índice de vehículo que la Sesión (0 bici, 1 moto, 2 monopatín), así el
 * mapa y el menú comparten los dibujos.
 */
final class DibujoVehiculo {

    private static final Color CAJA = new Color(255, 140, 50);
    private static final Color CAJA_BORDE = new Color(190, 90, 20);
    private static final Color RUEDA = new Color(24, 26, 34);

    private DibujoVehiculo() {
    }

    /**
     * @param angulo   dirección en radianes (0 = hacia la derecha)
     * @param escala   1 = tamaño del mapa
     * @param tiempo   reloj de animación en segundos
     * @param moviendo si es true hay rebote y se enciende el faro
     */
    static void dibujar(Graphics2D g, int tipo, double x, double y, double angulo, double escala, double tiempo,
                        boolean moviendo) {
        AffineTransform original = g.getTransform();
        g.translate(x, y);
        g.scale(escala, escala);
        g.rotate(angulo);
        double rebote = moviendo ? Math.sin(tiempo * 26) * 0.7 : 0;

        // Sombra
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(-17, -8, 36, 18);

        switch (tipo) {
            case 1:
                moto(g, rebote, moviendo);
                break;
            case 2:
                monopatin(g, rebote);
                break;
            default:
                bicicleta(g, rebote);
                break;
        }
        g.setTransform(original);
    }

    private static void rueda(Graphics2D g, double cx, double largo, double ancho) {
        g.setColor(RUEDA);
        g.fillRoundRect((int) (cx - largo / 2), (int) (-ancho / 2), (int) largo, (int) ancho, 3, 3);
        g.setColor(new Color(120, 126, 150));
        g.drawLine((int) (cx - largo / 2 + 2), 0, (int) (cx + largo / 2 - 2), 0);
    }

    private static void caja(Graphics2D g, double cx, double lado) {
        g.setColor(CAJA);
        g.fillRoundRect((int) (cx - lado / 2), (int) (-lado / 2), (int) lado, (int) lado, 4, 4);
        g.setColor(CAJA_BORDE);
        g.drawRoundRect((int) (cx - lado / 2), (int) (-lado / 2), (int) lado, (int) lado, 4, 4);
        g.drawLine((int) (cx - lado / 2 + 2), 0, (int) (cx + lado / 2 - 2), 0);
    }

    private static void piloto(Graphics2D g, double cx, double rebote, Color campera, Color casco) {
        g.setColor(campera);
        g.fillOval((int) (cx - 6), (int) (-7 + rebote), 13, 15);
        g.setColor(Estilo.mezclar(campera, Color.BLACK, 0.35));
        g.drawOval((int) (cx - 6), (int) (-7 + rebote), 13, 15);
        g.setColor(casco);
        g.fillOval((int) (cx - 1), (int) (-5 + rebote), 10, 10);
        g.setColor(new Color(20, 24, 34));
        g.fillRoundRect((int) (cx + 5), (int) (-3 + rebote), 4, 6, 3, 3);
    }

    private static void bicicleta(Graphics2D g, double rebote) {
        rueda(g, -12, 12, 4);
        rueda(g, 12, 12, 4);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(200, 205, 225));
        g.drawLine(-12, 0, 12, 0);
        g.drawLine(9, -7, 9, 7);
        g.setStroke(new BasicStroke(1f));
        caja(g, -15, 13);
        piloto(g, -1, rebote, Estilo.AZUL, Estilo.DORADO);
    }

    private static void moto(Graphics2D g, double rebote, boolean moviendo) {
        if (moviendo) {
            // Haz del faro
            g.setColor(new Color(255, 240, 160, 50));
            java.awt.Polygon haz = new java.awt.Polygon();
            haz.addPoint(17, -2);
            haz.addPoint(46, -13);
            haz.addPoint(46, 13);
            haz.addPoint(17, 2);
            g.fillPolygon(haz);
        }
        rueda(g, -14, 10, 5);
        rueda(g, 14, 10, 5);
        g.setColor(new Color(224, 58, 66));
        g.fillRoundRect(-13, -4, 28, 8, 8, 8);
        g.setColor(new Color(130, 30, 38));
        g.drawRoundRect(-13, -4, 28, 8, 8, 8);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(190, 195, 215));
        g.drawLine(12, -8, 12, 8);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(255, 236, 150));
        g.fillOval(15, -3, 5, 6);
        caja(g, -18, 15);
        piloto(g, -3, rebote, new Color(50, 56, 80), Color.WHITE);
    }

    private static void monopatin(Graphics2D g, double rebote) {
        g.setColor(RUEDA);
        g.fillOval(-12, -6, 5, 5);
        g.fillOval(-12, 1, 5, 5);
        g.fillOval(8, -6, 5, 5);
        g.fillOval(8, 1, 5, 5);
        g.setColor(new Color(60, 200, 170));
        g.fillRoundRect(-13, -4, 27, 8, 8, 8);
        g.setColor(new Color(20, 110, 96));
        g.drawRoundRect(-13, -4, 27, 8, 8, 8);
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(200, 205, 225));
        g.drawLine(11, -7, 11, 7);
        g.setStroke(new BasicStroke(1f));
        caja(g, -9, 12);
        piloto(g, 0, rebote, new Color(150, 110, 230), Estilo.DORADO);
    }
}
