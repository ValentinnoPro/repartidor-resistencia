package markov;

import java.util.Random;

/**
 * Qué hacer cuando un estado no tiene sucesores en la tabla (por ejemplo, un
 * contexto de 4 letras que apareció una sola vez justo al final de una línea).
 * Devuelve el próximo carácter, o {@link ModeloMarkov#FIN} para cerrar la cadena.
 */
public interface EstrategiaSinSucesores {

    char resolver(ModeloMarkov modelo, String estado, Random azar);

    String descripcion();
}
