package com.umg.model.mongo;

import java.util.ArrayList;
import java.util.List;

public class MongoLexer {

    private final String input;
    private int pos;

    public MongoLexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    public List<MongoToken> tokenize() {
        List<MongoToken> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            switch (c) {
                case '{' -> { tokens.add(new MongoToken(MongoTokenType.LBRACE, "{", pos)); pos++; }
                case '}' -> { tokens.add(new MongoToken(MongoTokenType.RBRACE, "}", pos)); pos++; }
                case '[' -> { tokens.add(new MongoToken(MongoTokenType.LBRACKET, "[", pos)); pos++; }
                case ']' -> { tokens.add(new MongoToken(MongoTokenType.RBRACKET, "]", pos)); pos++; }
                case ':' -> { tokens.add(new MongoToken(MongoTokenType.COLON, ":", pos)); pos++; }
                case ',' -> { tokens.add(new MongoToken(MongoTokenType.COMMA, ",", pos)); pos++; }
                case '"' -> tokens.add(readString());
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        tokens.add(readNumber());
                    } else if (c == '$' || Character.isUnicodeIdentifierStart(c)) {
                        tokens.add(readKeywordOrOperator());
                    } else {
                        tokens.add(new MongoToken(MongoTokenType.INVALID, String.valueOf(c), pos));
                        pos++;
                    }
                }
            }
        }
        tokens.add(new MongoToken(MongoTokenType.EOF, "", pos));
        return tokens;
    }

    private MongoToken readString() {
        int start = pos;
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '\\') {
                pos++;
                if (pos < input.length()) {
                    sb.append(input.charAt(pos));
                    pos++;
                }
            } else if (c == '"') {
                pos++;
                return new MongoToken(MongoTokenType.STRING, sb.toString(), start);
            } else {
                sb.append(c);
                pos++;
            }
        }
        return new MongoToken(MongoTokenType.INVALID, sb.toString(), start);
    }

    private MongoToken readNumber() {
        int start = pos;
        StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '-') {
            sb.append('-');
            pos++;
        }
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        if (pos < input.length() && input.charAt(pos) == '.') {
            sb.append('.');
            pos++;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                sb.append(input.charAt(pos));
                pos++;
            }
        }
        return new MongoToken(MongoTokenType.NUMBER, sb.toString(), start);
    }

    private MongoToken readKeywordOrOperator() {
        int start = pos;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isWhitespace(c) || c == '{' || c == '}' || c == '[' || c == ']' || c == ':' || c == ',') {
                break;
            }
            sb.append(c);
            pos++;
        }
        String word = sb.toString();
        return switch (word) {
            case "true" -> new MongoToken(MongoTokenType.TRUE, word, start);
            case "false" -> new MongoToken(MongoTokenType.FALSE, word, start);
            case "null" -> new MongoToken(MongoTokenType.NULL, word, start);
            default -> {
                if (word.startsWith("$")) {
                    yield new MongoToken(MongoTokenType.OPERATOR, word, start);
                }
                yield new MongoToken(MongoTokenType.FIELD_PATH, word, start);
            }
        };
    }
}
