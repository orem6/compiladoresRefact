package com.umg.model.dialect.mysql;

import com.umg.model.dialect.FunctionRegistry;
import java.util.Set;

public class MySqlFunctions {
    public static boolean isFunction(String word) {
        return FunctionRegistry.isCommonFunction(word)
            || FunctionRegistry.isMySqlFunction(word);
    }
}
