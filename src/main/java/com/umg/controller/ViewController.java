package com.umg.controller;
import com.umg.model.ViewModel;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.lexer.*;
import com.umg.model.semantic.AnalizadorSemanticoSql;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.metadata.DatabaseMetadataService;
import com.umg.model.semantic.metadata.JdbcConnectionFactory;
import com.umg.model.semantic.metadata.JdbcDatabaseMetadataService;
import com.umg.model.semantic.result.ResultadoSemantico;

import java.awt.event.*;
import java.sql.Connection;

public class ViewController implements ActionListener, MouseListener, WindowListener {
    private final ViewModel view;
    private ResultadoLexer ultimoResultado;
    private ResultadoSemantico ultimoResultadoSemantico;
    private AnalizadorSemanticoSql analizadorSemantico;
    private boolean conexionActiva;

    public ViewController(ViewModel viewModel) {
        this.view = viewModel;
        this.analizadorSemantico = new AnalizadorSemanticoSql();
        this.conexionActiva = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object fuente = e.getSource();

        if (fuente == view.getVista().BtnAnalizar) {
            analizarConsulta();
        } else if (fuente == view.getVista().BtnLimpiar) {
            limpiarCampos();
        } else if (fuente == view.getVista().BtnConsola) {
            mostrarConsola();
        } else if (fuente == view.getVista().BtnTokens) {
            mostrarTokens();
        } else if (fuente == view.getVista().BtnAyuda) {
            mostrarAyuda();
        } else if (fuente == view.getVista().BtnRegesar) {
            System.exit(0);
        } else if (fuente == view.getVista().BtnConectar) {
            conectarBaseDatos();
        }
    }

    private void conectarBaseDatos() {
        String host = view.getVista().txtHost.getText().trim();
        String puertoStr = view.getVista().txtPuerto.getText().trim();
        String baseDatos = view.getVista().txtBaseDatos.getText().trim();
        String usuario = view.getVista().txtUsuario.getText().trim();
        String password = new String(view.getVista().txtPassword.getPassword());

        if (host.isEmpty() || puertoStr.isEmpty() || baseDatos.isEmpty() || usuario.isEmpty()) {
            view.getVista().lblEstadoConexion.setText("Campos incompletos");
            view.getVista().lblEstadoConexion.setForeground(new java.awt.Color(255, 200, 0));
            return;
        }

        int puerto;
        try {
            puerto = Integer.parseInt(puertoStr);
        } catch (NumberFormatException ex) {
            view.getVista().lblEstadoConexion.setText("Puerto invalido");
            view.getVista().lblEstadoConexion.setForeground(new java.awt.Color(255, 100, 100));
            return;
        }

        String seleccion = (String) view.getVista().DataBases.getSelectedItem();
        SqlDialect dialecto = obtenerDialecto(seleccion);
        if (dialecto == null) {
            view.getVista().lblEstadoConexion.setText("Selecciona un dialecto");
            view.getVista().lblEstadoConexion.setForeground(new java.awt.Color(255, 200, 0));
            return;
        }

        ConexionBaseDatosConfig config = new ConexionBaseDatosConfig();
        config.setDialecto(dialecto);
        config.setHost(host);
        config.setPuerto(puerto);
        config.setBaseDatos(baseDatos);
        config.setUsuario(usuario);
        config.setPassword(password);

        JdbcConnectionFactory factory = new JdbcConnectionFactory();
        try (Connection conn = factory.crearConexion(config)) {
            if (conn != null && conn.isValid(5)) {
                conexionActiva = true;
                view.getVista().lblEstadoConexion.setText("Conectado");
                view.getVista().lblEstadoConexion.setForeground(new java.awt.Color(100, 255, 100));
                view.getVista().resultArea.setText("Conexion exitosa a " + dialecto + "\n"
                    + "Host: " + host + ":" + puerto + "\n"
                    + "Base de datos: " + baseDatos);
            }
        } catch (Exception ex) {
            conexionActiva = false;
            view.getVista().lblEstadoConexion.setText("Error");
            view.getVista().lblEstadoConexion.setForeground(new java.awt.Color(255, 100, 100));
            view.getVista().resultArea.setText("Error de conexion:\n" + ex.getMessage());
        }
    }

