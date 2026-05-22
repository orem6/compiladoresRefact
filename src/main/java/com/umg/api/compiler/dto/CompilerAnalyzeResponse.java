package com.umg.api.compiler.dto;

import com.umg.model.dialect.CompilerDialect;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Respuesta unificada del compilador SQL/NoSQL.")
public class CompilerAnalyzeResponse {

    @Schema(description = "Identificador unico del request.", example = "REQ-001")
    private String requestId;

    @Schema(
        description = "Dialecto general del compilador: SQL y NoSQL.",
        example = "MONGODB",
        allowableValues = {"MYSQL", "POSTGRESQL", "SQL_SERVER", "MONGODB", "CASSANDRA_CQL"}
    )
    private CompilerDialect dialect;

    @Schema(description = "Modo de analisis ejecutado.", example = "LEXICAL_SYNTAX")
    private AnalysisMode analysisMode;

    @Schema(description = "Indica si la sentencia fue valida segun el modo de analisis ejecutado.", example = "true")
    private boolean valid;

    @Schema(description = "Mensaje descriptivo del resultado del analisis.", example = "La sentencia SQL es valida a nivel lexico y sintactico.")
    private String message;

    @Schema(description = "Estado tecnico de ejecucion.", example = "SUCCESS")
    private ExecutionStatus executionStatus;

    @Schema(description = "Resumen de conteos del analisis.")
    private CompilerSummaryDto summary;

    @Schema(description = "Resultado de prueba de conexion, si aplica.", nullable = true)
    private Object connectionResult;

    @Schema(description = "Resultado del analisis lexico.", nullable = true)
    private LexicalResultDto lexicalResult;

    @Schema(description = "Resultado del analisis sintactico.", nullable = true)
    private SyntaxResultDto syntaxResult;

    @Schema(description = "Resultado del analisis semantico. Null cuando no aplica.", nullable = true)
    private SemanticResultDto semanticResult;

    @Schema(description = "Lista de errores detectados en todas las fases.", nullable = true)
    private List<CompilerErrorDto> errors;

    @Schema(description = "Salida de consola del analizador.", nullable = true)
    private List<String> console;

    public CompilerAnalyzeResponse() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public CompilerDialect getDialect() {
        return dialect;
    }

    public void setDialect(CompilerDialect dialect) {
        this.dialect = dialect;
    }

    public AnalysisMode getAnalysisMode() {
        return analysisMode;
    }

    public void setAnalysisMode(AnalysisMode analysisMode) {
        this.analysisMode = analysisMode;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public CompilerSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(CompilerSummaryDto summary) {
        this.summary = summary;
    }

    public Object getConnectionResult() {
        return connectionResult;
    }

    public void setConnectionResult(Object connectionResult) {
        this.connectionResult = connectionResult;
    }

    public LexicalResultDto getLexicalResult() {
        return lexicalResult;
    }

    public void setLexicalResult(LexicalResultDto lexicalResult) {
        this.lexicalResult = lexicalResult;
    }

    public SyntaxResultDto getSyntaxResult() {
        return syntaxResult;
    }

    public void setSyntaxResult(SyntaxResultDto syntaxResult) {
        this.syntaxResult = syntaxResult;
    }

    public SemanticResultDto getSemanticResult() {
        return semanticResult;
    }

    public void setSemanticResult(SemanticResultDto semanticResult) {
        this.semanticResult = semanticResult;
    }

    public List<CompilerErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<CompilerErrorDto> errors) {
        this.errors = errors;
    }

    public List<String> getConsole() {
        return console;
    }

    public void setConsole(List<String> console) {
        this.console = console;
    }
}
