package main.java.entregable2.restaurante.modelo;

public class Menu {
    private final int id;
    private final String nombre;
    private final long tcMin;
    private final long tcMax;

    public Menu(int id, String nombre, long tcMin, long tcMax) {
        this.id = id;
        this.nombre = nombre;
        this.tcMin = tcMin;
        this.tcMax = tcMax;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public long getTcMin() { return tcMin; }
    public long getTcMax() { return tcMax; }

    @Override
    public String toString() {
        return "Menu{" + id + ", '" + nombre + "'}";
    }
}