package com.umg.api.compiler.dto;

import com.umg.model.dialect.SqlDialect;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CompilerAnalyzeRequest {

    private String requestId;

    @NotNull(message = "dialect es obligatorio")
    private SqlDialect dialect;

    @NotBlank(message = "sql no puede estar vacio")
    private String sql;

    @NotNull(message = "analysisMode es obligatorio")
    private AnalysisMode analysisMode;

    private CompilerOptionsRequest options;

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
}
