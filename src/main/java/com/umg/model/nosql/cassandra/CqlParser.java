package com.umg.model.nosql.cassandra;

import com.umg.model.nosql.common.NoSqlAnalysisResult;
import com.umg.model.nosql.common.NoSqlSyntaxError;
import com.umg.model.nosql.common.NoSqlToken;
import com.umg.model.nosql.common.NoSqlTokenType;

import java.util.*;

public class CqlParser {

    private List<NoSqlToken> tokens;
    private int pos;
    private final NoSqlAnalysisResult result;

    public CqlParser() {
        this.result = new NoSqlAnalysisResult();
    }

    public NoSqlAnalysisResult parse(List<NoSqlToken> inputTokens) {
        this.tokens = inputTokens;
        this.pos = 0;
        result.getSyntaxErrors().clear();
        result.getDetectedClauses().clear();
        result.setSyntaxValid(true);
        result.setStatementType("UNKNOWN");

        if (tokens.isEmpty()) {
            addError("CQL_UNSUPPORTED_STATEMENT", "Sentencia CQL vacia.", 0, 0, "");
            return result;
        }

        NoSqlToken first = skipCommentsAndSemicolons();
        if (first.getType() == NoSqlTokenType.EOF) {
            addError("CQL_UNSUPPORTED_STATEMENT", "Sentencia CQL vacia.", 0, 0, "");
            return result;
        }

        String kw = first.getLexeme().toUpperCase();
        switch (kw) {
            case "SELECT" -> parseSelect();
            case "INSERT" -> parseInsert();
            case "UPDATE" -> parseUpdate();
            case "DELETE" -> parseDelete();
            case "CREATE" -> parseCreate();
            case "ALTER" -> parseAlter();
            case "DROP" -> parseDrop();
            case "TRUNCATE" -> parseTruncate();
            case "USE" -> parseUse();
            default -> addError("CQL_UNSUPPORTED_STATEMENT",
                "Sentencia CQL no soportada: " + first.getLexeme() + ".", first);
        }

        return result;
    }

    private NoSqlToken skipCommentsAndSemicolons() {
        while (pos < tokens.size()) {
            NoSqlToken t = tokens.get(pos);
            if (t.getType() == NoSqlTokenType.COMMENT || t.getType() == NoSqlTokenType.SEMICOLON) {
                pos++;
            } else {
                return tokens.get(pos);
            }
        }
        return new NoSqlToken(NoSqlTokenType.EOF, "", 0, 0);
    }

    private static final Set<String> WHERE_STOP_WORDS = Set.of(
        "ALLOW", "ORDER", "LIMIT", "IF"
    );

    private void parseSelect() {
        result.setStatementType("CASSANDRA_SELECT");
        result.addDetectedClause("SELECT");
        advance();

        NoSqlToken next = current();
        if (next.getType() == NoSqlTokenType.OPERATOR && "*".equals(next.getLexeme())) {
            advance();
        } else {
            parseIdentifierList();
        }

        expectKeyword("FROM", "CQL_EXPECTED_FROM", "Se esperaba FROM en SELECT.");
        result.addDetectedClause("FROM");

        NoSqlToken tableToken = current();
        if (tableToken.getType() == NoSqlTokenType.IDENTIFIER) {
            advance();
        } else {
            addError("CQL_EXPECTED_TABLE", "Se esperaba nombre de tabla despues de FROM.", tableToken);
        }

        if (current().getLexeme().equalsIgnoreCase("WHERE")) {
            advance();
            result.addDetectedClause("WHERE");
            skipUntilClauseOrSemicolon();
        }

        if (current().getLexeme().equalsIgnoreCase("ALLOW")) {
            advance();
            expectKeyword("FILTERING", "CQL_EXPECTED_FILTERING", "Se esperaba FILTERING despues de ALLOW.");
            result.addDetectedClause("ALLOW_FILTERING");
        }

        if (current().getLexeme().equalsIgnoreCase("ORDER")) {
            advance();
            expectKeyword("BY", "CQL_EXPECTED_BY", "Se esperaba BY despues de ORDER.");
            result.addDetectedClause("ORDER_BY");
            skipUntilSemicolon();
        }

        if (current().getLexeme().equalsIgnoreCase("LIMIT")) {
            advance();
            result.addDetectedClause("LIMIT");
            if (current().getType() == NoSqlTokenType.NUMBER) advance();
        }
    }

    private void parseInsert() {
        result.setStatementType("CASSANDRA_INSERT");
        result.addDetectedClause("INSERT");
        advance();

        expectKeyword("INTO", "CQL_EXPECTED_INTO", "Se esperaba INTO despues de INSERT.");
        result.addDetectedClause("INTO");

        if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
        else addError("CQL_EXPECTED_TABLE", "Se esperaba nombre de tabla.", current());

        if (current().getType() == NoSqlTokenType.LEFT_PAREN) {
            advance();
            parseIdentifierList();
            if (current().getType() == NoSqlTokenType.RIGHT_PAREN) advance();
            else addError("CQL_UNBALANCED_PARENTHESES", "Parentesis desbalanceados en INSERT.", current());
        }

        expectKeyword("VALUES", "CQL_EXPECTED_VALUES", "Se esperaba VALUES en INSERT.");
        result.addDetectedClause("VALUES");

        if (current().getType() == NoSqlTokenType.LEFT_PAREN) {
            advance();
            skipUntilRightParen();
        }
    }

