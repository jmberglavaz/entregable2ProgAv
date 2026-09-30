package main.java.entregable2.restaurante.actores;


import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;
import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.Evento;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.util.Aleatorio;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Cajero implements Runnable {

    private final int id;
    private final ColaCobros colaCobros;
    private final CanalEventos canalEventos;
    private final LogSimulacion log;
    private final Configuracion config;
    private final AtomicBoolean activo;
    private final AtomicInteger clientesCobrando;   // ← NUEVO

    public Cajero(int id,
                  ColaCobros colaCobros,
                  CanalEventos canalEventos,
                  LogSimulacion log,
                  Configuracion config,
                  AtomicBoolean activo,
                  AtomicInteger clientesCobrando) {   // ← NUEVO
        this.id = id;
        this.colaCobros = colaCobros;
        this.canalEventos = canalEventos;
        this.log = log;
        this.config = config;
        this.activo = activo;
        this.clientesCobrando = clientesCobrando;
    }

    @Override
    public void run() {
        log.log("Cajero " + id + " inicia turno");
        try {
            while (activo.get() && !Thread.currentThread().isInterrupted()) {
                Cliente cliente;
                try {
                    cliente = colaCobros.sacarDeLaCola();
                } catch (InterruptedException e) {
                    break;
                }
                if (cliente == null) continue;

                clientesCobrando.incrementAndGet();   // ← NUEVO
                try {
                    cobrar(cliente);
                } finally {
                    clientesCobrando.decrementAndGet();   // ← NUEVO
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.log("Cajero " + id + " termina turno");
    }

    private void cobrar(Cliente cliente) throws InterruptedException {
        long demora = Aleatorio.entre(config.getTyMin(), config.getTyMax());
        Thread.sleep(demora);

        canalEventos.publicar(new Evento("CAJERO", id,
                "Cajero " + id + " cobró a cliente " + cliente.getId()));
        log.accion(id, "Cajero " + id + " cobró a cliente " + cliente.getId());
    }
}