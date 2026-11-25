package juego.reglas;

/**
 * Un pedido: un cliente que espera en una esquina antes de una hora límite.
 * Se ordena por hora límite y, si empatan, por id, así el ABB de pedidos
 * pendientes nunca recibe dos claves iguales y {@code minimo()} es siempre
 * el pedido más urgente.
 */
public class Pedido implements Comparable<Pedido> {

    // Atributos
    private final int id;
    private final String cliente;
    private final Esquina destino;
    private final double horaLimite;
    private final int monto;
    private double horaAceptado = -1;
    private double tiempoOptimo;

    // Constructor con validación
    public Pedido(int id, String cliente, Esquina destino, double horaLimite, int monto) {
        if (id <= 0) {
            throw new IllegalArgumentException("el id debe ser positivo: " + id);
        }
        if (cliente == null || cliente.trim().isEmpty()) {
            throw new IllegalArgumentException("el cliente necesita nombre");
        }
        if (destino == null) {
            throw new IllegalArgumentException("el pedido necesita un destino");
        }
        if (!(horaLimite > 0)) {
            throw new IllegalArgumentException("la hora límite debe ser positiva: " + horaLimite);
        }
        if (monto < 0) {
            throw new IllegalArgumentException("el monto no puede ser negativo: " + monto);
        }
        this.id = id;
        this.cliente = cliente.trim();
        this.destino = destino;
        this.horaLimite = horaLimite;
        this.monto = monto;
    }

    // Aceptación: el jugador lo toma y se calcula cuánto debería tardar como mínimo
    public void aceptar(double hora, double tiempoOptimo) {
        if (hora < 0 || tiempoOptimo < 0) {
            throw new IllegalArgumentException("hora y tiempo óptimo no pueden ser negativos");
        }
        this.horaAceptado = hora;
        this.tiempoOptimo = tiempoOptimo;
    }

    /** Lanza PedidoVencidoException si a la hora dada ya pasó el límite. */
    public void verificarVigente(double horaActual) throws PedidoVencidoException {
        if (horaActual > horaLimite) {
            throw new PedidoVencidoException("el pedido de " + cliente + " venció");
        }
    }

    // Consultas
    public int getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public Esquina getDestino() {
        return destino;
    }

    public double getHoraLimite() {
        return horaLimite;
    }

    public int getMonto() {
        return monto;
    }

    public double getHoraAceptado() {
        return horaAceptado;
    }

    public double getTiempoOptimo() {
        return tiempoOptimo;
    }

    // Orden natural: hora límite y luego id
    @Override
    public int compareTo(Pedido otro) {
        int porHora = Double.compare(horaLimite, otro.horaLimite);
        return porHora != 0 ? porHora : Integer.compare(id, otro.id);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (otro == null || getClass() != otro.getClass()) {
            return false;
        }
        return id == ((Pedido) otro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
