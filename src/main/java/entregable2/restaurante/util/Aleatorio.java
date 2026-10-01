package main.java.entregable2.restaurante.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Número aleatorio en [min, max] con ThreadLocalRandom: cada hilo tiene su propio
 * generador, así no hay contención (a diferencia de un único Random compartido).
 */
public final class Aleatorio {
    private Aleatorio() { }
    public static long entre(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }
}