package com.dataquery.sqlcompiler.model.lexer;

import com.dataquery.sqlcompiler.model.error.CompilerError;
import com.dataquery.sqlcompiler.model.error.ErrorCollector;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final ErrorCollector errorCollector;
    private String input;
    private int position;
    private int line;
    private int column;

    public Lexer(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    public List<Token> tokenize(String sql) {
        this.input = sql;
        this.position = 0;
        this.line = 1;
        this.column = 1;

        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isWhitespace(current)) {
                skipWhitespace();
                continue;
            }

            if (current == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, ";", line, column));
                advance();
            } else if (current == ',') {
                tokens.add(new Token(TokenType.COMMA, ",", line, column));
                advance();
            } else if (current == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", line, column));
                advance();
            } else if (current == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", line, column));
                advance();
            } else if (current == '*') {
                tokens.add(new Token(TokenType.STAR, "*", line, column));
                advance();
            } else if (current == '.') {
                tokens.add(new Token(TokenType.DOT, ".", line, column));
                advance();
            } else if (isOperator(current)) {
                readOperator(tokens);
            } else if (Character.isDigit(current)) {
                readNumber(tokens);
            } else if (Character.isLetter(current) || current == '_') {
                readIdentifierOrKeyword(tokens);
            } else if (current == '\'') {
                readStringLiteral(tokens);
            } else {
                errorCollector.addError(new CompilerError(
                    "LEXICAL",
                    "Caracter no reconocido: '" + current + "'",
                    line,
                    column
                ));
                advance();
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            if (input.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            advance();
        }
    }

    private void readOperator(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && isOperator(input.charAt(position))) {
            sb.append(input.charAt(position));
            advance();
        }
        tokens.add(new Token(TokenType.OPERATOR, sb.toString(), startLine, startColumn));
    }

    private void readNumber(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        boolean hasDecimal = false;
        while (position < input.length() && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
            if (input.charAt(position) == '.') hasDecimal = true;
            sb.append(input.charAt(position));
            advance();
        }
        TokenType type = hasDecimal ? TokenType.DECIMAL : TokenType.INTEGER;
        tokens.add(new Token(type, sb.toString(), startLine, startColumn));
    }

    private void readIdentifierOrKeyword(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            sb.append(input.charAt(position));
            advance();
        }
        String word = sb.toString().toUpperCase();
        if (isKeyword(word)) {
            tokens.add(new Token(TokenType.KEYWORD, word, startLine, startColumn));
        } else {
            tokens.add(new Token(TokenType.IDENTIFIER, word, startLine, startColumn));
        }
    }

    private void readStringLiteral(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        advance();
        while (position < input.length() && input.charAt(position) != '\'') {
            sb.append(input.charAt(position));
            advance();
        }
        if (position < input.length()) {
            advance();
        }
        tokens.add(new Token(TokenType.STRING_LITERAL, sb.toString(), startLine, startColumn));
    }

    private boolean isOperator(char c) {
        return "<>=!+-/*%".indexOf(c) != -1;
    }

    private boolean isKeyword(String word) {
        return switch (word) {
            case "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET",
                 "DELETE", "CREATE", "TABLE", "DROP", "INT", "VARCHAR", "FLOAT", "DOUBLE",
                 "AND", "OR", "NOT", "NULL", "ORDER", "BY", "GROUP", "HAVING", "JOIN",
                 "INNER", "LEFT", "RIGHT", "ON", "AS", "IN", "BETWEEN", "LIKE", "IS",
                 "ASC", "DESC", "DISTINCT", "COUNT", "SUM", "AVG", "MAX", "MIN", "PRIMARY",
                 "KEY", "FOREIGN", "REFERENCES", "DEFAULT", "UNIQUE", "INDEX", "VIEW",
                 "ALTER", "ADD", "MODIFY", "TRUNCATE" -> true;
            default -> false;
        };
    }

    private void advance() {
        position++;
        column++;
    }
}
