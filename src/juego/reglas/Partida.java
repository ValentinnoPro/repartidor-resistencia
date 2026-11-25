package juego.reglas;

import arboles.ABB;
import estructuras.Cola;
import estructuras.ColaEnlazada;
import estructuras.Lista;
import estructuras.ListaEnlazada;
import estructuras.Pila;
import estructuras.PilaEnlazada;
import grafos.CalculadorPeso;
import grafos.Conectividad;
import grafos.Dijkstra;
import grafos.Grafo;
import grafos.Recorridos;
import grafos.ResultadoCamino;
import java.util.Random;
import markov.ModeloMarkov;
import motor.Temporizador;
import tokens.Tokenizador;

/**
 * Un turno de trabajo. Reúne las estructuras que deciden el juego:
 *
 * - Cola de pedidos entrantes: se atienden en orden de llegada (FIFO).
 * - ABB de pedidos pendientes: ordenado por hora límite, el mínimo es el más
 *   urgente, entregar es eliminar su nodo y el HUD muestra el in-orden.
 * - Pila de tramos: deshacer el último movimiento (LIFO).
 * - Cola con la ruta automática ("ir hacia" una esquina escrita).
 * - Grafo: el mapa; Dijkstra da el GPS y el tiempo óptimo para puntuar;
 *   las componentes conexas deciden qué pedidos se cancelan al cortar una calle.
 *
 * El tiempo está en MINUTOS DE JUEGO; cada minuto dura SEGUNDOS_POR_MINUTO
 * segundos reales.
 */
public class Partida implements CalculadorPeso<Esquina> {

    // Constantes de juego
    public static final double DURACION_TURNO = 12.0;
    public static final double SEGUNDOS_POR_MINUTO = 6.0;
    public static final int MAX_ENTRANTES = 5;
    public static final int PUNTOS_ENTREGA = 100;
    public static final int PENALIDAD_GPS = 15;
    public static final int PENALIDAD_VENCIDO = 50;
    public static final double COSTO_DESHACER = 0.5;
    private static final double RECARGO_CALLE_ANEGADA = 1.4;
    private static final int RADIO_PEDIDOS = 5;

    // Mundo
    private final Grafo<Esquina> grafo;
    private final Vehiculo vehiculo;
    private final Tokenizador tokenizador = new Tokenizador();
    private final Random azar;
    private final ModeloMarkov modeloNombres;
    private final ModeloMarkov modeloReclamos;
    private final Lista<Evento> eventos = new ListaEnlazada<>();

    // Estructuras del turno
    private final Cola<Pedido> entrantes = new ColaEnlazada<>();
    private final ABB<Pedido> pendientes = new ABB<>();
    private final Pila<Tramo> movimientos = new PilaEnlazada<>();
    private final Cola<Esquina> rutaAutomatica = new ColaEnlazada<>();
    private final Temporizador relojPedidos = new Temporizador(7.0);
    private final Temporizador relojEventos = new Temporizador(22.0);

    // Estado
    private Esquina actual;
    private Esquina destinoTramo;
    private Esquina origenTramo;
    private double restanteTramo;
    private double duracionTramo;
    private ResultadoCamino<Esquina> rutaGps;
    private double reloj;
    private boolean terminada;
    private boolean lloviendo;
    private double lluviaRestante;
    private int proximoIdPedido = 1;
    private String mensaje = "";
    private double mensajeRestante;

    // Puntaje
    private int puntaje;
    private int dinero;
    private int entregas;
    private int vencidos;
    private double sumaOptimo;
    private double sumaReal;