    private void parseUpdate() {
        result.setStatementType("CASSANDRA_UPDATE");
        result.addDetectedClause("UPDATE");
        advance();

        if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
        else addError("CQL_EXPECTED_TABLE", "Se esperaba nombre de tabla.", current());

        if (current().getLexeme().equalsIgnoreCase("USING")) {
            advance();
            result.addDetectedClause("USING");
            skipUntilKeyword("SET");
        }

        expectKeyword("SET", "CQL_EXPECTED_SET", "Se esperaba SET en UPDATE.");
        result.addDetectedClause("SET");
        skipUntilKeyword("WHERE");

        expectKeyword("WHERE", "CQL_EXPECTED_WHERE", "Se esperaba WHERE en UPDATE.");
        result.addDetectedClause("WHERE");
        skipUntilClauseOrSemicolon();

        if (current().getLexeme().equalsIgnoreCase("IF")) {
            advance();
            result.addDetectedClause("IF");
            skipUntilSemicolon();
        }
    }

    private void parseDelete() {
        result.setStatementType("CASSANDRA_DELETE");
        result.addDetectedClause("DELETE");
        advance();

        NoSqlToken next = current();
        if (next.getType() == NoSqlTokenType.IDENTIFIER) {
            parseIdentifierList();
        }

        expectKeyword("FROM", "CQL_EXPECTED_FROM", "Se esperaba FROM en DELETE.");
        result.addDetectedClause("FROM");

        if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
        else addError("CQL_EXPECTED_TABLE", "Se esperaba nombre de tabla.", current());

        if (current().getLexeme().equalsIgnoreCase("WHERE")) {
            advance();
            result.addDetectedClause("WHERE");
            skipUntilClauseOrSemicolon();
        }

        if (current().getLexeme().equalsIgnoreCase("IF")) {
            advance();
            result.addDetectedClause("IF");
            skipUntilSemicolon();
        }
    }

    private void parseCreate() {
        advance();
        String next = current().getLexeme().toUpperCase();
        if (next.equals("KEYSPACE")) {
            parseCreateKeyspace();
        } else if (next.equals("TABLE")) {
            parseCreateTable();
        } else {
            addError("CQL_UNSUPPORTED_STATEMENT", "CREATE debe ser KEYSPACE o TABLE.", current());
        }
    }

    private void parseCreateKeyspace() {
        result.setStatementType("CASSANDRA_CREATE_KEYSPACE");
        result.addDetectedClause("CREATE_KEYSPACE");
        advance();

        if (current().getLexeme().equalsIgnoreCase("IF")) {
            advance(); expectKeyword("NOT", "CQL_EXPECTED_NOT", ""); expectKeyword("EXISTS", "CQL_EXPECTED_EXISTS", "");
            result.addDetectedClause("IF_NOT_EXISTS");
        }

        if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
        else addError("CQL_EXPECTED_KEYSPACE_NAME", "Se esperaba nombre de keyspace.", current());

        if (current().getLexeme().equalsIgnoreCase("WITH")) {
            advance();
            result.addDetectedClause("WITH");
            skipUntilSemicolon();
        }
    }

    private void parseCreateTable() {
        result.setStatementType("CASSANDRA_CREATE_TABLE");
        result.addDetectedClause("CREATE_TABLE");
        advance();

        if (current().getLexeme().equalsIgnoreCase("IF")) {
            advance(); expectKeyword("NOT", "CQL_EXPECTED_NOT", ""); expectKeyword("EXISTS", "CQL_EXPECTED_EXISTS", "");
            result.addDetectedClause("IF_NOT_EXISTS");
        }

        if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
        else addError("CQL_EXPECTED_TABLE", "Se esperaba nombre de tabla.", current());

        if (current().getType() == NoSqlTokenType.LEFT_PAREN) {
            advance();
            int parenLevel = 1;
            while (pos < tokens.size() && parenLevel > 0) {
                NoSqlToken t = advance();
                if (t.getType() == NoSqlTokenType.LEFT_PAREN) parenLevel++;
                if (t.getType() == NoSqlTokenType.RIGHT_PAREN) parenLevel--;
                if (t.getLexeme().equalsIgnoreCase("PRIMARY")) {
                    result.addDetectedClause("PRIMARY_KEY");
                }
            }
        } else {
            addError("CQL_EXPECTED_PARENTHESES", "Se esperaba '(' para definicion de columnas.", current());
        }

        if (current().getLexeme().equalsIgnoreCase("WITH")) {
            advance();
            result.addDetectedClause("WITH");
            skipUntilSemicolon();
        }
    }

