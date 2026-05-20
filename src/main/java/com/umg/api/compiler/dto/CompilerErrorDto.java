package com.umg.api.compiler.dto;

public class CompilerErrorDto {

    private String stage;
    private String code;
    private String message;
    private Integer line;
    private Integer column;
    private String lexeme;
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
