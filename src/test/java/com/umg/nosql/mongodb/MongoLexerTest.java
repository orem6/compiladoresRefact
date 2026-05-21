package com.umg.nosql.mongodb;

import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;
import com.umg.model.nosql.mongodb.MongoLexer;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MongoLexerTest {

    @Test
    void testTokenizeFind() {
        MongoLexer lexer = new MongoLexer("db.users.find({})");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty(), "No deberia tener errores");
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("db", tokens.get(0).getLexeme());
        assertEquals(NoSqlTokenType.DOT, tokens.get(1).getType());
        assertEquals(NoSqlTokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals("users", tokens.get(2).getLexeme());
        assertEquals(NoSqlTokenType.DOT, tokens.get(3).getType());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(4).getType());
        assertEquals("find", tokens.get(4).getLexeme());
        assertEquals(NoSqlTokenType.LEFT_PAREN, tokens.get(5).getType());
        assertEquals(NoSqlTokenType.LEFT_BRACE, tokens.get(6).getType());
        assertEquals(NoSqlTokenType.RIGHT_BRACE, tokens.get(7).getType());
        assertEquals(NoSqlTokenType.RIGHT_PAREN, tokens.get(8).getType());
    }

    @Test
    void testTokenizeFindWithFilter() {
        MongoLexer lexer = new MongoLexer("db.users.find({ \"status\": \"active\" })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.LEFT_BRACE, tokens.get(6).getType());
        assertEquals(NoSqlTokenType.STRING, tokens.get(7).getType());
        assertEquals("\"status\"", tokens.get(7).getLexeme());
        assertEquals(NoSqlTokenType.COLON, tokens.get(8).getType());
        assertEquals(NoSqlTokenType.STRING, tokens.get(9).getType());
        assertEquals("\"active\"", tokens.get(9).getLexeme());
        assertEquals(NoSqlTokenType.RIGHT_BRACE, tokens.get(10).getType());
    }

    @Test
    void testTokenizeWithMongoOperator() {
        MongoLexer lexer = new MongoLexer("db.users.find({ age: { $gte: 18 } })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.MONGO_OPERATOR && "$gte".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeInsertOne() {
        MongoLexer lexer = new MongoLexer("db.users.insertOne({ name: \"Kevin\" })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.KEYWORD, tokens.get(4).getType());
        assertEquals("insertOne", tokens.get(4).getLexeme());
    }

    @Test
    void testTokenizeBooleanAndNull() {
        MongoLexer lexer = new MongoLexer("db.test.find({ active: true, deleted: null })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.BOOLEAN && "true".equals(t.getLexeme())));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.NULL && "null".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeArray() {
        MongoLexer lexer = new MongoLexer("db.test.find({ tags: [\"a\", \"b\"] })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.LEFT_BRACKET, tokens.get(9).getType());
        assertEquals(NoSqlTokenType.STRING, tokens.get(10).getType());
        assertEquals(NoSqlTokenType.COMMA, tokens.get(11).getType());
        assertEquals(NoSqlTokenType.STRING, tokens.get(12).getType());
        assertEquals(NoSqlTokenType.RIGHT_BRACKET, tokens.get(13).getType());
    }

    @Test
    void testTokenizeNumber() {
        MongoLexer lexer = new MongoLexer("db.test.find({ count: 42 })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.NUMBER && "42".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeNegativeNumber() {
        MongoLexer lexer = new MongoLexer("db.test.find({ temp: -10 })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.NUMBER && "-10".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeDecimalNumber() {
        MongoLexer lexer = new MongoLexer("db.test.find({ pi: 3.14 })");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.NUMBER && "3.14".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeLineComment() {
        MongoLexer lexer = new MongoLexer("db.test.find({}) // find all");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.COMMENT));
    }

    @Test
    void testTokenizeBlockComment() {
        MongoLexer lexer = new MongoLexer("db.test.find({}) /* comment */");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.COMMENT));
    }

    @Test
    void testUnclosedStringError() {
        MongoLexer lexer = new MongoLexer("db.test.find({ name: \"unclosed })");
        lexer.tokenize();
        assertFalse(lexer.getErrors().isEmpty());
        assertTrue(lexer.getErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_UNCLOSED_STRING")));
    }

    @Test
    void testUnclosedBlockCommentError() {
        MongoLexer lexer = new MongoLexer("db.test.find({}) /* unclosed");
        lexer.tokenize();
        assertFalse(lexer.getErrors().isEmpty());
        assertTrue(lexer.getErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_UNCLOSED_COMMENT")));
    }

    @Test
    void testUnsupportedMongoOperator() {
        MongoLexer lexer = new MongoLexer("db.test.find({ $unsupported: 1 })");
        lexer.tokenize();
        assertFalse(lexer.getErrors().isEmpty());
        assertTrue(lexer.getErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_UNSUPPORTED_OPERATOR")));
    }

    @Test
    void testInvalidCharacter() {
        MongoLexer lexer = new MongoLexer("db.test.find({ @: 1 })");
        lexer.tokenize();
        assertFalse(lexer.getErrors().isEmpty());
        assertTrue(lexer.getErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_INVALID_CHARACTER")));
    }

    @Test
    void testTokenizeAggregate() {
        MongoLexer lexer = new MongoLexer("db.orders.aggregate([ { $match: { status: \"completed\" } } ])");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.KEYWORD && "aggregate".equals(t.getLexeme())));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == NoSqlTokenType.MONGO_OPERATOR && "$match".equals(t.getLexeme())));
    }

    @Test
    void testTokenizeSemicolon() {
        MongoLexer lexer = new MongoLexer("db.test.find({});");
        List<NoSqlToken> tokens = lexer.tokenize();
        assertTrue(lexer.getErrors().isEmpty());
        assertEquals(NoSqlTokenType.SEMICOLON, tokens.get(tokens.size() - 2).getType());
        assertEquals(NoSqlTokenType.EOF, tokens.get(tokens.size() - 1).getType());
    }
}
