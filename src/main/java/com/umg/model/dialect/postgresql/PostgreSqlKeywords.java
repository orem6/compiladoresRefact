package com.umg.model.dialect.postgresql;

import com.umg.model.dialect.KeywordRegistry;

public class PostgreSqlKeywords {
    public static boolean isKeyword(String word) {
        return KeywordRegistry.isCommonKeyword(word)
            || KeywordRegistry.isPostgreSqlKeyword(word);
    }
}
