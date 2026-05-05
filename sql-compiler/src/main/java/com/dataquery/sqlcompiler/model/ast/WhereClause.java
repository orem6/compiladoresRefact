package com.dataquery.sqlcompiler.model.ast;

import java.util.ArrayList;
import java.util.List;

public class WhereClause extends ASTNode {
    private List<Expression> conditions;

    public WhereClause() {
        super("WHERE");
        this.conditions = new ArrayList<>();
    }

    @Override public String toSqlString() {
        StringBuilder sb = new StringBuilder("WHERE ");
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) sb.append(" AND ");
            sb.append(conditions.get(i).toSqlString());
        }
        return sb.toString();
    }

    public List<Expression> getConditions() { return conditions; }
    public void addCondition(Expression condition) { conditions.add(condition); }
}
