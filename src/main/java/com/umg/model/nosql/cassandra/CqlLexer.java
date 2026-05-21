package com.umg.model.nosql.cassandra;

import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;
import com.umg.model.nosql.common.NoSqlSyntaxError;

import java.util.*;

public class CqlLexer {

    private static final Set<String> KEYWORDS = Set.of(
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES",
        "UPDATE", "SET", "DELETE", "CREATE", "KEYSPACE", "TABLE",
        "ALTER", "DROP", "TRUNCATE", "PRIMARY", "KEY", "WITH",
        "AND", "OR", "IF", "EXISTS", "NOT", "NULL", "USE",
        "USING", "TTL", "TIMESTAMP", "ORDER", "BY", "ALLOW",
        "FILTERING", "LIMIT", "ASC", "DESC", "BEGIN", "BATCH",
        "APPLY", "ADD", "RENAME", "COMPACT", "STORAGE"
    );

    private static final Set<String> DATA_TYPES = Set.of(
        "TEXT", "VARCHAR", "ASCII", "INT", "BIGINT", "SMALLINT",
        "TINYINT", "VARINT", "BOOLEAN", "UUID", "TIMEUUID",
        "TIMESTAMP", "DATE", "TIME", "FLOAT", "DOUBLE", "DECIMAL",
        "BLOB", "LIST", "SET", "MAP", "COUNTER"
    );

    private final String input;
    private int pos;
    private int line;
    private int col;
    private final List<NoSqlSyntaxError> errors;
    private final List<NoSqlToken> tokens;

    public CqlLexer(String input) {
        this.input = input;
        this.pos = 0;
        this.line = 1;
        this.col = 1;
        this.errors = new ArrayList<>();
        this.tokens = new ArrayList<>();
    }

