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

/** Fin del turno: puntaje que sube, estrellas, resumen y alta en el ranking (todo con mouse o teclado). */
public class EscenaFinTurno extends Escena {

    private static final int PUNTOS_DOS_ESTRELLAS = 600;
    private static final int PUNTOS_TRES_ESTRELLAS = 1200;

    private final Sesion sesion;
    private final Partida partida;
    private final CajaTexto nombre = new CajaTexto(EntradaRanking.LARGO_MAXIMO_NOMBRE);
    private final Efectos efectos = new Efectos();
    private final Boton guardar = new Boton(396, 524, 288, 46, "GUARDAR EN EL RANKING", "ENTER", Estilo.VERDE);
    private final Boton alTaller = new Boton(300, 524, 220, 46, "IR AL TALLER", "ENTER", Estilo.DORADO);
    private final Boton alMenu = new Boton(532, 524, 130, 46, "MENÚ", "ESC", new Color(90, 100, 135));
    private final Boton otraVez = new Boton(674, 524, 170, 46, "OTRO TURNO", "R", Estilo.VERDE);
    private boolean guardado;
    private double edad;
    private boolean festejado;

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

    private int estrellas() {
        if (partida.getPuntaje() >= PUNTOS_TRES_ESTRELLAS) {
            return 3;
        }
        if (partida.getPuntaje() >= PUNTOS_DOS_ESTRELLAS) {
            return 2;
        }
        return partida.getPuntaje() > 0 ? 1 : 0;
    }

    @Override
    public void actualizar(double dt) {
        Entrada entrada = getEntrada();
        edad += dt;
        efectos.actualizar(dt);
        if (!festejado && edad > 1.9 && estrellas() >= 2) {
            festejado = true;
            efectos.confeti(540, 190, 90);
            efectos.confeti(300, 230, 40);
            efectos.confeti(780, 230, 40);
        }
        if (!guardado) {
            nombre.procesar(entrada);
            if (entrada.fuePresionada(KeyEvent.VK_ENTER) || guardar.fuePulsado(entrada)) {
                registrar();
            }
        } else {
            if (entrada.fuePresionada(KeyEvent.VK_ENTER) || alTaller.fuePulsado(entrada)) {
                getContexto().irA(new EscenaTaller(sesion));
            } else if (entrada.fuePresionada(KeyEvent.VK_ESCAPE) || alMenu.fuePulsado(entrada)) {
                getContexto().irA(new EscenaMenu(sesion));
            } else if (entrada.fuePresionada(KeyEvent.VK_R) || otraVez.fuePulsado(entrada)) {
                getContexto().irA(new EscenaMapa(sesion, sesion.nuevaPartida()));
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
        Entrada entrada = getEntrada();
        Estilo.fondo(g, ancho, alto);
        double centro = ancho / 2.0;

        Estilo.sombreadoCentrado(g, "FIN DEL TURNO", centro, 70, 34, Estilo.DORADO);

        // El puntaje sube hasta su valor final
        double avance = Math.min(1.0, edad / 1.4);
        double suave = 1 - (1 - avance) * (1 - avance);
        int mostrado = (int) Math.round(partida.getPuntaje() * suave);
        Estilo.sombreadoCentrado(g, mostrado + " puntos", centro, 136, 52,
                partida.getPuntaje() < 0 ? Estilo.ROJO : Estilo.TEXTO);

        // Estrellas que aparecen una por una
        int ganadas = estrellas();
        for (int i = 0; i < 3; i++) {
            double aparece = Math.max(0, Math.min(1.0, (edad - 1.5 - i * 0.3) / 0.25));
            double x = centro + (i - 1) * 60;
            Estilo.estrella(g, x, 190, 24, new Color(60, 68, 96));
            if (i < ganadas && aparece > 0) {
                Estilo.estrella(g, x, 190, 24 * (0.6 + 0.4 * aparece) + (1 - aparece) * 6, Estilo.DORADO);
            }
        }

        dibujarResumen(g, centro);

        if (!guardado) {
            Texto.centrado(g, "TU NOMBRE PARA EL RANKING", centro, 462, 12, Estilo.TEXTO_SUAVE);
            Estilo.panel(g, centro - 160, 472, 320, 40, 12, Estilo.PANEL_CLARO, Estilo.AZUL);
            boolean cursor = Estilo.pulso(edad, 1.2) > 0.5;
            Texto.centrado(g, nombre.getTexto().isEmpty() ? "Cadete" : nombre.getTexto() + (cursor ? "|" : ""), centro, 499, 20,
                    nombre.getTexto().isEmpty() ? Estilo.TEXTO_SUAVE : Estilo.TEXTO);
            guardar.dibujar(g, entrada);
        } else {
            Texto.centrado(g, "Guardado en el ranking", centro, 480, 18, Estilo.VERDE);
            alTaller.dibujar(g, entrada);
            alMenu.dibujar(g, entrada);
            otraVez.dibujar(g, entrada);
        }
        efectos.dibujar(g);
    }

    private void dibujarResumen(Graphics2D g, double centro) {
        int x = (int) centro - 230;
        Estilo.panel(g, x, 232, 460, 212, 20, Estilo.PANEL, Estilo.BORDE);
        fila(g, x, 264, "Entregas a tiempo", String.valueOf(partida.getEntregas()), Estilo.VERDE);
        fila(g, x, 296, "Pedidos vencidos", String.valueOf(partida.getVencidos()),
                partida.getVencidos() > 0 ? Estilo.ROJO : Estilo.TEXTO);
        fila(g, x, 328, "Eficiencia de rutas", Math.round(partida.getEficiencia() * 100) + " %", Estilo.AZUL);
        // Barra de eficiencia
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(x + 24, 336, 412, 6, 6, 6);
        g.setColor(Estilo.AZUL);
        g.fillRoundRect(x + 24, 336, (int) (412 * partida.getEficiencia()), 6, 6, 6);
        fila(g, x, 372, "Dinero ganado", "$" + partida.getDinero(), Estilo.VERDE);
        fila(g, x, 404, "Ahorro en el taller", "$" + sesion.getTaller().getDinero(), Estilo.VERDE);
        Texto.centrado(g, "2 estrellas: " + PUNTOS_DOS_ESTRELLAS + " pts   ·   3 estrellas: " + PUNTOS_TRES_ESTRELLAS + " pts",
                centro, 432, 11, Estilo.TEXTO_SUAVE);
    }

    private void fila(Graphics2D g, int x, int y, String etiqueta, String valor, Color color) {
        Texto.izquierda(g, etiqueta, x + 24, y, 16, new Color(190, 198, 222));
        Texto.derecha(g, valor, x + 436, y, 18, color);
    }
}
