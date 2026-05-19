package com.umg.lexer;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    void testTokenizeSelectSimple() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes;");
        assertFalse(ec.hasErrors());
        assertTrue(tokens.size() > 0);
        assertEquals(TokenType.PALABRA_RESERVADA, tokens.get(0).getType());
        assertEquals("SELECT", tokens.get(0).getLexema());
        assertEquals(TokenType.ASTERISCO, tokens.get(1).getType());
        assertEquals(TokenType.PALABRA_RESERVADA, tokens.get(2).getType());
        assertEquals("FROM", tokens.get(2).getLexema());
        assertEquals(TokenType.IDENTIFICADOR, tokens.get(3).getType());
        assertEquals(TokenType.PUNTO_Y_COMA, tokens.get(4).getType());
        assertEquals(TokenType.EOF, tokens.get(5).getType());
    }

    @Test
    void testTokenizeSelectWithWhere() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT id, nombre FROM clientes WHERE estado = 1;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.PALABRA_RESERVADA, tokens.get(0).getType());
        assertEquals(TokenType.IDENTIFICADOR, tokens.get(1).getType());
        assertEquals("id", tokens.get(1).getLexema());
        assertEquals(TokenType.COMA, tokens.get(2).getType());
    }

    @Test
    void testTokenizeInsertStatement() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("INSERT INTO clientes (nombre, edad) VALUES ('Kevin', 25);");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.PALABRA_RESERVADA, tokens.get(0).getType());
        assertEquals("INSERT", tokens.get(0).getLexema());
        assertEquals(TokenType.CADENA, tokens.get(10).getType());
        assertEquals("'Kevin'", tokens.get(10).getLexema());
        assertEquals(TokenType.NUMERO_ENTERO, tokens.get(12).getType());
    }

    @Test
    void testTokenizeStringLiteral() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 'hola mundo';");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.CADENA, tokens.get(1).getType());
        assertEquals("'hola mundo'", tokens.get(1).getLexema());
    }

    @Test
    void testUnclosedStringLiteral() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT 'cadena sin cerrar FROM clientes;");
        assertTrue(ec.hasErrors());
    }

    @Test
    void testLineComment() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes; -- esto es un comentario");
        assertFalse(ec.hasErrors());
        boolean foundComment = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.COMENTARIO_LINEA) {
                foundComment = true;
                break;
            }
        }
        assertTrue(foundComment);
    }

    @Test
    void testBlockComment() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * /* comentario */ FROM clientes;");
        assertFalse(ec.hasErrors());
        boolean foundComment = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.COMENTARIO_BLOQUE) {
                foundComment = true;
                break;
            }
        }
        assertTrue(foundComment);
    }

    @Test
    void testUnclosedBlockComment() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT /* comentario sin cerrar FROM clientes;");
        assertTrue(ec.hasErrors());
    }

    @Test
    void testMySqlDelimitedIdentifier() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT `nombre_cliente` FROM `clientes`;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.IDENTIFICADOR_DELIMITADO, tokens.get(1).getType());
        assertEquals(SqlDialect.MYSQL, tokens.get(1).getDialecto());
        assertEquals(SqlDialect.MYSQL, lexer.getDetectedDialect());
    }

    @Test
    void testSqlServerDelimitedIdentifier() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT [nombre cliente] FROM [dbo].[clientes];");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.IDENTIFICADOR_DELIMITADO, tokens.get(1).getType());
        assertEquals(SqlDialect.SQL_SERVER, tokens.get(1).getDialecto());
        assertEquals(SqlDialect.SQL_SERVER, lexer.getDetectedDialect());
    }

    @Test
    void testPostgreSqlDelimitedIdentifier() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT \"nombre_cliente\" FROM \"clientes\";");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.IDENTIFICADOR_DELIMITADO, tokens.get(1).getType());
        assertEquals(SqlDialect.POSTGRESQL, tokens.get(1).getDialecto());
        assertEquals(SqlDialect.POSTGRESQL, lexer.getDetectedDialect());
    }

    @Test
    void testUnclosedBacktick() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT `nombre FROM clientes;");
        assertTrue(ec.hasErrors());
    }

    @Test
    void testUnclosedBracket() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT [nombre FROM clientes;");
        assertTrue(ec.hasErrors());
    }

    @Test
    void testUnclosedDoubleQuote() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT \"nombre FROM clientes;");
        assertTrue(ec.hasErrors());
    }

    @Test
    void testUnrecognizedCharacter() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT @ FROM clientes;");
        assertTrue(ec.hasErrors());
    }

    @Test
    void testTokenizeFunction() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT COUNT(*) FROM clientes;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.FUNCION, tokens.get(1).getType());
        assertEquals("COUNT", tokens.get(1).getLexema());
    }

    @Test
    void testTokenizeFunctionWithArgs() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT CONCAT(nombre, ' ', apellido) FROM clientes;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.FUNCION, tokens.get(1).getType());
        assertEquals("CONCAT", tokens.get(1).getLexema());
    }

    @Test
    void testTokenizeDecimalNumber() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 3.14 FROM dual;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.NUMERO_DECIMAL, tokens.get(1).getType());
        assertEquals("3.14", tokens.get(1).getLexema());
    }

    @Test
    void testMalformedNumber() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT 3. FROM dual;");
        assertTrue(ec.hasErrors());
    }

    @Test
    void testParameters() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE id = ?;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.PARAMETRO, tokens.get(7).getType());
    }

    @Test
    void testPlaceholders() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM clientes WHERE id = :id;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.PLACEHOLDER, tokens.get(7).getType());
    }

    @Test
    void testComparisonOperators() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t WHERE a >= 1 AND b <= 2 AND c <> 3;");
        assertFalse(ec.hasErrors());
        boolean foundGE = false, foundLE = false, foundNE = false;
        for (Token t : tokens) {
            if (">=".equals(t.getLexema())) foundGE = true;
            if ("<=".equals(t.getLexema())) foundLE = true;
            if ("<>".equals(t.getLexema())) foundNE = true;
        }
        assertTrue(foundGE);
        assertTrue(foundLE);
        assertTrue(foundNE);
    }

    @Test
    void testLogicalOperators() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t WHERE a = 1 AND b = 2 OR c = 3 NOT IN (1);");
        boolean foundAND = false, foundOR = false, foundNOT = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.OPERADOR_LOGICO) {
                if ("AND".equals(t.getLexema())) foundAND = true;
                if ("OR".equals(t.getLexema())) foundOR = true;
                if ("NOT".equals(t.getLexema())) foundNOT = true;
            }
        }
        assertTrue(foundAND);
        assertTrue(foundOR);
        assertTrue(foundNOT);
    }

    @Test
    void testNullLiteral() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT NULL;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.PALABRA_RESERVADA, tokens.get(1).getType());
        assertEquals("NULL", tokens.get(1).getLexema());
    }

    @Test
    void testDataTypeRecognition() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("CREATE TABLE t (id INT, nombre VARCHAR(100));");
        boolean foundInt = false, foundVarchar = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.TIPO_DATO) {
                if ("INT".equals(t.getLexema())) foundInt = true;
                if ("VARCHAR".equals(t.getLexema())) foundVarchar = true;
            }
        }
        assertTrue(foundInt);
        assertTrue(foundVarchar);
    }
}
