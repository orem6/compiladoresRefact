package com.umg.model.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorCollector {
    private final List<CompilerError> errors;

    public ErrorCollector() {
        this.errors = new ArrayList<>();
    }

    public void addError(CompilerError error) {
        errors.add(error);
    }

    public List<CompilerError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void clear() {
        errors.clear();
    }

    public int getErrorCount() {
        return errors.size();
    }
}
