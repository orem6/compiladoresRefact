package com.umg.application.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.model.dialect.CompilerDialect;
import com.umg.model.nosql.cassandra.CassandraCqlLexicalSyntaxAnalyzer;
import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlSyntaxError;
import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;
import com.umg.model.nosql.mongodb.MongoLexer;
import com.umg.model.nosql.mongodb.MongoParser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoSqlLexicalSyntaxAnalysisService {

    public CompilerAnalyzeResponse analyze(CompilerAnalyzeRequest request) {
        CompilerDialect dialect = request.getDialect();
        String sql = request.getSql().trim();
        AnalysisMode mode = request.getAnalysisMode();

        if (sql.isBlank()) {
            return buildError(request, ExecutionStatus.INVALID_REQUEST, "SQL/NoSQL vacio.");
        }

        List<NoSqlToken> tokens;
        NoSqlAnalysisResult parseResult;

        if (dialect == CompilerDialect.MONGODB) {
            MongoLexer lexer = new MongoLexer(sql);
            tokens = lexer.tokenize();
            List<NoSqlSyntaxError> lexErrors = lexer.getErrors();

            MongoParser parser = new MongoParser();
            parseResult = parser.parse(tokens);

            for (NoSqlSyntaxError err : lexErrors) {
                parseResult.addSyntaxError(err);
            }
        } else if (dialect == CompilerDialect.CASSANDRA_CQL) {
            com.umg.model.nosql.cassandra.CqlLexer cqlLexer = new com.umg.model.nosql.cassandra.CqlLexer(sql);
            tokens = cqlLexer.tokenize();
            List<NoSqlSyntaxError> lexErrors = cqlLexer.getErrors();

            com.umg.model.nosql.cassandra.CqlParser cqlParser = new com.umg.model.nosql.cassandra.CqlParser();
            parseResult = cqlParser.parse(tokens);

            for (NoSqlSyntaxError err : lexErrors) {
                parseResult.addSyntaxError(err);
            }
        } else {
            return buildError(request, ExecutionStatus.UNSUPPORTED_DIALECT, "Dialecto NoSQL no soportado: " + dialect);
        }

        List<CompilerErrorDto> lexicalErrors = new ArrayList<>();
        List<CompilerErrorDto> syntaxErrors = new ArrayList<>();
        for (NoSqlSyntaxError err : parseResult.getSyntaxErrors()) {
            CompilerErrorDto dto = new CompilerErrorDto();
            dto.setCode(err.getCode());
            dto.setMessage(err.getMessage());
            dto.setLine(err.getLine());
            dto.setColumn(err.getColumn());
            dto.setLexeme(err.getLexeme());
            dto.setStage(err.getStage());
            dto.setSeverity("ERROR");
            if ("LEXICAL".equals(err.getStage())) {
                lexicalErrors.add(dto);
            } else {
                syntaxErrors.add(dto);
            }
        }

        LexicalResultDto lexicalResult = new LexicalResultDto();
        lexicalResult.setValid(lexicalErrors.isEmpty());
        lexicalResult.setMessage(lexicalErrors.isEmpty() ? "Analisis lexico finalizado correctamente." : "Se detectaron errores lexicos.");
        List<TokenDto> tokenDtos = new ArrayList<>();
        for (NoSqlToken t : tokens) {
            if (t.getType() == NoSqlTokenType.EOF) continue;
            TokenDto td = new TokenDto();
            td.setType(t.getType().name());
            td.setLexeme(t.getLexeme());
            td.setLine(t.getLine());
            td.setColumn(t.getColumn());
            tokenDtos.add(td);
        }
        lexicalResult.setTokens(tokenDtos);
        lexicalResult.setErrors(lexicalErrors.isEmpty() ? null : lexicalErrors);

        SyntaxResultDto syntaxResult = null;
        List<CompilerErrorDto> allErrors = new ArrayList<>();
        allErrors.addAll(lexicalErrors);
        allErrors.addAll(syntaxErrors);

        boolean hasLexicalErrors = !lexicalErrors.isEmpty();
        boolean hasSyntaxErrors = !syntaxErrors.isEmpty();

        if (mode == AnalysisMode.LEXICAL_SYNTAX) {
            syntaxResult = new SyntaxResultDto();
            syntaxResult.setValid(!hasSyntaxErrors && !hasLexicalErrors);
            syntaxResult.setMessage(hasSyntaxErrors ? "La estructura no es valida." : "La estructura es correcta.");
            syntaxResult.setStatementType(parseResult.getStatementType());
            syntaxResult.setDetectedClauses(new ArrayList<>(parseResult.getDetectedClauses()));
            syntaxResult.setErrors(syntaxErrors.isEmpty() ? null : syntaxErrors);
        }

        CompilerAnalyzeResponse response = new CompilerAnalyzeResponse();
        response.setRequestId(request.getRequestId());
        response.setDialect(dialect);
        response.setAnalysisMode(mode);
        response.setValid(!hasLexicalErrors && (syntaxResult == null || syntaxResult.isValid()));

        if (hasLexicalErrors) {
            response.setExecutionStatus(ExecutionStatus.LEXICAL_ERROR);
            response.setMessage("La instruccion contiene errores lexicos.");
        } else if (hasSyntaxErrors) {
            response.setExecutionStatus(ExecutionStatus.SYNTAX_ERROR);
            response.setMessage("La instruccion contiene errores sintacticos.");
        } else {
            response.setExecutionStatus(ExecutionStatus.SUCCESS);
            response.setMessage("La instruccion es valida a nivel lexico y sintactico.");
        }

        CompilerSummaryDto summary = new CompilerSummaryDto();
        summary.setTokenCount(tokenDtos.size());
        summary.setLexicalErrorCount(lexicalErrors.size());
        summary.setSyntaxErrorCount(syntaxErrors.size());
        summary.setSemanticErrorCount(0);
        summary.setWarningCount(0);
        summary.setAnalyzedAt(LocalDateTime.now());

        response.setSummary(summary);
        response.setConnectionResult(null);
        response.setLexicalResult(mode == AnalysisMode.LEXICAL_ONLY || mode == AnalysisMode.LEXICAL_SYNTAX ? lexicalResult : null);
        response.setSyntaxResult(syntaxResult);
        response.setSemanticResult(null);
        response.setErrors(allErrors.isEmpty() ? null : allErrors);
        response.setConsole(new ArrayList<>());
        return response;
    }

    private CompilerAnalyzeResponse buildError(CompilerAnalyzeRequest request, ExecutionStatus status, String message) {
        CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
        resp.setRequestId(request.getRequestId());
        resp.setDialect(request.getDialect());
        resp.setAnalysisMode(request.getAnalysisMode());
        resp.setValid(false);
        resp.setMessage(message);
        resp.setExecutionStatus(status);
        resp.setConnectionResult(null);
        resp.setLexicalResult(null);
        resp.setSyntaxResult(null);
        resp.setSemanticResult(null);
        resp.setErrors(null);
        resp.setConsole(new ArrayList<>());
        return resp;
    }
}
