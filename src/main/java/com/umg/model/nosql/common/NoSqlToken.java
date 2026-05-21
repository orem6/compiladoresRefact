package com.umg.model.nosql.common;

public class NoSqlToken {

    private final NoSqlTokenType type;
    private final String lexeme;
    private final int line;
    private final int column;

    public NoSqlToken(NoSqlTokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public NoSqlTokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
}
