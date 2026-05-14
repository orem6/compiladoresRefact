package com.dataquery.sqlcompiler.model.ast;

public class UpdateStatement extends ASTNode {
    public UpdateStatement() { super("UPDATE"); }
    @Override public String toSqlString() { return "UPDATE ... SET ..."; }
}
