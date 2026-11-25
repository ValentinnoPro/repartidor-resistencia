package juego.pantallas;

import estructuras.Lista;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.reglas.CalleInexistenteException;
import juego.reglas.Esquina;
import juego.reglas.MovimientoInvalidoException;
import juego.reglas.Partida;
import juego.reglas.Pedido;
import motor.Entrada;
import motor.Escena;
import motor.Temporizador;
import ui.Texto;

/**
 * La partida: mapa a la izquierda, HUD a la derecha y la radio local abajo.
 * Controles: click en una esquina vecina para moverse, TAB para escribir un
 * destino, A aceptar pedido, G GPS, Z deshacer.
 */
public class EscenaMapa extends Escena {

    private static final int ANCHO_MAPA = 790;
    private static final int ALTO_MAPA = 610;

    private final Sesion sesion;
    private final Partida partida;
    private final DibujoGrafo dibujo = new DibujoGrafo();
    private final CajaTexto caja = new CajaTexto(30);
    private final Temporizador cambioRadio = new Temporizador(12.0);
    private boolean escribiendo;
    private boolean cerrada;
    private String radio = "";
    private Esquina bajoMouse;

    public EscenaMapa(Sesion sesion, Partida partida) {
        if (sesion == null || partida == null) {
            throw new IllegalArgumentException("la escena necesita sesión y partida");
        }
        this.sesion = sesion;
        this.partida = partida;
    }

    @Override
    public void alEntrar() {
        radio = sesion.getModeloRadio().generar(sesion.getAzar(), 70);
    }

    @Override
    public void actualizar(double dt) {
        Entrada entrada = getEntrada();
        bajoMouse = dibujo.esquinaEn(partida, entrada.getMouseX(), entrada.getMouseY());

        if (escribiendo) {
            manejarEscritura(entrada);
        } else {
            manejarComandos(entrada);
        }

        partida.actualizar(dt);
        if (cambioRadio.avanzar(dt)) {
            radio = sesion.getModeloRadio().generar(sesion.getAzar(), 70);
        }
        if (partida.haTerminado() && !cerrada) {
            cerrada = true;
            getContexto().irA(new EscenaFinTurno(sesion, partida));
        }
    }

    private void manejarEscritura(Entrada entrada) {
        caja.procesar(entrada);
        if (entrada.fuePresionada(KeyEvent.VK_ENTER)) {
            try {
                partida.irHacia(caja.getTexto());
                escribiendo = false;
                caja.vaciar();
            } catch (CalleInexistenteException | MovimientoInvalidoException e) {
                partida.setMensaje(e.getMessage());
            }
        } else if (entrada.fuePresionada(KeyEvent.VK_ESCAPE) || entrada.fuePresionada(KeyEvent.VK_TAB)) {
            escribiendo = false;
            caja.vaciar();
        }
    }

