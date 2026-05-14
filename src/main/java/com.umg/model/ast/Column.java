package com.dataquery.sqlcompiler.model.ast;

public class Column extends ASTNode {
    private String name;
    private String dataType;

    public Column() { super("COLUMN"); }
    public Column(String name, String dataType) {
        super("COLUMN");
        this.name = name;
        this.dataType = dataType;
    }

    @Override public String toSqlString() { return name + " " + dataType; }
    public String getName() { return name; }
    public String getDataType() { return dataType; }
}
