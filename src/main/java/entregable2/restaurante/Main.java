package main.java.entregable2.restaurante;

import main.java.entregable2.restaurante.config.Configuracion;

public class Main {
    public static void main(String[] args) {
        Configuracion cfg = Configuracion.get();
        System.out.println(cfg);
    }
}
