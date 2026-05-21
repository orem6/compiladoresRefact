package com.umg.application.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.model.dialect.CompilerDialect;
import com.umg.model.dialect.DialectMapper;
import org.springframework.stereotype.Component;

@Component
public class DialectAnalysisRouter {

    private final LexicalSyntaxAnalysisService sqlAnalysisService;
    private final NoSqlLexicalSyntaxAnalysisService noSqlAnalysisService;

    public DialectAnalysisRouter(LexicalSyntaxAnalysisService sqlAnalysisService,
                                  NoSqlLexicalSyntaxAnalysisService noSqlAnalysisService) {
        this.sqlAnalysisService = sqlAnalysisService;
        this.noSqlAnalysisService = noSqlAnalysisService;
    }

    public CompilerAnalyzeResponse route(CompilerAnalyzeRequest request) {
        CompilerDialect dialect = request.getDialect();

        if (DialectMapper.isSql(dialect)) {
            return sqlAnalysisService.analyze(request);
        } else if (DialectMapper.isNoSql(dialect)) {
            AnalysisMode mode = request.getAnalysisMode();
            if (mode == AnalysisMode.FULL || mode == AnalysisMode.SEMANTIC_ONLY) {
                CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
                resp.setRequestId(request.getRequestId());
                resp.setDialect(dialect);
                resp.setAnalysisMode(mode);
                resp.setValid(false);
                resp.setExecutionStatus(ExecutionStatus.INVALID_REQUEST);
                resp.setMessage("El analisis " + mode.name() + " para dialectos NoSQL queda pendiente para la fase semantica.");
                resp.setConnectionResult(null);
                resp.setSemanticResult(null);
                resp.setLexicalResult(null);
                resp.setSyntaxResult(null);
                resp.setErrors(null);
                return resp;
            }
            return noSqlAnalysisService.analyze(request);
        } else {
            CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
            resp.setRequestId(request.getRequestId());
            resp.setDialect(dialect);
            resp.setAnalysisMode(request.getAnalysisMode());
            resp.setValid(false);
            resp.setExecutionStatus(ExecutionStatus.UNSUPPORTED_DIALECT);
            resp.setMessage("Dialecto no soportado: " + dialect);
            resp.setConnectionResult(null);
            resp.setSemanticResult(null);
            resp.setLexicalResult(null);
            resp.setSyntaxResult(null);
            resp.setErrors(null);
            return resp;
        }
    }
}