    public List<NoSqlToken> tokenize() {
        tokens.clear();
        errors.clear();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isWhitespace(c)) {
                if (c == '\n') { line++; col = 1; }
                else { col++; }
                pos++;
                continue;
            }
            if (c == '-' && peek() == '-') { readLineComment(); continue; }
            if (c == '/' && peek() == '/') { readLineComment(); continue; }
            if (c == '/' && peek() == '*') { readBlockComment(); continue; }
            if (c == '(') { addToken(NoSqlTokenType.LEFT_PAREN, "("); pos++; col++; continue; }
            if (c == ')') { addToken(NoSqlTokenType.RIGHT_PAREN, ")"); pos++; col++; continue; }
            if (c == '{') { addToken(NoSqlTokenType.LEFT_BRACE, "{"); pos++; col++; continue; }
            if (c == '}') { addToken(NoSqlTokenType.RIGHT_BRACE, "}"); pos++; col++; continue; }
            if (c == '[') { addToken(NoSqlTokenType.LEFT_BRACKET, "["); pos++; col++; continue; }
            if (c == ']') { addToken(NoSqlTokenType.RIGHT_BRACKET, "]"); pos++; col++; continue; }
            if (c == ',') { addToken(NoSqlTokenType.COMMA, ","); pos++; col++; continue; }
            if (c == '.') { addToken(NoSqlTokenType.DOT, "."); pos++; col++; continue; }
            if (c == ';') { addToken(NoSqlTokenType.SEMICOLON, ";"); pos++; col++; continue; }
            if (c == ':') { addToken(NoSqlTokenType.COLON, ":"); pos++; col++; continue; }
            if (c == '=') { addToken(NoSqlTokenType.OPERATOR, "="); pos++; col++; continue; }
            if (c == '<') {
                if (peek() == '=') { addToken(NoSqlTokenType.OPERATOR, "<="); pos += 2; col += 2; }
                else { addToken(NoSqlTokenType.OPERATOR, "<"); pos++; col++; }
                continue;
            }
            if (c == '>') {
                if (peek() == '=') { addToken(NoSqlTokenType.OPERATOR, ">="); pos += 2; col += 2; }
                else { addToken(NoSqlTokenType.OPERATOR, ">"); pos++; col++; }
                continue;
            }
            if (c == '*') { addToken(NoSqlTokenType.OPERATOR, "*"); pos++; col++; continue; }
            if (c == '\'') { readString(); continue; }
            if (Character.isDigit(c) || (c == '-' && pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1)))) {
                readNumber();
                continue;
            }
            if (Character.isLetter(c) || c == '_') { readIdentifier(); continue; }
            error("CQL_INVALID_CHARACTER", "Caracter no reconocido en CQL: '" + c + "'");
            pos++; col++;
        }
        addToken(NoSqlTokenType.EOF, "");
        return tokens;
    }

    public List<NoSqlSyntaxError> getErrors() { return errors; }
    public List<NoSqlToken> getTokens() { return tokens; }

    private char peek() { return pos + 1 < input.length() ? input.charAt(pos + 1) : '\0'; }

    private void addToken(NoSqlTokenType type, String lexeme) {
        tokens.add(new NoSqlToken(type, lexeme, line, col));
    }

    private void error(String code, String message) {
        String lex = pos < input.length() ? String.valueOf(input.charAt(pos)) : "";
        errors.add(new NoSqlSyntaxError(code, message, line, col, lex, "LEXICAL"));
    }

    private void readLineComment() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder(input.charAt(pos) == '-' ? "--" : "//");
        pos += 2; col += 2;
        while (pos < input.length() && input.charAt(pos) != '\n') {
            sb.append(input.charAt(pos)); pos++; col++;
        }
        tokens.add(new NoSqlToken(NoSqlTokenType.COMMENT, sb.toString(), sl, sc));
    }

    private void readBlockComment() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder("/*");
        pos += 2; col += 2;
        while (pos + 1 < input.length()) {
            if (input.charAt(pos) == '*' && input.charAt(pos + 1) == '/') {
                sb.append("*/"); pos += 2; col += 2;
                tokens.add(new NoSqlToken(NoSqlTokenType.COMMENT, sb.toString(), sl, sc));
                return;
            }
            if (input.charAt(pos) == '\n') { line++; col = 0; }
            sb.append(input.charAt(pos)); pos++; col++;
        }
        errors.add(new NoSqlSyntaxError("CQL_UNCLOSED_COMMENT", "Comentario multilinea sin cerrar.", sl, sc, "/*", "LEXICAL"));
    }

    private void readString() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder("'");
        pos++; col++;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '\'') {
                sb.append("'"); pos++; col++;
                tokens.add(new NoSqlToken(NoSqlTokenType.STRING, sb.toString(), sl, sc));
                return;
            }
            if (c == '\n') {
                errors.add(new NoSqlSyntaxError("CQL_UNCLOSED_STRING", "String sin cerrar.", sl, sc, sb.toString(), "LEXICAL"));
                return;
            }
            sb.append(c); pos++; col++;
        }
        errors.add(new NoSqlSyntaxError("CQL_UNCLOSED_STRING", "String sin cerrar.", sl, sc, sb.toString(), "LEXICAL"));
    }

    private void readNumber() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '-') { sb.append('-'); pos++; col++; }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos)); pos++; col++;
        }
        tokens.add(new NoSqlToken(NoSqlTokenType.NUMBER, sb.toString(), sl, sc));
    }

    private void readIdentifier() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_' || input.charAt(pos) == '.')) {
            sb.append(input.charAt(pos)); pos++; col++;
        }
        String word = sb.toString();
        String upper = word.toUpperCase();
        if (KEYWORDS.contains(upper)) {
            tokens.add(new NoSqlToken(NoSqlTokenType.KEYWORD, word, sl, sc));
        } else if (DATA_TYPES.contains(upper)) {
            tokens.add(new NoSqlToken(NoSqlTokenType.KEYWORD, word, sl, sc));
        } else if (upper.equals("TRUE") || upper.equals("FALSE")) {
            tokens.add(new NoSqlToken(NoSqlTokenType.BOOLEAN, word, sl, sc));
        } else if (upper.equals("NULL")) {
            tokens.add(new NoSqlToken(NoSqlTokenType.NULL, word, sl, sc));
        } else {
            tokens.add(new NoSqlToken(NoSqlTokenType.IDENTIFIER, word, sl, sc));
        }
    }
}
