package main.java.entregable2.restaurante.concurrencia.implementaciones;

import main.java.entregable2.restaurante.concurrencia.interfaces.MostradorPlatos;
import main.java.entregable2.restaurante.modelo.Plato;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementación de MostradorPlatos usando BlockingQueue.
 *
 * Herramienta: LinkedBlockingQueue.
 * Patrón: Productor-Consumidor.
 *   - Productores: Cocineros (dejan platos listos).
 *   - Consumidores: Mozos (retiran platos para servir).
 *
 * OJO: el Mozo usa retirar() con timeout (poll) y devuelve null si no hay
 * plato en el momento. Ver Mozo.java:
 *     Plato plato = mostradorPlatos.retirar();
 *     if (plato != null) { ... }
 * Eso implica que retirar() NO puede bloquearse indefinidamente.
 */
public class MostradorPlatosImpl implements MostradorPlatos {

    private static final long TIMEOUT_MS = 200;

    private final BlockingQueue<Plato> mostrador = new LinkedBlockingQueue<>();

    @Override
    public void poner(Plato plato) throws InterruptedException {
        mostrador.put(plato);
    }

    @Override
    public Plato retirar() throws InterruptedException {
        // Poll con timeout: devuelve null si no hay plato en 200ms.
        // El Mozo lo usa así para poder alternar entre servir, tomar pedidos y limpiar.
        return mostrador.poll(TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public int tamano() {
        return mostrador.size();
    }
}
