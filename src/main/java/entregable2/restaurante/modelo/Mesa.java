package main.java.entregable2.restaurante.modelo;

import main.java.entregable2.restaurante.modelo.enums.EstadoMesa;
import main.java.entregable2.restaurante.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RECURSO COMPARTIDO principal (clientes, mozos y GestorMesas la usan a la vez).
 *
 * Patrón: Monitor. Todo el estado mutable (estado, ocupantes, clientesEligieron,
 * clientesServidos, menusElegidos, pedidoActual) se toca SOLO desde métodos
 * synchronized -> exclusión mutua + visibilidad. Los hilos que deben esperar un
 * cambio de estado usan wait() y se despiertan con notifyAll().
 *
 * Máquina de estados:
 * LIBRE -> ESPERANDO_PEDIDO -> ESPERANDO_COMIDA -> COMIENDO -> LIMPIEZA -> LIBRE
 *
 * Nunca llama a código de otras clases mientras tiene su monitor -> no puede
 * formar ciclos de espera (sin deadlock).
 */
public class Mesa {

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    private final int id;
    private final int capacidad;
    private EstadoMesa estado = EstadoMesa.LIBRE;
    private int ocupantes = 0;
    private int clientesEligieron = 0;
    private int clientesServidos = 0;
    private List<Menu> menusElegidos = new ArrayList<>();
    private Pedido pedidoActual = null;


    public Mesa(int capacidad) {
        this.id = ID_GEN.getAndIncrement();
        this.capacidad = capacidad;
    }

    /** Intenta ocupar la mesa. Solo tiene éxito si está LIBRE. */
    public synchronized boolean intentarOcupar() {
        if (estado != EstadoMesa.LIBRE) return false;
        estado = EstadoMesa.ESPERANDO_PEDIDO;
        ocupantes = capacidad;
        clientesEligieron = 0;
        clientesServidos = 0;
        return true;
    }

    /** El mozo la deja lista para reutilizar. */
    public synchronized void marcarLibre() {
        estado = EstadoMesa.LIBRE;
        ocupantes = 0;
        clientesEligieron = 0;
        clientesServidos = 0;
        menusElegidos.clear();
        notifyAll();
    }

    /**
     * Cada cliente avisa que ya eligió su menú.
     * @return true si este cliente fue el último (debe avisar al mozo).
     */
    public synchronized boolean registrarMenuElegido(Menu menu) {
        menusElegidos.add(menu);
        clientesEligieron++;
        if (clientesEligieron == capacidad) {
            notifyAll();
            return true;
        }
        return false;
    }


    /**
     * El mozo avisa que sirvió un plato.
     * @return true si ya se sirvieron todos (dispara el "a comer").
     */
    public synchronized boolean registrarPlatoServido() {
        clientesServidos++;
        if (clientesServidos == capacidad) {
            estado = EstadoMesa.COMIENDO;
            notifyAll();
            return true;
        }
        return false;
    }

    /** Los clientes esperan acá hasta que todos estén servidos. */
    public synchronized void esperarComida() throws InterruptedException {
        while (estado != EstadoMesa.COMIENDO) {
            wait();
        }
    }

    /** Cada cliente avisa que se retira. */
    public synchronized void clienteSeVa() {
        if (ocupantes > 0) ocupantes--;
        if (ocupantes == 0) {
            estado = EstadoMesa.LIMPIEZA;
        }
        notifyAll();
    }

    public synchronized boolean estaVacia() { return ocupantes == 0; }
    public synchronized EstadoMesa getEstado() { return estado; }
    public int getId() { return id; }
    public int getCapacidad() { return capacidad; }
    public synchronized List<Menu> getMenusElegidos() {
        return new ArrayList<>(menusElegidos);
    }
    /**
     * Construye el Pedido a partir de los menús elegidos.
     * El Mozo lo llama al tomar el pedido.
     */
    public synchronized Pedido construirPedido() {
        if (menusElegidos.isEmpty()) {
            throw new IllegalStateException(
                    "Mesa " + id + " no tiene menús elegidos");
        }
        int idPedido = IdGenerator.nextPedidoId();
        this.pedidoActual = new Pedido(idPedido, id, new ArrayList<>(menusElegidos));
        return this.pedidoActual;
    }

    /**
     * El Mozo ya pasó el pedido a cocina.
     */
    public synchronized void marcarPedidoEnviado() {
        this.estado = EstadoMesa.ESPERANDO_COMIDA;
        notifyAll();
    }

    /**
     * El Mozo entrega un plato. Cuando están todos, los clientes comen.
     */
    public synchronized void recibirPlato(Plato plato) {
        clientesServidos++;
        if (clientesServidos == capacidad) {
            estado = EstadoMesa.COMIENDO;
            notifyAll();
        }
    }

    /**
     * El Mozo terminó de limpiar.
     */
    public synchronized void marcarComoLibre() {
        estado = EstadoMesa.LIBRE;
        ocupantes = 0;
        clientesEligieron = 0;
        clientesServidos = 0;
        menusElegidos.clear();
        pedidoActual = null;
        notifyAll();
    }

}
