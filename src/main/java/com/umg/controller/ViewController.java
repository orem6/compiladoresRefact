package com.umg.controller;
import com.umg.model.ViewModel;
import com.umg.model.lexer.*;
import com.umg.model.dialect.SqlDialect;

import java.awt.event.*;

public class ViewController implements ActionListener, MouseListener, WindowListener {
    private final ViewModel view;
    private ResultadoLexer ultimoResultado;

    public ViewController(ViewModel viewModel) {
        this.view = viewModel;
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
        }
    }

    private void analizarConsulta() {
        String sql = view.getVista().TxAConsultas.getText();
        if (sql.trim().isEmpty()) {
            view.getVista().resultArea.setText("⚠ Error: No hay consulta SQL para analizar.\nEscribe una consulta en el area de texto.");
            return;
        }

        try {
            SqlDialect dialecto = obtenerDialectoSeleccionado();
            AnalizadorSql analizador = new AnalizadorSql();
            ResultadoLexer resultado;

            if (dialecto != null) {
                resultado = analizador.analizar(sql, dialecto);
            } else {
                resultado = analizador.analizar(sql);
            }

            ultimoResultado = resultado;

            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════\n");
            sb.append("          RESULTADO DEL ANALISIS\n");
            sb.append("═══════════════════════════════════════════════\n\n");

            if (resultado.isValido()) {
                sb.append("✓ CONSULTA VALIDA\n");
            } else {
                sb.append("✗ CONSULTA INVALIDA\n");
            }

            sb.append("  Mensaje: ").append(resultado.getMensaje()).append("\n");
            sb.append("  Dialecto detectado: ").append(resultado.getDialectoDetectado()).append("\n");
            sb.append("  Tokens encontrados: ").append(resultado.getTokens().size()).append("\n");
            sb.append("  Errores encontrados: ").append(resultado.getErrores().size()).append("\n");

            if (!resultado.getErrores().isEmpty()) {
                sb.append("\n-- Errores ---------------------------------\n");
                for (ErrorLexico err : resultado.getErrores()) {
                    sb.append("  • ").append(err.toString()).append("\n");
                }
            }

            view.getVista().resultArea.setText(sb.toString());

        } catch (Exception ex) {
            view.getVista().resultArea.setText("✗ Error inesperado: " + ex.getMessage());
        }
    }

    private SqlDialect obtenerDialectoSeleccionado() {
        String seleccion = (String) view.getVista().DataBases.getSelectedItem();
        if (seleccion == null) return null;
        return switch (seleccion) {
            case "Postgres" -> SqlDialect.POSTGRESQL;
            case "SQL Server" -> SqlDialect.SQL_SERVER;
            case "MariaDB" -> SqlDialect.MYSQL;
            default -> null;
        };
    }

    private void mostrarConsola() {
        if (ultimoResultado == null) {
            view.getVista().resultArea.setText("i No hay analisis previo.\nPresiona 'Analizar' primero.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("                 CONSOLA\n");
        sb.append("═══════════════════════════════════════════════\n\n");
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
            view.getVista().resultArea.setText("i No hay analisis previo.\nPresiona 'Analizar' primero.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("             TABLA DE TOKENS\n");
        sb.append("═══════════════════════════════════════════════\n\n");

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

            ALTER TABLE:
              ALTER TABLE tabla ADD COLUMN col TIPO;

            TRUNCATE:
              TRUNCATE TABLE tabla;

            --- BOTONES -----------------------------------

            Analizar  - Ejecuta el analisis lexico y sintactico
            Limpiar   - Borra el contenido de la consulta y resultados
            Consola   - Muestra el detalle completo del analisis
            Tokens    - Muestra la lista de tokens generados
            Ayuda     - Muestra esta guia de uso
            Regresar  - Cierra la aplicacion

            --- DIALECTOS ---------------------------------

            Puedes seleccionar un motor en el combo superior
            para validar funciones especificas de cada dialecto:
              MariaDB    -> Funciones de MySQL
              Postgres   -> Funciones de PostgreSQL
              SQL Server -> Funciones de SQL Server

            --- EJEMPLOS RAPIDOS ---------------------------

            1. SELECT * FROM clientes;
            2. SELECT id, nombre FROM usuarios WHERE edad > 18;
            3. INSERT INTO productos (nombre, precio) VALUES ('Laptop', 999.99);
            4. UPDATE empleados SET salario = 5000 WHERE id = 10;
            5. DELETE FROM pedidos WHERE fecha < '2024-01-01';
            6. CREATE TABLE test (id INT PRIMARY KEY, dato VARCHAR(50));
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
    }
}
