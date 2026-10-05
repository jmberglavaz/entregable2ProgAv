package main.java.entregable2.restaurante.concurrencia;

import java.util.concurrent.CountDownLatch;

/**
 * Barrera de inicio: todos los hilos esperan hasta que el Simulador
 * dé la orden de arranque.
 *
 * Herramienta: CountDownLatch
 *
 * Uso:
 *   - Simulador llama a liberar() cuando todos los hilos están creados.
 *   - Cada actor llama a esperarOrdenDeInicio() al empezar su run().
 */
public class SincronizacionInicio {

    private final CountDownLatch latch;

    public SincronizacionInicio(int cantidadActores) {
        this.latch = new CountDownLatch(1); // 1 solo countdown: el Simulador
    }

    /**
     * Los actores llaman a este método al inicio de su run().
     * Bloquea hasta que el Simulador llame a liberar().
     */
    public void esperarOrdenDeInicio() throws InterruptedException {
        latch.await();
    }

    /**
     * El Simulador llama a este método cuando todos los hilos están listos.
     * Libera a todos los actores de golpe.
     */
    public void liberar() {
        latch.countDown();
    }

    /**
     * Getter para debug: cuántos faltan para liberar.
     */
    public long getCuenta() {
        return latch.getCount();
    }
}