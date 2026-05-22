package com.umg.model.nosql.common;

import java.util.ArrayList;
import java.util.List;

public class NoSqlAnalysisResult {
    private boolean syntaxValid;
    private String statementType;
    private final List<String> detectedClauses;
    private final List<NoSqlSyntaxError> syntaxErrors;

    public NoSqlAnalysisResult() {
        this.syntaxValid = true;
        this.statementType = "UNKNOWN";
        this.detectedClauses = new ArrayList<>();
        this.syntaxErrors = new ArrayList<>();
    }

    public boolean isSyntaxValid() { return syntaxValid; }
    public void setSyntaxValid(boolean syntaxValid) { this.syntaxValid = syntaxValid; }
    public String getStatementType() { return statementType; }
    public void setStatementType(String statementType) { this.statementType = statementType; }
    public List<String> getDetectedClauses() { return detectedClauses; }
    public List<NoSqlSyntaxError> getSyntaxErrors() { return syntaxErrors; }

    public void addDetectedClause(String clause) {
        detectedClauses.add(clause);
    }

    public void addSyntaxError(NoSqlSyntaxError error) {
        syntaxErrors.add(error);
        syntaxValid = false;
    }
}