    private void analizarConsulta() {
        String sql = view.getVista().TxAConsultas.getText();
        if (sql.trim().isEmpty()) {
            view.getVista().resultArea.setText("Error: No hay consulta SQL para analizar.\nEscribe una consulta en el area de texto.");
            return;
        }

        try {
            SqlDialect dialecto = obtenerDialectoSeleccionado();

            if (conexionActiva && dialecto != null) {
                ConexionBaseDatosConfig config = crearConfigDesdeVista(dialecto);
                ResultadoSemantico resultadoSemantico = analizadorSemantico.analizar(sql, config);
                ultimoResultadoSemantico = resultadoSemantico;
                ultimoResultado = resultadoSemantico.getResultadoLexer();

                view.getVista().resultArea.setText(resultadoSemantico.getResumenParaVista());
            } else {
                AnalizadorSql analizador = new AnalizadorSql();
                ResultadoLexer resultado;

                if (dialecto != null) {
                    resultado = analizador.analizar(sql, dialecto);
                } else {
                    resultado = analizador.analizar(sql);
                }

                ultimoResultado = resultado;
                ultimoResultadoSemantico = null;

                StringBuilder sb = new StringBuilder();
                sb.append("===============================================\n");
                sb.append("          RESULTADO DEL ANALISIS\n");
                sb.append("===============================================\n\n");

                if (resultado.isValido()) {
                    sb.append("  CONSULTA VALIDA\n");
                } else {
                    sb.append("  CONSULTA INVALIDA\n");
                }

                sb.append("  Mensaje: ").append(resultado.getMensaje()).append("\n");
                sb.append("  Dialecto detectado: ").append(resultado.getDialectoDetectado()).append("\n");
                sb.append("  Tokens encontrados: ").append(resultado.getTokens().size()).append("\n");
                sb.append("  Errores encontrados: ").append(resultado.getErrores().size()).append("\n");

                if (!conexionActiva) {
                    sb.append("\n  Nota: Conectate a una BD para validacion semantica.\n");
                }

                if (!resultado.getErrores().isEmpty()) {
                    sb.append("\n-- Errores ---------------------------------\n");
                    for (ErrorLexico err : resultado.getErrores()) {
                        sb.append("  * ").append(err.toString()).append("\n");
                    }
                }

                view.getVista().resultArea.setText(sb.toString());
            }

        } catch (Exception ex) {
            view.getVista().resultArea.setText("Error inesperado: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private ConexionBaseDatosConfig crearConfigDesdeVista(SqlDialect dialecto) {
        ConexionBaseDatosConfig config = new ConexionBaseDatosConfig();
        config.setDialecto(dialecto);
        config.setHost(view.getVista().txtHost.getText().trim());
        try {
            config.setPuerto(Integer.parseInt(view.getVista().txtPuerto.getText().trim()));
        } catch (NumberFormatException e) {
            config.setPuerto(dialecto == SqlDialect.MYSQL ? 3306 :
                dialecto == SqlDialect.POSTGRESQL ? 5432 : 1433);
        }
        config.setBaseDatos(view.getVista().txtBaseDatos.getText().trim());
        config.setUsuario(view.getVista().txtUsuario.getText().trim());
        config.setPassword(new String(view.getVista().txtPassword.getPassword()));
        return config;
    }

    private SqlDialect obtenerDialectoSeleccionado() {
        String seleccion = (String) view.getVista().DataBases.getSelectedItem();
        return obtenerDialecto(seleccion);
    }

    private SqlDialect obtenerDialecto(String seleccion) {
        if (seleccion == null) return null;
        return switch (seleccion) {
            case "MySQL" -> SqlDialect.MYSQL;
            case "Postgres" -> SqlDialect.POSTGRESQL;
            case "SQL Server" -> SqlDialect.SQL_SERVER;
            case "MariaDB" -> SqlDialect.MYSQL;
            default -> null;
        };
    }

    private void mostrarConsola() {
        if (ultimoResultadoSemantico != null) {
            view.getVista().resultArea.setText(ultimoResultadoSemantico.getResumenParaVista());
            return;
        }
        if (ultimoResultado == null) {
            view.getVista().resultArea.setText("No hay analisis previo.\nPresiona 'Analizar' primero.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("===============================================\n");
        sb.append("                 CONSOLA\n");
        sb.append("===============================================\n\n");
        sb.append("Valido: ").append(ultimoResultado.isValido()).append("\n");
        sb.append("Mensaje: ").append(ultimoResultado.getMensaje()).append("\n");
        sb.append("Dialecto detectado: ").append(ultimoResultado.getDialectoDetectado()).append("\n");
        sb.append("Dialectos compatibles: ").append(ultimoResultado.getDialectosCompatibles()).append("\n");
        sb.append("Sintaxis valida: ").append(ultimoResultado.isSintaxisBasicaValida()).append("\n\n");

        sb.append("-- Tokens ------------------------------------\n");
        for (Token t : ultimoResultado.getTokens()) {
            sb.append("  ").append(t.toString()).append("\n");
        }

        sb.append("\n-- Errores -----------------------------------\n");
        if (ultimoResultado.getErrores().isEmpty()) {
            sb.append("  Sin errores.\n");
        } else {
            for (ErrorLexico err : ultimoResultado.getErrores()) {
                sb.append("  ").append(err.toString()).append("\n");
            }
        }

        view.getVista().resultArea.setText(sb.toString());
    }

    private void mostrarTokens() {
        if (ultimoResultado == null) {
            view.getVista().resultArea.setText("No hay analisis previo.\nPresiona 'Analizar' primero.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("===============================================\n");
        sb.append("             TABLA DE TOKENS\n");
        sb.append("===============================================\n\n");

        int i = 0;
        for (Token t : ultimoResultado.getTokens()) {
            sb.append(String.format("%-4d | %s%n", i++, t.toString()));
        }

        sb.append("\nTotal de tokens: ").append(ultimoResultado.getTokens().size()).append("\n");

        view.getVista().resultArea.setText(sb.toString());
    }

    private void mostrarAyuda() {
        String ayuda = """
            +==============================================+
            |               GUIA DE AYUDA                  |
            +==============================================+

            --- CONSULTAS SOPORTADAS -----------------------

            SELECT:
              SELECT * FROM tabla;
              SELECT col1, col2 FROM tabla WHERE condicion;
              SELECT COUNT(*) FROM tabla GROUP BY col;

            INSERT:
              INSERT INTO tabla (col1, col2) VALUES ('val', 1);

            UPDATE:
              UPDATE tabla SET col = 'valor' WHERE condicion;

            DELETE:
              DELETE FROM tabla WHERE condicion;

            CREATE TABLE:
              CREATE TABLE tabla (id INT, nombre VARCHAR(100));

            DROP TABLE:
              DROP TABLE tabla;

            --- BOTONES -----------------------------------

            Analizar  - Ejecuta analisis lexico, sintactico y semantico
            Limpiar   - Borra consulta y resultados
            Consola   - Muestra detalle completo del analisis
            Tokens    - Muestra lista de tokens generados
            Ayuda     - Muestra esta guia
            Regresar  - Cierra la aplicacion

            --- CONEXION BD --------------------------------

            Llena los campos Host, Puerto, BD, User, Pass
            y presiona Conectar para validacion semantica
            contra base de datos real (MySQL, PostgreSQL,
            SQL Server o MariaDB).

            --- EJEMPLOS RAPIDOS ---------------------------

            1. SELECT * FROM clientes;
            2. SELECT id, nombre FROM usuarios WHERE edad > 18;
            3. INSERT INTO productos (nombre, precio) VALUES ('Laptop', 999.99);
            4. UPDATE empleados SET salario = 5000 WHERE id = 10;
            5. DELETE FROM pedidos WHERE fecha < '2024-01-01';
            """;

        view.getVista().resultArea.setText(ayuda);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void windowOpened(WindowEvent e) {}

    @Override
    public void windowClosing(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}

    public void limpiarCampos(){
        view.getVista().TxAConsultas.setText("");
        view.getVista().resultArea.setText("");
        ultimoResultado = null;
        ultimoResultadoSemantico = null;
    }
}
