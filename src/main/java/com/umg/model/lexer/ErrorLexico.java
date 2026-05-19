package com.umg.model.lexer;

public class ErrorLexico {
    private final String codigo;
    private final String mensaje;
    private final int linea;
    private final int columna;
    private final String lexema;

    public ErrorLexico(String codigo, String mensaje, int linea, int columna, String lexema) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
        this.lexema = lexema;
    }

    public String getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public String getLexema() { return lexema; }

    @Override
    public String toString() {
        if (lexema != null && !lexema.isEmpty()) {
            return codigo + " - " + mensaje + " en línea " + linea + ", columna " + columna + " (lexema: '" + lexema + "')";
        }
        return codigo + " - " + mensaje + " en línea " + linea + ", columna " + columna;
    }
}
