package main.java.entregable2.restaurante.modelo;

import main.java.entregable2.restaurante.modelo.enums.EstadoMesa;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Recurso compartido entre clientes y mozos.
 * Herramientas: synchronized + wait/notifyAll.
 * Patrón: Monitor.
 */
public class Mesa {

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    private final int id;
    private final int capacidad;
    private EstadoMesa estado = EstadoMesa.LIBRE;
    private int ocupantes = 0;
    private int clientesEligieron = 0;
    private int clientesServidos = 0;

    public Mesa(int capacidad) {
        this.id = ID_GEN.getAndIncrement();
        this.capacidad = capacidad;
    }

    /** Intenta ocupar la mesa. Solo tiene éxito si está LIBRE. */
    public synchronized boolean intentarOcupar() {
        if (estado != EstadoMesa.LIBRE) return false;
        estado = EstadoMesa.ESPERANDO_PEDIDO;
        ocupantes = capacidad;
        clientesEligieron = 0;
        clientesServidos = 0;
        return true;
    }

    /** El mozo la deja lista para reutilizar. */
    public synchronized void marcarLibre() {
        estado = EstadoMesa.LIBRE;
        ocupantes = 0;
        clientesEligieron = 0;
        clientesServidos = 0;
        notifyAll();
    }

    /**
     * Cada cliente avisa que ya eligió su menú.
     * @return true si este cliente fue el último (debe avisar al mozo).
     */
    public synchronized boolean registrarMenuElegido() {
        clientesEligieron++;
        return clientesEligieron == capacidad;
    }

    /**
     * El mozo avisa que sirvió un plato.
     * @return true si ya se sirvieron todos (dispara el "a comer").
     */
    public synchronized boolean registrarPlatoServido() {
        clientesServidos++;
        if (clientesServidos == capacidad) {
            estado = EstadoMesa.COMIENDO;
            notifyAll();
            return true;
        }
        return false;
    }

    /** Los clientes esperan acá hasta que todos estén servidos. */
    public synchronized void esperarComida() throws InterruptedException {
        while (estado != EstadoMesa.COMIENDO) {
            wait();
        }
    }

    /** Cada cliente avisa que se retira. */
    public synchronized void clienteSeVa() {
        if (ocupantes > 0) ocupantes--;
        if (ocupantes == 0) {
            estado = EstadoMesa.LIMPIEZA;
        }
        notifyAll();
    }

    public synchronized boolean estaVacia() { return ocupantes == 0; }
    public synchronized EstadoMesa getEstado() { return estado; }
    public int getId() { return id; }
    public int getCapacidad() { return capacidad; }
}
