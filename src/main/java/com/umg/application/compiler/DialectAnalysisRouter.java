package com.umg.application.compiler;

import com.umg.api.compiler.dto.CompilerAnalyzeRequest;
import com.umg.api.compiler.dto.CompilerAnalyzeResponse;
import com.umg.api.compiler.dto.ExecutionStatus;
import com.umg.model.dialect.CompilerDialect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DialectAnalysisRouter {

    private final LexicalSyntaxAnalysisService lexicalSyntaxAnalysisService;
    private final NoSqlLexicalSyntaxAnalysisService noSqlLexicalSyntaxAnalysisService;

    public DialectAnalysisRouter(LexicalSyntaxAnalysisService lexicalSyntaxAnalysisService,
                                  NoSqlLexicalSyntaxAnalysisService noSqlLexicalSyntaxAnalysisService) {
        this.lexicalSyntaxAnalysisService = lexicalSyntaxAnalysisService;
        this.noSqlLexicalSyntaxAnalysisService = noSqlLexicalSyntaxAnalysisService;
    }

    public CompilerAnalyzeResponse route(CompilerAnalyzeRequest request) {
        CompilerDialect dialect = request.getDialect();

        if (dialect == null) {
            CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
            resp.setRequestId(request.getRequestId());
            resp.setDialect(null);
            resp.setValid(false);
            resp.setMessage("Dialecto no especificado.");
            resp.setExecutionStatus(ExecutionStatus.INVALID_REQUEST);
            resp.setConsole(new ArrayList<>());
            return resp;
        }

        switch (dialect) {
            case MONGODB:
            case CASSANDRA_CQL:
                return noSqlLexicalSyntaxAnalysisService.analyze(request);
            default:
                return lexicalSyntaxAnalysisService.analyze(request);
        }
    }
}
