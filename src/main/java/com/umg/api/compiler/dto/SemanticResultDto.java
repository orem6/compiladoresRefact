package com.umg.api.compiler.dto;

import java.util.List;

public class SemanticResultDto {

    private boolean valid;
    private String message;
    private List<CompilerErrorDto> errors;
    private List<String> warnings;

    public SemanticResultDto() {
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

    public List<CompilerErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<CompilerErrorDto> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
