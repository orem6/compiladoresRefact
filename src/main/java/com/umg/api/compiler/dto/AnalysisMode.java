package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Modo de analisis solicitado para el compilador: LEXICAL_ONLY, LEXICAL_SYNTAX, SEMANTIC_ONLY o FULL.")
public enum AnalysisMode {
    LEXICAL_ONLY,
    LEXICAL_SYNTAX,
    SEMANTIC_ONLY,
    FULL
}
