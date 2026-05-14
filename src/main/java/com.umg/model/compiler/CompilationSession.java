package com.dataquery.sqlcompiler.model.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompilationSession {
    private final SQLCompiler compiler;
    private final List<String> queryHistory;

    public CompilationSession() {
        this.compiler = new SQLCompiler();
        this.queryHistory = new ArrayList<>();
    }

    public CompilationResult compile(String sql) {
        CompilationResult result = compiler.compile(sql);
        if (result.isValid() || !sql.trim().isEmpty()) {
            queryHistory.add(sql);
        }
        return result;
    }

    public List<String> getQueryHistory() {
        return Collections.unmodifiableList(queryHistory);
    }

    public SQLCompiler getCompiler() {
        return compiler;
    }
}
