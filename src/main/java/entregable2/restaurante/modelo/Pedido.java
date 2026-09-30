package main.java.entregable2.restaurante.modelo;

import main.java.entregable2.restaurante.modelo.enums.EstadoPedido;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pedido de una mesa.
 * Contiene los menús elegidos por los P clientes.
 */
public class Pedido {

    private final int id;
    private final int idMesa;
    private final List<Menu> menus;
    private EstadoPedido estado;
    private final int totalPlatos;
    private final AtomicInteger platosListos = new AtomicInteger(0);

    public Pedido(int id, int idMesa, List<Menu> menus) {
        this.id = id;
        this.idMesa = idMesa;
        this.menus = menus;
        this.totalPlatos = menus.size();
        this.estado = EstadoPedido.PENDIENTE;
    }

    /* El cocinero llama a esto cuando termina un plato.
        @return true si este fue el ÚLTIMO plato (hay que avisar al mozo).*/
    public boolean registrarPlatoListo() {
        int listos = platosListos.incrementAndGet();
        if (listos == totalPlatos) {
            marcarListo();
            return true;
        }
        return false;
    }

    public int getId() { return id; }
    public int getIdMesa() { return idMesa; }
    public List<Menu> getMenus() { return menus; }
    public EstadoPedido getEstado() { return estado; }

    public void setEstado(EstadoPedido e) { this.estado = e; }
    public void marcarEnCocina() { this.estado = EstadoPedido.EN_COCINA; }
    public void marcarListo() { this.estado = EstadoPedido.LISTO; }
    public void marcarServido() { this.estado = EstadoPedido.SERVIDO; }

    @Override
    public String toString() {
        return "Pedido{id=" + id + ", idMesa=" + idMesa
                + ", menus=" + menus.size() + ", estado=" + estado + "}";
    }
}