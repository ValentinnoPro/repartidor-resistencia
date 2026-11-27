package juego.pantallas;

import estructuras.Lista;
import estructuras.ListaEnlazada;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import ui.Texto;

/**
 * Efectos de pantalla de vida corta: textos que flotan (+puntos), confeti y
 * polvo. Se guardan en una Lista propia y se descartan al terminar su vida.
 */
class Efectos {

    private static final int MAXIMO = 160;

    private static class Particula {
        private double x;
        private double y;
        private final double vx;
        private final double vy;
        private final double gravedad;
        private final double vidaTotal;
        private double vida;
        private final double tamanio;
        private final Color color;
        private final String texto;

        private Particula(double x, double y, double vx, double vy, double gravedad, double vida, double tamanio,
                          Color color, String texto) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.gravedad = gravedad;
            this.vidaTotal = vida;
            this.vida = vida;
            this.tamanio = tamanio;
            this.color = color;
            this.texto = texto;
        }
    }

    private final Lista<Particula> particulas = new ListaEnlazada<>();
    private final Random azar = new Random(11);

    /** Texto que sube y se desvanece. */
    void texto(double x, double y, String texto, Color color) {
        agregar(new Particula(x, y, 0, -38, 0, 1.6, 20, color, texto));
    }

    /** Explosión de confeti. */
    void confeti(double x, double y, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            double angulo = azar.nextDouble() * Math.PI * 2;
            double fuerza = 60 + azar.nextDouble() * 150;
            agregar(new Particula(x, y, Math.cos(angulo) * fuerza, Math.sin(angulo) * fuerza - 60, 260,
                    0.9 + azar.nextDouble() * 0.7, 3 + azar.nextDouble() * 3, colorAlAzar(), null));
        }
    }

    // Sin arreglos: el color del confeti se elige con un switch
    private Color colorAlAzar() {
        switch (azar.nextInt(5)) {
            case 0:
                return Estilo.DORADO;
            case 1:
                return Estilo.VERDE;
            case 2:
                return Estilo.AZUL;
            case 3:
                return Estilo.ROJO;
            default:
                return Color.WHITE;
        }
    }

    /** Nubecita de polvo detrás del vehículo. */
    void polvo(double x, double y, Color color) {
        agregar(new Particula(x + azar.nextDouble() * 4 - 2, y + azar.nextDouble() * 4 - 2,
                azar.nextDouble() * 10 - 5, azar.nextDouble() * 10 - 5, 0, 0.5, 4 + azar.nextDouble() * 3, color, null));
    }

    private void agregar(Particula p) {
        if (particulas.tamanio() >= MAXIMO) {
            particulas.eliminar(0);
        }
        particulas.agregar(p);
    }

    void actualizar(double dt) {
        for (int i = particulas.tamanio() - 1; i >= 0; i--) {
            Particula p = particulas.obtener(i);
            p.vida -= dt;
            if (p.vida <= 0) {
                particulas.eliminar(i);
                continue;
            }
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if (p.gravedad != 0) {
                // La gravedad solo cambia la caída: se integra con la edad de la partícula
                p.y += p.gravedad * (p.vidaTotal - p.vida) * dt;
            }
        }
    }

    void dibujar(Graphics2D g) {
        for (int i = 0; i < particulas.tamanio(); i++) {
            Particula p = particulas.obtener(i);
            double restante = Math.max(0, p.vida / p.vidaTotal);
            int alfa = (int) (255 * Math.min(1.0, restante * 1.6));
            if (p.texto != null) {
                Texto.centrado(g, p.texto, p.x + 1, p.y + 2, (int) p.tamanio, new Color(0, 0, 0, alfa / 2));
                Texto.centrado(g, p.texto, p.x, p.y, (int) p.tamanio, Estilo.alfa(p.color, alfa));
            } else {
                g.setColor(Estilo.alfa(p.color, alfa));
                double t = p.tamanio * (p.gravedad == 0 ? restante : 1);
                g.fillRect((int) (p.x - t / 2), (int) (p.y - t / 2), (int) Math.max(1, t), (int) Math.max(1, t));
            }
        }
    }
}
