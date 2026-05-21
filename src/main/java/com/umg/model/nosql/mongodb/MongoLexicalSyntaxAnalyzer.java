package com.umg.model.nosql.mongodb;

import com.umg.model.nosql.common.NoSqlAnalysisResult;

public class MongoLexicalSyntaxAnalyzer {

    public NoSqlAnalysisResult analyze(String input) {
        MongoLexer lexer = new MongoLexer(input);
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

        MongoParser parser = new MongoParser();
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
