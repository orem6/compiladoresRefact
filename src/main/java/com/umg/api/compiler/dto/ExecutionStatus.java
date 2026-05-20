package com.umg.api.compiler.dto;

public enum ExecutionStatus {
    SUCCESS,
    LEXICAL_ERROR,
    SYNTAX_ERROR,
    SEMANTIC_ERROR,
    CONNECTION_ERROR,
    UNSUPPORTED_DIALECT,
    INVALID_REQUEST,
    INTERNAL_ERROR
}
