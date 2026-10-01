package main.java.entregable2.restaurante.simulacion;


import main.java.entregable2.restaurante.actores.Cajero;
import main.java.entregable2.restaurante.actores.Cliente;
import main.java.entregable2.restaurante.actores.Cocinero;
import main.java.entregable2.restaurante.actores.GestorMesas;
import main.java.entregable2.restaurante.actores.Mozo;
import main.java.entregable2.restaurante.concurrencia.AforoRestaurante;
import main.java.entregable2.restaurante.concurrencia.PuertasRestaurante;
import main.java.entregable2.restaurante.concurrencia.SincronizacionInicio;
import main.java.entregable2.restaurante.concurrencia.implementaciones.CanalEventosImpl;
import main.java.entregable2.restaurante.concurrencia.implementaciones.ColaCobrosImpl;
import main.java.entregable2.restaurante.concurrencia.implementaciones.ColaLlamadosMozoImpl;
import main.java.entregable2.restaurante.concurrencia.implementaciones.ColaPedidosImpl;
import main.java.entregable2.restaurante.concurrencia.implementaciones.MostradorPlatosImpl;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaLlamadosMozo;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaPedidos;
import main.java.entregable2.restaurante.concurrencia.interfaces.MostradorPlatos;
import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.Display;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Menu;
import main.java.entregable2.restaurante.modelo.Mesa;
import main.java.entregable2.restaurante.util.Aleatorio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Orquestador de la simulación (hilo "main").
 *
 * Crea los recursos compartidos (colas, mesas, aforo, puertas), lanza todos los
 * actores en un pool de hilos y coordina el apagado.
 *
 * Concurrencia:
 *  - AtomicBoolean activo: bandera de "simulación en curso" que leen todos los actores.
 *    Es atómica y visible entre hilos (equivale a volatile), por eso no se cachea.
 *  - AtomicInteger clientesCobrando: cuenta cuántos cajeros están cobrando ahora
 *    (lo usa GestorCierre para saber si todavía hay trabajo en curso).
 *  - Las colas son interfaces (ColaPedidos, MostradorPlatos...) con implementación
 *    sobre BlockingQueue: los actores solo conocen el contrato, no la estructura.
 */

public class Simulador {

    private final Configuracion config;
    private final LogSimulacion log;

    private final AtomicBoolean activo = new AtomicBoolean(true);
    private final AtomicInteger clientesCobrando = new AtomicInteger(0);

    private final ColaPedidos colaPedidos;
    private final MostradorPlatos mostradorPlatos;
    private final ColaCobros colaCobros;
    private final ColaLlamadosMozo colaLlamadosMozo;
    private final BlockingQueue<Mesa> mesasParaLimpiar;
    private final CanalEventos canalEventos;
    private final SincronizacionInicio inicio;
    private final PuertasRestaurante puertas;

    private final AforoRestaurante aforo;
    private final GestorMesas gestorMesas;

    private final List<Mesa> mesas;
    private final List<Menu> menuDisponible;

    private ExecutorService pool;
    private Display display;

    public Simulador(Configuracion config, LogSimulacion log) {
        this.config = config;
        this.log = log;

        this.colaPedidos = new ColaPedidosImpl();
        this.mostradorPlatos = new MostradorPlatosImpl();
        this.colaCobros = new ColaCobrosImpl();
        this.colaLlamadosMozo = new ColaLlamadosMozoImpl();
        this.mesasParaLimpiar = new LinkedBlockingQueue<>();
        this.canalEventos = new CanalEventosImpl();
        this.inicio = new SincronizacionInicio(1);
        this.puertas = new PuertasRestaurante();

        this.mesas = new ArrayList<>();
        for (int i = 0; i < config.getM(); i++) {
            mesas.add(new Mesa(config.getP()));
        }

        this.aforo = new AforoRestaurante(config.getM() * config.getP());
        this.gestorMesas = new GestorMesas(config.getP(), mesas);

        this.menuDisponible = crearMenus();
    }

