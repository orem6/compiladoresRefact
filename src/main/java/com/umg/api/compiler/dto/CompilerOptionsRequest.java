package com.umg.api.compiler.dto;

public class CompilerOptionsRequest {

    private Boolean includeCommentsAsTokens;
    private Boolean stopOnLexicalError;
    private Boolean stopOnSyntaxError;
    private Boolean returnTokenList;
    private Boolean returnConsoleOutput;

    public CompilerOptionsRequest() {
    }

    public boolean isIncludeCommentsAsTokens() {
        return includeCommentsAsTokens != null ? includeCommentsAsTokens : true;
    }

    public boolean isStopOnLexicalError() {
        return stopOnLexicalError != null ? stopOnLexicalError : true;
    }

    public boolean isStopOnSyntaxError() {
        return stopOnSyntaxError != null ? stopOnSyntaxError : true;
    }

    public boolean isReturnTokenList() {
        return returnTokenList != null ? returnTokenList : true;
    }

    public boolean isReturnConsoleOutput() {
        return returnConsoleOutput != null ? returnConsoleOutput : true;
    }

    public void setIncludeCommentsAsTokens(Boolean includeCommentsAsTokens) {
        this.includeCommentsAsTokens = includeCommentsAsTokens;
    }

    public void setStopOnLexicalError(Boolean stopOnLexicalError) {
        this.stopOnLexicalError = stopOnLexicalError;
    }

    public void setStopOnSyntaxError(Boolean stopOnSyntaxError) {
        this.stopOnSyntaxError = stopOnSyntaxError;
    }

    public void setReturnTokenList(Boolean returnTokenList) {
        this.returnTokenList = returnTokenList;
    }

    public void setReturnConsoleOutput(Boolean returnConsoleOutput) {
        this.returnConsoleOutput = returnConsoleOutput;
    }
}
