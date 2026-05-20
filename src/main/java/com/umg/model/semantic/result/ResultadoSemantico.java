package com.umg.model.semantic.result;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.lexer.ResultadoLexer;
import com.umg.model.lexer.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultadoSemantico {
    private boolean valido;
    private String mensaje;
    private ResultadoLexer resultadoLexer;
    private List<ErrorSemantico> erroresSemanticos;
    private List<String> advertencias;
    private SqlDialect dialecto;

    public ResultadoSemantico() {
        this.erroresSemanticos = new ArrayList<>();
        this.advertencias = new ArrayList<>();
    }

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public ResultadoLexer getResultadoLexer() { return resultadoLexer; }
    public void setResultadoLexer(ResultadoLexer resultadoLexer) { this.resultadoLexer = resultadoLexer; }

    public List<ErrorSemantico> getErroresSemanticos() { return Collections.unmodifiableList(erroresSemanticos); }
    public void setErroresSemanticos(List<ErrorSemantico> erroresSemanticos) {
        this.erroresSemanticos = erroresSemanticos != null ? erroresSemanticos : new ArrayList<>();
    }
    public void addErrorSemantico(ErrorSemantico error) { this.erroresSemanticos.add(error); }

    public List<String> getAdvertencias() { return Collections.unmodifiableList(advertencias); }
    public void setAdvertencias(List<String> advertencias) {
        this.advertencias = advertencias != null ? advertencias : new ArrayList<>();
    }
    public void addAdvertencia(String advertencia) { this.advertencias.add(advertencia); }

    public SqlDialect getDialecto() { return dialecto; }
    public void setDialecto(SqlDialect dialecto) { this.dialecto = dialecto; }

    public boolean tieneErroresSemanticos() { return !erroresSemanticos.isEmpty(); }

    public List<Token> getTokens() {
        if (resultadoLexer != null) return resultadoLexer.getTokens();
        return Collections.emptyList();
    }

    public String getResumenParaVista() {
        StringBuilder sb = new StringBuilder();
        sb.append("===============================================\n");
        sb.append("          RESULTADO DEL ANALISIS\n");
        sb.append("===============================================\n\n");

        if (resultadoLexer != null) {
            sb.append("Resultado lexico: ")
              .append(!resultadoLexer.hasErrores() ? "VALIDA" : "INVALIDA").append("\n");
            sb.append("Resultado sintactico: ")
              .append(resultadoLexer.isSintaxisBasicaValida() ? "VALIDA" : "INVALIDA").append("\n");
        }
        sb.append("Resultado semantico: ").append(valido ? "VALIDA" : "INVALIDA").append("\n");
        sb.append("Dialecto: ").append(dialecto != null ? dialecto : "N/A").append("\n");

        if (resultadoLexer != null && resultadoLexer.hasErrores()) {
            sb.append("\n-- Errores lexicos/sintacticos --\n");
            resultadoLexer.getErrores().forEach(e -> sb.append("  * ").append(e).append("\n"));
        }

        if (!erroresSemanticos.isEmpty()) {
            sb.append("\n-- Errores semanticos --\n");
            for (ErrorSemantico err : erroresSemanticos) {
                sb.append("  * ").append(err).append("\n");
            }
        }

        if (!advertencias.isEmpty()) {
            sb.append("\n-- Advertencias --\n");
            for (String adv : advertencias) {
                sb.append("  * ").append(adv).append("\n");
            }
        }

        sb.append("\n").append(mensaje != null ? mensaje : "").append("\n");
        return sb.toString();
    }
}
