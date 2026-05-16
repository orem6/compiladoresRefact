package com.umg.model.parser;

import com.umg.model.ast.ASTNode;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.Token;
import com.umg.model.lexer.TokenType;

import java.util.List;

public class Parser {
    private final ErrorCollector errorCollector;
    private List<Token> tokens;
    private int position;

    public Parser(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
        this.position = 0;
    }

    public ASTNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        ASTNode node = parseStatement();
        return node;
    }

    private ASTNode parseStatement() {
        if (position >= tokens.size()) return null;
        Token current = tokens.get(position);

        if (current.getType() == TokenType.EOF) return null;

        if (current.getType() != TokenType.KEYWORD) {
            errorCollector.addError(new com.umg.model.error.CompilerError(
                "SYNTAX", "Se esperaba una palabra clave SQL", current.getLine(), current.getColumn()));
            return null;
        }

        String keyword = current.getLexeme().toUpperCase();
        return switch (keyword) {
            case "SELECT" -> parseSelect();
            case "INSERT" -> parseInsert();
            case "UPDATE" -> parseUpdate();
            case "DELETE" -> parseDelete();
            case "CREATE" -> parseCreateTable();
            case "DROP" -> parseDropTable();
            default -> {
                errorCollector.addError(new com.umg.model.error.CompilerError(
                    "SYNTAX", "Sentencia no reconocida: " + keyword, current.getLine(), current.getColumn()));
                yield null;
            }
        };
    }

    private ASTNode parseSelect() {
        consume();
        consumeToken(TokenType.STAR);
        consumeKeyword("FROM");
        consumeToken(TokenType.IDENTIFIER);
        if (checkKeyword("WHERE")) {
            consume();
            parseExpression();
        }
        if (checkKeyword("ORDER")) {
            consume();
            consumeKeyword("BY");
            consumeToken(TokenType.IDENTIFIER);
            if (checkKeyword("ASC") || checkKeyword("DESC")) consume();
        }
        consumeToken(TokenType.SEMICOLON);
        return new com.umg.model.ast.SelectStatement();
    }

    private ASTNode parseInsert() {
        consume();
        consumeKeyword("INTO");
        consumeToken(TokenType.IDENTIFIER);
        consumeToken(TokenType.LPAREN);
        while (checkToken(TokenType.IDENTIFIER)) {
            consume();
            if (checkToken(TokenType.COMMA)) consume();
        }
        consumeToken(TokenType.RPAREN);
        consumeKeyword("VALUES");
        consumeToken(TokenType.LPAREN);
        parseExpression();
        while (checkToken(TokenType.COMMA)) {
            consume();
            parseExpression();
        }
        consumeToken(TokenType.RPAREN);
        consumeToken(TokenType.SEMICOLON);
        return new com.umg.model.ast.InsertStatement();
    }

    private ASTNode parseUpdate() {
        consume();
        consumeToken(TokenType.IDENTIFIER);
        consumeKeyword("SET");
        consumeToken(TokenType.IDENTIFIER);
        consumeToken(TokenType.OPERATOR);
        parseExpression();
        if (checkKeyword("WHERE")) {
            consume();
            parseExpression();
        }
        consumeToken(TokenType.SEMICOLON);
        return new com.umg.model.ast.UpdateStatement();
    }

    private ASTNode parseDelete() {
        consume();
        consumeKeyword("FROM");
        consumeToken(TokenType.IDENTIFIER);
        if (checkKeyword("WHERE")) {
            consume();
            parseExpression();
        }
        consumeToken(TokenType.SEMICOLON);
        return new com.umg.model.ast.DeleteStatement();
    }

    private ASTNode parseCreateTable() {
        consume();
        consumeKeyword("TABLE");
        consumeToken(TokenType.IDENTIFIER);
        consumeToken(TokenType.LPAREN);
        parseColumnDefinition();
        while (checkToken(TokenType.COMMA)) {
            consume();
            parseColumnDefinition();
        }
        consumeToken(TokenType.RPAREN);
        consumeToken(TokenType.SEMICOLON);
        return new com.umg.model.ast.CreateTableStatement();
    }

    private ASTNode parseDropTable() {
        consume();
        consumeKeyword("TABLE");
        consumeToken(TokenType.IDENTIFIER);
        consumeToken(TokenType.SEMICOLON);
        return new com.umg.model.ast.DropTableStatement();
    }

    private void parseColumnDefinition() {
        consumeToken(TokenType.IDENTIFIER);
        consumeToken(TokenType.IDENTIFIER);
    }

    private void parseExpression() {
        consumeToken(TokenType.IDENTIFIER);
        if (checkToken(TokenType.OPERATOR)) {
            consume();
            consumeToken(TokenType.INTEGER);
        }
        while (checkKeyword("AND") || checkKeyword("OR")) {
            consume();
            consumeToken(TokenType.IDENTIFIER);
            consumeToken(TokenType.OPERATOR);
            consumeToken(TokenType.INTEGER);
        }
    }

    private Token consume() {
        Token current = tokens.get(position);
        position++;
        return current;
    }

    private void consumeToken(TokenType expectedType) {
        if (position < tokens.size() && tokens.get(position).getType() == expectedType) {
            position++;
        } else {
            Token t = position < tokens.size() ? tokens.get(position) : null;
            errorCollector.addError(new com.umg.model.error.CompilerError(
                "SYNTAX", "Se esperaba token de tipo " + expectedType,
                t != null ? t.getLine() : -1, t != null ? t.getColumn() : -1));
        }
    }

    private void consumeKeyword(String expected) {
        if (position < tokens.size() && tokens.get(position).getType() == TokenType.KEYWORD
            && tokens.get(position).getLexeme().equalsIgnoreCase(expected)) {
            position++;
        } else {
            Token t = position < tokens.size() ? tokens.get(position) : null;
            errorCollector.addError(new com.umg.model.error.CompilerError(
                "SYNTAX", "Se esperaba palabra clave: " + expected,
                t != null ? t.getLine() : -1, t != null ? t.getColumn() : -1));
        }
    }

    private boolean checkToken(TokenType type) {
        return position < tokens.size() && tokens.get(position).getType() == type;
    }

    private boolean checkKeyword(String keyword) {
        return position < tokens.size() && tokens.get(position).getType() == TokenType.KEYWORD
            && tokens.get(position).getLexeme().equalsIgnoreCase(keyword);
    }
}
