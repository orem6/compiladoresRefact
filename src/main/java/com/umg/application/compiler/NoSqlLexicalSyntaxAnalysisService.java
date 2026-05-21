package com.umg.application.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.api.compiler.mapper.CompilerResponseMapper;
import com.umg.model.nosql.cassandra.CassandraCqlLexicalSyntaxAnalyzer;
import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlSyntaxError;
import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;
import com.umg.model.nosql.mongodb.MongoLexicalSyntaxAnalyzer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoSqlLexicalSyntaxAnalysisService {

    private final CompilerResponseMapper mapper;
    private final CompilerConsoleBuilder console;

    public NoSqlLexicalSyntaxAnalysisService(CompilerResponseMapper mapper, CompilerConsoleBuilder console) {
        this.mapper = mapper;
        this.console = console;
    }

    public CompilerAnalyzeResponse analyze(CompilerAnalyzeRequest request) {
        console.reset();
        console.info("Iniciando analisis NoSQL.");
        console.info("Dialecto seleccionado: " + request.getDialect().name());
        console.info("Modo de analisis: " + request.getAnalysisMode().name());

        String input = request.getSql().trim();
        if (input.isBlank()) {
            return buildNoSqlErrorResponse(request, ExecutionStatus.INVALID_REQUEST,
                "SQL vacio o solo espacios en blanco.", "Campo sql vacio.");
        }

        NoSqlAnalysisResult result;
        String dialectName = request.getDialect().name();

        switch (request.getDialect()) {
            case MONGODB -> {
                console.info("Analizador MongoDB seleccionado.");
                MongoLexicalSyntaxAnalyzer mongoAnalyzer = new MongoLexicalSyntaxAnalyzer();
                result = mongoAnalyzer.analyze(input);
            }
            case CASSANDRA_CQL -> {
                console.info("Analizador Cassandra CQL seleccionado.");
                CassandraCqlLexicalSyntaxAnalyzer cqlAnalyzer = new CassandraCqlLexicalSyntaxAnalyzer();
                result = cqlAnalyzer.analyze(input);
            }
            default -> {
                return buildNoSqlErrorResponse(request, ExecutionStatus.UNSUPPORTED_DIALECT,
                    "Dialecto NoSQL no soportado: " + request.getDialect(), "Dialecto invalido.");
            }
        }

        List<CompilerErrorDto> lexicalErrorDtos = mapLexicalErrors(result.getLexicalErrors(), dialectName);
        List<CompilerErrorDto> syntaxErrorDtos = mapSyntaxErrors(result.getSyntaxErrors(), dialectName);
        List<CompilerErrorDto> allErrors = new ArrayList<>();
        allErrors.addAll(lexicalErrorDtos);
        allErrors.addAll(syntaxErrorDtos);

        boolean hasLexicalErrors = !result.getLexicalErrors().isEmpty();
        boolean hasSyntaxErrors = !result.getSyntaxErrors().isEmpty();

        CompilerOptionsRequest options = request.getOptions() != null ? request.getOptions() : new CompilerOptionsRequest();

        LexicalResultDto lexicalResult = new LexicalResultDto();
        lexicalResult.setValid(result.isLexicalValid());
        lexicalResult.setMessage(hasLexicalErrors
            ? "Se detectaron errores lexicos."
            : "Analisis lexico " + dialectName + " finalizado correctamente.");
        lexicalResult.setTokens(options.isReturnTokenList() ? mapTokens(result.getTokens(), dialectName) : new ArrayList<>());
        lexicalResult.setErrors(lexicalErrorDtos);

        SyntaxResultDto syntaxResult = null;
        if (request.getAnalysisMode() == AnalysisMode.LEXICAL_SYNTAX) {
            if (hasLexicalErrors && options.isStopOnLexicalError()) {
                console.info("Analisis lexico finalizado con errores.");
                console.failed("Analisis detenido por error lexico.");
                return buildLexicalErrorResponse(request, lexicalResult, allErrors);
            }

            syntaxResult = new SyntaxResultDto();
            syntaxResult.setValid(result.isSyntaxValid());
            syntaxResult.setMessage(hasSyntaxErrors
                ? "La estructura de la instruccion " + dialectName + " no es valida."
                : "La estructura de la instruccion " + dialectName + " es correcta.");
            syntaxResult.setStatementType(result.getStatementType());
            syntaxResult.setDetectedClauses(result.getDetectedClauses().isEmpty() ? null : result.getDetectedClauses());
            syntaxResult.setErrors(syntaxErrorDtos);

            if (hasLexicalErrors) {
                console.info("Analisis lexico finalizado con errores.");
                console.addErrors(lexicalErrorDtos);
            } else if (hasSyntaxErrors) {
                console.info("Analisis lexico finalizado sin errores.");
                console.addErrors(syntaxErrorDtos);
                console.failed("Instruccion invalida a nivel sintactico.");
            } else {
                console.info("Analisis lexico finalizado sin errores.");
                console.info("Analisis sintactico finalizado sin errores.");
                console.success("Instruccion " + dialectName + " valida a nivel lexico y sintactico.");
            }
        } else {
            if (hasLexicalErrors) {
                console.info("Analisis lexico finalizado con errores.");
                console.failed("Analisis detenido por error lexico.");
                return buildLexicalErrorResponse(request, lexicalResult, allErrors);
            }
            console.info("Analisis lexico finalizado sin errores.");
        }

        boolean isValid = !hasLexicalErrors && (syntaxResult == null || syntaxResult.isValid());

        CompilerAnalyzeResponse response = new CompilerAnalyzeResponse();
        response.setRequestId(request.getRequestId());
        response.setDialect(request.getDialect());
        response.setAnalysisMode(request.getAnalysisMode());
        response.setValid(isValid);
        response.setSemanticResult(null);
        response.setConnectionResult(null);

        if (hasLexicalErrors) {
            response.setExecutionStatus(ExecutionStatus.LEXICAL_ERROR);
            response.setMessage("La instruccion contiene errores lexicos.");
        } else if (hasSyntaxErrors) {
            response.setExecutionStatus(ExecutionStatus.SYNTAX_ERROR);
            response.setMessage("La instruccion contiene errores sintacticos.");
        } else {
            response.setExecutionStatus(ExecutionStatus.SUCCESS);
            response.setMessage("La instruccion " + dialectName + " es valida a nivel lexico y sintactico.");
        }

        CompilerSummaryDto summary = new CompilerSummaryDto();
        summary.setTokenCount(lexicalResult.getTokens().size());
        summary.setLexicalErrorCount(lexicalErrorDtos.size());
        summary.setSyntaxErrorCount(syntaxResult != null ? syntaxResult.getErrors().size() : 0);
        summary.setSemanticErrorCount(0);
        summary.setWarningCount(0);
        summary.setAnalyzedAt(LocalDateTime.now());

        response.setSummary(summary);
        response.setLexicalResult(lexicalResult);
        response.setSyntaxResult(syntaxResult);
        response.setErrors(allErrors.isEmpty() ? null : allErrors);
        response.setConsole(console.build());

        return response;
    }

    private List<TokenDto> mapTokens(List<NoSqlToken> tokens, String dialect) {
        List<TokenDto> result = new ArrayList<>();
        for (NoSqlToken t : tokens) {
            result.add(new TokenDto(t.getType().name(), t.getLexeme(), t.getLine(), t.getColumn(), dialect));
        }
        return result;
    }

    private List<CompilerErrorDto> mapLexicalErrors(List<NoSqlSyntaxError> errors, String dialect) {
        List<CompilerErrorDto> result = new ArrayList<>();
        for (NoSqlSyntaxError e : errors) {
            result.add(new CompilerErrorDto("LEXICAL", e.getCode(), e.getMessage(),
                e.getLine(), e.getColumn(), e.getLexeme(), "ERROR"));
        }
        return result;
    }

    private List<CompilerErrorDto> mapSyntaxErrors(List<NoSqlSyntaxError> errors, String dialect) {
        List<CompilerErrorDto> result = new ArrayList<>();
        for (NoSqlSyntaxError e : errors) {
            result.add(new CompilerErrorDto("SYNTAX", e.getCode(), e.getMessage(),
                e.getLine(), e.getColumn(), e.getLexeme(), "ERROR"));
        }
        return result;
    }

    private CompilerAnalyzeResponse buildNoSqlErrorResponse(CompilerAnalyzeRequest request, ExecutionStatus status,
                                                             String message, String consoleMsg) {
        console.error(consoleMsg);
        console.failed("Solicitud invalida.");
        CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
        resp.setRequestId(request.getRequestId());
        resp.setDialect(request.getDialect());
        resp.setAnalysisMode(request.getAnalysisMode());
        resp.setValid(false);
        resp.setExecutionStatus(status);
        resp.setMessage(message);
        resp.setConnectionResult(null);
        resp.setSemanticResult(null);
        resp.setLexicalResult(null);
        resp.setSyntaxResult(null);
        resp.setConsole(console.build());
        return resp;
    }

    private CompilerAnalyzeResponse buildLexicalErrorResponse(CompilerAnalyzeRequest request, LexicalResultDto lexicalResult,
                                                               List<CompilerErrorDto> allErrors) {
        CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
        resp.setRequestId(request.getRequestId());
        resp.setDialect(request.getDialect());
        resp.setAnalysisMode(request.getAnalysisMode());
        resp.setValid(false);
        resp.setExecutionStatus(ExecutionStatus.LEXICAL_ERROR);
        resp.setMessage("La instruccion contiene errores lexicos.");
        resp.setSemanticResult(null);
        resp.setConnectionResult(null);

        CompilerSummaryDto summary = new CompilerSummaryDto();
        summary.setTokenCount(lexicalResult.getTokens().size());
        summary.setLexicalErrorCount(lexicalResult.getErrors().size());
        summary.setSyntaxErrorCount(0);
        summary.setSemanticErrorCount(0);
        summary.setWarningCount(0);
        summary.setAnalyzedAt(LocalDateTime.now());

        resp.setSummary(summary);
        resp.setLexicalResult(lexicalResult);
        resp.setSyntaxResult(null);
        resp.setErrors(allErrors.isEmpty() ? null : allErrors);
        resp.setConsole(console.build());
        return resp;
    }
}
