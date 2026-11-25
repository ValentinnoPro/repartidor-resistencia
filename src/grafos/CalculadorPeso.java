package grafos;

/**
 * Permite que Dijkstra use pesos "efectivos" (con lluvia, vehículo, mejoras)
 * sin que el paquete grafos conozca al juego: el juego pasa su propia
 * implementación y grafos solo ve esta interfaz.
 */
public interface CalculadorPeso<T> {

    double peso(T origen, T destino, double pesoBase);

    /** Calculador que usa el peso tal como está guardado en la arista. */
    static <T> CalculadorPeso<T> base() {
        return (origen, destino, pesoBase) -> pesoBase;
    }
}
