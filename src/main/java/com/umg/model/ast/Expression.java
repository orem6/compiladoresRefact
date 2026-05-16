package com.umg.model.ast;

public class Expression extends ASTNode {
    private String leftOperand;
    private String operator;
    private String rightOperand;

    public Expression() { super("EXPRESSION"); }
    public Expression(String left, String op, String right) {
        super("EXPRESSION");
        this.leftOperand = left;
        this.operator = op;
        this.rightOperand = right;
    }

    @Override public String toSqlString() { return leftOperand + " " + operator + " " + rightOperand; }

    public String getLeftOperand() { return leftOperand; }
    public void setLeftOperand(String leftOperand) { this.leftOperand = leftOperand; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRightOperand() { return rightOperand; }
    public void setRightOperand(String rightOperand) { this.rightOperand = rightOperand; }
}
