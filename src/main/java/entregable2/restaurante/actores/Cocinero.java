package main.java.entregable2.restaurante.actores;

import main.java.entregable2.restaurante.concurrencia.interfaces.ColaPedidos;
import main.java.entregable2.restaurante.concurrencia.interfaces.MostradorPlatos;
import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.Evento;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Menu;
import main.java.entregable2.restaurante.modelo.Pedido;
import main.java.entregable2.restaurante.modelo.Plato;
import main.java.entregable2.restaurante.util.Aleatorio;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cocinero: consume pedidos de ColaPedidos, cocina cada menú y deja los platos listos en MostradorPlatos.
 *
 * Herramientas:
 *  - Thread/Runnable
 *  - ColaPedidos y MostradorPlatos: BlockingQueue → pasaje de mensajes
 *  - AtomicInteger para IDs de plato
 *  - AtomicBoolean para señal de cierre cooperativo
 *
 * Patrón: Productor-Consumidor
 *   - Cocineros = productores de platos.
 *   - Mozo = consumidor de platos.
 */
public class Cocinero implements Runnable {

    /** Contador global de IDs de plato, compartido entre todos los cocineros. */
    private static final AtomicInteger ID_PLATO = new AtomicInteger(1);

    private final int id;
    private final ColaPedidos colaPedidos;
    private final MostradorPlatos mostradorPlatos;
    private final CanalEventos canalEventos;
    private final LogSimulacion log;
    private final Configuracion config;
    private final AtomicBoolean activo;

    public Cocinero(int id,
                    ColaPedidos colaPedidos,
                    MostradorPlatos mostradorPlatos,
                    CanalEventos canalEventos,
                    LogSimulacion log,
                    Configuracion config,
                    AtomicBoolean activo) {
        this.id = id;
        this.colaPedidos = colaPedidos;
        this.mostradorPlatos = mostradorPlatos;
        this.canalEventos = canalEventos;
        this.log = log;
        this.config = config;
        this.activo = activo;
    }

    @Override
    public void run() {
        log.log("Cocinero " + id + " inicia turno");
        try {
            while (activo.get() && !Thread.currentThread().isInterrupted()) {
                Pedido pedido;
                try {
                    // Bloquea si no hay pedidos. Sale por InterruptedException
                    // cuando el Simulador interrumpe al cierre.
                    pedido = colaPedidos.tomar();
                } catch (InterruptedException e) {
                    break;
                }
                if (pedido == null) continue;
                cocinarPedido(pedido);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.log("Cocinero " + id + " termina turno");
    }

    /**
     * Cocina todos los menús del pedido, uno a uno, y los va depositando
     * en el MostradorPlatos.
     */
    private void cocinarPedido(Pedido pedido) throws InterruptedException {
        pedido.marcarEnCocina();
        log.accion(id, "Cocinero " + id + " tomó pedido " + pedido.getId()
                + " de mesa " + pedido.getIdMesa());

        for (Menu menu : pedido.getMenus()) {
            if (Thread.currentThread().isInterrupted()) return;

            // 1. Simular tiempo de cocción (específico del menú, no global)
            long tiempo = Aleatorio.entre(menu.getTcMin(), menu.getTcMax());
            Thread.sleep(tiempo);

            // 2. Crear el Plato y marcarlo listo
            int idPlato = ID_PLATO.getAndIncrement();
            Plato plato = new Plato(idPlato, pedido.getId(), pedido.getIdMesa(), menu);
            plato.marcarCocinando();
            plato.marcarListo();

            // 3. Dejarlo en el mostrador → el mozo lo retira
            mostradorPlatos.poner(plato);

            // 4. Notificar a display + log
            canalEventos.publicar(new Evento("COCINERO", id,
                    "Cocinero " + id + " dejó listo " + menu.getNombre()
                            + " (pedido " + pedido.getId() + ")"));
            log.accion(id, "Cocinero " + id + " cocinó " + menu.getNombre()
                    + " para pedido " + pedido.getId());

            // 5. Contar; si fue el último, marcar pedido LISTO y avisar
            boolean todosListos = pedido.registrarPlatoListo();
            if (todosListos) {
                canalEventos.publicar(new Evento("COCINERO", id,
                        "Pedido " + pedido.getId() + " completo, listo para servir"));
                log.accion(id, "Pedido " + pedido.getId() + " completo");
            }
        }
    }
}
