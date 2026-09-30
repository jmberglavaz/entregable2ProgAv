package main.java.entregable2.restaurante;


import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.simulacion.Simulador;

public class Main {
    public static void main(String[] args) {
        Configuracion config = Configuracion.get();
        LogSimulacion log = new LogSimulacion();

        System.out.println("=== CONFIGURACIÓN ===");
        System.out.println(config);
        System.out.println();

        Simulador simulador = new Simulador(config, log);
        simulador.iniciar();
        System.exit(0);
    }
}