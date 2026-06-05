package com.umg.model.lexer;

import com.umg.model.dialect.FunctionRegistry;
import com.umg.model.dialect.KeywordRegistry;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.error.CompilerError;
import com.umg.model.error.ErrorCollector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Lexer {
    private final ErrorCollector errorCollector;
    private String input;
    private int position;
    private int line;
    private int column;
    private SqlDialect detectedDialect;
    private boolean foundMySqlDelimiter;
    private boolean foundSqlServerDelimiter;
    private boolean foundPostgreSqlDelimiter;

    public Lexer(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.detectedDialect = SqlDialect.COMMON;
    }

    public List<Token> tokenize(String sql) {
        this.input = sql != null ? sql : "";
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.detectedDialect = SqlDialect.COMMON;
        this.foundMySqlDelimiter = false;
        this.foundSqlServerDelimiter = false;
        this.foundPostgreSqlDelimiter = false;

        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isWhitespace(current)) {
                skipWhitespace();
                continue;
            }

            if (tryReadLineComment(tokens)) continue;
            if (tryReadBlockComment(tokens)) continue;

            if (current == '\'') {
                readStringLiteral(tokens);
                continue;
            }

            if (current == '`') {
                readMySqlDelimitedIdentifier(tokens);
                continue;
            }

            if (current == '[') {
                readSqlServerDelimitedIdentifier(tokens);
                continue;
            }

            if (current == '"') {
                readPostgreSqlDelimitedIdentifier(tokens);
                continue;
            }

            if (current == ';') {
                tokens.add(new Token(TokenType.PUNTO_Y_COMA, ";", line, column, SqlDialect.COMMON));
                advance();
                continue;
            }

            if (current == ',') {
                tokens.add(new Token(TokenType.COMA, ",", line, column, SqlDialect.COMMON));
                advance();
                continue;
            }

            if (current == '(') {
                tokens.add(new Token(TokenType.PARENTESIS_IZQUIERDO, "(", line, column, SqlDialect.COMMON));
                advance();
                continue;
            }

            if (current == ')') {
                tokens.add(new Token(TokenType.PARENTESIS_DERECHO, ")", line, column, SqlDialect.COMMON));
                advance();
                continue;
            }

            if (current == '*') {
                tokens.add(new Token(TokenType.ASTERISCO, "*", line, column, SqlDialect.COMMON));
                advance();
                continue;
            }

            if (current == '.') {
                tokens.add(new Token(TokenType.PUNTO, ".", line, column, SqlDialect.COMMON));
                advance();
                continue;
            }

            if (current == '?') {
                tokens.add(new Token(TokenType.PARAMETRO, "?", line, column, SqlDialect.COMMON));
                advance();
                continue;
            }

            if (current == ':') {
                // Check for := (assignment) and :: (PostgreSQL cast) before placeholder
                if (position + 1 < input.length()
                    && (input.charAt(position + 1) == '=' || input.charAt(position + 1) == ':')) {
                    readOperator(tokens);
                    continue;
                }
                readPlaceholder(tokens);
                continue;
            }

            if (current == '$') {
                readPlaceholder(tokens);
                continue;
            }

            if (isDigit(current)) {
                readNumber(tokens);
                continue;
            }

            if (isLetter(current) || current == '_') {
                readIdentifierOrKeywordOrFunction(tokens);
                continue;
            }

            if (isOperatorStart(current)) {
                readOperator(tokens);
                continue;
            }

            errorCollector.addError(new CompilerError(
                "LEXICAL",
                "Caracter no reconocido: '" + current + "'",
                line, column
            ));
            advance();
        }

        tokens.add(new Token(TokenType.EOF, "", line, column, detectedDialect));
        return tokens;
    }

    public SqlDialect getDetectedDialect() {
        return detectedDialect;
    }

    public void detectDialect() {
        if (foundMySqlDelimiter) {
            detectedDialect = SqlDialect.MYSQL;
        } else if (foundSqlServerDelimiter) {
            detectedDialect = SqlDialect.SQL_SERVER;
        } else if (foundPostgreSqlDelimiter) {
            detectedDialect = SqlDialect.POSTGRESQL;
        }
    }

    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            if (input.charAt(position) == '\n') {
                line++;
                column = 1;
            } else if (input.charAt(position) == '\r') {
                column = 1;
            } else {
                column++;
            }
            position++;
        }
    }

    private boolean tryReadLineComment(List<Token> tokens) {
        if (position + 1 < input.length()
            && input.charAt(position) == '-'
            && input.charAt(position + 1) == '-') {
            int startLine = line;
            int startColumn = column;
            StringBuilder sb = new StringBuilder();
            sb.append("--");
            advance();
            advance();
            while (position < input.length() && input.charAt(position) != '\n') {
                sb.append(input.charAt(position));
                advance();
            }
            tokens.add(new Token(TokenType.COMENTARIO_LINEA, sb.toString(), startLine, startColumn, SqlDialect.COMMON));
            return true;
        }
        return false;
    }

    private boolean tryReadBlockComment(List<Token> tokens) {
        if (position + 1 < input.length()
            && input.charAt(position) == '/'
            && input.charAt(position + 1) == '*') {
            int startLine = line;
            int startColumn = column;
            StringBuilder sb = new StringBuilder();
            sb.append("/*");
            advance();
            advance();
            boolean closed = false;
            while (position + 1 < input.length()) {
                if (input.charAt(position) == '*' && input.charAt(position + 1) == '/') {
                    sb.append("*/");
                    advance();
                    advance();
                    closed = true;
                    break;
                }
                if (input.charAt(position) == '\n') {
                    line++;
                    column = 1;
                }
                sb.append(input.charAt(position));
                advance();
            }
            if (!closed) {
                errorCollector.addError(new CompilerError(
                    "LEXICAL",
                    "Comentario multilinea sin cerrar",
                    startLine, startColumn
                ));
            }
            tokens.add(new Token(TokenType.COMENTARIO_BLOQUE, sb.toString(), startLine, startColumn, SqlDialect.COMMON));
            return true;
        }
        return false;
    }

    private void readStringLiteral(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        sb.append('\'');
        advance();
        boolean closed = false;
        while (position < input.length()) {
            if (input.charAt(position) == '\'') {
                sb.append('\'');
                advance();
                if (position < input.length() && input.charAt(position) == '\'') {
                    sb.append('\'');
                    advance();
                } else {
                    closed = true;
                    break;
                }
            } else if (input.charAt(position) == '\n' || input.charAt(position) == '\r') {
                break;
            } else {
                sb.append(input.charAt(position));
                advance();
            }
        }
        if (!closed) {
            errorCollector.addError(new CompilerError(
                "LEXICAL",
                "Cadena sin cerrar",
                startLine, startColumn
            ));
        }
        tokens.add(new Token(TokenType.CADENA, sb.toString(), startLine, startColumn, SqlDialect.COMMON));
    }

    private void readMySqlDelimitedIdentifier(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        advance();
        boolean closed = false;
        while (position < input.length()) {
            if (input.charAt(position) == '`') {
                sb.append('`');
                advance();
                closed = true;
                break;
            }
            if (input.charAt(position) == '\n' || input.charAt(position) == '\r') {
                break;
            }
            sb.append(input.charAt(position));
            advance();
        }
        if (!closed) {
            errorCollector.addError(new CompilerError(
                "LEXICAL",
                "Identificador delimitado MySQL sin cerrar",
                startLine, startColumn
            ));
        }
        foundMySqlDelimiter = true;
        detectedDialect = SqlDialect.MYSQL;
        tokens.add(new Token(TokenType.IDENTIFICADOR_DELIMITADO, sb.toString(), startLine, startColumn, SqlDialect.MYSQL));
    }

    private void readSqlServerDelimitedIdentifier(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        advance();
        boolean closed = false;
        while (position < input.length()) {
            if (input.charAt(position) == ']') {
                sb.append(']');
                advance();
                closed = true;
                break;
            }
            if (input.charAt(position) == '\n' || input.charAt(position) == '\r') {
                break;
            }
            sb.append(input.charAt(position));
            advance();
        }
        if (!closed) {
            errorCollector.addError(new CompilerError(
                "LEXICAL",
                "Identificador delimitado SQL Server sin cerrar",
                startLine, startColumn
            ));
        }
        foundSqlServerDelimiter = true;
        detectedDialect = SqlDialect.SQL_SERVER;
        tokens.add(new Token(TokenType.IDENTIFICADOR_DELIMITADO, sb.toString(), startLine, startColumn, SqlDialect.SQL_SERVER));
    }

    private void readPostgreSqlDelimitedIdentifier(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        advance();
        boolean closed = false;
        while (position < input.length()) {
            if (input.charAt(position) == '"') {
                sb.append('"');
                advance();
                closed = true;
                break;
            }
            if (input.charAt(position) == '\n' || input.charAt(position) == '\r') {
                break;
            }
            sb.append(input.charAt(position));
            advance();
        }
        if (!closed) {
            errorCollector.addError(new CompilerError(
                "LEXICAL",
                "Identificador delimitado PostgreSQL sin cerrar",
                startLine, startColumn
            ));
        }
        foundPostgreSqlDelimiter = true;
        detectedDialect = SqlDialect.POSTGRESQL;
        tokens.add(new Token(TokenType.IDENTIFICADOR_DELIMITADO, sb.toString(), startLine, startColumn, SqlDialect.POSTGRESQL));
    }

    private void readNumber(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        boolean hasDecimal = false;
        boolean malformed = false;

        while (position < input.length() && isDigit(input.charAt(position))) {
            sb.append(input.charAt(position));
            advance();
        }

        if (position < input.length() && input.charAt(position) == '.') {
            if (position + 1 < input.length() && isDigit(input.charAt(position + 1))) {
                hasDecimal = true;
                sb.append('.');
                advance();
                while (position < input.length() && isDigit(input.charAt(position))) {
                    sb.append(input.charAt(position));
                    advance();
                }
            } else {
                malformed = true;
            }
        }

        if (position < input.length() && (input.charAt(position) == 'e' || input.charAt(position) == 'E')) {
            sb.append(input.charAt(position));
            advance();
            if (position < input.length() && (input.charAt(position) == '+' || input.charAt(position) == '-')) {
                sb.append(input.charAt(position));
                advance();
            }
            if (position < input.length() && isDigit(input.charAt(position))) {
                while (position < input.length() && isDigit(input.charAt(position))) {
                    sb.append(input.charAt(position));
                    advance();
                }
            } else {
                malformed = true;
            }
        }

        if (malformed) {
            errorCollector.addError(new CompilerError(
                "LEXICAL",
                "Numero mal formado",
                startLine, startColumn
            ));
        }

        TokenType type = hasDecimal ? TokenType.NUMERO_DECIMAL : TokenType.NUMERO_ENTERO;
        tokens.add(new Token(type, sb.toString(), startLine, startColumn, SqlDialect.COMMON));
    }

    private void readIdentifierOrKeywordOrFunction(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (position < input.length() && (isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            sb.append(input.charAt(position));
            advance();
        }

        String word = sb.toString();
        String wordUpper = word.toUpperCase();

        boolean isKeyword = KeywordRegistry.isKeyword(wordUpper);
        boolean isFunction = false;

        if (position < input.length() && input.charAt(position) == '(') {
            isFunction = FunctionRegistry.isKnownFunction(wordUpper);
        }

        if (isFunction) {
            SqlDialect funcDialect = SqlDialect.COMMON;
            if (FunctionRegistry.isCommonFunction(wordUpper)) {
                funcDialect = SqlDialect.COMMON;
            } else if (FunctionRegistry.isMySqlFunction(wordUpper)) {
                funcDialect = SqlDialect.MYSQL;
                if (detectedDialect == SqlDialect.COMMON) detectedDialect = SqlDialect.MYSQL;
            } else if (FunctionRegistry.isPostgreSqlFunction(wordUpper)) {
                funcDialect = SqlDialect.POSTGRESQL;
                if (detectedDialect == SqlDialect.COMMON) detectedDialect = SqlDialect.POSTGRESQL;
            } else if (FunctionRegistry.isSqlServerFunction(wordUpper)) {
                funcDialect = SqlDialect.SQL_SERVER;
                if (detectedDialect == SqlDialect.COMMON) detectedDialect = SqlDialect.SQL_SERVER;
            }
            tokens.add(new Token(TokenType.FUNCION, word, startLine, startColumn, funcDialect));
            return;
        }

        if (isKeyword) {
            if (isDataTypeKeyword(wordUpper)) {
                tokens.add(new Token(TokenType.TIPO_DATO, word, startLine, startColumn, SqlDialect.COMMON));
            } else if (isLogicalOperator(wordUpper)) {
                tokens.add(new Token(TokenType.OPERADOR_LOGICO, word, startLine, startColumn, SqlDialect.COMMON));
            } else {
                tokens.add(new Token(TokenType.PALABRA_RESERVADA, word, startLine, startColumn, SqlDialect.COMMON));
            }
            return;
        }

        tokens.add(new Token(TokenType.IDENTIFICADOR, word, startLine, startColumn, SqlDialect.COMMON));
    }

    private void readPlaceholder(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();
        sb.append(input.charAt(position));
        advance();
        while (position < input.length() && (isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            sb.append(input.charAt(position));
            advance();
        }
        tokens.add(new Token(TokenType.PLACEHOLDER, sb.toString(), startLine, startColumn, SqlDialect.COMMON));
    }

    private void readOperator(List<Token> tokens) {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        char c1 = input.charAt(position);
        sb.append(c1);
        advance();

        if (position < input.length()) {
            char c2 = input.charAt(position);
            if ((c1 == '<' && c2 == '=') ||
                (c1 == '>' && c2 == '=') ||
                (c1 == '<' && c2 == '>') ||
                (c1 == '!' && c2 == '=') ||
                (c1 == '|' && c2 == '|') ||
                (c1 == ':' && c2 == '=') ||
                (c1 == ':' && c2 == ':')) {
                sb.append(c2);
                advance();
            }
        }

        TokenType type = isComparisonOperator(sb.toString())
            ? TokenType.OPERADOR_COMPARACION
            : TokenType.OPERADOR;
        tokens.add(new Token(type, sb.toString(), startLine, startColumn, SqlDialect.COMMON));
    }

    private boolean isComparisonOperator(String op) {
        return op.equals("=") || op.equals("<") || op.equals(">") ||
               op.equals("<=") || op.equals(">=") || op.equals("<>") ||
               op.equals("!=");
    }

    private boolean isOperatorStart(char c) {
        return "<>=!+-*/%|".indexOf(c) != -1;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isLetterOrDigit(char c) {
        return isLetter(c) || isDigit(c);
    }

    private boolean isDataTypeKeyword(String word) {
        String u = word.toUpperCase();
        return u.equals("INT") || u.equals("INTEGER") || u.equals("VARCHAR") ||
               u.equals("CHAR") || u.equals("BOOLEAN") || u.equals("BOOL") ||
               u.equals("FLOAT") || u.equals("DOUBLE") || u.equals("DECIMAL") ||
               u.equals("NUMERIC") || u.equals("TEXT") || u.equals("BLOB") ||
               u.equals("DATE") || u.equals("TIME") || u.equals("TIMESTAMP") ||
               u.equals("DATETIME") || u.equals("BIGINT") || u.equals("SMALLINT") ||
               u.equals("TINYINT") || u.equals("BYTEA") || u.equals("SERIAL") ||
               u.equals("REAL") || u.equals("ENUM") ||
               u.equals("CHARACTER") || u.equals("VARYING") || u.equals("PRECISION") ||
               u.equals("UNSIGNED") || u.equals("SIGNED") || u.equals("INTERVAL") ||
               u.equals("NCHAR") || u.equals("NVARCHAR") || u.equals("MONEY") ||
               u.equals("SMALLMONEY") || u.equals("UNIQUEIDENTIFIER") ||
               u.equals("IMAGE") || u.equals("NTEXT") || u.equals("XML") ||
               u.equals("JSON") || u.equals("JSONB");
    }

    private boolean isLogicalOperator(String word) {
        String u = word.toUpperCase();
        return u.equals("AND") || u.equals("OR") || u.equals("NOT");
    }

    private void advance() {
        position++;
        column++;
    }
}
