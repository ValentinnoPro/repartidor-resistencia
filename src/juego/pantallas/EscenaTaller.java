package juego.pantallas;

import estructuras.Lista;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.reglas.Mejora;
import juego.reglas.Taller;
import motor.Entrada;
import motor.Escena;
import ui.Texto;

/**
 * Taller: el árbol de mejoras. Click en una caja dorada para comprarla (solo
 * se pueden comprar las que cuelgan de una mejora ya comprada).
 */
public class EscenaTaller extends Escena {

    private static final double CENTRO_X = 540;
    private static final double ARRIBA = 170;
    private static final double SEPARACION = 230;
    private static final double BAJADA = 120;

    private final Sesion sesion;
    private final DibujoArbol dibujo = new DibujoArbol();
    private String mensaje = "Click en una mejora disponible (dorada) para comprarla";

    public EscenaTaller(Sesion sesion) {
        if (sesion == null) {
            throw new IllegalArgumentException("la sesión no puede ser nula");
        }
        this.sesion = sesion;
    }

    @Override
    public void actualizar(double dt) {
        Entrada entrada = getEntrada();
        Taller taller = sesion.getTaller();
        if (entrada.huboClick()) {
            Mejora elegida = dibujo.mejoraEn(taller, CENTRO_X, ARRIBA, SEPARACION, BAJADA,
                    entrada.getMouseX(), entrada.getMouseY());
            if (elegida != null) {
                intentarComprar(taller, elegida);
            }
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER)) {
            getContexto().irA(new EscenaMapa(sesion, sesion.nuevaPartida()));
        }
        if (entrada.fuePresionada(KeyEvent.VK_ESCAPE)) {
            getContexto().irA(new EscenaMenu(sesion));
        }
    }

    private void intentarComprar(Taller taller, Mejora mejora) {
        if (mejora.isComprada()) {
            mensaje = mejora.getNombre() + " ya está comprada";
        } else if (taller.puedeComprar(mejora)) {
            taller.comprar(mejora);
            sesion.guardarTaller();
            mensaje = "Compraste " + mejora.getNombre() + ": el tiempo de cada tramo baja a x" + mejora.getFactorVelocidad();
        } else if (taller.getDinero() < mejora.getCosto()) {
            mensaje = "No te alcanza la plata para " + mejora.getNombre();
        } else {
            mensaje = "Primero tenés que comprar la mejora anterior";
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        Taller taller = sesion.getTaller();
        g.setColor(new Color(20, 24, 36));
        g.fillRect(0, 0, ancho, alto);

        Texto.centrado(g, "TALLER", ancho / 2.0, 60, 36, new Color(255, 190, 70));
        Texto.izquierda(g, "Ahorro: $" + taller.getDinero(), 40, 60, 22, new Color(120, 230, 150));
        Texto.derecha(g, "Factor de tiempo: x" + String.format("%.2f", taller.getFactorVelocidad()), ancho - 40, 60, 18,
                Color.WHITE);

        dibujo.dibujar(g, taller, CENTRO_X, ARRIBA, SEPARACION, BAJADA);

        // Todas las mejoras en in-orden, con el costo de su rama (post-orden)
        Lista<Mejora> todas = taller.getArbol().inOrden();
        Texto.izquierda(g, "Costo de cada rama completa", 790, 400, 14, new Color(190, 195, 215));
        for (int i = 0; i < todas.tamanio(); i++) {
            Mejora m = todas.obtener(i);
            Texto.izquierda(g, m.getNombre() + ": $" + taller.costoDeRama(m), 790, 426 + i * 20, 13,
                    new Color(150, 155, 175));
        }

        Texto.centrado(g, mensaje, ancho / 2.0, alto - 70, 16, new Color(255, 230, 150));
        Texto.centrado(g, "ENTER: nuevo turno     ESC: menú", ancho / 2.0, alto - 36, 16, new Color(190, 195, 215));
    }
}