    private void manejarComandos(Entrada entrada) {
        try {
            if (entrada.fuePresionada(KeyEvent.VK_TAB)) {
                escribiendo = true;
                caja.vaciar();
            } else if (entrada.fuePresionada(KeyEvent.VK_A)) {
                partida.aceptarSiguiente();
            } else if (entrada.fuePresionada(KeyEvent.VK_G)) {
                partida.pedirGps();
            } else if (entrada.fuePresionada(KeyEvent.VK_Z)) {
                partida.deshacer();
            } else if (entrada.fuePresionada(KeyEvent.VK_ESCAPE)) {
                getContexto().irA(new EscenaMenu(sesion));
            } else if (entrada.huboClick() && bajoMouse != null) {
                partida.mover(bajoMouse);
            }
        } catch (MovimientoInvalidoException e) {
            partida.setMensaje(e.getMessage());
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        g.setColor(new Color(20, 24, 36));
        g.fillRect(0, 0, ancho, alto);

        dibujo.dibujar(g, partida, sesion.getMapa().vertice(0), bajoMouse);
        if (partida.estaLloviendo()) {
            g.setColor(new Color(60, 90, 150, 55));
            g.fillRect(0, 0, ANCHO_MAPA, ALTO_MAPA);
            Texto.izquierda(g, "LLUVIA", 16, 30, 20, new Color(150, 190, 255));
        }
        dibujarMensaje(g);
        dibujarHud(g, ancho);
        dibujarRadio(g, ancho, alto);
    }

    private void dibujarMensaje(Graphics2D g) {
        String mensaje = partida.getMensaje();
        if (!mensaje.isEmpty()) {
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRoundRect(16, ALTO_MAPA - 44, ANCHO_MAPA - 32, 32, 10, 10);
            Texto.centrado(g, mensaje, ANCHO_MAPA / 2.0, ALTO_MAPA - 22, 16, new Color(255, 230, 150));
        }
    }

    private void dibujarHud(Graphics2D g, int ancho) {
        int x = ANCHO_MAPA + 10;
        g.setColor(new Color(28, 33, 50));
        g.fillRect(ANCHO_MAPA, 0, ancho - ANCHO_MAPA, ALTO_MAPA);

        Texto.izquierda(g, "Tiempo  " + Formato.reloj(partida.getTiempoRestante()), x, 34, 22, Color.WHITE);
        Texto.izquierda(g, "Puntos " + partida.getPuntaje(), x, 62, 16, new Color(255, 210, 90));
        Texto.derecha(g, "$" + partida.getDinero(), ancho - 12, 62, 16, new Color(120, 230, 150));
        Texto.izquierda(g, partida.getVehiculo().getNombre() + "  x" + String.format("%.2f", partida.getVehiculo().getFactorMejoras()),
                x, 86, 13, new Color(170, 175, 195));

        // Cola de pedidos entrantes
        Texto.izquierda(g, "Esperando (" + partida.cantidadEntrantes() + ")  [A] aceptar", x, 120, 14, new Color(90, 220, 140));
        Pedido proximo = partida.getProximoEntrante();
        if (proximo != null) {
            Texto.izquierda(g, recortar(proximo.getCliente(), 26), x, 142, 13, Color.WHITE);
            Texto.izquierda(g, "a " + proximo.getDestino().getNombre() + "  $" + proximo.getMonto(), x, 160, 12,
                    new Color(190, 195, 215));
        }

        // Pendientes: in-orden del ABB (del más urgente al menos urgente)
        Texto.izquierda(g, "Pedidos aceptados", x, 196, 14, new Color(240, 210, 70));
        Lista<Pedido> pendientes = partida.getPendientes();
        if (pendientes.estaVacia()) {
            Texto.izquierda(g, "ninguno", x, 218, 12, new Color(150, 155, 175));
        }
        for (int i = 0; i < Math.min(5, pendientes.tamanio()); i++) {
            Pedido p = pendientes.obtener(i);
            double restante = p.getHoraLimite() - partida.getReloj();
            Color color = i == 0 ? new Color(255, 130, 130) : new Color(225, 230, 245);
            Texto.izquierda(g, (i + 1) + ". " + recortar(p.getCliente(), 18), x, 218 + i * 36, 13, color);
            Texto.derecha(g, Formato.reloj(restante), ancho - 12, 218 + i * 36, 13, color);
            Texto.izquierda(g, p.getDestino().getNombre(), x + 14, 234 + i * 36, 11, new Color(170, 175, 195));
        }

        // Destino por escrito
        int yCaja = 420;
        Texto.izquierda(g, "[TAB] Ir a una esquina", x, yCaja, 14, new Color(120, 200, 255));
        g.setColor(escribiendo ? new Color(60, 70, 105) : new Color(38, 44, 64));
        g.fillRoundRect(x, yCaja + 8, ancho - x - 12, 30, 8, 8);
        String texto = caja.getTexto();
        boolean cursor = escribiendo && (System.currentTimeMillis() / 400) % 2 == 0;
        Texto.izquierda(g, texto.isEmpty() && !escribiendo ? "ej: sarmiento y belgrano" : texto + (cursor ? "|" : ""),
                x + 8, yCaja + 29, 14, texto.isEmpty() && !escribiendo ? new Color(120, 125, 145) : Color.WHITE);
        if (escribiendo) {
            Lista<String> sugerencias = partida.sugerencias(texto);
            for (int i = 0; i < Math.min(4, sugerencias.tamanio()); i++) {
                Texto.izquierda(g, "· " + Formato.mayuscula(sugerencias.obtener(i)), x + 8, yCaja + 58 + i * 18, 12,
                        new Color(170, 215, 255));
            }
        }

        Texto.izquierda(g, "[G] GPS (-" + Partida.PENALIDAD_GPS + ")   [Z] deshacer (-30s)   [ESC] menú", x, ALTO_MAPA - 40, 11,
                new Color(150, 155, 175));
        Texto.izquierda(g, "Click en una esquina vecina para moverte", x, ALTO_MAPA - 22, 11, new Color(150, 155, 175));
    }

    private void dibujarRadio(Graphics2D g, int ancho, int alto) {
        g.setColor(new Color(36, 30, 52));
        g.fillRect(0, ALTO_MAPA, ancho, alto - ALTO_MAPA);
        Texto.izquierda(g, "RADIO", 14, alto - 16, 14, new Color(255, 120, 150));
        Texto.izquierda(g, radio, 80, alto - 16, 15, new Color(225, 215, 245));
    }

    private String recortar(String texto, int largo) {
        return texto.length() <= largo ? texto : texto.substring(0, largo - 1) + "…";
    }
}
