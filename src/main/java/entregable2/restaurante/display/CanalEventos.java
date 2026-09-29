package main.java.entregable2.restaurante.display;

/**
 * Canal de eventos: actores (productores) → display (consumidor).
 * Hereda de BlockingQueue para que el display haga take() en bucle.
 */
public interface CanalEventos {
    void publicar(Evento evento) throws InterruptedException;
    Evento siguiente() throws InterruptedException;
    boolean hayEventos();
}
