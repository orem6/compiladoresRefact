package com.umg.model.semantic.extractor;

import com.umg.model.dialect.SqlDialect;
import com.umg.model.lexer.ErrorLexico;
import com.umg.model.lexer.ResultadoLexer;
import com.umg.model.lexer.Token;
import com.umg.model.lexer.TokenType;
import com.umg.model.semantic.util.IdentifierNormalizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SqlReferenceExtractor {
    private static final Set<String> STMT_KEYWORDS = new HashSet<>(Arrays.asList(
        "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER", "TRUNCATE"));

    private static final Set<String> JOIN_KEYWORDS = new HashSet<>(Arrays.asList(
        "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "CROSS", "OUTER"));

    private static final Set<String> CLAUSE_KEYWORDS = new HashSet<>(Arrays.asList(
        "WHERE", "GROUP", "ORDER", "HAVING", "LIMIT", "OFFSET", "UNION",
        "ON", "INTO", "VALUES", "SET", "FROM"));

    private final IdentifierNormalizer normalizer;

    public SqlReferenceExtractor() {
        this.normalizer = new IdentifierNormalizer();
    }

    public ReferenciasSql extraer(ResultadoLexer resultado, SqlDialect dialecto) {
        ReferenciasSql refs = new ReferenciasSql();
        List<Token> tokens = resultado.getTokens();
        if (tokens == null || tokens.isEmpty()) return refs;

        String tipoStmt = detectarTipoSentencia(tokens);
        if (tipoStmt == null) return refs;
        refs.setTipoSentencia(tipoStmt);

        switch (tipoStmt) {
            case "SELECT" -> extraerSelect(refs, tokens, dialecto);
            case "INSERT" -> extraerInsert(refs, tokens, dialecto);
            case "UPDATE" -> extraerUpdate(refs, tokens, dialecto);
            case "DELETE" -> extraerDelete(refs, tokens, dialecto);
            case "CREATE" -> extraerCreate(refs, tokens, dialecto);
            case "DROP" -> extraerDrop(refs, tokens, dialecto);
            case "ALTER" -> extraerAlter(refs, tokens, dialecto);
            case "TRUNCATE" -> extraerTruncate(refs, tokens, dialecto);
        }
        return refs;
    }

    private String detectarTipoSentencia(List<Token> tokens) {
        for (Token t : tokens) {
            if (esPalabraReservada(t) && STMT_KEYWORDS.contains(t.getLexeme().toUpperCase())) {
                return t.getLexeme().toUpperCase();
            }
        }
        return null;
    }

    private void extraerSelect(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int fromIdx = indexOfKeyword(tokens, "FROM");
        if (fromIdx < 0) return;

        int i = fromIdx + 1;
        while (i < tokens.size()) {
            Token t = tokens.get(i);
            if (esFinDeConsulta(t)) break;
            String upper = t.getLexeme().toUpperCase();
            if (esClauseKeyword(upper) && !upper.equals("ON")) break;

            if (esIdentificador(t)) {
                String rawNombre = t.getLexeme();
                String nombre = normalizer.normalizar(rawNombre, dialecto);
                int linea = t.getLinea();
                int columna = t.getColumna();

                if (i + 1 < tokens.size() && tokens.get(i + 1).getType() == TokenType.PUNTO) {
                    i += 2;
                    if (i < tokens.size() && esIdentificador(tokens.get(i))) {
                        String nombreTabla = normalizer.normalizar(tokens.get(i).getLexeme(), dialecto);
                        ReferenciaTabla rt = new ReferenciaTabla(nombreTabla, nombre, null,
                            tokens.get(i).getLinea(), tokens.get(i).getColumna());
                        refs.addTabla(rt);
                        refs.addAlias(nombre, rt);
                    }
                    i++;
                    continue;
                }

                ReferenciaTabla rt = new ReferenciaTabla(nombre, null, null, linea, columna);
                refs.addTabla(rt);

                i++;
                if (i < tokens.size() && esIdentificador(tokens.get(i))
                    && !esClauseKeyword(tokens.get(i).getLexeme().toUpperCase())
                    && !STMT_KEYWORDS.contains(tokens.get(i).getLexeme().toUpperCase())
                    && !tokens.get(i).getLexeme().toUpperCase().equals("ON")) {
                    String alias = normalizer.normalizar(tokens.get(i).getLexeme(), dialecto);
                    rt.setAlias(alias);
                    refs.addAlias(alias, rt);
                    i++;
                }

                while (i < tokens.size()) {
                    Token ct = tokens.get(i);
                    String cu = ct.getLexeme().toUpperCase();
                    if (esFinDeConsulta(ct)) break;
                    if (esClauseKeyword(cu)) break;
                    if (esIdentificador(ct)) {
                        ReferenciaTabla rt2 = new ReferenciaTabla(normalizer.normalizar(ct.getLexeme(), dialecto), null, null,
                            ct.getLinea(), ct.getColumna());
                        refs.addTabla(rt2);
                        i++;
                        if (i < tokens.size() && esIdentificador(tokens.get(i))
                            && !esClauseKeyword(tokens.get(i).getLexeme().toUpperCase())) {
                            rt2.setAlias(normalizer.normalizar(tokens.get(i).getLexeme(), dialecto));
                            refs.addAlias(rt2.getAlias(), rt2);
                            i++;
                        }
                        continue;
                    }
                    i++;
                }
            }
            i++;
        }

        extraerColumnasSelect(refs, tokens, dialecto, fromIdx);
        extraerColumnasWhere(refs, tokens, dialecto, "WHERE");
        extraerColumnasWhere(refs, tokens, dialecto, "ON");
        extraerColumnasWhere(refs, tokens, dialecto, "GROUP");
        extraerColumnasWhere(refs, tokens, dialecto, "ORDER");
        extraerColumnasWhere(refs, tokens, dialecto, "HAVING");
    }

    private void extraerColumnasSelect(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto, int fromIdx) {
        int i = 1;
        while (i < fromIdx) {
            Token t = tokens.get(i);
            if (t.getLexeme().equals("*")) { i++; continue; }
            if (t.getType() == TokenType.COMA || t.getType() == TokenType.PUNTO) { i++; continue; }
            if (t.getType() == TokenType.FUNCION) {
                i++;
                if (i < tokens.size() && tokens.get(i).getType() == TokenType.PARENTESIS_IZQUIERDO) {
                    int parenDepth = 1;
                    i++;
                    while (i < tokens.size() && parenDepth > 0) {
                        if (tokens.get(i).getType() == TokenType.PARENTESIS_IZQUIERDO) parenDepth++;
                        else if (tokens.get(i).getType() == TokenType.PARENTESIS_DERECHO) parenDepth--;
                        if (parenDepth > 0) {
                            String colRaw = tokens.get(i).getLexeme();
                            if (!colRaw.equals(",") && !colRaw.equals("*")) {
                                refs.addColumna(new ReferenciaColumna(
                                    normalizer.normalizar(colRaw, dialecto), null,
                                    tokens.get(i).getLinea(), tokens.get(i).getColumna()));
                            }
                        }
                        i++;
                    }
                }
                continue;
            }
            if (esIdentificador(t)) {
                String raw = t.getLexeme();
                String nombre = normalizer.normalizar(raw, dialecto);
                int linea = t.getLinea();
                int col = t.getColumna();

                if (i + 1 < tokens.size() && tokens.get(i + 1).getType() == TokenType.PUNTO) {
                    String calificador = nombre;
                    i += 2;
                    if (i < tokens.size() && esIdentificador(tokens.get(i))) {
                        String colName = normalizer.normalizar(tokens.get(i).getLexeme(), dialecto);
                        refs.addColumna(new ReferenciaColumna(colName, calificador,
                            tokens.get(i).getLinea(), tokens.get(i).getColumna()));
                    }
                } else {
                    refs.addColumna(new ReferenciaColumna(nombre, null, linea, col));
                }
            }
            i++;
        }
    }

    private void extraerColumnasWhere(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto, String keyword) {
        int idx = indexOfKeyword(tokens, keyword);
        if (idx < 0) return;
        idx++;
        while (idx < tokens.size()) {
            Token t = tokens.get(idx);
            if (esFinDeConsulta(t)) break;
            String upper = t.getLexeme().toUpperCase();
            if (esClauseKeyword(upper) && !upper.equals(keyword) && !upper.equals("ON")) break;

            if (esIdentificador(t)) {
                String raw = t.getLexeme();
                String nombre = normalizer.normalizar(raw, dialecto);
                int linea = t.getLinea();
                int col = t.getColumna();

                if (idx + 1 < tokens.size() && tokens.get(idx + 1).getType() == TokenType.PUNTO) {
                    String calificador = nombre;
                    idx += 2;
                    if (idx < tokens.size() && esIdentificador(tokens.get(idx))) {
                        refs.addColumna(new ReferenciaColumna(
                            normalizer.normalizar(tokens.get(idx).getLexeme(), dialecto),
                            calificador, tokens.get(idx).getLinea(), tokens.get(idx).getColumna()));
                    }
                } else if (idx + 1 < tokens.size()
                    && (tokens.get(idx + 1).getType() == TokenType.OPERADOR_COMPARACION
                        || tokens.get(idx + 1).getType() == TokenType.OPERADOR
                        || tokens.get(idx + 1).getLexeme().equals("="))
                    && !upper.equals("AND") && !upper.equals("OR")
                    && !upper.equals("IN") && !upper.equals("LIKE")
                    && !upper.equals("BETWEEN") && !upper.equals("IS")
                    && !upper.equals("NOT") && !upper.equals("NULL")
                    && !upper.equals("TRUE") && !upper.equals("FALSE")) {
                    refs.addColumna(new ReferenciaColumna(nombre, null, linea, col));
                }
            }
            idx++;
        }
    }

    private void extraerInsert(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int intoIdx = indexOfKeyword(tokens, "INTO");
        int start = intoIdx >= 0 ? intoIdx + 1 : 2;
        if (start < tokens.size() && esIdentificador(tokens.get(start))) {
            Token t = tokens.get(start);
            String nombre = normalizer.normalizar(t.getLexeme(), dialecto);
            String esquema = null;
            if (start + 1 < tokens.size() && tokens.get(start + 1).getType() == TokenType.PUNTO) {
                esquema = nombre;
                start += 2;
                if (start < tokens.size() && esIdentificador(tokens.get(start))) {
                    nombre = normalizer.normalizar(tokens.get(start).getLexeme(), dialecto);
                }
            }
            refs.addTabla(new ReferenciaTabla(nombre, esquema, null, t.getLinea(), t.getColumna()));

            if (start + 1 < tokens.size() && tokens.get(start + 1).getType() == TokenType.PARENTESIS_IZQUIERDO) {
                int j = start + 2;
                while (j < tokens.size() && tokens.get(j).getType() != TokenType.PARENTESIS_DERECHO) {
                    if (esIdentificador(tokens.get(j))) {
                        refs.addColumna(new ReferenciaColumna(
                            normalizer.normalizar(tokens.get(j).getLexeme(), dialecto),
                            nombre, tokens.get(j).getLinea(), tokens.get(j).getColumna()));
                    }
                    j++;
                }
            }
        }
    }

    private void extraerUpdate(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int i = 1;
        if (i < tokens.size() && esIdentificador(tokens.get(i))) {
            String nombre = normalizer.normalizar(tokens.get(i).getLexeme(), dialecto);
            String esquema = null;
            int linea = tokens.get(i).getLinea();
            int col = tokens.get(i).getColumna();
            if (i + 1 < tokens.size() && tokens.get(i + 1).getType() == TokenType.PUNTO) {
                esquema = nombre;
                i += 2;
                if (i < tokens.size() && esIdentificador(tokens.get(i))) {
                    nombre = normalizer.normalizar(tokens.get(i).getLexeme(), dialecto);
                    linea = tokens.get(i).getLinea();
                    col = tokens.get(i).getColumna();
                }
            }
            refs.addTabla(new ReferenciaTabla(nombre, esquema, null, linea, col));

            int setIdx = indexOfKeyword(tokens, "SET");
            if (setIdx > 0) {
                int j = setIdx + 1;
                while (j < tokens.size()) {
                    Token t = tokens.get(j);
                    if (esFinDeConsulta(t)) break;
                    String upper = t.getLexeme().toUpperCase();
                    if (upper.equals("WHERE")) break;
                    if (j + 1 < tokens.size() && tokens.get(j + 1).getLexeme().equals("=")
                        && esIdentificador(t)) {
                        refs.addColumna(new ReferenciaColumna(
                            normalizer.normalizar(t.getLexeme(), dialecto),
                            nombre, t.getLinea(), t.getColumna()));
                    }
                    j++;
                }
                extraerColumnasWhere(refs, tokens, dialecto, "WHERE");
            }
        }
    }

    private void extraerDelete(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int fromIdx = indexOfKeyword(tokens, "FROM");
        int start = fromIdx >= 0 ? fromIdx + 1 : 1;
        if (start < tokens.size() && esIdentificador(tokens.get(start))) {
            Token t = tokens.get(start);
            String nombre = normalizer.normalizar(t.getLexeme(), dialecto);
            String esquema = null;
            if (start + 1 < tokens.size() && tokens.get(start + 1).getType() == TokenType.PUNTO) {
                esquema = nombre;
                start += 2;
                if (start < tokens.size() && esIdentificador(tokens.get(start))) {
                    nombre = normalizer.normalizar(tokens.get(start).getLexeme(), dialecto);
                }
            }
            refs.addTabla(new ReferenciaTabla(nombre, esquema, null, t.getLinea(), t.getColumna()));
        }
        extraerColumnasWhere(refs, tokens, dialecto, "WHERE");
    }

    private void extraerCreate(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int tableIdx = indexOfKeyword(tokens, "TABLE");
        if (tableIdx < 0) return;
        if (tableIdx + 1 < tokens.size() && esIdentificador(tokens.get(tableIdx + 1))) {
            Token t = tokens.get(tableIdx + 1);
            String nombre = normalizer.normalizar(t.getLexeme(), dialecto);
            String esquema = null;
            int idx = tableIdx + 1;
            if (idx + 1 < tokens.size() && tokens.get(idx + 1).getType() == TokenType.PUNTO) {
                esquema = nombre;
                idx += 2;
                if (idx < tokens.size() && esIdentificador(tokens.get(idx))) {
                    nombre = normalizer.normalizar(tokens.get(idx).getLexeme(), dialecto);
                }
            }
            refs.addTabla(new ReferenciaTabla(nombre, esquema, null, t.getLinea(), t.getColumna()));
        }
    }

    private void extraerDrop(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int tableIdx = indexOfKeyword(tokens, "TABLE");
        if (tableIdx < 0) return;
        if (tableIdx + 1 < tokens.size() && esIdentificador(tokens.get(tableIdx + 1))) {
            Token t = tokens.get(tableIdx + 1);
            refs.addTabla(new ReferenciaTabla(
                normalizer.normalizar(t.getLexeme(), dialecto), null, null,
                t.getLinea(), t.getColumna()));
        }
    }

    private void extraerAlter(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int tableIdx = indexOfKeyword(tokens, "TABLE");
        if (tableIdx < 0) return;
        if (tableIdx + 1 < tokens.size() && esIdentificador(tokens.get(tableIdx + 1))) {
            Token t = tokens.get(tableIdx + 1);
            refs.addTabla(new ReferenciaTabla(
                normalizer.normalizar(t.getLexeme(), dialecto), null, null,
                t.getLinea(), t.getColumna()));
        }
    }

    private void extraerTruncate(ReferenciasSql refs, List<Token> tokens, SqlDialect dialecto) {
        int tableIdx = indexOfKeyword(tokens, "TABLE");
        int start = tableIdx >= 0 ? tableIdx + 1 : 1;
        if (start < tokens.size() && esIdentificador(tokens.get(start))) {
            Token t = tokens.get(start);
            refs.addTabla(new ReferenciaTabla(
                normalizer.normalizar(t.getLexeme(), dialecto), null, null,
                t.getLinea(), t.getColumna()));
        }
    }

    private int indexOfKeyword(List<Token> tokens, String keyword) {
        for (int i = 0; i < tokens.size(); i++) {
            if (esPalabraReservada(tokens.get(i))
                && tokens.get(i).getLexeme().equalsIgnoreCase(keyword)) {
                return i;
            }
        }
        return -1;
    }

    private boolean esPalabraReservada(Token t) {
        return t.getType() == TokenType.PALABRA_RESERVADA
            || t.getType() == TokenType.KEYWORD;
    }

    private boolean esIdentificador(Token t) {
        return t.getType() == TokenType.IDENTIFICADOR
            || t.getType() == TokenType.PALABRA_RESERVADA
            || t.getType() == TokenType.IDENTIFICADOR_DELIMITADO
            || t.getType() == TokenType.KEYWORD;
    }

    private boolean esClauseKeyword(String word) {
        return CLAUSE_KEYWORDS.contains(word) || JOIN_KEYWORDS.contains(word);
    }

    private boolean esFinDeConsulta(Token t) {
        return t.getType() == TokenType.PUNTO_Y_COMA
            || t.getType() == TokenType.EOF
            || t.getType() == TokenType.COMENTARIO_LINEA
            || t.getType() == TokenType.COMENTARIO_BLOQUE;
    }
}