    // Constructor con validación
    public Partida(Grafo<Esquina> mapa, Vehiculo vehiculo, Esquina inicio,
                   ModeloMarkov modeloNombres, ModeloMarkov modeloReclamos, Random azar) {
        if (mapa == null || vehiculo == null || inicio == null || modeloNombres == null
                || modeloReclamos == null || azar == null) {
            throw new IllegalArgumentException("la partida necesita todos sus componentes");
        }
        if (!mapa.existeVertice(inicio)) {
            throw new IllegalArgumentException("la esquina inicial no está en el mapa");
        }
        this.grafo = mapa.copiar(); // el corte de calles no daña el mapa original
        this.vehiculo = vehiculo;
        this.actual = inicio;
        this.modeloNombres = modeloNombres;
        this.modeloReclamos = modeloReclamos;
        this.azar = azar;

        // El vocabulario del Trie son las calles del mapa
        Lista<Esquina> esquinas = grafo.vertices();
        for (int i = 0; i < esquinas.tamanio(); i++) {
            tokenizador.agregarAlVocabulario(esquinas.obtener(i).getCalle1());
            tokenizador.agregarAlVocabulario(esquinas.obtener(i).getCalle2());
        }
        eventos.agregar(new Lluvia());
        eventos.agregar(new CorteDeCalle());
        eventos.agregar(new Propina());

        generarPedido();
        generarPedido();
    }

    // Peso efectivo de una calle: base × anegamiento × vehículo (con mejoras y lluvia)
    @Override
    public double peso(Esquina origen, Esquina destino, double pesoBase) {
        double base = pesoBase;
        if (lloviendo && origen.getId() % 2 == 0) {
            base *= RECARGO_CALLE_ANEGADA;
        }
        return vehiculo.tiempoTramo(base, lloviendo);
    }

    // ---------------------------------------------------------------- avance

    public void actualizar(double dt) {
        if (terminada) {
            return;
        }
        double minutos = dt / SEGUNDOS_POR_MINUTO;
        reloj += minutos;
        if (mensajeRestante > 0) {
            mensajeRestante -= dt;
        }
        if (lloviendo) {
            lluviaRestante -= minutos;
            if (lluviaRestante <= 0) {
                lloviendo = false;
                setMensaje("Dejó de llover");
            }
        }
        if (reloj >= DURACION_TURNO) {
            reloj = DURACION_TURNO;
            terminada = true;
            return;
        }

        revisarVencidos();

        if (destinoTramo != null) {
            restanteTramo -= minutos;
            if (restanteTramo <= 0) {
                llegar();
            }
        } else if (!rutaAutomatica.estaVacia()) {
            seguirRutaAutomatica();
        }

        if (relojPedidos.avanzar(dt)) {
            generarPedido();
        }
        if (relojEventos.avanzar(dt)) {
            // Polimorfismo: no se pregunta qué evento es
            eventos.obtener(azar.nextInt(eventos.tamanio())).aplicar(this);
        }
    }

    private void seguirRutaAutomatica() {
        Esquina siguiente = rutaAutomatica.desencolar();
        try {
            iniciarTramo(siguiente);
        } catch (MovimientoInvalidoException e) {
            rutaAutomatica.vaciar(); // la calle se cortó: se abandona la ruta
            setMensaje("Ruta interrumpida: " + e.getMessage());
        }
    }

    // Vencimientos: el mínimo del ABB es siempre el más urgente
    private void revisarVencidos() {
        boolean seguir = true;
        while (seguir && !pendientes.estaVacio()) {
            Pedido urgente = pendientes.minimo();
            try {
                urgente.verificarVigente(reloj);
                seguir = false;
            } catch (PedidoVencidoException e) {
                pendientes.eliminar(urgente);
                puntaje -= PENALIDAD_VENCIDO;
                vencidos++;
                setMensaje("Reclamo de " + urgente.getCliente() + ": " + modeloReclamos.generar(azar, 60));
            }
        }
        // Los pedidos que esperan en la cola sin que los acepten también se pierden
        boolean limpiando = true;
        while (limpiando && !entrantes.estaVacia()) {
            try {
                entrantes.frente().verificarVigente(reloj);
                limpiando = false;
            } catch (PedidoVencidoException e) {
                Pedido perdido = entrantes.desencolar();
                puntaje -= PENALIDAD_VENCIDO;
                vencidos++;
                setMensaje("Se perdió el pedido de " + perdido.getCliente());
            }
        }
    }

    // ------------------------------------------------------------- pedidos

