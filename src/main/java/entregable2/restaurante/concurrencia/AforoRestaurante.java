package main.java.entregable2.restaurante.concurrencia;

import java.util.concurrent.Semaphore;

/**
 * Límite de personas dentro del restaurante (capacidad = M * P).
 *
 * Herramienta: Semaphore con fairness = true.
 *  - entrar() = acquire(): bloquea al cliente si el local está lleno.
 *  - salir()  = release(): libera un lugar al retirarse.
 *  - fair = true: los clientes entran en orden de llegada (FIFO), evita inanición.
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
