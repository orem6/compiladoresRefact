package com.dataquery.sqlcompiler.model.semantic;

import com.dataquery.sqlcompiler.model.error.ErrorCollector;

public class TypeChecker {
    private final ErrorCollector errorCollector;

    public TypeChecker(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    public boolean checkTypeCompatibility(String expected, String actual) {
        return expected.equalsIgnoreCase(actual);
    }
}
