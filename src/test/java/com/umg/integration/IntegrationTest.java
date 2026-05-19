package com.umg.integration;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.lexer.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    @Test
    void testFullCompilationSelect() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT * FROM clientes;");
        assertTrue(resultado.isValido(), "SELECT simple deberia ser valido");
        assertTrue(resultado.hasTokens());
        assertFalse(resultado.hasErrores());
        assertNotNull(resultado.getMensaje());
    }

    @Test
    void testFullCompilationSelectWithWhere() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT id, nombre FROM clientes WHERE estado = 1;");
        assertTrue(resultado.isValido());
        assertFalse(resultado.hasErrores());
    }

    @Test
    void testFullCompilationCreateTable() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("CREATE TABLE clientes (id INT PRIMARY KEY, nombre VARCHAR(100));");
        assertTrue(resultado.isValido(), "CREATE TABLE deberia ser valido");
    }

    @Test
    void testInsertCompilation() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("INSERT INTO clientes (nombre, edad) VALUES ('Kevin', 25);");
        assertTrue(resultado.isValido(), "INSERT deberia ser valido");
    }

    @Test
    void testUpdateCompilation() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("UPDATE clientes SET estado = 1 WHERE id = 10;");
        assertTrue(resultado.isValido(), "UPDATE deberia ser valido");
    }

    @Test
    void testDeleteCompilation() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("DELETE FROM clientes WHERE id = 10;");
        assertTrue(resultado.isValido(), "DELETE deberia ser valido");
    }

    @Test
    void testAlterTableAddColumn() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("ALTER TABLE clientes ADD COLUMN correo VARCHAR(100);");
        assertTrue(resultado.isValido(), "ALTER TABLE ADD deberia ser valido");
    }

    @Test
    void testDropTable() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("DROP TABLE clientes;");
        assertTrue(resultado.isValido(), "DROP TABLE deberia ser valido");
    }

    @Test
    void testTruncateTable() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("TRUNCATE TABLE clientes;");
        assertTrue(resultado.isValido(), "TRUNCATE deberia ser valido");
    }

    @Test
    void testInnerJoin() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT a.id FROM clientes a INNER JOIN pedidos p ON a.id = p.cliente_id;");
        assertTrue(resultado.isValido(), "INNER JOIN deberia ser valido");
    }

    @Test
    void testWithCTE() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("WITH cte AS (SELECT id FROM clientes) SELECT * FROM cte;");
        assertTrue(resultado.isValido(), "WITH CTE deberia ser valido");
    }

    @Test
    void testCommonFunctions() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT COUNT(*) FROM clientes;");
        assertTrue(resultado.isValido(), "COUNT deberia ser valido");

        resultado = analizador.analizar("SELECT CONCAT(nombre, ' ', apellido) FROM clientes;");
        assertTrue(resultado.isValido(), "CONCAT deberia ser valido");
    }

    @Test
    void testMySqlDelimitedSyntax() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT `nombre_cliente` FROM `clientes`;");
        assertTrue(resultado.isValido(), "MySQL backtick deberia ser valido");
        assertEquals(SqlDialect.MYSQL, resultado.getDialectoDetectado());
    }

    @Test
    void testPostgreSqlDelimitedSyntax() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT \"nombre_cliente\" FROM \"clientes\";");
        assertTrue(resultado.isValido(), "PostgreSQL double-quote deberia ser valido");
        assertEquals(SqlDialect.POSTGRESQL, resultado.getDialectoDetectado());
    }

    @Test
    void testSqlServerDelimitedSyntax() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT [nombre cliente] FROM [dbo].[clientes];");
        assertTrue(resultado.isValido(), "SQL Server bracket deberia ser valido");
        assertEquals(SqlDialect.SQL_SERVER, resultado.getDialectoDetectado());
    }

    @Test
    void testMySqlFunctionValidation() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT DATE_FORMAT(fecha, '%Y-%m-%d') FROM ventas;", SqlDialect.MYSQL);
        assertTrue(resultado.isValido(), "DATE_FORMAT deberia ser valido para MySQL");
    }

    @Test
    void testPostgreSqlFunctionValidation() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT TO_CHAR(fecha, 'YYYY-MM-DD') FROM ventas;", SqlDialect.POSTGRESQL);
        assertTrue(resultado.isValido(), "TO_CHAR deberia ser valido para PostgreSQL");
    }

    @Test
    void testSqlServerFunctionValidation() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT ISNULL(nombre, 'N/A') FROM clientes;", SqlDialect.SQL_SERVER);
        assertTrue(resultado.isValido(), "ISNULL deberia ser valido para SQL Server");

        resultado = analizador.analizar("SELECT GETDATE();", SqlDialect.SQL_SERVER);
        assertTrue(resultado.isValido(), "GETDATE deberia ser valido para SQL Server");
    }

    @Test
    void testInvalidQuerySelectFrom() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT FROM clientes;");
        assertFalse(resultado.isValido(), "SELECT FROM deberia ser invalido");
        assertTrue(resultado.hasErrores());
    }

    @Test
    void testInvalidQuerySelectNoFrom() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT nombre WHERE id = 1;");
        assertFalse(resultado.isValido(), "SELECT sin FROM deberia ser invalido");
        assertTrue(resultado.hasErrores());
    }

    @Test
    void testInvalidInsertMissingInto() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("INSERT clientes (nombre) VALUES ('Kevin');");
        assertFalse(resultado.isValido(), "INSERT sin INTO deberia ser invalido");
    }

    @Test
    void testInvalidUpdateMissingTable() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("UPDATE SET nombre = 'Kevin';");
        assertFalse(resultado.isValido(), "UPDATE sin tabla deberia ser invalido");
    }

    @Test
    void testInvalidDeleteMissingFrom() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("DELETE clientes WHERE id = 1;");
        assertFalse(resultado.isValido(), "DELETE sin FROM deberia ser invalido");
    }

    @Test
    void testUnclosedStringError() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT 'cadena sin cerrar FROM clientes;");
        assertFalse(resultado.isValido());
        assertTrue(resultado.hasErrores());
    }

    @Test
    void testUnclosedBlockCommentError() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT /* comentario sin cerrar FROM clientes;");
        assertFalse(resultado.isValido());
        assertTrue(resultado.hasErrores());
    }

    @Test
    void testUnrecognizedCharacterError() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT @ FROM clientes;");
        assertFalse(resultado.isValido());
        assertTrue(resultado.hasErrores());
    }

    @Test
    void testSelectFromNonexistentTable() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT nombre FROM;");
        assertFalse(resultado.isValido());
        assertTrue(resultado.hasErrores());
    }

    @Test
    void testCreateTableWithoutName() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("CREATE TABLE (id INT);");
        assertFalse(resultado.isValido(), "CREATE TABLE sin nombre deberia ser invalido");
    }

    @Test
    void testCTEWithoutAs() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("WITH cte SELECT * FROM clientes;");
        assertFalse(resultado.isValido(), "WITH sin AS deberia ser invalido");
    }

    @Test
    void testInventedFunctionValidation() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT FUNCION_INVENTADA(nombre) FROM clientes;");
        assertFalse(resultado.isValido(), "Funcion inventada no es una funcion valida -> error");
        boolean hasFuncError = resultado.getErrores().stream()
            .anyMatch(e -> e.getCodigo().equals("E007"));
        assertTrue(hasFuncError, "Debe haber un error sintactico por funcion no reconocida");
    }

    @Test
    void testResultadoLexerStructure() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT * FROM clientes;");
        assertNotNull(resultado.isValido());
        assertNotNull(resultado.getMensaje());
        assertNotNull(resultado.getTokens());
        assertNotNull(resultado.getErrores());
        assertNotNull(resultado.getDialectoDetectado());
        assertNotNull(resultado.getDialectosCompatibles());
    }

    @Test
    void testTokenStructure() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT id FROM clientes;");
        Token token = resultado.getTokens().get(0);
        assertNotNull(token.getTipo());
        assertNotNull(token.getLexema());
        assertTrue(token.getLinea() > 0);
        assertTrue(token.getColumna() > 0);
        assertNotNull(token.getDialecto());
    }

    @Test
    void testDialectDetectionMySQL() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT DATE_FORMAT(fecha, '%Y-%m-%d') FROM ventas;");
        assertEquals(SqlDialect.MYSQL, resultado.getDialectoDetectado());
    }

    @Test
    void testDialectDetectionPostgreSQL() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT TO_CHAR(fecha, 'YYYY-MM-DD') FROM ventas;");
        assertEquals(SqlDialect.POSTGRESQL, resultado.getDialectoDetectado());
    }

    @Test
    void testDialectDetectionSqlServer() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT GETDATE();");
        assertEquals(SqlDialect.SQL_SERVER, resultado.getDialectoDetectado());
    }

    @Test
    void testEmptyQuery() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("");
        assertFalse(resultado.isValido());
        assertEquals("La consulta SQL no puede estar vacia", resultado.getMensaje());
    }

    @Test
    void testNullQuery() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar(null);
        assertFalse(resultado.isValido());
        assertEquals("La consulta SQL no puede estar vacia", resultado.getMensaje());
    }

    @Test
    void testComentarioLinea() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT * FROM clientes; -- listar todos");
        assertTrue(resultado.isValido());
    }

    @Test
    void testComentarioBloque() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT * /* comentario */ FROM clientes;");
        assertTrue(resultado.isValido());
    }

    @Test
    void testNowFunction() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT NOW();");
        assertTrue(resultado.isValido(), "NOW deberia ser valido");
    }

    @Test
    void testGroupByAndOrderByCombo() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT departamento, COUNT(*) FROM empleados GROUP BY departamento HAVING COUNT(*) > 1 ORDER BY departamento;");
        assertTrue(resultado.isValido());
    }

    @Test
    void testUnionAll() {
        AnalizadorSql analizador = new AnalizadorSql();
        ResultadoLexer resultado = analizador.analizar("SELECT nombre FROM clientes UNION ALL SELECT nombre FROM proveedores;");
        assertTrue(resultado.isValido(), "UNION ALL deberia ser valido");
    }
}
