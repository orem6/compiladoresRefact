package com.umg.model.mongo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoParser {

    private static final Set<String> VALID_STAGES = new HashSet<>(java.util.Arrays.asList(
        "$match", "$project", "$group", "$sort", "$limit", "$skip",
        "$unwind", "$lookup", "$addFields", "$bucket", "$bucketAuto",
        "$count", "$facet", "$geoNear", "$graphLookup",
        "$indexStats", "$out", "$merge", "$redact", "$replaceRoot",
        "$replaceWith", "$sample", "$set", "$sortByCount",
        "$unionWith", "$fill", "$setWindowFields"
    ));

    private static final Set<String> ACCUMULATORS = new HashSet<>(java.util.Arrays.asList(
        "$sum", "$avg", "$first", "$last", "$max", "$min", "$push", "$addToSet",
        "$stdDevPop", "$stdDevSamp", "$top", "$topN", "$bottom", "$bottomN",
        "$count", "$median", "$percentile", "$accumulator"
    ));

    private final List<MongoToken> tokens;
    private int pos;
    private final List<MongoParseError> errors;

    public MongoParser(List<MongoToken> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.errors = new ArrayList<>();
    }

    public MongoParseResult parse() {
        MongoParseResult result = new MongoParseResult();
        result.setTokens(tokens);

        if (tokens == null || tokens.isEmpty()) {
            result.setValid(false);
            result.setMessage("Pipeline vacio o sin tokens");
            return result;
        }

        if (!expect(MongoTokenType.LBRACKET)) {
            result.setValid(false);
            result.setMessage("El pipeline MongoDB debe ser un arreglo JSON ([])");
            result.setErrors(errors);
            return result;
        }
        consume();

        while (!check(MongoTokenType.RBRACKET) && !check(MongoTokenType.EOF)) {
            MongoParseError stageError = parseStage(result);
            if (stageError != null) {
                errors.add(stageError);
            }

            if (check(MongoTokenType.COMMA)) {
                consume();
            } else if (!check(MongoTokenType.RBRACKET) && !check(MongoTokenType.EOF)) {
                errors.add(new MongoParseError("E003", "Se esperaba ',' o ']' entre stages", current().getPosition()));
                break;
            }
        }

        if (check(MongoTokenType.RBRACKET)) {
            consume();
        }

        if (!errors.isEmpty()) {
            result.setValid(false);
            result.setMessage("El pipeline contiene errores sintacticos");
        } else {
            result.setMessage("Pipeline de MongoDB valido");
        }

        result.setErrors(errors);
        return result;
    }

    private MongoParseError parseStage(MongoParseResult result) {
        if (!expect(MongoTokenType.LBRACE)) {
            return new MongoParseError("E001", "Cada stage debe ser un objeto JSON {}", current().getPosition());
        }
        consume();

        if (expect(MongoTokenType.RBRACE)) {
            consume();
            return new MongoParseError("E002", "Stage vacio", current().getPosition());
        }

        if (!expect(MongoTokenType.STRING)) {
            return new MongoParseError("E004", "Se esperaba nombre de operador (ej: $match)", current().getPosition());
        }

        String operatorName = current().getLexeme();
        consume();

        if (!expect(MongoTokenType.COLON)) {
            return new MongoParseError("E005", "Se esperaba ':' despues del operador", current().getPosition());
        }
        consume();

        if (!operatorName.startsWith("$")) {
            return new MongoParseError("E006", "Los operadores de stage deben comenzar con '$': " + operatorName, current().getPosition());
        }

        if (!VALID_STAGES.contains(operatorName)) {
            return new MongoParseError("E007", "Operador de stage no reconocido: " + operatorName, current().getPosition());
        }

        result.getDetectedStages().add(operatorName);

        parseValue();

        if (!expect(MongoTokenType.RBRACE)) {
            return new MongoParseError("E008", "Se esperaba '}' para cerrar el stage", current().getPosition());
        }
        consume();

        return null;
    }

    private void parseValue() {
        if (check(MongoTokenType.STRING)) {
            String val = current().getLexeme();
            consume();
        } else if (check(MongoTokenType.NUMBER)) {
            consume();
        } else if (check(MongoTokenType.TRUE) || check(MongoTokenType.FALSE) || check(MongoTokenType.NULL)) {
            consume();
        } else if (check(MongoTokenType.LBRACE)) {
            parseObject();
        } else if (check(MongoTokenType.LBRACKET)) {
            parseArray();
        } else if (check(MongoTokenType.OPERATOR)) {
            consume();
            if (check(MongoTokenType.COLON)) {
                consume();
                parseValue();
            }
        } else if (check(MongoTokenType.FIELD_PATH)) {
            consume();
        }
    }

    private void parseObject() {
        if (!expect(MongoTokenType.LBRACE)) return;
        consume();

        while (!check(MongoTokenType.RBRACE) && !check(MongoTokenType.EOF)) {
            if (check(MongoTokenType.STRING) || check(MongoTokenType.OPERATOR) || check(MongoTokenType.FIELD_PATH)) {
                consume();
                if (check(MongoTokenType.COLON)) {
                    consume();
                    parseValue();
                }
            } else {
                break;
            }

            if (check(MongoTokenType.COMMA)) {
                consume();
            } else if (!check(MongoTokenType.RBRACE)) {
                break;
            }
        }

        if (check(MongoTokenType.RBRACE)) {
            consume();
        }
    }

    private void parseArray() {
        if (!expect(MongoTokenType.LBRACKET)) return;
        consume();

        while (!check(MongoTokenType.RBRACKET) && !check(MongoTokenType.EOF)) {
            parseValue();
            if (check(MongoTokenType.COMMA)) {
                consume();
            } else if (!check(MongoTokenType.RBRACKET)) {
                break;
            }
        }

        if (check(MongoTokenType.RBRACKET)) {
            consume();
        }
    }

    private MongoToken current() {
        if (pos >= tokens.size()) return new MongoToken(MongoTokenType.EOF, "", -1);
        return tokens.get(pos);
    }

    private boolean check(MongoTokenType type) {
        return current().getType() == type;
    }

    private boolean expect(MongoTokenType type) {
        return current().getType() == type;
    }

    private void consume() {
        if (pos < tokens.size()) pos++;
    }
}
