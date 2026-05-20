package com.umg.api.compiler.dto;

import com.umg.model.dialect.SqlDialect;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request principal para analisis SQL.")
public class CompilerAnalyzeRequest {

    @Schema(
        description = "Identificador opcional enviado por el frontend para trazabilidad.",
        example = "REQ-001"
    )
    private String requestId;

    @NotNull(message = "dialect es obligatorio")
    @Schema(
        description = "Motor SQL seleccionado.",
        example = "MYSQL",
        allowableValues = {"MYSQL", "POSTGRESQL", "SQL_SERVER"}
    )
    private SqlDialect dialect;

    @NotBlank(message = "sql no puede estar vacio")
    @Schema(
        description = "Sentencia SQL que sera analizada. El backend no ejecuta esta sentencia.",
        example = "SELECT id, nombre FROM clientes WHERE estado = 1;"
    )
    private String sql;

    @NotNull(message = "analysisMode es obligatorio")
    @Schema(
        description = "Modo de analisis solicitado.",
        example = "LEXICAL_SYNTAX",
        allowableValues = {"LEXICAL_ONLY", "LEXICAL_SYNTAX", "SEMANTIC_ONLY", "FULL"}
    )
    private AnalysisMode analysisMode;

    @Schema(description = "Opciones de ejecucion del analizador.")
    private CompilerOptionsRequest options;

    @Schema(description = "Datos de conexion requeridos solo para analisis semantico o full.")
    private ConnectionConfigDto connectionConfig;

    public CompilerAnalyzeRequest() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public AnalysisMode getAnalysisMode() {
        return analysisMode;
    }

    public void setAnalysisMode(AnalysisMode analysisMode) {
        this.analysisMode = analysisMode;
    }

    public CompilerOptionsRequest getOptions() {
        return options;
    }

    public void setOptions(CompilerOptionsRequest options) {
        this.options = options;
    }

    public ConnectionConfigDto getConnectionConfig() {
        return connectionConfig;
    }

    public void setConnectionConfig(ConnectionConfigDto connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
}
