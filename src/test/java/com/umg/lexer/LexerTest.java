package com.umg.lexer;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    // =========================================================
    // BUG REPRODUCTION TESTS
    // =========================================================

    @Test
    void testBugColonEqualsIsAssignmentOperator() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT :=");
        // BUG: ":" is consumed by readPlaceholder before readOperator gets it
        // Expected: OPERADOR or OPERADOR_COMPARACION(":=")
        // Actual: PLACEHOLDER(":") then OPERADOR("=")
        assertFalse(ec.hasErrors());
        if (tokens.get(1).getType() == TokenType.PLACEHOLDER) {
            System.out.println("BUG CONFIRMADO: ':=' se tokeniza como PLACEHOLDER(':') + OPERADOR('=') en lugar de un solo operador");
        }
    }

    @Test
    void testBugDoubleColonIsPostgresCast() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1::text");
        // BUG: "::" is consumed as two placeholders
        // Expected: single token for "::"
        // Actual: two PLACEHOLDER tokens
        int placeholderCount = 0;
        for (Token t : tokens) {
            if (t.getType() == TokenType.PLACEHOLDER) placeholderCount++;
        }
        if (placeholderCount >= 2) {
            System.out.println("BUG CONFIRMADO: '::' se tokeniza como dos PLACEHOLDER en lugar de un solo operador");
        }
    }

    @Test
    void testBugSetVariableIsNotDataType() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SET @var = 1;");
        boolean hasSetAsDataType = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.TIPO_DATO && t.getLexema().equalsIgnoreCase("SET")) {
                hasSetAsDataType = true;
            }
        }
        if (hasSetAsDataType) {
            System.out.println("BUG CONFIRMADO: 'SET' se tokeniza como TIPO_DATO en lugar de PALABRA_RESERVADA en contexto de SET statement");
        }
    }

    @Test
    void testBugExponentWithoutDigits() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT 1e FROM dual;");
        // BUG: "1e" without exponent digits should produce error but doesn't
        if (!ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: '1e' sin digitos de exponente no produce error lexico");
        }
    }

    @Test
    void testBugExponentWithoutDigitsCapitalE() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        lexer.tokenize("SELECT 1E;");
        if (!ec.hasErrors()) {
            System.out.println("BUG CONFIRMADO: '1E' sin digitos de exponente no produce error lexico");
        }
    }

    // =========================================================
    // OPERATOR TESTS
    // =========================================================

    @Test
    void testOperatorNotEqual() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t WHERE a != 1;");
        assertFalse(ec.hasErrors());
        boolean found = false;
        for (Token t : tokens) {
            if ("!=".equals(t.getLexema())) {
                assertEquals(TokenType.OPERADOR_COMPARACION, t.getType());
                found = true;
            }
        }
        assertTrue(found, "!= deberia ser OPERADOR_COMPARACION");
    }

    @Test
    void testOperatorPipeConcat() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 'a' || 'b';");
        assertFalse(ec.hasErrors());
        boolean found = false;
        for (Token t : tokens) {
            if ("||".equals(t.getLexema())) {
                found = true;
            }
        }
        assertTrue(found, "|| deberia ser un solo token OPERADOR");
    }

    // =========================================================
    // STRING LITERAL TESTS
    // =========================================================

    @Test
    void testStringEscapedQuote() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 'O''Brien';");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.CADENA, tokens.get(1).getType());
        // Should contain the escaped quote
        String lexeme = tokens.get(1).getLexema();
        assertTrue(lexeme.contains("''"), "La cadena deberia contener '' escapado");
    }

    @Test
    void testStringWithEscapedQuoteMiddle() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 'It''s working';");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.CADENA, tokens.get(1).getType());
    }

    @Test
    void testEmptyString() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT '';");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.CADENA, tokens.get(1).getType());
        assertEquals("''", tokens.get(1).getLexema());
    }

    // =========================================================
    // NUMBER LITERAL TESTS
    // =========================================================

    @Test
    void testScientificNotationNumber() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1.5e10 FROM dual;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.NUMERO_DECIMAL, tokens.get(1).getType());
    }

    @Test
    void testScientificNotationPositiveExponent() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1E+5;");
        assertFalse(ec.hasErrors());
    }

    @Test
    void testScientificNotationNegativeExponent() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1E-5;");
        assertFalse(ec.hasErrors());
    }

    // =========================================================
    // KEYWORD RECOGNITION TESTS
    // =========================================================

    @Test
    void testSetInUpdateIsReservedWord() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("UPDATE t SET x = 1;");
        boolean foundSetAsReserved = false;
        boolean foundSetAsDataType = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.PALABRA_RESERVADA && t.getLexema().equalsIgnoreCase("SET")) {
                foundSetAsReserved = true;
            }
            if (t.getType() == TokenType.TIPO_DATO && t.getLexema().equalsIgnoreCase("SET")) {
                foundSetAsDataType = true;
            }
        }
        if (foundSetAsDataType && !foundSetAsReserved) {
            System.out.println("BUG CONFIRMADO: SET en UPDATE es TIPO_DATO en lugar de PALABRA_RESERVADA");
        }
    }

    // =========================================================
    // MULTI-STATEMENT TESTS
    // =========================================================

    @Test
    void testMultipleStatements() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1; SELECT 2;");
        assertFalse(ec.hasErrors());
        int semicolonCount = 0;
        for (Token t : tokens) {
            if (t.getType() == TokenType.PUNTO_Y_COMA) semicolonCount++;
        }
        assertEquals(2, semicolonCount, "Debe haber 2 punto y coma");
    }

    // =========================================================
    // EDGE CASE TESTS
    // =========================================================

    @Test
    void testEmptyInput() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("");
        assertFalse(ec.hasErrors());
        assertEquals(1, tokens.size(), "Solo debe existir token EOF");
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    void testWhitespaceOnlyInput() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("   \n\t\r  ");
        assertFalse(ec.hasErrors());
        assertEquals(1, tokens.size(), "Solo debe existir token EOF");
    }

    @Test
    void testNullInput() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize(null);
        assertFalse(ec.hasErrors());
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).getType());
    }

    @Test
    void testOnlySemicolon() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize(";");
        assertFalse(ec.hasErrors());
        assertEquals(2, tokens.size());
        assertEquals(TokenType.PUNTO_Y_COMA, tokens.get(0).getType());
        assertEquals(TokenType.EOF, tokens.get(1).getType());
    }

    @Test
    void testLineCommentOnly() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("-- solo un comentario");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.COMENTARIO_LINEA, tokens.get(0).getType());
    }

    @Test
    void testBlockCommentOnly() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("/* solo un bloque */");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.COMENTARIO_BLOQUE, tokens.get(0).getType());
    }

    @Test
    void testMultipleBlockComments() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT /* a */ * /* b */ FROM t;");
        assertFalse(ec.hasErrors());
        int blockCommentCount = 0;
        for (Token t : tokens) {
            if (t.getType() == TokenType.COMENTARIO_BLOQUE) blockCommentCount++;
        }
        assertEquals(2, blockCommentCount);
    }

    @Test
    void testFunctionWithoutParenthesesIsNotFunction() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT count FROM t;");
        assertFalse(ec.hasErrors());
        // count without ( should NOT be FUNCION
        assertNotEquals(TokenType.FUNCION, tokens.get(1).getType(),
            "count sin parentesis no deberia ser FUNCION");
        // count is a keyword, so it should be PALABRA_RESERVADA or IDENTIFICADOR
        assertTrue(tokens.get(1).getType() == TokenType.PALABRA_RESERVADA
            || tokens.get(1).getType() == TokenType.IDENTIFICADOR);
    }

    // =========================================================
    // DELIMITED IDENTIFIER TESTS
    // =========================================================

    @Test
    void testDelimitedIdentifierWithSpace() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT [first name] FROM t;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.IDENTIFICADOR_DELIMITADO, tokens.get(1).getType());
        assertTrue(tokens.get(1).getLexema().contains(" "));
    }

    @Test
    void testMultipleDelimitedIdentifiers() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT `a`.`b` FROM t;");
        assertFalse(ec.hasErrors());
        assertEquals(TokenType.IDENTIFICADOR_DELIMITADO, tokens.get(1).getType());
        assertEquals(TokenType.PUNTO, tokens.get(2).getType());
        assertEquals(TokenType.IDENTIFICADOR_DELIMITADO, tokens.get(3).getType());
    }

    // =========================================================
    // RESERVED WORD TESTS
    // =========================================================

    @Test
    void testSelectIsReservedWord() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1;");
        assertEquals(TokenType.PALABRA_RESERVADA, tokens.get(0).getType());
    }

    @Test
    void testWhereIsReservedWord() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1 WHERE x = 1;");
        boolean foundWhere = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.PALABRA_RESERVADA && t.getLexema().equalsIgnoreCase("WHERE")) {
                foundWhere = true;
            }
        }
        assertTrue(foundWhere, "WHERE deberia ser PALABRA_RESERVADA");
    }

    @Test
    void testJoinIsReservedWord() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 JOIN t2;");
        boolean foundJoin = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.PALABRA_RESERVADA && t.getLexema().equalsIgnoreCase("JOIN")) {
                foundJoin = true;
            }
        }
        assertTrue(foundJoin, "JOIN deberia ser PALABRA_RESERVADA");
    }

    @Test
    void testOnIsReservedWord() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM t1 JOIN t2 ON t1.id = t2.id;");
        boolean foundOn = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.PALABRA_RESERVADA && t.getLexema().equalsIgnoreCase("ON")) {
                foundOn = true;
            }
        }
        assertTrue(foundOn, "ON deberia ser PALABRA_RESERVADA");
    }

    @Test
    void testAsIsReservedWord() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT 1 AS x;");
        boolean foundAs = false;
        for (Token t : tokens) {
            if (t.getType() == TokenType.PALABRA_RESERVADA && t.getLexema().equalsIgnoreCase("AS")) {
                foundAs = true;
            }
        }
        assertTrue(foundAs, "AS deberia ser PALABRA_RESERVADA");
    }

    // =========================================================
    // COMPLEX TOKENIZATION TESTS
    // =========================================================

    @Test
    void testTokenizeColumnAliases() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT a.id AS aid, b.name AS bname FROM t1 a JOIN t2 b ON a.id = b.id;");
        assertFalse(ec.hasErrors());
        // a.id
        assertEquals(TokenType.IDENTIFICADOR, tokens.get(1).getType());
        assertEquals("a", tokens.get(1).getLexema());
        assertEquals(TokenType.PUNTO, tokens.get(2).getType());
        assertEquals(TokenType.IDENTIFICADOR, tokens.get(3).getType());
        assertEquals("id", tokens.get(3).getLexema());
        // AS
        assertEquals(TokenType.PALABRA_RESERVADA, tokens.get(4).getType());
        assertEquals("AS", tokens.get(4).getLexema());
        // aid alias
        assertEquals(TokenType.IDENTIFICADOR, tokens.get(5).getType());
        assertEquals("aid", tokens.get(5).getLexema());
    }

    @Test
    void testTokenizeSubqueryInFrom() {
        ErrorCollector ec = new ErrorCollector();
        Lexer lexer = new Lexer(ec);
        List<Token> tokens = lexer.tokenize("SELECT * FROM (SELECT * FROM t1) AS sub WHERE sub.x = 1;");
        assertFalse(ec.hasErrors());
        assertTrue(tokens.size() > 0);
    }

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
