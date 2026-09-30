package main.java.entregable2.restaurante.simulacion;

import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Mesa;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gestor de cierre de la simulación.
 *
 * Responsabilidades:
 *  1. Cerrar las puertas (restauranteAbierto = false).
 *  2. Esperar a que todos los clientes terminen (mesas vacías).
 *  3. Detener todos los hilos (activo = false).
 *
 * Herramientas:
 *  - AtomicBoolean para flags thread-safe.
 *  - Thread.sleep para esperar entre chequeos.
 */
public class GestorCierre implements Runnable {

    private final Configuracion config;
    private final AtomicBoolean restauranteAbierto;
    private final AtomicBoolean activo;
    private final List<Mesa> mesas;
    private final LogSimulacion log;

    public GestorCierre(Configuracion config,
                        AtomicBoolean restauranteAbierto,
                        AtomicBoolean activo,
                        List<Mesa> mesas,
                        LogSimulacion log) {
        this.config = config;
        this.restauranteAbierto = restauranteAbierto;
        this.activo = activo;
        this.mesas = mesas;
        this.log = log;
    }

    /**
     * Inicia el proceso de cierre.
     * Lo llama el RelojSimulacion cuando T se cumple.
     */
    public void iniciarCierre() {
        log.log("=== INICIANDO CIERRE DEL RESTAURANTE ===");
        System.out.println("\n*** RESTAURANTE CERRADO — NO ENTRAN MÁS CLIENTES ***\n");

        // 1. Cerrar puertas (impedir nuevos clientes)
        restauranteAbierto.set(false);

        // 2. Esperar a que todos los clientes terminen
        esperarClientesTerminen();

        // 3. Detener todos los hilos
        log.log("=== TODOS LOS CLIENTES SE RETIRARON — DETENIENDO HILOS ===");
        activo.set(false);
    }

    /**
     * Espera a que todas las mesas estén vacías.
     * Chequea cada 500ms.
     */
    private void esperarClientesTerminen() {
        log.log("GestorCierre: esperando que los clientes terminen...");
        try {
            while (hayClientesEnRestaurante()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log("GestorCierre: interrumpido mientras esperaba");
        }
    }

    /**
     * @return true si hay al menos una mesa con clientes.
     */
    private boolean hayClientesEnRestaurante() {
        for (Mesa mesa : mesas) {
            if (!mesa.estaVacia()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        // Este run() se ejecuta si el GestorCierre se lanza como hilo aparte.
        // Pero normalmente lo llama RelojSimulacion.iniciarCierre().
        // Dejamos el run() por compatibilidad con Runnable.
        iniciarCierre();
    }
}