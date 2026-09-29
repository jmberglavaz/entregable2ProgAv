package main.java.entregable2.restaurante.concurrencia;

import main.java.entregable2.restaurante.modelo.Plato;

/**
 * Mostrador de platos listos: cocina (productor) → mozos (consumidores).
 * Pasaje de mensajes vía BlockingQueue.
 */
public interface MostradorPlatos {
    void poner(Plato plato) throws InterruptedException;
    Plato retirar() throws InterruptedException;
    int tamanio();
}
