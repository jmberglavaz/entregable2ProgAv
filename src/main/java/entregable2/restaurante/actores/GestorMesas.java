package main.java.entregable2.restaurante.actores;

import main.java.entregable2.restaurante.modelo.Mesa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Agrupa clientes de a P y les asigna una mesa libre.
 *
 * Patrón: Maestro-Trabajadores + Productor-Consumidor.
 * Herramientas:
 *  - BlockingQueue para pasaje de mensajes entre clientes y hostess.
 *  - ReentrantLock + Condition para exclusión mutua al elegir mesa.
 */
public final class GestorMesas implements Runnable {

    private record Solicitud(Cliente cliente, BlockingQueue<Mesa> respuesta) { }

    private final int p;
    private final List<Mesa> mesas;
    private final BlockingQueue<Solicitud> colaSolicitudes = new LinkedBlockingQueue<>();
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition mesaLibre = lock.newCondition();
    private volatile boolean activo = true;

    public GestorMesas(int p, List<Mesa> mesas) {
        this.p = p;
        this.mesas = mesas;
    }

    /** Llamado por cada Cliente. Bloquea hasta que le toque mesa. */
    public Mesa solicitarMesa(Cliente c) throws InterruptedException {
        BlockingQueue<Mesa> respuesta = new ArrayBlockingQueue<>(1);
        colaSolicitudes.put(new Solicitud(c, respuesta));
        return respuesta.take();
    }

    /** Llamado por el Mozo cuando terminó de limpiar una mesa. */
    public void liberarMesa(Mesa m) {
        lock.lock();
        try {
            m.marcarLibre();
            mesaLibre.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void detener() { activo = false; }

    @Override
    public void run() {
        try {
            while (activo) {
                // 1. Agrupar de a P (bloquea hasta que haya P solicitudes)
                List<Solicitud> grupo = new ArrayList<>(p);
                for (int i = 0; i < p; i++) {
                    grupo.add(colaSolicitudes.take());
                }
                // 2. Conseguir mesa libre
                Mesa mesa = esperarMesaLibre();
                // 3. Avisar a los P del grupo
                for (Solicitud s : grupo) {
                    s.respuesta().put(mesa);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Mesa esperarMesaLibre() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                for (Mesa m : mesas) {
                    if (m.intentarOcupar()) {
                        return m;
                    }
                }
                mesaLibre.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
