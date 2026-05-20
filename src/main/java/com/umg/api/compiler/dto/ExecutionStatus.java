package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado tecnico de ejecucion del analisis.")
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
