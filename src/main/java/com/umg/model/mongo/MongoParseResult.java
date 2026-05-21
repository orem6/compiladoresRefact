package com.umg.model.mongo;

import java.util.ArrayList;
import java.util.List;

public class MongoParseResult {
    private boolean valid;
    private String message;
    private List<MongoParseError> errors;
    private List<MongoToken> tokens;
    private List<String> detectedStages;
    private List<String> detectedCollections;

    public MongoParseResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
        this.tokens = new ArrayList<>();
        this.detectedStages = new ArrayList<>();
        this.detectedCollections = new ArrayList<>();
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<MongoParseError> getErrors() { return errors; }
    public void setErrors(List<MongoParseError> errors) { this.errors = errors; }
    public void addError(MongoParseError error) { this.errors.add(error); }

    public List<MongoToken> getTokens() { return tokens; }
    public void setTokens(List<MongoToken> tokens) { this.tokens = tokens; }

    public List<String> getDetectedStages() { return detectedStages; }
    public void setDetectedStages(List<String> detectedStages) { this.detectedStages = detectedStages; }

    public List<String> getDetectedCollections() { return detectedCollections; }
    public void setDetectedCollections(List<String> detectedCollections) { this.detectedCollections = detectedCollections; }
}
