package com.umg.api.compiler.dto;

public class TokenDto {

    private String type;
    private String lexeme;
    private int line;
    private int column;
    private String dialect;

    public TokenDto() {
    }

    public TokenDto(String type, String lexeme, int line, int column, String dialect) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
        this.dialect = dialect;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
