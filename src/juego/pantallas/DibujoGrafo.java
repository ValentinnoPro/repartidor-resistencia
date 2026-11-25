package juego.pantallas;

import estructuras.Lista;
import grafos.Arista;
import grafos.Grafo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import juego.reglas.Esquina;
import juego.reglas.Partida;
import juego.reglas.Pedido;
import ui.Texto;

/**
 * Dibuja el mapa: calles (con flecha si son de una mano), esquinas, la ruta
 * del GPS, los destinos de los pedidos y al cadete.
 */
public class DibujoGrafo {

    private static final int RADIO = 11;
    private static final Color CALLE = new Color(90, 98, 120);
    private static final Color CALLE_UNA_MANO = new Color(170, 120, 90);
    private static final Color RUTA_GPS = new Color(255, 170, 40);

    public void dibujar(Graphics2D g, Partida partida, Esquina local, Esquina bajoMouse) {
        Grafo<Esquina> grafo = partida.getGrafo();
        Lista<Esquina> esquinas = grafo.vertices();

        dibujarCalles(g, grafo, esquinas);
        dibujarGps(g, partida);
        dibujarEsquinas(g, esquinas, local);
        dibujarDestinos(g, partida);
        dibujarCadete(g, partida);

        if (bajoMouse != null) {
            Texto.centrado(g, bajoMouse.getNombre(), bajoMouse.getX(), bajoMouse.getY() - RADIO - 8, 13, Color.WHITE);
        }
    }

    /** La esquina bajo el mouse (a menos de 16 px), o null. */
    public Esquina esquinaEn(Partida partida, int mx, int my) {
        Lista<Esquina> esquinas = partida.getGrafo().vertices();
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina e = esquinas.obtener(i);
            double dx = e.getX() - mx;
            double dy = e.getY() - my;
            if (dx * dx + dy * dy <= 16 * 16) {
                return e;
            }
        }
        return null;
    }

    private void dibujarCalles(Graphics2D g, Grafo<Esquina> grafo, Lista<Esquina> esquinas) {
        g.setStroke(new BasicStroke(3f));
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina origen = esquinas.obtener(i);
            Lista<Arista<Esquina>> salientes = grafo.vecinos(origen);
            for (int j = 0; j < salientes.tamanio(); j++) {
                Esquina destino = salientes.obtener(j).getDestino();
                boolean dobleMano = grafo.existeArista(destino, origen);
                if (dobleMano) {
                    if (origen.getId() < destino.getId()) {
                        g.setColor(CALLE);
                        g.drawLine(origen.getX(), origen.getY(), destino.getX(), destino.getY());
                    }
                } else {
                    g.setColor(CALLE_UNA_MANO);
                    g.drawLine(origen.getX(), origen.getY(), destino.getX(), destino.getY());
                    flecha(g, origen, destino);
                }
            }
        }
    }

    private void flecha(Graphics2D g, Esquina desde, Esquina hasta) {
        double dx = hasta.getX() - desde.getX();
        double dy = hasta.getY() - desde.getY();
        double largo = Math.sqrt(dx * dx + dy * dy);
        if (largo < 1) {
            return;
        }
        double ux = dx / largo;
        double uy = dy / largo;
        double px = hasta.getX() - ux * (RADIO + 6);
        double py = hasta.getY() - uy * (RADIO + 6);
        Polygon punta = new Polygon();
        punta.addPoint((int) (px + ux * 8), (int) (py + uy * 8));
        punta.addPoint((int) (px - uy * 6), (int) (py + ux * 6));
        punta.addPoint((int) (px + uy * 6), (int) (py - ux * 6));
        g.fillPolygon(punta);
    }

    private void dibujarGps(Graphics2D g, Partida partida) {
        if (partida.getRutaGps() == null) {
            return;
        }
        Lista<Esquina> ruta = partida.getRutaGps().getRuta();
        Stroke anterior = g.getStroke();
        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(RUTA_GPS);
        for (int i = 0; i + 1 < ruta.tamanio(); i++) {
            Esquina a = ruta.obtener(i);
            Esquina b = ruta.obtener(i + 1);
            g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
        g.setStroke(anterior);
    }

    private void dibujarEsquinas(Graphics2D g, Lista<Esquina> esquinas, Esquina local) {
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina e = esquinas.obtener(i);
            g.setColor(new Color(40, 46, 66));
            g.fillOval(e.getX() - RADIO, e.getY() - RADIO, RADIO * 2, RADIO * 2);
            g.setColor(new Color(150, 160, 190));
            g.setStroke(new BasicStroke(2f));
            g.drawOval(e.getX() - RADIO, e.getY() - RADIO, RADIO * 2, RADIO * 2);
            if (e.equals(local)) {
                Texto.centrado(g, "UTN", e.getX(), e.getY() + RADIO + 14, 11, new Color(120, 200, 255));
            }
        }
    }

    private void dibujarDestinos(Graphics2D g, Partida partida) {
        g.setStroke(new BasicStroke(3f));
        // Próximo pedido de la cola (todavía sin aceptar): anillo verde
        Pedido proximo = partida.getProximoEntrante();
        if (proximo != null) {
            anillo(g, proximo.getDestino(), new Color(90, 220, 140), RADIO + 5);
        }
        // Pedidos aceptados: amarillo, y el más urgente en rojo
        Lista<Pedido> pendientes = partida.getPendientes();
        Pedido urgente = partida.getUrgente();
        for (int i = 0; i < pendientes.tamanio(); i++) {
            Pedido p = pendientes.obtener(i);
            boolean esUrgente = p.equals(urgente);
            anillo(g, p.getDestino(), esUrgente ? new Color(240, 70, 70) : new Color(240, 210, 70), RADIO + 8);
            Texto.centrado(g, String.valueOf(i + 1), p.getDestino().getX(), p.getDestino().getY() + 4, 12,
                    esUrgente ? new Color(255, 150, 150) : new Color(255, 235, 140));
        }
    }

    private void anillo(Graphics2D g, Esquina e, Color color, int radio) {
        g.setColor(color);
        g.drawOval(e.getX() - radio, e.getY() - radio, radio * 2, radio * 2);
    }

    private void dibujarCadete(Graphics2D g, Partida partida) {
        double x = partida.getActual().getX();
        double y = partida.getActual().getY();
        if (partida.estaEnTransito()) {
            double t = partida.getProgresoTramo();
            x = partida.getOrigenTramo().getX() + (partida.getDestinoTramo().getX() - partida.getOrigenTramo().getX()) * t;
            y = partida.getOrigenTramo().getY() + (partida.getDestinoTramo().getY() - partida.getOrigenTramo().getY()) * t;
        }
        g.setColor(new Color(70, 150, 255));
        g.fillOval((int) x - 9, (int) y - 9, 18, 18);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2f));
        g.drawOval((int) x - 9, (int) y - 9, 18, 18);
    }
}
