package main.java.entregable2.restaurante.display;


import main.java.entregable2.restaurante.concurrencia.interfaces.ColaCobros;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaLlamadosMozo;
import main.java.entregable2.restaurante.concurrencia.interfaces.ColaPedidos;
import main.java.entregable2.restaurante.concurrencia.interfaces.MostradorPlatos;
import main.java.entregable2.restaurante.log.LogSimulacion;
import main.java.entregable2.restaurante.modelo.Mesa;
import main.java.entregable2.restaurante.modelo.enums.EstadoMesa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ventana Swing con el monitor en tiempo real. Implementa Runnable: corre en su
 * propio hilo del pool para consumir eventos.
 *
 * Hay tres hilos involucrados:
 *  1. Hilo "Display" del pool: lee eventos del canal (bloqueante con timeout).
 *  2. Hilo EDT de Swing: es el único que puede modificar componentes gráficos.
 *  3. Timer de Swing: dispara actualizarEstado() cada 500 ms en el EDT.
 *
 * Regla de oro de Swing respetada: todo cambio visual se hace en el EDT con
 * SwingUtilities.invokeLater(...).
 *
 * Para leer el estado actual (mesas, colas) usa métodos thread-safe:
 * Mesa.getEstado() es synchronized y las colas usan size() de BlockingQueue.
 */
public class Display extends JFrame implements Runnable {

    private final CanalEventos canalEventos;
    private final AtomicBoolean activo;
    private final LogSimulacion log;
    private final List<Mesa> mesas;
    private final ColaPedidos colaPedidos;
    private final MostradorPlatos mostradorPlatos;
    private final ColaCobros colaCobros;
    private final ColaLlamadosMozo colaLlamadosMozo;

    private final JTextArea areaEventos;
    private final JPanel panelMesas;
    private final JLabel labelPedidos;
    private final JLabel labelPlatos;
    private final JLabel labelCobros;
    private final JLabel labelResumen;
    private final JLabel labelEstado;

    private final Queue<String> eventosRecientes = new LinkedList<>();
    private static final int MAX_EVENTOS = 30;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Colores
    private static final Color COLOR_FONDO = new Color(25, 25, 35);
    private static final Color COLOR_PANEL = new Color(40, 40, 55);
    private static final Color COLOR_TITULO = new Color(255, 200, 100);
    private static final Color COLOR_TEXTO = new Color(230, 230, 230);
    private static final Color COLOR_LIBRE = new Color(76, 175, 80);
    private static final Color COLOR_ESPERANDO_PEDIDO = new Color(255, 193, 7);
    private static final Color COLOR_ESPERANDO_COMIDA = new Color(255, 152, 0);
    private static final Color COLOR_COMIENDO = new Color(33, 150, 243);
    private static final Color COLOR_LIMPIEZA = new Color(244, 67, 54);

