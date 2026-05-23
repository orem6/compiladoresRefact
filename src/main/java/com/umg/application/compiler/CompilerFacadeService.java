package com.umg.application.compiler;

import com.umg.api.compiler.dto.*;
import com.umg.api.compiler.mapper.CompilerResponseMapper;
import com.umg.model.dialect.CompilerDialect;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CompilerFacadeService {

    private final DialectAnalysisRouter analysisRouter;
    private final ConnectionValidationService connectionValidationService;
    private final NoSqlSemanticAnalysisService noSqlSemanticAnalysisService;
    private final CompilerResponseMapper mapper;

    public CompilerFacadeService(DialectAnalysisRouter analysisRouter,
                                 ConnectionValidationService connectionValidationService,
                                 NoSqlSemanticAnalysisService noSqlSemanticAnalysisService,
                                 CompilerResponseMapper mapper) {
        this.analysisRouter = analysisRouter;
        this.connectionValidationService = connectionValidationService;
        this.noSqlSemanticAnalysisService = noSqlSemanticAnalysisService;
        this.mapper = mapper;
    }

    public CompilerAnalyzeResponse analyzeFull(CompilerAnalyzeRequest request) {
        if (request.getAnalysisMode() != AnalysisMode.FULL) {
            return badRequest(request, "El endpoint /analyze/full requiere analysisMode=FULL.");
        }

        if (request.getDialect() != CompilerDialect.MONGODB && request.getDialect() != CompilerDialect.CASSANDRA_CQL) {
            return analysisRouter.route(request);
        }

        CompilerAnalyzeResponse lexicalSyntaxResponse;
        CompilerAnalyzeRequest lexicalRequest = cloneForLexicalSyntax(request);
        lexicalSyntaxResponse = analysisRouter.route(lexicalRequest);

        if (lexicalSyntaxResponse.getExecutionStatus() == ExecutionStatus.LEXICAL_ERROR
            || lexicalSyntaxResponse.getExecutionStatus() == ExecutionStatus.SYNTAX_ERROR
            || !lexicalSyntaxResponse.isValid()) {
            lexicalSyntaxResponse.setAnalysisMode(AnalysisMode.FULL);
            return lexicalSyntaxResponse;
        }

        if (request.getConnectionConfig() == null || request.getConnectionConfig().getDialect() == null) {
            return badRequest(request, "connectionConfig es obligatorio para FULL.");
        }

        com.umg.model.semantic.config.ConexionBaseDatosConfig config;
        com.umg.api.compiler.dto.ConnectionConfigDto connDto = request.getConnectionConfig();
        if (request.getDialect() == CompilerDialect.MONGODB || request.getDialect() == CompilerDialect.CASSANDRA_CQL) {
            config = new com.umg.model.semantic.config.ConexionBaseDatosConfig();
            config.setDialecto(
                request.getDialect() == CompilerDialect.CASSANDRA_CQL
                    ? com.umg.model.dialect.SqlDialect.CASSANDRA
                    : com.umg.model.dialect.SqlDialect.MONGODB
            );
            config.setHost(connDto.getHost());
            config.setPuerto(connDto.getPort() != null ? connDto.getPort() : 0);
            config.setBaseDatos(connDto.getDatabase());
            config.setEsquema(connDto.getSchema());
            config.setUsuario(connDto.getUsername());
            config.setPassword(connDto.getPassword());
            config.setUrlJdbc(connDto.getJdbcUrl());
            config.setUsarUrlJdbcDirecta(connDto.getUseDirectJdbcUrl() != null ? connDto.getUseDirectJdbcUrl() : false);
            config.setLocalDatacenter(connDto.getLocalDatacenter());
        } else {
            config = mapper.toConexionConfig(connDto, request.getDialect());
        }

        Map<String, Object> connection = connectionValidationService
            .testConnection(request.getConnectionConfig().getDialect(), config);

        boolean connected = Boolean.TRUE.equals(connection.get("connected"));
        if (!connected) {
            lexicalSyntaxResponse.setAnalysisMode(AnalysisMode.FULL);
            lexicalSyntaxResponse.setValid(false);
            lexicalSyntaxResponse.setExecutionStatus(ExecutionStatus.CONNECTION_ERROR);
            lexicalSyntaxResponse.setMessage(String.valueOf(connection.get("message")));
            lexicalSyntaxResponse.setConnectionResult(connection);
            return lexicalSyntaxResponse;
        }

        lexicalSyntaxResponse.setAnalysisMode(AnalysisMode.FULL);
        lexicalSyntaxResponse.setConnectionResult(connection);

        if (request.getDialect() == CompilerDialect.MONGODB || request.getDialect() == CompilerDialect.CASSANDRA_CQL) {
            SemanticResultDto semantic = noSqlSemanticAnalysisService.analyze(request);
            lexicalSyntaxResponse.setSemanticResult(semantic);
            boolean semanticValid = semantic != null && semantic.isValid();
            lexicalSyntaxResponse.setValid(semanticValid);
            lexicalSyntaxResponse.setExecutionStatus(semanticValid ? ExecutionStatus.SUCCESS : ExecutionStatus.SEMANTIC_ERROR);
            lexicalSyntaxResponse.setMessage(semantic != null ? semantic.getMessage() : lexicalSyntaxResponse.getMessage());
            if (lexicalSyntaxResponse.getSummary() == null) {
                CompilerSummaryDto summary = new CompilerSummaryDto();
                summary.setAnalyzedAt(LocalDateTime.now());
                lexicalSyntaxResponse.setSummary(summary);
            }
            lexicalSyntaxResponse.getSummary().setSemanticErrorCount(
                semantic != null && semantic.getErrors() != null ? semantic.getErrors().size() : 0
            );
            lexicalSyntaxResponse.getSummary().setWarningCount(
                semantic != null && semantic.getWarnings() != null ? semantic.getWarnings().size() : 0
            );
        }

        return lexicalSyntaxResponse;
    }

    private CompilerAnalyzeRequest cloneForLexicalSyntax(CompilerAnalyzeRequest request) {
        CompilerAnalyzeRequest clone = new CompilerAnalyzeRequest();
        clone.setRequestId(request.getRequestId());
        clone.setDialect(request.getDialect());
        clone.setSql(request.getSql());
        clone.setAnalysisMode(AnalysisMode.LEXICAL_SYNTAX);
        clone.setOptions(request.getOptions());
        clone.setConnectionConfig(request.getConnectionConfig());
        clone.setTargetCollection(request.getTargetCollection());
        return clone;
    }

    private CompilerAnalyzeResponse badRequest(CompilerAnalyzeRequest request, String message) {
        CompilerAnalyzeResponse response = new CompilerAnalyzeResponse();
        response.setRequestId(request.getRequestId());
        response.setDialect(request.getDialect());
        response.setAnalysisMode(request.getAnalysisMode());
        response.setValid(false);
        response.setExecutionStatus(ExecutionStatus.INVALID_REQUEST);
        response.setMessage(message);
        response.setConsole(new ArrayList<>());
        return response;
    }
}
