import arboles.ABB;
import arboles.ArbolBinario;
import estructuras.Lista;
import estructuras.ListaEnlazada;
import grafos.Conectividad;
import grafos.Dijkstra;
import grafos.Grafo;
import grafos.Recorridos;
import grafos.ResultadoCamino;
import java.util.Random;
import juego.reglas.Bicicleta;
import juego.reglas.Esquina;
import juego.reglas.Mejora;
import juego.reglas.Moto;
import juego.reglas.Monopatin;
import juego.reglas.Partida;
import juego.reglas.Pedido;
import juego.reglas.Taller;
import juego.reglas.Vehiculo;
import markov.CorteConFin;
import markov.ModeloMarkov;
import markov.RetrocesoDeContexto;
import persistencia.ArchivoMapa;
import persistencia.ArchivoMejoras;
import persistencia.ArchivoRanking;
import persistencia.ArchivoTexto;
import persistencia.ErrorCarga;
import persistencia.Respaldo;
import tokens.Tokenizador;

/**
 * Pruebas del proyecto, sin librerías externas: cada comprobación imprime OK o
 * FALLA y al final hay un total. Se ejecuta con: ejecutar.bat PruebaEstructuras
 */
public class PruebaEstructuras {

    private static int total;
    private static int fallas;

    public static void main(String[] args) throws Exception {
        arbolVacio();
        arbolDeUnNodo();
        recorridosConocidos();
        abbEliminar();
        abbIterativo();
        grafoSinAristas();
        dijkstra();
        componentes();
        markov();
        trie();
        persistencia();
        dominio();
        simulacionDePartidas();

        System.out.println();
        System.out.println((total - fallas) + " de " + total + " comprobaciones correctas -> "
                + (fallas == 0 ? "OK" : "HAY FALLAS"));
        if (fallas > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------ utilidades
    private static void comprobar(String descripcion, boolean condicion) {
        total++;
        if (!condicion) {
            fallas++;
        }
        System.out.println((condicion ? "OK     " : "FALLA  ") + descripcion);
    }

    private static <T> String texto(Lista<T> lista) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lista.tamanio(); i++) {
            sb.append(i == 0 ? "" : ",").append(lista.obtener(i));
        }
        return sb.toString();
    }

    private static ArbolBinario<Integer> completo(int hasta) {
        ArbolBinario<Integer> arbol = new ArbolBinario<>();
        for (int i = 1; i <= hasta; i++) {
            arbol.insertar(i);
        }
        return arbol;
    }

    private static ABB<Integer> abbDe(int primero, int segundo, int tercero, int cuarto, int quinto, int sexto, int septimo) {
        ABB<Integer> abb = new ABB<>();
        abb.insertar(primero);
        abb.insertar(segundo);
        abb.insertar(tercero);
        abb.insertar(cuarto);
        abb.insertar(quinto);
        abb.insertar(sexto);
        abb.insertar(septimo);
        return abb;
    }

    // ------------------------------------------------------------- árboles
    private static void arbolVacio() {
        ArbolBinario<Integer> vacio = new ArbolBinario<>();
        comprobar("1. Árbol vacío: altura 0, 0 nodos, 0 hojas",
                vacio.altura() == 0 && vacio.cantidadNodos() == 0 && vacio.cantidadHojas() == 0);
        comprobar("1. Árbol vacío: los cuatro recorridos devuelven Lista vacía",
                vacio.preOrden().estaVacia() && vacio.inOrden().estaVacia()
                        && vacio.postOrden().estaVacia() && vacio.porNiveles().estaVacia());
    }

    private static void arbolDeUnNodo() {
        ArbolBinario<Integer> uno = completo(1);
        comprobar("2. Árbol de un nodo: altura 1, 1 nodo, 1 hoja",
                uno.altura() == 1 && uno.cantidadNodos() == 1 && uno.cantidadHojas() == 1);
        comprobar("2. Árbol de un nodo: los cuatro recorridos devuelven ese elemento",
                texto(uno.preOrden()).equals("1") && texto(uno.inOrden()).equals("1")
                        && texto(uno.postOrden()).equals("1") && texto(uno.porNiveles()).equals("1"));
    }

