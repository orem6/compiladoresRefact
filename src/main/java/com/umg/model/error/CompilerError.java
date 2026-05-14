package com.umg.model.error;

public class CompilerError {
    private final String type;
    private final String message;
    private final int line;
    private final int column;

    public CompilerError(String type, String message, int line, int column) {
        this.type = type;
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public String getType() { return type; }
    public String getMessage() { return message; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        return "Error [" + type + "] Line " + line + ", Col " + column + ": " + message;
    }
}
