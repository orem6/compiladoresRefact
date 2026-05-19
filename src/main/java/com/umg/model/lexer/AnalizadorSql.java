package com.umg.model.lexer;

import com.umg.model.dialect.FunctionRegistry;
import com.umg.model.dialect.SqlDialect;
import com.umg.model.error.CompilerError;
import com.umg.model.error.ErrorCollector;
import com.umg.model.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSql {
    private SqlDialect dialectoPreferido;

    public AnalizadorSql() {
        this.dialectoPreferido = null;
    }

    public AnalizadorSql(SqlDialect dialectoPreferido) {
        this.dialectoPreferido = dialectoPreferido;
    }

    public ResultadoLexer analizar(String sql) {
        return analizar(sql, null);
    }

    public ResultadoLexer analizar(String sql, SqlDialect dialectoPreferido) {
        ResultadoLexer resultado = new ResultadoLexer();

        if (sql == null || sql.trim().isEmpty()) {
            resultado.setValido(false);
            resultado.setMensaje("La consulta SQL no puede estar vacia");
            return resultado;
        }

        ErrorCollector errorCollector = new ErrorCollector();
        Lexer lexer = new Lexer(errorCollector);
        List<Token> tokens = lexer.tokenize(sql);

        resultado.setTokens(tokens);

        SqlDialect dialectoDetectado = lexer.getDetectedDialect();
        resultado.setDialectoDetectado(dialectoDetectado);

        List<SqlDialect> compatibles = detectarDialectosCompatibles(tokens);
        resultado.setDialectosCompatibles(compatibles);

        List<ErrorLexico> erroresLexicos = convertirErrores(errorCollector.getErrors());
        resultado.setErrores(erroresLexicos);

        if (!errorCollector.hasErrors()) {
            resultado.setValido(true);
            resultado.setMensaje("Analisis lexico completado sin errores");
            resultado.setSintaxisBasicaValida(true);

            Parser parser = new Parser(errorCollector);
            parser.parse(tokens);

            if (errorCollector.hasErrors()) {
                List<ErrorLexico> erroresSintacticos = convertirErrores(errorCollector.getErrors());
                for (ErrorLexico err : erroresSintacticos) {
                    if (!resultado.getErrores().contains(err)) {
                        resultado.addError(err);
                    }
                }
                resultado.setValido(false);
                resultado.setSintaxisBasicaValida(false);
                resultado.setMensaje("Error de sintaxis en la sentencia SQL");
            } else {
                resultado.setMensaje("Sentencia SQL valida (lexica y sintacticamente)");
            }
        } else {
            resultado.setValido(false);
            resultado.setSintaxisBasicaValida(false);
            resultado.setMensaje("Error lexico en la sentencia SQL");
        }

        if (dialectoPreferido != null) {
            validarFuncionesParaDialecto(resultado, dialectoPreferido, tokens);
        }

        return resultado;
    }

    private List<SqlDialect> detectarDialectosCompatibles(List<Token> tokens) {
        List<SqlDialect> compatibles = new ArrayList<>();
        compatibles.add(SqlDialect.COMMON);

        boolean hasMySql = false;
        boolean hasPostgreSql = false;
        boolean hasSqlServer = false;

        for (Token token : tokens) {
            if (token.getDialecto() == SqlDialect.MYSQL) hasMySql = true;
            if (token.getDialecto() == SqlDialect.POSTGRESQL) hasPostgreSql = true;
            if (token.getDialecto() == SqlDialect.SQL_SERVER) hasSqlServer = true;

            if (token.getType() == TokenType.FUNCION && token.getDialecto() != null) {
                switch (token.getDialecto()) {
                    case MYSQL -> hasMySql = true;
                    case POSTGRESQL -> hasPostgreSql = true;
                    case SQL_SERVER -> hasSqlServer = true;
                }
            }
        }

        if (hasMySql) compatibles.add(SqlDialect.MYSQL);
        if (hasPostgreSql) compatibles.add(SqlDialect.POSTGRESQL);
        if (hasSqlServer) compatibles.add(SqlDialect.SQL_SERVER);

        return compatibles;
    }

    private void validarFuncionesParaDialecto(ResultadoLexer resultado, SqlDialect dialecto, List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getType() == TokenType.FUNCION) {
                String funcName = token.getLexema().toUpperCase();
                if (!FunctionRegistry.isCommonFunction(funcName) &&
                    !FunctionRegistry.isFunctionForDialect(funcName, dialecto)) {
                    resultado.addError(new ErrorLexico(
                        "E010",
                        "Funcion no soportada para el dialecto " + dialecto,
                        token.getLinea(), token.getColumna(), funcName
                    ));
                    resultado.setValido(false);
                }
            }
        }
    }

    private List<ErrorLexico> convertirErrores(List<CompilerError> errores) {
        List<ErrorLexico> result = new ArrayList<>();
        for (CompilerError err : errores) {
            String codigo = switch (err.getType().toUpperCase()) {
                case "LEXICAL" -> "E001";
                case "SYNTAX" -> "E007";
                default -> "E000";
            };
            result.add(new ErrorLexico(codigo, err.getMessage(), err.getLine(), err.getColumn(), ""));
        }
        return result;
    }
}
