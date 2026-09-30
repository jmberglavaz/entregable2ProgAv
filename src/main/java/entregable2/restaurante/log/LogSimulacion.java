package main.java.entregable2.restaurante.log;

public class LogSimulacion {
    public void log(String mensaje) {
        System.out.println("[LOG] " + mensaje);
    }

    public void accion(int idActor, String mensaje) {
        System.out.println("[LOG #" + idActor + "] " + mensaje);
    }
}
