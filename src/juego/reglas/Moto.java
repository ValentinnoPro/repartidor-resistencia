package juego.reglas;

/** Moto: la más rápida (60 % del tiempo base), pero con lluvia va un 50 % más lenta. */
public class Moto extends Vehiculo {

    private static final double VELOCIDAD = 0.60;
    private static final double RECARGO_LLUVIA = 1.50;

    public Moto() {
        super("Moto");
    }

    @Override
    public double tiempoTramo(double pesoBase, boolean lloviendo) {
        double tiempo = pesoBase * VELOCIDAD * getFactorMejoras();
        return lloviendo ? tiempo * RECARGO_LLUVIA : tiempo;
    }

    @Override
    public String descripcion() {
        return "Rápida, pero la lluvia la frena mucho (+50 %)";
    }
}