    private static void recorridosConocidos() {
        ArbolBinario<Integer> arbol = completo(7);
        comprobar("3. Pre-orden de un árbol conocido = 1,2,4,5,3,6,7", texto(arbol.preOrden()).equals("1,2,4,5,3,6,7"));
        comprobar("3. In-orden = 4,2,5,1,6,3,7", texto(arbol.inOrden()).equals("4,2,5,1,6,3,7"));
        comprobar("3. Post-orden = 4,5,2,6,7,3,1", texto(arbol.postOrden()).equals("4,5,2,6,7,3,1"));
        comprobar("3. Por niveles = 1,2,3,4,5,6,7", texto(arbol.porNiveles()).equals("1,2,3,4,5,6,7"));
        comprobar("3. Altura 3, 7 nodos, 4 hojas",
                arbol.altura() == 3 && arbol.cantidadNodos() == 7 && arbol.cantidadHojas() == 4);
    }

    private static void abbEliminar() {
        // Forma: 50 raíz; 30 (20, 40); 70 (60, 80)
        ABB<Integer> hoja = abbDe(50, 30, 70, 20, 40, 60, 80);
        hoja.eliminar(20);
        comprobar("4. ABB: eliminar una hoja", texto(hoja.inOrden()).equals("30,40,50,60,70,80"));

        ABB<Integer> unHijo = abbDe(50, 30, 70, 20, 40, 60, 80);
        unHijo.eliminar(20);
        unHijo.eliminar(30); // 30 queda con un solo hijo (40)
        comprobar("5. ABB: eliminar un nodo con un hijo", texto(unHijo.inOrden()).equals("40,50,60,70,80"));

        ABB<Integer> dosHijos = abbDe(50, 30, 70, 20, 40, 60, 80);
        boolean estaba = dosHijos.eliminar(50); // la raíz tiene dos hijos
        comprobar("6. ABB: eliminar un nodo con dos hijos mantiene el in-orden ordenado",
                estaba && texto(dosHijos.inOrden()).equals("20,30,40,60,70,80") && dosHijos.minimo() == 20
                        && dosHijos.maximo() == 80);
        comprobar("6. ABB: eliminar algo que no está devuelve false y los duplicados se ignoran",
                !dosHijos.eliminar(99) && cantidadTrasDuplicar() == 3);
    }

    private static int cantidadTrasDuplicar() {
        ABB<Integer> abb = new ABB<>();
        abb.insertar(2);
        abb.insertar(2);
        abb.insertar(1);
        abb.insertar(3);
        abb.insertar(3);
        return abb.cantidadNodos();
    }

    private static void abbIterativo() {
        ABB<Integer> abb = abbDe(50, 30, 70, 20, 40, 60, 80);
        comprobar("7. ABB: el in-orden iterativo (Pila) es igual al recursivo",
                texto(abb.inOrdenIterativo()).equals(texto(abb.inOrden())));
    }

    // -------------------------------------------------------------- grafos
    private static Grafo<String> grafoChico() {
        Grafo<String> g = new Grafo<>();
        g.agregarVertice("A");
        g.agregarVertice("B");
        g.agregarVertice("C");
        g.agregarVertice("D");
        return g;
    }

    private static void grafoSinAristas() {
        Grafo<String> g = grafoChico();
        comprobar("8. Grafo sin aristas: BFS y DFS devuelven solo el origen",
                texto(Recorridos.bfs(g, "A")).equals("A") && texto(Recorridos.dfs(g, "A")).equals("A"));
    }

    private static void dijkstra() {
        Grafo<String> g = grafoChico();
        comprobar("9. Dijkstra: camino inexistente devuelve 'sin camino'", !Dijkstra.caminoMinimo(g, "A", "D").existe());
        g.agregarArista("A", "B", 1);
        g.agregarArista("B", "C", 2);
        g.agregarArista("A", "C", 5);
        g.agregarArista("C", "D", 1);
        ResultadoCamino<String> r = Dijkstra.caminoMinimo(g, "A", "D");
        comprobar("10. Dijkstra con grafo conocido: ruta A,B,C,D con costo 4",
                r.existe() && texto(r.getRuta()).equals("A,B,C,D") && r.getCosto() == 4.0);
        comprobar("10. Dijkstra respeta el sentido de las aristas (D no llega a A)",
                !Dijkstra.caminoMinimo(g, "D", "A").existe());
        Grafo<String> chico = grafoChico();
        chico.agregarArista("A", "B", 1);
        chico.agregarArista("B", "C", 1);
        comprobar("10. BFS por saltos: ruta con menos cuadras", texto(Recorridos.rutaPorSaltos(g, "A", "C").getRuta()).equals("A,C"));
        comprobar("10. bfsHastaDistancia(A, 1) = A,B", texto(Recorridos.bfsHastaDistancia(chico, "A", 1)).equals("A,B"));
    }

