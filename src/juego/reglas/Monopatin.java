package juego.reglas;

/** Monopatín eléctrico: velocidad intermedia (80 % del tiempo base) y lluvia +40 %. */
public class Monopatin extends Vehiculo {

    private static final double VELOCIDAD = 0.80;
    private static final double RECARGO_LLUVIA = 1.40;

    public Monopatin() {
        super("Monopatín");
    }

    @Override
    public double tiempoTramo(double pesoBase, boolean lloviendo) {
        double tiempo = pesoBase * VELOCIDAD * getFactorMejoras();
        return lloviendo ? tiempo * RECARGO_LLUVIA : tiempo;
    }

    @Override
    public String descripcion() {
        return "Intermedio: más veloz que la bici, lluvia +40 %";
    }
}
