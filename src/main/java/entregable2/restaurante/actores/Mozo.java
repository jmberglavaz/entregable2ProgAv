package main.java.entregable2.restaurante.actores;

import main.java.entregable2.restaurante.concurrencia.interfaces.ColaLlamadosMozo;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaPedidos;
import main.java.entregable2.restaurante.concurrencia.interfaces.MostradorPlatos;
import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.Evento;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Mesa;
import main.java.entregable2.restaurante.modelo.*;
import main.java.entregable2.restaurante.util.Aleatorio;

import java.util.concurrent.TimeUnit;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mozo implements Runnable {
    private final int id;
    private final ColaPedidos colaPedidos;
    private final MostradorPlatos mostradorPlatos;
    private final ColaLlamadosMozo colaLlamados;
    private final List<Mesa> mesas;
    private final CanalEventos canalEventos;
    private final LogSimulacion log;
    private final AtomicBoolean activo;
    private final Configuracion config;
    private final BlockingQueue<Mesa> mesasParaLimpiar;


    public Mozo(int id, ColaPedidos colaPedidos, MostradorPlatos mostradorPlatos,
                ColaLlamadosMozo colaLlamados, List<Mesa> mesas, CanalEventos canalEventos,
                LogSimulacion log, AtomicBoolean activo, Configuracion config,  BlockingQueue<Mesa> mesasParaLimpiar) {
        this.id = id;
        this.colaPedidos = colaPedidos;
        this.mostradorPlatos = mostradorPlatos;
        this.colaLlamados = colaLlamados;
        this.mesas = mesas;
        this.canalEventos = canalEventos;
        this.log = log;
        this.activo = activo;
        this.config = config;
        this.mesasParaLimpiar = mesasParaLimpiar;
    }

    /**
     * Un hilo por mozo. Atiende TRES fuentes en rotación con poll(200 ms):
     *   1) mostradorPlatos.retirar()        -> servir un plato listo
     *   2) colaLlamados.esperarLlamado()    -> tomar un pedido
     *   3) mesasParaLimpiar.poll(...)       -> limpiar una mesa
     *
     * Nunca se bloquea indefinidamente en una sola cola; así no se queda esperando
     * un plato mientras hay una mesa sucia (evita inanición entre tareas).
     * Termina cuando activo pasa a false o lo interrumpen.
     */
    @Override
    public void run() {
        log.log("Mozo"+ id + "inicia turno");
        try{
            while(activo.get()){
                boolean hizoAlgo = false;
                //servir platos listos
                Plato plato = mostradorPlatos.retirar();
                if (plato != null){
                    servirPlato(plato);
                    hizoAlgo = true;
                }
                //atender llamado de mesa
                if (!hizoAlgo){
                    Mesa mesa= colaLlamados.esperarLlamado();
                    if (mesa != null){
                        tomarPedido(mesa);
                        hizoAlgo = true;
                    }
                }
                //limpiar mesas sucias
                if (!hizoAlgo) {
                    Mesa mesaSucia = mesasParaLimpiar.poll(200, TimeUnit.MILLISECONDS);
                    if (mesaSucia != null) {
                        limpiarMesa(mesaSucia);
                        hizoAlgo = true;
                    }
                }

            }
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
        log.log("Mozo" + id + "termina turno");

    }
    /**
     * Simula el tiempo de anotar (sleep TZ), arma el Pedido desde la mesa
     * (Mesa.construirPedido, synchronized), lo deposita en colaPedidos para los
     * cocineros y pasa la mesa a ESPERANDO_COMIDA.
     * Como la mesa viene de colaLlamados, solo un mozo atiende cada llamado.
     */
    private void tomarPedido(Mesa mesa) throws InterruptedException{
        long demora = Aleatorio.entre(config.getTzMin(), config.getTzMax());
        Thread.sleep(demora);
        //constuir pedido desde la mesa
        Pedido pedido = mesa.construirPedido();
        canalEventos.publicar(new Evento("MOZO", id, "Mozo " + id + " toma pedido de mesa " + mesa.getId()));
        log.log("Mozo " + id + " toma pedido " + pedido.getId() + " de mesa " + mesa.getId());
        //pasar pedido a cocina
        colaPedidos.agregarALaCola(pedido);
        mesa.marcarPedidoEnviado(); //cambia a estado ESPERANDO_COMIDA
    }

    /**
     * Simula el tiempo de servir (sleep TR), busca la mesa por id y llama
     * mesa.recibirPlato(plato). Cuando sirve el último de los P platos, la Mesa
     * hace notifyAll() y los clientes salen de esperarComida().
     */
    private void servirPlato(Plato plato) throws InterruptedException{
        long demora = Aleatorio.entre(config.getTrMin(), config.getTrMax());
        Thread.sleep(demora);
        canalEventos.publicar(new Evento("MOZO", id, "Mozo" + id + " sirve plato" + plato.getId() + "a mesa" + plato.getIdMesa()));
        log.log("Mozo" + id + "sirve plato" + plato.getId() + "a mesa" + plato.getIdMesa());
        Mesa mesa = buscarMesa(plato.getIdMesa());
        if (mesa != null){
            mesa.recibirPlato(plato);
        }
    }

    /**
     * Simula el tiempo de limpieza (sleep TL) y deja la mesa LIBRE.
     * Nota: el GestorMesas se entera por su espera con timeout de 500 ms
     * (await(500ms)), no por señal inmediata.
     */
    private void limpiarMesa(Mesa mesa) throws InterruptedException {
        long demora = Aleatorio.entre(config.getTlMin(), config.getTlMax());
        Thread.sleep(demora);
        canalEventos.publicar(new Evento("MOZO", id, "Mozo" + id + " limpia mesa" + mesa.getId()));
        log.log("Mozo" + id + "limpia mesa" + mesa.getId());
        mesa.marcarLibre();
    }


    private Mesa buscarMesa(int idMesa){
        return mesas.stream().filter(m -> m.getId()==idMesa).findFirst().orElse(null);
    }
}
