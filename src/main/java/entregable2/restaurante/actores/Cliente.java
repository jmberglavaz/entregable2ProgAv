package main.java.entregable2.restaurante.actores;

import main.java.entregable2.restaurante.concurrencia.*;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaLlamadosMozo;
import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.Evento;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Menu;
import main.java.entregable2.restaurante.modelo.Mesa;
import main.java.entregable2.restaurante.util.Aleatorio;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cliente del restaurante.
 *
 * Herramientas:
 *  - Thread / Runnable.
 *  - BlockingQueue: pasaje de mensajes.
 *  - AforoRestaurante con Semaphore.
 *  - Mesa con synchronized + wait/notifyAll.
 *  - AtomicInteger para IDs.
 */
public class Cliente implements Runnable {

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);
    private static final Random RND = new Random();

    private final int id;
    private final Configuracion cfg;
    private final AforoRestaurante aforo;
    private final PuertasRestaurante puertas;
    private final GestorMesas gestorMesas;
    private final ColaCobros colaCobros;
    private final ColaLlamadosMozo colaLlamadosMozo;
    private final BlockingQueue<Mesa> mesasParaLimpiar;
    private final List<Menu> menuDisponible;
    private final SincronizacionInicio inicio;
    private final LogSimulacion log;
    private final CanalEventos eventos;

    // estado confinado al hilo (no necesita sync)
    private Menu menuElegido;
    private Mesa mesa;

    public Cliente(Configuracion cfg,
                   AforoRestaurante aforo,
                   PuertasRestaurante puertas,
                   GestorMesas gestorMesas,
                   ColaCobros colaCobros,
                   BlockingQueue<Mesa> llamarMozo, ColaLlamadosMozo colaLlamadosMozo,
                   BlockingQueue<Mesa> mesasParaLimpiar,
                   List<Menu> menuDisponible,
                   SincronizacionInicio inicio,
                   LogSimulacion log,
                   CanalEventos eventos) {
        this.id = ID_GEN.getAndIncrement();
        this.cfg = cfg;
        this.aforo = aforo;
        this.puertas = puertas;
        this.gestorMesas = gestorMesas;
        this.colaCobros = colaCobros;
        this.colaLlamadosMozo = colaLlamadosMozo;
        this.mesasParaLimpiar = mesasParaLimpiar;
        this.menuDisponible = menuDisponible;
        this.inicio = inicio;
        this.log = log;
        this.eventos = eventos;
    }

    @Override
    public void run() {
        try {
            inicio.esperarOrdenDeInicio();

            // 1. ¿Puertas abiertas? Si no, no entra.
            if (!puertas.estanAbiertas()) {
                log.accion(id, "Cliente " + id + " no entró: puertas cerradas");
                return;
            }

            // 2. Entrar (bloquea si está lleno el aforo)
            aforo.entrar();
            log.accion(id, "Cliente " + id + " ingresó al restaurante");
            eventos.publicar(Evento.clienteIngreso(id));

            // 3. Agruparse de a P y obtener mesa
            mesa = gestorMesas.solicitarMesa(this);
            log.accion(id, "Cliente " + id + " se sentó en mesa " + mesa.getId());
            eventos.publicar(Evento.clienteSentado(id, mesa.getId()));

            // 4. Seleccionar menú (tiempo aleatorio entre TMmin y TMmax)
            Thread.sleep(Aleatorio.entre(cfg.getTmMin(), cfg.getTmMax()));
            menuElegido = elegirMenuAleatorio();
            log.accion(id, "Cliente " + id + " eligió menú " + menuElegido.getNombre());
            eventos.publicar(Evento.clienteEligioMenu(id, menuElegido.getNombre()));

            // 5. Avisar al mozo: solo el último del grupo encola la mesa
            boolean soyUltimo = mesa.registrarMenuElegido(menuElegido);
            if (soyUltimo) {
                log.accion(id, "Mesa " + mesa.getId() + " lista para pedir");
                eventos.publicar(Evento.mesaListaParaPedir(mesa.getId()));
                colaLlamadosMozo.llamarMozo(mesa);
            }

            // 6. Esperar que el mozo sirva todos los platos
            mesa.esperarComida();
            log.accion(id, "Cliente " + id + " tiene su plato en mesa " + mesa.getId());
            eventos.publicar(Evento.clienteServido(id, mesa.getId()));

            // 7. Comer (tiempo aleatorio entre TQmin y TQmax)
            Thread.sleep(Aleatorio.entre(cfg.getTqMin(), cfg.getTqMax()));
            log.accion(id, "Cliente " + id + " terminó de comer");
            eventos.publicar(Evento.clienteTerminoComer(id));

            // 8. Ir a la caja a pagar
            colaCobros.agregarALaCola(this);
            log.accion(id, "Cliente " + id + " pagó");
            eventos.publicar(Evento.clientePago(id));

            // 9. Retirarse
            mesa.clienteSeVa();
            if (mesa.estaVacia()) {
                log.accion(id, "Mesa " + mesa.getId() + " quedó sin clientes");
                eventos.publicar(Evento.mesaVacia(mesa.getId()));
                mesasParaLimpiar.put(mesa);  // avisar al mozo para limpiar
            }
            aforo.salir();
            log.accion(id, "Cliente " + id + " se retiró del restaurante");
            eventos.publicar(Evento.clienteRetiro(id));

        } catch (InterruptedException e) {
            // Cierre de simulación: el cliente se retira sin terminar
            Thread.currentThread().interrupt();
            log.accion(id, "Cliente " + id + " interrumpido (cierre)");
            if (mesa != null) {
                mesa.clienteSeVa();
                try { mesasParaLimpiar.put(mesa); } catch (InterruptedException ignored) { }
            }
            aforo.salir();
        }
    }

    private Menu elegirMenuAleatorio() {
        return menuDisponible.get(RND.nextInt(menuDisponible.size()));
    }

    public int getId() { return id; }
    public Menu getMenuElegido() { return menuElegido; }
    public Mesa getMesa() { return mesa; }

    @Override
    public String toString() {
        return "Cliente#" + id + (menuElegido != null ? " menu=" + menuElegido.getNombre() : "");
    }
}
