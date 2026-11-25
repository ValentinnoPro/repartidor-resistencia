package juego.pantallas;

import estructuras.Lista;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import juego.reglas.EntradaRanking;
import juego.reglas.Vehiculo;
import motor.Entrada;
import motor.Escena;
import ui.Texto;

/** Menú principal: elegir vehículo, ver el ranking y los avisos de carga. */
public class EscenaMenu extends Escena {

    private final Sesion sesion;

    public EscenaMenu(Sesion sesion) {
        if (sesion == null) {
            throw new IllegalArgumentException("la sesión no puede ser nula");
        }
        this.sesion = sesion;
    }

    @Override
    public void actualizar(double dt) {
        Entrada entrada = getEntrada();
        if (entrada.fuePresionada(KeyEvent.VK_1)) {
            sesion.setTipoVehiculo(0);
        }
        if (entrada.fuePresionada(KeyEvent.VK_2)) {
            sesion.setTipoVehiculo(1);
        }
        if (entrada.fuePresionada(KeyEvent.VK_3)) {
            sesion.setTipoVehiculo(2);
        }
        if (entrada.fuePresionada(KeyEvent.VK_ENTER)) {
            getContexto().irA(new EscenaMapa(sesion, sesion.nuevaPartida()));
        }
        if (entrada.fuePresionada(KeyEvent.VK_T)) {
            getContexto().irA(new EscenaTaller(sesion));
        }
        if (entrada.fuePresionada(KeyEvent.VK_ESCAPE)) {
            getContexto().salir();
        }
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        g.setColor(new Color(20, 24, 36));
        g.fillRect(0, 0, ancho, alto);

        Texto.centrado(g, "REPARTIDOR EN RESISTENCIA", ancho / 2.0, 80, 40, new Color(255, 190, 70));
        Texto.centrado(g, "Entregá a tiempo por las calles del centro de Resistencia, Chaco", ancho / 2.0, 112, 16,
                new Color(190, 195, 215));

        // Vehículos
        Texto.izquierda(g, "Elegí tu vehículo (teclas 1, 2, 3)", 80, 175, 18, Color.WHITE);
        for (int i = 0; i < Sesion.VEHICULOS; i++) {
            Vehiculo v = sesion.crearVehiculo(i);
            boolean elegido = i == sesion.getTipoVehiculo();
            Color color = elegido ? new Color(110, 220, 150) : new Color(150, 155, 175);
            Texto.izquierda(g, (elegido ? "> " : "  ") + (i + 1) + ". " + v.getNombre(), 80, 210 + i * 30, 18, color);
            Texto.izquierda(g, v.descripcion(), 270, 210 + i * 30, 14, color);
        }

        // Ranking
        Texto.izquierda(g, "Mejores cadetes", 80, 340, 18, Color.WHITE);
        Lista<EntradaRanking> filas = sesion.getRanking().inOrden();
        if (filas.estaVacia()) {
            Texto.izquierda(g, "Todavía no hay entregas registradas", 80, 370, 14, new Color(150, 155, 175));
        }
        for (int i = 0; i < Math.min(5, filas.tamanio()); i++) {
            EntradaRanking e = filas.obtener(i);
            Texto.izquierda(g, (i + 1) + ". " + e.getNombre(), 80, 370 + i * 24, 15, new Color(220, 225, 240));
            Texto.derecha(g, e.getPuntaje() + " pts  (" + e.getEntregas() + " entregas)", 460, 370 + i * 24, 15,
                    new Color(220, 225, 240));
        }

        // Avisos de carga (archivos ausentes o dañados)
        Lista<persistencia.ErrorCarga> avisos = sesion.getAvisos();
        if (!avisos.estaVacia()) {
            Texto.izquierda(g, "Avisos", 560, 340, 18, new Color(255, 190, 70));
            int primero = Math.max(0, avisos.tamanio() - 6);
            for (int i = primero; i < avisos.tamanio(); i++) {
                String texto = avisos.obtener(i).toString();
                if (texto.length() > 62) {
                    texto = texto.substring(0, 59) + "...";
                }
                Texto.izquierda(g, texto, 560, 370 + (i - primero) * 22, 12, new Color(235, 200, 140));
            }
        }

        Texto.centrado(g, "ENTER: empezar el turno     T: taller     ESC: salir", ancho / 2.0, alto - 30, 16,
                new Color(190, 195, 215));
    }
}
