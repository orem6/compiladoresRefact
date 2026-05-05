package com.dataquery.sqlcompiler.model.ast;

public abstract class ASTNode {
    protected String nodeType;

    public ASTNode(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public abstract String toSqlString();
}
