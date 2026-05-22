package com.umg.application.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.api.compiler.mapper.CompilerResponseMapper;
import com.umg.model.dialect.CompilerDialect;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.error.CompilerError;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.*;
import com.umg.model.parser.Parser;
import com.umg.model.semantic.AnalizadorSemanticoSql;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.result.ResultadoSemantico;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LexicalSyntaxAnalysisService {

    private final CompilerResponseMapper mapper;
    private final CompilerConsoleBuilder console;

    public LexicalSyntaxAnalysisService(CompilerResponseMapper mapper, CompilerConsoleBuilder console) {
        this.mapper = mapper;
        this.console = console;
    }

    public CompilerAnalyzeResponse analyze(CompilerAnalyzeRequest request) {
        console.reset();

        CompilerDialect compilerDialect = request.getDialect();
        SqlDialect dialect = toSqlDialect(compilerDialect);
        String sql = request.getSql().trim();
        AnalysisMode mode = request.getAnalysisMode();
        CompilerOptionsRequest options = request.getOptions() != null ? request.getOptions() : new CompilerOptionsRequest();

        console.info("Iniciando analisis.");
        console.info("Dialecto seleccionado: " + dialect.name());
        console.info("Modo de analisis: " + mode.name());

        if (sql.isBlank()) {
            CompilerAnalyzeResponse resp = buildErrorResponse(request, ExecutionStatus.INVALID_REQUEST);
            resp.setMessage("SQL vacio o solo espacios en blanco.");
            console.error("SQL vacio o solo espacios en blanco.");
            console.failed("Solicitud invalida.");
            resp.setConsole(console.build());
            return resp;
        }

        if (mode == AnalysisMode.SEMANTIC_ONLY || mode == AnalysisMode.FULL) {
            return analyzeSemantic(request, dialect, sql, mode, options);
        }

        return analyzeLexicalSyntax(request, dialect, sql, mode, options);
    }

    private static SqlDialect toSqlDialect(CompilerDialect dialect) {
        return switch (dialect) {
            case MYSQL -> SqlDialect.MYSQL;
            case POSTGRESQL -> SqlDialect.POSTGRESQL;
            case SQL_SERVER -> SqlDialect.SQL_SERVER;
            default -> throw new IllegalArgumentException("Dialecto no SQL: " + dialect);
        };
    }

    private static CompilerDialect toCompilerDialect(SqlDialect dialect) {
        return switch (dialect) {
            case MYSQL -> CompilerDialect.MYSQL;
            case POSTGRESQL -> CompilerDialect.POSTGRESQL;
            case SQL_SERVER -> CompilerDialect.SQL_SERVER;
            default -> throw new IllegalArgumentException("SqlDialect desconocido: " + dialect);
        };
    }

    private CompilerAnalyzeResponse analyzeSemantic(CompilerAnalyzeRequest request, SqlDialect dialect,
                                                     String sql, AnalysisMode mode, CompilerOptionsRequest options) {
        ConexionBaseDatosConfig dbConfig = mapper.toConexionConfig(request.getConnectionConfig());

        if (dbConfig == null || !dbConfig.esValida()) {
            CompilerAnalyzeResponse resp = buildErrorResponse(request, ExecutionStatus.INVALID_REQUEST);
            resp.setMessage("Se requiere configuracion de base de datos para el modo " + mode.name() + ".");
            console.error("Configuracion de base de datos no proporcionada o invalida.");
            console.failed("Solicitud invalida.");
            resp.setConsole(console.build());
            return resp;
        }

        console.info("Configuracion de base de datos valida.");
        console.info("Iniciando analisis semantico...");

        AnalizadorSemanticoSql semantico = new AnalizadorSemanticoSql();
        ResultadoSemantico resultadoSemantico = semantico.analizar(sql, dbConfig);

        boolean lexicoValido = resultadoSemantico.getResultadoLexer() != null
            && resultadoSemantico.getResultadoLexer().isValido();
        boolean semanticoValido = resultadoSemantico.isValido();

        List<ErrorLexico> lexicalErrors = new ArrayList<>();
        List<ErrorLexico> syntaxErrors = new ArrayList<>();
        if (resultadoSemantico.getResultadoLexer() != null) {
            for (ErrorLexico err : resultadoSemantico.getResultadoLexer().getErrores()) {
                if (err.getCodigo().equals("E001") || err.getCodigo().equals("E000")) {
                    lexicalErrors.add(err);
                } else {
                    syntaxErrors.add(err);
                }
            }
        }

        List<CompilerErrorDto> lexicalErrorDtos = mapper.toCompilerErrorDtoList(lexicalErrors, "LEXICAL", "ERROR");
        List<CompilerErrorDto> syntaxErrorDtos = mapper.toCompilerErrorDtoList(syntaxErrors, "SYNTAX", "ERROR");
        SemanticResultDto semanticResult = mapper.toSemanticResultDto(resultadoSemantico);

        LexicalResultDto lexicalResult = null;
        SyntaxResultDto syntaxResult = null;

        if (mode == AnalysisMode.FULL && resultadoSemantico.getResultadoLexer() != null) {
            List<Token> tokens = resultadoSemantico.getResultadoLexer().getTokens();
            List<Token> filteredTokens = new ArrayList<>();
            for (Token t : tokens) {
                if (!options.isReturnTokenList()) continue;
                if (!options.isIncludeCommentsAsTokens() &&
                    (t.getType() == TokenType.COMENTARIO_LINEA || t.getType() == TokenType.COMENTARIO_BLOQUE)) {
                    continue;
                }
                filteredTokens.add(t);
            }

            lexicalResult = new LexicalResultDto();
            lexicalResult.setValid(lexicoValido);
            lexicalResult.setMessage(lexicoValido ? "Analisis lexico finalizado correctamente." : "Se detectaron errores lexicos.");
            lexicalResult.setTokens(options.isReturnTokenList() ? mapper.toTokenDtoList(filteredTokens) : new ArrayList<>());
            lexicalResult.setErrors(lexicalErrorDtos);

            syntaxResult = new SyntaxResultDto();
            syntaxResult.setValid(!syntaxErrors.isEmpty() ? false : lexicoValido);
            syntaxResult.setMessage(syntaxErrors.isEmpty() ? "La estructura de la sentencia SQL es correcta." : "La estructura de la sentencia SQL no es valida.");
            syntaxResult.setStatementType(detectStatementType(filteredTokens));
            syntaxResult.setDetectedClauses(detectClauses(filteredTokens));
            syntaxResult.setErrors(syntaxErrorDtos);
        }

        List<CompilerErrorDto> allErrors = new ArrayList<>();
        allErrors.addAll(lexicalErrorDtos);
        allErrors.addAll(syntaxErrorDtos);
        if (semanticResult != null && semanticResult.getErrors() != null) {
            allErrors.addAll(semanticResult.getErrors());
        }

        ExecutionStatus status;
        String message;
        boolean valid;

        if (!lexicoValido) {
            status = ExecutionStatus.LEXICAL_ERROR;
            message = "La sentencia contiene errores lexicos.";
            valid = false;
        } else if (!syntaxErrors.isEmpty()) {
            status = ExecutionStatus.SYNTAX_ERROR;
            message = "La sentencia contiene errores sintacticos.";
            valid = false;
        } else if (!semanticoValido) {
            status = ExecutionStatus.SEMANTIC_ERROR;
            message = resultadoSemantico.getMensaje();
            valid = false;
        } else {
            status = ExecutionStatus.SUCCESS;
            message = "La sentencia SQL es valida a nivel lexico, sintactico y semantico.";
            valid = true;
        }

        console.info("Analisis lexico " + (lexicoValido ? "finalizado sin errores." : "finalizado con errores."));
        if (!syntaxErrors.isEmpty()) {
            console.addErrors(syntaxErrorDtos);
        }
        if (!semanticoValido) {
            console.info("Analisis semantico finalizado con errores.");
            if (semanticResult != null && semanticResult.getErrors() != null) {
                for (CompilerErrorDto err : semanticResult.getErrors()) {
                    console.error(err.getMessage() + " en linea " + err.getLine() + ", columna " + err.getColumn());
                }
            }
        } else if (lexicoValido && syntaxErrors.isEmpty()) {
            console.success("Analisis semantico finalizado sin errores.");
        }

        if (valid) {
            console.success("Sentencia valida a nivel lexico, sintactico y semantico.");
        } else {
            console.failed("Sentencia invalida.");
        }

        int warningCount = semanticResult != null && semanticResult.getWarnings() != null
            ? semanticResult.getWarnings().size() : 0;

        CompilerSummaryDto summary = new CompilerSummaryDto();
        summary.setTokenCount(lexicalResult != null ? lexicalResult.getTokens().size() : 0);
        summary.setLexicalErrorCount(lexicalErrorDtos.size());
        summary.setSyntaxErrorCount(syntaxErrorDtos.size());
        summary.setSemanticErrorCount(semanticResult != null && semanticResult.getErrors() != null
            ? semanticResult.getErrors().size() : 0);
        summary.setWarningCount(warningCount);
        summary.setAnalyzedAt(LocalDateTime.now());

        CompilerAnalyzeResponse response = new CompilerAnalyzeResponse();
        response.setRequestId(request.getRequestId());
        response.setDialect(toCompilerDialect(dialect));
        response.setAnalysisMode(mode);
        response.setValid(valid);
        response.setMessage(message);
        response.setExecutionStatus(status);
        response.setSummary(summary);
        response.setConnectionResult(null);
        response.setLexicalResult(lexicalResult);
        response.setSyntaxResult(syntaxResult);
        response.setSemanticResult(semanticResult);
        response.setErrors(allErrors.isEmpty() ? null : allErrors);
        response.setConsole(console.build());

        return response;
    }

    private CompilerAnalyzeResponse analyzeLexicalSyntax(CompilerAnalyzeRequest request, SqlDialect dialect,
                                                          String sql, AnalysisMode mode, CompilerOptionsRequest options) {
        List<ErrorLexico> lexicalErrors = new ArrayList<>();
        List<ErrorLexico> syntaxErrors = new ArrayList<>();

        ErrorCollector ec = new ErrorCollector();
        Lexer lexerOnly = new Lexer(ec);
        List<Token> lexerTokens = lexerOnly.tokenize(sql);

        for (CompilerError ce : ec.getErrors()) {
            if ("LEXICAL".equalsIgnoreCase(ce.getType())) {
                lexicalErrors.add(new ErrorLexico("E001", ce.getMessage(), ce.getLine(), ce.getColumn(), ""));
            }
        }

        if (mode == AnalysisMode.LEXICAL_SYNTAX) {
            boolean shouldStopLexical = !lexicalErrors.isEmpty() && options.isStopOnLexicalError();
            if (!shouldStopLexical) {
                ErrorCollector parserEc = new ErrorCollector();
                Parser parser = new Parser(parserEc);
                parser.parse(lexerTokens);
                for (CompilerError ce : parserEc.getErrors()) {
                    if ("SYNTAX".equalsIgnoreCase(ce.getType())) {
                        syntaxErrors.add(new ErrorLexico("E007", ce.getMessage(), ce.getLine(), ce.getColumn(), ""));
                    }
                }
            }
        }

        List<Token> filteredTokens = new ArrayList<>();
        for (Token t : lexerTokens) {
            if (!options.isReturnTokenList()) continue;
            if (!options.isIncludeCommentsAsTokens() &&
                (t.getType() == TokenType.COMENTARIO_LINEA || t.getType() == TokenType.COMENTARIO_BLOQUE)) {
                continue;
            }
            filteredTokens.add(t);
        }

        boolean hasLexicalErrors = !lexicalErrors.isEmpty();
        boolean hasSyntaxErrors = !syntaxErrors.isEmpty();
        List<CompilerErrorDto> lexicalErrorDtos = mapper.toCompilerErrorDtoList(lexicalErrors, "LEXICAL", "ERROR");
        List<CompilerErrorDto> syntaxErrorDtos = mapper.toCompilerErrorDtoList(syntaxErrors, "SYNTAX", "ERROR");

        LexicalResultDto lexicalResult = new LexicalResultDto();
        lexicalResult.setValid(!hasLexicalErrors);
        lexicalResult.setMessage(hasLexicalErrors ? "Se detectaron errores lexicos." : "Analisis lexico finalizado correctamente.");
        lexicalResult.setTokens(options.isReturnTokenList() ? mapper.toTokenDtoList(filteredTokens) : new ArrayList<>());
        lexicalResult.setErrors(lexicalErrorDtos);

        SyntaxResultDto syntaxResult = null;
        List<CompilerErrorDto> allErrors = new ArrayList<>();
        allErrors.addAll(lexicalErrorDtos);
        allErrors.addAll(syntaxErrorDtos);

        if (mode == AnalysisMode.LEXICAL_SYNTAX) {
            if (hasLexicalErrors && options.isStopOnLexicalError()) {
                console.info("Analisis lexico finalizado con errores.");
                console.failed("Analisis detenido por error lexico.");
                CompilerAnalyzeResponse errResp = buildLexicalErrorResponse(request, lexicalResult, allErrors);
                errResp.setSyntaxResult(null);
                errResp.setConsole(console.build());
                return errResp;
            }

            syntaxResult = new SyntaxResultDto();
            syntaxResult.setValid(!hasSyntaxErrors);
            syntaxResult.setMessage(hasSyntaxErrors ? "La estructura de la sentencia SQL no es valida." : "La estructura de la sentencia SQL es correcta.");
            syntaxResult.setStatementType(detectStatementType(filteredTokens));
            syntaxResult.setDetectedClauses(detectClauses(filteredTokens));
            syntaxResult.setErrors(syntaxErrorDtos);

            if (hasSyntaxErrors) {
                console.info("Analisis lexico finalizado sin errores.");
                console.addErrors(syntaxErrorDtos);
                console.failed("Sentencia invalida a nivel sintactico.");
            } else {
                console.info("Analisis lexico finalizado sin errores.");
                console.info("Analisis sintactico finalizado sin errores.");
                console.success("Sentencia valida a nivel lexico y sintactico.");
            }
        } else {
            if (hasLexicalErrors) {
                console.info("Analisis lexico finalizado con errores.");
                console.failed("Analisis detenido por error lexico.");
                CompilerAnalyzeResponse errResp = buildLexicalErrorResponse(request, lexicalResult, allErrors);
                errResp.setSyntaxResult(null);
                errResp.setConsole(console.build());
                return errResp;
            }
            console.info("Analisis lexico finalizado sin errores.");
        }

        CompilerAnalyzeResponse response = new CompilerAnalyzeResponse();
        response.setRequestId(request.getRequestId());
        response.setDialect(toCompilerDialect(dialect));
        response.setAnalysisMode(mode);
        response.setValid(!hasLexicalErrors && (syntaxResult == null || syntaxResult.isValid()));
        if (hasLexicalErrors) {
            response.setExecutionStatus(ExecutionStatus.LEXICAL_ERROR);
            response.setMessage("La sentencia contiene errores lexicos.");
        } else if (hasSyntaxErrors) {
            response.setExecutionStatus(ExecutionStatus.SYNTAX_ERROR);
            response.setMessage("La sentencia contiene errores sintacticos.");
        } else {
            response.setExecutionStatus(ExecutionStatus.SUCCESS);
            response.setMessage("La sentencia SQL es valida a nivel lexico y sintactico.");
        }

        CompilerSummaryDto summary = new CompilerSummaryDto();
        summary.setTokenCount(filteredTokens.size());
        summary.setLexicalErrorCount(lexicalErrorDtos.size());
        summary.setSyntaxErrorCount(syntaxResult != null ? syntaxResult.getErrors().size() : 0);
        summary.setSemanticErrorCount(0);
        summary.setWarningCount(0);
        summary.setAnalyzedAt(LocalDateTime.now());

        response.setSummary(summary);
        response.setConnectionResult(null);
        response.setLexicalResult(lexicalResult);
        response.setSyntaxResult(syntaxResult);
        response.setSemanticResult(null);
        response.setErrors(allErrors.isEmpty() ? null : allErrors);
        response.setConsole(console.build());

        return response;
    }



    private CompilerAnalyzeResponse buildErrorResponse(CompilerAnalyzeRequest request, ExecutionStatus status) {
        CompilerAnalyzeResponse resp = new CompilerAnalyzeResponse();
        resp.setRequestId(request.getRequestId());
        resp.setDialect(request.getDialect());
        resp.setAnalysisMode(request.getAnalysisMode());
        resp.setValid(false);
        resp.setExecutionStatus(status);
        resp.setConnectionResult(null);
        resp.setSemanticResult(null);
        return resp;
    }

    private CompilerAnalyzeResponse buildLexicalErrorResponse(CompilerAnalyzeRequest request, LexicalResultDto lexicalResult, List<CompilerErrorDto> allErrors) {
        CompilerAnalyzeResponse resp = buildErrorResponse(request, ExecutionStatus.LEXICAL_ERROR);
        resp.setMessage("La sentencia contiene errores lexicos.");

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

    private String detectStatementType(List<Token> tokens) {
        for (Token t : tokens) {
            String lex = t.getLexema().toUpperCase();
            if (t.getType() == TokenType.PALABRA_RESERVADA || t.getType() == TokenType.KEYWORD) {
                if (lex.equals("SELECT") || lex.equals("INSERT") || lex.equals("UPDATE") ||
                    lex.equals("DELETE") || lex.equals("CREATE") || lex.equals("ALTER") ||
                    lex.equals("DROP") || lex.equals("TRUNCATE") || lex.equals("WITH")) {
                    return lex;
                }
            }
        }
        return "UNKNOWN";
    }

    private List<String> detectClauses(List<Token> tokens) {
        List<String> clauses = new ArrayList<>();
        for (Token t : tokens) {
            String lex = t.getLexema().toUpperCase();
            if (t.getType() == TokenType.PALABRA_RESERVADA || t.getType() == TokenType.KEYWORD) {
                if (lex.equals("SELECT") || lex.equals("FROM") || lex.equals("WHERE") ||
                    lex.equals("INSERT") || lex.equals("INTO") || lex.equals("VALUES") ||
                    lex.equals("UPDATE") || lex.equals("SET") || lex.equals("DELETE") ||
                    lex.equals("CREATE") || lex.equals("TABLE") || lex.equals("ALTER") ||
                    lex.equals("DROP") || lex.equals("TRUNCATE") ||
                    lex.equals("GROUP") || lex.equals("ORDER") || lex.equals("HAVING") ||
                    lex.equals("LIMIT") || lex.equals("OFFSET") || lex.equals("JOIN") ||
                    lex.equals("INNER") || lex.equals("LEFT") || lex.equals("RIGHT") ||
                    lex.equals("FULL") || lex.equals("CROSS") || lex.equals("UNION") ||
                    lex.equals("ALL") || lex.equals("WITH") || lex.equals("ON")) {
                    if (!clauses.contains(lex)) {
                        clauses.add(lex);
                    }
                }
            }
        }
        return clauses;
    }
}
