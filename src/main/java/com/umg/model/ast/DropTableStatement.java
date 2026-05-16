package com.umg.model.ast;

public class DropTableStatement extends ASTNode {
    public DropTableStatement() { super("DROP_TABLE"); }
    @Override public String toSqlString() { return "DROP TABLE ...;"; }
}
