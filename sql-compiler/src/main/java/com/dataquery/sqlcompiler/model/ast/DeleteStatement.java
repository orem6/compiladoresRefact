package com.dataquery.sqlcompiler.model.ast;

public class DeleteStatement extends ASTNode {
    public DeleteStatement() { super("DELETE"); }
    @Override public String toSqlString() { return "DELETE FROM ..."; }
}
