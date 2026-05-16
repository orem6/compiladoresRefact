package com.umg.model.semantic;

import com.umg.model.error.ErrorCollector;

public class TypeChecker {
    private final ErrorCollector errorCollector;

    public TypeChecker(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    public boolean checkTypeCompatibility(String expected, String actual) {
        return expected.equalsIgnoreCase(actual);
    }
}
