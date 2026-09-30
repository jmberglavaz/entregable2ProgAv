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

    private void esperarClientesTerminen() {
        try {
            while (hayClientesEnRestaurante()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean hayClientesEnRestaurante() {
        // 1. Mesas con clientes o en LIMPIEZA
        for (Mesa mesa : mesas) {
            if (!mesa.estaVacia()) return true;
            if (mesa.getEstado() == EstadoMesa.LIMPIEZA) return true;
        }

        // 2. Cola de cobros no vacía
        if (colaCobros.tamano() > 0) return true;

        // 3. Cajeros cobrando
        if (clientesCobrando.get() > 0) return true;

        return false;
    }

    @Override
    public void run() {
        iniciarCierre();
    }
}