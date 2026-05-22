package com.umg.api.compiler.mapper;

import com.umg.api.compiler.dto.*;
import com.umg.model.dialect.CompilerDialect;
import com.umg.model.dialect.DialectMapper;
import com.umg.model.lexer.ErrorLexico;
import com.umg.model.lexer.Token;
import com.umg.model.lexer.TokenType;
import com.umg.model.semantic.config.ConexionBaseDatosConfig;
import com.umg.model.semantic.result.ErrorSemantico;
import com.umg.model.semantic.result.ResultadoSemantico;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompilerResponseMapper {

    public ConexionBaseDatosConfig toConexionConfig(ConnectionConfigDto dto) {
        if (dto == null) return null;
        ConexionBaseDatosConfig config = new ConexionBaseDatosConfig();
        config.setDialecto(DialectMapper.toSqlDialect(dto.getDialect()));
        config.setHost(dto.getHost());
        config.setPuerto(dto.getPort() != null ? dto.getPort() : 0);
        config.setBaseDatos(dto.getDatabase());
        config.setEsquema(dto.getSchema());
        config.setUsuario(dto.getUsername());
        config.setPassword(dto.getPassword());
        config.setUrlJdbc(dto.getJdbcUrl());
        config.setUsarUrlJdbcDirecta(dto.getUseDirectJdbcUrl() != null ? dto.getUseDirectJdbcUrl() : false);
        config.setLocalDatacenter(dto.getLocalDatacenter());
        return config;
    }

    public ConexionBaseDatosConfig toConexionConfig(ConnectionConfigDto dto, CompilerDialect fallbackDialect) {
        ConexionBaseDatosConfig config = toConexionConfig(dto);
        if (config != null && config.getDialecto() == null) {
            config.setDialecto(DialectMapper.toSqlDialect(fallbackDialect));
        }
        return config;
    }

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

    public CompilerErrorDto toCompilerErrorDtoFromSemantic(ErrorSemantico error) {
        return new CompilerErrorDto(
            "SEMANTIC",
            error.getCodigo(),
            error.getMensaje(),
            error.getLinea(),
            error.getColumna(),
            error.getEntidad(),
            "ERROR"
        );
    }

    public List<CompilerErrorDto> toCompilerErrorDtoListFromSemantic(List<ErrorSemantico> errors) {
        return errors.stream().map(this::toCompilerErrorDtoFromSemantic).collect(Collectors.toList());
    }

    public SemanticResultDto toSemanticResultDto(ResultadoSemantico resultado) {
        if (resultado == null) return null;
        SemanticResultDto dto = new SemanticResultDto();
        dto.setValid(resultado.isValido());
        dto.setMessage(resultado.getMensaje());
        dto.setErrors(toCompilerErrorDtoListFromSemantic(resultado.getErroresSemanticos()));
        dto.setWarnings(resultado.getAdvertencias());
        dto.setValidatedObjects(resultado.getObjetosValidados());
        return dto;
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
