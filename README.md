# Repartidor en Resistencia (Game Jam Java II)

Juego 2D en Java sobre el kit de la Game Jam: un cadete entrega pedidos por las calles del centro de Resistencia, Chaco.
Requiere un JDK 11 o superior.

## Compilar y ejecutar

| Sistema | Compilar | Ejecutar el juego | Otras clases |
| --- | --- | --- | --- |
| Linux / macOS | `./compilar.sh` | `./ejecutar.sh juego.Main` | `./ejecutar.sh PruebaEstructuras` |
| Windows | `compilar.bat` | `ejecutar.bat juego.Main` | `ejecutar.bat PruebaEstructuras` |

Se ejecuta desde esta carpeta (el juego lee `datos/`). En un IDE, abrir `src` como carpeta de fuentes y usar codificación **UTF-8**.

- `PruebaEstructuras`: 40 comprobaciones (árboles, ABB, grafos, Dijkstra, Markov, Trie, persistencia, partidas simuladas).
- `verificador.PruebaMisEstructuras`: el verificador del kit sobre `ListaEnlazada`, `PilaEnlazada` y `ColaEnlazada`.
- `PruebaMarkov`: 10 salidas de Markov con k = 1 a 4 para el informe.

El informe técnico (estructuras, Big-O, formatos de archivo, decisiones) está en `docs/INFORME.md`.

## Paquetes

| Paquete | Contenido | ¿Se modifica? |
| --- | --- | --- |
| `motor`, `contratos`, `ui`, `verificador` | Kit: ventana, escenas, entrada, temporizador, texto, verificador | No (solo la plantilla `PruebaMisEstructuras`) |
| `estructuras` | Interfaces del kit + `ListaEnlazada`, `PilaEnlazada`, `ColaEnlazada`, `Par` | Se agregan implementaciones |
| `arboles` | `ArbolBase`, `ArbolBinario`, `ABB` | Propio |
| `tokens` | `Trie`, `Tokenizador` | Propio |
| `grafos` | `Grafo`, `Recorridos` (BFS/DFS), `Dijkstra`, `Conectividad` | Propio |
| `markov` | `ModeloMarkov` y estrategias para estados sin sucesores | Propio |
| `juego.reglas` | `Partida`, `Vehiculo` y subclases, `Evento` y subclases, `Taller`, `Pedido`, `Esquina`... | Propio |
| `persistencia` | Lectura y escritura de `datos/*.txt`, respaldo `.bak`, `ErrorCarga` | Propio |
| `juego.pantallas` | Las cuatro escenas, `DibujoGrafo`, `DibujoArbol`, `CajaTexto`, `Sesion` | Propio |
| `demo` | Ejemplo del motor | Referencia |
