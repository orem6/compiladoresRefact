package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultado del analisis lexico.")
public class LexicalResultDto {

    @Schema(description = "Indica si el analisis lexico fue valido.", example = "true")
    private boolean valid;

    @Schema(description = "Mensaje descriptivo del resultado lexico.", example = "Analisis lexico finalizado correctamente.")
    private String message;

    @Schema(description = "Lista de tokens generados.")
    private List<TokenDto> tokens;

    @Schema(description = "Errores lexicos detectados.")
    private List<CompilerErrorDto> errors;

    public LexicalResultDto() {
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

    public List<TokenDto> getTokens() {
        return tokens;
    }

    public void setTokens(List<TokenDto> tokens) {
        this.tokens = tokens;
    }

    public List<CompilerErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<CompilerErrorDto> errors) {
        this.errors = errors;
    }
}
