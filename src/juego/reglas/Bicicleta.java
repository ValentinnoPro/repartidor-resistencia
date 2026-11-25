package juego.reglas;

/** Bicicleta: velocidad de referencia; con lluvia va un 30 % más lenta. */
public class Bicicleta extends Vehiculo {

    private static final double RECARGO_LLUVIA = 1.30;

    public Bicicleta() {
        super("Bicicleta");
    }

    @Override
    public double tiempoTramo(double pesoBase, boolean lloviendo) {
        double tiempo = pesoBase * getFactorMejoras();
        return lloviendo ? tiempo * RECARGO_LLUVIA : tiempo;
    }

    @Override
    public String descripcion() {
        return "Estable: la lluvia la frena poco (+30 %)";
    }
}
