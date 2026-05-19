package com.umg.model.lexer;

import com.umg.model.dialect.SqlDialect;

public class Token {
    private final TokenType tipo;
    private final String lexema;
    private final int linea;
    private final int columna;
    private final SqlDialect dialecto;

    public Token(TokenType tipo, String lexema, int linea, int columna) {
        this(tipo, lexema, linea, columna, null);
    }

    public Token(TokenType tipo, String lexema, int linea, int columna, SqlDialect dialecto) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.dialecto = dialecto;
    }

    public TokenType getTipo() { return tipo; }
    public String getLexema() { return lexema; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public SqlDialect getDialecto() { return dialecto; }

    public TokenType getType() { return tipo; }
    public String getLexeme() { return lexema; }
    public int getLine() { return linea; }
    public int getColumn() { return columna; }

    @Override
    public String toString() {
        return String.format("%-23s | %-15s | línea %d | columna %d | %s",
            tipo, lexema, linea, columna,
            dialecto != null ? dialecto.name() : "COMMON");
    }
}
