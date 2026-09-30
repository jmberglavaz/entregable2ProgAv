package main.java.entregable2.restaurante.display;

import main.java.entregable2.restaurante.log.LogSimulacion;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Display del restaurante.
 *
 * Hilo dedicado a consumir eventos de CanalEventos y mostrarlos en pantalla.
 *
 * Según el PDF:
 *  - "Sugerimos un hilo encargado de esta tarea, que reciba las notificaciones
 *     de cada cambio, en una cola y los vaya procesando en orden."
 *  - "Al finalizar el tiempo T, el display deberá continuar mostrando todos los
 *     cambios realizados y cuando no se tenga ningún cambio más en la cola se finaliza."
 *
 * Herramientas:
 *  - CanalEventos (BlockingQueue).
 *  - AtomicBoolean para el flag activo.
 */
public class Display implements Runnable {

    private final CanalEventos canalEventos;
    private final AtomicBoolean activo;
    private final LogSimulacion log;

    public Display(CanalEventos canalEventos,
                   AtomicBoolean activo,
                   LogSimulacion log) {
        this.canalEventos = canalEventos;
        this.activo = activo;
        this.log = log;
    }

    @Override
    public void run() {
        System.out.println("=== DISPLAY INICIADO ===");
        log.log("Display: iniciado");

        try {
            // Mientras el restaurante esté activo O haya eventos pendientes
            while (activo.get() || canalEventos.hayEventos()) {
                Evento evento = canalEventos.siguiente();
                if (evento != null) {
                    mostrar(evento);
                } else {
                    // No hay eventos, esperar un poco
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("=== DISPLAY FINALIZADO ===");
        log.log("Display: finalizado");
    }

    /**
     * Muestra un evento en pantalla.
     * Limpia la pantalla y muestra el evento + (opcionalmente) el snapshot.
     */
    private void mostrar(Evento evento) {
        // Limpiar pantalla (ANSI escape)
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("========================================");
        System.out.println("EVENTO: " + evento.getMensaje());
        System.out.println("Actor: " + evento.getTipo() + " #" + evento.getIdActor());
        System.out.println("========================================");
        System.out.println("(El snapshot completo se agregará después)");
        System.out.println("========================================");
    }
}