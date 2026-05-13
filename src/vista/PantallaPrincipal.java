package vista;

import controlador.NavegacionListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Pantalla principal del sistema de compiladores.
 * Funciona como un dashboard profesional con navegación lateral,
 * barra superior, panel de bienvenida, indicadores de estado y pie de página.
 *
 * Proporciona acceso a los módulos de análisis léxico, sintáctico,
 * semántico y tabla de tokens a través de un panel de navegación.
 * La lógica de negocio se desacopla mediante la interfaz
 * {@link NavegacionListener}.
 */
public class PantallaPrincipal extends JFrame {

    // ================================================================
    // PALETA DE COLORES (Tema oscuro profesional)
    // ================================================================

    private static final Color FONDO_GLOBAL       = new Color(15, 23, 42);
    private static final Color SIDEBAR_BG         = new Color(30, 41, 59);
    private static final Color TOPBAR_BG          = new Color(30, 41, 59);
    private static final Color FOOTER_BG          = new Color(15, 23, 42);
    private static final Color CARD_BG            = new Color(30, 41, 59);
    private static final Color ACCENT             = new Color(59, 130, 246);
    private static final Color ACCENT_HOVER       = new Color(37, 99, 235);
    private static final Color TEXTO_PRIMARIO     = new Color(241, 245, 249);
    private static final Color TEXTO_SECUNDARIO   = new Color(148, 163, 184);
    private static final Color BORDE              = new Color(51, 65, 85);
    private static final Color NAV_HOVER          = new Color(51, 65, 85);
    private static final Color NAV_ACTIVE_BG      = new Color(59, 130, 246, 25);
    private static final Color SALIR_COLOR        = new Color(239, 68, 68);
    private static final Color VERDE_ONLINE       = new Color(34, 197, 94);
    private static final Color CARD_BORDER        = new Color(51, 65, 85);

    // ================================================================
    // TIPOGRAFÍA
    // ================================================================

    private static final Font TITULO_FONT       = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font SUBTITULO_FONT    = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font NAV_FONT          = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font CUERPO_FONT       = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font PEQ_FONT          = new Font("Segoe UI", Font.PLAIN, 11);

    // ================================================================
    // COMPONENTES
    // ================================================================

    private JPanel panelCentral;
    private JLabel labelBienvenida;
    private JLabel labelUsuario;
    private JLabel labelEstadoCompilador;

    private NavegacionListener listener;
    private JButton btnActivo;

    // ================================================================
    // CONSTRUCTOR
    // ================================================================

    public PantallaPrincipal() {
        super("Compilador - Sistema de An\u00e1lisis");
        inicializar();
        configurarVentana();
    }

