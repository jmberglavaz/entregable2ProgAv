package main.java.entregable2.restaurante.display;

import main.java.entregable2.restaurante.concurrencia.ColaLlamadosMozo;
import main.java.entregable2.restaurante.modelo.Mesa;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ColaLlamadosMozoImpl implements ColaLlamadosMozo {

    private final BlockingQueue<Mesa> cola = new LinkedBlockingQueue<>();
    private static final long TIMEOUT_MS = 200;

    @Override
    public void llamarMozo(Mesa mesa) throws InterruptedException {
        cola.put(mesa);
    }

    @Override
    public Mesa esperarLlamado() throws InterruptedException {
        return cola.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean hayLlamados() {
        return !cola.isEmpty();
    }
}