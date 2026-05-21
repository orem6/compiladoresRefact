package com.umg.model.nosql.common;

import java.util.ArrayList;
import java.util.List;

public class NoSqlAnalysisResult {

    private boolean lexicalValid;
    private boolean syntaxValid;
    private final List<NoSqlToken> tokens;
    private final List<NoSqlSyntaxError> lexicalErrors;
    private final List<NoSqlSyntaxError> syntaxErrors;
    private String statementType;
    private final List<String> detectedClauses;

    public NoSqlAnalysisResult() {
        this.tokens = new ArrayList<>();
        this.lexicalErrors = new ArrayList<>();
        this.syntaxErrors = new ArrayList<>();
        this.detectedClauses = new ArrayList<>();
        this.lexicalValid = true;
        this.syntaxValid = true;
    }

    public boolean isLexicalValid() { return lexicalValid; }
    public void setLexicalValid(boolean lexicalValid) { this.lexicalValid = lexicalValid; }

    public boolean isSyntaxValid() { return syntaxValid; }
    public void setSyntaxValid(boolean syntaxValid) { this.syntaxValid = syntaxValid; }

    public List<NoSqlToken> getTokens() { return tokens; }
    public void addToken(NoSqlToken token) { tokens.add(token); }

    public List<NoSqlSyntaxError> getLexicalErrors() { return lexicalErrors; }
    public void addLexicalError(NoSqlSyntaxError error) { lexicalErrors.add(error); this.lexicalValid = false; }

    public List<NoSqlSyntaxError> getSyntaxErrors() { return syntaxErrors; }
    public void addSyntaxError(NoSqlSyntaxError error) { syntaxErrors.add(error); this.syntaxValid = false; }

    public String getStatementType() { return statementType; }
    public void setStatementType(String statementType) { this.statementType = statementType; }

    public List<String> getDetectedClauses() { return detectedClauses; }
    public void addDetectedClause(String clause) { detectedClauses.add(clause); }
}
