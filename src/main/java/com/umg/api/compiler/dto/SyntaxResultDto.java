package com.umg.api.compiler.dto;

import java.util.List;

public class SyntaxResultDto {

    private boolean valid;
    private String message;
    private String statementType;
    private List<String> detectedClauses;
    private List<CompilerErrorDto> errors;

    public SyntaxResultDto() {
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

    public String getStatementType() {
        return statementType;
    }

    public void setStatementType(String statementType) {
        this.statementType = statementType;
    }

    public List<String> getDetectedClauses() {
        return detectedClauses;
    }

    public void setDetectedClauses(List<String> detectedClauses) {
        this.detectedClauses = detectedClauses;
    }

    public List<CompilerErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<CompilerErrorDto> errors) {
        this.errors = errors;
    }
}
