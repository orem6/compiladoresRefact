package com.dataquery.sqlcompiler.model.ast;

public class JoinClause extends ASTNode {
    private String joinType;
    private String tableName;
    private Expression onCondition;

    public JoinClause() { super("JOIN"); }
    public JoinClause(String joinType, String tableName, Expression onCondition) {
        super("JOIN");
        this.joinType = joinType;
        this.tableName = tableName;
        this.onCondition = onCondition;
    }

    @Override public String toSqlString() {
        return joinType + " JOIN " + tableName + " ON " + onCondition.toSqlString();
    }

    public String getJoinType() { return joinType; }
    public String getTableName() { return tableName; }
    public Expression getOnCondition() { return onCondition; }
}
