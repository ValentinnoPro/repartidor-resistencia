package juego.pantallas;

import estructuras.Lista;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.reglas.EntradaRanking;
import juego.reglas.Vehiculo;
import motor.Entrada;
import motor.Escena;
import persistencia.ErrorCarga;
import ui.Texto;

/**
 * Menú principal: tarjetas de vehículos para elegir con el mouse (o con 1, 2, 3 y
 * las flechas), botones para jugar, ir al taller y salir, y el ranking.
 */
public class EscenaMenu extends Escena {

    private static final int TARJETA_X = 60;
    private static final int TARJETA_Y = 200;
    private static final int TARJETA_ANCHO = 156;
    private static final int TARJETA_ALTO = 232;
    private static final int TARJETA_PASO = 172;

    private final Sesion sesion;
    private final Boton jugar = new Boton(60, 462, 232, 56, "JUGAR", "ENTER", Estilo.VERDE);
    private final Boton taller = new Boton(304, 462, 124, 56, "TALLER", "T", Estilo.DORADO);
    private final Boton salir = new Boton(440, 462, 104, 56, "SALIR", "", new Color(150, 80, 90));
    private double tiempo;

    public EscenaMenu(Sesion sesion) {
        if (sesion == null) {
            throw new IllegalArgumentException("la sesión no puede ser nula");
        }
        this.sesion = sesion;
    }

    @Override
    public void actualizar(double dt) {
        Entrada entrada = getEntrada();
        tiempo += dt;

        if (entrada.fuePresionada(KeyEvent.VK_1)) {
            sesion.setTipoVehiculo(0);
        }
        if (entrada.fuePresionada(KeyEvent.VK_2)) {
            sesion.setTipoVehiculo(1);
        }
        if (entrada.fuePresionada(KeyEvent.VK_3)) {
            sesion.setTipoVehiculo(2);
        }
        if (entrada.fuePresionada(KeyEvent.VK_LEFT) && sesion.getTipoVehiculo() > 0) {
            sesion.setTipoVehiculo(sesion.getTipoVehiculo() - 1);
        }
        if (entrada.fuePresionada(KeyEvent.VK_RIGHT) && sesion.getTipoVehiculo() < Sesion.VEHICULOS - 1) {
            sesion.setTipoVehiculo(sesion.getTipoVehiculo() + 1);
        }
        if (entrada.huboClick()) {
            for (int i = 0; i < Sesion.VEHICULOS; i++) {
                if (enTarjeta(entrada.getMouseX(), entrada.getMouseY(), i)) {
                    sesion.setTipoVehiculo(i);
                }
            }
        }

        if (entrada.fuePresionada(KeyEvent.VK_ENTER) || jugar.fuePulsado(entrada)) {
            getContexto().irA(new EscenaMapa(sesion, sesion.nuevaPartida()));
        } else if (entrada.fuePresionada(KeyEvent.VK_T) || taller.fuePulsado(entrada)) {
            getContexto().irA(new EscenaTaller(sesion));
        } else if (entrada.fuePresionada(KeyEvent.VK_ESCAPE) || salir.fuePulsado(entrada)) {
            getContexto().salir();
        }
    }

    private boolean enTarjeta(int mx, int my, int indice) {
        int x = TARJETA_X + indice * TARJETA_PASO;
        return mx >= x && mx < x + TARJETA_ANCHO && my >= TARJETA_Y && my < TARJETA_Y + TARJETA_ALTO;
    }

    // ----------------------------------------------------------------- dibujo

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        Entrada entrada = getEntrada();
        Estilo.fondo(g, ancho, alto);
        dibujarCalleAnimada(g, ancho, alto);

        Estilo.sombreadoCentrado(g, "REPARTIDOR", 330, 92, 54, Estilo.DORADO);
        Estilo.sombreadoCentrado(g, "EN RESISTENCIA", 330, 132, 28, Estilo.TEXTO);
        Texto.centrado(g, "Entregá a tiempo por las calles del centro de Resistencia, Chaco", 330, 164, 14, Estilo.TEXTO_SUAVE);

