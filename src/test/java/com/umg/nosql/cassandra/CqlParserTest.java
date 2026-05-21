package com.umg.nosql.cassandra;

import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.cassandra.CqlLexer;
import com.umg.model.nosql.cassandra.CqlParser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CqlParserTest {

    private NoSqlAnalysisResult parse(String input) {
        CqlLexer lexer = new CqlLexer(input);
        List<NoSqlToken> tokens = lexer.tokenize();
        CqlParser parser = new CqlParser();
        return parser.parse(tokens);
    }

    @Test
    void testValidSelect() {
        NoSqlAnalysisResult result = parse("SELECT * FROM users;");
        assertTrue(result.isSyntaxValid(), "SELECT deberia ser valido");
        assertEquals("CASSANDRA_SELECT", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("SELECT"));
        assertTrue(result.getDetectedClauses().contains("FROM"));
    }

    @Test
    void testValidSelectWithWhere() {
        NoSqlAnalysisResult result = parse("SELECT name, age FROM users WHERE age > 18;");
        assertTrue(result.isSyntaxValid());
        assertEquals("CASSANDRA_SELECT", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("WHERE"));
    }

    @Test
    void testValidSelectWithAllowFiltering() {
        NoSqlAnalysisResult result = parse("SELECT * FROM users WHERE age > 18 ALLOW FILTERING;");
        assertTrue(result.isSyntaxValid());
        assertTrue(result.getDetectedClauses().contains("ALLOW_FILTERING"));
    }

    @Test
    void testValidSelectWithOrderBy() {
        NoSqlAnalysisResult result = parse("SELECT * FROM users ORDER BY name ASC;");
        assertTrue(result.isSyntaxValid());
        assertTrue(result.getDetectedClauses().contains("ORDER_BY"));
    }

    @Test
    void testValidSelectWithLimit() {
        NoSqlAnalysisResult result = parse("SELECT * FROM users LIMIT 10;");
        assertTrue(result.isSyntaxValid());
        assertTrue(result.getDetectedClauses().contains("LIMIT"));
    }

    @Test
    void testValidInsert() {
        NoSqlAnalysisResult result = parse("INSERT INTO users (id, name) VALUES (1, 'Kevin');");
        assertTrue(result.isSyntaxValid(), "INSERT deberia ser valido");
        assertEquals("CASSANDRA_INSERT", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("INSERT"));
        assertTrue(result.getDetectedClauses().contains("INTO"));
        assertTrue(result.getDetectedClauses().contains("VALUES"));
    }

    @Test
    void testValidUpdate() {
        NoSqlAnalysisResult result = parse("UPDATE users SET name = 'Kevin' WHERE id = 1;");
        assertTrue(result.isSyntaxValid(), "UPDATE deberia ser valido");
        assertEquals("CASSANDRA_UPDATE", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("SET"));
        assertTrue(result.getDetectedClauses().contains("WHERE"));
    }

    @Test
    void testValidDelete() {
        NoSqlAnalysisResult result = parse("DELETE FROM users WHERE id = 1;");
        assertTrue(result.isSyntaxValid(), "DELETE deberia ser valido");
        assertEquals("CASSANDRA_DELETE", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("DELETE"));
        assertTrue(result.getDetectedClauses().contains("FROM"));
    }

    @Test
    void testValidCreateKeyspace() {
        NoSqlAnalysisResult result = parse("CREATE KEYSPACE mykeyspace WITH replication = {'class': 'SimpleStrategy'};");
        assertTrue(result.isSyntaxValid());
        assertEquals("CASSANDRA_CREATE_KEYSPACE", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("CREATE_KEYSPACE"));
    }

    @Test
    void testValidCreateTable() {
        NoSqlAnalysisResult result = parse("CREATE TABLE users (id INT PRIMARY KEY, name TEXT);");
        assertTrue(result.isSyntaxValid(), "CREATE TABLE deberia ser valido");
        assertEquals("CASSANDRA_CREATE_TABLE", result.getStatementType());
        assertTrue(result.getDetectedClauses().contains("CREATE_TABLE"));
    }

    @Test
    void testValidCreateTableWithIfNotExists() {
        NoSqlAnalysisResult result = parse("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY);");
        assertTrue(result.isSyntaxValid());
        assertTrue(result.getDetectedClauses().contains("IF_NOT_EXISTS"));
    }

    @Test
    void testValidAlterTableAdd() {
        NoSqlAnalysisResult result = parse("ALTER TABLE users ADD email TEXT;");
        assertTrue(result.isSyntaxValid());
        assertEquals("CASSANDRA_ALTER_TABLE", result.getStatementType());
    }

    @Test
    void testValidDropTable() {
        NoSqlAnalysisResult result = parse("DROP TABLE users;");
        assertTrue(result.isSyntaxValid(), "DROP TABLE deberia ser valido");
        assertEquals("CASSANDRA_DROP_TABLE", result.getStatementType());
    }

    @Test
    void testValidDropKeyspace() {
        NoSqlAnalysisResult result = parse("DROP KEYSPACE mykeyspace;");
        assertTrue(result.isSyntaxValid());
        assertEquals("CASSANDRA_DROP_KEYSPACE", result.getStatementType());
    }

    @Test
    void testValidTruncate() {
        NoSqlAnalysisResult result = parse("TRUNCATE users;");
        assertTrue(result.isSyntaxValid(), "TRUNCATE deberia ser valido");
        assertEquals("CASSANDRA_TRUNCATE", result.getStatementType());
    }

    @Test
    void testValidUse() {
        NoSqlAnalysisResult result = parse("USE mykeyspace;");
        assertTrue(result.isSyntaxValid(), "USE deberia ser valido");
        assertEquals("CASSANDRA_USE", result.getStatementType());
    }

    @Test
    void testUnsupportedStatement() {
        NoSqlAnalysisResult result = parse("GRANT ALL ON keyspace TO user;");
        assertFalse(result.isSyntaxValid());
        assertTrue(result.getSyntaxErrors().stream().anyMatch(e -> e.getCode().equals("CQL_UNSUPPORTED_STATEMENT")));
    }

    @Test
    void testEmptyInput() {
        NoSqlAnalysisResult result = parse("");
        assertFalse(result.isSyntaxValid());
    }
}
