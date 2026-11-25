package juego;

import juego.pantallas.EscenaMenu;
import juego.pantallas.Sesion;
import motor.Ventana;

/** Punto de entrada: carga los datos de la carpeta datos/ y abre el menú. */
public class Main {

    public static void main(String[] args) {
        Sesion sesion = Sesion.cargar("datos/");
        Ventana.abrir("Repartidor en Resistencia", 1080, 660, new EscenaMenu(sesion));
    }
}
