package com.umg.model.mongo;

public class MongoToken {
    private final MongoTokenType type;
    private final String lexeme;
    private final int position;

    public MongoToken(MongoTokenType type, String lexeme, int position) {
        this.type = type;
        this.lexeme = lexeme;
        this.position = position;
    }

    public MongoTokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public int getPosition() { return position; }

    @Override
    public String toString() {
        return "MongoToken{" + type + ": '" + lexeme + "' @" + position + "}";
    }
}
