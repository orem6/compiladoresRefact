package com.umg.api.compiler.dto;

import com.umg.model.dialect.SqlDialect;
import java.util.List;

public class CompilerAnalyzeResponse {

    private String requestId;
    private SqlDialect dialect;
    private AnalysisMode analysisMode;
    private boolean valid;
    private String message;
    private ExecutionStatus executionStatus;
    private CompilerSummaryDto summary;
    private Object connectionResult;
    private LexicalResultDto lexicalResult;
    private SyntaxResultDto syntaxResult;
    private Object semanticResult;
    private List<CompilerErrorDto> errors;
    private List<String> console;

    public CompilerAnalyzeResponse() {
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

    public Object getSemanticResult() {
        return semanticResult;
    }

    public void setSemanticResult(Object semanticResult) {
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
