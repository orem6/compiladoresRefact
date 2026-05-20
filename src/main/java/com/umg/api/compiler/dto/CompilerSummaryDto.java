package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resumen de conteos del analisis.")
public class CompilerSummaryDto {

    @Schema(description = "Cantidad de tokens generados.", example = "10")
    private int tokenCount;

    @Schema(description = "Cantidad de errores lexicos.", example = "0")
    private int lexicalErrorCount;

    @Schema(description = "Cantidad de errores sintacticos.", example = "0")
    private int syntaxErrorCount;

    @Schema(description = "Cantidad de errores semanticos.", example = "0")
    private int semanticErrorCount;

    @Schema(description = "Cantidad de advertencias.", example = "0")
    private int warningCount;

    @Schema(description = "Fecha y hora del analisis.")
    private LocalDateTime analyzedAt;

    public CompilerSummaryDto() {
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public int getLexicalErrorCount() {
        return lexicalErrorCount;
    }

    public void setLexicalErrorCount(int lexicalErrorCount) {
        this.lexicalErrorCount = lexicalErrorCount;
    }

    public int getSyntaxErrorCount() {
        return syntaxErrorCount;
    }

    public void setSyntaxErrorCount(int syntaxErrorCount) {
        this.syntaxErrorCount = syntaxErrorCount;
    }

    public int getSemanticErrorCount() {
        return semanticErrorCount;
    }

    public void setSemanticErrorCount(int semanticErrorCount) {
        this.semanticErrorCount = semanticErrorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
