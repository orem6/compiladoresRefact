package com.umg.model.parser;

import com.umg.model.ast.*;
import com.umg.model.error.CompilerError;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.Token;
import com.umg.model.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class CqlParser {
    private final ErrorCollector errorCollector;
    private List<Token> tokens;
    private int position;

    public CqlParser(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
        this.position = 0;
    }

    public ASTNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        if (tokens == null || tokens.isEmpty()) return null;
        return parseStatement();
    }

    private ASTNode parseStatement() {
        if (position >= tokens.size()) return null;
        skipComments();
        Token current = tokens.get(position);

        if (current.getType() == TokenType.EOF || current.getType() == TokenType.EOF_OLD) return null;

        if (current.getType() != TokenType.PALABRA_RESERVADA &&
            current.getType() != TokenType.KEYWORD &&
            current.getType() != TokenType.IDENTIFICADOR &&
            current.getType() != TokenType.IDENTIFIER) {
            addError("Se esperaba una palabra clave CQL al inicio de la sentencia", current);
            return null;
        }

        String keyword = current.getLexeme().toUpperCase();
        return switch (keyword) {
            case "SELECT" -> parseSelect();
            case "INSERT" -> parseInsert();
            case "UPDATE" -> parseUpdate();
            case "DELETE" -> parseDelete();
            case "CREATE" -> parseCreate();
            case "ALTER" -> parseAlter();
            case "DROP" -> parseDrop();
            case "TRUNCATE" -> parseTruncate();
            default -> {
                addError("Sentencia CQL no reconocida: " + keyword, current);
                yield null;
            }
        };
    }

    private ASTNode parseSelect() {
        consume();
        SelectStatement stmt = new SelectStatement();

        if (checkKeyword("JSON")) {
            consume();
        }

        if (checkTokenType(TokenType.ASTERISCO) || checkTokenType(TokenType.STAR)) {
            consume();
        } else if (checkKeyword("DISTINCT")) {
            consume();
            parseColumnsUntilFrom();
        } else {
            parseColumnsUntilFrom();
        }

        if (checkKeyword("FROM")) {
            consume();
            if (position < tokens.size()) {
                consume();
            }
        }

        if (checkKeyword("WHERE")) {
            consume();
            parseWhereClause();
        }

        if (checkKeyword("GROUP") && peekKeyword("BY")) {
            consume();
            consume();
            while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
                if (peekKeyword("ORDER") || peekKeyword("LIMIT") || peekKeyword("ALLOW") || peekKeyword("PER")) break;
                consume();
                if (checkTokenType(TokenType.COMA) || checkTokenType(TokenType.COMMA)) consume();
            }
        }

        if (checkKeyword("ORDER") && peekKeyword("BY")) {
            consume();
            consume();
            while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
                if (peekKeyword("LIMIT") || peekKeyword("ALLOW") || peekKeyword("PER")) break;
                consume();
                if (checkKeyword("ASC") || checkKeyword("DESC")) consume();
                if (checkTokenType(TokenType.COMA) || checkTokenType(TokenType.COMMA)) consume();
            }
        }

        if (checkKeyword("PER") && peekKeyword("PARTITION")) {
            consume();
            consume();
            if (peekKeyword("LIMIT")) consume();
            if (position < tokens.size()) consume();
        }

        if (checkKeyword("LIMIT")) {
            consume();
            if (position < tokens.size()) consume();
        }

        if (checkKeyword("ALLOW") && peekKeyword("FILTERING")) {
            consume();
            consume();
        }

        return stmt;
    }

    private void parseColumnsUntilFrom() {
        while (position < tokens.size() && !checkKeyword("FROM") && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
            consume();
            if (checkTokenType(TokenType.COMA) || checkTokenType(TokenType.COMMA)) consume();
        }
    }

    private void parseWhereClause() {
        while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
            if (peekKeyword("ALLOW") || peekKeyword("ORDER") || peekKeyword("LIMIT") || peekKeyword("PER") || peekKeyword("GROUP")) break;
            if (peekKeyword("AND") || peekKeyword("OR")) consume();
            consume();
            if (checkTokenType(TokenType.OPERADOR_COMPARACION) || checkTokenType(TokenType.OPERADOR) || checkTokenType(TokenType.OPERATOR)) consume();
            consumeValue();
        }
    }

    private void consumeValue() {
        if (position >= tokens.size()) return;
        Token t = tokens.get(position);
        if (t.getType() == TokenType.CADENA || t.getType() == TokenType.STRING_LITERAL ||
            t.getType() == TokenType.NUMERO_ENTERO || t.getType() == TokenType.INTEGER ||
            t.getType() == TokenType.NUMERO_DECIMAL || t.getType() == TokenType.DECIMAL ||
            t.getType() == TokenType.IDENTIFICADOR || t.getType() == TokenType.IDENTIFIER ||
            t.getType() == TokenType.PALABRA_RESERVADA || t.getType() == TokenType.KEYWORD) {
            consume();
        }
    }

    private ASTNode parseInsert() {
        consume();
        InsertStatement stmt = new InsertStatement();

        if (checkKeyword("INTO")) consume();

        if (position < tokens.size()) {
            consume();
        }

        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO) || checkTokenType(TokenType.LPAREN)) {
            consume();
            while (!checkTokenType(TokenType.PARENTESIS_DERECHO) && !checkTokenType(TokenType.RPAREN) && position < tokens.size()) {
                consume();
                if (checkTokenType(TokenType.COMA) || checkTokenType(TokenType.COMMA)) consume();
            }
            if (checkTokenType(TokenType.PARENTESIS_DERECHO) || checkTokenType(TokenType.RPAREN)) consume();
        }

        if (checkKeyword("VALUES")) {
            consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO) || checkTokenType(TokenType.LPAREN)) {
                consume();
                while (!checkTokenType(TokenType.PARENTESIS_DERECHO) && !checkTokenType(TokenType.RPAREN) && position < tokens.size()) {
                    consume();
                    if (checkTokenType(TokenType.COMA) || checkTokenType(TokenType.COMMA)) consume();
                }
                if (checkTokenType(TokenType.PARENTESIS_DERECHO) || checkTokenType(TokenType.RPAREN)) consume();
            }
        }

        if (checkKeyword("USING")) {
            consume();
            if (checkKeyword("TTL")) {
                consume();
                if (position < tokens.size()) consume();
            } else if (checkKeyword("TIMESTAMP")) {
                consume();
                if (position < tokens.size()) consume();
            }
        }

        return stmt;
    }

    private ASTNode parseUpdate() {
        consume();
        UpdateStatement stmt = new UpdateStatement();

        if (checkKeyword("USING")) {
            consume();
            if (checkKeyword("TTL")) {
                consume();
                if (position < tokens.size()) consume();
            } else if (checkKeyword("TIMESTAMP")) {
                consume();
                if (position < tokens.size()) consume();
            }
        }

        if (position < tokens.size()) {
            consume();
        }

        if (checkKeyword("SET")) {
            consume();
            while (position < tokens.size() && !checkKeyword("WHERE") && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
                consume();
                if (checkTokenType(TokenType.OPERADOR_COMPARACION) || checkTokenType(TokenType.OPERADOR) || checkTokenType(TokenType.OPERATOR)) consume();
                consumeValue();
                if (checkTokenType(TokenType.COMA) || checkTokenType(TokenType.COMMA)) consume();
            }
        }

        if (checkKeyword("WHERE")) {
            consume();
            parseWhereClause();
        }

        return stmt;
    }

    private ASTNode parseDelete() {
        consume();
        DeleteStatement stmt = new DeleteStatement();

        if (checkKeyword("FROM")) consume();

        if (position < tokens.size()) {
            consume();
        }

        if (checkKeyword("USING")) {
            consume();
            if (checkKeyword("TIMESTAMP")) {
                consume();
                if (position < tokens.size()) consume();
            }
        }

        if (checkKeyword("WHERE")) {
            consume();
            parseWhereClause();
        }

        return stmt;
    }

    private ASTNode parseCreate() {
        consume();
        if (checkKeyword("KEYSPACE")) {
            return parseCreateKeyspace();
        }
        if (checkKeyword("TABLE")) {
            return parseCreateTable();
        }
        if (checkKeyword("TYPE")) {
            return parseCreateType();
        }
        if (checkKeyword("INDEX")) {
            return parseCreateIndex();
        }
        if (checkKeyword("MATERIALIZED") && peekKeyword("VIEW")) {
            consume();
            consume();
            return parseCreateMaterializedView();
        }
        addError("CREATE debe ser KEYSPACE, TABLE, TYPE, INDEX o MATERIALIZED VIEW", null);
        return null;
    }

    private ASTNode parseCreateKeyspace() {
        if (checkKeyword("IF") && peekKeyword("NOT")) {
            consume();
            if (peekKeyword("EXISTS")) consume();
            consume();
        } else if (checkKeyword("IF") && peekKeyword("EXISTS")) {
            consume();
            consume();
        }

        if (position < tokens.size()) {
            consume();
        }

        if (checkKeyword("WITH")) {
            consume();
            parseReplication();
        }

        if (checkKeyword("AND")) {
            consume();
            if (checkKeyword("DURABLE") && peekKeyword("WRITES")) {
                consume();
                consume();
                if (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON)) {
                    consume();
                }
            }
        }

        return new CreateTableStatement();
    }

    private void parseReplication() {
        if (checkKeyword("REPLICATION")) {
            consume();
            if (checkTokenType(TokenType.OPERADOR_COMPARACION) || checkTokenType(TokenType.OPERADOR) || checkTokenType(TokenType.OPERATOR)) consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO) || checkTokenType(TokenType.LPAREN)) {
                int depth = 1;
                consume();
                while (depth > 0 && position < tokens.size()) {
                    Token t = tokens.get(position);
                    if (t.getType() == TokenType.PARENTESIS_IZQUIERDO || t.getType() == TokenType.LPAREN) depth++;
                    if (t.getType() == TokenType.PARENTESIS_DERECHO || t.getType() == TokenType.RPAREN) depth--;
                    consume();
                }
            }
        }
    }

    private ASTNode parseCreateTable() {
        if (checkKeyword("IF") && peekKeyword("NOT")) {
            consume();
            if (peekKeyword("EXISTS")) consume();
            consume();
        } else if (checkKeyword("IF") && peekKeyword("EXISTS")) {
            consume();
            consume();
        }

        if (position < tokens.size()) {
            consume();
        }

        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO) || checkTokenType(TokenType.LPAREN)) {
            consume();
            int depth = 1;
            while (depth > 0 && position < tokens.size()) {
                Token t = tokens.get(position);
                if (t.getType() == TokenType.PARENTESIS_IZQUIERDO || t.getType() == TokenType.LPAREN) depth++;
                if (t.getType() == TokenType.PARENTESIS_DERECHO || t.getType() == TokenType.RPAREN) depth--;
                consume();
            }
        }

        if (checkKeyword("WITH")) {
            consume();
            while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
                consume();
                if (checkTokenType(TokenType.OPERADOR_COMPARACION) || checkTokenType(TokenType.OPERADOR) || checkTokenType(TokenType.OPERATOR)) consume();
                if (checkTokenType(TokenType.COMA) || checkTokenType(TokenType.COMMA)) break;
            }
        }

        return new CreateTableStatement();
    }

    private ASTNode parseCreateType() {
        if (checkKeyword("IF") && peekKeyword("NOT")) {
            consume();
            if (peekKeyword("EXISTS")) consume();
            consume();
        } else if (checkKeyword("IF") && peekKeyword("EXISTS")) {
            consume();
            consume();
        }
        if (position < tokens.size()) consume();
        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO) || checkTokenType(TokenType.LPAREN)) {
            int depth = 1;
            consume();
            while (depth > 0 && position < tokens.size()) {
                Token t = tokens.get(position);
                if (t.getType() == TokenType.PARENTESIS_IZQUIERDO || t.getType() == TokenType.LPAREN) depth++;
                if (t.getType() == TokenType.PARENTESIS_DERECHO || t.getType() == TokenType.RPAREN) depth--;
                consume();
            }
        }
        return new CreateTableStatement();
    }

    private ASTNode parseCreateIndex() {
        if (checkKeyword("IF") && peekKeyword("NOT")) {
            consume();
            if (peekKeyword("EXISTS")) consume();
            consume();
        } else if (checkKeyword("IF") && peekKeyword("EXISTS")) {
            consume();
            consume();
        }
        if (checkKeyword("ON")) {
            consume();
            if (position < tokens.size()) consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO) || checkTokenType(TokenType.LPAREN)) {
                int depth = 1;
                consume();
                while (depth > 0 && position < tokens.size()) {
                    Token t = tokens.get(position);
                    if (t.getType() == TokenType.PARENTESIS_IZQUIERDO || t.getType() == TokenType.LPAREN) depth++;
                    if (t.getType() == TokenType.PARENTESIS_DERECHO || t.getType() == TokenType.RPAREN) depth--;
                    consume();
                }
            }
        }
        return new CreateTableStatement();
    }

    private ASTNode parseCreateMaterializedView() {
        if (checkKeyword("IF") && peekKeyword("NOT")) {
            consume();
            if (peekKeyword("EXISTS")) consume();
            consume();
        } else if (checkKeyword("IF") && peekKeyword("EXISTS")) {
            consume();
            consume();
        }
        if (position < tokens.size()) consume();
        if (checkKeyword("AS")) {
            consume();
            while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
                consume();
            }
        }
        return new CreateTableStatement();
    }

    private ASTNode parseAlter() {
        consume();
        if (checkKeyword("TABLE")) {
            consume();
            if (position < tokens.size()) consume();
            while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON) && !checkTokenType(TokenType.EOF) && !checkTokenType(TokenType.EOF_OLD)) {
                consume();
            }
        }
        return new CreateTableStatement();
    }

    private ASTNode parseDrop() {
        consume();
        if (checkKeyword("TABLE") || checkKeyword("KEYSPACE") || checkKeyword("INDEX") || checkKeyword("TYPE") || checkKeyword("MATERIALIZED") || checkKeyword("VIEW") || checkKeyword("ROLE")) {
            consume();
        }
        if (checkKeyword("IF") && peekKeyword("EXISTS")) {
            consume();
            consume();
        }
        if (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.SEMICOLON)) {
            consume();
        }
        return new DropTableStatement();
    }

    private ASTNode parseTruncate() {
        consume();
        if (checkKeyword("TABLE")) consume();
        if (position < tokens.size()) consume();
        return new DropTableStatement();
    }

    private Token consume() {
        if (position >= tokens.size()) {
            return null;
        }
        Token t = tokens.get(position);
        position++;
        skipComments();
        return t;
    }

    private boolean checkTokenType(TokenType type) {
        if (position >= tokens.size()) return false;
        return tokens.get(position).getType() == type;
    }

    private boolean checkKeyword(String keyword) {
        if (position >= tokens.size()) return false;
        Token t = tokens.get(position);
        return (t.getType() == TokenType.PALABRA_RESERVADA || t.getType() == TokenType.KEYWORD || t.getType() == TokenType.IDENTIFICADOR || t.getType() == TokenType.IDENTIFIER)
            && t.getLexeme().equalsIgnoreCase(keyword);
    }

    private boolean peekKeyword(String keyword) {
        int next = position;
        while (next < tokens.size() && (tokens.get(next).getType() == TokenType.COMENTARIO_LINEA || tokens.get(next).getType() == TokenType.COMENTARIO_BLOQUE)) {
            next++;
        }
        if (next >= tokens.size()) return false;
        Token t = tokens.get(next);
        return (t.getType() == TokenType.PALABRA_RESERVADA || t.getType() == TokenType.KEYWORD || t.getType() == TokenType.IDENTIFICADOR || t.getType() == TokenType.IDENTIFIER)
            && t.getLexeme().equalsIgnoreCase(keyword);
    }

    private void skipComments() {
        while (position < tokens.size()) {
            Token t = tokens.get(position);
            if (t.getType() == TokenType.COMENTARIO_LINEA || t.getType() == TokenType.COMENTARIO_BLOQUE) {
                position++;
            } else {
                break;
            }
        }
    }

    private void addError(String message, Token token) {
        errorCollector.addError(new CompilerError(
            "SYNTAX",
            message,
            token != null ? token.getLinea() : 0,
            token != null ? token.getColumna() : 0
        ));
    }
}
