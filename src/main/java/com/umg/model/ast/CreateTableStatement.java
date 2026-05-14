package com.umg.model.ast;

public class CreateTableStatement extends ASTNode {
    public CreateTableStatement() { super("CREATE_TABLE"); }
    @Override public String toSqlString() { return "CREATE TABLE ... (...);"; }
}
