package com.umg.controller;

import com.umg.model.compiler.CompilationResult;
import com.umg.model.compiler.CompilationSession;

public class SQLCompilerController {
    private final CompilationSession session;
    private boolean lastResultValid;

    public SQLCompilerController() {
        this.session = new CompilationSession();
        this.lastResultValid = false;
    }

    public String validateQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            lastResultValid = false;
            return "Error: La consulta no puede estar vacia.";
        }

        CompilationResult result = session.compile(sql);
        lastResultValid = result.isValid();

        StringBuilder output = new StringBuilder();

        if (result.isValid()) {
            output.append("=== RESULTADO DE VALIDACION ===\n");
            output.append("Estado: CONSULTA VALIDA\n");
            output.append("Mensaje: ").append(result.getMessage()).append("\n");
            output.append("Tipo de sentencia: ").append(
                result.getAst() != null ? result.getAst().getNodeType() : "N/A"
            );
        } else {
            output.append("=== RESULTADO DE VALIDACION ===\n");
            output.append("Estado: CONSULTA INVALIDA\n");
            output.append("Mensaje: ").append(result.getMessage()).append("\n\n");
            output.append("Errores encontrados (" + result.getErrors().size() + "):\n");
            result.getErrors().forEach(error ->
                output.append("  - [").append(error.getType())
                      .append("] Linea ").append(error.getLine())
                      .append(", Columna ").append(error.getColumn())
                      .append(": ").append(error.getMessage()).append("\n")
            );
        }

        return output.toString();
    }

    public boolean lastResultValid() {
        return lastResultValid;
    }

    public CompilationSession getSession() {
        return session;
    }
}
