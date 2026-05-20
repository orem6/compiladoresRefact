package com.umg.api.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error detectado durante una fase del compilador.")
public class CompilerErrorDto {

    @Schema(description = "Fase donde ocurrio el error.", example = "SYNTAX")
    private String stage;

    @Schema(description = "Codigo interno del error.", example = "EXPECTED_IDENTIFIER")
    private String code;

    @Schema(description = "Mensaje legible del error.", example = "Se esperaba un identificador despues de SELECT.")
    private String message;

    @Schema(description = "Linea donde ocurrio el error, si esta disponible.", example = "1")
    private Integer line;

    @Schema(description = "Columna donde ocurrio el error, si esta disponible.", example = "8")
    private Integer column;

    @Schema(description = "Lexema relacionado con el error.", example = "FROM")
    private String lexeme;

    @Schema(description = "Severidad del error.", example = "ERROR")
    private String severity;

    public CompilerErrorDto() {
    }

    public CompilerErrorDto(String stage, String code, String message, Integer line, Integer column, String lexeme, String severity) {
        this.stage = stage;
        this.code = code;
        this.message = message;
        this.line = line;
        this.column = column;
        this.lexeme = lexeme;
        this.severity = severity;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
