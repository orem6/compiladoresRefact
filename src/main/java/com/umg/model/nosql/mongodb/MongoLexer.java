package com.umg.model.nosql.mongodb;

import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;
import com.umg.model.nosql.common.NoSqlSyntaxError;

import java.util.*;

public class MongoLexer {

    private static final Set<String> MONGO_OPERATORS = Set.of(
        "$eq", "$ne", "$gt", "$gte", "$lt", "$lte",
        "$in", "$nin", "$and", "$or", "$not", "$nor",
        "$exists", "$regex",
        "$set", "$unset", "$inc", "$push", "$pull",
        "$match", "$group", "$project", "$sort", "$lookup",
        "$limit", "$skip", "$count",
        "$sum", "$avg", "$min", "$max", "$first", "$last"
    );

    private static final Set<String> SUPPORTED_METHODS = Set.of(
        "find", "findOne", "insertOne", "insertMany",
        "updateOne", "updateMany", "deleteOne", "deleteMany",
        "aggregate"
    );

    private final String input;
    private int pos;
    private int line;
    private int col;
    private final List<NoSqlSyntaxError> errors;
    private final List<NoSqlToken> tokens;

    public MongoLexer(String input) {
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
            if (c == '/' && peek() == '/') { readLineComment(); continue; }
            if (c == '/' && peek() == '*') { readBlockComment(); continue; }
            if (c == '{') { addToken(NoSqlTokenType.LEFT_BRACE, "{"); pos++; col++; continue; }
            if (c == '}') { addToken(NoSqlTokenType.RIGHT_BRACE, "}"); pos++; col++; continue; }
            if (c == '[') { addToken(NoSqlTokenType.LEFT_BRACKET, "["); pos++; col++; continue; }
            if (c == ']') { addToken(NoSqlTokenType.RIGHT_BRACKET, "]"); pos++; col++; continue; }
            if (c == '(') { addToken(NoSqlTokenType.LEFT_PAREN, "("); pos++; col++; continue; }
            if (c == ')') { addToken(NoSqlTokenType.RIGHT_PAREN, ")"); pos++; col++; continue; }
            if (c == ':') { addToken(NoSqlTokenType.COLON, ":"); pos++; col++; continue; }
            if (c == ',') { addToken(NoSqlTokenType.COMMA, ","); pos++; col++; continue; }
            if (c == '.') { addToken(NoSqlTokenType.DOT, "."); pos++; col++; continue; }
            if (c == ';') { addToken(NoSqlTokenType.SEMICOLON, ";"); pos++; col++; continue; }
            if (c == '"' || c == '\'') { readString(c); continue; }
            if (c == '$') { readMongoOperator(); continue; }
            if (c == '-' && pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1))) { readNumber(); continue; }
            if (Character.isDigit(c)) { readNumber(); continue; }
            if (Character.isLetter(c) || c == '_') { readIdentifier(); continue; }
            error("MONGO_INVALID_CHARACTER", "Caracter no reconocido: '" + c + "'");
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
        StringBuilder sb = new StringBuilder("//");
        pos += 2; col += 2;
        while (pos < input.length() && input.charAt(pos) != '\n') {
            sb.append(input.charAt(pos));
            pos++; col++;
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
        errors.add(new NoSqlSyntaxError("MONGO_UNCLOSED_COMMENT", "Comentario multilinea sin cerrar.", sl, sc, "/*", "LEXICAL"));
    }

    private void readString(char quote) {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder();
        sb.append(quote);
        pos++; col++;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '\\') { sb.append(c); pos++; col++; if (pos < input.length()) { sb.append(input.charAt(pos)); pos++; col++; } continue; }
            if (c == quote) { sb.append(quote); pos++; col++; tokens.add(new NoSqlToken(NoSqlTokenType.STRING, sb.toString(), sl, sc)); return; }
            if (c == '\n') { errors.add(new NoSqlSyntaxError("MONGO_UNCLOSED_STRING", "String sin cerrar.", sl, sc, sb.toString(), "LEXICAL")); return; }
            sb.append(c); pos++; col++;
        }
        errors.add(new NoSqlSyntaxError("MONGO_UNCLOSED_STRING", "String sin cerrar.", sl, sc, sb.toString(), "LEXICAL"));
    }

    private void readMongoOperator() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder("$");
        pos++; col++;
        while (pos < input.length() && (Character.isLetter(input.charAt(pos)) || input.charAt(pos) == '_')) {
            sb.append(input.charAt(pos)); pos++; col++;
        }
        String op = sb.toString();
        if (MONGO_OPERATORS.contains(op)) {
            tokens.add(new NoSqlToken(NoSqlTokenType.MONGO_OPERATOR, op, sl, sc));
        } else {
            errors.add(new NoSqlSyntaxError("MONGO_UNSUPPORTED_OPERATOR", "Operador MongoDB no soportado: " + op, sl, sc, op, "SYNTAX"));
            tokens.add(new NoSqlToken(NoSqlTokenType.OPERATOR, op, sl, sc));
        }
    }

    private void readNumber() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '-') { sb.append('-'); pos++; col++; }
        boolean hasDot = false;
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            if (input.charAt(pos) == '.') { if (hasDot) break; hasDot = true; }
            sb.append(input.charAt(pos)); pos++; col++;
        }
        tokens.add(new NoSqlToken(NoSqlTokenType.NUMBER, sb.toString(), sl, sc));
    }

    private void readIdentifier() {
        int sl = line; int sc = col;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            sb.append(input.charAt(pos)); pos++; col++;
        }
        String word = sb.toString();
        if (word.equals("true") || word.equals("false")) {
            tokens.add(new NoSqlToken(NoSqlTokenType.BOOLEAN, word, sl, sc));
        } else if (word.equals("null")) {
            tokens.add(new NoSqlToken(NoSqlTokenType.NULL, word, sl, sc));
        } else if (word.equals("db") || SUPPORTED_METHODS.contains(word)) {
            tokens.add(new NoSqlToken(NoSqlTokenType.KEYWORD, word, sl, sc));
        } else {
            tokens.add(new NoSqlToken(NoSqlTokenType.IDENTIFIER, word, sl, sc));
        }
    }
}
