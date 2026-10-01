package main.java.entregable2.restaurante.modelo;

import main.java.entregable2.restaurante.modelo.enums.EstadoPlato;

public class Plato {
    private final int id;
    private final int idPedido;
    private final int idMesa;
    private final Menu menu;
    private volatile EstadoPlato estado;

    /**
     * Plato individual. El campo estado es volatile: el cocinero lo escribe y el
     * mozo/Display lo leen desde otros hilos; volatile asegura que vean el último
     * valor.
     */
    public Plato(int id, int idPedido, int idMesa, Menu menu) {
        this.id = id;
        this.idPedido = idPedido;
        this.idMesa = idMesa;
        this.menu = menu;
        this.estado = EstadoPlato.CRUDO;
    }
      //El cocinero empieza a cocinar este plato.
    public synchronized void marcarCocinando() {
       this.estado = EstadoPlato.COCINANDO;
    }
     //El cocinero terminó de cocinar. El plato queda listo en el mostrador.
    public synchronized void marcarListo() {
        this.estado = EstadoPlato.LISTO;
    }
    public int getId() {
        return id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public Menu getMenu() {
        return menu;
    }

    public EstadoPlato getEstado() {
        return estado;
    }

    public boolean estaListo() {
        return estado == EstadoPlato.LISTO;
    }

    @Override
    public String toString() {
        return "Plato{" +
                "id=" + id +
                ", idPedido=" + idPedido +
                ", idMesa=" + idMesa +
                ", menu=" + (menu != null ? menu.getNombre() : "null") +
                ", estado=" + estado +
                '}';
    }
}
