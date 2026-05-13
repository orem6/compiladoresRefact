package vista;

import controlador.CompiladorListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * Vista principal del compilador.
 * Proporciona una interfaz gráfica moderna, ordenada y profesional
 * para escribir código fuente, analizarlo léxica y sintácticamente,
 * y visualizar los resultados.
 *
 * Diseñada con separación de responsabilidades: la lógica del compilador
 * se comunica a través de la interfaz {@link CompiladorListener}.
 */
public class VistaCompilador extends JFrame {

    // ============================================================
    // PALETA DE COLORES MODERNA
    // ============================================================

    private static final Color FONDO_PRINCIPAL      = new Color(236, 240, 241);
    private static final Color FONDO_PANEL           = Color.WHITE;
    private static final Color NAVY                  = new Color(44, 62, 80);
    private static final Color NAVY_OSCURO           = new Color(33, 47, 60);
    private static final Color AZUL_ACCENT           = new Color(52, 152, 219);
    private static final Color AZUL_HOVER            = new Color(41, 128, 185);
    private static final Color VERDE_EXITO           = new Color(46, 204, 113);
    private static final Color ROJO_ERROR            = new Color(231, 76, 60);
    private static final Color NARANJA_WARNING       = new Color(243, 156, 18);
    private static final Color TEXTO_CLARO           = new Color(236, 240, 241);
    private static final Color TEXTO_OSCURO          = new Color(52, 73, 94);
    private static final Color TEXTO_SUBTITULO       = new Color(127, 140, 141);
    private static final Color BORDE                 = new Color(189, 195, 199);
    private static final Color FONDO_CODIGO          = new Color(40, 44, 52);
    private static final Color TEXTO_CODIGO          = new Color(171, 178, 191);
    private static final Color FILA_TABLA_PAR        = new Color(245, 247, 250);

    // ============================================================
    // TIPOGRAFÍA
    // ============================================================

    private static final Font FUENTE_UI       = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FUENTE_UI_BOLD  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FUENTE_CODIGO   = new Font("Consolas", Font.PLAIN, 14);
    private static final Font FUENTE_TITULO   = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FUENTE_TABLA    = new Font("Segoe UI", Font.PLAIN, 12);

    // ============================================================
    // COMPONENTES
    // ============================================================

    private JTextArea areaCodigo;
    private JTable tablaTokens;
    private DefaultTableModel modeloTokens;
    private JTextArea areaErrores;
    private JTextArea areaResultadoSintactico;
    private JTabbedPane panelResultados;

    private JButton btnAnalizar;
    private JButton btnLimpiar;
    private JButton btnAbrir;
    private JButton btnGuardar;
    private JButton btnRegresar;

    private JLabel labelEstado;

    private CompiladorListener listener;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public VistaCompilador() {
        super("Compilador - IDE de Análisis");
        inicializarComponentes();
        configurarVentana();
    }

