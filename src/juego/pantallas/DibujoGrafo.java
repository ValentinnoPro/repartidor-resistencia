package juego.pantallas;

import estructuras.Lista;
import estructuras.ListaEnlazada;
import grafos.Arista;
import grafos.Grafo;
import grafos.Recorridos;
import grafos.ResultadoCamino;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Arc2D;
import juego.reglas.Esquina;
import juego.reglas.Partida;
import juego.reglas.Pedido;
import ui.Texto;

/**
 * Dibuja el mapa como una ciudad vista desde arriba: manzanas con edificios y
 * plazas, calles con su señalización (doble mano con línea amarilla, una mano
 * con flechas), calles cortadas, pedidos como pines con cuenta regresiva, el
 * GPS animado, la lluvia y al cadete con su vehículo.
 */
public class DibujoGrafo {

    static final int ANCHO_MAPA = 790;
    static final int ALTO_MAPA = 610;

    private static final int RADIO = 12;
    private static final int ANCHO_CALLE = 24;
    private static final int RADIO_CLICK = 20;

    private static final Color SUELO_ARRIBA = new Color(24, 31, 48);
    private static final Color SUELO_ABAJO = new Color(16, 21, 34);
    private static final Color CALLE = new Color(66, 75, 98);
    private static final Color CALLE_UNA_MANO = new Color(86, 78, 82);
    private static final Color CORDON = new Color(96, 108, 140);
    private static final Color CORDON_UNA_MANO = new Color(138, 108, 92);
    private static final Color RUTA_GPS = new Color(255, 178, 40);

    // Una manzana: el rectángulo entre cuatro esquinas
    private static class Bloque {
        private final int x;
        private final int y;
        private final int ancho;
        private final int alto;
        private final int semilla;

        private Bloque(int x, int y, int ancho, int alto, int semilla) {
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
            this.semilla = semilla;
        }
    }

    // Nombre de una calle escrito en el borde del mapa
    private static class Rotulo {
        private final String texto;
        private final double x;
        private final double y;
        private final boolean horizontal;

        private Rotulo(String texto, double x, double y, boolean horizontal) {
            this.texto = texto;
            this.x = x;
            this.y = y;
            this.horizontal = horizontal;
        }
    }

    private Lista<Bloque> bloques;
    private Lista<Rotulo> rotulos;
    private double tiempo;
    private double angulo;
    private double cadeteX;
    private double cadeteY;
    private boolean moviendo;
    private double relojPolvo;
    private Esquina vistaDestino;
    private Esquina vistaOrigen;
    private ResultadoCamino<Esquina> vistaRuta;

    // ------------------------------------------------------------ animación

    /** Avanza la animación: posición del cadete, giro suave hacia donde va y polvo. */
    void actualizar(double dt, Partida partida, Efectos efectos) {
        tiempo += dt;
        cadeteX = partida.getActual().getX();
        cadeteY = partida.getActual().getY();
        moviendo = partida.estaEnTransito();
        if (moviendo) {
            Esquina o = partida.getOrigenTramo();
            Esquina d = partida.getDestinoTramo();
            double t = partida.getProgresoTramo();
            cadeteX = o.getX() + (d.getX() - o.getX()) * t;
            cadeteY = o.getY() + (d.getY() - o.getY()) * t;
            double objetivo = Math.atan2(d.getY() - o.getY(), d.getX() - o.getX());
            double diferencia = objetivo - angulo;
            while (diferencia > Math.PI) {
                diferencia -= Math.PI * 2;
            }
            while (diferencia < -Math.PI) {
                diferencia += Math.PI * 2;
            }
            angulo += diferencia * Math.min(1.0, dt * 14);
            relojPolvo += dt;
            if (relojPolvo > 0.05) {
                relojPolvo = 0;
                efectos.polvo(cadeteX - Math.cos(angulo) * 16, cadeteY - Math.sin(angulo) * 16, new Color(200, 208, 230, 200));
            }
        }
    }

    double getCadeteX() {
        return cadeteX;
    }

    double getCadeteY() {
        return cadeteY;
    }

    double getTiempo() {
        return tiempo;
    }

    // --------------------------------------------------------------- consulta

