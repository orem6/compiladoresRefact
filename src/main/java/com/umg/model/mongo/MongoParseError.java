package com.umg.model.mongo;

public class MongoParseError {
    private final String code;
    private final String message;
    private final int position;

    public MongoParseError(String code, String message, int position) {
        this.code = code;
        this.message = message;
        this.position = position;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public int getPosition() { return position; }
}
