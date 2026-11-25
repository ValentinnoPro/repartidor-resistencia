package markov;

import java.util.Random;

/**
 * Estrategia por defecto: si el contexto de k caracteres no tiene sucesores,
 * prueba con los últimos k-1, k-2, ..., 1 caracteres usando los modelos de
 * orden menor, que se entrenaron junto con el principal. Si ni siquiera el de
 * orden 1 conoce el estado, cierra la cadena.
 */
public class RetrocesoDeContexto implements EstrategiaSinSucesores {

    @Override
    public char resolver(ModeloMarkov modelo, String estado, Random azar) {
        ModeloMarkov menor = modelo.getMenor();
        while (menor != null) {
            String contexto = estado.substring(estado.length() - menor.getOrden());
            Character sucesor = menor.sucesorDe(contexto, azar);
            if (sucesor != null) {
                return sucesor;
            }
            menor = menor.getMenor();
        }
        return ModeloMarkov.FIN;
    }

    @Override
    public String descripcion() {
        return "retroceso de contexto";
    }
}
