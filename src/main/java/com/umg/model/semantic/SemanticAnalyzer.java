package com.umg.model.semantic;

import com.umg.model.ast.ASTNode;
import com.umg.model.error.ErrorCollector;

public class SemanticAnalyzer {
    private final ErrorCollector errorCollector;

    public SemanticAnalyzer(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    public boolean analyze(ASTNode node) {
        if (node == null) {
            return false;
        }

        boolean valid = true;

        switch (node.getNodeType()) {
            case "SELECT" -> valid = analyzeSelect(node);
            case "INSERT" -> valid = analyzeInsert(node);
            case "UPDATE" -> valid = analyzeUpdate(node);
            case "DELETE" -> valid = analyzeDelete(node);
            case "CREATE_TABLE" -> valid = analyzeCreateTable(node);
            case "DROP_TABLE" -> valid = analyzeDropTable(node);
            default -> {
            }
        }

        return valid;
    }

    private boolean analyzeSelect(ASTNode node) {
        return true;
    }

    private boolean analyzeInsert(ASTNode node) {
        return true;
    }

    private boolean analyzeUpdate(ASTNode node) {
        return true;
    }

    private boolean analyzeDelete(ASTNode node) {
        return true;
    }

    private boolean analyzeCreateTable(ASTNode node) {
        return true;
    }

    private boolean analyzeDropTable(ASTNode node) {
        return true;
    }
}
