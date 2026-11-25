package juego.reglas;

/** Un cliente generoso: suma dinero al turno. */
public class Propina implements Evento {

    private static final int MINIMO = 20;
    private static final int VARIACION = 60;

    @Override
    public void aplicar(Partida partida) {
        int monto = MINIMO + partida.getAzar().nextInt(VARIACION);
        partida.agregarDinero(monto);
        partida.setMensaje("Propina: +$" + monto);
    }

    @Override
    public String descripcion() {
        return "Propina inesperada";
    }
}
