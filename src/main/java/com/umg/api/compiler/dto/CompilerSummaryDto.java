package com.umg.api.compiler.dto;

import java.time.LocalDateTime;

public class CompilerSummaryDto {

    private int tokenCount;
    private int lexicalErrorCount;
    private int syntaxErrorCount;
    private int semanticErrorCount;
    private int warningCount;
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
