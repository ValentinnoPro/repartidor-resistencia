package juego.pantallas;

import estructuras.Lista;
import grafos.Arista;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
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
 * La partida: mapa a la izquierda, panel con botones a la derecha y la radio
 * local abajo. Se juega solo con el mouse:
 * - click en una esquina verde: te movés hasta ahí;
 * - click en una esquina lejana: ruta automática (si estás en camino, queda en cola);
 * - click en un pedido de la lista: ir hacia su destino;
 * - botones ACEPTAR, GPS, DESHACER, IR A... y PAUSA.
 * El teclado sigue funcionando: flechas (mantenidas) para moverte, A/ENTER/ESPACIO aceptar,
 * G GPS, Z deshacer, TAB escribir un destino, P o ESC pausa.
 */
public class EscenaMapa extends Escena {

    private static final int ANCHO_MAPA = DibujoGrafo.ANCHO_MAPA;
    private static final int ALTO_MAPA = DibujoGrafo.ALTO_MAPA;
    private static final int X0 = ANCHO_MAPA + 12;
    private static final int ANCHO_UTIL = 266;
    private static final int CENTRO_X = 540;
    private static final int FILA_Y = 292;
    private static final int FILA_ALTO = 42;
    private static final int FILA_PASO = 46;
    private static final int MAX_FILAS = 4;

    private final Sesion sesion;
    private final Partida partida;
    private final DibujoGrafo dibujo = new DibujoGrafo();
    private final Efectos efectos = new Efectos();
    private final CajaTexto caja = new CajaTexto(30);
    private final Temporizador cambioRadio = new Temporizador(12.0);

    private final Boton aceptar = new Boton(X0 + 12, 222, ANCHO_UTIL - 24, 30, "ACEPTAR PEDIDO", "A", Estilo.VERDE);
    private final Boton gps = new Boton(X0, 480, 129, 34, "GPS  -" + Partida.PENALIDAD_GPS, "G", Estilo.DORADO);
    private final Boton deshacer = new Boton(X0 + 137, 480, 129, 34, "DESHACER", "Z", Estilo.AZUL);
    private final Boton irA = new Boton(X0, 522, 129, 34, "IR A...", "TAB", new Color(120, 110, 220));
    private final Boton pausa = new Boton(X0 + 137, 522, 129, 34, "PAUSA", "P", new Color(90, 100, 135));
    private final Boton continuar = new Boton(CENTRO_X - 130, 290, 260, 44, "CONTINUAR", "P", Estilo.VERDE);
    private final Boton salir = new Boton(CENTRO_X - 130, 346, 260, 44, "SALIR AL MENÚ", "", new Color(150, 80, 90));

    private double tiempo;
    private boolean escribiendo;
    private boolean cerrada;
    private boolean pausado;
    private String radio = "";
    private double radioDesde;
    private Esquina bajoMouse;
    private Pedido resaltado;
    private Esquina destinoPendiente;

