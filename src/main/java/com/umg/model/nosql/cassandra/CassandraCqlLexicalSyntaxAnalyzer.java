package com.umg.model.nosql.cassandra;

import com.umg.model.nosql.common.NoSqlAnalysisResult;

public class CassandraCqlLexicalSyntaxAnalyzer {

    public NoSqlAnalysisResult analyze(String input) {
        CqlLexer lexer = new CqlLexer(input);
        lexer.tokenize();

        NoSqlAnalysisResult result = new NoSqlAnalysisResult();
        result.getTokens().addAll(lexer.getTokens());
        for (var err : lexer.getErrors()) {
            if ("LEXICAL".equals(err.getStage())) {
                result.addLexicalError(err);
            }
        }

        if (!result.getLexicalErrors().isEmpty()) {
            result.setLexicalValid(false);
            result.setSyntaxValid(false);
            return result;
        }

        CqlParser parser = new CqlParser();
        NoSqlAnalysisResult parseResult = parser.parse(lexer.getTokens());
        result.setSyntaxValid(parseResult.isSyntaxValid());
        result.setStatementType(parseResult.getStatementType());
        result.getDetectedClauses().addAll(parseResult.getDetectedClauses());
        for (var err : parseResult.getSyntaxErrors()) {
            result.addSyntaxError(err);
        }

        return result;
    }
}
