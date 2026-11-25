package juego.pantallas;

import arboles.ABB;
import estructuras.Lista;
import estructuras.ListaEnlazada;
import grafos.Grafo;
import java.util.Random;
import juego.reglas.Bicicleta;
import juego.reglas.Corpus;
import juego.reglas.EntradaRanking;
import juego.reglas.Esquina;
import juego.reglas.Monopatin;
import juego.reglas.Moto;
import juego.reglas.Partida;
import juego.reglas.Taller;
import juego.reglas.Vehiculo;
import markov.ModeloMarkov;
import markov.RetrocesoDeContexto;
import persistencia.ArchivoCorpus;
import persistencia.ArchivoMapa;
import persistencia.ArchivoMejoras;
import persistencia.ArchivoRanking;
import persistencia.ErrorCarga;

/**
 * Lo que vive entre escenas: el mapa, el taller, el ranking, los modelos de
 * Markov entrenados y los avisos de carga. Cada escena recibe la Sesion por
 * constructor (el motor no sabe nada de ella).
 */
public class Sesion {

    public static final int VEHICULOS = 3;

    // Atributos
    private final String rutaRanking;
    private final String rutaMejoras;
    private final Grafo<Esquina> mapa;
    private final Taller taller;
    private final ABB<EntradaRanking> ranking;
    private final ModeloMarkov modeloNombres;
    private final ModeloMarkov modeloReclamos;
    private final ModeloMarkov modeloRadio;
    private final Lista<ErrorCarga> avisos;
    private final Random azar = new Random();
    private int tipoVehiculo;

    private Sesion(String carpeta) {
        avisos = new ListaEnlazada<>();
        rutaRanking = carpeta + "ranking.txt";
        rutaMejoras = carpeta + "mejoras.txt";
        mapa = ArchivoMapa.cargar(carpeta + "mapa.txt", avisos);
        taller = ArchivoMejoras.cargar(rutaMejoras, avisos);
        ranking = ArchivoRanking.cargar(rutaRanking, avisos);
        Corpus corpus = ArchivoCorpus.cargar(carpeta + "corpus_clientes.txt", avisos);

        modeloNombres = new ModeloMarkov(3, new RetrocesoDeContexto());
        modeloNombres.setValorPorDefecto("Cliente Anonimo");
        modeloNombres.entrenar(corpus.getNombres());
        modeloReclamos = new ModeloMarkov(4, new RetrocesoDeContexto());
        modeloReclamos.setValorPorDefecto("Esto es un desastre");
        modeloReclamos.entrenar(corpus.getReclamos());
        modeloRadio = new ModeloMarkov(4, new RetrocesoDeContexto());
        modeloRadio.setValorPorDefecto("Radio Resistencia, siempre con vos");
        modeloRadio.entrenar(corpus.getRadio());
    }

    /** Carga todos los datos de la carpeta (con "/" al final), tolerando archivos ausentes o rotos. */
    public static Sesion cargar(String carpeta) {
        return new Sesion(carpeta);
    }

    // Vehículo elegido en el menú (0 bici, 1 moto, 2 monopatín)
    public Vehiculo crearVehiculo(int tipo) {
        Vehiculo vehiculo;
        switch (tipo) {
            case 1:
                vehiculo = new Moto();
                break;
            case 2:
                vehiculo = new Monopatin();
                break;
            default:
                vehiculo = new Bicicleta();
                break;
        }
        return vehiculo;
    }

    public void setTipoVehiculo(int tipo) {
        if (tipo < 0 || tipo >= VEHICULOS) {
            throw new IllegalArgumentException("vehículo inexistente: " + tipo);
        }
        this.tipoVehiculo = tipo;
    }

    public int getTipoVehiculo() {
        return tipoVehiculo;
    }

    /** Arma una partida nueva con el vehículo elegido y las mejoras compradas. */
    public Partida nuevaPartida() {
        Vehiculo vehiculo = crearVehiculo(tipoVehiculo);
        vehiculo.setFactorMejoras(taller.getFactorVelocidad());
        return new Partida(mapa, vehiculo, mapa.vertice(0), modeloNombres, modeloReclamos, azar);
    }

    // Persistencia: los errores de guardado se muestran como avisos en el menú
    public void guardarRanking() {
        ArchivoRanking.guardar(rutaRanking, ranking, avisos);
    }

    public void guardarTaller() {
        ArchivoMejoras.guardar(rutaMejoras, taller, avisos);
    }

    // Consultas
    public Grafo<Esquina> getMapa() {
        return mapa;
    }

    public Taller getTaller() {
        return taller;
    }

    public ABB<EntradaRanking> getRanking() {
        return ranking;
    }

    public ModeloMarkov getModeloRadio() {
        return modeloRadio;
    }

    public Lista<ErrorCarga> getAvisos() {
        return avisos;
    }

    public Random getAzar() {
        return azar;
    }
}
