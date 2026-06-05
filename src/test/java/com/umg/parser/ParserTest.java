package com.umg.parser;

import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.Lexer;
import com.umg.model.lexer.Token;
import com.umg.model.parser.Parser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testParseSelectSimple() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT * FROM clientes deberia ser valido");
    }

    @Test
    void testParseSelectWithWhere() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT id, nombre FROM clientes WHERE id = 1;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT con WHERE deberia ser valido");
    }

    @Test
    void testParseSelectWithMultipleColumns() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT departamento, COUNT(*) FROM empleados GROUP BY departamento HAVING COUNT(*) > 1 ORDER BY departamento ASC;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT con GROUP BY, HAVING, ORDER BY deberia ser valido");
    }

    @Test
    void testParseInsert() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("INSERT INTO clientes (nombre, edad) VALUES ('Kevin', 25);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "INSERT deberia ser valido");
    }

    @Test
    void testParseInsertWithoutColumns() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("INSERT INTO clientes VALUES (1, 'Kevin');");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "INSERT sin columnas deberia ser valido");
    }

    @Test
    void testParseUpdate() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("UPDATE clientes SET estado = 1 WHERE id = 10;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "UPDATE deberia ser valido");
    }

    @Test
    void testParseDelete() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("DELETE FROM clientes WHERE id = 10;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "DELETE deberia ser valido");
    }

    @Test
    void testParseCreateTable() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("CREATE TABLE clientes (id INT PRIMARY KEY, nombre VARCHAR(100) NOT NULL);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "CREATE TABLE deberia ser valido");
    }

    @Test
    void testParseAlterTableAdd() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("ALTER TABLE clientes ADD COLUMN correo VARCHAR(100);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "ALTER TABLE ADD deberia ser valido");
    }

    @Test
    void testParseAlterTableDrop() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("ALTER TABLE clientes DROP COLUMN correo;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "ALTER TABLE DROP deberia ser valido");
    }

    @Test
    void testParseAlterTableAlter() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("ALTER TABLE clientes ALTER COLUMN nombre VARCHAR(150);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "ALTER TABLE ALTER COLUMN deberia ser valido");
    }

    @Test
    void testParseDropTable() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("DROP TABLE clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "DROP TABLE deberia ser valido");
    }

    @Test
    void testParseTruncate() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("TRUNCATE TABLE clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "TRUNCATE deberia ser valido");
    }

    @Test
    void testParseInnerJoin() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT a.id, b.nombre FROM clientes a INNER JOIN pedidos b ON a.id = b.cliente_id;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "INNER JOIN deberia ser valido");
    }

    @Test
    void testParseLeftJoin() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes LEFT JOIN pedidos ON clientes.id = pedidos.cliente_id;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "LEFT JOIN deberia ser valido");
    }

    @Test
    void testParseCrossJoin() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes CROSS JOIN pedidos;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "CROSS JOIN deberia ser valido");
    }

    @Test
    void testParseWithCTE() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("WITH cte AS (SELECT id FROM clientes) SELECT * FROM cte;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WITH CTE deberia ser valido");
    }

    @Test
    void testInvalidSelectMissingFrom() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT nombre WHERE id = 1;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "SELECT sin FROM deberia ser invalido");
    }

    @Test
    void testInvalidSelectFromWithoutTable() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT nombre FROM;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "SELECT ... FROM; deberia ser invalido");
    }

    @Test
    void testInvalidSelectEmptyColumns() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "SELECT FROM deberia ser invalido");
    }

    @Test
    void testInvalidInsertMissingInto() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("INSERT clientes (nombre) VALUES ('Kevin');");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "INSERT sin INTO deberia ser invalido");
    }

    @Test
    void testInvalidUpdateMissingTable() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("UPDATE SET nombre = 'Kevin';");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "UPDATE sin tabla deberia ser invalido");
    }

    @Test
    void testInvalidDeleteMissingFrom() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("DELETE clientes WHERE id = 1;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "DELETE sin FROM deberia ser invalido");
    }

    @Test
    void testInvalidDeleteFromWithoutTable() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("DELETE FROM WHERE id = 1;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "DELETE FROM sin tabla deberia ser invalido");
    }

    @Test
    void testInvalidCreateTableNoName() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("CREATE TABLE (id INT);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "CREATE TABLE sin nombre deberia ser invalido");
    }

    @Test
    void testInvalidCTEMissingAs() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("WITH cte SELECT * FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "WITH sin AS deberia ser invalido");
    }

    @Test
    void testParseUnion() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT nombre FROM clientes UNION SELECT nombre FROM proveedores;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "UNION deberia ser valido");
    }

    @Test
    void testParseLimit() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes LIMIT 10;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT con LIMIT deberia ser valido");
    }

    @Test
    void testParseOrderByDesc() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes ORDER BY nombre DESC;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT con ORDER BY DESC deberia ser valido");
    }

    @Test
    void testParseFullJoin() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 FULL OUTER JOIN t2 ON t1.id = t2.id;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "FULL OUTER JOIN deberia ser valido");
    }

    @Test
    void testParseRightOuterJoin() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 RIGHT OUTER JOIN t2 ON t1.id = t2.id;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "RIGHT OUTER JOIN deberia ser valido");
    }

    @Test
    void testParseSubqueryInSelectList() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT id, (SELECT MAX(id) FROM pedidos) AS max_id FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "Subquery en SELECT deberia ser valido. Errores: " + ec.getErrors());
    }

    @Test
    void testParseSubqueryInFrom() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM (SELECT id, nombre FROM clientes) AS sub;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "Subquery en FROM deberia ser valido. Errores: " + ec.getErrors());
    }

    @Test
    void testParseCaseExpression() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT CASE WHEN id = 1 THEN 'one' ELSE 'other' END FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "CASE WHEN en SELECT deberia ser valido. Errores: " + ec.getErrors());
    }

    @Test
    void testParseSelectWithTop() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT TOP 5 id, nombre FROM clientes ORDER BY nombre;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT TOP deberia ser valido. Errores: " + ec.getErrors());
    }

    @Test
    void testParseSelectWithDistinct() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT DISTINCT estado FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT DISTINCT deberia ser valido. Errores: " + ec.getErrors());
    }

    @Test
    void testInvalidWhereWithoutCondition() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "WHERE sin condicion deberia ser invalido");
    }
}