    private static void componentes() {
        Grafo<String> g = grafoChico();
        g.agregarArista("A", "B", 1);
        g.agregarArista("B", "A", 1);
        g.agregarArista("B", "C", 1);
        g.agregarArista("C", "B", 1);
        g.agregarArista("C", "D", 1);
        g.agregarArista("D", "C", 1);
        int antes = Conectividad.componentesConexas(g).tamanio();
        g.eliminarArista("B", "C");
        g.eliminarArista("C", "B");
        int despues = Conectividad.componentesConexas(g).tamanio();
        comprobar("11. Componentes conexas: 1 antes de cortar y 2 después", antes == 1 && despues == 2);
    }

    // -------------------------------------------------------------- markov
    private static Lista<String> corpusChico() {
        Lista<String> c = new ListaEnlazada<>();
        c.agregar("Lucia Gomez");
        c.agregar("Mateo Benitez");
        c.agregar("Camila Acosta");
        c.agregar("Joaquin Romero");
        c.agregar("Valentina Silva");
        return c;
    }

    private static void markov() {
        Random azar = new Random(1);
        ModeloMarkov sinEntrenar = new ModeloMarkov(2, new RetrocesoDeContexto());
        String salida = sinEntrenar.generar(azar, 20);
        comprobar("12. Markov sin sucesores: la estrategia actúa y la salida no queda vacía",
                !salida.trim().isEmpty() && !sinEntrenar.getAdvertencia().isEmpty());

        ModeloMarkov entrenado = new ModeloMarkov(2, new RetrocesoDeContexto());
        Lista<String> unaLinea = new ListaEnlazada<>();
        unaLinea.agregar("abc");
        entrenado.entrenar(unaLinea);
        comprobar("12. RetrocesoDeContexto usa el modelo de orden menor ('xb' -> 'c')",
                new RetrocesoDeContexto().resolver(entrenado, "xb", azar) == 'c');
        comprobar("12. CorteConFin cierra la cadena con el marcador de fin",
                new CorteConFin().resolver(entrenado, "xb", azar) == ModeloMarkov.FIN);

        boolean todosNoVacios = true;
        for (int k = 1; k <= 4; k++) {
            ModeloMarkov modelo = new ModeloMarkov(k, new RetrocesoDeContexto());
            modelo.entrenar(corpusChico());
            for (int i = 0; i < 20; i++) {
                String s = modelo.generar(azar, 24);
                if (s.trim().isEmpty() || s.length() > 27) {
                    todosNoVacios = false;
                }
            }
        }
        comprobar("13. Markov con k = 1, 2, 3 y 4 genera cadenas no vacías y acotadas", todosNoVacios);
    }

    // ---------------------------------------------------------------- trie
    private static void trie() {
        Tokenizador t = new Tokenizador();
        t.agregarAlVocabulario("sarmiento");
        t.agregarAlVocabulario("belgrano");
        t.agregarAlVocabulario("9");
        t.agregarAlVocabulario("9 de julio");
        Lista<String> desconocido = t.tokenizar("qwerty");
        comprobar("14. Trie sin coincidencias: el token desconocido se marca y no rompe",
                desconocido.tamanio() == 1 && desconocido.obtener(0).equals("?qwerty"));
        comprobar("15. Coincidencia más larga: '9 de julio' es un solo token",
                texto(t.tokenizar("Sarmiento y 9 de Julio")).equals("sarmiento,9 de julio"));
        comprobar("15. Se normalizan tildes, mayúsculas y signos; 'esq' separa",
                texto(t.tokenizar("BELGRANO, esq. Sarmiénto")).equals("belgrano,sarmiento"));
        comprobar("15. Autocompletar con prefijo ('sar' -> sarmiento)", texto(t.sugerencias("belgrano y sar")).equals("sarmiento"));
    }

