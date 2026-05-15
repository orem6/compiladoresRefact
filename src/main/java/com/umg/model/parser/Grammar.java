package com.umg.model.parser;

public class Grammar {
    public static final String SQL_GRAMMAR =
        "statement   : selectStmt | insertStmt | updateStmt | deleteStmt | createStmt | dropStmt\n" +
        "selectStmt  : SELECT columnList FROM tableName [WHERE condition] [ORDER BY column [ASC|DESC]] ;\n" +
        "insertStmt  : INSERT INTO tableName (columnList) VALUES (valueList) ;\n" +
        "updateStmt  : UPDATE tableName SET column = value [WHERE condition] ;\n" +
        "deleteStmt  : DELETE FROM tableName [WHERE condition] ;\n" +
        "createStmt  : CREATE TABLE tableName (columnDef [, columnDef]*) ;\n" +
        "dropStmt    : DROP TABLE tableName ;\n" +
        "columnList  : * | column [, column]*\n" +
        "condition   : expression (AND|OR expression)*\n" +
        "expression  : identifier operator value\n";

    public static boolean isValidStatement(String statementType) {
        return switch (statementType.toUpperCase()) {
            case "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP" -> true;
            default -> false;
        };
    }
}
