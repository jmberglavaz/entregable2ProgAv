package main.java.entregable2.restaurante.concurrencia.implementaciones;

import main.java.entregable2.restaurante.concurrencia.interfaces.ColaPedidos;
import main.java.entregable2.restaurante.modelo.Pedido;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementación de ColaPedidos usando BlockingQueue.
 *
 * Herramienta: LinkedBlockingQueue.
 * Patrón: Productor-Consumidor.
 *   - Productores: Mozos (agregan pedidos al tomar de una mesa).
 *   - Consumidores: Cocineros (retiran pedidos para cocinar).
 *
 * Sin tamaño fijo: los pedidos pueden acumularse sin bloquear al mozo.
 * Los cocineros se bloquean en tomar() si no hay pedidos.
 */
public class ColaPedidosImpl implements ColaPedidos {

    private final BlockingQueue<Pedido> cola = new LinkedBlockingQueue<>();

    @Override
    public void agregarALaCola(Pedido pedido) throws InterruptedException {
        cola.put(pedido);
    }

    @Override
    public Pedido tomar() throws InterruptedException {
        return cola.take();
    }

    @Override
    public int tamano() {
        return cola.size();
    }

    @Override
    public boolean estaVacia() {
        return cola.isEmpty();
    }
}