    public Display(CanalEventos canalEventos,
                   AtomicBoolean activo,
                   LogSimulacion log,
                   List<Mesa> mesas,
                   ColaPedidos colaPedidos,
                   MostradorPlatos mostradorPlatos,
                   ColaCobros colaCobros,
                   ColaLlamadosMozo colaLlamadosMozo) {
        this.canalEventos = canalEventos;
        this.activo = activo;
        this.log = log;
        this.mesas = mesas;
        this.colaPedidos = colaPedidos;
        this.mostradorPlatos = mostradorPlatos;
        this.colaCobros = colaCobros;
        this.colaLlamadosMozo = colaLlamadosMozo;

        setTitle("🍽 Restaurante - Monitor en tiempo real");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));

        // HEADER
        JLabel titulo = new JLabel("ESTADO DEL RESTAURANTE", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(COLOR_TITULO);
        titulo.setBorder(new EmptyBorder(15, 10, 15, 10));
        add(titulo, BorderLayout.NORTH);

        // PANEL CENTRAL
        JPanel centro = new JPanel();
        centro.setLayout(new BorderLayout(10, 10));
        centro.setBackground(COLOR_FONDO);
        centro.setBorder(new EmptyBorder(0, 15, 0, 15));

        // Eventos
        areaEventos = new JTextArea();
        areaEventos.setEditable(false);
        areaEventos.setFont(new Font("Monospaced", Font.BOLD, 16));
        areaEventos.setBackground(COLOR_PANEL);
        areaEventos.setForeground(COLOR_TEXTO);
        areaEventos.setMargin(new Insets(10, 10, 10, 10));
        areaEventos.setLineWrap(false);
        areaEventos.setWrapStyleWord(false);

        JScrollPane scrollEventos = new JScrollPane(areaEventos);
        scrollEventos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_TITULO, 2),
                "EVENTOS", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16), COLOR_TITULO));
        scrollEventos.setPreferredSize(new Dimension(700, 0));
        scrollEventos.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        centro.add(scrollEventos, BorderLayout.WEST);

        // Mesas y colas
        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(new BorderLayout(10, 10));
        panelDerecho.setBackground(COLOR_FONDO);

        panelMesas = new JPanel();
        panelMesas.setLayout(new GridLayout(0, 2, 12, 12));
        panelMesas.setBackground(COLOR_PANEL);
        panelMesas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_TITULO, 2),
                "MESAS", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16), COLOR_TITULO));
        panelDerecho.add(panelMesas, BorderLayout.CENTER);

        JPanel panelColas = new JPanel();
        panelColas.setLayout(new GridLayout(1, 3, 15, 15));
        panelColas.setBackground(COLOR_PANEL);
        panelColas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_TITULO, 2),
                "COLAS", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 16), COLOR_TITULO));

        labelPedidos = crearLabelCola("Pedidos", "0");
        labelPlatos = crearLabelCola("Platos", "0");
        labelCobros = crearLabelCola("Cobros", "0");

        panelColas.add(labelPedidos);
        panelColas.add(labelPlatos);
        panelColas.add(labelCobros);

        panelDerecho.add(panelColas, BorderLayout.SOUTH);

        centro.add(panelDerecho, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel panelFooter = new JPanel(new BorderLayout());
        panelFooter.setBackground(COLOR_FONDO);
        panelFooter.setBorder(new EmptyBorder(10, 15, 15, 15));

        labelResumen = new JLabel(" ", SwingConstants.CENTER);
        labelResumen.setFont(new Font("SansSerif", Font.BOLD, 16));
        labelResumen.setForeground(COLOR_TEXTO);
        panelFooter.add(labelResumen, BorderLayout.CENTER);

        labelEstado = new JLabel("● EN EJECUCIÓN", SwingConstants.RIGHT);
        labelEstado.setFont(new Font("SansSerif", Font.BOLD, 16));
        labelEstado.setForeground(COLOR_LIBRE);
        panelFooter.add(labelEstado, BorderLayout.EAST);

        add(panelFooter, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JLabel crearLabelCola(String titulo, String valor) {
        JLabel label = new JLabel(
                "<html><center>" +
                        "<b style='color:#FFC864;font-size:14px;'>" + titulo + "</b><br>" +
                        "<span style='font-size:28px;color:#FFFFFF;'>" + valor + "</span>" +
                        "</center></html>",
                SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(COLOR_FONDO);
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        return label;
    }

    /**
     * Bucle del hilo Display:
     *  - arranca el Timer de refresco (500 ms),
     *  - mientras activo.get() sea true o queden eventos, pide el siguiente evento y
     *    lo muestra con agregarEvento(); el "o queden eventos" asegura vaciar el
     *    canal antes de terminar,
     *  - al salir detiene el Timer, hace un último refresco y registra el cierre.
     */
    @Override
    public void run() {
        log.log("Display: iniciado");
        System.out.println("=== DISPLAY INICIADO (ventana) ===");

        Timer timer = new Timer(500, e -> actualizarEstado());
        timer.start();

        try {
            while (activo.get() || canalEventos.hayEventos()) {
                Evento evento = canalEventos.siguiente();
                if (evento != null) {
                    agregarEvento(evento);
                } else {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        timer.stop();
        actualizarEstado();
        log.log("Display: finalizado");
        System.out.println("=== DISPLAY FINALIZADO ===");
    }


    /**
     * Se llama desde el hilo Display pero la actualización visual se manda al EDT con
     * invokeLater. Agrega la línea con hora formateada y mantiene solo los últimos
     * 30 eventos (MAX_EVENTOS). La lista eventosRecientes se modifica únicamente
     * dentro del EDT: está "confinada" a un solo hilo, por eso no necesita locks.
     * Al final mueve el cursor al final del texto para el auto-scroll.
     */
    private void agregarEvento(Evento evento) {
        SwingUtilities.invokeLater(() -> {
            String hora = LocalTime.now().format(TIME_FMT);
            String linea = String.format("[%s] %-10s #%-3d %s",
                    hora,
                    evento.getTipo(),
                    evento.getIdActor(),
                    evento.getMensaje());
            eventosRecientes.add(linea);
            while (eventosRecientes.size() > MAX_EVENTOS) {
                eventosRecientes.poll();
            }
            StringBuilder sb = new StringBuilder();
            for (String s : eventosRecientes) {
                sb.append(s).append("\n");
            }
            areaEventos.setText(sb.toString());
            areaEventos.setCaretPosition(areaEventos.getDocument().getLength());
        });
    }

    /**
     * Redibuja el panel de mesas y los contadores (pedidos, platos, cobros), más el
     * resumen de mesas libres y ocupadas. Todo dentro de invokeLater (EDT).
     * Lee valores compartidos con métodos thread-safe; el resultado es una
     * "foto" del momento, y puede quedar un poco atrasada respecto al estado real
     * (aceptable para una pantalla de monitoreo).
     * Si activo es false muestra "RESTAURANTE CERRADO".
     */

    private void actualizarEstado() {
        SwingUtilities.invokeLater(() -> {
            panelMesas.removeAll();
            for (Mesa mesa : mesas) {
                panelMesas.add(crearPanelMesa(mesa));
            }
            panelMesas.revalidate();
            panelMesas.repaint();

            labelPedidos.setText("<html><center><b style='color:#FFC864;font-size:14px;'>Pedidos</b><br>" +
                    "<span style='font-size:28px;color:#FFFFFF;'>" +
                    colaPedidos.tamano() + "</span></center></html>");
            labelPlatos.setText("<html><center><b style='color:#FFC864;font-size:14px;'>Platos</b><br>" +
                    "<span style='font-size:28px;color:#FFFFFF;'>" +
                    mostradorPlatos.tamano() + "</span></center></html>");
            labelCobros.setText("<html><center><b style='color:#FFC864;font-size:14px;'>Cobros</b><br>" +
                    "<span style='font-size:28px;color:#FFFFFF;'>" +
                    colaCobros.tamano() + "</span></center></html>");

            long libres = mesas.stream()
                    .filter(m -> m.getEstado() == EstadoMesa.LIBRE).count();
            long ocupadas = mesas.size() - libres;
            labelResumen.setText("Total mesas: " + mesas.size() +
                    "  |  Libres: " + libres +
                    "  |  Ocupadas: " + ocupadas);

            if (!activo.get()) {
                labelEstado.setText("● RESTAURANTE CERRADO");
                labelEstado.setForeground(COLOR_LIMPIEZA);
            }
        });
    }



    /**
     * Dibuja el cuadro de cada mesa. El color depende del EstadoMesa:
     *  verde = LIBRE, amarillo = ESPERANDO_PEDIDO, naranja = ESPERANDO_COMIDA,
     *  azul = COMIENDO, rojo = LIMPIEZA.
     * getEstado() es synchronized, pero acá se llama dos veces (una para el color y
     * otra para el texto): entre ambas llamadas el estado podría cambiar y mostrar
     * color y texto desfasados por un instante. Mejora simple: guardar el estado en
     * una variable local (EstadoMesa e = mesa.getEstado();) y usarla en las dos.
     */
    private JPanel crearPanelMesa(Mesa mesa) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(obtenerColorEstado(mesa.getEstado()));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel labelId = new JLabel("Mesa " + mesa.getId(), SwingConstants.CENTER);
        labelId.setFont(new Font("SansSerif", Font.BOLD, 14));
        labelId.setForeground(Color.WHITE);
        panel.add(labelId, BorderLayout.NORTH);

        JLabel labelEstado = new JLabel(formatearEstado(mesa.getEstado()), SwingConstants.CENTER);
        labelEstado.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelEstado.setForeground(Color.WHITE);
        panel.add(labelEstado, BorderLayout.CENTER);

        return panel;
    }

    private Color obtenerColorEstado(EstadoMesa estado) {
        switch (estado) {
            case LIBRE:            return COLOR_LIBRE;
            case ESPERANDO_PEDIDO: return COLOR_ESPERANDO_PEDIDO;
            case ESPERANDO_COMIDA: return COLOR_ESPERANDO_COMIDA;
            case COMIENDO:         return COLOR_COMIENDO;
            case LIMPIEZA:         return COLOR_LIMPIEZA;
            default:               return Color.GRAY;
        }
    }

    private String formatearEstado(EstadoMesa estado) {
        switch (estado) {
            case LIBRE:            return "LIBRE";
            case ESPERANDO_PEDIDO: return "ESPERANDO PEDIDO";
            case ESPERANDO_COMIDA: return "ESPERANDO COMIDA";
            case COMIENDO:         return "COMIENDO";
            case LIMPIEZA:         return "LIMPIEZA";
            default:               return estado.toString();
        }
    }

    /**
     * La llama el Simulador al final (desde main). Como se toca un componente Swing,
     * se delega al EDT con invokeLater para no violar la regla de hilos de Swing.
     */
    public void cerrar() {
        SwingUtilities.invokeLater(() -> {
            labelEstado.setText("SIMULACIÓN FINALIZADA");
            labelEstado.setForeground(COLOR_LIMPIEZA);
        });
    }
}