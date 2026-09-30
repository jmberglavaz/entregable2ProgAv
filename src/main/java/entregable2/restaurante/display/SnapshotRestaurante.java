package main.java.entregable2.restaurante.display;

import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaLlamadosMozo;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaPedidos;
import main.java.entregable2.restaurante.concurrencia.interfaces.MostradorPlatos;
import main.java.entregable2.restaurante.modelo.Mesa;
import main.java.entregable2.restaurante.modelo.enums.EstadoMesa;

import java.util.List;

/**
 * Snapshot del estado del restaurante en un momento dado.
 */
public class SnapshotRestaurante {

    private final List<Mesa> mesas;
    private final ColaPedidos colaPedidos;
    private final MostradorPlatos mostradorPlatos;
    private final ColaCobros colaCobros;
    private final ColaLlamadosMozo colaLlamadosMozo;

    public SnapshotRestaurante(List<Mesa> mesas,
                               ColaPedidos colaPedidos,
                               MostradorPlatos mostradorPlatos,
                               ColaCobros colaCobros,
                               ColaLlamadosMozo colaLlamadosMozo) {
        this.mesas = mesas;
        this.colaPedidos = colaPedidos;
        this.mostradorPlatos = mostradorPlatos;
        this.colaCobros = colaCobros;
        this.colaLlamadosMozo = colaLlamadosMozo;
    }

    public List<Mesa> getMesas() { return mesas; }
    public int getPedidosPendientes() { return colaPedidos.tamano(); }
    public int getPlatosListos() { return mostradorPlatos.tamano(); }
    public int getCobrosPendientes() { return colaCobros.tamano(); }
    public boolean hayLlamados() { return colaLlamadosMozo.hayLlamados(); }

    public long getMesasLibres() {
        return mesas.stream().filter(m -> m.getEstado() == EstadoMesa.LIBRE).count();
    }

    public long getMesasOcupadas() {
        return mesas.size() - getMesasLibres();
    }
}
