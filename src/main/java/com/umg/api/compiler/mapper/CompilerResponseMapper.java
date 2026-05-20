package com.umg.api.compiler.mapper;

import com.umg.api.compiler.dto.*;
import com.umg.model.lexer.ErrorLexico;
import com.umg.model.lexer.Token;
import com.umg.model.lexer.TokenType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompilerResponseMapper {

    public TokenDto toTokenDto(Token token) {
        String typeName = mapTokenType(token.getType());
        String dialectName = token.getDialecto() != null ? token.getDialecto().name() : "COMMON";
        return new TokenDto(typeName, token.getLexema(), token.getLinea(), token.getColumna(), dialectName);
    }

    public List<TokenDto> toTokenDtoList(List<Token> tokens) {
        List<TokenDto> result = new ArrayList<>();
        for (Token t : tokens) {
            result.add(toTokenDto(t));
        }
        return result;
    }

    public CompilerErrorDto toCompilerErrorDto(ErrorLexico error, String stage, String severity) {
        return new CompilerErrorDto(
            stage,
            error.getCodigo(),
            error.getMensaje(),
            error.getLinea(),
            error.getColumna(),
            error.getLexema(),
            severity
        );
    }

    public List<CompilerErrorDto> toCompilerErrorDtoList(List<ErrorLexico> errors, String stage, String severity) {
        List<CompilerErrorDto> result = new ArrayList<>();
        for (ErrorLexico e : errors) {
            result.add(toCompilerErrorDto(e, stage, severity));
        }
        return result;
    }

    private String mapTokenType(TokenType type) {
        if (type == null) return "UNKNOWN";
        return switch (type) {
            case PALABRA_RESERVADA, KEYWORD -> "KEYWORD";
            case IDENTIFICADOR, IDENTIFIER -> "IDENTIFIER";
            case IDENTIFICADOR_DELIMITADO -> "DELIMITED_IDENTIFIER";
            case FUNCION -> "FUNCTION";
            case TIPO_DATO -> "DATA_TYPE";
            case OPERADOR, OPERATOR -> "OPERATOR";
            case OPERADOR_COMPARACION -> "COMPARISON_OPERATOR";
            case OPERADOR_LOGICO -> "LOGICAL_OPERATOR";
            case NUMERO_ENTERO, INTEGER -> "INTEGER";
            case NUMERO_DECIMAL, DECIMAL -> "DECIMAL";
            case CADENA, STRING_LITERAL -> "STRING";
            case COMENTARIO_LINEA -> "LINE_COMMENT";
            case COMENTARIO_BLOQUE -> "BLOCK_COMMENT";
            case PARENTESIS_IZQUIERDO, LPAREN -> "LEFT_PAREN";
            case PARENTESIS_DERECHO, RPAREN -> "RIGHT_PAREN";
            case COMA, COMMA -> "COMMA";
            case PUNTO, DOT -> "DOT";
            case PUNTO_Y_COMA, SEMICOLON -> "SEMICOLON";
            case ASTERISCO, STAR -> "ASTERISK";
            case PARAMETRO -> "PARAMETER";
            case PLACEHOLDER -> "PLACEHOLDER";
            case EOF, EOF_OLD -> "EOF";
            case DESCONOCIDO -> "UNKNOWN";
        };
    }
}