    private void parseAlter() {
        advance();
        if (current().getLexeme().equalsIgnoreCase("TABLE")) {
            result.setStatementType("CASSANDRA_ALTER_TABLE");
            result.addDetectedClause("ALTER_TABLE");
            advance();
            if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
            else addError("CQL_EXPECTED_TABLE", "Se esperaba nombre de tabla.", current());
            if (current().getLexeme().equalsIgnoreCase("ADD") || current().getLexeme().equalsIgnoreCase("DROP") || current().getLexeme().equalsIgnoreCase("RENAME")) {
                advance();
                result.addDetectedClause(current().getLexeme().toUpperCase());
                skipUntilSemicolon();
            }
        } else {
            addError("CQL_UNSUPPORTED_STATEMENT", "ALTER debe ser TABLE.", current());
        }
    }

    private void parseDrop() {
        advance();
        String what = current().getLexeme().toUpperCase();
        if (what.equals("TABLE") || what.equals("KEYSPACE")) {
            if (what.equals("TABLE")) result.setStatementType("CASSANDRA_DROP_TABLE");
            else result.setStatementType("CASSANDRA_DROP_KEYSPACE");
            result.addDetectedClause("DROP_" + what);
            advance();

            if (current().getLexeme().equalsIgnoreCase("IF")) {
                advance(); expectKeyword("EXISTS", "CQL_EXPECTED_EXISTS", "");
            }

            if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
            else addError("CQL_EXPECTED_NAME", "Se esperaba nombre.", current());
        } else {
            addError("CQL_UNSUPPORTED_STATEMENT", "DROP debe ser TABLE o KEYSPACE.", current());
        }
    }

    private void parseTruncate() {
        result.setStatementType("CASSANDRA_TRUNCATE");
        result.addDetectedClause("TRUNCATE");
        advance();
        if (current().getLexeme().equalsIgnoreCase("TABLE")) {
            advance();
        }
        if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
        else addError("CQL_EXPECTED_TABLE", "Se esperaba nombre de tabla.", current());
    }

    private void parseUse() {
        result.setStatementType("CASSANDRA_USE");
        result.addDetectedClause("USE");
        advance();
        if (current().getType() == NoSqlTokenType.IDENTIFIER) advance();
        else addError("CQL_EXPECTED_KEYSPACE_NAME", "Se esperaba nombre de keyspace.", current());
    }

    private void parseIdentifierList() {
        boolean first = true;
        while (pos < tokens.size()) {
            NoSqlToken t = current();
            if (t.getType() == NoSqlTokenType.IDENTIFIER || t.getType() == NoSqlTokenType.OPERATOR && "*".equals(t.getLexeme())) {
                advance();
                first = false;
            } else if (t.getType() == NoSqlTokenType.COMMA) {
                advance();
            } else {
                break;
            }
        }
    }

    private void skipUntilSemicolon() {
        while (pos < tokens.size()) {
            NoSqlToken t = current();
            if (t.getType() == NoSqlTokenType.SEMICOLON || t.getType() == NoSqlTokenType.EOF) break;
            advance();
        }
    }

    private void skipUntilClauseOrSemicolon() {
        while (pos < tokens.size()) {
            NoSqlToken t = current();
            if (t.getType() == NoSqlTokenType.SEMICOLON || t.getType() == NoSqlTokenType.EOF) break;
            if (WHERE_STOP_WORDS.contains(t.getLexeme().toUpperCase())) break;
            advance();
        }
    }

    private void skipUntilRightParen() {
        int depth = 1;
        while (pos < tokens.size() && depth > 0) {
            NoSqlToken t = advance();
            if (t.getType() == NoSqlTokenType.LEFT_PAREN) depth++;
            if (t.getType() == NoSqlTokenType.RIGHT_PAREN) depth--;
        }
    }

    private void skipUntilKeyword(String keyword) {
        while (pos < tokens.size()) {
            NoSqlToken t = current();
            if (t.getLexeme().equalsIgnoreCase(keyword)) break;
            if (t.getType() == NoSqlTokenType.SEMICOLON || t.getType() == NoSqlTokenType.EOF) break;
            advance();
        }
    }

    private void expectKeyword(String keyword, String errorCode, String message) {
        if (pos < tokens.size() && current().getLexeme().equalsIgnoreCase(keyword)) {
            advance();
        } else if (!message.isEmpty()) {
            addError(errorCode, message, current());
        }
    }

    private NoSqlToken advance() {
        if (pos < tokens.size()) return tokens.get(pos++);
        return new NoSqlToken(NoSqlTokenType.EOF, "", 0, 0);
    }

    private NoSqlToken current() {
        if (pos < tokens.size()) return tokens.get(pos);
        return new NoSqlToken(NoSqlTokenType.EOF, "", 0, 0);
    }

    private void addError(String code, String message, NoSqlToken token) {
        result.addSyntaxError(new NoSqlSyntaxError(code, message, token.getLine(), token.getColumn(), token.getLexeme(), "SYNTAX"));
    }

    private void addError(String code, String message, int line, int col, String lexeme) {
        result.addSyntaxError(new NoSqlSyntaxError(code, message, line, col, lexeme, "SYNTAX"));
    }
}
