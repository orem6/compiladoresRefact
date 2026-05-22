package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.List;

@Schema(description = "Resultado del analisis semantico.")
public class SemanticResultDto {

    @Schema(description = "Indica si el analisis semantico fue valido.", example = "true")
    private boolean valid;

    @Schema(description = "Mensaje descriptivo del resultado semantico.", example = "Analisis semantico completado sin errores.")
    private String message;

    @Schema(description = "Errores semanticos detectados.")
    private List<CompilerErrorDto> errors;

    @Schema(description = "Advertencias semanticas.")
    private List<String> warnings;

    @Schema(description = "Objetos validados durante el analisis semantico.")
    private Map<String, Object> validatedObjects;

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

    public Map<String, Object> getValidatedObjects() {
        return validatedObjects;
    }

    public void setValidatedObjects(Map<String, Object> validatedObjects) {
        this.validatedObjects = validatedObjects;
    }
}
