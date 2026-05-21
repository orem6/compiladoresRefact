package com.umg.nosql.cassandra;

import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;
import com.umg.model.nosql.cassandra.CqlLexer;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CqlLexerTest {

    @Test
    void testTokenizeSelect() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM users;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("SELECT", tokens.get(0).getLexeme());
        assertEquals(NoSqlTokenType.OPERATOR, tokens.get(1).getType());
        assertEquals("*", tokens.get(1).getLexeme());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(2).getType());
        assertEquals("FROM", tokens.get(2).getLexeme());
        assertEquals(NoSqlTokenType.IDENTIFIER, tokens.get(3).getType());
        assertEquals("users", tokens.get(3).getLexeme());
        assertEquals(NoSqlTokenType.SEMICOLON, tokens.get(4).getType());
    }

    @Test
    void testTokenizeInsert() {
        CqlLexer lexer = new CqlLexer("INSERT INTO users (id, name) VALUES (1, 'Kevin');");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("INSERT", tokens.get(0).getLexeme());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(1).getType());
        assertEquals("INTO", tokens.get(1).getLexeme());
        assertEquals(NoSqlTokenType.STRING, tokens.get(12).getType());
        assertEquals("'Kevin'", tokens.get(12).getLexeme());
    }

    @Test
    void testTokenizeCreateKeyspace() {
        CqlLexer lexer = new CqlLexer("CREATE KEYSPACE mykeyspace WITH replication = {'class': 'SimpleStrategy'};");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.KEYWORD && t.getLexeme().equalsIgnoreCase("KEYSPACE")));
        assertEquals(NoSqlTokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("mykeyspace", tokens.get(2).getLexeme());
    }

    @Test
    void testTokenizeCreateTable() {
        CqlLexer lexer = new CqlLexer("CREATE TABLE users (id INT PRIMARY KEY, name TEXT);");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.KEYWORD && t.getLexeme().equalsIgnoreCase("TABLE")));
    }

    @Test
    void testTokenizeUpdate() {
        CqlLexer lexer = new CqlLexer("UPDATE users SET name = 'Kevin' WHERE id = 1;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("UPDATE", tokens.get(0).getLexeme());
        assertEquals(NoSqlTokenType.OPERATOR, tokens.get(4).getType());
        assertEquals("=", tokens.get(4).getLexeme());
    }

    @Test
    void testTokenizeDelete() {
        CqlLexer lexer = new CqlLexer("DELETE FROM users WHERE id = 1;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("DELETE", tokens.get(0).getLexeme());
    }

    @Test
    void testTokenizeUse() {
        CqlLexer lexer = new CqlLexer("USE mykeyspace;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("USE", tokens.get(0).getLexeme());
    }

    @Test
    void testTokenizeSelectWithWhere() {
        CqlLexer lexer = new CqlLexer("SELECT name, age FROM users WHERE age > 18 ALLOW FILTERING;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.COMMA));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.OPERATOR && ">".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeComparisonOperators() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM t WHERE a >= 1 AND b <= 2;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> ">=".equals(t.getLexeme())));
        assertTrue(tokens.stream().anyMatch(t -> "<=".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeLineComment() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM users; -- comment");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.COMMENT));
    }

    @Test
    void testTokenizeBlockComment() {
        CqlLexer lexer = new CqlLexer("SELECT * /* block */ FROM users;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.COMMENT));
    }

    @Test
    void testUnclosedStringError() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM users WHERE name = 'unclosed;");
        lexer.tokenize();
        assertFalse(lexer.getErrors().isEmpty());
        assertTrue(lexer.getErrors().stream().anyMatch(e -> e.getCode().equals("CQL_UNCLOSED_STRING")));
    }

    @Test
    void testUnclosedBlockCommentError() {
        CqlLexer lexer = new CqlLexer("SELECT * /* unclosed FROM users;");
        lexer.tokenize();
        assertFalse(lexer.getErrors().isEmpty());
        assertTrue(lexer.getErrors().stream().anyMatch(e -> e.getCode().equals("CQL_UNCLOSED_COMMENT")));
    }

    @Test
    void testInvalidCharacter() {
        CqlLexer lexer = new CqlLexer("SELECT @ FROM users;");
        lexer.tokenize();
        assertFalse(lexer.getErrors().isEmpty());
    }

    @Test
    void testTokenizeBoolean() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM users WHERE active = true;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.BOOLEAN));
    }

    @Test
    void testTokenizeNull() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM users WHERE deleted = null;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.KEYWORD && "null".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeOrderBy() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM users ORDER BY name ASC;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.KEYWORD && t.getLexeme().equalsIgnoreCase("ORDER")));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.KEYWORD && t.getLexeme().equalsIgnoreCase("ASC")));
    }

    @Test
    void testTokenizeLimit() {
        CqlLexer lexer = new CqlLexer("SELECT * FROM users LIMIT 10;");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.KEYWORD && t.getLexeme().equalsIgnoreCase("LIMIT")));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.NUMBER && "10".equals(t.getLexeme())));
    }
}
