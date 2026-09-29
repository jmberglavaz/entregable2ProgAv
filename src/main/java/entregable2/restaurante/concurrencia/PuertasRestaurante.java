package main.java.entregable2.restaurante.concurrencia;

/**
 * Estado de las puertas del restaurante.
 * volatile garantiza visibilidad entre hilos.
 */
public final class PuertasRestaurante {
    private volatile boolean abierto = true;

    public boolean estanAbiertas() { return abierto; }

    public void cerrar() { abierto = false; }
}
