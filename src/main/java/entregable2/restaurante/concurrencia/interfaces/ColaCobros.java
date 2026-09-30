package main.java.entregable2.restaurante.concurrencia.interfaces;

import main.java.entregable2.restaurante.actores.Cliente;

/**
 * Cola única de cobro: clientes → cajeros.
 */
public interface ColaCobros {
    void agregarALaCola(Cliente cliente) throws InterruptedException;
    Cliente sacarDeLaCola() throws InterruptedException;
    int tamano();
}