    // Para detectar lo que pasó entre un paso y el siguiente y mostrar efectos
    private int entregasPrevias;
    private int vencidosPrevios;
    private int puntosPrevios;
    private int dineroPrevio;
    private String mensajeVisto = "";
    private double mensajeEdad;
    private double sacudida;

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
        partida.setMensaje("Aceptá el pedido verde y llevalo antes de que se venza");
    }

    // ---------------------------------------------------------------- lógica

    @Override
    public void actualizar(double dt) {
        Entrada entrada = getEntrada();
        tiempo += dt;
        mensajeEdad += dt;
        bajoMouse = pausado ? null : dibujo.esquinaEn(partida, entrada.getMouseX(), entrada.getMouseY());
        resaltado = null;

        if (pausado) {
            manejarPausa(entrada);
            return;
        }

        if (escribiendo) {
            manejarEscritura(entrada);
        } else {
            manejarComandos(entrada);
        }

        if (destinoPendiente != null && !partida.estaEnTransito()) {
            Esquina objetivo = destinoPendiente;
            destinoPendiente = null;
            irAEsquina(objetivo);
        }

        partida.actualizar(dt);
        dibujo.actualizar(dt, partida, efectos);
        efectos.actualizar(dt);
        sacudida = Math.max(0, sacudida - dt);
        if (cambioRadio.avanzar(dt)) {
            radio = sesion.getModeloRadio().generar(sesion.getAzar(), 70);
            radioDesde = tiempo;
        }
        reaccionarACambios();

        if (partida.haTerminado() && !cerrada) {
            cerrada = true;
            getContexto().irA(new EscenaFinTurno(sesion, partida));
        }
    }

    private void manejarPausa(Entrada entrada) {
        if (continuar.fuePulsado(entrada) || entrada.fuePresionada(KeyEvent.VK_P)
                || entrada.fuePresionada(KeyEvent.VK_ESCAPE) || entrada.fuePresionada(KeyEvent.VK_ENTER)) {
            pausado = false;
        } else if (salir.fuePulsado(entrada)) {
            getContexto().irA(new EscenaMenu(sesion));
        }
    }

    private void manejarEscritura(Entrada entrada) {
        caja.procesar(entrada);
        if (entrada.fuePresionada(KeyEvent.VK_ENTER)) {
            try {
                ordenarIr(partida.buscarEsquina(caja.getTexto()));
                escribiendo = false;
                caja.vaciar();
            } catch (CalleInexistenteException e) {
                partida.setMensaje(e.getMessage());
            }
        } else if (entrada.fuePresionada(KeyEvent.VK_ESCAPE) || entrada.fuePresionada(KeyEvent.VK_TAB)
                || irA.fuePulsado(entrada)) {
            escribiendo = false;
            caja.vaciar();
        }
    }

    private void manejarComandos(Entrada entrada) {
        try {
            if (entrada.fuePresionada(KeyEvent.VK_TAB) || irA.fuePulsado(entrada)) {
                escribiendo = true;
                caja.vaciar();
            } else if (entrada.fuePresionada(KeyEvent.VK_A) || entrada.fuePresionada(KeyEvent.VK_ENTER)
                    || entrada.fuePresionada(KeyEvent.VK_SPACE) || aceptar.fuePulsado(entrada)) {
                partida.aceptarSiguiente();
            } else if (entrada.fuePresionada(KeyEvent.VK_G) || gps.fuePulsado(entrada)) {
                partida.pedirGps();
                efectos.texto(dibujo.getCadeteX(), dibujo.getCadeteY() - 30, "-" + Partida.PENALIDAD_GPS + " GPS", Estilo.AZUL);
            } else if (entrada.fuePresionada(KeyEvent.VK_Z) || deshacer.fuePulsado(entrada)) {
                partida.deshacer();
            } else if (entrada.fuePresionada(KeyEvent.VK_P) || entrada.fuePresionada(KeyEvent.VK_ESCAPE)
                    || pausa.fuePulsado(entrada)) {
                pausado = true;
            } else {
                manejarMouseYFlechas(entrada);
            }
        } catch (MovimientoInvalidoException e) {
            partida.setMensaje(e.getMessage());
        }
    }

    private void manejarMouseYFlechas(Entrada entrada) throws MovimientoInvalidoException {
        // Click en la lista de pedidos aceptados: ir hacia el destino
        Lista<Pedido> pendientes = partida.getPendientes();
        for (int i = 0; i < Math.min(MAX_FILAS, pendientes.tamanio()); i++) {
            if (enFila(entrada, i)) {
                resaltado = pendientes.obtener(i);
                if (entrada.huboClick()) {
                    ordenarIr(resaltado.getDestino());
                    return;
                }
            }
        }
        if (entrada.huboClick() && bajoMouse != null) {
            ordenarIr(bajoMouse);
            return;
        }
        // Flechas: mantenerlas apretadas encadena movimientos
        if (!partida.estaEnTransito()) {
            if (entrada.estaPresionada(KeyEvent.VK_LEFT)) {
                moverEn(-1, 0);
            } else if (entrada.estaPresionada(KeyEvent.VK_RIGHT)) {
                moverEn(1, 0);
            } else if (entrada.estaPresionada(KeyEvent.VK_UP)) {
                moverEn(0, -1);
            } else if (entrada.estaPresionada(KeyEvent.VK_DOWN)) {
                moverEn(0, 1);
            }
        }
    }

    private boolean enFila(Entrada entrada, int indice) {
        int y = FILA_Y + indice * FILA_PASO;
        int mx = entrada.getMouseX();
        int my = entrada.getMouseY();
        return !escribiendo && mx >= X0 && mx < X0 + ANCHO_UTIL && my >= y && my < y + FILA_ALTO;
    }

    // La esquina vecina que mejor apunta hacia (dx, dy), o nada si no hay calle en esa dirección
    private void moverEn(int dx, int dy) throws MovimientoInvalidoException {
        Esquina actual = partida.getActual();
        Lista<Arista<Esquina>> salientes = partida.getGrafo().vecinos(actual);
        Esquina mejor = null;
        double mejorCoseno = 0.7;
        for (int i = 0; i < salientes.tamanio(); i++) {
            Esquina e = salientes.obtener(i).getDestino();
            double vx = e.getX() - actual.getX();
            double vy = e.getY() - actual.getY();
            double largo = Math.sqrt(vx * vx + vy * vy);
            double coseno = largo > 0 ? (vx * dx + vy * dy) / largo : -1;
            if (coseno > mejorCoseno) {
                mejor = e;
                mejorCoseno = coseno;
            }
        }
        if (mejor != null) {
            partida.mover(mejor);
        }
    }

    /** Mueve si es vecina, sale en ruta automática si es lejana y, si ya está en camino, lo deja en cola. */
    private void ordenarIr(Esquina destino) {
        if (partida.estaEnTransito()) {
            destinoPendiente = destino;
            partida.setMensaje("Siguiente: " + destino.getNombre() + " (apenas llegues a la esquina)");
        } else {
            irAEsquina(destino);
        }
    }

    private void irAEsquina(Esquina destino) {
        try {
            if (destino.equals(partida.getActual())) {
                partida.setMensaje("Ya estás en " + destino.getNombre());
            } else if (partida.getGrafo().existeArista(partida.getActual(), destino)) {
                partida.mover(destino);
            } else {
                partida.irHacia(destino.getCalle1() + " y " + destino.getCalle2());
            }
        } catch (MovimientoInvalidoException | CalleInexistenteException e) {
            partida.setMensaje(e.getMessage());
        }
    }

    // Efectos al entregar, al vencerse un pedido y al aparecer un mensaje nuevo
    private void reaccionarACambios() {
        if (partida.getEntregas() > entregasPrevias) {
            int puntos = partida.getPuntaje() - puntosPrevios;
            efectos.texto(dibujo.getCadeteX(), dibujo.getCadeteY() - 34, "+" + puntos, Estilo.DORADO);
            efectos.texto(dibujo.getCadeteX(), dibujo.getCadeteY() - 56, "+$" + (partida.getDinero() - dineroPrevio),
                    Estilo.VERDE);
            efectos.confeti(dibujo.getCadeteX(), dibujo.getCadeteY(), 46);
        }
        if (partida.getVencidos() > vencidosPrevios) {
            efectos.texto(dibujo.getCadeteX(), dibujo.getCadeteY() - 34, "-" + Partida.PENALIDAD_VENCIDO, Estilo.ROJO);
            sacudida = 0.35;
        }
        entregasPrevias = partida.getEntregas();
        vencidosPrevios = partida.getVencidos();
        puntosPrevios = partida.getPuntaje();
        dineroPrevio = partida.getDinero();

        String mensaje = partida.getMensaje();
        if (!mensaje.equals(mensajeVisto)) {
            mensajeVisto = mensaje;
            mensajeEdad = 0;
        }
    }

    // ----------------------------------------------------------------- dibujo

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        Entrada entrada = getEntrada();
        Estilo.fondo(g, ancho, alto);

        double temblor = sacudida > 0 ? Math.sin(tiempo * 90) * sacudida * 14 : 0;
        g.translate(temblor, 0);
        Shape recorte = g.getClip();
        g.clipRect(0, 0, ANCHO_MAPA, ALTO_MAPA);
        dibujo.dibujar(g, partida, sesion.getMapa(), sesion.getMapa().vertice(0), bajoMouse, resaltado,
                sesion.getTipoVehiculo());
        efectos.dibujar(g);
        dibujarAvisoDeTiempo(g);
        g.setClip(recorte);
        g.translate(-temblor, 0);

        dibujarMensaje(g);
        dibujarPanel(g, ancho, entrada);
        dibujarRadio(g, ancho, alto);
        if (pausado) {
            dibujarPausa(g, ancho, alto, entrada);
        }
    }

    // Viñeta roja que late durante el último minuto
    private void dibujarAvisoDeTiempo(Graphics2D g) {
        if (partida.getTiempoRestante() > 1.0) {
            return;
        }
        int alfa = (int) (30 + 50 * Estilo.pulso(tiempo, 1.5));
        g.setColor(new Color(246, 60, 60, alfa));
        g.setStroke(new java.awt.BasicStroke(26f));
        g.drawRect(-6, -6, ANCHO_MAPA + 12, ALTO_MAPA + 12);
    }

    private void dibujarMensaje(Graphics2D g) {
        String mensaje = partida.getMensaje();
        if (mensaje.isEmpty()) {
            return;
        }
        double entra = Math.min(1.0, mensajeEdad / 0.25);
        int ancho = Math.min(ANCHO_MAPA - 40, mensaje.length() * 8 + 40);
        double x = (ANCHO_MAPA - ancho) / 2.0;
        double y = 14 - (1 - entra) * 40;
        Estilo.panel(g, x, y, ancho, 34, 14, new Color(14, 18, 32, 235), Estilo.DORADO);
        Texto.centrado(g, mensaje, ANCHO_MAPA / 2.0, y + 22, 14, new Color(255, 232, 160));
    }

    private void dibujarPanel(Graphics2D g, int ancho, Entrada entrada) {
        g.setColor(new Color(20, 26, 44));
        g.fillRect(ANCHO_MAPA, 0, ancho - ANCHO_MAPA, ALTO_MAPA);
        g.setColor(Estilo.BORDE);
        g.drawLine(ANCHO_MAPA, 0, ANCHO_MAPA, ALTO_MAPA);

        dibujarReloj(g);

        // Vehículo y lluvia
        Texto.izquierda(g, partida.getVehiculo().getNombre() + "  x" + String.format("%.2f", partida.getVehiculo().getFactorMejoras())
                + (partida.getVehiculo().getFactorMejoras() < 1.0 ? "  (mejorado)" : ""), X0, 118, 12, Estilo.TEXTO_SUAVE);
        if (partida.estaLloviendo()) {
            Estilo.panel(g, X0 + ANCHO_UTIL - 70, 104, 70, 18, 10, new Color(40, 70, 130), null);
            Texto.centrado(g, "LLUVIA", X0 + ANCHO_UTIL - 35, 117, 11, new Color(170, 205, 255));
        }

        dibujarNuevoPedido(g, entrada);
        if (escribiendo) {
            dibujarEscritura(g);
        } else {
            dibujarEnCamino(g, entrada);
        }

        gps.setHabilitado(!partida.getPendientes().estaVacia());
        deshacer.setHabilitado(partida.cantidadTramosDeshacibles() > 0 && !partida.estaEnTransito());
        irA.setTexto(escribiendo ? "CANCELAR" : "IR A...");
        gps.dibujar(g, entrada);
        deshacer.dibujar(g, entrada);
        irA.dibujar(g, entrada);
        pausa.dibujar(g, entrada);

        Texto.izquierda(g, "Click en el mapa: moverte / ir hacia", X0, ALTO_MAPA - 36, 11, Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, "Flechas: mover   Click en un pedido: ir", X0, ALTO_MAPA - 20, 11, Estilo.TEXTO_SUAVE);
    }

    private void dibujarReloj(Graphics2D g) {
        Estilo.panel(g, X0, 12, ANCHO_UTIL, 84, 14, Estilo.PANEL, Estilo.BORDE);
        double restante = partida.getTiempoRestante();
        boolean urgente = restante <= 1.0;
        Color colorReloj = urgente ? Estilo.mezclar(Estilo.ROJO, Color.WHITE, Estilo.pulso(tiempo, 1.5) * 0.5) : Estilo.TEXTO;
        Texto.izquierda(g, "TIEMPO", X0 + 14, 32, 11, Estilo.TEXTO_SUAVE);
        Texto.izquierda(g, Formato.reloj(restante), X0 + 14, 64, 32, colorReloj);

        double fraccion = restante / Partida.DURACION_TURNO;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(X0 + 14, 76, ANCHO_UTIL - 28, 8, 8, 8);
        g.setPaint(new GradientPaint(X0 + 14, 0, Estilo.VERDE, X0 + ANCHO_UTIL - 14, 0, Estilo.mezclar(Estilo.ROJO, Estilo.DORADO, fraccion)));
        g.fillRoundRect(X0 + 14, 76, (int) ((ANCHO_UTIL - 28) * fraccion), 8, 8, 8);

        Texto.derecha(g, "PUNTOS", X0 + ANCHO_UTIL - 14, 32, 11, Estilo.TEXTO_SUAVE);
        Texto.derecha(g, String.valueOf(partida.getPuntaje()), X0 + ANCHO_UTIL - 14, 54, 22,
                partida.getPuntaje() < 0 ? Estilo.ROJO : Estilo.DORADO);
        Texto.derecha(g, "$" + partida.getDinero(), X0 + ANCHO_UTIL - 14, 70, 14, Estilo.VERDE);
    }

    private void dibujarNuevoPedido(Graphics2D g, Entrada entrada) {
        Texto.izquierda(g, "PEDIDO NUEVO", X0, 146, 12, Estilo.VERDE);
        int cantidad = partida.cantidadEntrantes();
        Texto.derecha(g, cantidad + " esperando", X0 + ANCHO_UTIL, 146, 12, Estilo.TEXTO_SUAVE);
        Estilo.panel(g, X0, 154, ANCHO_UTIL, 104, 14, Estilo.PANEL, Estilo.alfa(Estilo.VERDE, 110));
        Pedido p = partida.getProximoEntrante();
        aceptar.setHabilitado(p != null);
        if (p == null) {
            Texto.centrado(g, "Sin pedidos esperando", X0 + ANCHO_UTIL / 2.0, 200, 14, Estilo.TEXTO_SUAVE);
        } else {
            Texto.izquierda(g, Estilo.recortar(p.getCliente(), 22), X0 + 14, 177, 15, Estilo.TEXTO);
            Texto.izquierda(g, "a " + Estilo.recortar(p.getDestino().getNombre(), 26), X0 + 14, 195, 12, new Color(190, 198, 222));
            Texto.izquierda(g, "$" + p.getMonto(), X0 + 14, 212, 13, Estilo.VERDE);
            double vence = p.getHoraLimite() - partida.getReloj();
            Texto.derecha(g, "vence en " + Formato.reloj(vence), X0 + ANCHO_UTIL - 14, 212, 12,
                    vence < 1.0 ? Estilo.ROJO : Estilo.TEXTO_SUAVE);
        }
        aceptar.dibujar(g, entrada);
    }

    private void dibujarEnCamino(Graphics2D g, Entrada entrada) {
        Lista<Pedido> pendientes = partida.getPendientes();
        Texto.izquierda(g, "EN CAMINO", X0, 282, 12, Estilo.DORADO);
        Texto.derecha(g, pendientes.tamanio() + (pendientes.tamanio() == 1 ? " pedido" : " pedidos"), X0 + ANCHO_UTIL, 282, 12, Estilo.TEXTO_SUAVE);
        if (pendientes.estaVacia()) {
            Estilo.panel(g, X0, FILA_Y, ANCHO_UTIL, 80, 14, Estilo.PANEL, null);
            Texto.centrado(g, "Todavía no aceptaste ninguno", X0 + ANCHO_UTIL / 2.0, FILA_Y + 36, 13, Estilo.TEXTO_SUAVE);
            Texto.centrado(g, "Tocá ACEPTAR PEDIDO", X0 + ANCHO_UTIL / 2.0, FILA_Y + 56, 12, Estilo.TEXTO_SUAVE);
            return;
        }
        Pedido urgente = partida.getUrgente();
        for (int i = 0; i < Math.min(MAX_FILAS, pendientes.tamanio()); i++) {
            Pedido p = pendientes.obtener(i);
            int y = FILA_Y + i * FILA_PASO;
            boolean esUrgente = p.equals(urgente);
            boolean encima = enFila(entrada, i);
            Color color = esUrgente ? Estilo.ROJO : Estilo.DORADO;
            Estilo.panel(g, X0, y, ANCHO_UTIL, FILA_ALTO, 12, encima ? Estilo.PANEL_CLARO : Estilo.PANEL,
                    esUrgente ? Estilo.alfa(Estilo.ROJO, 160) : null);
            g.setColor(color);
            g.fillOval(X0 + 10, y + 9, 24, 24);
            Texto.centrado(g, String.valueOf(i + 1), X0 + 22, y + 26, 14, new Color(35, 20, 10));
            Texto.izquierda(g, Estilo.recortar(p.getCliente(), 17), X0 + 44, y + 18, 13, Estilo.TEXTO);
            Texto.izquierda(g, Estilo.recortar(p.getDestino().getNombre(), 24), X0 + 44, y + 34, 11, Estilo.TEXTO_SUAVE);
            double restante = p.getHoraLimite() - partida.getReloj();
            Texto.derecha(g, Formato.reloj(restante), X0 + ANCHO_UTIL - 10, y + 18, 14,
                    restante < 1.0 ? Estilo.ROJO : Estilo.TEXTO);
            Texto.derecha(g, encima ? "ir hacia" : "$" + p.getMonto(), X0 + ANCHO_UTIL - 10, y + 34, 11,
                    encima ? Estilo.AZUL : Estilo.VERDE);
        }
        if (pendientes.tamanio() > MAX_FILAS) {
            Texto.centrado(g, "+" + (pendientes.tamanio() - MAX_FILAS) + " más", X0 + ANCHO_UTIL / 2.0, FILA_Y + MAX_FILAS * FILA_PASO + 6, 11,
                    Estilo.TEXTO_SUAVE);
        }
    }

    private void dibujarEscritura(Graphics2D g) {
        Texto.izquierda(g, "IR A UNA ESQUINA", X0, 282, 12, new Color(160, 150, 255));
        Estilo.panel(g, X0, FILA_Y, ANCHO_UTIL, 34, 12, Estilo.PANEL_CLARO, new Color(160, 150, 255));
        boolean cursor = Estilo.pulso(tiempo, 1.2) > 0.5;
        Texto.izquierda(g, caja.getTexto().isEmpty() ? "ej: sarmiento y belgrano" : caja.getTexto() + (cursor ? "|" : ""), X0 + 12,
                FILA_Y + 22, 14, caja.getTexto().isEmpty() ? Estilo.TEXTO_SUAVE : Estilo.TEXTO);
        Lista<String> sugerencias = partida.sugerencias(caja.getTexto());
        for (int i = 0; i < Math.min(5, sugerencias.tamanio()); i++) {
            Texto.izquierda(g, "· " + Formato.mayuscula(sugerencias.obtener(i)), X0 + 12, FILA_Y + 62 + i * 20, 13, new Color(170, 215, 255));
        }
        Texto.izquierda(g, "ENTER: ir     TAB: cancelar", X0, FILA_Y + 176, 11, Estilo.TEXTO_SUAVE);
    }

    private void dibujarRadio(Graphics2D g, int ancho, int alto) {
        g.setPaint(new GradientPaint(0, ALTO_MAPA, new Color(40, 30, 64), 0, alto, new Color(24, 18, 40)));
        g.fillRect(0, ALTO_MAPA, ancho, alto - ALTO_MAPA);
        g.setColor(Estilo.BORDE);
        g.drawLine(0, ALTO_MAPA, ancho, ALTO_MAPA);

        double centroY = ALTO_MAPA + (alto - ALTO_MAPA) / 2.0;
        g.setColor(Estilo.alfa(Estilo.ROJO, (int) (130 + 125 * Estilo.pulso(tiempo, 1.0))));
        g.fillOval(18, (int) centroY - 5, 10, 10);
        Texto.izquierda(g, "RADIO RESISTENCIA", 36, centroY + 5, 13, new Color(255, 120, 150));

        // Texto que corre de derecha a izquierda dentro de su franja
        int inicio = 210;
        Shape recorte = g.getClip();
        g.clipRect(inicio, ALTO_MAPA + 2, ancho - inicio - 10, alto - ALTO_MAPA - 4);
        Texto.izquierda(g, radio, ancho - 20 - (tiempo - radioDesde) * 120, centroY + 5, 15, new Color(225, 215, 245));
        g.setClip(recorte);
    }

    private void dibujarPausa(Graphics2D g, int ancho, int alto, Entrada entrada) {
        g.setColor(new Color(6, 8, 16, 190));
        g.fillRect(0, 0, ancho, alto);
        Estilo.panel(g, CENTRO_X - 170, 200, 340, 220, 20, Estilo.PANEL, Estilo.BORDE);
        Estilo.sombreadoCentrado(g, "PAUSA", CENTRO_X, 255, 34, Estilo.DORADO);
        Texto.centrado(g, "El reloj está detenido", CENTRO_X, 278, 13, Estilo.TEXTO_SUAVE);
        continuar.dibujar(g, entrada);
        salir.dibujar(g, entrada);
    }
}