    private void generarPedido() {
        if (entrantes.tamanio() >= MAX_ENTRANTES) {
            return;
        }
        // Solo aparecen pedidos dentro del radio de cobertura (BFS limitado)
        Lista<Esquina> cercanas = Recorridos.bfsHastaDistancia(grafo, actual, RADIO_PEDIDOS);
        Lista<Esquina> candidatas = new ListaEnlazada<>();
        for (int i = 0; i < cercanas.tamanio(); i++) {
            if (!cercanas.obtener(i).equals(actual)) {
                candidatas.agregar(cercanas.obtener(i));
            }
        }
        if (candidatas.estaVacia()) {
            return;
        }
        Esquina destino = candidatas.obtener(azar.nextInt(candidatas.tamanio()));
        ResultadoCamino<Esquina> optimo = Dijkstra.caminoMinimo(grafo, actual, destino, this);
        if (!optimo.existe()) {
            return;
        }
        double limite = reloj + optimo.getCosto() * 1.6 + 1.5;
        int monto = 40 + azar.nextInt(80);
        String cliente = modeloNombres.generar(azar, 24);
        entrantes.encolar(new Pedido(proximoIdPedido++, cliente, destino, limite, monto));
    }

    /** Acepta el pedido que más hace que espera (frente de la Cola) y lo pasa al ABB. */
    public void aceptarSiguiente() throws MovimientoInvalidoException {
        if (entrantes.estaVacia()) {
            throw new MovimientoInvalidoException("No hay pedidos esperando");
        }
        Pedido pedido = entrantes.desencolar();
        ResultadoCamino<Esquina> optimo = Dijkstra.caminoMinimo(grafo, actual, pedido.getDestino(), this);
        if (!optimo.existe()) {
            throw new MovimientoInvalidoException("Ese pedido quedó inalcanzable y se descartó");
        }
        pedido.aceptar(reloj, optimo.getCosto());
        pendientes.insertar(pedido);
        setMensaje("Aceptaste a " + pedido.getCliente() + " → " + pedido.getDestino().getNombre());
        entregarEnActual();
    }

    private void entregarEnActual() {
        Lista<Pedido> lista = pendientes.inOrden();
        for (int i = 0; i < lista.tamanio(); i++) {
            Pedido pedido = lista.obtener(i);
            if (!pedido.getDestino().equals(actual)) {
                continue;
            }
            pendientes.eliminar(pedido);
            entregas++;
            double sobrante = Math.max(0, pedido.getHoraLimite() - reloj);
            int bono = (int) Math.round(sobrante * 10);
            double real = reloj - pedido.getHoraAceptado();
            double eficiencia = real <= 0.0001 ? 1.0 : Math.min(1.0, pedido.getTiempoOptimo() / real);
            int puntosEficiencia = (int) Math.round(100 * eficiencia);
            puntaje += PUNTOS_ENTREGA + bono + puntosEficiencia;
            dinero += pedido.getMonto();
            sumaOptimo += pedido.getTiempoOptimo();
            sumaReal += Math.max(real, 0);
            setMensaje("Entregado a " + pedido.getCliente() + "  +" + (PUNTOS_ENTREGA + bono + puntosEficiencia)
                    + " pts  +$" + pedido.getMonto());
        }
    }

    // ---------------------------------------------------------- movimiento

    /** Se mueve a una esquina vecina (una calle que sale de la esquina actual). */
    public void mover(Esquina destino) throws MovimientoInvalidoException {
        if (destinoTramo != null) {
            throw new MovimientoInvalidoException("Ya estás en camino");
        }
        rutaAutomatica.vaciar();
        iniciarTramo(destino);
    }

    private void iniciarTramo(Esquina destino) throws MovimientoInvalidoException {
        if (!grafo.existeArista(actual, destino)) {
            throw new MovimientoInvalidoException("No hay calle (o es contramano) hacia " + destino.getNombre());
        }
        origenTramo = actual;
        destinoTramo = destino;
        duracionTramo = peso(actual, destino, grafo.getPeso(actual, destino));
        restanteTramo = duracionTramo;
    }

