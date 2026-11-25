package markov;

import java.util.Random;

/** Estrategia alternativa: termina la cadena como si hubiera aparecido el marcador de fin. */
public class CorteConFin implements EstrategiaSinSucesores {

    @Override
    public char resolver(ModeloMarkov modelo, String estado, Random azar) {
        return ModeloMarkov.FIN;
    }

    @Override
    public String descripcion() {
        return "corte con fin";
    }
}
