package com.umg.model.nosql.mongodb;

import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlSyntaxError;
import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;

import java.util.*;

public class MongoParser {

    private static final Set<String> SUPPORTED_METHODS = Set.of(
        "find", "findOne", "insertOne", "insertMany",
        "updateOne", "updateMany", "deleteOne", "deleteMany",
        "aggregate"
    );

    private static final Map<String, Integer> METHOD_MIN_ARGS = Map.of(
        "find", 1, "findOne", 1, "insertOne", 1, "insertMany", 1,
        "updateOne", 2, "updateMany", 2,
        "deleteOne", 1, "deleteMany", 1,
        "aggregate", 1
    );

    private static final Map<String, String> METHOD_TYPE_MAP = Map.of(
        "find", "MONGODB_FIND", "findOne", "MONGODB_FIND_ONE",
        "insertOne", "MONGODB_INSERT_ONE", "insertMany", "MONGODB_INSERT_MANY",
        "updateOne", "MONGODB_UPDATE_ONE", "updateMany", "MONGODB_UPDATE_MANY",
        "deleteOne", "MONGODB_DELETE_ONE", "deleteMany", "MONGODB_DELETE_MANY",
        "aggregate", "MONGODB_AGGREGATE"
    );

    private List<NoSqlToken> tokens;
    private int pos;
    private final NoSqlAnalysisResult result;
    private final Deque<Character> braceStack;

    public MongoParser() {
        this.result = new NoSqlAnalysisResult();
        this.braceStack = new ArrayDeque<>();
    }

    public NoSqlAnalysisResult parse(List<NoSqlToken> inputTokens) {
        this.tokens = inputTokens;
        this.pos = 0;
        this.braceStack.clear();
        result.getSyntaxErrors().clear();
        result.getDetectedClauses().clear();
        result.setSyntaxValid(true);
        result.setStatementType("UNKNOWN");

        if (tokens.isEmpty()) {
            addError("MONGO_EXPECTED_DB_PREFIX", "Se esperaba 'db' al inicio.", 0, 0, "");
            return result;
        }

        NoSqlToken first = advance();
        if (!first.getLexeme().equals("db")) {
            addError("MONGO_EXPECTED_DB_PREFIX", "La expresion debe iniciar con 'db'.", first);
            return result;
        }
        result.addDetectedClause("DB");

        expectToken(NoSqlTokenType.DOT, "MONGO_EXPECTED_COLLECTION", "Se esperaba '.' despues de 'db'.");
        result.addDetectedClause("COLLECTION");

        NoSqlToken collectionToken = advance();
        if (collectionToken.getType() != NoSqlTokenType.IDENTIFIER) {
            addError("MONGO_EXPECTED_COLLECTION", "Se esperaba nombre de coleccion.", collectionToken);
            return result;
        }

        expectToken(NoSqlTokenType.DOT, "MONGO_EXPECTED_METHOD", "Se esperaba '.' despues del nombre de coleccion.");

        NoSqlToken methodToken = advance();
        if (methodToken.getType() != NoSqlTokenType.KEYWORD || !SUPPORTED_METHODS.contains(methodToken.getLexeme())) {
            addError("MONGO_UNSUPPORTED_METHOD",
                "Metodo MongoDB no soportado: " + methodToken.getLexeme() + ". Soportados: " + SUPPORTED_METHODS, methodToken);
            return result;
        }
        String methodName = methodToken.getLexeme();
        result.setStatementType(METHOD_TYPE_MAP.getOrDefault(methodName, "MONGODB_" + methodName.toUpperCase()));
        result.addDetectedClause(methodName.toUpperCase());

        expectToken(NoSqlTokenType.LEFT_PAREN, "MONGO_EXPECTED_LEFT_PAREN", "Se esperaba '(' despues del metodo.");

        int argsParsed = parseArguments();
        Integer expectedMin = METHOD_MIN_ARGS.get(methodName);
        if (expectedMin != null && argsParsed < expectedMin) {
            addError("MONGO_INVALID_ARGUMENT_COUNT",
                "El metodo " + methodName + " requiere al menos " + expectedMin + " argumento(s).", methodToken);
        }

        if (pos < tokens.size() && current().getType() == NoSqlTokenType.RIGHT_PAREN) {
            advance();
        }

        if (pos < tokens.size() && current().getType() == NoSqlTokenType.SEMICOLON) {
            advance();
        }

        if (!braceStack.isEmpty()) {
            addError("MONGO_UNBALANCED_BRACES",
                "Llaves/corchetes/parentesis desbalanceados. Faltan " + braceStack.size() + " cierres.",
                tokens.get(tokens.size() - 1));
        }

        return result;
    }

