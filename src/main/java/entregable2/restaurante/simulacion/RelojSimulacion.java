package main.java.entregable2.restaurante.simulacion;

import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.log.LogSimulacion;

/**
 * Duerme T segundos y luego dispara el GestorCierre.
 *
 * Herramienta: Thread con sleep.
 */
public class RelojSimulacion implements Runnable {

    private final Configuracion config;
    private final GestorCierre gestorCierre;
    private final LogSimulacion log;

    public RelojSimulacion(Configuracion config,
                           GestorCierre gestorCierre,
                           LogSimulacion log) {
        this.config = config;
        this.gestorCierre = gestorCierre;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            long tiempoMs = config.getT() * 1000L;
            log.log("Reloj: esperando T=" + config.getT() + "s (" + tiempoMs + "ms)");
            Thread.sleep(tiempoMs);

            log.log("Reloj: T cumplido, disparando cierre");
            gestorCierre.iniciarCierre();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log("Reloj: interrumpido");
        }
    }
}