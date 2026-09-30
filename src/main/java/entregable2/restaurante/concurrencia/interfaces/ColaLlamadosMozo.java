package main.java.entregable2.restaurante.concurrencia.interfaces;

import main.java.entregable2.restaurante.modelo.Mesa;

public interface ColaLlamadosMozo {
    void llamarMozo(Mesa mesa) throws InterruptedException;
    Mesa esperarLlamado() throws InterruptedException;
    boolean hayLlamados();
}