    // ================================================================
    // CONFIGURACIÓN INICIAL
    // ================================================================

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 760);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);
        setIconImage(crearImagenIconoApp(64, "C>"));
    }

    private void inicializar() {
        setJMenuBar(crearMenuBar());
        add(crearTopBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(crearSidebar());
        split.setRightComponent(crearPanelCentral());
        split.setDividerSize(2);
        split.setBorder(null);
        split.setResizeWeight(0);
        split.setBackground(FONDO_GLOBAL);
        split.setEnabled(false);
        add(split, BorderLayout.CENTER);

        add(crearFooter(), BorderLayout.SOUTH);
    }

    // ================================================================
    // BARRA DE MENÚ SUPERIOR
    // ================================================================

    private JMenuBar crearMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(TOPBAR_BG);
        mb.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JMenu mArchivo = new JMenu("Archivo");
        mArchivo.setForeground(TEXTO_PRIMARIO);
        mArchivo.setFont(CUERPO_FONT);
        mArchivo.add(itemMenu("Salir", KeyEvent.VK_Q, e -> salir()));

        JMenu mAyuda = new JMenu("Ayuda");
        mAyuda.setForeground(TEXTO_PRIMARIO);
        mAyuda.setFont(CUERPO_FONT);
        mAyuda.add(itemMenu("Acerca de", KeyEvent.VK_F1, e -> mostrarAcercaDe()));

        mb.add(mArchivo);
        mb.add(Box.createHorizontalGlue());
        mb.add(mAyuda);
        return mb;
    }

    private JMenuItem itemMenu(String texto, int tecla, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(CUERPO_FONT);
        item.setAccelerator(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(accion);
        item.setForeground(TEXTO_PRIMARIO);
        item.setBackground(TOPBAR_BG);
        return item;
    }

    // ================================================================
    // BARRA SUPERIOR
    // ================================================================

    private JPanel crearTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(TOPBAR_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        panel.setPreferredSize(new Dimension(0, 52));

        JLabel lblTituloProyecto = new JLabel(
                crearIcono(0x2699, ACCENT, 22), JLabel.LEFT);
        lblTituloProyecto.setText("  Compilador — Sistema de An\u00e1lisis");
        lblTituloProyecto.setFont(TITULO_FONT);
        lblTituloProyecto.setForeground(TEXTO_PRIMARIO);
        lblTituloProyecto.setIconTextGap(8);

        labelUsuario = new JLabel(
                crearIcono(0x25C9, ACCENT, 18), JLabel.RIGHT);
        labelUsuario.setText("  Usuario: Admin");
        labelUsuario.setFont(CUERPO_FONT);
        labelUsuario.setForeground(TEXTO_SECUNDARIO);
        labelUsuario.setIconTextGap(6);

        panel.add(lblTituloProyecto, BorderLayout.WEST);
        panel.add(labelUsuario, BorderLayout.EAST);
        return panel;
    }

    // ================================================================
    // SIDEBAR — NAVEGACIÓN LATERAL
    // ================================================================

    private JPanel crearSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        panel.setPreferredSize(new Dimension(250, 0));

        JLabel lblNav = new JLabel("  Navegaci\u00f3n");
        lblNav.setFont(PEQ_FONT);
        lblNav.setForeground(TEXTO_SECUNDARIO);
        lblNav.setBorder(BorderFactory.createEmptyBorder(0, 20, 12, 0));
        lblNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblNav);

        panel.add(crearNavSeparador());
        panel.add(Box.createVerticalStrut(4));

        panel.add(crearBotonNav(
                "An\u00e1lisis L\u00e9xico",    0x25CE, ACCENT,
                () -> { if (listener != null) listener.onAnalisisLexico(); }));
        panel.add(Box.createVerticalStrut(2));

        panel.add(crearBotonNav(
                "An\u00e1lisis Sint\u00e1ctico", 0x25B6, ACCENT,
                () -> { if (listener != null) listener.onAnalisisSintactico(); }));
        panel.add(Box.createVerticalStrut(2));

        panel.add(crearBotonNav(
                "An\u00e1lisis Sem\u00e1ntico", 0x25C8, ACCENT,
                () -> { if (listener != null) listener.onAnalisisSemantico(); }));
        panel.add(Box.createVerticalStrut(2));

        panel.add(crearBotonNav(
                "Tabla de Tokens",              0x2B21, ACCENT,
                () -> { if (listener != null) listener.onTablaTokens(); }));
        panel.add(Box.createVerticalStrut(2));

        panel.add(crearBotonNav(
                "Editor de C\u00f3digo",        0x270E, ACCENT,
                () -> { if (listener != null) listener.onAbrirEditor(); }));
        panel.add(Box.createVerticalStrut(2));

        panel.add(Box.createVerticalGlue());
        panel.add(crearNavSeparador());
        panel.add(Box.createVerticalStrut(4));

        panel.add(crearBotonNav(
                "Salir",                        0x2715, SALIR_COLOR,
                () -> salir()));

        return panel;
    }

    private JButton crearBotonNav(String texto, int unicode, Color colorIcono,
                                  Runnable accion) {
        JButton btn = new JButton(texto);
        btn.setFont(NAV_FONT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(14);
        btn.setIcon(crearIcono(unicode, colorIcono, 20));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(texto.equals("Salir") ? SALIR_COLOR : TEXTO_PRIMARIO);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        btn.addActionListener(e -> {
            setActivo(btn);
            accion.run();
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent ev) {
                if (btn != btnActivo)
                    btn.setBackground(NAV_HOVER);
            }
            public void mouseExited(MouseEvent ev) {
                if (btn != btnActivo)
                    btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    /**
     * Marca el botón como activo (resalta con indicador izquierdo).
     */
    private void setActivo(JButton btn) {
        if (btnActivo != null) {
            btnActivo.setBackground(SIDEBAR_BG);
            btnActivo.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        }
        btnActivo = btn;
        if (btnActivo != null) {
            btnActivo.setBackground(NAV_ACTIVE_BG);
            btnActivo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT),
                    BorderFactory.createEmptyBorder(10, 17, 10, 20)));
        }
    }

    private JSeparator crearNavSeparador() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BORDE);
        sep.setBackground(BORDE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 20));
        return sep;
    }

    // ================================================================
    // PANEL CENTRAL — BIENVENIDA + TARJETAS DE ESTADO
    // ================================================================

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FONDO_GLOBAL);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // --- Sección de bienvenida ---
        gbc.weighty = 0.4;
        panel.add(crearSeccionBienvenida(), gbc);

        // --- Separador ---
        gbc.weighty = 0.0;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(Box.createVerticalStrut(10), gbc);

        // --- Tarjetas de estado ---
        gbc.weighty = 0.6;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(crearPanelTarjetas(), gbc);

        return panel;
    }

    private JPanel crearSeccionBienvenida() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        // Logo grande
        JLabel logo = new JLabel(crearIconoAppGrande());
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(logo, gbc);

        // Título de bienvenida
        labelBienvenida = new JLabel(
                "Bienvenido al Sistema de An\u00e1lisis");
        labelBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 26));
        labelBienvenida.setForeground(TEXTO_PRIMARIO);
        labelBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(labelBienvenida, gbc);

        // Descripción
        JLabel desc = new JLabel(
                "Seleccione un m\u00f3dulo en el panel lateral para comenzar el an\u00e1lisis.");
        desc.setFont(SUBTITULO_FONT);
        desc.setForeground(TEXTO_SECUNDARIO);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(desc, gbc);

        // Indicador de estado del compilador
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        estadoPanel.setBackground(CARD_BG);
        estadoPanel.setOpaque(true);

        JLabel dot = new JLabel(crearIcono(0x25CF, VERDE_ONLINE, 12));
        labelEstadoCompilador = new JLabel("Compilador listo");
        labelEstadoCompilador.setFont(CUERPO_FONT);
        labelEstadoCompilador.setForeground(VERDE_ONLINE);

        estadoPanel.add(dot);
        estadoPanel.add(labelEstadoCompilador);
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(estadoPanel, gbc);

        return card;
    }

    private JPanel crearPanelTarjetas() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 16, 0));
        panel.setBackground(FONDO_GLOBAL);
        panel.setOpaque(true);

        panel.add(crearTarjeta(
                "Estado del Sistema", "Operativo",
                0x25CF, VERDE_ONLINE));
        panel.add(crearTarjeta(
                "\u00daltimo An\u00e1lisis", "Ninguno",
                0x25CB, TEXTO_SECUNDARIO));
        panel.add(crearTarjeta(
                "Motor de Compilaci\u00f3n", "v1.0.0",
                0x2699, ACCENT));

        return panel;
    }

    private JPanel crearTarjeta(String titulo, String valor,
                                int unicode, Color colorIcono) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Fila superior: icono + título
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setBackground(CARD_BG);
        header.setOpaque(true);

        JLabel icono = new JLabel(crearIcono(unicode, colorIcono, 18));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(PEQ_FONT);
        lblTitulo.setForeground(TEXTO_SECUNDARIO);

        header.add(icono);
        header.add(lblTitulo);

        // Valor grande
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblValor.setForeground(TEXTO_PRIMARIO);
        lblValor.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        card.add(header, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.WEST);
        return card;
    }

    // ================================================================
    // PIE DE PÁGINA
    // ================================================================

    private JPanel crearFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FOOTER_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        panel.setPreferredSize(new Dimension(0, 32));

        JLabel info = new JLabel(
                "\u00a9 2026 — Proyecto Compiladores | Todos los derechos reservados");
        info.setFont(PEQ_FONT);
        info.setForeground(TEXTO_SECUNDARIO);

        JLabel version = new JLabel("v1.0.0");
        version.setFont(PEQ_FONT);
        version.setForeground(TEXTO_SECUNDARIO);

        panel.add(info, BorderLayout.WEST);
        panel.add(version, BorderLayout.EAST);
        return panel;
    }

    // ================================================================
    // ACCIONES
    // ================================================================

    private void salir() {
        if (listener != null) listener.onSalir();
        System.exit(0);
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "Compilador — Sistema de An\u00e1lisis v1.0.0\n\n" +
                "Herramienta educativa para an\u00e1lisis l\u00e9xico,\n" +
                "sint\u00e1ctico y sem\u00e1ntico de lenguajes.\n\n" +
                "Desarrollado para el Proyecto de Compiladores.",
                "Acerca del Sistema",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ================================================================
    // MÉTODOS PÚBLICOS — API PARA EL CONTROLADOR
    // ================================================================

    public void setNavegacionListener(NavegacionListener listener) {
        this.listener = listener;
    }

    public void setUsuarioActivo(String usuario) {
        labelUsuario.setText("  Usuario: " + usuario);
    }

    public void setEstadoCompilador(String estado, boolean online) {
        labelEstadoCompilador.setText(estado);
        labelEstadoCompilador.setForeground(
                online ? VERDE_ONLINE : SALIR_COLOR);
    }

    // ================================================================
    // GENERACIÓN DE ICONOS (programáticos, sin archivos externos)
    // ================================================================

    private ImageIcon crearIcono(int unicode, Color color, int size) {
        BufferedImage img = new BufferedImage(
                size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        float r = size / 2f;
        g.setColor(color);
        g.fillOval(0, 0, size - 1, size - 1);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size - 6));
        FontMetrics fm = g.getFontMetrics();
        String ch = String.valueOf((char) unicode);
        int x = (size - fm.stringWidth(ch)) / 2;
        int y = (size + fm.getHeight() / 2) / 2 - 1;
        g.drawString(ch, x, y);
        g.dispose();
        return new ImageIcon(img);
    }

    private ImageIcon crearIconoAppGrande() {
        int size = 80;
        BufferedImage img = new BufferedImage(
                size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ACCENT);
        g.fillRoundRect(8, 8, size - 16, size - 16, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 34));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("C>", (size - fm.stringWidth("C>")) / 2,
                (size + fm.getAscent() / 2) / 2);
        g.dispose();
        return new ImageIcon(img);
    }

    private Image crearImagenIconoApp(int size, String texto) {
        BufferedImage img = new BufferedImage(
                size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(ACCENT);
        g.fillRoundRect(6, 6, size - 12, size - 12, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, size / 3));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(texto, (size - fm.stringWidth(texto)) / 2,
                (size + fm.getAscent() / 2) / 2);
        g.dispose();
        return img;
    }

    // ================================================================
    // MAIN
    // ================================================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new PantallaPrincipal().setVisible(true);
        });
    }
}
