package com.umg.model.dialect.sqlserver;

import com.umg.model.dialect.FunctionRegistry;

public class SqlServerFunctions {
    public static boolean isFunction(String word) {
        return FunctionRegistry.isCommonFunction(word)
            || FunctionRegistry.isSqlServerFunction(word);
    }
}
