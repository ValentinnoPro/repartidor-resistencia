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
        Lista<Marco> marcos = disponer(taller, centroX, arriba, separacion, bajada);
        g.setStroke(new BasicStroke(2f));
        // Primero las líneas, para que las cajas las tapen
        g.setColor(new Color(110, 120, 150));
        for (int i = 0; i < marcos.tamanio(); i++) {
            Marco m = marcos.obtener(i);
            if (m.tienePadre) {
                g.drawLine((int) m.padreX, (int) m.padreY + ALTO_CAJA / 2, (int) m.x, (int) m.y - ALTO_CAJA / 2);
            }
        }
        for (int i = 0; i < marcos.tamanio(); i++) {
            Marco m = marcos.obtener(i);
            Mejora mejora = m.mejora;
            Color relleno;
            Color borde;
            if (mejora.isComprada()) {
                relleno = new Color(40, 100, 70);
                borde = new Color(110, 220, 150);
            } else if (taller.puedeComprar(mejora)) {
                relleno = new Color(80, 70, 30);
                borde = new Color(250, 210, 80);
            } else {
                relleno = new Color(40, 44, 60);
                borde = new Color(100, 108, 135);
            }
            int x = (int) m.x - ANCHO_CAJA / 2;
            int y = (int) m.y - ALTO_CAJA / 2;
            g.setColor(relleno);
            g.fillRoundRect(x, y, ANCHO_CAJA, ALTO_CAJA, 12, 12);
            g.setColor(borde);
            g.drawRoundRect(x, y, ANCHO_CAJA, ALTO_CAJA, 12, 12);
            Texto.centrado(g, mejora.getNombre(), m.x, m.y - 6, 14, Color.WHITE);
            String detalle = mejora.isComprada() ? "comprada" : "$" + mejora.getCosto() + "  x" + mejora.getFactorVelocidad();
            Texto.centrado(g, detalle, m.x, m.y + 14, 12, new Color(200, 205, 225));
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
