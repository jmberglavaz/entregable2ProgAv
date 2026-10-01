package main.java.entregable2.restaurante.simulacion;


import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.concurrencia.AforoRestaurante;
import main.java.entregable2.restaurante.concurrencia.PuertasRestaurante;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.Evento;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Mesa;
import main.java.entregable2.restaurante.modelo.enums.EstadoMesa;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GestorCierre implements Runnable {

    private final Configuracion config;
    private final PuertasRestaurante puertas;
    private final AtomicBoolean activo;
    private final List<Mesa> mesas;
    private final ColaCobros colaCobros;
    private final AforoRestaurante aforo;
    private final AtomicInteger clientesCobrando;   // ← NUEVO
    private final CanalEventos canalEventos;
    private final LogSimulacion log;

    public GestorCierre(Configuracion config,
                        PuertasRestaurante puertas,
                        AtomicBoolean activo,
                        List<Mesa> mesas,
                        ColaCobros colaCobros,
                        AforoRestaurante aforo,
                        AtomicInteger clientesCobrando,   // ← NUEVO
                        CanalEventos canalEventos,
                        LogSimulacion log) {
        this.config = config;
        this.puertas = puertas;
        this.activo = activo;
        this.mesas = mesas;
        this.colaCobros = colaCobros;
        this.aforo = aforo;
        this.clientesCobrando = clientesCobrando;
        this.canalEventos = canalEventos;
        this.log = log;
    }

    /**
     * Cierre ordenado del restaurante (se ejecuta en el hilo del Reloj).
     *
     * 1. puertas.cerrar(): escribe un campo volatile -> el GeneradorClientes y los
     *    Clientes ven el cambio de inmediato (sin caché) y dejan de entrar gente.
     * 2. Publica el evento "RESTAURANTE CERRADO" en el canal para el Display.
     * 3. Espera a que se vacíe el local (esperarClientesTerminen).
     * 4. activo.set(false): señal cooperativa que hace salir a Mozos, Cocineros,
     *    Cajeros y Display de sus bucles.
     */
    public void iniciarCierre() {
        log.log("=== INICIANDO CIERRE DEL RESTAURANTE ===");
        System.out.println("\n*** RESTAURANTE CERRADO ***\n");

        puertas.cerrar();

        try {
            canalEventos.publicar(new Evento("SISTEMA", 0,
                    "RESTAURANTE CERRADO — NO ENTRAN MÁS CLIENTES"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        esperarClientesTerminen();

        log.log("=== TODOS LOS CLIENTES SE RETIRARON ===");
        try {
            canalEventos.publicar(new Evento("SISTEMA", 0,
                    "TODOS LOS CLIENTES SE RETIRARON"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        activo.set(false);
    }

    /**
     * Espera activa con sleep(500): consulta la condición cada medio segundo.
     * Si el hilo es interrumpido, restaura el flag y sale del bucle.
     */
    private void esperarClientesTerminen() {
        try {
            while (hayClientesEnRestaurante()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Condición de "todavía hay trabajo". Devuelve true si:
     *  - alguna mesa tiene ocupantes o está en LIMPIEZA
     *    (Mesa.estaVacia() y getEstado() son synchronized: lectura consistente),
     *  - la cola de cobros no está vacía, o
     *  - algún cajero está cobrando (AtomicInteger clientesCobrando > 0).
     *
     * Es solo lectura: no toma locks propios, por lo que no puede causar deadlock.
     */
    private boolean hayClientesEnRestaurante() {
        // Mesas con clientes o en LIMPIEZA
        for (Mesa mesa : mesas) {
            if (!mesa.estaVacia()) return true;
            if (mesa.getEstado() == EstadoMesa.LIMPIEZA) return true;
        }

        //Cola de cobros no vacía
        if (colaCobros.tamano() > 0) return true;

        // Cajeros cobrando
        if (clientesCobrando.get() > 0) return true;

        return false;
    }

    @Override
    public void run() {
        iniciarCierre();
    }
}