    private void llegar() {
        movimientos.apilar(new Tramo(origenTramo, destinoTramo, duracionTramo));
        actual = destinoTramo;
        destinoTramo = null;
        origenTramo = null;
        if (rutaGps != null && rutaGps.getRuta().obtener(rutaGps.getRuta().tamanio() - 1).equals(actual)) {
            rutaGps = null;
        }
        entregarEnActual();
    }

    /** Deshace el último tramo con la Pila; cuesta 30 segundos del turno. */
    public void deshacer() throws MovimientoInvalidoException {
        if (destinoTramo != null) {
            throw new MovimientoInvalidoException("No se puede deshacer en movimiento");
        }
        if (movimientos.estaVacia()) {
            throw new MovimientoInvalidoException("No hay nada que deshacer");
        }
        Tramo ultimo = movimientos.desapilar();
        actual = ultimo.getOrigen();
        rutaAutomatica.vaciar();
        reloj += COSTO_DESHACER;
        setMensaje("Volviste a " + actual.getNombre() + " (-30 s)");
        if (reloj >= DURACION_TURNO) {
            reloj = DURACION_TURNO;
            terminada = true;
        }
    }

    /** GPS: ruta de costo mínimo hasta el pedido más urgente; cuesta puntos. */
    public ResultadoCamino<Esquina> pedirGps() throws MovimientoInvalidoException {
        if (pendientes.estaVacio()) {
            throw new MovimientoInvalidoException("Aceptá un pedido para usar el GPS");
        }
        Pedido urgente = pendientes.minimo();
        ResultadoCamino<Esquina> ruta = Dijkstra.caminoMinimo(grafo, actual, urgente.getDestino(), this);
        if (!ruta.existe()) {
            throw new MovimientoInvalidoException("No hay ruta hasta ese pedido");
        }
        rutaGps = ruta;
        puntaje -= PENALIDAD_GPS;
        setMensaje("GPS: " + String.format("%.1f", ruta.getCosto()) + " min  (-" + PENALIDAD_GPS + " pts)");
        return ruta;
    }

    /**
     * Interpreta "belgrano y sarmiento" y sale hacia ahí por la ruta con menos
     * cuadras (BFS), que no siempre es la más rápida: para eso está el GPS.
     */
    public void irHacia(String texto) throws CalleInexistenteException, MovimientoInvalidoException {
        Esquina destino = buscarEsquina(texto);
        if (destino.equals(actual)) {
            throw new MovimientoInvalidoException("Ya estás en esa esquina");
        }
        ResultadoCamino<Esquina> ruta = Recorridos.rutaPorSaltos(grafo, actual, destino);
        if (!ruta.existe()) {
            throw new MovimientoInvalidoException("No hay forma de llegar hasta allá");
        }
        rutaAutomatica.vaciar();
        for (int i = 1; i < ruta.getRuta().tamanio(); i++) {
            rutaAutomatica.encolar(ruta.getRuta().obtener(i));
        }
        setMensaje("Yendo a " + destino.getNombre() + " (" + (int) ruta.getCosto() + " cuadras)");
    }

    /** Traduce el texto del jugador a una esquina usando el tokenizador (Trie). */
    public Esquina buscarEsquina(String texto) throws CalleInexistenteException {
        Lista<String> tokens = tokenizador.tokenizar(texto);
        for (int i = 0; i < tokens.tamanio(); i++) {
            if (tokens.obtener(i).startsWith(Tokenizador.PREFIJO_DESCONOCIDO)) {
                throw new CalleInexistenteException("No conozco la calle \"" + tokens.obtener(i).substring(1) + "\"");
            }
        }
        if (tokens.tamanio() != 2) {
            throw new CalleInexistenteException("Escribí dos calles, por ejemplo: sarmiento y belgrano");
        }
        String a = tokens.obtener(0);
        String b = tokens.obtener(1);
        Lista<Esquina> esquinas = grafo.vertices();
        for (int i = 0; i < esquinas.tamanio(); i++) {
            if (esquinas.obtener(i).cruza(a, b)) {
                return esquinas.obtener(i);
            }
        }
        throw new CalleInexistenteException("Esas calles no se cruzan en el mapa");
    }

