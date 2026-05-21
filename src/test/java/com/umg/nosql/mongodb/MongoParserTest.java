package com.umg.nosql.mongodb;

import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;
import com.umg.model.nosql.mongodb.MongoLexer;
import com.umg.model.nosql.mongodb.MongoParser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MongoParserTest {

    private NoSqlAnalysisResult parse(String input) {
        MongoLexer lexer = new MongoLexer(input);
        List<NoSqlToken> tokens = lexer.tokenize();
        MongoParser parser = new MongoParser();
        return parser.parse(tokens);
    }

    @Test
    void testValidFind() {
        NoSqlAnalysisResult result = parse("db.users.find({})");
        assertTrue(result.isSyntaxValid(), "find({}) deberia ser valido");
        assertTrue(result.getSyntaxErrors().isEmpty());
        assertEquals("MONGODB_FIND", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("FIND"));
    }

    @Test
    void testValidFindOne() {
        NoSqlAnalysisResult result = parse("db.users.findOne({ status: \"active\" })");
        assertTrue(result.isSyntaxValid());
        assertEquals("MONGODB_FIND_ONE", result.getStatementType());
    }

    @Test
    void testValidInsertOne() {
        NoSqlAnalysisResult result = parse("db.users.insertOne({ name: \"Kevin\" })");
        assertTrue(result.isSyntaxValid());
        assertEquals("MONGODB_INSERT_ONE", result.getStatementType());
    }

    @Test
    void testValidInsertMany() {
        NoSqlAnalysisResult result = parse("db.users.insertMany([{ name: \"Kevin\" }, { name: \"Ana\" }])");
        assertTrue(result.isSyntaxValid());
        assertEquals("MONGODB_INSERT_MANY", result.getStatementType());
    }

    @Test
    void testValidUpdateOne() {
        NoSqlAnalysisResult result = parse("db.users.updateOne({ _id: 1 }, { $set: { name: \"Kevin\" } })");
        assertTrue(result.isSyntaxValid());
        assertEquals("MONGODB_UPDATE_ONE", result.getStatementType());
    }

    @Test
    void testValidDeleteOne() {
        NoSqlAnalysisResult result = parse("db.users.deleteOne({ _id: 1 })");
        assertTrue(result.isSyntaxValid());
        assertEquals("MONGODB_DELETE_ONE", result.getStatementType());
    }

    @Test
    void testValidAggregate() {
        NoSqlAnalysisResult result = parse("db.orders.aggregate([ { $match: { status: \"completed\" } } ])");
        assertTrue(result.isSyntaxValid());
        assertEquals("MONGODB_AGGREGATE", result.getStatementType());
    }

    @Test
    void testMissingDbPrefix() {
        NoSqlAnalysisResult result = parse("users.find({})");
        assertFalse(result.isSyntaxValid());
        assertTrue(result.getSyntaxErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_EXPECTED_DB_PREFIX")));
    }

    @Test
    void testMissingCollection() {
        NoSqlAnalysisResult result = parse("db.find({})");
        assertFalse(result.isSyntaxValid());
    }

    @Test
    void testUnsupportedMethod() {
        NoSqlAnalysisResult result = parse("db.users.watch({})");
        assertFalse(result.isSyntaxValid());
        assertTrue(result.getSyntaxErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_UNSUPPORTED_METHOD")));
    }

    @Test
    void testMissingArgumentForFind() {
        NoSqlAnalysisResult result = parse("db.users.find()");
        assertFalse(result.isSyntaxValid());
        assertTrue(result.getSyntaxErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_INVALID_ARGUMENT_COUNT")));
    }

    @Test
    void testMissingSecondArgumentForUpdateOne() {
        NoSqlAnalysisResult result = parse("db.users.updateOne({ _id: 1 })");
        assertFalse(result.isSyntaxValid());
        assertTrue(result.getSyntaxErrors().stream().anyMatch(e -> e.getCode().equals("MONGO_INVALID_ARGUMENT_COUNT")));
    }

    @Test
    void testUnbalancedBraces() {
        NoSqlAnalysisResult result = parse("db.users.find({)");
        assertFalse(result.isSyntaxValid());
    }

    @Test
    void testEmptyInput() {
        NoSqlAnalysisResult result = parse("");
        assertFalse(result.isSyntaxValid());
    }
}
