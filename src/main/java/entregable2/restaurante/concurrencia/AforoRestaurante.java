package main.java.entregable2.restaurante.concurrencia;

import java.util.concurrent.Semaphore;

/**
 * Semáforo que limita cuántos clientes hay adentro.
 * Herramienta: Semaphore.
 */
public final class AforoRestaurante {
    private final Semaphore permisos;

    public AforoRestaurante(int capacidad) {
        this.permisos = new Semaphore(capacidad, true); // fair: evita inanición
    }

    public void entrar() throws InterruptedException { permisos.acquire(); }
    public void salir() { permisos.release(); }
    public int disponibles() { return permisos.availablePermits(); }
}
