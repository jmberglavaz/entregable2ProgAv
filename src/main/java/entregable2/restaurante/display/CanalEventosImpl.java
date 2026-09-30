package main.java.entregable2.restaurante.display;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CanalEventosImpl implements CanalEventos {

    private final BlockingQueue<Evento> cola = new LinkedBlockingQueue<>();
    private static final long TIMEOUT_MS = 200;

    @Override
    public void publicar(Evento evento) throws InterruptedException {
        cola.put(evento);
    }

    @Override
    public Evento siguiente() throws InterruptedException {
        return cola.poll(TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean hayEventos() {
        return !cola.isEmpty();
    }
}