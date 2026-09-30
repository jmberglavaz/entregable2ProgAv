package main.java.entregable2.restaurante.concurrencia.interfaces;

import main.java.entregable2.restaurante.modelo.Pedido;

/**
 * Cola de pedidos: clientes/mozos (productores) → cocina (consumidores).
 * Patrón: Productor-Consumidor.
 * Herramienta: BlockingQueue.
 */
public interface ColaPedidos {
    void agregarALaCola(Pedido pedido) throws InterruptedException;
    Pedido tomar() throws InterruptedException;
    int tamano();
    boolean estaVacia();
}