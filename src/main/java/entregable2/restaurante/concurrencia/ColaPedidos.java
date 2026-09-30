package main.java.entregable2.restaurante.concurrencia;

import main.java.entregable2.restaurante.modelo.Pedido;

/**
 * Cola de pedidos: clientes/mozos (productores) → cocina (consumidores).
 * Patrón: Productor-Consumidor (pág. 96-99 del PDF).
 * Herramienta: BlockingQueue (pág. 83-84).
 */
public interface ColaPedidos {
    void agregarALaCola(Pedido pedido) throws InterruptedException;
    Pedido tomar() throws InterruptedException;
    int tamano();
    boolean estaVacia();
}