    private int parseArguments() {
        int count = 0;
        while (pos < tokens.size() && current().getType() != NoSqlTokenType.RIGHT_PAREN
            && current().getType() != NoSqlTokenType.EOF) {
            if (current().getType() == NoSqlTokenType.LEFT_BRACE) {
                parseObject();
                count++;
            } else if (current().getType() == NoSqlTokenType.LEFT_BRACKET) {
                parseArray();
                count++;
            } else {
                break;
            }
            if (pos < tokens.size() && current().getType() == NoSqlTokenType.COMMA) {
                advance();
            }
        }
        return count;
    }

    private void parseObject() {
        if (current().getType() != NoSqlTokenType.LEFT_BRACE) return;
        braceStack.push('{');
        advance();
        boolean first = true;
        while (pos < tokens.size() && current().getType() != NoSqlTokenType.RIGHT_BRACE
            && current().getType() != NoSqlTokenType.EOF) {
            if (!first) {
                if (current().getType() == NoSqlTokenType.COMMA) {
                    advance();
                } else {
                    addError("MONGO_INVALID_OBJECT_SYNTAX",
                        "Se esperaba ',' entre pares del objeto.", current());
                    break;
                }
            }
            first = false;
            if (current().getType() != NoSqlTokenType.IDENTIFIER && current().getType() != NoSqlTokenType.STRING
                && current().getType() != NoSqlTokenType.MONGO_OPERATOR) {
                addError("MONGO_INVALID_OBJECT_SYNTAX",
                    "Se esperaba clave en el objeto.", current());
                break;
            }
            advance();
            if (current().getType() != NoSqlTokenType.COLON) {
                addError("MONGO_INVALID_OBJECT_SYNTAX",
                    "Se esperaba ':' despues de la clave.", current());
                break;
            }
            advance();
            parseValue();
        }
        if (pos < tokens.size() && current().getType() == NoSqlTokenType.RIGHT_BRACE) {
            advance();
            if (!braceStack.isEmpty() && braceStack.peek() == '{') braceStack.pop();
        }
    }

    private void parseArray() {
        if (current().getType() != NoSqlTokenType.LEFT_BRACKET) return;
        braceStack.push('[');
        advance();
        boolean first = true;
        while (pos < tokens.size() && current().getType() != NoSqlTokenType.RIGHT_BRACKET
            && current().getType() != NoSqlTokenType.EOF) {
            if (!first) {
                if (current().getType() == NoSqlTokenType.COMMA) {
                    advance();
                } else {
                    addError("MONGO_INVALID_ARRAY_SYNTAX",
                        "Se esperaba ',' entre elementos del arreglo.", current());
                    break;
                }
            }
            first = false;
            parseValue();
        }
        if (pos < tokens.size() && current().getType() == NoSqlTokenType.RIGHT_BRACKET) {
            advance();
            if (!braceStack.isEmpty() && braceStack.peek() == '[') braceStack.pop();
        }
    }

    private void parseValue() {
        if (pos >= tokens.size()) return;
        NoSqlTokenType type = current().getType();
        switch (type) {
            case STRING, NUMBER, BOOLEAN, NULL, IDENTIFIER -> advance();
            case MONGO_OPERATOR -> advance();
            case LEFT_BRACE -> parseObject();
            case LEFT_BRACKET -> parseArray();
            default -> {
                addError("MONGO_INVALID_VALUE", "Valor invalido en la expresion.", current());
                advance();
            }
        }
    }

    private NoSqlToken advance() {
        if (pos < tokens.size()) return tokens.get(pos++);
        return new NoSqlToken(NoSqlTokenType.EOF, "", 0, 0);
    }

    private NoSqlToken current() {
        if (pos < tokens.size()) return tokens.get(pos);
        return new NoSqlToken(NoSqlTokenType.EOF, "", 0, 0);
    }

    private void expectToken(NoSqlTokenType expectedType, String errorCode, String message) {
        if (pos < tokens.size() && tokens.get(pos).getType() == expectedType) {
            advance();
        } else {
            addError(errorCode, message, current());
        }
    }

    private void addError(String code, String message, NoSqlToken token) {
        result.addSyntaxError(new NoSqlSyntaxError(code, message, token.getLine(), token.getColumn(), token.getLexeme(), "SYNTAX"));
    }

    private void addError(String code, String message, int line, int col, String lexeme) {
        result.addSyntaxError(new NoSqlSyntaxError(code, message, line, col, lexeme, "SYNTAX"));
    }
}
