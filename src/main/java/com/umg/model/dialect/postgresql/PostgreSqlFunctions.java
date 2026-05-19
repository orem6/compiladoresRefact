package com.umg.model.dialect.postgresql;

import com.umg.model.dialect.FunctionRegistry;

public class PostgreSqlFunctions {
    public static boolean isFunction(String word) {
        return FunctionRegistry.isCommonFunction(word)
            || FunctionRegistry.isPostgreSqlFunction(word);
    }
}
