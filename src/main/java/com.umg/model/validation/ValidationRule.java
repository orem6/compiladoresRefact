package com.dataquery.sqlcompiler.model.validation;

public enum ValidationRule {
    NO_EMPTY_QUERY,
    NO_TRAILING_WHITESPACE_ONLY,
    MUST_END_WITH_SEMICOLON,
    VALID_KEYWORD_START,
    BALANCED_PARENS
}
