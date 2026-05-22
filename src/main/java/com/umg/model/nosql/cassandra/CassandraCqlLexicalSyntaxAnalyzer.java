package com.umg.model.nosql.cassandra;

import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlSyntaxError;
import com.umg.model.nosql.common.NoSqlToken;

import java.util.List;

public class CassandraCqlLexicalSyntaxAnalyzer {

    public static NoSqlAnalysisResult analyze(String input) {
        CqlLexer lexer = new CqlLexer(input);
        List<NoSqlToken> tokens = lexer.tokenize();

        NoSqlAnalysisResult result = new NoSqlAnalysisResult();

        for (NoSqlSyntaxError err : lexer.getErrors()) {
            result.addSyntaxError(err);
        }

        CqlParser parser = new CqlParser();
        NoSqlAnalysisResult parseResult = parser.parse(tokens);

        result.setStatementType(parseResult.getStatementType());
        result.getDetectedClauses().addAll(parseResult.getDetectedClauses());

        for (NoSqlSyntaxError err : parseResult.getSyntaxErrors()) {
            result.addSyntaxError(err);
        }

        return result;
    }
}
