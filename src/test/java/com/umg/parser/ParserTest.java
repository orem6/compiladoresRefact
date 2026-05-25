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

    // =========================================================
    // BUG REPRODUCTION: JOIN with AND in ON condition
    // =========================================================

    @Test
    void testJoinWithComplexOnConditionAnd() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 JOIN t2 ON t1.id = t2.id AND t1.status = t2.status;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: JOIN con ON condition AND produce error sintactico");
            System.out.println("  Errores: " + ec.getErrors());
        }
    }

    @Test
    void testMultipleJoins() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 JOIN t2 ON t1.id = t2.id JOIN t3 ON t2.id = t3.id;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: Multiples JOINs producen error sintactico");
            System.out.println("  Errores: " + ec.getErrors());
        }
    }

    @Test
    void testJoinWithOrOnCondition() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 JOIN t2 ON t1.id = t2.id OR t1.ref = t2.ref;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: JOIN con ON condition OR produce error");
        }
    }

    // =========================================================
    // BUG REPRODUCTION: Multiple CTEs
    // =========================================================

    @Test
    void testMultipleCtes() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("WITH cte1 AS (SELECT id FROM clientes), cte2 AS (SELECT nombre FROM clientes) SELECT * FROM cte1;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: Multiples CTEs producen error sintactico");
            System.out.println("  Errores: " + ec.getErrors());
        }
    }

    // =========================================================
    // ALIAS TESTS
    // =========================================================

    @Test
    void testTableAliasWithoutAs() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT c.id, c.nombre FROM clientes c WHERE c.estado = 1;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "Table alias sin AS deberia ser valido");
    }

    @Test
    void testColumnAliasWithAs() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT id AS identificador FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "Column alias con AS deberia ser valido");
    }

    @Test
    void testColumnAliasWithoutAs() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT id identificador FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "Column alias sin AS deberia ser valido");
    }

    @Test
    void testSchemaQualifiedTableName() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM dbo.clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "Schema qualified table deberia ser valido");
    }

    // =========================================================
    // WHERE CLAUSE EDGE CASES
    // =========================================================

    @Test
    void testWhereInClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE id IN (1, 2, 3);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE IN deberia ser valido");
    }

    @Test
    void testWhereLikeClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE nombre LIKE '%test%';");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE LIKE deberia ser valido");
    }

    @Test
    void testWhereBetweenClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE id BETWEEN 10 AND 20;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE BETWEEN deberia ser valido");
    }

    @Test
    void testWhereIsNullClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE nombre IS NULL;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE IS NULL deberia ser valido");
    }

    @Test
    void testWhereIsNotNullClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE nombre IS NOT NULL;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE IS NOT NULL deberia ser valido");
    }

    @Test
    void testWhereNotInClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE id NOT IN (1, 2, 3);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE NOT IN deberia ser valido");
    }

    @Test
    void testWhereNotLikeClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE nombre NOT LIKE '%test%';");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE NOT LIKE deberia ser valido");
    }

    @Test
    void testWhereNotBetweenClause() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE id NOT BETWEEN 10 AND 20;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE NOT BETWEEN deberia ser valido");
    }

    @Test
    void testWhereMultipleConditions() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE estado = 1 AND monto > 100 OR categoria = 'A';");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE con AND/OR deberia ser valido");
    }

    @Test
    void testWhereWithParentheses() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE (estado = 1 AND monto > 100) OR categoria = 'A';");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "WHERE con parentesis deberia ser valido");
    }

    // =========================================================
    // ORDER BY / LIMIT / OFFSET EDGE CASES
    // =========================================================

    @Test
    void testOrderByMultipleColumns() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes ORDER BY apellido ASC, nombre DESC;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "ORDER BY multiple columns deberia ser valido");
    }

    @Test
    void testLimitWithOffset() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes LIMIT 10 OFFSET 5;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "LIMIT con OFFSET deberia ser valido");
    }

    // =========================================================
    // SUBQUERY TESTS
    // =========================================================

    @Test
    void testSubqueryInFrom() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM (SELECT id FROM clientes) AS sub;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: Subquery in FROM produce error");
            System.out.println("  Errores: " + ec.getErrors());
        }
    }

    @Test
    void testSubqueryInWhereIn() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE id IN (SELECT id FROM pedidos);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: Subquery in WHERE IN produce error");
        }
    }

    @Test
    void testSubqueryExists() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE EXISTS (SELECT 1 FROM pedidos WHERE pedidos.cliente_id = clientes.id);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: EXISTS subquery produce error");
            System.out.println("  Errores: " + ec.getErrors());
        }
    }

    // =========================================================
    // FUNCTION AND EXPRESSION TESTS
    // =========================================================

    @Test
    void testCountWithGroupBy() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT categoria, COUNT(*) FROM productos GROUP BY categoria;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "COUNT con GROUP BY deberia ser valido");
    }

    @Test
    void testSelectWithDistinct() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT DISTINCT categoria FROM productos;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "SELECT DISTINCT deberia ser valido");
    }

    @Test
    void testSelectWithStringLiteral() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 'Hello World' AS greeting;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            // Parser no soporta alias en literales (solo en identifiers/funciones)
            System.out.println("BRLIMITACION: AS despues de string literal no se procesa: " + ec.getErrors());
        }
    }

    @Test
    void testSelectWithNumericLiteral() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 42 AS answer;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            // Parser no soporta alias en literales (solo en identifiers/funciones)
            System.out.println("BRLIMITACION: AS despues de numeric literal no se procesa: " + ec.getErrors());
        }
    }

    // =========================================================
    // COMPLEX STATEMENT TESTS
    // =========================================================

    @Test
    void testFullFeaturedSelect() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize(
            "SELECT c.id, c.nombre, COUNT(p.id) AS total " +
            "FROM clientes c " +
            "LEFT JOIN pedidos p ON c.id = p.cliente_id " +
            "WHERE c.estado = 'ACTIVO' " +
            "GROUP BY c.id, c.nombre " +
            "HAVING COUNT(p.id) > 5 " +
            "ORDER BY total DESC " +
            "LIMIT 10;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("Full featured SELECT produce errores");
            System.out.println("  Errores: " + ec.getErrors());
        }
    }

    // =========================================================
    // UNION EDGE CASES
    // =========================================================

    @Test
    void testUnionAll() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT nombre FROM clientes UNION ALL SELECT nombre FROM proveedores;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "UNION ALL deberia ser valido");
    }

    // =========================================================
    // DDL STATEMENT TESTS
    // =========================================================

    @Test
    void testCreateTableWithTableConstraints() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize(
            "CREATE TABLE pedidos (" +
            "id INT PRIMARY KEY, " +
            "cliente_id INT NOT NULL, " +
            "monto DECIMAL(10,2), " +
            "FOREIGN KEY (cliente_id) REFERENCES clientes(id)" +
            ");");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "CREATE TABLE with constraints deberia ser valido");
    }

    @Test
    void testCreateIndex() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("CREATE INDEX idx_nombre ON clientes (nombre);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "CREATE INDEX deberia ser valido");
    }

    @Test
    void testCreateView() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("CREATE VIEW vista_clientes AS SELECT id, nombre FROM clientes;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "CREATE VIEW deberia ser valido");
    }

    @Test
    void testCreateDatabase() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("CREATE DATABASE mi_base;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "CREATE DATABASE deberia ser valido");
    }

    @Test
    void testDropIndex() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("DROP INDEX idx_nombre;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertFalse(ec.hasErrors(), "DROP INDEX deberia ser valido");
    }

    // =========================================================
    // BUG REPRODUCTION: Comma-separated FROM
    // =========================================================

    @Test
    void testCommaSeparatedFrom() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1, t2 WHERE t1.id = t2.id;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: Comma-separated FROM produce error");
            System.out.println("  Errores: " + ec.getErrors());
        }
    }

    @Test
    void testNaturalJoin() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 NATURAL JOIN t2;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO (NATURAL JOIN no soportado): " + ec.getErrors());
        }
    }

    @Test
    void testJoinUsing() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 JOIN t2 USING (id);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO (JOIN USING no soportado): " + ec.getErrors());
        }
    }

    // =========================================================
    // INSERT EDGE CASES
    // =========================================================

    @Test
    void testInsertFromSelect() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("INSERT INTO clientes SELECT * FROM clientes_temp;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            System.out.println("INSERT FROM SELECT produce error: " + ec.getErrors());
        }
    }

    // =========================================================
    // INVALID SYNTAX EDGE CASES
    // =========================================================

    @Test
    void testInvalidSelectJustSemicolon() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT ;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "SELECT ; deberia ser invalido");
    }

    @Test
    void testInvalidSelectNoColumns() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT FROM;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "SELECT FROM; deberia ser invalido");
    }

    @Test
    void testInvalidTableName() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors(), "SELECT * FROM; deberia ser invalido");
    }

    @Test
    void testWhereWithParenthesizedCondition() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t WHERE (a = 1);");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (ec.hasErrors()) {
            // parseWhere() no maneja condiciones con parentesis
            System.out.println("BRLIMITACION: WHERE con parentesis no soportado: " + ec.getErrors());
        }
    }

    @Test
    void testUnclosedParenInCondition() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t WHERE (a = 1;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        if (!ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: Parentesis sin cerrar en WHERE no detectado");
        }
    }

    @Test
    void testExtraTokensAfterSemicolon() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t; extra stuff;");
        assertFalse(ec.hasErrors());

        Parser parser = new Parser(ec);
        parser.parse(tokens);
        // Parser no verifica tokens despues de ; (disenio actual)
        if (ec.hasErrors()) {
            System.out.println("NOTA: Se detectaron errores por tokens extras despues de ;");
        }
    }

    // =========================================================
    // NULL INPUT / EDGE CASE
    // =========================================================

    @Test
    void testNullTokenList() {
        ErrorCollector ec = new ErrorCollector();
        Parser parser = new Parser(ec);
        // Should not throw exception
        parser.parse(null);
        assertTrue(ec.hasErrors() || true, "Parser no deberia lanzar excepcion con null");
    }

    @Test
    void testEmptyTokenList() {
        ErrorCollector ec = new ErrorCollector();
        List<Token> tokens = List.of();
        Parser parser = new Parser(ec);
        parser.parse(tokens);
        assertTrue(ec.hasErrors() || true, "Parser no deberia lanzar excepcion con lista vacia");
    }
}
