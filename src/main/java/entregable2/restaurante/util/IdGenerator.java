package main.java.entregable2.restaurante.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generadores de IDs con AtomicInteger.getAndIncrement(): atómico, sin locks,
 * y no repite IDs aunque lo llamen varios hilos a la vez.
 */
public final class IdGenerator {

    private static final AtomicInteger PEDIDO_ID = new AtomicInteger(1);
    private static final AtomicInteger PLATO_ID = new AtomicInteger(1);

    private IdGenerator() { }

    public static int nextPedidoId() { return PEDIDO_ID.getAndIncrement(); }
    public static int nextPlatoId() { return PLATO_ID.getAndIncrement(); }
}