    // --------------------------------------------------------- persistencia
    private static void persistencia() throws Exception {
        String ruta = "prueba_mejoras_tmp.txt";
        Respaldo.borrar(ruta);
        Respaldo.borrar(ruta + ".bak");

        Taller original = Taller.porDefecto();
        original.depositar(900);
        original.comprar(original.getArbol().buscar(new Mejora("Motor", 0, 1.0, false)));
        ArchivoMejoras.guardar(ruta, original);
        ArchivoMejoras.guardar(ruta, original); // la segunda vez deja el respaldo .bak
        Lista<ErrorCarga> errores = new ListaEnlazada<>();
        Taller recargado = ArchivoMejoras.cargar(ruta, errores);
        comprobar("16. Persistencia: guardar y recargar el árbol de mejoras da el mismo pre-orden",
                errores.estaVacia() && nombres(original).equals(nombres(recargado))
                        && recargado.getDinero() == original.getDinero()
                        && Math.abs(recargado.getFactorVelocidad() - original.getFactorVelocidad()) < 1e-9);
        comprobar("16. Persistencia: se crea la copia de seguridad .bak", Respaldo.existe(ruta + ".bak"));

        Lista<ErrorCarga> faltante = new ListaEnlazada<>();
        Taller porDefecto = ArchivoMejoras.cargar("no_existe_mejoras.txt", faltante);
        Grafo<Esquina> mapaPorDefecto = ArchivoMapa.cargar("no_existe_mapa.txt", faltante);
        ABB<juego.reglas.EntradaRanking> rankingVacio = ArchivoRanking.cargar("no_existe_ranking.txt", faltante);
        comprobar("17. Persistencia: si faltan los archivos se usan los datos por defecto y se avisa",
                faltante.tamanio() == 3 && nombres(porDefecto).equals(nombres(Taller.porDefecto()))
                        && mapaPorDefecto.cantidadVertices() == 6 && rankingVacio.estaVacio());

        // Una línea rota se saltea y se reporta
        String rutaRanking = "prueba_ranking_tmp.txt";
        Lista<String> lineas = new ListaEnlazada<>();
        lineas.agregar("# comentario");
        lineas.agregar("Lucia;2350;14;2026-11-25");
        lineas.agregar("esta linea esta rota");
        lineas.agregar("Tomas;mil;3;2026-11-24");
        lineas.agregar("Camila;1420;9;2026-11-23");
        ArchivoTexto.escribirLineas(rutaRanking, lineas);
        Lista<ErrorCarga> rotos = new ListaEnlazada<>();
        ABB<juego.reglas.EntradaRanking> ranking = ArchivoRanking.cargar(rutaRanking, rotos);
        comprobar("17. Persistencia: 2 líneas rotas reportadas con su número y el resto se carga",
                rotos.tamanio() == 2 && rotos.obtener(0).getLinea() == 3 && rotos.obtener(1).getLinea() == 4
                        && ranking.cantidadNodos() == 2 && ranking.minimo().getNombre().equals("Lucia"));

        Respaldo.borrar(ruta);
        Respaldo.borrar(ruta + ".bak");
        Respaldo.borrar(rutaRanking);
    }

