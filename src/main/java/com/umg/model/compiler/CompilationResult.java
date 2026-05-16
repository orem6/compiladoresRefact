package com.umg.model.compiler;

import com.umg.model.ast.ASTNode;
import com.umg.model.error.CompilerError;

import java.util.List;

public class CompilationResult {
    private final boolean valid;
    private final String message;
    private final List<CompilerError> errors;
    private final ASTNode ast;

    public CompilationResult(boolean valid, String message, List<CompilerError> errors, ASTNode ast) {
        this.valid = valid;
        this.message = message;
        this.errors = errors;
        this.ast = ast;
    }

    public boolean isValid() { return valid; }
    public String getMessage() { return message; }
    public List<CompilerError> getErrors() { return errors; }
    public ASTNode getAst() { return ast; }

    public String getErrorMessage() {
        StringBuilder sb = new StringBuilder();
        for (CompilerError error : errors) {
            sb.append(error.toString()).append("\n");
        }
        return sb.toString().trim();
    }
}
