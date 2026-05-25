package com.umg.model.parser;

import com.umg.model.ast.*;
import com.umg.model.error.CompilerError;
import com.umg.model.error.ErrorCollector;
import com.umg.model.lexer.Token;
import com.umg.model.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final ErrorCollector errorCollector;
    private List<Token> tokens;
    private int position;

    public Parser(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
        this.position = 0;
    }

    public ASTNode parse(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        if (tokens == null || tokens.isEmpty()) return null;
        return parseStatement();
    }

    private ASTNode parseStatement() {
        if (position >= tokens.size()) return null;
        skipComments();
        Token current = tokens.get(position);

        if (current.getType() == TokenType.EOF) return null;

        if (current.getType() != TokenType.PALABRA_RESERVADA &&
            current.getType() != TokenType.KEYWORD &&
            current.getType() != TokenType.IDENTIFICADOR) {
            addError("Se esperaba una palabra clave SQL al inicio de la sentencia", current);
            return null;
        }

        String keyword = current.getLexeme().toUpperCase();
        return switch (keyword) {
            case "SELECT" -> parseSelect();
            case "INSERT" -> parseInsert();
            case "UPDATE" -> parseUpdate();
            case "DELETE" -> parseDelete();
            case "CREATE" -> parseCreate();
            case "ALTER" -> parseAlter();
            case "DROP" -> parseDrop();
            case "TRUNCATE" -> parseTruncate();
            case "WITH" -> parseWith();
            default -> {
                addError("Sentencia no reconocida: " + keyword, current);
                yield null;
            }
        };
    }

    private ASTNode parseSelect() {
        consume(); // SELECT

        if (checkTokenType(TokenType.PUNTO_Y_COMA) || checkTokenType(TokenType.EOF)) {
            addError("SELECT requiere al menos una columna o *");
            return new SelectStatement();
        }

        if (checkKeyword("DISTINCT")) {
            consume();
        }

        if (checkTokenType(TokenType.ASTERISCO)) {
            consume(); // *
        } else {
            parseSelectColumns();
        }

        skipComments();
        if (checkKeyword("FROM")) {
            consume();
            skipComments();
            if (!parseTableName()) {
                addError("Nombre de tabla invalido despues de FROM");
                advanceToSemicolonOrEnd();
                return new SelectStatement();
            }
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                if (!isJoinKeyword() && !isClauseKeyword()) {
                    consume(); // alias
                }
            }
        } else if (hasMoreTokensAfterSelect()) {
            addError("SELECT requiere clausula FROM");
            return new SelectStatement();
        }

        while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.EOF)) {
            skipComments();
            if (position >= tokens.size()) break;
            if (isJoinKeyword()) {
                parseJoin();
            } else if (checkKeyword("WHERE")) {
                parseWhere();
            } else if (checkKeyword("GROUP")) {
                parseGroupBy();
            } else if (checkKeyword("HAVING")) {
                parseHaving();
            } else if (checkKeyword("ORDER")) {
                parseOrderBy();
            } else if (checkKeyword("LIMIT")) {
                parseLimit();
            } else if (checkKeyword("OFFSET")) {
                parseOffset();
            } else if (checkKeyword("UNION")) {
                parseUnion();
            } else {
                break;
            }
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new SelectStatement();
    }

    private void parseSelectColumns() {
        boolean first = true;
        while (position < tokens.size()) {
            skipComments();
            if (position >= tokens.size()) break;
            if (checkKeyword("FROM")) break;
            if (isClauseKeyword()) break;

            if (!first) {
                if (!checkTokenType(TokenType.COMA)) break;
                consume();
                skipComments();
                if (checkKeyword("FROM")) break;
                if (isClauseKeyword()) break;
            }
            first = false;

            if (checkTokenType(TokenType.ASTERISCO)) {
                consume();
            } else if (checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
                consume();
            } else if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
                if (checkTokenType(TokenType.PUNTO)) {
                    consume();
                    if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
                        checkTokenType(TokenType.ASTERISCO)) {
                        consume();
                    }
                }
                if (checkKeyword("AS")) {
                    consume();
                    if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                        consume();
                    }
                }
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    if (!isJoinKeyword() && !isClauseKeyword()) {
                        consume();
                    }
                }
            } else if (checkTokenType(TokenType.FUNCION)) {
                consume();
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parseParenthesizedExpression();
                }
                if (checkKeyword("AS")) {
                    consume();
                    if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                        consume();
                    }
                }
            } else if (checkTokenType(TokenType.NUMERO_ENTERO) || checkTokenType(TokenType.NUMERO_DECIMAL)) {
                consume();
            } else if (checkTokenType(TokenType.CADENA)) {
                consume();
            } else {
                break;
            }
        }

        if (first) {
            addError("Lista de columnas vacia en SELECT");
        }
    }

    private void parseJoin() {
        String joinType = consume().getLexeme().toUpperCase();

        if (joinType.equals("JOIN")) {
            // Bare JOIN (equivalent to INNER JOIN) - already consumed, nothing else needed
        } else if (checkKeyword("OUTER")) {
            joinType += " " + consume().getLexeme().toUpperCase();
            if (checkKeyword("JOIN")) {
                consume();
            } else {
                addError("JOIN esperado despues de " + joinType);
                return;
            }
        } else if (checkKeyword("JOIN")) {
            consume(); // e.g. "INNER JOIN" - INNER was consumed, now consume JOIN
        } else if (checkKeyword("INNER") || checkKeyword("LEFT") || checkKeyword("RIGHT") ||
                   checkKeyword("FULL") || checkKeyword("CROSS")) {
            joinType += " " + consume().getLexeme().toUpperCase();
            if (checkKeyword("OUTER")) {
                joinType += " " + consume().getLexeme().toUpperCase();
            }
            if (checkKeyword("JOIN")) {
                consume();
            } else {
                addError("JOIN esperado despues de " + joinType);
                return;
            }
        } else {
            addError("Tipo de JOIN invalido");
            return;
        }

        if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
            checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
            consume();

            if (checkTokenType(TokenType.PUNTO)) {
                consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                }
            }

            if (checkTokenType(TokenType.IDENTIFICADOR) && !checkKeyword("ON")) {
                consume(); // alias
            }
        } else {
            addError("Nombre de tabla esperado en JOIN");
            return;
        }

        if (!joinType.contains("CROSS")) {
            if (checkKeyword("ON")) {
                consume();
                parseJoinCondition();
            } else {
                addError("ON requerido para JOIN");
            }
        }
    }

    private void parseJoinCondition() {
        parseCondition();
        while (checkKeyword("AND") || checkKeyword("OR")) {
            consume();
            parseCondition();
        }
    }

    private void parseWhere() {
        consume(); // WHERE
        parseWhereCondition();
        while (checkKeyword("AND") || checkKeyword("OR")) {
            consume();
            parseWhereCondition();
        }
    }

    private void parseWhereCondition() {
        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
            consume(); // (
            parseWhereCondition();
            while (checkKeyword("AND") || checkKeyword("OR")) {
                consume();
                parseWhereCondition();
            }
            if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                consume(); // )
            } else {
                addError(") esperado para cerrar parentesis en WHERE");
            }
        } else if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
            checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO) || checkTokenType(TokenType.NUMERO_ENTERO) ||
            checkTokenType(TokenType.NUMERO_DECIMAL) || checkTokenType(TokenType.CADENA) ||
            checkTokenType(TokenType.FUNCION)) {
            parseCondition();
        }
    }

    private void parseCondition() {
        parseValue();

        if (checkTokenType(TokenType.OPERADOR_COMPARACION) || checkTokenType(TokenType.OPERADOR)) {
            consume();
            parseValue();
        } else if (checkKeyword("IN")) {
            consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            } else {
                addError("( esperado despues de IN");
            }
        } else if (checkKeyword("LIKE")) {
            consume();
            parseValue();
        } else if (checkKeyword("BETWEEN")) {
            consume();
            parseValue();
            if (checkKeyword("AND")) consume();
            parseValue();
        } else if (checkKeyword("IS")) {
            consume();
            if (checkKeyword("NOT")) consume();
            if (checkKeyword("NULL")) consume();
        } else if (checkKeyword("NOT")) {
            consume();
            if (checkKeyword("IN") || checkKeyword("LIKE") || checkKeyword("BETWEEN")) {
                String op = consume().getLexeme();
                if (op.equals("IN")) {
                    if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                        parseParenthesizedExpression();
                    }
                } else if (op.equals("LIKE")) {
                    parseValue();
                } else if (op.equals("BETWEEN")) {
                    parseValue();
                    if (checkKeyword("AND")) consume();
                    parseValue();
                }
            }
        }
    }

    private void parseValue() {
        if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
            consume();
            if (checkTokenType(TokenType.PUNTO)) {
                consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                }
            }
        } else if (checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
            consume();
        } else if (checkTokenType(TokenType.NUMERO_ENTERO) || checkTokenType(TokenType.NUMERO_DECIMAL)) {
            consume();
        } else if (checkTokenType(TokenType.CADENA)) {
            consume();
        } else if (checkTokenType(TokenType.FUNCION)) {
            consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            }
        } else if (checkTokenType(TokenType.PARAMETRO)) {
            consume();
        } else if (checkTokenType(TokenType.PLACEHOLDER)) {
            consume();
        } else if (checkKeyword("NULL")) {
            consume();
        } else if (checkKeyword("TRUE") || checkKeyword("FALSE")) {
            consume();
        }
    }

    private void parseGroupBy() {
        consume(); // GROUP
        if (checkKeyword("BY")) {
            consume();
            boolean first = true;
            while (position < tokens.size()) {
                if (!first && checkTokenType(TokenType.COMA)) {
                    consume();
                }
                first = false;
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                } else {
                    break;
                }
            }
        } else {
            addError("BY esperado despues de GROUP");
        }
    }

    private void parseHaving() {
        consume(); // HAVING
        parseCondition();
    }

    private void parseOrderBy() {
        consume(); // ORDER
        if (checkKeyword("BY")) {
            consume();
            boolean first = true;
            while (position < tokens.size()) {
                if (!first && checkTokenType(TokenType.COMA)) {
                    consume();
                }
                first = false;
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
                    checkTokenType(TokenType.NUMERO_ENTERO)) {
                    consume();
                } else {
                    break;
                }
                if (checkKeyword("ASC") || checkKeyword("DESC")) {
                    consume();
                }
                if (checkKeyword("NULLS")) {
                    consume();
                    if (checkKeyword("FIRST") || checkKeyword("LAST")) {
                        consume();
                    }
                }
            }
        } else {
            addError("BY esperado despues de ORDER");
        }
    }

    private void parseLimit() {
        consume(); // LIMIT
        if (checkTokenType(TokenType.NUMERO_ENTERO)) {
            consume();
        } else {
            addError("Numero esperado despues de LIMIT");
        }
    }

    private void parseOffset() {
        consume(); // OFFSET
        if (checkTokenType(TokenType.NUMERO_ENTERO)) {
            consume();
        } else {
            addError("Numero esperado despues de OFFSET");
        }
    }

    private void parseUnion() {
        consume(); // UNION
        if (checkKeyword("ALL")) {
            consume();
        }
        parseSelect();
    }

    private ASTNode parseInsert() {
        consume(); // INSERT

        if (!checkKeyword("INTO")) {
            addError("INSERT debe seguido de INTO");
            advanceToSemicolonOrEnd();
            return new InsertStatement();
        }
        consume(); // INTO

        if (!parseTableName()) {
            addError("Nombre de tabla esperado despues de INSERT INTO");
            advanceToSemicolonOrEnd();
            return new InsertStatement();
        }

        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
            consume();
            boolean first = true;
            while (position < tokens.size() && !checkTokenType(TokenType.PARENTESIS_DERECHO) &&
                   !checkTokenType(TokenType.EOF)) {
                if (!first && checkTokenType(TokenType.COMA)) {
                    consume();
                }
                first = false;
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
                    checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
                    consume();
                } else {
                    break;
                }
            }
            if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                consume();
            } else {
                addError(") sin cerrar en lista de columnas de INSERT");
            }
        }

        if (checkKeyword("VALUES")) {
            consume();
            parseValuesList();
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new InsertStatement();
    }

    private void parseValuesList() {
        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
            consume();
            parseValueList();
            if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                consume();
            } else {
                addError(") sin cerrar en VALUES");
            }
        } else {
            addError("( esperado despues de VALUES");
        }

        while (checkTokenType(TokenType.COMA)) {
            consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                consume();
                parseValueList();
                if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                    consume();
                } else {
                    addError(") sin cerrar en VALUES");
                }
            } else {
                addError("( esperado en lista de VALUES");
            }
        }
    }

    private void parseValueList() {
        boolean first = true;
        while (position < tokens.size() && !checkTokenType(TokenType.PARENTESIS_DERECHO) &&
               !checkTokenType(TokenType.EOF)) {
            if (!first && checkTokenType(TokenType.COMA)) {
                consume();
            }
            first = false;
            if (checkTokenType(TokenType.NUMERO_ENTERO) || checkTokenType(TokenType.NUMERO_DECIMAL) ||
                checkTokenType(TokenType.CADENA) || checkKeyword("NULL") || checkKeyword("TRUE") ||
                checkKeyword("FALSE") || checkTokenType(TokenType.PARAMETRO) ||
                checkTokenType(TokenType.PLACEHOLDER) || checkTokenType(TokenType.FUNCION)) {
                if (checkTokenType(TokenType.FUNCION)) {
                    consume();
                    if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                        parseParenthesizedExpression();
                    }
                } else {
                    consume();
                }
            } else {
                break;
            }
        }
    }

    private ASTNode parseUpdate() {
        consume(); // UPDATE

        if (!parseTableName()) {
            addError("Nombre de tabla esperado despues de UPDATE");
            advanceToSemicolonOrEnd();
            return new UpdateStatement();
        }

        if (!checkKeyword("SET")) {
            addError("SET esperado despues del nombre de tabla en UPDATE");
            advanceToSemicolonOrEnd();
            return new UpdateStatement();
        }
        consume(); // SET

        parseSetAssignments();

        if (checkKeyword("WHERE")) {
            parseWhere();
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new UpdateStatement();
    }

    private void parseSetAssignments() {
        boolean first = true;
        while (position < tokens.size() && !checkKeyword("WHERE") &&
               !checkTokenType(TokenType.PUNTO_Y_COMA) && !checkTokenType(TokenType.EOF)) {
            if (!first && checkTokenType(TokenType.COMA)) {
                consume();
                first = false;
                continue;
            }
            first = false;

            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
                checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
                consume();
            } else {
                break;
            }

            if (checkTokenType(TokenType.OPERADOR) && tokens.get(position).getLexeme().equals("=")) {
                consume();
            } else if (checkTokenType(TokenType.OPERADOR_COMPARACION) && tokens.get(position).getLexeme().equals("=")) {
                consume();
            } else {
                addError("= esperado en asignacion SET");
                break;
            }

            parseValue();
        }
    }

    private void skipComments() {
        while (position < tokens.size() &&
               (checkTokenType(TokenType.COMENTARIO_LINEA) || checkTokenType(TokenType.COMENTARIO_BLOQUE))) {
            consume();
        }
    }

    private boolean hasMoreTokensAfterSelect() {
        int savedPos = position;
        while (savedPos < tokens.size()) {
            Token t = tokens.get(savedPos);
            if (t.getType() == TokenType.PUNTO_Y_COMA || t.getType() == TokenType.EOF) return false;
            if (t.getType() != TokenType.COMENTARIO_LINEA && t.getType() != TokenType.COMENTARIO_BLOQUE) return true;
            savedPos++;
        }
        return false;
    }

    private boolean parseTableName() {
        if (isClauseKeyword() || isJoinKeyword()) return false;
        if (checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
            consume();
            return true;
        }
        if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
            String word = tokens.get(position).getLexeme().toUpperCase();
            if (isClauseKeywordWord(word) || isJoinKeywordWord(word)) {
                return false;
            }
            consume();
            if (checkTokenType(TokenType.PUNTO)) {
                consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                }
            }
            return true;
        }
        return false;
    }

    private boolean isClauseKeywordWord(String word) {
        return word.equals("WHERE") || word.equals("GROUP") || word.equals("ORDER") ||
               word.equals("HAVING") || word.equals("LIMIT") || word.equals("OFFSET") ||
               word.equals("UNION") || word.equals("VALUES") || word.equals("SET") ||
               word.equals("FROM") || word.equals("INTO");
    }
    
    private boolean isJoinKeywordWord(String word) {
        return word.equals("JOIN") || word.equals("INNER") || word.equals("LEFT") ||
               word.equals("RIGHT") || word.equals("FULL") || word.equals("CROSS") ||
               word.equals("OUTER");
    }

    private ASTNode parseDelete() {
        consume(); // DELETE

        if (!checkKeyword("FROM")) {
            addError("DELETE debe ir seguido de FROM");
            if (parseTableName()) {
                addError("Se esperaba FROM antes del nombre de tabla");
            }
            advanceToSemicolonOrEnd();
            return new DeleteStatement();
        }
        consume(); // FROM

        if (!parseTableName()) {
            addError("Nombre de tabla esperado despues de DELETE FROM");
            advanceToSemicolonOrEnd();
            return new DeleteStatement();
        }

        if (checkKeyword("WHERE")) {
            parseWhere();
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new DeleteStatement();
    }

    private ASTNode parseCreate() {
        consume(); // CREATE

        if (checkKeyword("TABLE")) {
            return parseCreateTable();
        } else if (checkKeyword("DATABASE")) {
            return parseCreateDatabase();
        } else if (checkKeyword("INDEX")) {
            return parseCreateIndex();
        } else if (checkKeyword("VIEW")) {
            return parseCreateView();
        } else {
            addError("CREATE requiere TABLE, DATABASE, INDEX o VIEW");
            advanceToSemicolonOrEnd();
            return new CreateTableStatement();
        }
    }

    private ASTNode parseCreateTable() {
        consume(); // TABLE

        if (!parseTableName()) {
            addError("Nombre de tabla esperado despues de CREATE TABLE");
            advanceToSemicolonOrEnd();
            return new CreateTableStatement();
        }

        if (checkKeyword("IF")) {
            consume();
            if (checkKeyword("NOT")) consume();
            if (checkKeyword("EXISTS")) consume();
        }

        if (!checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
            addError("( esperado para definicion de columnas en CREATE TABLE");
            advanceToSemicolonOrEnd();
            return new CreateTableStatement();
        }
        consume(); // (

        parseColumnDefinitions();

        if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
            consume();
        } else {
            addError(") sin cerrar en CREATE TABLE");
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new CreateTableStatement();
    }

    private void parseColumnDefinitions() {
        boolean first = true;
        while (position < tokens.size() && !checkTokenType(TokenType.PARENTESIS_DERECHO) &&
               !checkTokenType(TokenType.EOF)) {
            skipComments();
            if (position >= tokens.size() || checkTokenType(TokenType.PARENTESIS_DERECHO)) break;
            if (!first && checkTokenType(TokenType.COMA)) {
                consume();
                skipComments();
            }
            first = false;

            if (checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
                consume();
                if (checkTokenType(TokenType.PALABRA_RESERVADA) || checkTokenType(TokenType.TIPO_DATO) ||
                    checkTokenType(TokenType.IDENTIFICADOR)) {
                    consume(); // data type
                    parseColumnTypeModifiers();
                    parseColumnConstraints();
                }
            } else if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                String word = tokens.get(position).getLexeme().toUpperCase();
                if (word.equals("PRIMARY") || word.equals("CONSTRAINT") || word.equals("UNIQUE") ||
                    word.equals("INDEX") || word.equals("KEY") || word.equals("CHECK") ||
                    word.equals("FOREIGN")) {
                    parseTableConstraint();
                } else {
                    consume(); // column name
                    if (checkTokenType(TokenType.PALABRA_RESERVADA) || checkTokenType(TokenType.TIPO_DATO) ||
                        checkTokenType(TokenType.IDENTIFICADOR)) {
                        consume(); // data type
                        parseColumnTypeModifiers();
                        parseColumnConstraints();
                    }
                }
            } else if (checkTokenType(TokenType.COMENTARIO_LINEA) || checkTokenType(TokenType.COMENTARIO_BLOQUE)) {
                consume();
            } else {
                break;
            }
        }
    }

    private void parseColumnTypeModifiers() {
        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
            consume();
            while (position < tokens.size() && !checkTokenType(TokenType.PARENTESIS_DERECHO) &&
                   !checkTokenType(TokenType.EOF)) {
                if (checkTokenType(TokenType.NUMERO_ENTERO) || checkTokenType(TokenType.NUMERO_DECIMAL)) {
                    consume();
                } else if (checkTokenType(TokenType.COMA)) {
                    consume();
                } else {
                    break;
                }
            }
            if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                consume();
            }
        }
        if (checkKeyword("UNSIGNED") || checkKeyword("SIGNED") || checkKeyword("ZEROFILL")) {
            consume();
        }
    }

    private void parseColumnConstraints() {
        while (position < tokens.size() && !checkTokenType(TokenType.COMA) &&
               !checkTokenType(TokenType.PARENTESIS_DERECHO) && !checkTokenType(TokenType.EOF)) {
            if (checkKeyword("PRIMARY")) {
                consume();
                if (checkKeyword("KEY")) consume();
            } else if (checkKeyword("NOT")) {
                consume();
                if (checkKeyword("NULL")) consume();
            } else if (checkKeyword("NULL")) {
                consume();
            } else if (checkKeyword("DEFAULT")) {
                consume();
                parseValue();
            } else if (checkKeyword("UNIQUE")) {
                consume();
            } else if (checkKeyword("CHECK")) {
                consume();
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parseParenthesizedExpression();
                }
            } else if (checkKeyword("REFERENCES")) {
                consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                }
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    consume();
                    if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                        consume();
                    }
                    if (checkTokenType(TokenType.PARENTESIS_DERECHO)) consume();
                }
            } else if (checkKeyword("AUTO_INCREMENT")) {
                consume();
            } else if (checkKeyword("IDENTITY")) {
                consume();
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parseParenthesizedExpression();
                }
            } else if (checkKeyword("COLLATE")) {
                consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                }
            } else if (checkKeyword("COMMENT")) {
                consume();
                if (checkTokenType(TokenType.CADENA)) consume();
            } else if (checkKeyword("GENERATED")) {
                consume();
                if (checkKeyword("ALWAYS")) consume();
                if (checkKeyword("AS")) {
                    consume();
                    if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                        parseParenthesizedExpression();
                    }
                }
                if (checkKeyword("STORED") || checkKeyword("VIRTUAL")) consume();
            } else if (checkTokenType(TokenType.COMENTARIO_LINEA) || checkTokenType(TokenType.COMENTARIO_BLOQUE)) {
                consume();
            } else {
                break;
            }
        }
    }

    private void parseTableConstraint() {
        if (checkKeyword("PRIMARY")) {
            consume();
            if (checkKeyword("KEY")) consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            }
        } else if (checkKeyword("CONSTRAINT")) {
            consume();
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
            }
            if (checkKeyword("PRIMARY")) {
                consume();
                if (checkKeyword("KEY")) consume();
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parseParenthesizedExpression();
                }
            } else if (checkKeyword("FOREIGN")) {
                consume();
                if (checkKeyword("KEY")) consume();
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parseParenthesizedExpression();
                }
                if (checkKeyword("REFERENCES")) {
                    consume();
                    if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                        consume();
                        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                            parseParenthesizedExpression();
                        }
                    }
                }
            } else if (checkKeyword("UNIQUE")) {
                consume();
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parseParenthesizedExpression();
                }
            } else if (checkKeyword("CHECK")) {
                consume();
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parseParenthesizedExpression();
                }
            }
        } else if (checkKeyword("UNIQUE")) {
            consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            }
        } else if (checkKeyword("FOREIGN")) {
            consume();
            if (checkKeyword("KEY")) consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            }
            if (checkKeyword("REFERENCES")) {
                consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                    if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                        parseParenthesizedExpression();
                    }
                }
            }
        } else if (checkKeyword("INDEX") || checkKeyword("KEY")) {
            consume();
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
            }
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            }
        } else if (checkKeyword("CHECK")) {
            consume();
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            }
        }
    }

    private ASTNode parseCreateDatabase() {
        consume();
        if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
            consume();
        }
        if (checkTokenType(TokenType.PUNTO_Y_COMA)) consume();
        return new CreateTableStatement();
    }

    private ASTNode parseCreateIndex() {
        consume();
        if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
            consume();
        }
        if (checkKeyword("ON")) {
            consume();
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
            }
            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                parseParenthesizedExpression();
            }
        }
        if (checkTokenType(TokenType.PUNTO_Y_COMA)) consume();
        return new CreateTableStatement();
    }

    private ASTNode parseCreateView() {
        consume();
        if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
            consume();
        }
        if (checkKeyword("AS")) {
            consume();
            parseSelect();
        }
        if (checkTokenType(TokenType.PUNTO_Y_COMA)) consume();
        return new CreateTableStatement();
    }

    private ASTNode parseAlter() {
        consume(); // ALTER

        if (checkKeyword("TABLE")) {
            consume();
            if (!parseTableName()) {
                addError("Nombre de tabla esperado despues de ALTER TABLE");
                advanceToSemicolonOrEnd();
                return new CreateTableStatement();
            }

            if (checkKeyword("ADD")) {
                consume();
                if (checkKeyword("COLUMN")) consume();
                if (checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
                    consume();
                    if (checkTokenType(TokenType.PALABRA_RESERVADA) || checkTokenType(TokenType.TIPO_DATO)) {
                        consume();
                    }
                } else if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    if (checkKeyword("PRIMARY") || checkKeyword("UNIQUE") || checkKeyword("FOREIGN") ||
                        checkKeyword("CONSTRAINT") || checkKeyword("CHECK") || checkKeyword("INDEX") ||
                        checkKeyword("KEY")) {
                        parseTableConstraint();
                    } else {
                        consume();
                        if (checkTokenType(TokenType.PALABRA_RESERVADA) || checkTokenType(TokenType.TIPO_DATO)) {
                            consume();
                            parseColumnTypeModifiers();
                            parseColumnConstraints();
                        }
                    }
                }
            } else if (checkKeyword("DROP")) {
                consume();
                if (checkKeyword("COLUMN")) consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
                    checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
                    consume();
                } else {
                    addError("Nombre de columna esperado en ALTER TABLE DROP");
                }
            } else if (checkKeyword("ALTER")) {
                consume();
                if (checkKeyword("COLUMN")) consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA) ||
                    checkTokenType(TokenType.IDENTIFICADOR_DELIMITADO)) {
                    consume();
                }
                if (checkTokenType(TokenType.PALABRA_RESERVADA) || checkTokenType(TokenType.TIPO_DATO)) {
                    consume();
                    parseColumnTypeModifiers();
                } else if (checkKeyword("SET") || checkKeyword("DROP")) {
                    consume();
                    if (checkKeyword("DEFAULT") || checkKeyword("NOT") || checkKeyword("NULL")) consume();
                }
            } else if (checkKeyword("RENAME")) {
                consume();
                if (checkKeyword("TO") || checkKeyword("COLUMN")) consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                }
            } else if (checkKeyword("MODIFY")) {
                consume();
                if (checkKeyword("COLUMN")) consume();
                if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                    consume();
                    if (checkTokenType(TokenType.PALABRA_RESERVADA) || checkTokenType(TokenType.TIPO_DATO)) {
                        consume();
                    }
                }
            }
        } else if (checkKeyword("DATABASE") || checkKeyword("SCHEMA")) {
            consume();
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
            }
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new CreateTableStatement();
    }

    private ASTNode parseDrop() {
        consume(); // DROP

        if (checkKeyword("TABLE")) {
            consume();
            if (!parseTableName()) {
                addError("Nombre de tabla esperado en DROP TABLE");
            }
        } else if (checkKeyword("DATABASE") || checkKeyword("SCHEMA")) {
            consume();
            if (checkKeyword("IF")) {
                consume();
                if (checkKeyword("EXISTS")) consume();
            }
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
            }
        } else if (checkKeyword("INDEX")) {
            consume();
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
            }
        } else if (checkKeyword("VIEW")) {
            consume();
            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume();
            }
        } else {
            addError("TABLE, DATABASE, INDEX o VIEW esperado despues de DROP");
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new DropTableStatement();
    }

    private ASTNode parseTruncate() {
        consume(); // TRUNCATE

        if (checkKeyword("TABLE")) {
            consume();
        }

        if (!parseTableName()) {
            addError("Nombre de tabla esperado en TRUNCATE");
        }

        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            consume();
        }

        return new DropTableStatement();
    }

    private ASTNode parseWith() {
        consume(); // WITH

        if (checkKeyword("RECURSIVE")) {
            consume();
        }

        boolean first = true;
        while (position < tokens.size() && !checkTokenType(TokenType.EOF)) {
            if (checkKeyword("SELECT")) break;

            if (!first && checkTokenType(TokenType.COMA)) {
                consume();
            }
            first = false;

            if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                consume(); // CTE name
            } else {
                addError("Nombre de CTE esperado");
                break;
            }

            if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                consume();
                boolean colFirst = true;
                while (position < tokens.size() && !checkTokenType(TokenType.PARENTESIS_DERECHO) &&
                       !checkTokenType(TokenType.EOF)) {
                    if (!colFirst && checkTokenType(TokenType.COMA)) {
                        consume();
                    }
                    colFirst = false;
                    if (checkTokenType(TokenType.IDENTIFICADOR) || checkTokenType(TokenType.PALABRA_RESERVADA)) {
                        consume();
                    } else {
                        break;
                    }
                }
                if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                    consume();
                } else {
                    addError(") sin cerrar en nombres de columnas CTE");
                }
            }

            if (!checkKeyword("AS")) {
                addError("AS esperado en definicion de CTE");
                advanceToSemicolonOrEnd();
                return new SelectStatement();
            }
            consume(); // AS

            if (!checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                addError("( esperado en cuerpo de CTE");
                advanceToSemicolonOrEnd();
                return new SelectStatement();
            }
            consume(); // (

            parseSelect();

            if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                consume();
            } else {
                addError(") sin cerrar en CTE");
            }

            // Check for comma to parse additional CTEs
            if (!checkTokenType(TokenType.COMA)) {
                break;
            }
        }

        if (position >= tokens.size() || checkTokenType(TokenType.EOF)) {
            addError("SELECT esperado despues de definicion(es) CTE");
            return new SelectStatement();
        }

        if (!checkKeyword("SELECT")) {
            addError("SELECT esperado despues de definicion(es) CTE");
        } else {
            parseSelect();
        }

        return new SelectStatement();
    }

    private void parseParenthesizedExpression() {
        if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
            int parenDepth = 0;
            consume(); // (
            while (position < tokens.size() && !checkTokenType(TokenType.EOF)) {
                if (checkTokenType(TokenType.PARENTESIS_IZQUIERDO)) {
                    parenDepth++;
                    consume();
                } else if (checkTokenType(TokenType.PARENTESIS_DERECHO)) {
                    if (parenDepth == 0) {
                        consume(); // )
                        return;
                    }
                    parenDepth--;
                    consume();
                } else {
                    consume();
                }
            }
            addError("Parentesis sin cerrar");
        }
    }

    private boolean isJoinKeyword() {
        if (position >= tokens.size()) return false;
        String word = tokens.get(position).getLexeme().toUpperCase();
        if (word.equals("JOIN")) return true;
        if (word.equals("INNER") || word.equals("LEFT") || word.equals("RIGHT") ||
            word.equals("FULL") || word.equals("CROSS")) {
            return true;
        }
        return false;
    }

    private boolean isClauseKeyword() {
        if (position >= tokens.size()) return false;
        String word = tokens.get(position).getLexeme().toUpperCase();
        return word.equals("WHERE") || word.equals("GROUP") || word.equals("ORDER") ||
               word.equals("HAVING") || word.equals("LIMIT") || word.equals("OFFSET") ||
               word.equals("UNION") || word.equals("INNER") || word.equals("LEFT") ||
               word.equals("RIGHT") || word.equals("FULL") || word.equals("CROSS") ||
               word.equals("JOIN") || word.equals("FROM") || word.equals("INTO") ||
               word.equals("VALUES") || word.equals("SET");
    }

    private void advanceToSemicolonOrEnd() {
        while (position < tokens.size() && !checkTokenType(TokenType.PUNTO_Y_COMA) &&
               !checkTokenType(TokenType.EOF)) {
            position++;
        }
        if (checkTokenType(TokenType.PUNTO_Y_COMA)) {
            position++;
        }
    }

    private Token consume() {
        if (position < tokens.size()) {
            Token current = tokens.get(position);
            position++;
            return current;
        }
        return null;
    }

    private boolean checkTokenType(TokenType type) {
        return position < tokens.size() && tokens.get(position).getType() == type;
    }

    private boolean checkKeyword(String keyword) {
        if (position >= tokens.size()) return false;
        Token t = tokens.get(position);
        return (t.getType() == TokenType.PALABRA_RESERVADA ||
                t.getType() == TokenType.KEYWORD ||
                t.getType() == TokenType.TIPO_DATO ||
                t.getType() == TokenType.IDENTIFICADOR ||
                t.getType() == TokenType.OPERADOR_LOGICO) &&
               t.getLexeme().equalsIgnoreCase(keyword);
    }

    private void addError(String message) {
        int line = 0, col = 0;
        if (position < tokens.size()) {
            line = tokens.get(position).getLine();
            col = tokens.get(position).getColumn();
        }
        errorCollector.addError(new CompilerError("SYNTAX", message, line, col));
    }

    private void addError(String message, Token token) {
        errorCollector.addError(new CompilerError(
            "SYNTAX", message,
            token != null ? token.getLine() : 0,
            token != null ? token.getColumn() : 0
        ));
    }
}