    // ============================================================
    // CONFIGURACIÓN INICIAL
    // ============================================================

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setIconImage(crearImagenIconoApp());
    }

    private void inicializarComponentes() {
        setJMenuBar(crearMenuBar());
        add(crearPanelToolbar(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);
    }

    // ============================================================
    // BARRA DE MENÚ
    // ============================================================

    private JMenuBar crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(NAVY);
        menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JMenu menuArchivo = crearMenu("Archivo", 'A');
        menuArchivo.add(crearMenuItem("Nuevo",          KeyEvent.VK_N, e -> limpiarTodo()));
        menuArchivo.add(crearMenuItem("Abrir",          KeyEvent.VK_A, e -> abrirArchivo()));
        menuArchivo.add(crearMenuItem("Guardar",        KeyEvent.VK_G, e -> guardarArchivo()));
        menuArchivo.add(crearMenuItem("Guardar Como",   KeyEvent.VK_S, e -> guardarArchivoComo()));
        menuArchivo.addSeparator();
        menuArchivo.add(crearMenuItem("Salir",          KeyEvent.VK_Q, e -> System.exit(0)));

        JMenu menuEditar = crearMenu("Editar", 'E');
        menuEditar.add(crearMenuItem("Deshacer",        KeyEvent.VK_Z, e -> {}));
        menuEditar.add(crearMenuItem("Rehacer",         KeyEvent.VK_Y, e -> {}));
        menuEditar.addSeparator();
        menuEditar.add(crearMenuItem("Cortar",          KeyEvent.VK_X, e -> areaCodigo.cut()));
        menuEditar.add(crearMenuItem("Copiar",          KeyEvent.VK_C, e -> areaCodigo.copy()));
        menuEditar.add(crearMenuItem("Pegar",           KeyEvent.VK_V, e -> areaCodigo.paste()));
        menuEditar.addSeparator();
        menuEditar.add(crearMenuItem("Seleccionar Todo", KeyEvent.VK_E, e -> areaCodigo.selectAll()));

        JMenu menuEjecutar = crearMenu("Ejecutar", 'E');
        menuEjecutar.add(crearMenuItem("Analizar",      KeyEvent.VK_R, e -> analizarCodigo()));
        menuEjecutar.add(crearMenuItem("Limpiar",       KeyEvent.VK_L, e -> limpiarTodo()));

        JMenu menuAyuda = crearMenu("Ayuda", 'A');
        menuAyuda.add(crearMenuItem("Acerca de",        KeyEvent.VK_F1, e -> mostrarAcercaDe()));

        menuBar.add(menuArchivo);
        menuBar.add(menuEditar);
        menuBar.add(menuEjecutar);
        menuBar.add(menuAyuda);

        return menuBar;
    }

    private JMenu crearMenu(String texto, char mnemonic) {
        JMenu menu = new JMenu(texto);
        menu.setForeground(TEXTO_CLARO);
        menu.setFont(FUENTE_UI);
        menu.setMnemonic(mnemonic);
        return menu;
    }

    private JMenuItem crearMenuItem(String texto, int tecla, ActionListener accion) {
        JMenuItem item = new JMenuItem(texto);
        item.setFont(FUENTE_UI);
        item.setAccelerator(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(accion);
        item.setBackground(Color.WHITE);
        item.setForeground(TEXTO_OSCURO);
        return item;
    }

    // ============================================================
    // TOOLBAR (BOTONES)
    // ============================================================

    private JPanel crearPanelToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        panel.setBackground(FONDO_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        btnAbrir    = crearBotonToolbar("Abrir Archivo",   crearIcono(0x1F4C2, AZUL_ACCENT),  e -> abrirArchivo());
        btnGuardar  = crearBotonToolbar("Guardar",         crearIcono(0x1F4BE, AZUL_ACCENT),  e -> guardarArchivo());
        panel.add(crearSeparadorToolbar());
        btnAnalizar = crearBotonToolbar("Analizar",        crearIcono(0x25B6, VERDE_EXITO),   e -> analizarCodigo());
        btnLimpiar  = crearBotonToolbar("Limpiar",        crearIcono(0x1F5D1, NARANJA_WARNING), e -> limpiarTodo());
        panel.add(crearSeparadorToolbar());
        btnRegresar = crearBotonToolbar("Regresar",        crearIcono(0x1F3E0, NAVY),         e -> regresarPaginaPrincipal());

        return panel;
    }

    private JButton crearBotonToolbar(String texto, Icon icono, ActionListener accion) {
        JButton btn = new JButton(texto, icono);
        btn.setFont(FUENTE_UI_BOLD);
        btn.setForeground(TEXTO_OSCURO);
        btn.setBackground(FONDO_PANEL);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(accion);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(240, 242, 245));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AZUL_ACCENT, 1, true),
                        BorderFactory.createEmptyBorder(6, 14, 6, 14)));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(FONDO_PANEL);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE, 1, true),
                        BorderFactory.createEmptyBorder(6, 14, 6, 14)));
            }
        });
        return btn;
    }

    private JSeparator crearSeparadorToolbar() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 30));
        sep.setForeground(BORDE);
        return sep;
    }

    // ============================================================
    // PANEL CENTRAL: EDITOR + RESULTADOS
    // ============================================================

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(FONDO_PRINCIPAL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(crearPanelEditor());
        split.setRightComponent(crearPanelResultados());
        split.setResizeWeight(0.6);
        split.setBorder(null);
        split.setDividerSize(4);
        split.setBackground(FONDO_PRINCIPAL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(split, gbc);

        return panel;
    }

    // ============================================================
    // PANEL IZQUIERDO: EDITOR DE CÓDIGO
    // ============================================================

    private JPanel crearPanelEditor() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(FONDO_PRINCIPAL);

        JLabel lblTitulo = new JLabel("C\u00f3digo Fuente");
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(TEXTO_OSCURO);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));

        areaCodigo = new JTextArea();
        areaCodigo.setFont(FUENTE_CODIGO);
        areaCodigo.setBackground(FONDO_CODIGO);
        areaCodigo.setForeground(TEXTO_CODIGO);
        areaCodigo.setCaretColor(TEXTO_CLARO);
        areaCodigo.setTabSize(4);
        areaCodigo.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        areaCodigo.setLineWrap(false);

        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        scrollCodigo.setBorder(BorderFactory.createLineBorder(BORDE));
        scrollCodigo.getViewport().setBackground(FONDO_CODIGO);

        JPanel contenedorScroll = new JPanel(new BorderLayout());
        contenedorScroll.setBackground(FONDO_PRINCIPAL);
        contenedorScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        contenedorScroll.add(scrollCodigo, BorderLayout.CENTER);

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(contenedorScroll, BorderLayout.CENTER);

        return panel;
    }

    // ============================================================
    // PANEL DERECHO: RESULTADOS (TABBED)
    // ============================================================

    private JPanel crearPanelResultados() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(FONDO_PRINCIPAL);

        JLabel lblTitulo = new JLabel("Resultados del An\u00e1lisis");
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(TEXTO_OSCURO);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));

        panelResultados = new JTabbedPane();
        panelResultados.setFont(FUENTE_UI);
        panelResultados.setBackground(FONDO_PANEL);
        panelResultados.setForeground(TEXTO_OSCURO);

        panelResultados.addTab("Tokens",         crearIconoPestana(0x1F4CB, AZUL_ACCENT),  crearPanelTokens());
        panelResultados.addTab("Resultado Sint\u00e1ctico", crearIconoPestana(0x1F4C4, VERDE_EXITO), crearPanelResultadoSintactico());
        panelResultados.addTab("Errores",        crearIconoPestana(0x26A0, ROJO_ERROR),     crearPanelErrores());

        // --- Tab: Tokens --- (JTable)
        String[] columnas = {"#", "Token", "Lexema", "L\u00ednea", "Columna"};
        modeloTokens = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaTokens = new JTable(modeloTokens);
        tablaTokens.setFont(FUENTE_TABLA);
        tablaTokens.setRowHeight(26);
        tablaTokens.getTableHeader().setFont(FUENTE_UI_BOLD);
        tablaTokens.getTableHeader().setBackground(NAVY);
        tablaTokens.getTableHeader().setForeground(TEXTO_CLARO);
        tablaTokens.setShowGrid(false);
        tablaTokens.setIntercellSpacing(new Dimension(0, 0));
        tablaTokens.setSelectionBackground(new Color(214, 234, 248));
        tablaTokens.setSelectionForeground(TEXTO_OSCURO);

        // Colores alternos de fila
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? FONDO_PANEL : FILA_TABLA_PAR);
                }
                c.setFont(FUENTE_TABLA);
                return c;
            }
        };
        for (int i = 0; i < tablaTokens.getColumnCount(); i++) {
            tablaTokens.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // --- Tab: Resultado Sintáctico ---
        areaResultadoSintactico = new JTextArea();
        areaResultadoSintactico.setFont(FUENTE_CODIGO);
        areaResultadoSintactico.setBackground(FONDO_PANEL);
        areaResultadoSintactico.setForeground(TEXTO_OSCURO);
        areaResultadoSintactico.setEditable(false);
        areaResultadoSintactico.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // --- Tab: Errores ---
        areaErrores = new JTextArea();
        areaErrores.setFont(FUENTE_CODIGO);
        areaErrores.setBackground(new Color(253, 245, 245));
        areaErrores.setForeground(ROJO_ERROR);
        areaErrores.setEditable(false);
        areaErrores.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelResultados, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane crearPanelTokens() {
        JScrollPane scroll = new JScrollPane(tablaTokens);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FONDO_PANEL);
        return scroll;
    }

    private JScrollPane crearPanelResultadoSintactico() {
        JScrollPane scroll = new JScrollPane(areaResultadoSintactico);
        scroll.setBorder(null);
        return scroll;
    }

    private JScrollPane crearPanelErrores() {
        JScrollPane scroll = new JScrollPane(areaErrores);
        scroll.setBorder(null);
        return scroll;
    }

    // ============================================================
    // BARRA DE ESTADO
    // ============================================================

    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(NAVY_OSCURO);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        labelEstado = new JLabel("Listo");
        labelEstado.setFont(FUENTE_UI);
        labelEstado.setForeground(TEXTO_CLARO);

        JLabel labelVersion = new JLabel("v1.0.0");
        labelVersion.setFont(FUENTE_UI);
        labelVersion.setForeground(TEXTO_SUBTITULO);

        panel.add(labelEstado, BorderLayout.WEST);
        panel.add(labelVersion, BorderLayout.EAST);

        return panel;
    }

    // ============================================================
    // MÉTODOS DE ACCIÓN (DELEGAN AL LISTENER)
    // ============================================================

    private void analizarCodigo() {
        if (listener == null) {
            mostrarAdvertencia("No hay un controlador asignado.\nConecte el compilador usando setCompiladorListener().");
            return;
        }
        listener.onAnalizar(areaCodigo.getText());
    }

    private void limpiarTodo() {
        areaCodigo.setText("");
        modeloTokens.setRowCount(0);
        areaErrores.setText("");
        areaResultadoSintactico.setText("");
        labelEstado.setText("Todo limpiado");
        if (listener != null) listener.onLimpiar();
    }

    private void abrirArchivo() {
        if (listener != null) {
            String contenido = listener.onAbrirArchivo();
            if (contenido != null) {
                areaCodigo.setText(contenido);
                labelEstado.setText("Archivo cargado correctamente");
            }
            return;
        }
        // Fallback nativo si no hay listener
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos de código (*.txt, *.java, *.c, *.cpp)", "txt", "java", "c", "cpp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                String linea;
                StringBuilder sb = new StringBuilder();
                while ((linea = br.readLine()) != null) {
                    sb.append(linea).append("\n");
                }
                areaCodigo.setText(sb.toString());
                labelEstado.setText("Archivo cargado: " + fc.getSelectedFile().getName());
            } catch (IOException ex) {
                mostrarError("Error al abrir archivo:\n" + ex.getMessage());
            }
        }
    }

    private void guardarArchivo() {
        if (listener != null) {
            listener.onGuardarArchivo(areaCodigo.getText());
            return;
        }
        // Fallback nativo
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("codigo_fuente.txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()))) {
                bw.write(areaCodigo.getText());
                labelEstado.setText("Archivo guardado: " + fc.getSelectedFile().getName());
            } catch (IOException ex) {
                mostrarError("Error al guardar archivo:\n" + ex.getMessage());
            }
        }
    }

    private void guardarArchivoComo() {
        listener.onGuardarArchivo(areaCodigo.getText());
    }

    private void regresarPaginaPrincipal() {
        if (listener != null) {
            listener.onRegresarPaginaPrincipal();
        }
    }

    // ============================================================
    // MÉTODOS PÚBLICOS PARA EL CONTROLADOR
    // ============================================================

    /**
     * Asigna el listener que manejará los eventos del compilador.
     * @param listener Implementación de CompiladorListener.
     */
    public void setCompiladorListener(CompiladorListener listener) {
        this.listener = listener;
    }

    /**
     * Agrega una fila a la tabla de tokens.
     * @param numero       Número de token.
     * @param token        Nombre del token.
     * @param lexema       Valor del lexema.
     * @param linea        Línea donde aparece.
     * @param columna      Columna donde aparece.
     */
    public void agregarToken(int numero, String token, String lexema, int linea, int columna) {
        modeloTokens.addRow(new Object[]{numero, token, lexema, linea, columna});
    }

    /**
     * Limpia la tabla de tokens.
     */
    public void limpiarTokens() {
        modeloTokens.setRowCount(0);
    }

    /**
     * Muestra un mensaje en el área de errores.
     * @param mensaje Descripción del error.
     */
    public void mostrarError(String mensaje) {
        areaErrores.setText(mensaje);
        panelResultados.setSelectedIndex(2);
        labelEstado.setText("Errores encontrados");
    }

    /**
     * Agrega un error al área de errores sin sobrescribir.
     * @param mensaje Descripción del error.
     */
    public void agregarError(String mensaje) {
        areaErrores.append(mensaje + "\n");
        panelResultados.setSelectedIndex(2);
        labelEstado.setText("Errores encontrados");
    }

    /**
     * Muestra el resultado del análisis sintáctico.
     * @param resultado Árbol o descripción del análisis.
     */
    public void mostrarResultadoSintactico(String resultado) {
        areaResultadoSintactico.setText(resultado);
        labelEstado.setText("Análisis completado");
    }

    /**
     * Actualiza el mensaje en la barra de estado.
     * @param mensaje Texto a mostrar.
     */
    public void setEstado(String mensaje) {
        labelEstado.setText(mensaje);
    }

    /**
     * Obtiene el código fuente actual del editor.
     * @return Texto del área de código.
     */
    public String getCodigoFuente() {
        return areaCodigo.getText();
    }

    /**
     * Establece el código fuente en el editor.
     * @param codigo Texto a colocar.
     */
    public void setCodigoFuente(String codigo) {
        areaCodigo.setText(codigo);
    }

    // ============================================================
    // DIÁLOGOS
    // ============================================================

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "Compilador - IDE de Análisis v1.0.0\n\n" +
                "Herramienta educativa para el análisis léxico,\nsintáctico y semántico de lenguajes de programación.\n\n" +
                "Desarrollado para el Proyecto de Compiladores.",
                "Acerca del Compilador",
                JOptionPane.INFORMATION_MESSAGE,
                crearIcono(0x2139, AZUL_ACCENT, 32));
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    // ============================================================
    // GENERACIÓN DE ICONOS PROGRAMÁTICAMENTE
    // ============================================================

    /**
     * Crea un icono circular con un carácter Unicode en el centro.
     */
    private ImageIcon crearIcono(int unicodeChar, Color color) {
        return crearIcono(unicodeChar, color, 18);
    }

    private ImageIcon crearIcono(int unicodeChar, Color color, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fillOval(0, 0, size - 1, size - 1);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size - 6));
        FontMetrics fm = g.getFontMetrics();
        String ch = String.valueOf((char) unicodeChar);
        int x = (size - fm.stringWidth(ch)) / 2;
        int y = (size + fm.getHeight() / 2) / 2 - 1;
        g.drawString(ch, x, y);
        g.dispose();
        return new ImageIcon(img);
    }

    private ImageIcon crearIconoPestana(int unicodeChar, Color color) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fillOval(1, 1, 13, 13);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI Symbol", Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String ch = String.valueOf((char) unicodeChar);
        int x = (16 - fm.stringWidth(ch)) / 2;
        int y = (16 + fm.getHeight() / 2) / 2 - 1;
        g.drawString(ch, x, y);
        g.dispose();
        return new ImageIcon(img);
    }

    private Image crearImagenIconoApp() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(AZUL_ACCENT);
        g.fillRoundRect(4, 4, 56, 56, 12, 12);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("C>", (64 - fm.stringWidth("C>")) / 2, (64 + fm.getAscent() / 2) / 2);
        g.dispose();
        return img;
    }

    // ============================================================
    // MÉTODO MAIN (PARA PRUEBAS INDEPENDIENTES)
    // ============================================================

    /**
     * Punto de entrada para pruebas de la interfaz sin el compilador.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            VistaCompilador vista = new VistaCompilador();
            vista.setVisible(true);
        });
    }


}
