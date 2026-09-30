package main.java.entregable2.restaurante.modelo;

import main.java.entregable2.restaurante.modelo.enums.EstadoPedido;

import java.util.List;

/**
 * Pedido de una mesa.
 * Contiene los menús elegidos por los P clientes.
 */
public class Pedido {

    private final int id;
    private final int idMesa;
    private final List<Menu> menus;
    private EstadoPedido estado;

    public Pedido(int id, int idMesa, List<Menu> menus) {
        this.id = id;
        this.idMesa = idMesa;
        this.menus = menus;
        this.estado = EstadoPedido.PENDIENTE;
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