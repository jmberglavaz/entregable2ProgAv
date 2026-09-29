package main.java.entregable2.restaurante.util;

import java.util.concurrent.ThreadLocalRandom;

public final class Aleatorio {
    private Aleatorio() { }
    public static long entre(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }
}