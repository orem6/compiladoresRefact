package com.dataquery.sqlcompiler.model.compiler;

import com.dataquery.sqlcompiler.model.ast.ASTNode;
import com.dataquery.sqlcompiler.model.error.CompilerError;
import com.dataquery.sqlcompiler.model.error.ErrorCollector;
import com.dataquery.sqlcompiler.model.lexer.Lexer;
import com.dataquery.sqlcompiler.model.lexer.Token;
import com.dataquery.sqlcompiler.model.parser.Parser;
import com.dataquery.sqlcompiler.model.semantic.SemanticAnalyzer;

import java.util.List;

public class SQLCompiler {
    private final Lexer lexer;
    private final Parser parser;
    private final SemanticAnalyzer semanticAnalyzer;
    private final ErrorCollector errorCollector;

    public SQLCompiler() {
        this.errorCollector = new ErrorCollector();
        this.lexer = new Lexer(errorCollector);
        this.parser = new Parser(errorCollector);
        this.semanticAnalyzer = new SemanticAnalyzer(errorCollector);
    }

    public CompilationResult compile(String sql) {
        errorCollector.clear();

        if (sql == null || sql.trim().isEmpty()) {
            return new CompilationResult(false, "La consulta no puede estar vacia", List.of(
                new CompilerError("SYNTAX", "Consulta vacia", 0, 0)
            ), null);
        }

        List<Token> tokens = lexer.tokenize(sql);

        if (errorCollector.hasErrors()) {
            return new CompilationResult(false, "Error lexico encontrado", errorCollector.getErrors(), null);
        }

        ASTNode ast = parser.parse(tokens);

        if (errorCollector.hasErrors()) {
            return new CompilationResult(false, "Error sintactico encontrado", errorCollector.getErrors(), ast);
        }

        boolean semanticOk = semanticAnalyzer.analyze(ast);

        if (!semanticOk || errorCollector.hasErrors()) {
            return new CompilationResult(false, "Error semantico encontrado", errorCollector.getErrors(), ast);
        }

        return new CompilationResult(true, "Consulta valida", errorCollector.getErrors(), ast);
    }

    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }
}