        Texto.izquierda(g, "ELEGÍ TU VEHÍCULO", TARJETA_X, 192, 12, Estilo.TEXTO_SUAVE);
        for (int i = 0; i < Sesion.VEHICULOS; i++) {
            dibujarTarjeta(g, i, entrada);
        }

        jugar.dibujar(g, entrada);
        taller.dibujar(g, entrada);
        salir.dibujar(g, entrada);

        dibujarAvisos(g);
        dibujarRanking(g, ancho);
        Texto.centrado(g, "Click en un vehículo y JUGAR   ·   1 2 3 / flechas: elegir   ·   ENTER: jugar   ·   T: taller   ·   ESC: salir",
                ancho / 2.0, alto - 22, 12, Estilo.TEXTO_SUAVE);
    }

    // Una calle de fondo con repartidores que cruzan la pantalla (posición = función del tiempo)
    private void dibujarCalleAnimada(Graphics2D g, int ancho, int alto) {
        for (int k = 0; k < 4; k++) {
            int y = 40 + k * 190;
            g.setColor(new Color(255, 255, 255, 10));
            g.fillRect(0, y - 14, ancho, 28);
            g.setColor(new Color(255, 255, 255, 18));
            g.setStroke(new BasicStroke(2f));
            for (int x = 0; x < ancho; x += 40) {
                g.drawLine(x, y, x + 18, y);
            }
        }
        for (int i = 0; i < 7; i++) {
            int fila = i % 4;
            boolean haciaIzquierda = i % 2 == 1;
            double velocidad = 40 + (i * 23) % 50;
            double recorrido = (tiempo * velocidad + i * 211) % (ancho + 160) - 80;
            double x = haciaIzquierda ? ancho - recorrido : recorrido;
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.55f));
            DibujoVehiculo.dibujar(g, i % 3, x, 40 + fila * 190, haciaIzquierda ? Math.PI : 0, 0.9, tiempo + i, true);
            g.setComposite(java.awt.AlphaComposite.SrcOver);
        }
    }

    private void dibujarTarjeta(Graphics2D g, int indice, Entrada entrada) {
        Vehiculo v = sesion.crearVehiculo(indice);
        boolean elegido = indice == sesion.getTipoVehiculo();
        boolean encima = enTarjeta(entrada.getMouseX(), entrada.getMouseY(), indice);
        int x = TARJETA_X + indice * TARJETA_PASO;
        int y = TARJETA_Y - (encima && !elegido ? 4 : 0);

        if (elegido) {
            g.setColor(Estilo.alfa(Estilo.VERDE, (int) (40 + 40 * Estilo.pulso(tiempo, 0.9))));
            g.fillRoundRect(x - 6, y - 6, TARJETA_ANCHO + 12, TARJETA_ALTO + 12, 26, 26);
        }
        Estilo.panel(g, x, y, TARJETA_ANCHO, TARJETA_ALTO, 20, elegido ? Estilo.PANEL_CLARO : Estilo.PANEL,
                elegido ? Estilo.VERDE : (encima ? Estilo.TEXTO_SUAVE : Estilo.BORDE));

        // Tecla de atajo
        g.setColor(new Color(0, 0, 0, 70));
        g.fillRoundRect(x + 10, y + 10, 24, 24, 8, 8);
        Texto.centrado(g, String.valueOf(indice + 1), x + 22, y + 28, 14, elegido ? Estilo.VERDE : Estilo.TEXTO_SUAVE);

        // Vehículo girando despacio
        double giro = elegido ? tiempo * 1.6 : -0.5;
        DibujoVehiculo.dibujar(g, indice, x + TARJETA_ANCHO / 2.0, y + 78, giro, 2.1, tiempo, elegido);

        Texto.centrado(g, v.getNombre(), x + TARJETA_ANCHO / 2.0, y + 134, 17, elegido ? Estilo.TEXTO : new Color(205, 212, 235));

        // Estadísticas calculadas con el propio polimorfismo del vehículo
        double base = v.tiempoTramo(1.0, false);
        double lluvia = v.tiempoTramo(1.0, true);
        barra(g, "VELOCIDAD", x + 14, y + 154, TARJETA_ANCHO - 28, Math.min(1.0, 0.5 / base), Estilo.AZUL);
        barra(g, "BAJO LLUVIA", x + 14, y + 190, TARJETA_ANCHO - 28, Math.max(0, Math.min(1.0, 1.0 - (lluvia / base - 1.0))),
                new Color(120, 150, 255));
    }

    private void barra(Graphics2D g, String etiqueta, int x, int y, int ancho, double fraccion, Color color) {
        Texto.izquierda(g, etiqueta, x, y + 2, 10, Estilo.TEXTO_SUAVE);
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(x, y + 9, ancho, 8, 8, 8);
        g.setColor(color);
        g.fillRoundRect(x, y + 9, (int) (ancho * fraccion), 8, 8, 8);
    }

    private void dibujarAvisos(Graphics2D g) {
        Lista<ErrorCarga> avisos = sesion.getAvisos();
        if (avisos.estaVacia()) {
            return;
        }
        Texto.izquierda(g, "AVISOS", 60, 556, 12, Estilo.DORADO);
        int primero = Math.max(0, avisos.tamanio() - 3);
        for (int i = primero; i < avisos.tamanio(); i++) {
            Texto.izquierda(g, Estilo.recortar(avisos.obtener(i).toString(), 80), 60, 578 + (i - primero) * 18, 12,
                    new Color(235, 200, 140));
        }
    }

    private void dibujarRanking(Graphics2D g, int ancho) {
        int x = 610;
        int panelAncho = ancho - x - 50;
        Estilo.panel(g, x, 150, panelAncho, 450, 20, Estilo.PANEL, Estilo.BORDE);
        Texto.izquierda(g, "MEJORES CADETES", x + 24, 186, 16, Estilo.DORADO);
        Lista<EntradaRanking> filas = sesion.getRanking().inOrden();
        if (filas.estaVacia()) {
            Texto.centrado(g, "Todavía no hay entregas registradas", x + panelAncho / 2.0, 330, 14, Estilo.TEXTO_SUAVE);
        }
        for (int i = 0; i < Math.min(8, filas.tamanio()); i++) {
            EntradaRanking e = filas.obtener(i);
            int y = 214 + i * 46;
            Estilo.panel(g, x + 14, y, panelAncho - 28, 38, 12, i == 0 ? new Color(56, 50, 36) : Estilo.PANEL_CLARO, null);
            Color medalla = i == 0 ? Estilo.DORADO : i == 1 ? new Color(200, 208, 225) : i == 2 ? new Color(210, 140, 90) : null;
            if (medalla != null) {
                g.setColor(medalla);
                g.fillOval(x + 24, y + 7, 24, 24);
                Texto.centrado(g, String.valueOf(i + 1), x + 36, y + 25, 14, new Color(30, 24, 20));
            } else {
                Texto.centrado(g, String.valueOf(i + 1), x + 36, y + 25, 14, Estilo.TEXTO_SUAVE);
            }
            Texto.izquierda(g, Estilo.recortar(e.getNombre(), 14), x + 62, y + 18, 14, Estilo.TEXTO);
            Texto.izquierda(g, e.getEntregas() + " entregas", x + 62, y + 32, 10, Estilo.TEXTO_SUAVE);
            Texto.derecha(g, e.getPuntaje() + " pts", x + panelAncho - 28, y + 25, 15,
                    e.getPuntaje() < 0 ? Estilo.ROJO : Estilo.TEXTO);
        }
    }
}