    /** La esquina bajo el mouse (a menos de 20 px), o null. */
    Esquina esquinaEn(Partida partida, int mx, int my) {
        if (mx < 0 || mx >= ANCHO_MAPA || my < 0 || my >= ALTO_MAPA) {
            return null;
        }
        Lista<Esquina> esquinas = partida.getGrafo().vertices();
        Esquina mejor = null;
        double mejorDistancia = RADIO_CLICK * RADIO_CLICK;
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina e = esquinas.obtener(i);
            double dx = e.getX() - mx;
            double dy = e.getY() - my;
            double distancia = dx * dx + dy * dy;
            if (distancia <= mejorDistancia) {
                mejor = e;
                mejorDistancia = distancia;
            }
        }
        return mejor;
    }

    // ----------------------------------------------------------------- dibujo

    void dibujar(Graphics2D g, Partida partida, Grafo<Esquina> original, Esquina local, Esquina bajoMouse,
                 Pedido resaltado, int tipoVehiculo) {
        Grafo<Esquina> grafo = partida.getGrafo();
        Lista<Esquina> esquinas = grafo.vertices();
        if (bloques == null) {
            construirBloques(esquinas);
            construirRotulos(esquinas);
        }

        g.setPaint(new GradientPaint(0, 0, SUELO_ARRIBA, 0, ALTO_MAPA, SUELO_ABAJO));
        g.fillRect(0, 0, ANCHO_MAPA, ALTO_MAPA);

        dibujarBloques(g);
        dibujarRotulos(g);
        dibujarCallesCortadas(g, grafo, original, esquinas);
        // Tres pasadas: primero los cordones, después el asfalto y al final la señalización
        for (int pasada = 0; pasada < 3; pasada++) {
            dibujarCalles(g, grafo, esquinas, pasada);
        }
        dibujarCruces(g, esquinas);
        dibujarLocal(g, local);
        dibujarVistaPrevia(g, partida, bajoMouse);
        dibujarGps(g, partida);
        dibujarVecinas(g, partida, bajoMouse);
        dibujarPedidos(g, partida, resaltado);
        DibujoVehiculo.dibujar(g, tipoVehiculo, cadeteX, cadeteY, angulo, 1.25, tiempo, moviendo);
        if (partida.estaLloviendo()) {
            dibujarLluvia(g);
        }
        if (bajoMouse != null) {
            dibujarEtiqueta(g, partida, bajoMouse);
        }
    }

    // ------------------------------------------------------- escenografía

    private void construirBloques(Lista<Esquina> esquinas) {
        bloques = new ListaEnlazada<>();
        int margen = ANCHO_CALLE / 2 + 5;
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina a = esquinas.obtener(i);
            Esquina derecha = null;
            Esquina abajo = null;
            for (int j = 0; j < esquinas.tamanio(); j++) {
                Esquina b = esquinas.obtener(j);
                if (b.getY() == a.getY() && b.getX() > a.getX() && (derecha == null || b.getX() < derecha.getX())) {
                    derecha = b;
                }
                if (b.getX() == a.getX() && b.getY() > a.getY() && (abajo == null || b.getY() < abajo.getY())) {
                    abajo = b;
                }
            }
            if (derecha != null && abajo != null) {
                bloques.agregar(new Bloque(a.getX() + margen, a.getY() + margen,
                        derecha.getX() - a.getX() - margen * 2, abajo.getY() - a.getY() - margen * 2, a.getId()));
            }
        }
    }

    private void construirRotulos(Lista<Esquina> esquinas) {
        rotulos = new ListaEnlazada<>();
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina e = esquinas.obtener(i);
            // calle1 corre de este a oeste: se rotula en su extremo izquierdo
            Rotulo izquierdo = rotuloDe(e.getCalle1());
            if (izquierdo == null) {
                rotulos.agregar(new Rotulo(e.getCalle1(), e.getX(), e.getY(), false));
            } else if (e.getX() < izquierdo.x) {
                rotulos.eliminar(rotulos.indiceDe(izquierdo));
                rotulos.agregar(new Rotulo(e.getCalle1(), e.getX(), e.getY(), false));
            }
            // calle2 corre de norte a sur: se rotula en su extremo superior
            Rotulo superior = rotuloDe(e.getCalle2());
            if (superior == null) {
                rotulos.agregar(new Rotulo(e.getCalle2(), e.getX(), e.getY(), true));
            } else if (e.getY() < superior.y) {
                rotulos.eliminar(rotulos.indiceDe(superior));
                rotulos.agregar(new Rotulo(e.getCalle2(), e.getX(), e.getY(), true));
            }
        }
    }

    private Rotulo rotuloDe(String texto) {
        for (int i = 0; i < rotulos.tamanio(); i++) {
            if (rotulos.obtener(i).texto.equals(texto)) {
                return rotulos.obtener(i);
            }
        }
        return null;
    }

    private static int mezcla(int valor) {
        int h = valor * 0x9E3779B1;
        h ^= h >>> 15;
        h *= 0x85EBCA6B;
        h ^= h >>> 13;
        return h & 0x7fffffff;
    }

    private void dibujarBloques(Graphics2D g) {
        for (int i = 0; i < bloques.tamanio(); i++) {
            Bloque b = bloques.obtener(i);
            int tipo = mezcla(b.semilla * 31 + 7) % 6;
            g.setColor(new Color(33, 42, 64));
            g.fillRoundRect(b.x, b.y, b.ancho, b.alto, 12, 12);
            switch (tipo) {
                case 0:
                    plaza(g, b);
                    break;
                case 1:
                case 2:
                    edificios(g, b, 2, 2);
                    break;
                case 3:
                    edificios(g, b, 3, 2);
                    break;
                case 4:
                    edificios(g, b, 1, 2);
                    break;
                default:
                    casas(g, b);
                    break;
            }
        }
    }

    private void plaza(Graphics2D g, Bloque b) {
        g.setColor(new Color(28, 74, 60));
        g.fillRoundRect(b.x + 4, b.y + 4, b.ancho - 8, b.alto - 8, 10, 10);
        g.setColor(new Color(40, 98, 78));
        g.drawRoundRect(b.x + 4, b.y + 4, b.ancho - 8, b.alto - 8, 10, 10);
        // Senderos en cruz y fuente central
        g.setColor(new Color(60, 90, 84));
        g.fillRect(b.x + b.ancho / 2 - 3, b.y + 6, 6, b.alto - 12);
        g.fillRect(b.x + 6, b.y + b.alto / 2 - 3, b.ancho - 12, 6);
        g.setColor(new Color(70, 140, 190));
        g.fillOval(b.x + b.ancho / 2 - 9, b.y + b.alto / 2 - 9, 18, 18);
        g.setColor(new Color(150, 205, 240));
        g.drawOval(b.x + b.ancho / 2 - 9, b.y + b.alto / 2 - 9, 18, 18);
        for (int k = 0; k < 7; k++) {
            int h = mezcla(b.semilla * 13 + k);
            int tx = b.x + 12 + h % Math.max(1, b.ancho - 24);
            int ty = b.y + 12 + (h / 97) % Math.max(1, b.alto - 24);
            g.setColor(new Color(22, 100, 66));
            g.fillOval(tx - 7, ty - 7, 14, 14);
            g.setColor(new Color(46, 150, 96, 150));
            g.fillOval(tx - 5, ty - 6, 8, 8);
        }
    }

    private void edificios(Graphics2D g, Bloque b, int columnas, int filas) {
        int hueco = 4;
        int w = (b.ancho - hueco * (columnas + 1)) / columnas;
        int h = (b.alto - hueco * (filas + 1)) / filas;
        for (int c = 0; c < columnas; c++) {
            for (int f = 0; f < filas; f++) {
                int x = b.x + hueco + c * (w + hueco);
                int y = b.y + hueco + f * (h + hueco);
                int tono = mezcla(b.semilla * 17 + c * 5 + f) % 3;
                Color techo = tono == 0 ? new Color(58, 68, 96) : tono == 1 ? new Color(64, 62, 92) : new Color(52, 74, 94);
                g.setColor(new Color(0, 0, 0, 60));
                g.fillRoundRect(x + 2, y + 3, w, h, 6, 6);
                g.setColor(techo);
                g.fillRoundRect(x, y, w, h, 6, 6);
                g.setColor(Estilo.alfa(Color.WHITE, 26));
                g.drawRoundRect(x, y, w, h, 6, 6);
                // Ventanas encendidas al azar
                for (int vx = x + 6; vx < x + w - 8; vx += 9) {
                    for (int vy = y + 6; vy < y + h - 8; vy += 9) {
                        if (mezcla(vx * 31 + vy * 17 + b.semilla) % 3 == 0) {
                            g.setColor(new Color(255, 216, 120, 150));
                            g.fillRect(vx, vy, 4, 4);
                        }
                    }
                }
            }
        }
    }

    private void casas(Graphics2D g, Bloque b) {
        int hueco = 4;
        int columnas = 3;
        int filas = 2;
        int w = (b.ancho - hueco * (columnas + 1)) / columnas;
        int h = (b.alto - hueco * (filas + 1)) / filas;
        for (int c = 0; c < columnas; c++) {
            for (int f = 0; f < filas; f++) {
                int x = b.x + hueco + c * (w + hueco);
                int y = b.y + hueco + f * (h + hueco);
                int tono = mezcla(b.semilla * 19 + c * 3 + f * 7) % 3;
                Color techo = tono == 0 ? new Color(112, 70, 70) : tono == 1 ? new Color(96, 78, 66) : new Color(80, 84, 110);
                g.setColor(techo);
                g.fillRoundRect(x, y, w, h, 5, 5);
                g.setColor(Estilo.mezclar(techo, Color.BLACK, 0.35));
                g.drawLine(x + w / 2, y + 1, x + w / 2, y + h - 2);
                g.setColor(Estilo.alfa(Color.WHITE, 30));
                g.drawRoundRect(x, y, w, h, 5, 5);
            }
        }
    }

    private void dibujarRotulos(Graphics2D g) {
        for (int i = 0; i < rotulos.tamanio(); i++) {
            Rotulo r = rotulos.obtener(i);
            String texto = titulo(r.texto);
            if (r.horizontal) {
                Texto.centrado(g, texto, r.x, r.y - 22, 10, new Color(150, 165, 200));
            } else {
                Texto.izquierda(g, texto, 6, r.y + 4, 10, new Color(150, 165, 200));
            }
        }
    }

    private static String titulo(String texto) {
        StringBuilder sb = new StringBuilder();
        boolean inicio = true;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            boolean esDe = texto.startsWith("de ", i);
            sb.append(inicio && !esDe ? Character.toUpperCase(c) : c);
            inicio = c == ' ';
        }
        return sb.toString();
    }

    // ----------------------------------------------------------------- calles

    private void dibujarCalles(Graphics2D g, Grafo<Esquina> grafo, Lista<Esquina> esquinas, int pasada) {
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina origen = esquinas.obtener(i);
            Lista<Arista<Esquina>> salientes = grafo.vecinos(origen);
            for (int j = 0; j < salientes.tamanio(); j++) {
                Esquina destino = salientes.obtener(j).getDestino();
                boolean dobleMano = grafo.existeArista(destino, origen);
                if (dobleMano && origen.getId() > destino.getId()) {
                    continue;
                }
                tramoDeCalle(g, origen, destino, dobleMano, pasada);
            }
        }
    }

    private void tramoDeCalle(Graphics2D g, Esquina a, Esquina b, boolean dobleMano, int pasada) {
        if (pasada == 0) {
            g.setStroke(new BasicStroke(ANCHO_CALLE + 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(dobleMano ? CORDON : CORDON_UNA_MANO);
            g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
        } else if (pasada == 1) {
            g.setStroke(new BasicStroke(ANCHO_CALLE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(dobleMano ? CALLE : CALLE_UNA_MANO);
            g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
        } else if (dobleMano) {
            g.setStroke(new BasicStroke(2f));
            lineaPunteada(g, a.getX(), a.getY(), b.getX(), b.getY(), 9, 9, 0, new Color(240, 200, 90, 150), RADIO + 6);
        } else {
            g.setStroke(new BasicStroke(2f));
            flechas(g, a, b);
        }
    }

    // Chevrones blancos que indican el sentido de una calle de una mano
    private void flechas(Graphics2D g, Esquina desde, Esquina hasta) {
        double dx = hasta.getX() - desde.getX();
        double dy = hasta.getY() - desde.getY();
        double largo = Math.sqrt(dx * dx + dy * dy);
        if (largo < 1) {
            return;
        }
        double ux = dx / largo;
        double uy = dy / largo;
        g.setColor(new Color(255, 205, 150, 190));
        double paso = 30;
        for (double d = RADIO + 14; d < largo - RADIO - 8; d += paso) {
            double cx = desde.getX() + ux * d;
            double cy = desde.getY() + uy * d;
            Polygon punta = new Polygon();
            punta.addPoint((int) (cx + ux * 6), (int) (cy + uy * 6));
            punta.addPoint((int) (cx - ux * 3 - uy * 6), (int) (cy - uy * 3 + ux * 6));
            punta.addPoint((int) (cx - ux * 1), (int) (cy - uy * 1));
            punta.addPoint((int) (cx - ux * 3 + uy * 6), (int) (cy - uy * 3 - ux * 6));
            g.fillPolygon(punta);
        }
    }

    // Sin arreglos de guiones: se dibuja segmento por segmento
    private void lineaPunteada(Graphics2D g, double x1, double y1, double x2, double y2, double largo, double hueco,
                               double fase, Color color, double recorteExtremos) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double total = Math.sqrt(dx * dx + dy * dy);
        if (total < 1) {
            return;
        }
        double ux = dx / total;
        double uy = dy / total;
        g.setColor(color);
        double ciclo = largo + hueco;
        for (double d = recorteExtremos - ciclo + (fase % ciclo); d < total - recorteExtremos; d += ciclo) {
            double inicio = Math.max(d, recorteExtremos);
            double fin = Math.min(d + largo, total - recorteExtremos);
            if (fin > inicio) {
                g.drawLine((int) (x1 + ux * inicio), (int) (y1 + uy * inicio), (int) (x1 + ux * fin), (int) (y1 + uy * fin));
            }
        }
    }

    private void dibujarCruces(Graphics2D g, Lista<Esquina> esquinas) {
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina e = esquinas.obtener(i);
            g.setColor(new Color(74, 84, 108));
            g.fillOval(e.getX() - ANCHO_CALLE / 2, e.getY() - ANCHO_CALLE / 2, ANCHO_CALLE, ANCHO_CALLE);
            g.setColor(new Color(150, 162, 196, 160));
            g.fillOval(e.getX() - 3, e.getY() - 3, 6, 6);
        }
    }

    // Una calle cortada: asfalto apagado, franja roja y conos
    private void dibujarCallesCortadas(Graphics2D g, Grafo<Esquina> grafo, Grafo<Esquina> original, Lista<Esquina> esquinas) {
        if (original == null) {
            return;
        }
        for (int i = 0; i < esquinas.tamanio(); i++) {
            Esquina a = esquinas.obtener(i);
            Lista<Arista<Esquina>> salientes = original.vecinos(a);
            for (int j = 0; j < salientes.tamanio(); j++) {
                Esquina b = salientes.obtener(j).getDestino();
                boolean ordenado = a.getId() < b.getId() || !original.existeArista(b, a);
                if (!ordenado || grafo.existeArista(a, b) || grafo.existeArista(b, a)) {
                    continue;
                }
                g.setStroke(new BasicStroke(ANCHO_CALLE + 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(110, 60, 66));
                g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
                g.setStroke(new BasicStroke(ANCHO_CALLE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(50, 38, 46));
                g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
                g.setStroke(new BasicStroke(3f));
                lineaPunteada(g, a.getX(), a.getY(), b.getX(), b.getY(), 8, 8, 0, new Color(246, 88, 88, 200), RADIO + 4);
                double mx = (a.getX() + b.getX()) / 2.0;
                double my = (a.getY() + b.getY()) / 2.0;
                g.setColor(Estilo.ROJO);
                g.fillOval((int) mx - 11, (int) my - 11, 22, 22);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) mx - 5, (int) my - 5, (int) mx + 5, (int) my + 5);
                g.drawLine((int) mx - 5, (int) my + 5, (int) mx + 5, (int) my - 5);
            }
        }
    }

    private void dibujarLocal(Graphics2D g, Esquina local) {
        double pulso = Estilo.pulso(tiempo, 0.8);
        g.setColor(new Color(96, 172, 255, (int) (40 + 40 * pulso)));
        int r = (int) (22 + 5 * pulso);
        g.fillOval(local.getX() - r, local.getY() - r, r * 2, r * 2);
        g.setColor(Estilo.AZUL);
        g.fillRoundRect(local.getX() - 17, local.getY() + 16, 34, 16, 8, 8);
        Texto.centrado(g, "UTN", local.getX(), local.getY() + 28, 11, new Color(12, 24, 48));
    }

    // ------------------------------------------------------ rutas e indicadores

    private void dibujarGps(Graphics2D g, Partida partida) {
        if (partida.getRutaGps() == null) {
            return;
        }
        Lista<Esquina> ruta = partida.getRutaGps().getRuta();
        g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(255, 178, 40, 60));
        for (int i = 0; i + 1 < ruta.tamanio(); i++) {
            g.drawLine(ruta.obtener(i).getX(), ruta.obtener(i).getY(), ruta.obtener(i + 1).getX(), ruta.obtener(i + 1).getY());
        }
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(RUTA_GPS);
        for (int i = 0; i + 1 < ruta.tamanio(); i++) {
            g.drawLine(ruta.obtener(i).getX(), ruta.obtener(i).getY(), ruta.obtener(i + 1).getX(), ruta.obtener(i + 1).getY());
        }
        // Brillo que avanza en el sentido de la ruta
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i + 1 < ruta.tamanio(); i++) {
            lineaPunteada(g, ruta.obtener(i).getX(), ruta.obtener(i).getY(), ruta.obtener(i + 1).getX(),
                    ruta.obtener(i + 1).getY(), 6, 18, tiempo * 40, new Color(255, 255, 255, 230), 0);
        }
    }

    // Camino que se recorrería al hacer click en una esquina lejana
    private void dibujarVistaPrevia(Graphics2D g, Partida partida, Esquina bajoMouse) {
        if (bajoMouse == null || partida.estaEnTransito() || bajoMouse.equals(partida.getActual())
                || partida.getGrafo().existeArista(partida.getActual(), bajoMouse)) {
            return;
        }
        if (vistaRuta == null || !bajoMouse.equals(vistaDestino) || !partida.getActual().equals(vistaOrigen)) {
            vistaDestino = bajoMouse;
            vistaOrigen = partida.getActual();
            vistaRuta = Recorridos.rutaPorSaltos(partida.getGrafo(), partida.getActual(), bajoMouse);
        }
        if (!vistaRuta.existe()) {
            return;
        }
        Lista<Esquina> ruta = vistaRuta.getRuta();
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i + 1 < ruta.tamanio(); i++) {
            lineaPunteada(g, ruta.obtener(i).getX(), ruta.obtener(i).getY(), ruta.obtener(i + 1).getX(),
                    ruta.obtener(i + 1).getY(), 8, 8, tiempo * 30, new Color(235, 240, 255, 220), 0);
        }
    }

    // Esquinas a las que se puede ir desde donde estás (respetando las manos)
    private void dibujarVecinas(Graphics2D g, Partida partida, Esquina bajoMouse) {
        if (partida.estaEnTransito()) {
            return;
        }
        Esquina actual = partida.getActual();
        Lista<Arista<Esquina>> salientes = partida.getGrafo().vecinos(actual);
        double pulso = Estilo.pulso(tiempo, 1.2);
        for (int i = 0; i < salientes.tamanio(); i++) {
            Esquina e = salientes.obtener(i).getDestino();
            boolean encima = e.equals(bajoMouse);
            int r = (int) (RADIO + 3 + pulso * 3 + (encima ? 4 : 0));
            g.setColor(new Color(96, 224, 150, encima ? 110 : 45));
            g.fillOval(e.getX() - r, e.getY() - r, r * 2, r * 2);
            g.setStroke(new BasicStroke(2.5f));
            g.setColor(new Color(96, 224, 150, encima ? 255 : 190));
            g.drawOval(e.getX() - r, e.getY() - r, r * 2, r * 2);
        }
    }

    // ---------------------------------------------------------------- pedidos

    private void dibujarPedidos(Graphics2D g, Partida partida, Pedido resaltado) {
        Pedido proximo = partida.getProximoEntrante();
        if (proximo != null) {
            pin(g, proximo.getDestino(), Estilo.VERDE, "", 0, -1, true);
        }
        Lista<Pedido> pendientes = partida.getPendientes();
        Pedido urgente = partida.getUrgente();
        for (int i = 0; i < pendientes.tamanio(); i++) {
            Pedido p = pendientes.obtener(i);
            boolean esUrgente = p.equals(urgente);
            double total = Math.max(0.01, p.getHoraLimite() - p.getHoraAceptado());
            double fraccion = Math.max(0, Math.min(1, (p.getHoraLimite() - partida.getReloj()) / total));
            Color color = esUrgente ? Estilo.ROJO : Estilo.DORADO;
            pin(g, p.getDestino(), color, String.valueOf(i + 1), fraccion, i, p.equals(resaltado));
        }
    }

    // Pin de mapa: base con cuenta regresiva, cuerpo con el número y rebote suave
    private void pin(Graphics2D g, Esquina e, Color color, String numero, double fraccion, int indice, boolean resaltado) {
        double pulso = Estilo.pulso(tiempo + indice * 0.3, 1.1);
        int x = e.getX();
        int y = e.getY();
        boolean entrante = indice < 0 && numero.isEmpty();

        // Base en el suelo
        g.setColor(Estilo.alfa(color, (int) (50 + 40 * pulso)));
        int r = (int) (17 + pulso * 4 + (resaltado ? 5 : 0));
        g.fillOval(x - r, y - r, r * 2, r * 2);
        if (!entrante) {
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(Estilo.mezclar(Estilo.ROJO, Estilo.VERDE, fraccion));
            g.draw(new Arc2D.Double(x - 19, y - 19, 38, 38, 90, -360 * fraccion, Arc2D.OPEN));
        } else {
            g.setStroke(new BasicStroke(2.5f));
            g.setColor(Estilo.alfa(color, 200));
            g.drawOval(x - 17, y - 17, 34, 34);
        }

        // Cuerpo del pin
        double rebote = Math.sin((tiempo + indice * 0.4) * 4) * 2.5;
        double py = y - 20 + rebote;
        Polygon punta = new Polygon();
        punta.addPoint(x - 8, (int) py + 4);
        punta.addPoint(x + 8, (int) py + 4);
        punta.addPoint(x, y - 2);
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(x - 10, y - 4, 20, 8);
        g.setColor(color);
        g.fillPolygon(punta);
        g.fillOval(x - 12, (int) py - 12, 24, 24);
        g.setColor(Estilo.mezclar(color, Color.BLACK, 0.4));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x - 12, (int) py - 12, 24, 24);
        g.setColor(entrante ? new Color(14, 40, 28) : new Color(40, 20, 10));
        if (entrante) {
            g.fillOval(x - 4, (int) py - 4, 8, 8);
        } else {
            Texto.centrado(g, numero, x, py + 5, 14, new Color(35, 20, 10));
        }
    }

    // ----------------------------------------------------------------- lluvia

    // Gotas sin estado: la posición de cada una es función del tiempo
    private void dibujarLluvia(Graphics2D g) {
        g.setColor(new Color(40, 70, 130, 70));
        g.fillRect(0, 0, ANCHO_MAPA, ALTO_MAPA);
        g.setStroke(new BasicStroke(1.4f));
        for (int i = 0; i < 170; i++) {
            double fase = (mezcla(i * 7 + 1) % 1000) / 1000.0;
            double velocidad = 520 + (mezcla(i * 11 + 3) % 300);
            double x0 = (mezcla(i * 13 + 5) % (ANCHO_MAPA + 120)) - 60;
            double y = (fase * (ALTO_MAPA + 40) + tiempo * velocidad) % (ALTO_MAPA + 40) - 20;
            double x = x0 - (y / (ALTO_MAPA + 40)) * 30;
            g.setColor(new Color(180, 210, 255, 110));
            g.drawLine((int) x, (int) y, (int) (x - 3), (int) (y + 12));
        }
    }

    // --------------------------------------------------------------- etiqueta

    private void dibujarEtiqueta(Graphics2D g, Partida partida, Esquina e) {
        String linea2;
        Color color = Estilo.TEXTO_SUAVE;
        if (e.equals(partida.getActual())) {
            linea2 = "Estás acá";
        } else if (partida.getGrafo().existeArista(partida.getActual(), e)) {
            double segundos = partida.peso(partida.getActual(), e, partida.getGrafo().getPeso(partida.getActual(), e)) * 60;
            linea2 = "Click: ir  (" + Math.round(segundos) + " s)";
            color = Estilo.VERDE;
        } else if (partida.getGrafo().existeArista(e, partida.getActual())) {
            linea2 = "Contramano: click para rodear";
            color = Estilo.DORADO;
        } else {
            linea2 = "Click: ir hacia ahí";
            color = Estilo.AZUL;
        }
        String linea1 = e.getNombre();
        int ancho = Math.max(linea1.length() * 8, linea2.length() * 7) + 20;
        double x = Math.max(4, Math.min(ANCHO_MAPA - ancho - 4, e.getX() - ancho / 2.0));
        double y = e.getY() - 70;
        if (y < 60) {
            y = e.getY() + 28;
        }
        Estilo.panel(g, x, y, ancho, 44, 10, new Color(14, 18, 32, 235), Estilo.BORDE);
        Texto.centrado(g, linea1, x + ancho / 2.0, y + 18, 13, Estilo.TEXTO);
        Texto.centrado(g, linea2, x + ancho / 2.0, y + 35, 11, color);
    }
}
