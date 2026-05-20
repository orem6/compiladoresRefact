package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Opciones de configuracion del analizador.")
public class CompilerOptionsRequest {

    @Schema(description = "Incluir comentarios como tokens en la respuesta.", example = "true")
    private Boolean includeCommentsAsTokens;

    @Schema(description = "Detener el analisis si se encuentra un error lexico.", example = "true")
    private Boolean stopOnLexicalError;

    @Schema(description = "Detener el analisis si se encuentra un error sintactico.", example = "true")
    private Boolean stopOnSyntaxError;

    @Schema(description = "Incluir la lista de tokens en la respuesta.", example = "true")
    private Boolean returnTokenList;

    @Schema(description = "Incluir la salida de consola en la respuesta.", example = "true")
    private Boolean returnConsoleOutput;

    public CompilerOptionsRequest() {
    }

    public boolean isIncludeCommentsAsTokens() {
        return includeCommentsAsTokens != null ? includeCommentsAsTokens : true;
    }

    public boolean isStopOnLexicalError() {
        return stopOnLexicalError != null ? stopOnLexicalError : true;
    }

    public boolean isStopOnSyntaxError() {
        return stopOnSyntaxError != null ? stopOnSyntaxError : true;
    }

    public boolean isReturnTokenList() {
        return returnTokenList != null ? returnTokenList : true;
    }

    public boolean isReturnConsoleOutput() {
        return returnConsoleOutput != null ? returnConsoleOutput : true;
    }

    public void setIncludeCommentsAsTokens(Boolean includeCommentsAsTokens) {
        this.includeCommentsAsTokens = includeCommentsAsTokens;
    }

    public void setStopOnLexicalError(Boolean stopOnLexicalError) {
        this.stopOnLexicalError = stopOnLexicalError;
    }

    public void setStopOnSyntaxError(Boolean stopOnSyntaxError) {
        this.stopOnSyntaxError = stopOnSyntaxError;
    }

    public void setReturnTokenList(Boolean returnTokenList) {
        this.returnTokenList = returnTokenList;
    }

    public void setReturnConsoleOutput(Boolean returnConsoleOutput) {
        this.returnConsoleOutput = returnConsoleOutput;
    }
}
