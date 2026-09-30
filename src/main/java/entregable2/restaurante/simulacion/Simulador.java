package main.java.entregable2.restaurante.simulacion;

import main.java.entregable2.restaurante.actores.Cajero;
import main.java.entregable2.restaurante.actores.Cliente;
import main.java.entregable2.restaurante.actores.Cocinero;
import main.java.entregable2.restaurante.actores.Mozo;
import main.java.entregable2.restaurante.concurrencia.ColaCobros;
import main.java.entregable2.restaurante.concurrencia.ColaLlamadosMozo;
import main.java.entregable2.restaurante.concurrencia.ColaPedidos;
import main.java.entregable2.restaurante.concurrencia.MostradorPlatos;
import main.java.entregable2.restaurante.concurrencia.SincronizacionInicio;
import main.java.entregable2.restaurante.display.ColaLlamadosMozoImpl;
import main.java.entregable2.restaurante.config.Configuracion;
import main.java.entregable2.restaurante.display.CanalEventos;
import main.java.entregable2.restaurante.display.CanalEventosImpl;
import main.java.entregable2.restaurante.display.Display;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Menu;
import main.java.entregable2.restaurante.modelo.Mesa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulador principal del restaurante.
 *
 * Responsabilidades:
 *  1. Crear todas las estructuras compartidas (colas, mesas, flags).
 *  2. Crear los actores (Mozos, Cocineros, Cajeros, Clientes).
 *  3. Lanzar los hilos.
 *  4. Esperar a que terminen.
 *  5. Cerrar el log.
 *
 * Herramientas:
 *  - ExecutorService: pool de hilos para actores.
 *  - CountDownLatch (vía SincronizacionInicio): barrera de inicio.
 *  - AtomicBoolean: flags de control.
 *  - BlockingQueue: pasaje de mensajes.
 */
public class Simulador {
}