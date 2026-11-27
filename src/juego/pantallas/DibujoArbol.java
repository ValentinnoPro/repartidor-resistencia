package juego.pantallas;

import arboles.NodoBinario;
import estructuras.Cola;
import estructuras.ColaEnlazada;
import estructuras.Lista;
import estructuras.ListaEnlazada;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import juego.reglas.Mejora;
import juego.reglas.Taller;
import ui.Texto;

/**
 * Dibuja el árbol de mejoras recorriéndolo POR NIVELES con la Cola propia:
 * cada nodo se dibuja una vez que se conocen la posición de su padre y su
 * nivel, y los hijos se ubican a izquierda y derecha, con separación mitad.
 */
public class DibujoArbol {

    private static final int ANCHO_CAJA = 132;
    private static final int ALTO_CAJA = 52;

    // Un nodo ya ubicado en pantalla
    private static class Marco {
        private final Mejora mejora;
        private final double x;
        private final double y;
        private final double padreX;
        private final double padreY;
        private final boolean tienePadre;

        private Marco(Mejora mejora, double x, double y, double padreX, double padreY, boolean tienePadre) {
            this.mejora = mejora;
            this.x = x;
            this.y = y;
            this.padreX = padreX;
            this.padreY = padreY;
            this.tienePadre = tienePadre;
        }
    }

    private static class Pendiente {
        private final NodoBinario<Mejora> nodo;
        private final Marco marco;
        private final double separacion;

        private Pendiente(NodoBinario<Mejora> nodo, Marco marco, double separacion) {
            this.nodo = nodo;
            this.marco = marco;
            this.separacion = separacion;
        }
    }

    // Disposición por niveles: devuelve los nodos ya ubicados
    private Lista<Marco> disponer(Taller taller, double centroX, double arriba, double separacion, double bajada) {
        Lista<Marco> marcos = new ListaEnlazada<>();
        NodoBinario<Mejora> raiz = taller.getArbol().getRaiz();
        if (raiz == null) {
            return marcos;
        }
        Cola<Pendiente> cola = new ColaEnlazada<>();
        Marco marcoRaiz = new Marco(raiz.getValor(), centroX, arriba, 0, 0, false);
        cola.encolar(new Pendiente(raiz, marcoRaiz, separacion));
        while (!cola.estaVacia()) {
            Pendiente p = cola.desencolar();
            marcos.agregar(p.marco);
            double hijoY = p.marco.y + bajada;
            if (p.nodo.getIzquierdo() != null) {
                Marco m = new Marco(p.nodo.getIzquierdo().getValor(), p.marco.x - p.separacion, hijoY,
                        p.marco.x, p.marco.y, true);
                cola.encolar(new Pendiente(p.nodo.getIzquierdo(), m, p.separacion / 2));
            }
            if (p.nodo.getDerecho() != null) {
                Marco m = new Marco(p.nodo.getDerecho().getValor(), p.marco.x + p.separacion, hijoY,
                        p.marco.x, p.marco.y, true);
                cola.encolar(new Pendiente(p.nodo.getDerecho(), m, p.separacion / 2));
            }
        }
        return marcos;
    }

    public void dibujar(Graphics2D g, Taller taller, double centroX, double arriba, double separacion, double bajada) {
        dibujar(g, taller, centroX, arriba, separacion, bajada, null, 0);
    }

    /** Igual que el anterior, pero resalta la mejora bajo el mouse y anima las que se pueden comprar. */
    public void dibujar(Graphics2D g, Taller taller, double centroX, double arriba, double separacion, double bajada,
                        Mejora resaltada, double tiempo) {
        Lista<Marco> marcos = disponer(taller, centroX, arriba, separacion, bajada);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Primero las líneas, para que las cajas las tapen: verdes si la rama ya está desbloqueada
        for (int i = 0; i < marcos.tamanio(); i++) {
            Marco m = marcos.obtener(i);
            if (m.tienePadre) {
                g.setColor(m.mejora.isComprada() ? new Color(80, 190, 130) : new Color(80, 92, 128));
                g.drawLine((int) m.padreX, (int) m.padreY + ALTO_CAJA / 2, (int) m.x, (int) m.y - ALTO_CAJA / 2);
            }
        }
        for (int i = 0; i < marcos.tamanio(); i++) {
            Marco m = marcos.obtener(i);
            Mejora mejora = m.mejora;
            boolean disponible = !mejora.isComprada() && taller.puedeComprar(mejora);
            boolean alcanza = disponible && taller.getDinero() >= mejora.getCosto();
            boolean encima = mejora.equals(resaltada);
            Color acento;
            if (mejora.isComprada()) {
                acento = Estilo.VERDE;
            } else if (disponible) {
                acento = alcanza ? Estilo.DORADO : Estilo.NARANJA;
            } else {
                acento = new Color(100, 108, 140);
            }
            int x = (int) m.x - ANCHO_CAJA / 2;
            int y = (int) m.y - ALTO_CAJA / 2 - (encima && disponible ? 3 : 0);
            if (alcanza) {
                g.setColor(Estilo.alfa(acento, (int) (40 + 50 * Estilo.pulso(tiempo, 0.9))));
                g.fillRoundRect(x - 6, y - 6, ANCHO_CAJA + 12, ALTO_CAJA + 12, 22, 22);
            }
            Color relleno = mejora.isComprada() ? new Color(30, 78, 60) : disponible ? new Color(58, 52, 32) : new Color(30, 36, 54);
            if (encima) {
                relleno = Estilo.mezclar(relleno, Color.WHITE, 0.12);
            }
            Estilo.panel(g, x, y, ANCHO_CAJA, ALTO_CAJA, 14, relleno, acento);
            Texto.centrado(g, mejora.getNombre(), m.x, y + 22, 15, Estilo.TEXTO);
            String detalle = mejora.isComprada() ? "COMPRADA" : "$" + mejora.getCosto() + "  ·  x" + mejora.getFactorVelocidad();
            Texto.centrado(g, detalle, m.x, y + 40, 12, mejora.isComprada() ? Estilo.VERDE : new Color(205, 212, 235));
        }
    }

    /** La mejora cuya caja contiene el punto, o null. */
    public Mejora mejoraEn(Taller taller, double centroX, double arriba, double separacion, double bajada,
                           int mx, int my) {
        Lista<Marco> marcos = disponer(taller, centroX, arriba, separacion, bajada);
        for (int i = 0; i < marcos.tamanio(); i++) {
            Marco m = marcos.obtener(i);
            if (Math.abs(mx - m.x) <= ANCHO_CAJA / 2.0 && Math.abs(my - m.y) <= ALTO_CAJA / 2.0) {
                return m.mejora;
            }
        }
        return null;
    }
}
