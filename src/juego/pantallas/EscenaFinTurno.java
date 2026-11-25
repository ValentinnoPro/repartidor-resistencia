package juego.pantallas;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import juego.reglas.EntradaRanking;
import juego.reglas.Partida;
import motor.Entrada;
import motor.Escena;
import ui.Texto;

/** Fin del turno: resumen de puntaje, entregas y eficiencia, y alta en el ranking. */
public class EscenaFinTurno extends Escena {

    private final Sesion sesion;
    private final Partida partida;
    private final CajaTexto nombre = new CajaTexto(EntradaRanking.LARGO_MAXIMO_NOMBRE);
    private boolean guardado;

    public EscenaFinTurno(Sesion sesion, Partida partida) {
        if (sesion == null || partida == null) {
            throw new IllegalArgumentException("la escena necesita sesión y partida");
        }
        this.sesion = sesion;
        this.partida = partida;
    }

    // La plata ganada pasa al taller y se guarda apenas termina el turno
    @Override
    public void alEntrar() {
        sesion.getTaller().depositar(partida.getDinero());
        sesion.guardarTaller();
    }

    @Override
    public void actualizar(double dt) {
        Entrada entrada = getEntrada();
        if (!guardado) {
            nombre.procesar(entrada);
            if (entrada.fuePresionada(KeyEvent.VK_ENTER)) {
                registrar();
            }
        } else {
            if (entrada.fuePresionada(KeyEvent.VK_ENTER)) {
                getContexto().irA(new EscenaTaller(sesion));
            }
            if (entrada.fuePresionada(KeyEvent.VK_ESCAPE)) {
                getContexto().irA(new EscenaMenu(sesion));
            }
        }
    }

    private void registrar() {
        String texto = nombre.getTexto().trim();
        String cadete = texto.isEmpty() ? "Cadete" : Formato.mayuscula(texto);
        sesion.getRanking().insertar(new EntradaRanking(cadete, partida.getPuntaje(), partida.getEntregas(),
                LocalDate.now().toString()));
        sesion.guardarRanking();
        guardado = true;
    }

    @Override
    public void dibujar(Graphics2D g) {
        int ancho = getContexto().getAncho();
        int alto = getContexto().getAlto();
        g.setColor(new Color(20, 24, 36));
        g.fillRect(0, 0, ancho, alto);

        Texto.centrado(g, "FIN DEL TURNO", ancho / 2.0, 90, 38, new Color(255, 190, 70));
        Texto.centrado(g, partida.getPuntaje() + " puntos", ancho / 2.0, 160, 44, Color.WHITE);

        int x = ancho / 2 - 190;
        Texto.izquierda(g, "Entregas a tiempo", x, 230, 20, new Color(190, 195, 215));
        Texto.derecha(g, String.valueOf(partida.getEntregas()), x + 380, 230, 20, Color.WHITE);
        Texto.izquierda(g, "Pedidos vencidos", x, 262, 20, new Color(190, 195, 215));
        Texto.derecha(g, String.valueOf(partida.getVencidos()), x + 380, 262, 20, Color.WHITE);
        Texto.izquierda(g, "Eficiencia de rutas", x, 294, 20, new Color(190, 195, 215));
        Texto.derecha(g, Math.round(partida.getEficiencia() * 100) + " %", x + 380, 294, 20, Color.WHITE);
        Texto.izquierda(g, "Dinero ganado", x, 326, 20, new Color(190, 195, 215));
        Texto.derecha(g, "$" + partida.getDinero(), x + 380, 326, 20, new Color(120, 230, 150));
        Texto.izquierda(g, "Ahorro en el taller", x, 358, 20, new Color(190, 195, 215));
        Texto.derecha(g, "$" + sesion.getTaller().getDinero(), x + 380, 358, 20, new Color(120, 230, 150));

        if (!guardado) {
            Texto.centrado(g, "Tu nombre para el ranking:", ancho / 2.0, 430, 18, Color.WHITE);
            g.setColor(new Color(50, 58, 88));
            g.fillRoundRect(ancho / 2 - 140, 445, 280, 38, 10, 10);
            boolean cursor = (System.currentTimeMillis() / 400) % 2 == 0;
            Texto.centrado(g, nombre.getTexto() + (cursor ? "|" : ""), ancho / 2.0, 472, 20, Color.WHITE);
            Texto.centrado(g, "ENTER: guardar", ancho / 2.0, alto - 40, 16, new Color(190, 195, 215));
        } else {
            Texto.centrado(g, "Guardado en el ranking", ancho / 2.0, 440, 20, new Color(120, 230, 150));
            Texto.centrado(g, "ENTER: ir al taller     ESC: menú", ancho / 2.0, alto - 40, 16, new Color(190, 195, 215));
        }
    }
}
