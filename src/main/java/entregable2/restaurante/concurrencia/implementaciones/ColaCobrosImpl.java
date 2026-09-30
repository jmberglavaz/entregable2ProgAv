package main.java.entregable2.restaurante.concurrencia.implementaciones;

import main.java.entregable2.restaurante.actores.Cliente;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementación de ColaCobros con BlockingQueue.
 *
 * Herramienta: LinkedBlockingQueue (pág. 83-84 del PDF).
 * Patrón: Productor-Consumidor (pág. 96-99).
 *   - Productores: Clientes (encolan al terminar de comer).
 *   - Consumidores: Cajeros (retiran para cobrar).
 *
 * Sin tamaño fijo: en la práctica, el aforo P*M limita cuántos pueden estar
 * simultáneamente, pero la cola de cobro podría crecer si los cajeros son pocos.
 */
public class ColaCobrosImpl implements ColaCobros {

    private final BlockingQueue<Cliente> cola = new LinkedBlockingQueue<>();

    @Override
    public void agregarALaCola(Cliente c) throws InterruptedException {
        cola.put(c);
    }

    @Override
    public Cliente sacarDeLaCola() throws InterruptedException {
        return cola.take();  // bloquea hasta que haya cliente
    }

    @Override
    public int tamano() {
        return cola.size();
    }
}