    /**
     * Arranca y apaga toda la simulación. Corre en el hilo main.
     *
     * Pasos:
     *  1. Crea el pool fijo de hilos (Z + C + Y + 100): cubre mozos, cocineros, cajeros
     *     y deja margen para los clientes concurrentes, que son tareas Runnable.
     *  2. Envía al pool: GestorMesas, Mozos, Cocineros, Cajeros, Display y RelojSimulacion.
     *  3. Crea un Thread aparte "GeneradorClientes" que, cada TPmin..TPmax ms, crea un
     *     Cliente y lo envía al pool mientras las puertas estén abiertas.
     *  4. inicio.liberar() hace countDown() del CountDownLatch: es la "largada" común.
     *  5. generador.join(): main se bloquea hasta que dejan de llegar clientes
     *     (se cerraron las puertas).
     *  6. Apagado ordenado: gestorMesas.detener() -> pool.shutdown() ->
     *     awaitTermination(5s) -> shutdownNow() si no terminó (interrumpe a los hilos
     *     que sigan bloqueados).
     *  7. Cierra el display y el archivo de log.
     */
    public void iniciar() {
        log.log("=== SIMULACIÓN INICIADA ===");
        log.log("Config: " + config);

        int tamanoPool = config.getZ() + config.getC() + config.getY() + 100;
        pool = Executors.newFixedThreadPool(tamanoPool);

        pool.submit(gestorMesas);

        // Mozos
        for (int i = 0; i < config.getZ(); i++) {
            pool.submit(new Mozo(i + 1, colaPedidos, mostradorPlatos,
                    colaLlamadosMozo, mesas, canalEventos, log, activo, config,
                    mesasParaLimpiar));
        }

        // Cocineros
        for (int i = 0; i < config.getC(); i++) {
            pool.submit(new Cocinero(i + 1, colaPedidos, mostradorPlatos,
                    canalEventos, log, config, activo));
        }

        // Cajeros
        for (int i = 0; i < config.getY(); i++) {
            pool.submit(new Cajero(i + 1, colaCobros, canalEventos, log, config,
                    activo, clientesCobrando));
        }

        // Display
        this.display = new Display(canalEventos, activo, log,
                mesas, colaPedidos, mostradorPlatos, colaCobros, colaLlamadosMozo);
        pool.submit(display);

        // Reloj + GestorCierre
        GestorCierre gestorCierre = new GestorCierre(config, puertas, activo, mesas,
                colaCobros, aforo, clientesCobrando, canalEventos, log);
        pool.submit(new RelojSimulacion(config, gestorCierre, log));

        // Generador de clientes
        Thread generador = new Thread(() -> {
            try {
                inicio.esperarOrdenDeInicio();
                while (activo.get() && puertas.estanAbiertas()) {
                    long espera = Aleatorio.entre(config.getTpMin(), config.getTpMax());
                    Thread.sleep(espera);
                    if (!puertas.estanAbiertas()) break;

                    Cliente c = new Cliente(config, aforo, puertas, gestorMesas,
                            colaCobros, null, colaLlamadosMozo, mesasParaLimpiar,
                            menuDisponible, inicio, log, canalEventos);
                    pool.submit(c);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "GeneradorClientes");
        generador.start();

        inicio.liberar();

        // Esperar a que el GeneradorClientes termine
        try {
            generador.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //Cerrar el pool
        gestorMesas.detener();  // ← primero
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {  // ← 5 segundos, no minutos
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
        }

        //Marcar el Display como finalizado
        if (display != null) {
            display.cerrar();
        }

        log.log("=== SIMULACIÓN FINALIZADA ===");
        log.cerrar();
        System.out.println("Simulación finalizada.");
    }

    private List<Menu> crearMenus() {
        List<Menu> menus = new ArrayList<>();
        menus.add(new Menu(1, "Ensalada", 1000, 2000));
        menus.add(new Menu(2, "Pasta", 2000, 4000));
        menus.add(new Menu(3, "Asado", 4000, 6000));
        return menus;
    }
}