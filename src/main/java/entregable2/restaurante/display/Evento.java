package main.java.entregable2.restaurante.display;

public class Evento {

    private final long timestamp;
    private final String tipo;
    private final int idActor;
    private final String mensaje;

    public Evento(String tipo, int idActor, String mensaje) {
        this.timestamp = System.currentTimeMillis();
        this.tipo = tipo;
        this.idActor = idActor;
        this.mensaje = mensaje;
    }

    // ---- Métodos estáticos para Cliente (Juanma) ----

    public static Evento clienteIngreso(int id) {
        return new Evento("CLIENTE", id, "Cliente " + id + " ingresó al restaurante");
    }

    public static Evento clienteSentado(int id, int idMesa) {
        return new Evento("CLIENTE", id, "Cliente " + id + " se sentó en mesa " + idMesa);
    }

    public static Evento clienteEligioMenu(int id, String nombreMenu) {
        return new Evento("CLIENTE", id, "Cliente " + id + " eligió menú " + nombreMenu);
    }

    public static Evento mesaListaParaPedir(int idMesa) {
        return new Evento("MESA", idMesa, "Mesa " + idMesa + " lista para pedir");
    }

    public static Evento clienteServido(int id, int idMesa) {
        return new Evento("CLIENTE", id, "Cliente " + id + " fue servido en mesa " + idMesa);
    }

    public static Evento clienteTerminoComer(int id) {
        return new Evento("CLIENTE", id, "Cliente " + id + " terminó de comer");
    }

    public static Evento clientePago(int id) {
        return new Evento("CLIENTE", id, "Cliente " + id + " pagó");
    }

    public static Evento mesaVacia(int idMesa) {
        return new Evento("MESA", idMesa, "Mesa " + idMesa + " quedó vacía");
    }

    public static Evento clienteRetiro(int id) {
        return new Evento("CLIENTE", id, "Cliente " + id + " se retiró");
    }

    // ---- Getters ----

    public long getTimestamp() { return timestamp; }
    public String getTipo() { return tipo; }
    public int getIdActor() { return idActor; }
    public String getMensaje() { return mensaje; }

    @Override
    public String toString() {
        return "[" + tipo + " #" + idActor + "] " + mensaje;
    }
}
