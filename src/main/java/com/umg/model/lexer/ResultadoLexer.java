package com.umg.model.lexer;

import com.umg.model.dialect.SqlDialect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultadoLexer {
    private boolean valido;
    private String mensaje;
    private List<Token> tokens;
    private List<ErrorLexico> errores;
    private SqlDialect dialectoDetectado;
    private List<SqlDialect> dialectosCompatibles;
    private boolean sintaxisBasicaValida;

    public ResultadoLexer() {
        this.valido = false;
        this.mensaje = "";
        this.tokens = new ArrayList<>();
        this.errores = new ArrayList<>();
        this.dialectoDetectado = SqlDialect.COMMON;
        this.dialectosCompatibles = new ArrayList<>();
        this.sintaxisBasicaValida = false;
    }

    public ResultadoLexer(boolean valido, String mensaje, List<Token> tokens, List<ErrorLexico> errores) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.errores = errores != null ? errores : new ArrayList<>();
        this.dialectoDetectado = SqlDialect.COMMON;
        this.dialectosCompatibles = new ArrayList<>();
        this.sintaxisBasicaValida = valido;
    }

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public List<Token> getTokens() { return Collections.unmodifiableList(tokens); }
    public void setTokens(List<Token> tokens) { this.tokens = tokens != null ? tokens : new ArrayList<>(); }
    public void addToken(Token token) { this.tokens.add(token); }

    public List<ErrorLexico> getErrores() { return Collections.unmodifiableList(errores); }
    public void setErrores(List<ErrorLexico> errores) { this.errores = errores != null ? errores : new ArrayList<>(); }
    public void addError(ErrorLexico error) { this.errores.add(error); }

    public SqlDialect getDialectoDetectado() { return dialectoDetectado; }
    public void setDialectoDetectado(SqlDialect dialectoDetectado) { this.dialectoDetectado = dialectoDetectado; }

    public List<SqlDialect> getDialectosCompatibles() { return dialectosCompatibles; }
    public void setDialectosCompatibles(List<SqlDialect> dialectosCompatibles) {
        this.dialectosCompatibles = dialectosCompatibles != null ? dialectosCompatibles : new ArrayList<>();
    }

    public boolean isSintaxisBasicaValida() { return sintaxisBasicaValida; }
    public void setSintaxisBasicaValida(boolean sintaxisBasicaValida) {
        this.sintaxisBasicaValida = sintaxisBasicaValida;
    }

    public boolean hasTokens() { return !tokens.isEmpty(); }
    public boolean hasErrores() { return !errores.isEmpty(); }
}
