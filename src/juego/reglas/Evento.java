package juego.reglas;

/**
 * Algo que le pasa al turno (lluvia, corte de calle, propina). La Partida
 * sortea un Evento y llama a {@link #aplicar} sin saber cuál es: cada clase
 * decide qué hacer, así no hace falta preguntarle su tipo a cada evento.
 */
public interface Evento {

    void aplicar(Partida partida);

    String descripcion();
}
