package com.dataquery.sqlcompiler.model.ast;

public class Table extends ASTNode {
    private String name;

    public Table() { super("TABLE"); }
    public Table(String name) {
        super("TABLE");
        this.name = name;
    }

    @Override public String toSqlString() { return name; }
    public String getName() { return name; }
}
