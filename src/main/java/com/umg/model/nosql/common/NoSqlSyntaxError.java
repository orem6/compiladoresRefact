package com.umg.model.nosql.common;

public class NoSqlSyntaxError {

    private final String code;
    private final String message;
    private final int line;
    private final int column;
    private final String lexeme;
    private final String stage;

    public NoSqlSyntaxError(String code, String message, int line, int column, String lexeme, String stage) {
        this.code = code;
        this.message = message;
        this.line = line;
        this.column = column;
        this.lexeme = lexeme;
        this.stage = stage;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getLexeme() { return lexeme; }
    public String getStage() { return stage; }
}
