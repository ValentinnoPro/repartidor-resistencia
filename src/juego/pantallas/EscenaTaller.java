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
 * Taller: el árbol de mejoras. Click en una caja disponible para comprarla (solo
 * se pueden comprar las que cuelgan de una mejora ya comprada).
 */
public class EscenaTaller extends Escena {

    private static final double CENTRO_X = 470;
    private static final double ARRIBA = 190;
    private static final double SEPARACION = 215;
    private static final double BAJADA = 120;

    private final Sesion sesion;
    private final DibujoArbol dibujo = new DibujoArbol();
    private final Efectos efectos = new Efectos();
    private final Boton nuevoTurno = new Boton(300, 584, 260, 46, "NUEVO TURNO", "ENTER", Estilo.VERDE);
    private final Boton menu = new Boton(572, 584, 150, 46, "MENÚ", "ESC", new Color(90, 100, 135));
    private String mensaje = "Click en una mejora disponible (dorada) para comprarla";
    private Color colorMensaje = Estilo.TEXTO_SUAVE;
    private double tiempo;
    private Mejora bajoMouse;

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
        tiempo += dt;
        efectos.actualizar(dt);
        bajoMouse = dibujo.mejoraEn(taller, CENTRO_X, ARRIBA, SEPARACION, BAJADA, entrada.getMouseX(), entrada.getMouseY());
        if (entrada.huboClick() && bajoMouse != null) {
            intentarComprar(taller, bajoMouse, entrada.getMouseX(), entrada.getMouseY());
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER) || nuevoTurno.fuePulsado(entrada)) {
            getContexto().irA(new EscenaMapa(sesion, sesion.nuevaPartida()));
        } else if (entrada.fuePresionada(KeyEvent.VK_ESCAPE) || menu.fuePulsado(entrada)) {
            getContexto().irA(new EscenaMenu(sesion));
        }
    }

    private void intentarComprar(Taller taller, Mejora mejora, int mx, int my) {
        if (mejora.isComprada()) {
            mensaje = mejora.getNombre() + " ya está comprada";
            colorMensaje = Estilo.TEXTO_SUAVE;
        } else if (taller.puedeComprar(mejora)) {
            taller.comprar(mejora);
            sesion.guardarTaller();
            mensaje = "Compraste " + mejora.getNombre() + ": el tiempo de cada tramo baja a x" + mejora.getFactorVelocidad();
            colorMensaje = Estilo.VERDE;
            efectos.confeti(mx, my, 40);
            efectos.texto(mx, my - 30, "-$" + mejora.getCosto(), Estilo.DORADO);
        } else if (taller.getDinero() < mejora.getCosto()) {
            mensaje = "No te alcanza la plata para " + mejora.getNombre();
            colorMensaje = Estilo.ROJO;
        } else {
            mensaje = "Primero tenés que comprar la mejora anterior";
            colorMensaje = Estilo.DORADO;
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        Entrada entrada = getEntrada();
        Taller taller = sesion.getTaller();
        Estilo.fondo(g, ancho, alto);

        Estilo.sombreadoCentrado(g, "TALLER", CENTRO_X, 64, 36, Estilo.DORADO);
        Texto.centrado(g, "Mejorá tu vehículo: cada mejora baja el tiempo de todos los tramos", CENTRO_X, 92, 13, Estilo.TEXTO_SUAVE);

        // Ahorro y factor de velocidad
        Estilo.panel(g, 30, 30, 190, 62, 16, Estilo.PANEL, Estilo.BORDE);
        Texto.izquierda(g, "AHORRO", 48, 52, 11, Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, "$" + taller.getDinero(), 48, 79, 26, Estilo.VERDE);
        Estilo.panel(g, 720, 30, 190, 62, 16, Estilo.PANEL, Estilo.BORDE);
        Texto.izquierda(g, "FACTOR DE TIEMPO", 738, 52, 11, Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, "x" + String.format("%.2f", taller.getFactorVelocidad()), 738, 79, 26, Estilo.AZUL);

        dibujo.dibujar(g, taller, CENTRO_X, ARRIBA, SEPARACION, BAJADA, bajoMouse, tiempo);

        // Todas las mejoras en in-orden, con el costo de su rama (post-orden)
        Lista<Mejora> todas = taller.getArbol().inOrden();
        int px = 790;
        Estilo.panel(g, px, 130, ancho - px - 30, 40 + todas.tamanio() * 24, 16, Estilo.PANEL, Estilo.BORDE);
        Texto.izquierda(g, "COSTO DE CADA RAMA COMPLETA", px + 16, 154, 11, Estilo.TEXTO_SUAVE);
        for (int i = 0; i < todas.tamanio(); i++) {
            Mejora m = todas.obtener(i);
            Texto.izquierda(g, m.getNombre(), px + 16, 182 + i * 24, 13, m.isComprada() ? Estilo.VERDE : new Color(205, 212, 235));
            Texto.derecha(g, "$" + taller.costoDeRama(m), ancho - 46, 182 + i * 24, 13, Estilo.TEXTO_SUAVE);
        }

        efectos.dibujar(g);
        Texto.centrado(g, mensaje, CENTRO_X, 560, 15, colorMensaje);
        nuevoTurno.dibujar(g, entrada);
        menu.dibujar(g, entrada);
    }
}
