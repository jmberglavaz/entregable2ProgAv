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
 * Display visual del restaurante.
 *
 * Muestra:
 *  - Panel de EVENTOS con timestamps (últimos 30).
 *  - Panel de MESAS con colores.
 *  - Panel de COLAS con contadores.
 *  - Panel de RESUMEN.
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
    private static final Color COLOR_PAGANDO = new Color(156, 39, 176);
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

        // Eventos (izquierda)
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

        // Mesas y colas (derecha)
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

        // Colas
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

        // FOOTER
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

    public void cerrar() {
        SwingUtilities.invokeLater(() -> {
            labelEstado.setText("● SIMULACIÓN FINALIZADA");
            labelEstado.setForeground(COLOR_LIMPIEZA);
        });
    }

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
}