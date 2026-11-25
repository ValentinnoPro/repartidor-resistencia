package juego.reglas;

/** Empieza a llover un rato: el vehículo se frena y algunas calles se anegan (pesan más). */
public class Lluvia implements Evento {

    private static final double DURACION_MINUTOS = 3.0;

    @Override
    public void aplicar(Partida partida) {
        partida.iniciarLluvia(DURACION_MINUTOS);
        partida.setMensaje(descripcion());
    }

    @Override
    public String descripcion() {
        return "Lluvia: calles anegadas y vehículo más lento";
    }
}
