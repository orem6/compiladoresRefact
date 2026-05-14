package com.dataquery.sqlcompiler.model.ast;

public class InsertStatement extends ASTNode {
    public InsertStatement() { super("INSERT"); }
    @Override public String toSqlString() { return "INSERT INTO ... VALUES ..."; }
}
