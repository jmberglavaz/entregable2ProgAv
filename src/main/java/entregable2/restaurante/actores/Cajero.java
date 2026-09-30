package main.java.entregable2.restaurante.actores;

import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;
import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.Evento;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.util.Aleatorio;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cajero: consume clientes de la cola de cobros y simula el tiempo de cobro.
 *
 * Herramientas:
 *  - Thread/Runnable.
 *  - ColaCobros: BlockingQueue → pasaje de mensajes.
 *  - AtomicBoolean para cierre cooperativo.
 *
 * Patrón: Productor-Consumidor.
 *   - Productores: Clientes al encolarse.
 *   - Consumidor: Cajeros.
 *
 * Una única cola compartida por los Y cajeros: el primero que llama a tomar()
 * se lleva al primer cliente de la cola (FIFO). Esto lo garantiza
 * BlockingQueue, sin necesidad de sincronización extra.
 */
public class Cajero implements Runnable {

    private final int id;
    private final ColaCobros colaCobros;
    private final CanalEventos canalEventos;
    private final LogSimulacion log;
    private final Configuracion config;
    private final AtomicBoolean activo;

    public Cajero(int id,
                  ColaCobros colaCobros,
                  CanalEventos canalEventos,
                  LogSimulacion log,
                  Configuracion config,
                  AtomicBoolean activo) {
        this.id = id;
        this.colaCobros = colaCobros;
        this.canalEventos = canalEventos;
        this.log = log;
        this.config = config;
        this.activo = activo;
    }

    @Override
    public void run() {
        log.log("Cajero " + id + " inicia turno");
        try {
            while (activo.get() && !Thread.currentThread().isInterrupted()) {
                Cliente cliente;
                try {
                    // Bloquea hasta que haya un cliente en la cola.
                    // Sale por InterruptedException al cierre.
                    cliente = colaCobros.sacarDeLaCola();
                } catch (InterruptedException e) {
                    break;
                }
                if (cliente == null) continue;

                cobrar(cliente);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.log("Cajero " + id + " termina turno");
    }

    private void cobrar(Cliente cliente) throws InterruptedException {
        // 1. Simular demora de cobro: TYmin..TYmax
        long demora = Aleatorio.entre(config.getTyMin(), config.getTyMax());
        Thread.sleep(demora);

        // 2. Notificar al display y al log
        canalEventos.publicar(new Evento("CAJERO", id,
                "Cajero " + id + " cobró a cliente " + cliente.getId()));
        log.accion(id, "Cajero " + id + " cobró a cliente " + cliente.getId());

        // El cliente ya se retiró de forma asíncrona al encolarse,
        // así que no hay nada más que hacer acá.
    }
}
