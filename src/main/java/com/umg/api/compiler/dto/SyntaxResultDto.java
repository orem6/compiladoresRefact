package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultado del analisis sintactico.")
public class SyntaxResultDto {

    @Schema(description = "Indica si el analisis sintactico fue valido.", example = "true")
    private boolean valid;

    @Schema(description = "Mensaje descriptivo del resultado sintactico.", example = "La estructura de la sentencia SQL es correcta.")
    private String message;

    @Schema(description = "Tipo de sentencia SQL detectada.", example = "SELECT")
    private String statementType;

    @Schema(description = "Clausulas detectadas en la sentencia.", example = "[\"SELECT\", \"FROM\", \"WHERE\"]")
    private List<String> detectedClauses;

    @Schema(description = "Errores sintacticos detectados.")
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