    public Lista<String> sugerencias(String texto) {
        return tokenizador.sugerencias(texto);
    }

    // ------------------------------------------------------------- eventos

    public void iniciarLluvia(double minutos) {
        lloviendo = true;
        lluviaRestante = minutos;
    }

    public void agregarDinero(int monto) {
        if (monto < 0) {
            throw new IllegalArgumentException("el monto no puede ser negativo");
        }
        dinero += monto;
    }

    /**
     * Después de cortar una calle: se calculan las componentes conexas y se
     * cancelan (sin penalización) los pedidos cuyo destino quedó en otra
     * componente que la del jugador. Devuelve cuántos se cancelaron.
     */
    public int recalcularConectividad() {
        Lista<Esquina> alcance = Conectividad.componenteDe(grafo, actual);
        int cancelados = 0;

        Lista<Pedido> aceptados = pendientes.inOrden();
        for (int i = 0; i < aceptados.tamanio(); i++) {
            if (alcance.indiceDe(aceptados.obtener(i).getDestino()) < 0) {
                pendientes.eliminar(aceptados.obtener(i));
                cancelados++;
            }
        }
        int esperando = entrantes.tamanio();
        for (int i = 0; i < esperando; i++) {
            Pedido pedido = entrantes.desencolar();
            if (alcance.indiceDe(pedido.getDestino()) < 0) {
                cancelados++;
            } else {
                entrantes.encolar(pedido);
            }
        }
        rutaGps = null;
        rutaAutomatica.vaciar();
        return cancelados;
    }

    // ------------------------------------------------------------ consultas

    public Grafo<Esquina> getGrafo() {
        return grafo;
    }

    public Random getAzar() {
        return azar;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Esquina getActual() {
        return actual;
    }

    public Esquina getDestinoTramo() {
        return destinoTramo;
    }

    public Esquina getOrigenTramo() {
        return origenTramo;
    }

    /** Avance del tramo en curso, de 0 a 1. */
    public double getProgresoTramo() {
        if (destinoTramo == null || duracionTramo <= 0) {
            return 0;
        }
        return Math.min(1.0, Math.max(0.0, 1.0 - restanteTramo / duracionTramo));
    }

    public boolean estaEnTransito() {
        return destinoTramo != null;
    }

    public boolean haTerminado() {
        return terminada;
    }

    public boolean estaLloviendo() {
        return lloviendo;
    }

    /** Minutos de juego transcurridos desde que arrancó el turno. */
    public double getReloj() {
        return reloj;
    }

    public double getTiempoRestante() {
        return Math.max(0, DURACION_TURNO - reloj);
    }

    public ResultadoCamino<Esquina> getRutaGps() {
        return rutaGps;
    }

    /** Pedidos aceptados, del más urgente al menos urgente (in-orden iterativo del ABB). */
    public Lista<Pedido> getPendientes() {
        return pendientes.inOrdenIterativo();
    }

    /** El pedido más urgente, o null si no hay ninguno. */
    public Pedido getUrgente() {
        return pendientes.estaVacio() ? null : pendientes.minimo();
    }

    public int cantidadEntrantes() {
        return entrantes.tamanio();
    }

    /** El próximo pedido que se aceptaría, o null si no hay. */
    public Pedido getProximoEntrante() {
        return entrantes.estaVacia() ? null : entrantes.frente();
    }

    public int cantidadTramosDeshacibles() {
        return movimientos.tamanio();
    }

    public int getPuntaje() {
        return puntaje;
    }

    public int getDinero() {
        return dinero;
    }

    public int getEntregas() {
        return entregas;
    }

    public int getVencidos() {
        return vencidos;
    }

    /** Tiempo óptimo de Dijkstra / tiempo real, en las entregas hechas (0 a 1). */
    public double getEficiencia() {
        return sumaReal <= 0.0001 ? 0 : Math.min(1.0, sumaOptimo / sumaReal);
    }

    public String getMensaje() {
        return mensajeRestante > 0 ? mensaje : "";
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje == null ? "" : mensaje;
        this.mensajeRestante = 5.0;
    }
}
