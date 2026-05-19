package com.umg.model.dialect.mysql;

import com.umg.model.dialect.KeywordRegistry;
import java.util.Set;

public class MySqlKeywords {
    public static boolean isKeyword(String word) {
        return KeywordRegistry.isCommonKeyword(word)
            || KeywordRegistry.isMySqlKeyword(word);
    }
}
