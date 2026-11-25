package juego.reglas;

import estructuras.Lista;
import grafos.Arista;
import grafos.Conectividad;
import grafos.Grafo;

/**
 * Corta una calle (borra la arista en ambos sentidos) y recalcula las
 * componentes conexas. La decisión usa el grafo de verdad: solo se corta una
 * calle si el jugador conserva al menos la mitad del mapa; los pedidos que
 * quedan en barrios aislados se cancelan sin penalización.
 */
public class CorteDeCalle implements Evento {

    private static final int INTENTOS = 12;

    @Override
    public void aplicar(Partida partida) {
        Grafo<Esquina> grafo = partida.getGrafo();
        Lista<Esquina> esquinas = grafo.vertices();
        for (int intento = 0; intento < INTENTOS; intento++) {
            Esquina origen = esquinas.obtener(partida.getAzar().nextInt(esquinas.tamanio()));
            Lista<Arista<Esquina>> vecinos = grafo.vecinos(origen);
            if (vecinos.estaVacia()) {
                continue;
            }
            Esquina destino = vecinos.obtener(partida.getAzar().nextInt(vecinos.tamanio())).getDestino();
            double pesoIda = grafo.getPeso(origen, destino);
            boolean habiaVuelta = grafo.existeArista(destino, origen);
            double pesoVuelta = habiaVuelta ? grafo.getPeso(destino, origen) : 0;

            grafo.eliminarArista(origen, destino);
            grafo.eliminarArista(destino, origen);

            Lista<Esquina> alcance = Conectividad.componenteDe(grafo, partida.getActual());
            if (alcance.tamanio() * 2 >= grafo.cantidadVertices()) {
                int cancelados = partida.recalcularConectividad();
                String extra = cancelados > 0 ? " (" + cancelados + " pedido(s) cancelado(s))" : "";
                partida.setMensaje("Corte: " + origen.getNombre() + " - " + destino.getNombre() + extra);
                return;
            }
            // Dejaba al jugador aislado: se deshace el corte y se prueba otra calle
            grafo.agregarArista(origen, destino, pesoIda);
            if (habiaVuelta) {
                grafo.agregarArista(destino, origen, pesoVuelta);
            }
        }
        partida.setMensaje("Aviso de corte de calle, pero no se concretó");
    }

    @Override
    public String descripcion() {
        return "Corte de calle";
    }
}
