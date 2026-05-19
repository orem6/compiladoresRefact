package com.umg.model.dialect.sqlserver;

import com.umg.model.dialect.KeywordRegistry;

public class SqlServerKeywords {
    public static boolean isKeyword(String word) {
        return KeywordRegistry.isCommonKeyword(word)
            || KeywordRegistry.isSqlServerKeyword(word);
    }
}
