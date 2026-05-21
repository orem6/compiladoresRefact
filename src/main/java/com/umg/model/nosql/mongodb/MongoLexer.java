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
            if (c == '/' && peek() == '/') {
                readLineComment();
                continue;
            }
            if (c == '/' && peek() == '*') {
                readBlockComment();
                continue;
            }
            if (c == '{') { addToken(NoSqlTokenType.LEFT_BRACE, "{", 1); pos++; col++; continue; }
            if (c == '}') { addToken(NoSqlTokenType.RIGHT_BRACE, "}", 1); pos++; col++; continue; }
            if (c == '[') { addToken(NoSqlTokenType.LEFT_BRACKET, "[", 1); pos++; col++; continue; }
            if (c == ']') { addToken(NoSqlTokenType.RIGHT_BRACKET, "]", 1); pos++; col++; continue; }
            if (c == '(') { addToken(NoSqlTokenType.LEFT_PAREN, "(", 1); pos++; col++; continue; }
            if (c == ')') { addToken(NoSqlTokenType.RIGHT_PAREN, ")", 1); pos++; col++; continue; }
            if (c == ':') { addToken(NoSqlTokenType.COLON, ":", 1); pos++; col++; continue; }
            if (c == ',') { addToken(NoSqlTokenType.COMMA, ",", 1); pos++; col++; continue; }
            if (c == '.') { addToken(NoSqlTokenType.DOT, ".", 1); pos++; col++; continue; }
            if (c == ';') { addToken(NoSqlTokenType.SEMICOLON, ";", 1); pos++; col++; continue; }
            if (c == '"' || c == '\'') { readString(c); continue; }
            if (c == '$') { readMongoOperator(); continue; }
            if (c == '-' && pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1))) {
                readNumber();
                continue;
            }
            if (Character.isDigit(c)) { readNumber(); continue; }
            if (Character.isLetter(c) || c == '_') { readIdentifier(); continue; }
            error("MONGO_INVALID_CHARACTER", "Caracter no reconocido: '" + c + "'", 1);
            pos++; col++;
        }
        addToken(NoSqlTokenType.EOF, "", line, col);
        return tokens;
    }

    public List<NoSqlSyntaxError> getErrors() { return errors; }
    public List<NoSqlToken> getTokens() { return tokens; }

    private char peek() {
        return pos + 1 < input.length() ? input.charAt(pos + 1) : '\0';
    }

    private void addToken(NoSqlTokenType type, String lexeme, int length) {
        tokens.add(new NoSqlToken(type, lexeme, line, col));
        col += length;
    }

    private void addToken(NoSqlTokenType type, String lexeme, int l, int c) {
        tokens.add(new NoSqlToken(type, lexeme, l, c));
    }

    private void error(String code, String message, int length) {
        int startCol = col;
        String lexeme = pos < input.length() ? String.valueOf(input.charAt(pos)) : "";
        errors.add(new NoSqlSyntaxError(code, message, line, startCol, lexeme, "LEXICAL"));
        col += length;
    }

    private void readLineComment() {
        int startCol = col;
        int startLine = line;
        StringBuilder sb = new StringBuilder("//");
        pos += 2; col += 2;
        while (pos < input.length() && input.charAt(pos) != '\n') {
            sb.append(input.charAt(pos));
            pos++; col++;
        }
        tokens.add(new NoSqlToken(NoSqlTokenType.COMMENT, sb.toString(), startLine, startCol));
    }

    private void readBlockComment() {
        int startCol = col;
        int startLine = line;
        StringBuilder sb = new StringBuilder("/*");
        pos += 2; col += 2;
        while (pos + 1 < input.length()) {
            if (input.charAt(pos) == '*' && input.charAt(pos + 1) == '/') {
                sb.append("*/");
                pos += 2; col += 2;
                tokens.add(new NoSqlToken(NoSqlTokenType.COMMENT, sb.toString(), startLine, startCol));
                return;
            }
            if (input.charAt(pos) == '\n') { line++; col = 0; }
            sb.append(input.charAt(pos));
            pos++; col++;
        }
        errors.add(new NoSqlSyntaxError("MONGO_UNCLOSED_COMMENT", "Comentario multilinea sin cerrar.", startLine, startCol, "/*", "LEXICAL"));
    }

    private void readString(char quote) {
        int startCol = col;
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        sb.append(quote);
        pos++; col++;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '\\') {
                sb.append(c);
                pos++; col++;
                if (pos < input.length()) { sb.append(input.charAt(pos)); pos++; col++; }
                continue;
            }
            if (c == quote) {
                sb.append(quote);
                pos++; col++;
                tokens.add(new NoSqlToken(NoSqlTokenType.STRING, sb.toString(), startLine, startCol));
                return;
            }
            if (c == '\n') {
                errors.add(new NoSqlSyntaxError("MONGO_UNCLOSED_STRING", "String sin cerrar antes de salto de linea.", startLine, startCol, sb.toString(), "LEXICAL"));
                return;
            }
            sb.append(c);
            pos++; col++;
        }
        errors.add(new NoSqlSyntaxError("MONGO_UNCLOSED_STRING", "String sin cerrar al final de la entrada.", startLine, startCol, sb.toString(), "LEXICAL"));
    }

    private void readMongoOperator() {
        int startCol = col;
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        sb.append('$');
        pos++; col++;
        while (pos < input.length() && (Character.isLetter(input.charAt(pos)) || input.charAt(pos) == '_')) {
            sb.append(input.charAt(pos));
            pos++; col++;
        }
        String op = sb.toString();
        if (MONGO_OPERATORS.contains(op)) {
            tokens.add(new NoSqlToken(NoSqlTokenType.MONGO_OPERATOR, op, startLine, startCol));
        } else {
            errors.add(new NoSqlSyntaxError("MONGO_UNSUPPORTED_OPERATOR",
                "Operador MongoDB no soportado: " + op, startLine, startCol, op, "SYNTAX"));
            tokens.add(new NoSqlToken(NoSqlTokenType.OPERATOR, op, startLine, startCol));
        }
    }

    private void readNumber() {
        int startCol = col;
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '-') {
            sb.append('-');
            pos++; col++;
        }
        boolean hasDot = false;
        while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
            if (input.charAt(pos) == '.') {
                if (hasDot) break;
                hasDot = true;
            }
            sb.append(input.charAt(pos));
            pos++; col++;
        }
        tokens.add(new NoSqlToken(NoSqlTokenType.NUMBER, sb.toString(), startLine, startCol));
    }

    private void readIdentifier() {
        int startCol = col;
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
            sb.append(input.charAt(pos));
            pos++; col++;
        }
        String word = sb.toString();
        if (word.equals("true") || word.equals("false")) {
            tokens.add(new NoSqlToken(NoSqlTokenType.BOOLEAN, word, startLine, startCol));
        } else if (word.equals("null")) {
            tokens.add(new NoSqlToken(NoSqlTokenType.NULL, word, startLine, startCol));
        } else if (word.equals("db") || SUPPORTED_METHODS.contains(word)) {
            tokens.add(new NoSqlToken(NoSqlTokenType.KEYWORD, word, startLine, startCol));
        } else {
            tokens.add(new NoSqlToken(NoSqlTokenType.IDENTIFIER, word, startLine, startCol));
        }
    }
}