    private static String nombres(Taller taller) {
        Lista<Mejora> pre = taller.getArbol().preOrden();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pre.tamanio(); i++) {
            sb.append(pre.obtener(i).getNombre()).append(pre.obtener(i).isComprada() ? "*" : "").append(' ');
        }
        return sb.toString();
    }

    // -------------------------------------------------------------- dominio
    private static void dominio() {
        Esquina e1 = new Esquina(1, "belgrano", "french", 10, 10);
        Pedido tarde = new Pedido(1, "Ana", e1, 5.0, 10);
        Pedido urgente = new Pedido(2, "Beto", e1, 2.0, 10);
        Pedido empate = new Pedido(3, "Carla", e1, 2.0, 10);
        ABB<Pedido> pendientes = new ABB<>();
        pendientes.insertar(tarde);
        pendientes.insertar(empate);
        pendientes.insertar(urgente);
        comprobar("18. ABB de pedidos: el mínimo es el más urgente y el empate se desempata por id",
                pendientes.minimo() == urgente && pendientes.cantidadNodos() == 3
                        && texto(pendientes.inOrden()).length() > 0);

        boolean validaciones = true;
        try {
            new Esquina(0, "a", "b", 1, 1);
            validaciones = false;
        } catch (IllegalArgumentException e) {
            // esperado
        }
        try {
            new Mejora("Turbo", 10, 0.2, false);
            validaciones = false;
        } catch (IllegalArgumentException e) {
            // esperado
        }
        comprobar("19. Encapsulamiento: los constructores rechazan datos inválidos", validaciones);

        Vehiculo bicicleta = new Bicicleta();
        double bici = bicicleta.tiempoTramo(1.0, true);
        double moto = new Moto().tiempoTramo(1.0, true);
        double monopatin = new Monopatin().tiempoTramo(1.0, false);
        comprobar("20. Polimorfismo: cada vehículo calcula distinto el tiempo de un tramo",
                Math.abs(bici - 1.3) < 1e-9 && Math.abs(moto - 0.9) < 1e-9
                        && Math.abs(monopatin - 0.8) < 1e-9);

        Taller taller = Taller.porDefecto();
        Mejora turbo = taller.getArbol().buscar(new Mejora("Turbo", 0, 1.0, false));
        Mejora motor = taller.getArbol().buscar(new Mejora("Motor", 0, 1.0, false));
        taller.depositar(2000);
        boolean sinPadre = !taller.puedeComprar(turbo);
        taller.comprar(motor);
        boolean conPadre = taller.puedeComprar(turbo);
        comprobar("21. Taller: no se compra una mejora hasta tener comprada la de arriba", sinPadre && conPadre);
        comprobar("21. Taller: el costo de una rama suma sus nodos (post-orden) = 200+500+300",
                taller.costoDeRama(motor) == 1000);
    }

    // ----------------------------------------------- partidas de punta a punta
    private static void simulacionDePartidas() {
        Random azar = new Random(7);
        ModeloMarkov nombres = new ModeloMarkov(2, new RetrocesoDeContexto());
        nombres.entrenar(corpusChico());
        ModeloMarkov reclamos = new ModeloMarkov(3, new RetrocesoDeContexto());
        Lista<String> r = new ListaEnlazada<>();
        r.agregar("Donde esta mi empanada");
        r.agregar("Llego frio el pedido");
        reclamos.entrenar(r);

        Lista<ErrorCarga> errores = new ListaEnlazada<>();
        Grafo<Esquina> mapa = ArchivoMapa.cargar("datos/mapa.txt", errores);
        boolean sinExcepciones = true;
        int entregasTotales = 0;
        for (int turno = 0; turno < 5; turno++) {
            Partida partida = new Partida(mapa, new Moto(), mapa.vertice(0), nombres, reclamos, azar);
            try {
                int pasos = 0;
                while (!partida.haTerminado() && pasos < 20000) {
                    partida.actualizar(0.05);
                    jugarComoBot(partida, azar);
                    pasos++;
                }
            } catch (RuntimeException e) {
                sinExcepciones = false;
                e.printStackTrace();
            }
            entregasTotales += partida.getEntregas();
        }
        comprobar("22. Simulación: 5 turnos completos de un bot sobre datos/mapa.txt sin excepciones", sinExcepciones);
        comprobar("22. Simulación: el mapa original no se daña con los cortes de calle (30 esquinas)",
                mapa.cantidadVertices() == 30 && errores.estaVacia());
        System.out.println("       (entregas del bot en 5 turnos: " + entregasTotales + ")");
    }

    // El bot acepta pedidos, pide GPS y sigue la ruta; así se ejercitan todas las ramas
    private static void jugarComoBot(Partida partida, Random azar) {
        try {
            if (partida.getProximoEntrante() != null && azar.nextInt(20) == 0) {
                partida.aceptarSiguiente();
            }
            if (!partida.estaEnTransito() && partida.getUrgente() != null) {
                ResultadoCamino<Esquina> ruta = partida.pedirGps();
                if (ruta.getRuta().tamanio() > 1) {
                    partida.mover(ruta.getRuta().obtener(1));
                }
            }
            if (azar.nextInt(400) == 0) {
                partida.deshacer();
            }
        } catch (juego.reglas.MovimientoInvalidoException e) {
            // esperable: contramano, nada que deshacer, etc.
        }
    }
}
