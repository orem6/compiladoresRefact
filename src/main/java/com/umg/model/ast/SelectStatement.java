package com.umg.model.ast;

public class SelectStatement extends ASTNode {
    public SelectStatement() {
        super("SELECT");
    }

    @Override
    public String toSqlString() {
        return "SELECT * FROM ...";
    }
}
