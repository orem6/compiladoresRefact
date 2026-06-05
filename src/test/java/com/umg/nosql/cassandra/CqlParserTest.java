package com.umg.nosql.cassandra;

import com.umg.model.nosql.cassandra.CqlLexer;
import com.umg.model.nosql.cassandra.CqlParser;
import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlToken;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CqlParserTest {

    @Test
    void testSelectCountStarIsValid() {
        CqlLexer lexer = new CqlLexer("SELECT COUNT(*) FROM clientes;");
        List<NoSqlToken> tokens = lexer.tokenize();
        CqlParser parser = new CqlParser();
        NoSqlAnalysisResult result = parser.parse(tokens);

        assertTrue(result.isSyntaxValid(), "SELECT COUNT(*) FROM clientes deberia ser valido en CQL");
    }

    @Test
    void testSelectAggregateFunctionsIsValid() {
        CqlLexer lexer = new CqlLexer("SELECT COUNT(*), SUM(precio), AVG(precio), MAX(precio), MIN(precio) FROM clientes;");
        List<NoSqlToken> tokens = lexer.tokenize();
        CqlParser parser = new CqlParser();
        NoSqlAnalysisResult result = parser.parse(tokens);

        assertTrue(result.isSyntaxValid(), "SELECT con funciones agregadas deberia ser valido en CQL");
    }
}
