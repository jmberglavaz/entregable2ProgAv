package main.java.entregable2.restaurante.log;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger thread-safe que escribe a consola Y a archivo.
 *
 * Herramienta: synchronized para escritura atómica.
 */
public class LogSimulacion {

    private static final String ARCHIVO = "logs/simulacion.log";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final BufferedWriter writer;

    public LogSimulacion() {
        try {
            // Crear carpeta logs si no existe
            new java.io.File("logs").mkdirs();
            this.writer = new BufferedWriter(new FileWriter(ARCHIVO, false));
            System.out.println("[LOG] Archivo de log: " + ARCHIVO);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo abrir el log: " + e.getMessage(), e);
        }
    }

    /**
     * Log simple.
     */
    public synchronized void log(String mensaje) {
        escribir("LOG", mensaje);
    }

    /**
     * Log con ID de actor.
     */
    public synchronized void accion(int idActor, String mensaje) {
        escribir("LOG #" + idActor, mensaje);
    }

    private void escribir(String prefijo, String mensaje) {
        String timestamp = LocalDateTime.now().format(FMT);
        String linea = "[" + timestamp + "] [" + prefijo + "] " + mensaje;

        // Consola
        System.out.println(linea);

        // Archivo
        try {
            writer.write(linea);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error escribiendo log: " + e.getMessage());
        }
    }

    /**
     * Cierra el BufferedWriter al final. Es synchronized para no cerrar mientras
     * otro hilo está escribiendo.
     */
    public synchronized void cerrar() {
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("Error cerrando log: " + e.getMessage());
        }
    }
}