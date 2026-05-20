package com.umg.application.compiler;

import com.umg.api.compiler.dto.CompilerErrorDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompilerConsoleBuilder {

    private final List<String> lines;

    public CompilerConsoleBuilder() {
        this.lines = new ArrayList<>();
    }

    public void reset() {
        lines.clear();
    }

    public void info(String message) {
        lines.add("[INFO] " + message);
    }

    public void success(String message) {
        lines.add("[SUCCESS] " + message);
    }

    public void error(String message) {
        lines.add("[ERROR] " + message);
    }

    public void failed(String message) {
        lines.add("[FAILED] " + message);
    }

    public void addErrors(List<CompilerErrorDto> errors) {
        for (CompilerErrorDto err : errors) {
            error(err.getMessage() + " en linea " + err.getLine() + ", columna " + err.getColumn());
        }
    }

    public List<String> build() {
        return new ArrayList<>(lines);
    }
}
